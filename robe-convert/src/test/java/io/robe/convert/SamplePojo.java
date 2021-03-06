package io.robe.convert;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.robe.convert.common.annotation.Convert;

import java.math.BigDecimal;
import java.util.Date;

public class SamplePojo {

    @Convert(order = 0, unique = true, title = "Kullanıcı Id")
    private int id;

    @Convert(order = 0)
    private String name;

    @Convert(title = "Soyadı")
    private String surname;

    @Convert(title = "Long Id")
    private long longid;

    @Convert(title = "Double Id")
    private double doubleid;

    @Convert(title = "Big Id")
    private BigDecimal big = BigDecimal.ONE;

    @Convert
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy", timezone = "EET")
    private Date date2;

    @Convert
    private SampleEnum sampleEnum;

    public SamplePojo() {

    }

    public SamplePojo(int id, String name, String surname, long longid, double doubleid, BigDecimal big, Date date2, SampleEnum sampleEnum) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.longid = longid;
        this.doubleid = doubleid;
        this.big = big;
        this.date2 = date2;
        this.sampleEnum = sampleEnum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public long getLongid() {
        return longid;
    }

    public void setLongid(long longid) {
        this.longid = longid;
    }

    public double getDoubleid() {
        return doubleid;
    }

    public void setDoubleid(double doubleid) {
        this.doubleid = doubleid;
    }

    public BigDecimal getBig() {
        return big;
    }

    public void setBig(BigDecimal big) {
        this.big = big;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

    public SampleEnum getSampleEnum() {
        return sampleEnum;
    }

    public void setSampleEnum(SampleEnum sampleEnum) {
        this.sampleEnum = sampleEnum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SamplePojo{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", surname='").append(surname).append('\'');
        sb.append(", longid=").append(longid);
        sb.append(", doubleid=").append(doubleid);
        sb.append(", big=").append(big);
        sb.append(", date2=").append(date2);
        sb.append(", sampleEnum=").append(sampleEnum);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SamplePojo)) return false;

        SamplePojo that = (SamplePojo) o;

        if (Double.compare(that.doubleid, doubleid) != 0) return false;
        if (id != that.id) return false;
        if (longid != that.longid) return false;
        if (big != null ? big.compareTo(that.big) != 0 : that.big != null) return false;
        if (date2 != null ? !date2.equals(that.date2) : that.date2 != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (sampleEnum != that.sampleEnum) return false;
        if (surname != null ? !surname.equals(that.surname) : that.surname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (int) (longid ^ (longid >>> 32));
        temp = Double.doubleToLongBits(doubleid);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (big != null ? big.hashCode() : 0);
        result = 31 * result + (date2 != null ? date2.hashCode() : 0);
        result = 31 * result + (sampleEnum != null ? sampleEnum.hashCode() : 0);
        return result;
    }
}