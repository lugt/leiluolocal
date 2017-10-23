package earth.server.user;

import iotsampl.Constant;
import earth.server.utils.Verifier;
import io.netty.buffer.ByteBuf;
import iotsampl.iot.oo.MiheUserEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.nio.charset.Charset;

/**
 * Created by Frapo on 2017/1/25.
 * Version :10
 * Earth - Moudule earth.server.user
 */
public class UserReg {
    static Log log = LogFactory.getLog(UserReg.class);
    public String create(ByteBuf content) {
        String m = content.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 4) {
            return "C,fail,param,no";
        }
        if (!Verifier.isMobile(x[0])) {
            return "C,fail,mobile,string";
        }
        // Cell to Long
        Long cell = Long.valueOf(x[0]);

        if (cell < 13000000000L || cell > 19999999999L) {
            return "C,fail,mobile,abs";
        }
        // Globalised Long Cell(+86)
        cell = cell + 9998600000000000L;

        if (!Verifier.isValidH64(x[1])) {
            // Passwd is valid
            return "C,fail,pass,base64";
        }

        String DefaultName = "Earth_CN_" + x[0];

        return "C,"+commitCreate(cell, x[1], DefaultName);
    }

    private void getTransact(Session session){
        if(session.getTransaction() == null){
            session.beginTransaction().setTimeout(3);
        }else {
            if(!session.getTransaction().isActive()){
                session.beginTransaction();
            }
        }
    }

    private String commitCreate(long cellphone, String passWd, String dispName) {

        MiheUserEntity udE = new MiheUserEntity();
        udE.setName(dispName);
        udE.setPhone(cellphone);
        udE.setPriv("view,cluster0,newreg");
        Session session = null;
        try {

            session = Constant.getSession();
            Constant.getTransact(session);
            // 检验是否存在

            Query q = session.createQuery("from MiheUserEntity where phone = :cell");
            q.setParameter("cell", cellphone);

            if(q.uniqueResult() != null){
                // Error
                return "fail,repeatcell";
            }

            session.save(udE);

            passWd = Signin.PasswordDigest(udE.getUid(), passWd);

            udE.setPss(passWd);

        } catch (Exception e) {
            Long k = System.currentTimeMillis();
            e.printStackTrace();
            log.error("Commit Fail/ Digest Fail -" + e.getLocalizedMessage()+k.toString());
            if (session != null) {
                session.delete(udE);
            }
            return "fail,commit,"+k.toString();
        }

        try {
            finishbasic(udE,session);
        } catch (Exception e) {
            Long k = System.currentTimeMillis();
            log.info("[Reg/f] Error Id:" + k.toString());
            e.printStackTrace();
            session.delete(udE);
            return "fail,postbasic"+k.toString();
        }
        session.getTransaction().commit();
        return "ok,"+udE.getUid();
        //session.saveDuration( new UserDao( "A follow up event", new Date() ) );
        //session.getTransaction().commit();
    }

    private void finishbasic(MiheUserEntity udE, Session session) throws Exception {
        // ok
    }
}
