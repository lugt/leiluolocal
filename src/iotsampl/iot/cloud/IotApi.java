package iotsampl.iot.cloud;

import earth.server.utils.SHA3Digest;
import io.netty.buffer.ByteBuf;
import iotsampl.iot.core.IotIds;
import iotsampl.iot.data.IotQueryDataProvider;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.core.IotManager;
import iotsampl.iot.data.IotSync;
import iotsampl.iot.oo.MiheChannelCacheEntity;
import iotsampl.iot.oo.MiheChannelDataEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

public class IotApi {



    /**
     *  处理请求
     *  执行相关方法
     * */
    public String distributeCall(String a , ByteBuf content) throws Exception {
        // Authenticate
        if (a != null && a.length() > 0) {
            String[] k = a.split(",");
            if (k.length < 2) {
                return "CA,fail,querylist";
            }else if("u_cache".equals(k[0])){
                long g = checkAuth(k[1],k[2],k[1]);
                if(g > 0){
                    return saveChannel(g,content);
                }else{
                    return "CA,fail,auth";
                }
            }else if("u_origin".equals(k[0])){
                long g = checkAuth(k[1],k[2],k[1]);
                if(g > 0){
                    return saveChannel(g,content);
                }else{
                    return "CA,fail,auth";
                }
            }else if("save_pro".equals(k[0])){
                String ss = content.toString(Charset.forName("UTF-8"));
                long g = checkAuth(k[1]+ss,k[2],k[1]);
                if(g > 0){
                    return saveProtocol(g,ss);
                }else{
                    return "CA,fail,auth";
                }
            }else if("reload_node".equals(k[0])){
                String ss = content.toString(Charset.forName("UTF-8"));
                long g = checkAuth(k[1]+"reload",k[2],k[1]);
                if(g > 0){
                    return reloadNode(g);
                }else {
                    return "CA,fail,auth";
                }
            }else{
                return "CA,fail,direct";
            }
        } else {
            return "CA,fail,query";
        }
    }

    private String reloadNode(long g) {
        int shortId = IotIds.extractShorId(g);
        IotManager.getOne().getNode(shortId).init();
        return "CA,ok";
    }

    private String saveProtocol(long g, String value) throws Exception{
        try {
            int shortId = IotIds.extractShorId(g);
            IotSync.setOption("pR-" + shortId, value);
        }catch (Exception e){
            e.printStackTrace();
            return "CA,fail,saving,"+e.getMessage();
        }
        return "CA,ok,"+value;
    }

    private String saveChannel(long gid,ByteBuf content) throws NumberFormatException, NullPointerException, IOException, Exception {
        int lastindex = 0;
        String l = content.toString(Charset.forName("UTF-8"));
        if(l == null) return "CA,fail,nullcontent";
        String[] k = l.split("@");
        if(k.length < 1) return "CA,fail,empty";

        int duration = IotIds.extractDuration(gid);
        int shortId = IotIds.extractShorId(gid);

        if(duration > 0) {
            MiheChannelCacheEntity mh  = IotQueryDataProvider.getCacheLastOne(shortId, duration);
            if (mh != null) {
                lastindex = Math.toIntExact(mh.getStart());
            }
            for (String m : k) {
                if (m != null && m.length() > 0) {
                    if (m.length() < 8) {
                        return "CA,fail,adding5," + Long.toHexString(lastindex);
                    }
                    int start = Integer.parseInt(m.substring(0, 8), 16);
                    String dataPart = m.substring(8);
                    String[] datas = dataPart.split(",");
                    if(datas.length != 3){
                        return "CA,fail,adding4," + Long.toHexString(lastindex);
                    }
                    int havg = Integer.parseInt(datas[0],16);
                    int hmin = Integer.parseInt(datas[1],16);
                    int hmax = Integer.parseInt(datas[2],16);
                    if (start > lastindex) {
                        IotSync.localsavecache(shortId,havg,hmin,hmax,duration,start);
                        IotCloudAutomate.ClawCheckonDuration(shortId,start,hmin,hmax,havg,duration);
                        lastindex = start;
                    } else {
                        return "CA,fail,adding3," + Long.toHexString(lastindex);
                    }
                }
            }
        }else{
            MiheChannelDataEntity mh  = IotQueryDataProvider.getDataLastOne(shortId);
            if (mh != null) {
                lastindex = Math.toIntExact(mh.getStart());
            }
            for (String m : k) {
                if (m != null && m.length() > 0) {
                    if (m.length() < 8) {
                        return "CA,fail,adding1," + Long.toHexString(lastindex);
                    }
                    int start = Integer.parseInt(m.substring(0, 8), 16);
                    long data = Long.parseLong(m.substring(8),16);
                    if (start > lastindex) {
                        IotSync.localsavedata(shortId, data, start);
                        lastindex = start;
                    } else {
                        return "CA,fail,adding2," + Long.toHexString(lastindex);
                    }
                }
            }
        }


        return "CA,ok,"+Long.toHexString(lastindex);
    }

    private long checkAuth(String orig,String ans,String gid) throws Exception {
        try {
            if(Objects.equals(SHA3Digest.StringDigest(256, orig + getAuth(gid)), ans)){
                return Long.parseLong(gid,16);
            }else{
                IotLogger.i("Fail on auth: 1-"+gid+"2-"+ans + "3-"+ SHA3Digest.StringDigest(256, orig));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        IotLogger.i("Fail on auth: excep ");
        return -100;
    }

    private String getAuth(String gid) throws Exception {
        return "#*CBDCYBVV*!*'A{5150231215671975549678070113093598753779818874173425479177197()(OP:P#!P_+)_)''''!+(@*&@&*^#)";
    }

}
