package github.chenzhihui.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * RPC常量
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 11:09
 **/
public class RpcConstants {

    /**
     * 魔数，用于校验RpcMessage
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};

    // 默认字符集
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 协议版本号
    public static final byte VERSION = 1;
    // 总长度
    public static final byte TOTAL_LENGTH = 16;
    // 请求类型
    public static final byte REQUEST_TYPE = 1;
    // 响应类型
    public static final byte RESPONSE_TYPE = 2;
    // 心跳请求类型（检测连接是否有效）
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    // 心跳响应类型
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    // 头部长度（元信息：版本号、消息长度等）
    public static final int HEAD_LENGTH = 16;
    // 心跳请求消息
    public static final String PING = "ping";
    public static final String PONG = "pong";
    // 最大帧长度
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
