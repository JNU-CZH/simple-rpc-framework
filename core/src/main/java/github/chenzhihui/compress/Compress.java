package github.chenzhihui.compress;

import github.chenzhihui.extension.SPI;

/**
 * compress interface是一个压缩接口，定义了两个方法，分别是压缩和解压缩
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 20:13
 **/

@SPI
public interface Compress {


    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);

}
