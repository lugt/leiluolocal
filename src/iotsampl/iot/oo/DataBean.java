package iotsampl.iot.oo;

import Radnor.Utils.Byter;

/**
 * Created by Frapo on 2017/8/8.
 * Version :16
 * Earth - Moudule Radnor
 */
public class DataBean {
    private long sdata;
    public byte type;
    public long time;
    private long syncid;

    public void setData(long data){
        sdata = data;
    }

    @Override
    public String toString(){
        /*byte[] b1 = sdata.getBytes();
        int s = b1.length + 8;
        byte[] b2 = new byte[s];
        System.arraycopy(b1,0,b2,0,b1.length);*/
        byte[] x = Byter.long2Bytes(time);/*
        b2[s-4] = x[0];
        b2[s-3] = x[1];
        b2[s-2] = x[2];
        b2[s-1] = x[3];
        b2[s-4] = x[0];
        b2[s-3] = x[1];
        b2[s-2] = x[2];
        b2[s-1] = x[3];*/
        return sdata + Double.toHexString(time);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setSyncId(long syncid) {
        this.syncid = syncid;
    }

    public long getData() {
        return sdata;
    }
}
