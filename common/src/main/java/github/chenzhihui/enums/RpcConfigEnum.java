package github.chenzhihui.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:43
 **/
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
