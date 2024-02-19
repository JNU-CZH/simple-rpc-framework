package github.chenzhihui.registry.zk;

import github.chenzhihui.registry.ServiceRegistry;
import github.chenzhihui.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * 服务注册中心实现类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:24
 **/

@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry {


    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        // 创建服务路径
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        // 获取Zookeeper客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        // 创建持久节点（将服务注册到Zookeeper）
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
