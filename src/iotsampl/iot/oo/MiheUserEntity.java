package iotsampl.iot.oo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "mihe_user", schema = "mihe", catalog = "")
public class MiheUserEntity {
    private int uid;
    private String usn;
    private Serializable pss;
    private Serializable name;
    private Serializable title;
    private Serializable priv;
    private int state;
    private Serializable sess;
    private Serializable memo;
    private Serializable phone;
    private String otell;

    @Id
    @Column(name = "uid", nullable = false)
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Basic
    @Column(name = "usn", nullable = false, length = 40)
    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    @Basic
    @Column(name = "pss", nullable = false, length = -1)
    public Serializable getPss() {
        return pss;
    }

    public void setPss(Serializable pss) {
        this.pss = pss;
    }

    @Basic
    @Column(name = "name", nullable = true, length = -1)
    public Serializable getName() {
        return name;
    }

    public void setName(Serializable name) {
        this.name = name;
    }

    @Basic
    @Column(name = "title", nullable = true, length = -1)
    public Serializable getTitle() {
        return title;
    }

    public void setTitle(Serializable title) {
        this.title = title;
    }

    @Basic
    @Column(name = "priv", nullable = true, length = -1)
    public Serializable getPriv() {
        return priv;
    }

    public void setPriv(Serializable priv) {
        this.priv = priv;
    }

    @Basic
    @Column(name = "state", nullable = false)
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Basic
    @Column(name = "sess", nullable = true, length = -1)
    public Serializable getSess() {
        return sess;
    }

    public void setSess(Serializable sess) {
        this.sess = sess;
    }

    @Basic
    @Column(name = "memo", nullable = true, length = -1)
    public Serializable getMemo() {
        return memo;
    }

    public void setMemo(Serializable memo) {
        this.memo = memo;
    }

    @Basic
    @Column(name = "phone", nullable = true, length = -1)
    public Serializable getPhone() {
        return phone;
    }

    public void setPhone(Serializable phone) {
        this.phone = phone;
    }

    @Basic
    @Column(name = "otell", nullable = true, length = 40)
    public String getOtell() {
        return otell;
    }

    public void setOtell(String otell) {
        this.otell = otell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiheUserEntity that = (MiheUserEntity) o;

        if (uid != that.uid) return false;
        if (state != that.state) return false;
        if (usn != null ? !usn.equals(that.usn) : that.usn != null) return false;
        if (pss != null ? !pss.equals(that.pss) : that.pss != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (priv != null ? !priv.equals(that.priv) : that.priv != null) return false;
        if (sess != null ? !sess.equals(that.sess) : that.sess != null) return false;
        if (memo != null ? !memo.equals(that.memo) : that.memo != null) return false;
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) return false;
        if (otell != null ? !otell.equals(that.otell) : that.otell != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + (usn != null ? usn.hashCode() : 0);
        result = 31 * result + (pss != null ? pss.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (priv != null ? priv.hashCode() : 0);
        result = 31 * result + state;
        result = 31 * result + (sess != null ? sess.hashCode() : 0);
        result = 31 * result + (memo != null ? memo.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (otell != null ? otell.hashCode() : 0);
        return result;
    }
}
