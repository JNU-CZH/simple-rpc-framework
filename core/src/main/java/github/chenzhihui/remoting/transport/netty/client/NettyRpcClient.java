package github.chenzhihui.remoting.transport.netty.client;

import github.chenzhihui.enums.CompressTypeEnum;
import github.chenzhihui.enums.SerializationTypeEnum;
import github.chenzhihui.enums.ServiceDiscoveryEnum;
import github.chenzhihui.extension.ExtensionLoader;
import github.chenzhihui.factor.SingletonFactory;
import github.chenzhihui.registry.ServiceDiscovery;
import github.chenzhihui.remoting.constants.RpcConstants;
import github.chenzhihui.remoting.dto.RpcMessage;
import github.chenzhihui.remoting.dto.RpcRequest;
import github.chenzhihui.remoting.dto.RpcResponse;
import github.chenzhihui.remoting.transport.RpcRequestTransport;
import github.chenzhihui.remoting.transport.netty.codec.RpcMessageDecoder;
import github.chenzhihui.remoting.transport.netty.codec.RpcMessageEncoder;
import github.chenzhihui.serialize.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * RPC客户端：通过Netty网络框架与服务器进行通信
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/1/17 21:51
 **/

@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {

    // 服务发现
    private final ServiceDiscovery serviceDiscovery;
    // 处理未处理的请求
    private final UnprocessedRequests unprocessedRequests;
    // 提供通道
    private final ChannelProvider channelProvider;
    // Netty的启动类
    private final Bootstrap bootstrap;
    // Netty的事件循环组
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        // 初始化资源，如EventLoopGroup、Bootstrap
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        // 设置Netty客户端的线程模型、IO模型、日志、连接超时时间、编解码器、处理器
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //  The timeout period of the connection.
                //  If this time is exceeded or the connection cannot be established, the connection fails.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // If no data is sent to the server within 15 seconds, a heartbeat request is sent
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * 连接服务器（目标方法所在服务器）并获取通道channel，以便可以向服务器发送RPC消息。
     * 描述：有了channel便可以发送数据到服务端
     *
     * @param inetSocketAddress 服务器地址：如127.0.0.1:8080
     * @return 通道
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        // 创建一个CompletableFuture对象，用于异步计算结果
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 使用Bootstrap连接服务器
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            // 如果连接成功
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                // 将连接的通道作为CompletableFuture的结果
                completableFuture.complete(future.channel());
            } else {
                // 连接失败，则抛出异常
                throw new IllegalStateException();
            }
        });
        // 获取并返回CompletableFuture的结果，如果结果还未计算完成，阻塞
        return completableFuture.get();
    }

    // 发送RPC请求到服务器，并获取服务器响应
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 创建一个CompletableFuture对象，用于异步计算结果
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // 通过服务发现组件查找服务器地址
        // todo：如果获得服务器的地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // 判断通道是否活跃
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // 将未处理的请求放入unprocessedRequests中
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            // 创建RPCMessage对象，并设置相关属性
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    // todo：设置请求ID（是否有必要）
                    .requestId(Integer.valueOf(rpcRequest.getRequestId())).build();
            // 将RpcMessage对象写入通道，并添加监听器处理发送结果
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) { // 如果发送成功，记录日志
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    // 如果发送失败，关闭通道，完成异常结果，并记录错误日志
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            // 如果通道不活跃，抛出异常
            throw new IllegalStateException();
        }
        // 返回CompletableFuture结果
        return resultFuture;
    }

    // 获取通道
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            // 如果没有通道，则创建地址对应的通道
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
