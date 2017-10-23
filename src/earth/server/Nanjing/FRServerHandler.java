package earth.server.Nanjing;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import earth.server.Constant;
import earth.server.Monitor;
import earth.server.utils.Verifier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.stomp.StompHeaders.CONTENT_TYPE;

/**
 * Created by God on 2017/2/11.
 */
public class FRServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private String uri = null;

    private HttpRequest request = null;

    private HttpPostRequestDecoder decoder;

    //message、download、upload
    private String type = "message";

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    static {
        DiskFileUpload.baseDirectory = Constant.fileUploadDir;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            uri = sanitizeUri(request.uri());
            if (request.method() == HttpMethod.POST) {
                if(uri.startsWith("/facial/pic")){
                    if(execute_facial(ctx,uri,(HttpRequest)msg)<0){
                        return;
                    }
                }else{
                    EerrorX(ctx,"functional");
                    ctx.channel().close();
                    return;
                }

                if (decoder != null) {
                    decoder.cleanFiles();
                    decoder = null;
                }
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (Exception e) {
                    Eerror(ctx,e);
                    ctx.channel().close();
                    return;
                }
            }else{
                if(uri != null && uri.startsWith("/te")){
                    writeResponse(ctx.channel(),HttpResponseStatus.OK,"<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "<script src=\"/jquery/jquery-1.11.1.min.js\"></script>\n" +
                            "<script>\n" +
                            "$(document).ready(function(){\n" +
                            "  $(\"button\").click(function(){\n" +
                            "    $.post(\"/uploadfile\",function(data,status){\n" +
                            "      alert(\"数据：\" + data + \"\\n状态：\" + status);\n" +
                            "    });\n" +
                            "  });\n" +
                            "});\n" +
                            "</script>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "\n" +
                            "<button>向页面发送 HTTP GET 请求，然后获得返回的结果</button>\n" +
                            "\n" +
                            "<h1>File API Demo</h1> \n" +
                            " <p> \n" +
                            " <!-- 用于文件上传的表单元素 --> \n" +
                            " <form name=\"demoForm\" id=\"demoForm\" method=\"post\" enctype=\"multipart/form-data\" \n" +
                            " action=\"/facial/pic?2011,2,2\"> \n" +
                            " <p>Upload File: <input type=\"file\" name=\"file\" /></p> \n" +
                            " <p><input type=\"submit\" value=\"Submit\" /></p> \n" +
                            " </form> \n" +
                            " <div>Progessing (in Bytes): <span id=\"bytesRead\"> \n" +
                            " </span> / <span id=\"bytesTotal\"></span> \n" +
                            " </div> \n" +
                            " </p> "+
                            "</body>\n" +
                            "</html>\n");
                }else{
                    writeResponse(ctx.channel(),HttpResponseStatus.OK,"EarthFileServer,welcome");
                }
            }
        }

        if (decoder != null && msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;
            try {
                decoder.offer(chunk);
            } catch (Exception e) {
                Eerror(ctx,e);
                ctx.channel().close();
                return;
            }

            readHttpDataChunkByChunk();

            if (chunk instanceof LastHttpContent) {
                finishingRead(ctx);
                reset();
                ctx.channel().close();
            }
        }
    }

    private int execute_facial(ChannelHandlerContext ctx, String uri, HttpRequest msg) {
        int i = 0;
        if((i = uri.indexOf('?')) > -1 && uri.length() > i) {
            String q = uri.substring(i+1);
            String[] m = q.split(",");
            if(m.length != 3){
                EerrorX(ctx,"param");
                return -1;
            }
            Long etid = Long.parseLong(m[0]);
            if(!Verifier.isValidEtid(etid)){
                EerrorX(ctx,"etid");
                return -3;
            }
            fname = System.currentTimeMillis() + "_" +m[0];
            handler = 1;
            return 1000;
        }else {
            EerrorX(ctx,"param");
            return -2;
        }
    }

    private int handler = -1;
    private String fname = null;

    private void EerrorX(ChannelHandlerContext ctx, String reason) {
        writeResponse(ctx.channel(),HttpResponseStatus.INTERNAL_SERVER_ERROR,reason);
        ctx.channel().close();
    }

    private void Eerror(ChannelHandlerContext ctx, Exception e) {
        Long k = System.currentTimeMillis();
        Monitor.error("["+k+"] FileServer:" + e.getMessage());
        e.printStackTrace();
        writeResponse(ctx.channel(),HttpResponseStatus.INTERNAL_SERVER_ERROR,k+"");
    }

    private void finishingRead(ChannelHandlerContext ctx) {
        // 收到了全部的消息
        // 分析请求
        switch (handler) {
            case 1:
                if (fname != null) {
                    String fileNameBuf = DiskFileUpload.baseDirectory +
                            fname;
                    /*
                    String out = (new FacialExecutor()).getLandscapes(fileNameBuf);
                    //String out = "OK";
                    if(out != null) {
                        writeUploadAnswer(ctx.channel(), out);
                    }else{
                        EerrorX(ctx,"parse");
                    }*/
                    EerrorX(ctx,"nosuchmet");
                }
                break;
            default:
                writeUploadAnswerOK(ctx.channel(),"outoforder");
        }
    }


    private String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error("10000020 Unsupportted Charsets");
            }
        }

        return uri;
    }

    private void reset() {
        request = null;
        //销毁decoder释放所有的资源
        decoder.destroy();
        decoder = null;
    }

    /**
     * 通过chunk读取request，获取chunk数据
     *
     * @throws IOException
     */
    private void readHttpDataChunkByChunk() throws IOException {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            System.out.println("end chunk");
        }
    }

    private void writeHttpData(InterfaceHttpData data) throws IOException {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
            FileUpload fileUpload = (FileUpload) data;
            if (fileUpload.isCompleted() && handler >= 0) {
                String fileNameBuf = DiskFileUpload.baseDirectory +
                        fname;
                fileUpload.renameTo(new File(fileNameBuf));
            }
        } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            parseUploadAttr(attribute);
            /*if (CommonParam.DOWNLOAD_COLLECTION.equals(attribute.getName())) {
                SynchMessageWatcher.newBuild().getMsgQueue().add(attribute.getValue());
            }*/
        }
    }

    private void parseUploadAttr(Attribute att) {
        try {
            Monitor.logger("FileServer Attribute Rcvd:" + att.getName() + "="+att.getValue());
        } catch (IOException e) {
            Monitor.logger("FileServer Attribute Rcvd, display failed :" + e.getMessage());
            e.printStackTrace();
            //e.printStackTrace();
        }
    }

    private void writeDownLoadResponse(ChannelHandlerContext ctx, RandomAccessFile raf, File file) throws Exception {

        long fileLength = raf.length();


        //判断是否关闭请求响应连接
        boolean close = HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(request.headers().get(CONNECTION))
                || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(request.headers().get(CONNECTION));

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(CONTENT_LENGTH,fileLength);
        setContentHeader(response, file);

        if (!close) {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);
        System.out.println("读取大小：" + fileLength);

        final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, 1000);
        ChannelFuture writeFuture = ctx.write(region, ctx.newProgressivePromise());
        writeFuture.addListener(new ChannelProgressiveFutureListener() {
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) {
                    System.err.println(future.channel() + " Transfer progress: " + progress);
                } else {
                    System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }

            public void operationComplete(ChannelProgressiveFuture future) {
            }
        });

        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (close) {
            raf.close();
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void setContentHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));

        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(LAST_MODIFIED, dateFormatter.format(new Date(file.lastModified())));
    }


    private void writeUploadAnswer(Channel channel, String out) {
        writeResponse(channel,HttpResponseStatus.OK,out);
    }

    private void writeUploadAnswerOK(Channel ch,String ok){
        String resultStr = "";
        resultStr += "FileS,ok,";
        if ("message".equals(type)) {
            resultStr += "string";
        } else if ("upload".equals(type)) {
            resultStr += "file";
        } else if ("download".equals(type)) {
            resultStr += "download";
        }
        resultStr += ok;
        writeResponse(ch,HttpResponseStatus.OK,resultStr);
    }

    private void writeResponse(Channel channel, HttpResponseStatus httpResponseStatus, String returnMsg) {
        String resultStr = "";
        if (httpResponseStatus.code() == HttpResponseStatus.OK.code()) {
           resultStr = returnMsg;
        } else if (httpResponseStatus.code() == HttpResponseStatus.INTERNAL_SERVER_ERROR.code()) {
            resultStr = "FileS,fail," +returnMsg;
        }
        //将请求响应的内容转换成ChannelBuffer.
        ByteBuf buf = copiedBuffer(resultStr, CharsetUtil.UTF_8);
        //判断是否关闭请求响应连接
        boolean close = true; //HttpHeaderValues.CLOSE.toString().equalsIgnoreCase(request.headers().get(CONNECTION))
        //        || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
        //        && !HttpHeaderValues.KEEP_ALIVE.toString().equalsIgnoreCase(request.headers().get(CONNECTION));

        //构建请求响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, httpResponseStatus, buf);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

        if (!close) {
            //若该请求响应是最后的响应，则在响应头中没有必要添加'Content-Length'
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        }

        //发送请求响应
        ChannelFuture future = channel.writeAndFlush(response);
        //发送请求响应操作结束后关闭连接
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Long k = System.currentTimeMillis();
        Monitor.logger("["+k+"] : Error in FRSERVER - "+cause.getMessage());
        cause.printStackTrace();
        writeResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR, "exp," + k);
        ctx.channel().close();
    }
}
