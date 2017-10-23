package earth.server.user;
import iotsampl.Constant;
import earth.server.Monitor;
import earth.server.utils.Verifier;
import iotsampl.iot.oo.MiheUserEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Created by Frapo on 2017/1/25.
 * Version :10
 * Earth - Moudule earth.server.user
 */
public class InnerLogin {


    public long verify(String e, String s) {
        try {
            if(e == null || s == null || !Verifier.isValidB64(s)){
                return 0L;
            }
            Long target = Long.parseLong(e);
            Session session = Constant.getSession();
            Constant.getTransact(session);
            if(!session.isConnected() || !session.isOpen()){
                return 0L;
            }
            Query q = session.createQuery("from MiheUserEntity where sess = :sess");
            q.setParameter("sess", s);
            List l = q.list();
            session.close();
            //UserdaoEntity udE = (UserdaoEntity) q.uniqueResult();
            if(l.get(0) instanceof MiheUserEntity) {
                MiheUserEntity udE = (MiheUserEntity) l.get(0);
                return (target == udE.getUid()) ? udE.getUid() : 0L;
            }else{
                return 0L;
            }
        } catch (Exception se) {
            se.printStackTrace();
            Monitor.logger("[Inner/Login] Query Fail"  + se.getMessage());
            return 0L;
        }
    }

    public long get(String s) {
        try {
            if(s == null){ return 0L; }
            if(!Verifier.isValidH64(s)) return 0;
            Session session = Constant.getSession();
            Constant.getTransact(session);
            if(!session.isConnected() || !session.isOpen()){
                return 0L;
            }
            Query q = session.createQuery("from MiheUserEntity where sess = :sess");
            q.setParameter("sess", s);
            List l = q.list();
            session.close();
            //UserdaoEntity udE = (UserdaoEntity) q.uniqueResult();
            if (l.size() == 0){
                return 0L;
            }
            //udE == null || udE.getEtid() <= Constant.MINIMAL_ETID){
            if(l.get(0) instanceof MiheUserEntity) {
                MiheUserEntity udE = (MiheUserEntity) l.get(0);
                return udE.getUid();
            }else{
                return 0L;
            }
        } catch (Exception se) {
            se.printStackTrace();
            Monitor.logger("[Inner/Login] Query Fail"  + se.getMessage());
            return 0;
        }
    }
}
