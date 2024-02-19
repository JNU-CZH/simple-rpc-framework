package github.chenzhihui.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 读取属性文件
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/19 16:43
 **/
@Slf4j
public final class PropertiesFileUtil {

    private PropertiesFileUtil() {
    }

    /**
     * 读取属性文件并返回一个Properties对象。
     * @param fileName 属性文件的文件名
     * @return 包含属性文件内容的Properties对象
     */
    public static Properties readPropertiesFile(String fileName) {
        // 获取当前线程的类加载器的资源路径
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            // 拼接得到完整的属性文件路径
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            // 创建Properties对象并加载属性文件
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }
}
