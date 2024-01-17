package github.chenzhihui.exception;

/**
 * 序列化异常提示类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/1/14 18:54
 **/
public class SerializeException extends RuntimeException{

    public SerializeException(String message) {
        super((message));
    }

}
