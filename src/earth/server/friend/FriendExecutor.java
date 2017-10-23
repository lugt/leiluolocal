package earth.server.friend;

import earth.server.data.RedisConnect;
import earth.server.user.InnerLogin;
import earth.server.utils.Verifier;
import io.netty.buffer.ByteBuf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Created by Frapo on 2017/2/5.
 * Version :14
 * Earth - Moudule earth.server.friend
 */
public class FriendExecutor{

    Log log = LogFactory.getLog(FriendExecutor.class);

    public String getList(ByteBuf content) {

        String m = content.toString(Charset.forName("UTF-8"));
        String[] t = m.split(",");
        if(t.length != 2){
            return "FrL,fail,param";
        }
        // usign,kiosk
        long h = new InnerLogin().get(t[0]);
        if(!Verifier.isValidEtid(h)) return "FrL,fail,usign";
        try {
            String out = FriendManager.getList(h);
            if(out == null || Objects.equals(out, "null")){
                return "FrL,fail,empty";
            }
            return "FrL,ok,"+out;
        } catch (Exception e) {
            Long l = System.currentTimeMillis();
            log.info("["+l + "] - Friend-List- : " + e.getClass().getName() + " : " +e.getMessage());
            e.printStackTrace();
            return "FrL,fail,except,"+l.toString();
        }
    }

    public String add(ByteBuf content){
        String m = content.toString(Charset.forName("UTF-8"));
        String[] query = m.split(",");
        if(query.length != 3) return "FAdd,fail,param";
        long h = (new InnerLogin()).get(query[0]);
        long t = Long.valueOf(query[1]);
        if(!Verifier.isValidEtid(h)) {
            return "FAdd,fail,usign";
        }
        if(!Verifier.isValidEtid(t)) return "FAdd,fail,etid";
        try {
            String out = FriendManager.addFriend(h, t);
            return "FAdd,"+out;
        } catch (Exception e) {
            Long l = System.currentTimeMillis();
            log.info("["+ l + "] Friend-add : " + e.getClass().getName() + " : "+ e.getMessage());
            e.printStackTrace();
            return "FAdd,fail,except,"+l.toString();
        }
    }

    public String remove(ByteBuf content) {
        String m = content.toString(Charset.forName("UTF-8"));
        String[] query = m.split(",");
        if(query.length != 3) return "Frm,fail,param";
        long h = (new InnerLogin()).get(query[0]);
        long t = Long.valueOf(query[1]);
        if(!Verifier.isValidEtid(h)) return "Frm,fail,usign";
        if(!Verifier.isValidEtid(t)) return "Frm,fail,etid";
        try {
            String out = FriendManager.remove(h, t);
            return "Frm,"+out;
        } catch (Exception e) {
            Long l = System.currentTimeMillis();
            log.info(l + " Fr-Remove- "+ e.getClass().getName() + " : " + e.getMessage());
            e.printStackTrace();
            return "Frm,fail,except,"+l.toString();
        }
    }
}
