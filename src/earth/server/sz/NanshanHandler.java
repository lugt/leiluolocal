package earth.server.sz;

/**
 * Created by Frapo on 2017/1/22.
 */

import Radnor.Utils.RadnorParse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import iotsampl.iot.cloud.IotApi;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.cloud.IotQuery;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NanshanHandler extends ChannelInboundHandlerAdapter {

    private HttpRequest request;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            String uri = request.uri();
            //Monitor.access(request.method() + " - Uri: " + uri);
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            // 在此处判断
            /**
             *  未进行任何操作
             *  @apiNote 请求错误1001
             * */
            String res = null;
            String uri = request.uri();
            int i = 0;
            String q;
            if ((i = uri.indexOf('?')) > -1 && uri.length() > i + 1) {
                q = uri.substring(i + 1);
                //buf = ByteBufAllocator.DEFAULT.ioBuffer(q.getBytes("UTF-8").length);
                //buf.writeBytes(q.getBytes("UTF-8"));
            } else {
                //buf = content.content();
                q = null;
            }

            try {
                if (content.content().readableBytes() > 1024000) {
                    // Bigger Than 1MB
                    res = "Exp,9001";
                } else if (uri.startsWith("/m/api.do?stop")) {
                    System.exit(0);
                    res="ok";
                } else if (uri.startsWith("/data/deliver")) {
                    res = (String) Class.forName("Radnor.Utils.RadnorParse").getMethod("deliver", String.class, ByteBuf.class).invoke(null, q, content.content());
                } else if (uri.startsWith("/m/api.do")) {
                    res = (String) Class.forName("iotsampl.iot.cloud.IotApi").getMethod("distributeCall", String.class, ByteBuf.class).invoke(null, q, content.content());
                } else if (uri.startsWith("/cloud/deliver.do")) {
                    res = (String) Class.forName("iotsampl.iot.cloud.IotQuery").getMethod("deliver", String.class, ByteBuf.class).invoke(null, q, content.content());
                } else if (uri.equals("/")) {
                    res = "欢迎 - Welcome - Bienvenue";
                } else {
                    res = "Exp,1001";
                }
            }catch (Exception fe){
                fe.printStackTrace();
            }
            content.content().release();

            if (res == null) {
                res = "Exp,1002";
            }

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    OK, Unpooled.wrappedBuffer(res.getBytes("UTF-8")));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
            //response.headers().set(CONTENT_ENCODING,"");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                    response.content().readableBytes());
            //if (Values.KEEP_ALIVE.equals(request.headers().get("Connection"))) {
            //response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            //}
            ctx.write(response);
            ctx.flush();
            ctx.close();

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(!(cause.getMessage().startsWith("远程主机")) && !(cause.getMessage().startsWith("Connection"))) {
            IotLogger.i(cause.getMessage());
            cause.printStackTrace();
        }
        ctx.close();
    }

}

/*
*
              if (uri.startsWith("/sign/auto")) {
                    // 登录态恢复（本地token验证） Session 恢复
                    res = (new UserLogin()).reborn(buf);

                } else if (uri.startsWith("/sign/etid")) {
                    // 登录 通过
                    res = (new UserLogin()).etid(buf);

                } else if (uri.startsWith("/sign/phone")) {
                    // 登录 通过
                    res = (new UserLogin()).phone(buf);

                } else if (uri.startsWith("/exit")) {
                    // 登录 通过
                    res = (new UserLogin()).exit(buf);

                } else if (uri.startsWith("/basic/me")) {
                    // 用户信息
                    res = (new UserInfo()).basic(buf);

                } else if (uri.startsWith("/avatar/get")) {
                    // 指定用户头像
                    res = "Exp,-100014";

                } else if (uri.startsWith("/reg")) {
                    // 注册
                    res = (new UserReg()).create(buf);

                } else if (uri.startsWith("/id/get")) {

                    res = (new UserInfo()).getIdentity(buf);

                } else if (uri.startsWith("/id/verify")) {
                    // 身份认证

                    res = new UserInfo().verify(buf);

                } else if (uri.startsWith("/friend/list")) {
                    //获取
                    res = (new FriendExecutor()).getList(buf);

                } else if (uri.startsWith("/friend/add")) {
                    // 添加好友
                    res = (new FriendExecutor()).add(buf);

                } else if (uri.startsWith("/friend/remove")) {
                    // 添加好友
                    res = (new FriendExecutor()).remove(buf);

                }else if (uri.startsWith("/welcome")) {

                    String[] m = new String[6];
                    m[0] = "W";
                    m[1] = DataService.ServerV;
                    m[2] = DataService.ClientV;
                    m[3] = DataService.ProtoV;
                    m[4] = ((Long) (System.currentTimeMillis() / 1000)).intValue() + "";
                    m[5] = DataService.SecureEnforce;
                    res = String.join(",", m);

                } else if (uri.startsWith("/test")) {
                    // Session 持续
                    res = "T,ok,"+buf.toString(Charset.forName("UTF-8"));

                } else if (uri.startsWith("/search/cell")) {
                    // 搜索朋友
                    // 通过手机号/ETID
                    res = new UserInfo().getPublicCell(buf);

                } else if (uri.startsWith("/basic/etid")) {
                    // 通过/ETID 查看基本信息
                    res = new UserInfo().getPublicEtid(buf);

                } else if (uri.startsWith("/search/topic")) {
                    // 搜索
                    res="Top,1001";

                } else if (uri.startsWith("/update")) {

                    res= UpdateManager.getUpdate();

                } else if (uri.startsWith("/feedback")) {

                    res = UpdateManager.setLog(buf);

                } else if (uri.startsWith("/kiosk/get")) {
                    // 通过智能问答来断定真假，确保问题不重复
                    res = (new KioskExecutor()).getQ("CN");
                } else
*
* */