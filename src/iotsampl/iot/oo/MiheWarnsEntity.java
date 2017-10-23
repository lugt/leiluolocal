package iotsampl.iot.oo;

import javax.persistence.*;

@Entity
@Table(name = "mihe_warns", schema = "mihe", catalog = "")
public class MiheWarnsEntity {
    private Integer chid;
    private Integer warn;
    private long start;
    private long globalid;

    @Basic
    @Column(name = "chid", nullable = true)
    public Integer getChid() {
        return chid;
    }

    public void setChid(Integer chid) {
        this.chid = chid;
    }

    @Basic
    @Column(name = "warn", nullable = true)
    public Integer getWarn() {
        return warn;
    }

    public void setWarn(Integer warn) {
        this.warn = warn;
    }

    @Basic
    @Column(name = "start", nullable = false)
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    @Id
    @Column(name = "globalid", nullable = false)
    public long getGlobalid() {
        return globalid;
    }

    public void setGlobalid(long globalid) {
        this.globalid = globalid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiheWarnsEntity that = (MiheWarnsEntity) o;

        if (start != that.start) return false;
        if (globalid != that.globalid) return false;
        if (chid != null ? !chid.equals(that.chid) : that.chid != null) return false;
        if (warn != null ? !warn.equals(that.warn) : that.warn != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = chid != null ? chid.hashCode() : 0;
        result = 31 * result + (warn != null ? warn.hashCode() : 0);
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + (int) (globalid ^ (globalid >>> 32));
        return result;
    }
}
