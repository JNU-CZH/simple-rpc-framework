package github.chenzhihui.loadbalance.loadbalancer;

import github.chenzhihui.loadbalance.AbstractLoadBalance;
import github.chenzhihui.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性哈希负载均衡策略
 * 描述：很好的处理节点动态加入和退出问题，适合节点动态变化的场景
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:37
 **/
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    // 存储每个服务对应的一致性哈希选择器
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    // 实现AbstractLoadBalance的抽象类中的doSelect方法
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        // 获取服务地址列表的系统标识哈希码
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // 通过rpcRequest构建RPC服务名称
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 从选择器映射中获取对应的一致性哈希选择选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // check for updates
        // 检查是否需要更新选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            // 创建新的一致性哈希选择器并存入映射中
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        // 使用选择器选择一个服务地址
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;

        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        public String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();

            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}
