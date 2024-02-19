package github.chenzhihui.loadbalance;

import github.chenzhihui.extension.SPI;
import github.chenzhihui.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略接口
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:28
 **/
@SPI
public interface LoadBalance {

    /**
     * 从服务列表中选择一个服务
     *
     * @param serviceUrlList 服务地址列表
     * @return 服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);

}
