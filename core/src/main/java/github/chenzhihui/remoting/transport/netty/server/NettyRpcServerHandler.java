package github.chenzhihui.remoting.transport.netty.server;

import github.chenzhihui.enums.CompressTypeEnum;
import github.chenzhihui.enums.RpcResponseCodeEnum;
import github.chenzhihui.enums.SerializationTypeEnum;
import github.chenzhihui.factor.SingletonFactory;
import github.chenzhihui.remoting.constants.RpcConstants;
import github.chenzhihui.remoting.dto.RpcMessage;
import github.chenzhihui.remoting.dto.RpcRequest;
import github.chenzhihui.remoting.dto.RpcResponse;
import github.chenzhihui.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器处理器：处理客户端请求
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:49
 **/
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    /**
     * 构造函数，初始化RpcRequestHandler
     */
    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * 读取客户端发送的消息，并进行处理
     * @param ctx ChannelHandlerContext
     * @param msg 客户端发送的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // 检查消息是否为RpcMessage类型
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                // 获取消息类型
                byte messageType = ((RpcMessage) msg).getMessageType();
                // 创建一个新的RpcMessage对象
                RpcMessage rpcMessage = new RpcMessage();
                // 设置编解码方式
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                // 设置压缩格式为GZIP
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                // 判断消息类型是否为心跳类型
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    // 如果消息是心跳类型，设置消息类型为心跳类型，并设置数据为PONG
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    // 如果消息不是心跳类型，获取RpcRequest对象
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // 处理RpcRequest对象，获取执行结果
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    // 设置消息类型为响应类型
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    // 检查Channel是否可用和是否可写
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        // 如果活跃并可写，创建一个成功的RpcResponse对象，并设置数据
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        // 如果不活跃或不可写，创建一个失败的RpcResponse对象，并设置数据
                        RpcResponse<Object> rpcResponse = RpcResponse.fail();
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                // 将RpcMessage写入Channel并刷新，添加监听器
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            //Ensure that ByteBuf is released, otherwise there may be memory leaks
            // 确保ByteBuf被释放，否则可能导致内存泄露
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 用户事件触发，主要用于处理读空闲事件
     * @param ctx ChannelHandlerContext
     * @param evt 触发的事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常捕获，当出现异常时关闭ChannelHandlerContext
     * @param ctx ChannelHandlerContext
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
