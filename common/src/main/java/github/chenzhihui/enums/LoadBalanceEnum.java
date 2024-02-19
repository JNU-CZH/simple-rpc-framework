package github.chenzhihui.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 负载均衡策略枚举
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:30
 **/
@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {

    LOADBALANCE("loadBalance");

    private final String name;
}
