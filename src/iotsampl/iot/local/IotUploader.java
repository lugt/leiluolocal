package iotsampl.iot.local;

import earth.server.utils.SHA3Digest;
import iotsampl.iot.core.IotIds;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.core.IotManager;
import iotsampl.iot.data.HttpsUtil;
import iotsampl.iot.data.IotSync;
import iotsampl.iot.oo.MiheChannelCacheEntity;
import iotsampl.iot.oo.MiheChannelDataEntity;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static iotsampl.iot.core.IotIds.getsLong;

/**
 *
 * 本地数据向云端上传
 * 独立线程
 * Created by Frapo on 2017/8/8.
 * Version :17
 * Earth - Moudule iotsampl.iot
 */
public class IotUploader extends Thread{


    /*
    *   上传安全检验
    * */
    private static final String AUTH_PHRASE = "#*CBDCYBVV*!*'A{5150231215671975549678070113093598753779818874173425479177197()(OP:P#!P_+)_)''''!+(@*&@&*^#)";
    private static final String URL_UPLOAD = "http://121.42.198.57/m/api.do";//121.42.198.57
    private List<Integer> syncList;
    private List<Integer> cacheList;
    private List<Integer> cacheDuration;

    // 配置信息
    //  Chid : original
    //  Chid : duration-cache
    //  chid : warn
    public static IotUploader th;
    public static void init(){
        // TODO:读取本地同步情况
        //httpConnect to
        if(th == null) th = new IotUploader();
        th.permit = true;
        th.start();
    }

    /**
     *  实际操作
     *  阻塞型IO
     * */
    private static void cloudSave(long gid, String data) throws IOException,
        KeyManagementException , NoSuchProviderException , NoSuchAlgorithmException,
        CertificateException , KeyStoreException{
        String auth = null;
        try {
            auth = SHA3Digest.StringDigest(256,Long.toHexString(gid) + AUTH_PHRASE);
            String retn = HttpsUtil.basicHttpPost(URL_UPLOAD+"?u_cache,"+Long.toHexString(gid)+","+auth,data.getBytes());
            parseRetn(retn,gid);
        } catch (Exception e) {
            IotLogger.auto("CloudSave : "+e.toString());
        }
    }

    private static void parseRetn(String r, long gid) {
        if(r != null) {
            if(r.startsWith("CA,ok")){
                String[] kl = r.split(",");
                if(kl.length < 3) {
                    IotLogger.i("IotUploader Retn POI(@73");
                    return;
                }
                registerSI(kl[2],gid);
            }else if(r.startsWith("CA,fail,adding")){
                String[] kl = r.split(",");
                if(kl.length < 4) {
                    IotLogger.i("IotUploader Retn POI6@02");
                    return;
                }
                registerSI(kl[3],gid);
            }
        }else{
            IotLogger.i("error in parseRetn : null POI@553" );
        }
    }

