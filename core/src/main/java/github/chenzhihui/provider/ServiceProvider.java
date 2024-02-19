package github.chenzhihui.provider;

import github.chenzhihui.config.RpcServiceConfig;

/**
 * 服务提供者接口
 * 描述：添加服务、获取服务、发布服务
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:10
 **/
public interface ServiceProvider {

    /**
     * 添加服务
     * @param rpcServiceConfig rpc服务相关的属性，包含服务对象、服务类等信息
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务
     * @param rpcServiceName rpc服务名称，通常是接口等全限定名
     * @return 服务对象，用于执行具体的服务方法
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务，通常是将服务注册到注册中心，让客户端能够发现
     * @param rpcServiceConfig rpc相关的属性，包含服务对象、服务类等信息
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
