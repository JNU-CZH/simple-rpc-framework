package github.chenzhihui.remoting.transport.netty.codec;

import github.chenzhihui.compress.Compress;
import github.chenzhihui.enums.CompressTypeEnum;
import github.chenzhihui.enums.SerializationTypeEnum;
import github.chenzhihui.extension.ExtensionLoader;
import github.chenzhihui.remoting.constants.RpcConstants;
import github.chenzhihui.remoting.dto.RpcMessage;
import github.chenzhihui.remoting.dto.RpcRequest;
import github.chenzhihui.remoting.dto.RpcResponse;
import github.chenzhihui.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 自定义解码器：解码RPC消息
 * 功能：继承LengthFieldBasedFrameDecoder，用于解决TCP粘包和拆包问题
 * 描述：采用基于长度的解码器：通过消息头中的长度字段来获取消息体的长度，从而解决TCP粘包和拆包问题
 * 1、魔数：筛选来到服务端的数据包，识别出该数据包是否遵循自定义协议，防止无效数据包，保证数据传输的安全性
 *
 *  * custom protocol decoder
 *  * <pre>
 *  *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *  *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *  *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *  *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  *   |                                                                                                       |
 *  *   |                                         body                                                          |
 *  *   |                                                                                                       |
 *  *   |                                        ... ...                                                        |
 *  *   +-------------------------------------------------------------------------------------------------------+
 *  * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *  * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *  * body（object类型数据）
 *  * </pre>
 *  * <p>
 *  * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 *  * </p>
 *
 *
 * @Author: ChenZhiHui
 * @DateTime: 2024/2/18 20:18
 **/

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    public RpcMessageDecoder() {
        // lengthFieldOffset: 魔数是4B，版本是1B，然后是全长。所以值是5
        // lengthFieldLength: 全长是4B。所以值是4
        // lengthAdjustment: 全长包括所有数据并在之前读取9字节，所以剩下的长度是(fullLength-9)。所以值是-9
        // initialBytesToStrip: 我们将手动检查魔数和版本，所以不剥离任何字节。所以值是0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      最大帧长度。它决定了可以接收的数据的最大长度。
     *                            如果超过，数据将被丢弃。
     * @param lengthFieldOffset   长度字段偏移。长度字段是跳过指定长度的字节的那个字段。
     * @param lengthFieldLength   长度字段中的字节数。
     * @param lengthAdjustment    要添加到长度字段值的补偿值
     * @param initialBytesToStrip 跳过的字节数。
     *                            如果你需要接收所有的头+体数据，这个值是0
     *                            如果你只想接收体数据，那么你需要跳过头消耗的字节数。
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }

    /**
     * 解码帧
     * @param in 输入的ByteBuf
     * @return 解码后的对象
     */
    private Object decodeFrame(ByteBuf in) {
        // note: must read ByteBuf in order
        // 注意：必须按顺序读取ByteBuf
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // 构建RpcMessage对象
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType)
                .compress(compressType).build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // decompress the bytes
            // 解压缩字节
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            // deserialize the object
            // 反序列化对象
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;

    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

}
