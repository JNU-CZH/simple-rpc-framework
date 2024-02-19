package github.chenzhihui.exception;

import github.chenzhihui.enums.RpcErrorMessageEnum;

/**
 * @
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:25
 **/
public class RpcException extends RuntimeException {

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

}
