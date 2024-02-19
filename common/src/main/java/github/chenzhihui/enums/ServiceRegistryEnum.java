package github.chenzhihui.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:21
 **/

@AllArgsConstructor
@Getter
public enum ServiceRegistryEnum {

    ZK("zk");

    private final String name;
}
