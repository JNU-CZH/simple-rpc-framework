package github.chenzhihui.utils;

/**
 * String 工具类
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 19:57
 **/
public class StringUtil {

    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
