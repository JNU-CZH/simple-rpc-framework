package github.chenzhihui.registry.zk;

import github.chenzhihui.enums.LoadBalanceEnum;
import github.chenzhihui.enums.RpcErrorMessageEnum;
import github.chenzhihui.exception.RpcException;
import github.chenzhihui.extension.ExtensionLoader;
import github.chenzhihui.loadbalance.LoadBalance;
import github.chenzhihui.registry.ServiceDiscovery;
import github.chenzhihui.registry.zk.util.CuratorUtils;
import github.chenzhihui.remoting.dto.RpcRequest;
import github.chenzhihui.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现中心实现类
 * 描述：使用zk作为注册中心，通过zk客户端获取服务器地址列表，使用负载均衡策略选择服务器地址
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:26
 **/
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    // 使用SPI机制，获取负载均衡策略
    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    // 实现ServiceDiscovery接口的lookupService方法
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        // 从rpcRequest中获取rpc服务名称
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 获取zk客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 获取rpc服务名称对应的服务器地址列表
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        // 如果服务器地址列表为空，则抛出异常
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // load balancing
        // 使用负载均衡策略选择服务器地址
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        // 解析服务器地址，返回InetSocketAddress对象实例
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
