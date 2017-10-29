package iotsampl.iot.cloud;

import io.netty.buffer.ByteBuf;
import iotsampl.iot.ai.Protocol;
import iotsampl.iot.core.IotIds;
import iotsampl.iot.data.IotQueryDataProvider;
import iotsampl.iot.core.IotManager;
import iotsampl.iot.data.IotSync;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Created by Frapo on 2017/8/9.
 * Version :14
 * Earth - Moudule Radnor
 */
public class IotQuery {
    public static String deliver(String q, ByteBuf byteBuf) {
        // get all bufs
        if(q == null){
            return "DL,fail,noparam";
        }
        String[] x = q.split(","); // Cell,
        if(x.length < 1){
            return "DL,fail,param";
        }

        if(Objects.equals(x[0], "node-a")){
            try {
                return getNodeValue(x,false);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0], "node-b")){
            try {
                return getNodeValue(x,true);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0], "cache-a")){
            try {
                return getValueCacheDur(x,false);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }

        }else if(Objects.equals(x[0], "cache-b")){
            try {
                return getValueCacheDur(x,true);
            }catch (Exception e){
                e.printStackTrace();
                return "DL,fail,exp,"+e.toString();
            }
        }else if(Objects.equals(x[0], "warn-a")){
            try {
                return getValueWarnD(x,true);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0],"savepro")){
            try {
                return saveProtocol(x);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0], "option")){
            return getValueOption(x);
        }else if(Objects.equals(x[0], "test-proto")){
            try {
                return getTestProto(x);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0], "dismiss")){
            try {
                return getWarnDismiss(x);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else{
            return "DL,fail,nact";
        }
    }

    private static String getWarnDismiss(String[] x) throws NumberFormatException, Exception{
        //x 是单条
        Long TimeStamp = Long.parseLong(x[1]);
        if(x[2].startsWith("cluster_")){
            if(IotQueryDataProvider.removeNotificationCluster(TimeStamp,0)){
                List<String> v = IotQueryDataProvider.getWarnRListClust(0, 7);
                if(v != null && v.size() > 0) {
                    return parseListRtn(v);
                }else{
                    return "DL,fail,nodata";
                }
            }else{
                return "DL,fail,false";
            }
        }else{
            long LongId = Long.parseLong(x[2]);
            int shortId = IotIds.extractShorId(LongId);
            if(IotQueryDataProvider.removeNotification(TimeStamp,shortId)) {
                List<String> v = IotQueryDataProvider.getWarnRList(shortId, 7);
                if(v != null && v.size() > 0) {
                    return parseListRtn(v);
                }else{
                    return "DL,fail,nodata";
                }
            }else{
                return "DL,fail,false";
            }
        }
    }

    private static String saveProtocol(String[] value) throws Exception{
        try {
            long m = Long.parseLong(value[1]);
            int shortId = IotIds.extractShorId(m);
            String[] mj = value[2].split(";;");
            for(String ty :mj){
                String[] prr = ty.split("@");
                Protocol ptc = Protocol.parse(prr);
                if(ptc == null){
                    return "CA,fail,nopass,"+ty;
                }
            }
            if(!IotSync.setOption("pR-" + shortId, value[2])){
                return "CA,fail,hiber,poi@21";
            }
            if(!IotManager.getOne().getNode(shortId).init()){
                return "CA,fail,setting";
            }

        }catch (Exception e){
            e.printStackTrace();
            return "CA,fail,saving,"+e.getMessage();
        }
        return "CA,ok,set";
    }

    private static String getTestProto(String[] x) throws NumberFormatException, IOException{
        //proto
        if(x.length == 5){
            long id = Long.parseLong(x[1]);
            Long start = Long.parseLong(x[2]);
            int len = Integer.parseUnsignedInt(x[3]);
            long now = System.currentTimeMillis() / 1000;
            String prt  = x[4];
            String[] prr = prt.split("@");
            Protocol ptc = Protocol.parse(prr);
            List<String> ans;
            if(ptc != null && (ptc.type == 1 || ptc.type == 2)){
                // 直接数据检验
                // 反向取值
                ans = IotQueryDataProvider.getNodeOrig(id,start,now,len,false);
                if(ans == null || ans.size() < 1){
                    return "DL,fail,ZeroData";
                }
                List<Integer> eco = Protocol.test_single(ans,ptc);
                if(eco.size() > 0) {
                    JSONStringer son = new JSONStringer();
                    JSONWriter jsw = son.array();
                    for (int i = 0; i < eco.size(); i++) {
                        jsw.value(eco.get(i));
                    }
                    String ends = jsw.endArray().toString();
                    return "DL,ok," + ends;
                }else{
                    return "DL,ok,[]";
                }
            }else{
                return "DL,fail,ErrorProto";
            }
        }else{
            return "DL,fail,paramNum";
        }
    }

    private static String getValueWarnD(String[] x, boolean rev) {
        Long longChannelId = Long.parseLong(x[1]);
        int len = Integer.parseUnsignedInt(x[2]);
        List<String> r;
        if (rev){
            r = IotQueryDataProvider.getWarnRList(longChannelId, len);
        }else{
            r = IotQueryDataProvider.getWarnList(longChannelId, len);
        }
        return parseListRtn(r);
    }

    private static String getValueOption(String[] x) {
        if(x.length >= 2) {
            String vid = x[1];
            vid = vid.replaceAll("[^(A-Za-z)]", "");
            return IotSync.localgetOption(vid);
        }else{
            return "DL,fail,param";
        }
    }

    private static String getValueCacheDur(String[] x, boolean rev) throws NumberFormatException,IndexOutOfBoundsException {

        if(x.length != 6 && x.length != 7) {
            return "DL,fail,paramnum";
        }

        Long id = Long.parseLong(x[1]);
        int duration = Integer.parseUnsignedInt(x[2]);
        int len = Integer.parseUnsignedInt(x[3]);
        long st = Long.parseLong(x[4]);
        long end = 0;

        if("default".equals(x[5])) {
            end = System.currentTimeMillis() / 1000;
        }else if("dayat".equals(x[5])){
            // 今日
            long d = Long.parseLong(x[6]);
            if(d >= 0){
                end = System.currentTimeMillis() / 1000;
                st = end - 1200 * duration;
            }else{
                end = System.currentTimeMillis() / 1000;
                //end -= end % 3600 * 24;
                end += 1200 * duration * d;
                st = end - 1200 * duration;
            }
        }else {
            end = Long.parseLong(x[5]);
        }
        List<String> b = null;

        b  = IotQueryDataProvider.getNodeDur(IotIds.extractShorId(id), st, end, len, duration, rev);

        return parseListRtn(b);
    }

    private static String parseListRtn(List<String> b) throws JSONException,NumberFormatException{
        if(b == null || b.size() <= 0) {
            return "DL,fail,nodata";
        }else{
            JSONStringer son = new JSONStringer();
            JSONWriter jsw = son.array();

            for (int i = 0; i < b.size(); i++) {
                jsw.value(b.get(i));
            }
            String ends = jsw.endArray().toString();
            return "DL,ok,"+ends;
        }
    }

    private static String getNodeValue(String[] x, boolean rev) throws JSONException,NumberFormatException{

        if(x.length != 5 && x.length != 6 && x.length != 7) {
            return "DL,fail,paramnum";
        }

        Long id = Long.parseLong(x[1]);
        int len = Integer.parseUnsignedInt(x[2]);

        long st = Long.parseLong(x[3]);
        long end = 0;

        if("default".equals(x[4])) {
            end = System.currentTimeMillis() / 1000;
        }else if("dayat".equals(x[4])){
            // 今日
            long d = Long.parseLong(x[5]);
            if(d >= 0){
                end = System.currentTimeMillis() / 1000;
                st = end - 3600;
            }else{
                end = System.currentTimeMillis() / 1000;
                //end -= end % 3600 * 24;
                end += 3600 * d;
                st = end - 3600;
            }
        }else {
            end = Long.parseLong(x[4]);
        }

        List<String> b = IotQueryDataProvider.getNodeOrig(id, st, end, len, rev);

        return parseListRtn(b);
    }

}

