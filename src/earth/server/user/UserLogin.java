package earth.server.user;

import earth.server.Constant;
import earth.server.Monitor;
import earth.server.utils.Verifier;
import io.netty.buffer.ByteBuf;
import iotsampl.iot.oo.MiheUserEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.nio.charset.Charset;

/**
 * Created by Frapo on 2017/1/22.
 * Version :21
 * Earth - Moudule ${PACKAGE_NAME}
 */
public class UserLogin {

    private static Log log = LogFactory.getLog(UserLogin.class);

    public String phone(ByteBuf H) {
        String m = H.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 4) {
            return "L,fail,param";
        }
        if (!Verifier.isMobile(x[0])) {
            return "L,fail,mobile";
        }
        // Cell to Long
        Long cell = Long.valueOf(x[0]);

        if (cell < 13000000000L || cell > 19999999999L) {
            return "L,fail,mobile";
        }
        // Globalised Long Cell(+86)
        //cell = cell + 9998600000000000L;

        if (!Verifier.isValidH64(x[1]) || x[1].length() > 50 || x[1].length() < 2) {
            // Passwd is valid
            return "L,fail,pass";
        }

        try {
            String ret = commitLogin(cell, x[1]);
            return "L," + ret;
        } catch (Exception e) {
            Long k = System.currentTimeMillis();
            log.error("[JPA] Login Fail ID:"+k.toString());
            e.printStackTrace();
            return "L,fail,query,"+k.toString();
        }
    }

    public String reborn(ByteBuf content) {
        String[] m = content.toString(Charset.forName("UTF-8")).split(",");
        if (m.length != 2) return "Auto,fail,param";
        if (!Verifier.isValidH64(m[0])) return "Auto,fail,base64";
        try {
            Session session = iotsampl.Constant.getSession();
            Transaction tx = iotsampl.Constant.getTransact(session);

            Query q = session.createQuery("from MiheUserEntity where sess = :sess").setParameter("sess", m[0]);
            MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();
            if (udE != null) {
                return "Auto,ok";
            } else {
                return "Auto,fail,na";
            }
        } catch (Exception es) {
            Monitor.logger("[Auto/Commit] Fail " + es.getLocalizedMessage() + "/" + es.getStackTrace().toString());
            return "Auto,fail,commit";
        }
    }

    public String exit(ByteBuf content) {
        /**
         * 核心流程：
         * 1. 清除 Sessid
         * 2. 关闭Long（Tianjin）服务上的Channel
         * 3. 清理 Session/Cookie
         * */
        return "X,ok";
    }

    public long getEtidOnSSid(String ssid) {
        try {
            Session session = iotsampl.Constant.getSession();
            Query q = session.createQuery("from MiheUserEntity where sess = :sess");
            q.setParameter("sess", ssid);
            MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();
            if (udE == null) return 0L;
            return udE.getUid();
        } catch (Exception es) {
            Monitor.logger("[GetEtid]" + es.getMessage());
            es.printStackTrace();
            return 0L;
        }
    }

    public String etid(ByteBuf buf) {
        String m = buf.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 4) {
            return "L,fail,param";
        }
        Long target = Long.valueOf(x[0]);
        if (target < Constant.MINIMAL_ETID || target > Constant.MAX_ETID) {
            return "L,fail,etid";
        }

        if (!Verifier.isValidH64(x[1])) {
            // Passwd is valid
            return "L,fail,pass";
        }
        /**
         *    SSN
         * */
        String ssn = "";

        try {
            ssn = commitLoginEtid(target, x[1]);
            return "L,"+ssn;
        } catch (Exception e) {
            Long id = System.currentTimeMillis();
            Monitor.logger("["+id+"] Login Fail : " + e.getMessage());
            e.printStackTrace();
            return "L,fail,query,"+id;
        }

    }

    public String commitLogin(Long cell, String passWd) throws Exception {

        Session session = iotsampl.Constant.getSession();
        iotsampl.Constant.getTransact(session);
        Query q = session.createQuery("from MiheUserEntity where phone = :cell");
        q.setParameter("cell", cell);
        MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();

        if (udE == null || udE.getUid() <= Constant.MINIMAL_ETID) {
            session.close();
            return "fail,notfound";
        }

        try {
            if (udE.getPss() != null && udE.getPss().equals(Signin.PasswordDigest(udE.getUid(), passWd))) {
                udE.setSess(generateSessionId());
                session.save(udE);
                session.getTransaction().commit();
                return "ok," + udE.getSess()+","+udE.getUid();
            } else {
                session.close();
                return "fail,password";
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                Monitor.logger("[Digest Fail]" + e.getMessage() + e.getStackTrace().toString());
                session.close();
                return "fail,digest";
            } else {
                Long k = System.currentTimeMillis();
                Monitor.logger("[Commit Fail] ID:" + k.toString() + e.getStackTrace().toString() + " / " + e.getMessage());
                session.close();
                return "fail,commit," + k.toString();
            }
        }
    }

    private String commitLoginEtid(Long target, String s) {
        try{
            Session session = iotsampl.Constant.getSession();
            iotsampl.Constant.getTransact(session);

            Query q = session.createQuery("from MiheUserEntity where uid = :ev").setParameter("ev", target);
            MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();

            if (udE == null || udE.getUid() <= Constant.MINIMAL_ETID) {
                session.close();
                return "fail,notfound";
            }
            if (udE.getPss() != null && udE.getPss().equals(Signin.PasswordDigest(udE.getUid(), s))) {
                udE.setSess(generateSessionId());
                session.save(udE);
                session.getTransaction().commit();
                return "ok," + udE.getSess();
            } else {
                session.close();
                return "fail,password";
            }
        } catch (Exception e) {
            Long k = System.currentTimeMillis();
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                log.warn("[Login/Argu] ID:" + k.toString() + " - " + e.getMessage());
                e.printStackTrace();
                return "fail,commit,"+k.toString();
            } else {
                log.warn("[Login/Commit] ID:" + k.toString()  + " - " + e.getMessage());
                e.printStackTrace();
                return "fail,commit," + k.toString();
            }
        }

    }

    private String generateSessionId() {
        String result = null;
        do {
            result = ETID.GetETID();//UUID.randomUUID().toString().replaceAll("-","") + System.currentTimeMillis()+"x";
        } while (getEtidOnSSid(result) != 0L); //此处保证最终生成给客户端使用的SESSIONID一定是不重复的
        return result;
    }

}
