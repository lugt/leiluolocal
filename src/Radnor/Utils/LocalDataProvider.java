package Radnor.Utils;

import Radnor.Automation.Automation;
import Radnor.Reader.Reader;
import earth.server.Local;
import iotsampl.iot.core.IotIds;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.core.IotManager;
import iotsampl.iot.data.IotSync;
import iotsampl.iot.local.IotUploader;
import iotsampl.iot.oo.MiheChannelCacheEntity;
import iotsampl.iot.oo.MiheChannelDataEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frapo on 2017/8/9.
 * Version :14
 * Earth - Moudule Radnor
 */
public class LocalDataProvider {

    private static Reader cRead;

    public static void startAsService() throws Exception{
        IotManager.getOne();
        int i;

        cRead = new Reader();
        IotLogger.i("开始启动串口部分");
        i = cRead.startComPort();
        IotLogger.i("串口部分结束");
        if (i == 1) {
            // 启动线程来处理收到的数据
            IotLogger.i("启动运行线程ing");
            cRead.init();
            cRead.start();
            IotLogger.i("启动运行线程完毕");
        }else{
            if(Local.DEBUG){
                IotLogger.i("启动调试线程ing");
                cRead.start();
                IotLogger.i("启动了调试线程");
            }
        }
    }
    public static void startIotSync() {
        IotSync.init();
        if(!IotSync.getConnected()){
            IotLogger.i("Data Service conn failed , aboting. ");
            return;
        }
        IotUploader.init();
    }
    public static void startAutomation() {
        Automation.init();
    }

    /**
     * 正序查找指定位置
     *
     * */
    public static List<String> getNodeDataList(long id, long start, long end,int len){
        int shortId = IotIds.extractShorId(id);
        List<MiheChannelDataEntity> x = IotSync.localgetdata(shortId,start,end,len, false);
        return formatDataEntities(x,false);
    }

    public static List<String> getNodeDataList2(Long id, long start, long end, int len) {
        int shortId = IotIds.extractShorId(id);
        List<MiheChannelDataEntity> x = IotSync.localgetdata(shortId,start,end,len, true);
        return formatDataEntities(x,true);
    }

    /*
    *  缓存的结果
    * */
    public static List<String> getNodeCacheList(Long id, long st, long end, int len,int duration) {
        int shortId = IotIds.extractShorId(id);
        List<MiheChannelCacheEntity> x = IotSync.localgetcache(shortId,st,end,duration,len,false);
        return formatCacheEntities(x,false);
    }

    public static List<String> getNodeRevCacheList(Long id, long st, long end, int len,int duration) {
        int shortId = IotIds.extractShorId(id);
        List<MiheChannelCacheEntity> x = IotSync.localgetcache(shortId,st,end,duration,len,true);
        return formatCacheEntities(x,true);
    }


    /**
     * 转换OOM对象 为 String 以供前端使用
     * */
    public static List<String> formatCacheEntities(List<MiheChannelCacheEntity> x, boolean rev) {
        List<String> xd = new ArrayList<>();
        int i;
        if(rev){
            for(i=x.size() - 1;i >= 0;i --){
                MiheChannelCacheEntity v = x.get(i);
                String sb = Long.toHexString(v.getStart()) +
                        IotIds.getsLong(v.getMin()) +
                        "," +
                        IotIds.getsLong(v.getMax()) +
                        "," +
                        IotIds.getsLong(v.getAvg());
                xd.add(sb);
            }
        }else {
            for (MiheChannelCacheEntity v : x) {
                String sb = Long.toHexString(v.getStart()) +
                        IotIds.getsLong(v.getMin()) +
                        "," +
                        IotIds.getsLong(v.getMax()) +
                        "," +
                        IotIds.getsLong(v.getAvg());
                xd.add(sb);
            }
        }
        return xd;
    }

    public static List<String> formatDataEntities(List<MiheChannelDataEntity> x, boolean rev) {
        List<String> xd = new ArrayList<>();
        int i;
        if(rev){
            for(i=x.size() - 1;i >= 0;i --){
                MiheChannelDataEntity v = x.get(i);
                String sb = Long.toHexString(v.getStart()) + IotIds.getsLong(v.getData());
                xd.add(sb);
            }
        }else {
            for (MiheChannelDataEntity v : x) {
                String sb = Long.toHexString(v.getStart()) + IotIds.getsLong(v.getData());
                xd.add(sb);
            }
        }
        return xd;
    }

}
