package github.chenzhihui.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * RPC请求实体类
 * 描述：远程调用方法的时候，需要传输一个RPCRequest给对方，RPCRequest中包含了目标方法名、参数、参数类型等信息
 * 补充：version为后续不兼容升级提供可能；group主要用于处理一个接口有多个类实现的情况
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
