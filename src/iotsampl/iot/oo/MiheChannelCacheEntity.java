package iotsampl.iot.oo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "mihe_channel_cache", schema = "mihe", catalog = "")
public class MiheChannelCacheEntity {
    private long globalid;
    private int chid;
    private Serializable duration;
    private Integer min;
    private Integer max;
    private Integer avg;
    private Integer start;

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
    @Column(name = "duration", nullable = true)
    public Serializable getDuration() {
        return duration;
    }

    public void setDuration(Serializable duration) {
        this.duration = duration;
    }

    @Basic
    @Column(name = "min", nullable = true)
    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    @Basic
    @Column(name = "max", nullable = true)
    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    @Basic
    @Column(name = "avg", nullable = true)
    public Integer getAvg() {
        return avg;
    }

    public void setAvg(Integer avg) {
        this.avg = avg;
    }

    @Basic
    @Column(name = "start", nullable = true)
    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiheChannelCacheEntity that = (MiheChannelCacheEntity) o;

        if (globalid != that.globalid) return false;
        if (chid != that.chid) return false;
        if (duration != null ? !duration.equals(that.duration) : that.duration != null) return false;
        if (min != null ? !min.equals(that.min) : that.min != null) return false;
        if (max != null ? !max.equals(that.max) : that.max != null) return false;
        if (avg != null ? !avg.equals(that.avg) : that.avg != null) return false;
        if (start != null ? !start.equals(that.start) : that.start != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (globalid ^ (globalid >>> 32));
        result = 31 * result + chid;
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (min != null ? min.hashCode() : 0);
        result = 31 * result + (max != null ? max.hashCode() : 0);
        result = 31 * result + (avg != null ? avg.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        return result;
    }
}
