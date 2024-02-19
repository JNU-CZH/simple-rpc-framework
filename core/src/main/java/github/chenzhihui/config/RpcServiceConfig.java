package github.chenzhihui.config;

import lombok.*;

/**
 * RPC服务配置信息类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:14
 **/

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {

    /**
     * 服务版本
     */
    private String version = "";
    /**
     * 当接口有多个实现类时，通过group进行区分
     */
    private String group = "";

    /**
     * 目标服务
     */
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }

}
