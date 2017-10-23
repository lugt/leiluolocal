package earth.server.tianjin.Util;

/**
 * Created by Frapo on 2017/1/24.
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ProtocolUtil {

    /**
     * 编码报文的数据部分        TODO: 优化为特定格式
     *
     * @param encode
     * @param values
     * @return
     */
    public static ByteBuf encode(int encode, Map<String, String> values) {

        ByteBuf totalBuffer = null;

        if (values != null && values.size() > 0) {

            int tlength = 0;

            Charset charset = Charset.forName("utf-8");

            /**
             * TODO: 此处会严重拖慢性能，考虑优化
             * */
            for (Map.Entry<String, String> entry : values.entrySet()) {
                tlength += entry.getKey().length() + entry.getValue().length() + 2 * Integer.BYTES;
            }

            totalBuffer = ByteBufAllocator.DEFAULT.ioBuffer(tlength);

            for (Map.Entry<String, String> entry : values.entrySet()) {
                // 会导致变量的复制，转移
                String key = entry.getKey();
                String value = entry.getValue();
                totalBuffer.writeInt(key.length());
                totalBuffer.writeBytes(key.getBytes(charset));
                totalBuffer.writeInt(value.length());
                totalBuffer.writeBytes(value.getBytes(charset));
            }
        }
        return totalBuffer;
    }

    /**
     * 对于非固定的格式进行decode(Bytes -> Map) 对于固定格式应予以进行优化
     * TODO:优化为特定格式（？可保留，为升级保证）
     *
     * @param encode
     * @param dataBuffer
     * @return
     */
    public static Map<String, String> decode(int encode, ByteBuf dataBuffer) {

        Map<String, String> dataMap = new HashMap<String, String>();

        if (dataBuffer != null && dataBuffer.readableBytes() > 0) {

            int processIndex = 0, length = dataBuffer.readableBytes();

            Charset charset = Charset.forName("utf-8");

            while (processIndex < length) {
                /**
                 * 获取Key
                 */
                int size = dataBuffer.readInt();

                byte[] contents = new byte[size];

                dataBuffer.readBytes(contents);

                String key = new String(contents, charset);

                processIndex = processIndex + size + Integer.BYTES;

                /**
                 * 获取Value
                 */
                size = dataBuffer.readInt();

                contents = new byte[size];

                dataBuffer.readBytes(contents);

                String value = new String(contents, charset);

                dataMap.put(key, value);

                processIndex = processIndex + size + Integer.BYTES;
            }// end while
        }// end if
        return dataMap;
    }

    /**
     * 获取客户端IP
     *
     * @param channel
     * @return
     */
    public static String getClientIp(Channel channel) {
        /**
         * 获取客户端IP
         */
        SocketAddress address = channel.remoteAddress();
        String ip = "";
        if (address != null) {
            ip = address.toString().trim();
            int index = ip.lastIndexOf(':');
            if (index < 1) {
                index = ip.length();
            }
            ip = ip.substring(1, index);
        }
        if (ip.length() > 15) {
            ip = ip.substring(Math.max(ip.indexOf("/") + 1, ip.length() - 15));
        }
        return ip;
    }
}
