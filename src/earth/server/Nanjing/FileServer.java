package earth.server.Nanjing;

import earth.server.Constant;
import earth.server.Monitor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by God on 2017/2/11.
 */
public class FileServer extends Thread {

    //单实例
    private static FileServer dbServer = null;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
    //创建实例
    public static FileServer newBuild() {
        if(dbServer == null) {
            dbServer = new FileServer();
        }
        return dbServer;
    }

    public void run() {
        try {
            startServer();
        } catch(Exception e) {
            System.out.println("数据服务启动出现异常："+e.toString());
            e.printStackTrace();
        }
    }

    private void startServer() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.SO_TIMEOUT, 30000);
            b.option(ChannelOption.SO_SNDBUF, 1048576*200);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new FRServerInitializer());
            // 服务器绑定端口监听
            ChannelFuture f = b.bind(Constant.File_SERVER_Port).sync();
            Monitor.logger("数据服务：" + Constant.File_SERVER_Port +"启动完成...");
            // 监听服务器关闭监听
            //f.channel().close().sync();
        } finally {
            //bossGroup.shutdownGracefully();
            //workerGroup.shutdownGracefully();
        }
    }

    public static void stopper(){
        dbServer.bossGroup.shutdownGracefully();
        dbServer.workerGroup.shutdownGracefully();
    }
}