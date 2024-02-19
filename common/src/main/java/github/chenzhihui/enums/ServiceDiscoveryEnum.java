package github.chenzhihui.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务发现枚举类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 21:10
 **/

@AllArgsConstructor
@Getter
public enum ServiceDiscoveryEnum {

    ZK("zk");

    private final String name;

}
