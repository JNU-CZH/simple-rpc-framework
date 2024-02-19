package github.chenzhihui.remoting.handler;

import github.chenzhihui.exception.RpcException;
import github.chenzhihui.factor.SingletonFactory;
import github.chenzhihui.provider.ServiceProvider;
import github.chenzhihui.provider.impl.ZkServiceProviderImpl;
import github.chenzhihui.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description: 处理RPC请求
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:51
 **/
@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 处理rpc请求：调用对应的方法，然后返回方法
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 获得方法执行结果
     *
     * @param rpcRequest 客户端请求
     * @param service    服务器对象
     * @return 目标方法执行的结果
     * */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
