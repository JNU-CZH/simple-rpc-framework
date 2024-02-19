package github.chenzhihui.loadbalance;

import github.chenzhihui.remoting.dto.RpcRequest;
import github.chenzhihui.utils.CollectionUtil;

import java.util.List;

/**
 *
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:35
 **/
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);
}