    private static void registerSI(String s,long gidinDec) {

        try {
            long m = Long.parseLong(s,16);
            String y = IotSync.localgetOption("SI-"+gidinDec);
            long ak;
            if(y == null){
                ak = 0;
            }else{
                ak = Long.parseLong(y);
            }

            if(ak < m) {
                IotLogger.i(" Set - SI- " + gidinDec + " to " + s);
                IotSync.setOption("SI-" + gidinDec, Long.toString(m));
            }else if(ak == m){

            }else{
                IotLogger.i("refuse to set : SI-"+ak+", Server:"+m);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean permit = false;
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();

    private  static final long TIME_INTERVAL = 100;

    private static void parseQueue(String take) {

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if(!permit) {
            IotLogger.auto(" Iot Thread shutdown : without permit.");
            return;
        }

        SyncPrepare();

        try {
            System.out.println("-------------- IotUploader 已经开始运行--------------");
            while (true) {
                // 如果堵塞队列中存在数据就将其输出
                //Sync
                if (msgQueue.size() > 0) {
                    parseQueue(msgQueue.take());
                }
                try {
                    SyncTick();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
           // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

    }

    private void SyncPrepare(){
        syncList = IotManager.getOne().getHourList();
        cacheList = IotManager.getOne().getHourList();
        cacheDuration = new ArrayList<>();
        cacheDuration.add(36);
        cacheDuration.add(72);
        cacheDuration.add(216);
        cacheDuration.add(3600);
        cacheDuration.add(86400);
    }

    private void SyncTick() {
        // OriginalCh
        int i,j;
        for (i=0;i<syncList.size();i++) {
            // Original
            for (j=0;j<cacheDuration.size();j++) {
                //Duration
                CheckIndex(syncList.get(i), cacheDuration.get(j));
            }
            CheckIndex(syncList.get(i),0);
        }

    }

    private static void CheckIndex(int chid ,int duration){
        long index, gid = IotIds.getPrefix(duration) + chid;
        long now = System.currentTimeMillis() / 1000;
        try {
            String lastcheck = IotSync.localgetOption("SI-"+gid);
            if(lastcheck == null){
                index = 0;
            }else {
                index = Long.parseLong(lastcheck);
            }
            //小时级别缓存
            if(duration > 0) {
                SyncExpire(chid, duration, index, now, gid);
            }else{
                SyncExpireOrigin(chid, index, now, gid);
            }
            // 还未超时
        }catch (NumberFormatException | NullPointerException e){
            IotLogger.auto("IotUploader  CheckIndex : "+e.getMessage());
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private static void SyncExpireOrigin(int chid, long index, long now, long gid)
            throws IOException , NumberFormatException{
        long preindex = 0; //开始读取的第一个数据位置
        List<MiheChannelDataEntity> single = IotSync.localgetdata(chid, index + 1, now,1, false);
        if(single != null && single.size() == 1) {
            preindex = single.get(0).getStart();
        }else{
            // 区域内 没有数据 : 可能是刚刚更新完
            return;
        }
        try {
            single = IotSync.localgetdata(chid, preindex, now,10, false);
            StringBuilder sb = new StringBuilder(1000);
            if(single != null && single.size() > 0) {
                for (MiheChannelDataEntity df : single) {
                    sb.append(Long.toHexString(df.getStart()));
                    sb.append(getsLong(df.getData())).append("@");
                }
                sb.deleteCharAt(sb.length() - 1);
                cloudSave(gid, sb.toString());
            }else{
                // nothing to do
                //Why
                //IotLogger.auto("IotUploader POINT1392");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;

    }

    private static void SyncExpire(int chid, int duration, long index, long now, long gid)
            throws IOException{
        long preindex = 0; //开始读取的第一个数据位置
        List<MiheChannelCacheEntity> single = IotSync.localgetcache(chid, index + 1, now, duration,1,false);
        if(single != null && single.size() == 1) {
            preindex = single.get(0).getStart();
        }else{
            // 区域内 没有数据 : 可能是刚刚更新完
            IotLogger.auto(chid + " , " + duration +",no new data");
            return;
        }
        try {
            List<MiheChannelCacheEntity> someData = IotSync.localgetcache(chid, preindex, now,duration,10,false);
            StringBuilder sb = new StringBuilder(1000);
            //IotLogger.auto("Prepare : " + gid + " as durataion: " + duration + ",size:"+someData.size());
            if(someData != null && someData.size() > 0) {
                for (MiheChannelCacheEntity v : someData) {
                    sb.append(Long.toHexString(v.getStart()))
                            .append(getsLong(v.getMin()))
                            .append(",")
                            .append(getsLong(v.getMax()))
                            .append(",")
                            .append(getsLong(v.getAvg()))
                            .append("@");
                }
                sb.deleteCharAt(sb.length() - 1);
                cloudSave(gid, sb.toString());
            }else{
                // nothing to do
                //Why
                IotLogger.auto("IotUploader P(#8 ," + chid +","+gid+"，"+now);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }
}
