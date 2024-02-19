package github.chenzhihui.extension;

/**
 * Holder类: 用于持有对象
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 20:08
 **/
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
