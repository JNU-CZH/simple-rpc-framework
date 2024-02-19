package github.chenzhihui.provider.impl;

import github.chenzhihui.config.RpcServiceConfig;
import github.chenzhihui.enums.RpcErrorMessageEnum;
import github.chenzhihui.enums.ServiceRegistryEnum;
import github.chenzhihui.exception.RpcException;
import github.chenzhihui.extension.ExtensionLoader;
import github.chenzhihui.provider.ServiceProvider;
import github.chenzhihui.registry.ServiceRegistry;
import github.chenzhihui.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务提供者实现类
 * 描述：用于管理和提供RPC服务
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:17
 **/

@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {


    /**
     * 存储RPC服务的映射。
     * key: rpc服务名称（接口名称 + 版本 + 组）
     * value: 服务对象
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    /***
     * 构造函数：初始化服务映射、已注册服务集合、服务注册中心
     */
    /
    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }


    /**
     * 添加服务。
     * @param rpcServiceConfig rpc服务相关的属性，包括服务对象、服务类等信息
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        // 获取rpc服务名称
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        // 如果已注册服务集合中包含该服务名称，则直接返回
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }


    /**
     * 获取服务。
     * @param rpcServiceName rpc服务名称，通常是接口的全限定名
     * @return 服务对象，用于执行具体的服务方法
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 发布服务，通常是将服务注册到注册中心，让客户端能够发现。
     * @param rpcServiceConfig rpc服务相关的属性，包括服务对象、服务类等信息
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            // 获取本地主机地址
            String host = InetAddress.getLocalHost().getHostAddress();
            // 添加服务
            this.addService(rpcServiceConfig);
            // 将服务注册到注册中心
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }

    }
}
