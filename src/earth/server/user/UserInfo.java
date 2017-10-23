package earth.server.user;

import iotsampl.Constant;
import earth.server.Monitor;
import earth.server.utils.Verifier;
import io.netty.buffer.ByteBuf;
import iotsampl.iot.oo.MiheUserEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.nio.charset.Charset;

/**
 * Created by Frapo on 2017/1/22.
 */
public class UserInfo {

    public String basic(ByteBuf content) {
        String m = content.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 2) {
            return "Bas,fail,param";
        }
        if (!Verifier.isValidH64(x[0])) {
            return "Bas,fail,usign";
        }
        MiheUserEntity udE = getUserOnSSid(x[0]);
        if(udE == null || !Verifier.isValidEtid(udE.getUid())) return "Bas,fail,login";
        StringBuilder id = new StringBuilder();
        id.append(udE.getName()).append(",")
            .append(udE.getPhone()).append(",")
            .append(udE.getPriv()).append(",")
            .append(udE.getTitle()).append(",")
            .append(udE.getState());
        return "Bas,ok,"+id;
    }

    public String getIdentity(ByteBuf content) {
        String m = content.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 2) {
            return "Id,fail,param";
        }
        if (!Verifier.isValidH64(x[0])) {
            return "Id,fail,usign";
        }
        MiheUserEntity udE = getUserOnSSid(x[0]);
        if(udE == null || !Verifier.isValidEtid(udE.getUid())) return "Id,fail,login";
        String priv = udE.getPriv().toString();
        return "Id,ok,"+priv;
    }

    public MiheUserEntity getUserOnSSid(String ssid) {
        try {
            Session session = Constant.getSession();
            Query q = session.createQuery("from MiheUserEntity where sess = :sess");
            q.setParameter("sess", ssid);
            MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();
            if (udE == null) return null;
            return udE;
        } catch (Exception es) {
            Monitor.logger("[GetEtid]" + es.getMessage());
            es.printStackTrace();
            return null;
        }
    }

    public String verify(ByteBuf buf) {
        String y = buf.toString(Charset.forName("utf-8"));
        return "V,ok";
    }

    public String getPublicEtid(ByteBuf content) {
        String m = content.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 2) {
            return "Ppet,fail,param";
        }
        Long et = Long.valueOf(x[0]);
        if (!Verifier.isValidEtid(et)) {
            return "Ppet,fail,etid";
        }
        MiheUserEntity udE = getUserOnEtid(et);
        if(udE == null || !Verifier.isValidEtid(udE.getUid())) return "Ppet,fail,search";
        String id = udE.getUid() + "," + udE.getName();
        return "Ppet,ok,"+id;
    }

    public String getPublicCell(ByteBuf content) {
        String m = content.toString(Charset.forName("UTF-8"));
        String[] x = m.split(","); //Cell,
        if (x.length != 2) {
            return "Pcel,fail,param";
        }
        if (!Verifier.isMobile(x[0])) {
            return "Pcel,fail,mobile";
        }
        Long et = Long.valueOf(x[0]);
        MiheUserEntity udE = getUserOnCell(et);
        if(udE == null || !Verifier.isValidEtid(udE.getUid())) return "Pcel,fail,search";
        String id = udE.getUid() + "," + udE.getName();
        return "Pcel,ok,"+id;
    }

    private MiheUserEntity getUserOnCell(Long et) {
        try {
            Session session = Constant.getSession();
            Query q = session.createQuery("from MiheUserEntity where phone = :cell");
            q.setParameter("cell", et);
            MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();
            if (udE == null) return null;
            return udE;
        } catch (Exception es) {
            Monitor.logger("[GetEtid]" + es.getMessage());
            es.printStackTrace();
            return null;
        }
    }

    private MiheUserEntity getUserOnEtid(long et) {
        try {
            Session session = Constant.getSession();
            Query q = session.createQuery("from MiheUserEntity where uid = :eid");
            q.setParameter("eid", et);
            MiheUserEntity udE = (MiheUserEntity) q.uniqueResult();
            if (udE == null) return null;
            return udE;
        } catch (Exception es) {
            Monitor.logger("[GetEtid]" + es.getMessage());
            es.printStackTrace();
            return null;
        }
    }
}
