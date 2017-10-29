package iotsampl.iot.data;
import iotsampl.iot.core.IotIds;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.oo.MiheChannelCacheEntity;
import iotsampl.iot.oo.MiheChannelDataEntity;
import iotsampl.iot.oo.MiheWarnsEntity;

import java.util.ArrayList;
import java.util.List;

import static iotsampl.iot.core.IotIds.getsLong;

/**
 * Created by Frapo on 2017/8/9.
 * Version :14
 * Earth - Moudule Radnor
 */
public class IotQueryDataProvider {

    public static void startIotSync() {
        IotSync.init();
        if(!IotSync.getConnected()){
            IotLogger.i("Data Service conn failed , aboting. ");
            return;
        }
    }

    public static List<String> getNodeDur(int id, long st, long end, int len,int duration,boolean rev) {
        List<MiheChannelCacheEntity> x;
        x  = IotSync.localgetcache(id, st, end, duration,len,true);
        return formatCacheEntities(x);
    }

    /**
     * 转换OOM对象 为 String 以供前端使用
     * */
    public static List<String> formatCacheEntities(List<MiheChannelCacheEntity> x) {
        List<String> xd = new ArrayList<>();
        int i;
        if(x == null) return xd;
        for (MiheChannelCacheEntity v : x) {
            String sb = Long.toHexString(v.getStart()) +
                    getsLong(v.getMin()) +
                    "," +
                    getsLong(v.getMax()) +
                    "," +
                    getsLong(v.getAvg());
            xd.add(sb);
        }
        return xd;
    }

    public static List<String> formatDataEntities(List<MiheChannelDataEntity> x) {
        List<String> xd = new ArrayList<>();
        for (MiheChannelDataEntity v : x) {
            String sb = Long.toHexString(v.getStart()) + getsLong(v.getData());
            xd.add(sb);
        }
        return xd;
    }

    public static List<String> getNodeOrig(Long id, long st, long end, int len,boolean rev) {
        List<MiheChannelDataEntity> x;
        int aid = IotIds.extractShorId(id);
        x  = IotSync.localgetdata(aid, st, end, len, rev);
        return formatDataEntities(x);
    }

    /**
     *  获取指定设备的告警信息（列表）
     * */
    public static List<String> getWarnList(Long id, int len) {
        long t = System.currentTimeMillis();
        int shortId = IotIds.extractShorId(id);
        int duration = IotIds.extractDuration(id);
        if(duration == 0 && shortId > 0){
            List<MiheWarnsEntity> x = IotSync.localgetwarn(shortId,0,t,len,false);
            return formatWarnEntities(x);
        }else{
            return null;
        }
    }

    /**
     *  获取指定群组的告警信息（列表）
     * */
    public static List<String> getWarnRListClust(int cluster, int len) {
        long t = System.currentTimeMillis();
        if(cluster >= 0){
            List<MiheWarnsEntity> x = IotSync.localgetwarn(cluster,0,t,len,true);
            return formatWarnEntities(x);
        }else{
            return null;
        }
    }

    public static List<String> formatWarnEntities(List<MiheWarnsEntity> x){
        List<String> vm = new ArrayList<>();
        for(int i=x.size() -1;i>=0;i--){
             MiheWarnsEntity a = x.get(i);
             vm.add(Long.toHexString(a.getStart()) + "::" + Long.toHexString(a.getStart() / 1000) + "::" + a.getChid() +"::"+a.getWarn());
        }
        return vm;
    }

    public static List<String> getWarnRList(long tid, int len) {
        long t = System.currentTimeMillis();
        int chid = IotIds.extractShorId(tid);
        List<MiheWarnsEntity> vx = IotSync.localgetwarn(chid,0,t,len, true);
        //return IotSync.localrgetscore("wn"+Long.toHexString(id),0, t, len);
        return formatWarnEntities(vx);
    }

    /**
     * 从设备中删除告警
     * */
    public static boolean removeNotification(Long WarnTimeStamp, int chid){
        // chid
        long m = IotSync.removeNotify(chid,WarnTimeStamp);
        if(m == 1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 从群组中删除指定告警
     * */
    public static boolean removeNotificationCluster(Long WarnTimeStamp, int cluster) {
        long m = IotSync.removeNotify(cluster,WarnTimeStamp);
        if(m == 1){
            return true;
        }else{
            return false;
        }
    }


    public static MiheChannelCacheEntity getCacheLastOne(int gid, int duration) throws NullPointerException,NumberFormatException{
        long now = System.currentTimeMillis() / 1000;
        List<MiheChannelCacheEntity> single = IotSync.localgetcache(gid,0,now,duration,1,true);
        if(single != null && single.size() > 0){
            return single.get(0);
        }else{
            return null;
        }
    }

    public static MiheChannelDataEntity getDataLastOne(int gid) throws NullPointerException,NumberFormatException{
        long now = System.currentTimeMillis() / 1000;
        List<MiheChannelDataEntity> single = IotSync.localgetdata(gid,0,now,1,true);
        if(single != null && single.size() > 0){
            return single.get(0);
        }else{
            return null;
        }
    }

}
