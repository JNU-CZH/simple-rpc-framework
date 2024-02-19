package github.chenzhihui.remoting.transport.netty.client;

import github.chenzhihui.enums.CompressTypeEnum;
import github.chenzhihui.enums.SerializationTypeEnum;
import github.chenzhihui.factor.SingletonFactory;
import github.chenzhihui.remoting.constants.RpcConstants;
import github.chenzhihui.remoting.dto.RpcMessage;
import github.chenzhihui.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Netty客户端处理器：处理服务器发送数据
 * 描述：继承ChannelInboundHandlerAdapter，处理网络事件
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 20:32
 **/

@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    // 存储未处理的请求
    private final UnprocessedRequests unprocessedRequests;
    // Netty客户端实例
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    // 读取服务端返回的消息
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage) { // 判断消息是否是RpcMessage类型
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    // 心跳类型：打印心跳数据
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    // 响应类型：获取响应数据，并将其从未处理的请求中移除
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            // 双方消息对象，防止内存泄露
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * Netty心跳机制相关
     * 描述：保证客户端和服务端的连接不被断掉，避免重连
     * userEventTriggered是ChannelInboundHandlerAdapter的一个方法，用于处理用户事件（读空闲、写空闲、读写空闲）
     *
     * @param ctx 接口，提供许多操作：获取channel、获取pipeline、获取channelHandler等 该方法中主要用来获取当前的channel
     * @param evt 事件类型，用于判断是否是IdleStateEvent：IdleStateEvent在channel进行空闲的时候会被触发
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) { // 判断事件类型是否是IdleStateEvent
            // 获取状态
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) { // 如果是空闲状态，发送心跳请求
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            // 如果事件类型不是IdleStateEvent，调用父类的方法
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }

}
