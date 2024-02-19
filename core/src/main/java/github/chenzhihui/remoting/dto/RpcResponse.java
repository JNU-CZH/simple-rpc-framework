package github.chenzhihui.remoting.dto;

import lombok.*;

import java.io.Serializable;
import github.chenzhihui.enums.RpcResponseCodeEnum;

/**
 * RPC响应实体类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/1/14 18:42
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    /**
     * 请求Id
     * */
    private String requestId;

    /**
     * 响应码
     * */
    private Integer code;

    /**
     * 响应消息
     * */
    private String message;

    /**
     * 响应体
     * */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        if (data != null) {
            response.setData(data);
        }
        return response;
    }


    public static <T> RpcResponse<T> fail() {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.FAIL.getCode());
        response.setMessage(RpcResponseCodeEnum.FAIL.getMessage());
        return response;
    }



}
