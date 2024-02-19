package github.chenzhihui.utils;

/**
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:46
 **/
public class RuntimeUtil {

    /**
     * 获取CPU的核心数
     *
     * @return cpu的核心数
     */
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
