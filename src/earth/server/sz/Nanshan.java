package earth.server.sz;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import iotsampl.iot.core.IotLogger;

/**
 * Created by Frapo on 2017/1/22.
 */
public class Nanshan {

    public void start(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(5);
        //try {
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_KEEPALIVE,false);
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                        ch.pipeline().addLast(new HttpResponseEncoder());
                        // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                        ch.pipeline().addLast(new HttpRequestDecoder());
                        // server TianjinHandler 业务处理
                        ch.pipeline().addLast(new NanshanHandler());
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, false);

        ChannelFuture f = b.bind(port).sync();
        /**
             f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();*/
        //}
    }
    public static int ShortServerPort = 9011;

    public static void main(String[] args) throws Exception {
        Nanshan server = new Nanshan();
        IotLogger.i("Short Http Server listening on " + ShortServerPort + " ...");
        server.start(ShortServerPort);
        // 创建Service()/Daemon
    }

    public static void main(int a) throws Exception {
        ShortServerPort = a;
        Nanshan server = new Nanshan();
        server.start(ShortServerPort);
        IotLogger.i("EE30: Http Server started on " + ShortServerPort + " ...");
        // 创建Service()/Daemon
    }

    public static void service() {
        /*
        *   核心功能：
        *   1. 监测
        * */
    }
}
