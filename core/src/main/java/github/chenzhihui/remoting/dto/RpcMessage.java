package github.chenzhihui.remoting.dto;

import lombok.*;

/**
 * RPC消息格式类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 10:51
 **/

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * rpc消息类型
     */
    private byte messageType;
    /**
     * 序列化方式
     */
    private byte codec;
    /**
     * 压缩格式
     */
    private byte compress;
    /**
     * 请求id
     */
    private int requestId;
    /**
     * 请求数据
     */
    private Object data;

}
