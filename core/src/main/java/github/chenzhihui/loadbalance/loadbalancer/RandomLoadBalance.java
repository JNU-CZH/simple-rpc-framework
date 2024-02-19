package github.chenzhihui.loadbalance.loadbalancer;

import github.chenzhihui.loadbalance.AbstractLoadBalance;
import github.chenzhihui.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡策略
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:34
 **/
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
