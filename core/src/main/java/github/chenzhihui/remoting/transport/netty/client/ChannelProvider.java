package github.chenzhihui.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储和获取Channel对象
 * 描述：channel用于在服务器和客户端之间传输数据
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 10:44
 **/

@Slf4j
public class ChannelProvider {

    // key：服务器地址、value：channel对象
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    // 获取指定服务器地址相关的channel对象
    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // determine if there is a connection for the corresponding address
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // if so, determine if the connection is available, and if so, get it directly
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    // 将服务器地址和channel对象放入map中
    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    // 将服务器地址相关的channel对象从map中移除
    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }

}
