package earth.server.friend;

import earth.server.data.RedisConnect;

import java.util.Objects;

/**
 * Created by Frapo on 2017/2/5.
 * Version :13
 * Earth - Moudule earth.server.friend
 */
public class FriendManager {
    public static String getList(long etid) throws Exception{
        RedisConnect rC = new RedisConnect(3);
        String m = rC.getValue(etid + "=");
        rC.close();
        return m;
    }

    public static String remove(long etid,long friend) throws Exception{
        RedisConnect rC = new RedisConnect(3);
        String m = rC.getValue(etid + "=");
        String[] old = m.split("-");
        String tar = Long.toHexString(friend) +"+";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < old.length; i++) {
            if (!Objects.equals(old[i], tar) && !Objects.equals(old[i], "") && !Objects.equals(old[i], "+")) {
                sb.append(old[i]);
                sb.append("-");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        if(sb.length() == m.length()) return "fail,notexist";
        rC.setValue(etid+"=",sb.toString());
        rC.close();
        return "ok";
    }


    public static String addFriend(long etid,long fr) throws Exception{
        String f = Long.toHexString(fr)+"+";

        RedisConnect connect = new RedisConnect(3);

        String m = connect.getValue(etid + "=");
        String[] old = m.split("-");

        for (String anA : old) {
            if (Objects.equals(anA, Long.toHexString(fr))) {
                return "fail,exist";
            }
        }

        m += "-" + f;

        connect.setValue(etid+"=",m);
        connect.close();

        return "ok";
    }

    public static void initUser(long et) throws Exception{
        RedisConnect connect = new RedisConnect(3);
        connect.setValue(et+"=","3e9+-3ea+-3eb+");
        connect.close();
    }
}
