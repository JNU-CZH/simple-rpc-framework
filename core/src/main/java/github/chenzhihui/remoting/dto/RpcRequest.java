package github.chenzhihui.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * RPC请求类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/1/14 18:38
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;

    private String requestId;

    private String interfaceName;

    private String methodName;

    private Object[] parameters;

    private Class<?>[] paramTypes;

    private String version;

    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.group + this.getVersion();
    }
}
