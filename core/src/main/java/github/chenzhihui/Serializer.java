package github.chenzhihui;

import github.chenzhihui.extension.SPI;

/**
 * 自定义序列化技术选型接口：Kryo、Protostuff、hessian等
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/1/14 17:19
 **/
@SPI
public interface Serializer {

    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 字节数组
     * */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>  类的类型，eg：{@code String.class}的类型是{@code Class<String>}
     *             如果不知道类的类型，使用{@code Class<?>}
     * @return 反序列化的对象
     * */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
