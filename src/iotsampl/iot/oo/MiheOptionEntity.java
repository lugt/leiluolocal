package iotsampl.iot.oo;

import javax.persistence.*;

@Entity
@Table(name = "mihe_option", schema = "mihe", catalog = "")
public class MiheOptionEntity {
    private String name;
    private String val;

    @Id
    @Column(name = "name", nullable = false, length = 50)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "val", nullable = true, length = 200)
    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiheOptionEntity that = (MiheOptionEntity) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (val != null ? !val.equals(that.val) : that.val != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (val != null ? val.hashCode() : 0);
        return result;
    }
}
