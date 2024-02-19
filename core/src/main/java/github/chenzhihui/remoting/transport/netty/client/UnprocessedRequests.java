package github.chenzhihui.remoting.transport.netty.client;

import github.chenzhihui.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放未被服务器处理的请求
 * 描述：建议限制map容器大小，避免未处理的请求过多，导致OOM
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 10:38
 **/
public class UnprocessedRequests {

    // key：请求id、value：CompletableFuture对象
    // 目的：用于存储客户端发送给服务器的请求，但服务器还未处理的响应
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    // 将请求id和cf对象放入map中
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    // 将服务器处理完的响应放入cf对象中
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
