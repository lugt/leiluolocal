package earth.server.sz;

import earth.server.Constant;
import earth.server.Monitor;
import io.netty.buffer.ByteBuf;
import org.hibernate.Session;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by Frapo on 2017/2/5.
 * Version :16
 * Earth - Moudule earth.server.sz
 */
public class UpdateManager {

    public static String getUpdate (){
        return "U,ok,http://niimei.wicp.net/earth/earth-seagate.apk";
    }

    public static String setLog(ByteBuf log) {
        String m = log.toString(Charset.forName("UTF-8"));
        Long id = System.currentTimeMillis();
        try {
            Monitor.feedback("[" + new Date() + "] - Feedback-: " + m);
        }catch (Exception e){
            e.printStackTrace();
            return "Log,fail,except,"+id;
        }
        return "Log,ok";

    }
}
