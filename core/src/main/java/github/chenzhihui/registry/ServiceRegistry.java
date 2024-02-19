package github.chenzhihui.registry;

import github.chenzhihui.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/17 22:10
 **/

@SPI
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param rpcServiceName    rpc服务名
     * @param inetSocketAddress 服务地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
