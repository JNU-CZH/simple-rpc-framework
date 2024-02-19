package github.chenzhihui.registry;

import github.chenzhihui.extension.SPI;
import github.chenzhihui.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 21:51
 **/
@SPI
public interface ServiceDiscovery {

    /**
     * 按rpcServiceName查找服务
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
