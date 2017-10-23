package iotsampl.iot.oo;

import javax.persistence.*;

@Entity
@Table(name = "mihe_channel_data", schema = "mihe", catalog = "")
public class MiheChannelDataEntity {
    private long globalid;
    private int chid;
    private long start;
    private Long data;

    @Id
    @Column(name = "globalid", nullable = false)
    public long getGlobalid() {
        return globalid;
    }

    public void setGlobalid(long globalid) {
        this.globalid = globalid;
    }

    @Basic
    @Column(name = "chid", nullable = false)
    public int getChid() {
        return chid;
    }

    public void setChid(int chid) {
        this.chid = chid;
    }

    @Basic
    @Column(name = "start", nullable = false)
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    @Basic
    @Column(name = "data", nullable = true)
    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiheChannelDataEntity that = (MiheChannelDataEntity) o;

        if (globalid != that.globalid) return false;
        if (chid != that.chid) return false;
        if (start != that.start) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (globalid ^ (globalid >>> 32));
        result = 31 * result + chid;
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
