package iotsampl.iot.oo;

/**
 * Created by Frapo on 2017/8/8.
 * Version :16
 * Earth - Moudule Radnor
 */
public class DataManager {

    public static DataBean prepare(long data, long time) {
        DataBean s = new DataBean();
        s.setData(data);
        s.setTime(time);
        return s;
    }
}
