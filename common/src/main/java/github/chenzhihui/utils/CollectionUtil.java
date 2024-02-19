package github.chenzhihui.utils;

import java.util.Collection;

/**
 * 集合工具类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 23:31
 **/
public class CollectionUtil {

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

}
