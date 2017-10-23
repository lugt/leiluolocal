package Radnor.Utils;

import io.netty.buffer.ByteBuf;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.data.IotSync;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.util.List;
import java.util.Objects;

/**
 * Created by Frapo on 2017/8/9.
 * Version :14
 * Earth - Moudule Radnor
 */
public class RadnorParse {
    public String deliver(String q, ByteBuf byteBuf) {
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
                return getValueBySE(x);
            }catch (Exception e){
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0], "node-b")){
            try {
                return getValueRevSE(x);
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
                return "DL,fail,"+e.getLocalizedMessage();
            }
        }else if(Objects.equals(x[0], "option")){
            return getValueOption(x);
        }else{
            return "DL,fail,nact";
        }
    }

    private String getValueOption(String[] x) {
        if(x.length >= 2) {
            String vid = x[1];
            vid = vid.replaceAll("[^(A-Za-z)]", "");
            return IotSync.localgetOption(x[1]);
        }else{
            return "DL,fail,param";
        }
    }

    private String getValueCacheDur(String[] x, boolean rev) throws NumberFormatException,IndexOutOfBoundsException {

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
            }else{
                end = System.currentTimeMillis() / 1000;
                //end -= end % 3600 * 24;
                end += 1200 * duration * d;
            }
        }else {
            end = Long.parseLong(x[5]);
        }
        List<String> b = null;
        if(rev) {
            b  = LocalDataProvider.getNodeRevCacheList(id, st, end, len, duration);
        }else {
            b = LocalDataProvider.getNodeCacheList(id, st, end, len, duration);
        }
        return parseListRtn(b);
    }

    private String parseListRtn(List<String> b) throws JSONException,NumberFormatException{
        if(b.size() <= 0) {
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

    private String getValueRevSE(String[] x) throws JSONException,NumberFormatException{

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
            }else{
                end = System.currentTimeMillis() / 1000;
                //end -= end % 3600 * 24;
                end += 3600 * d;
                st = end - 3600;
            }
        }else {
            end = Long.parseLong(x[4]);
        }

        List<String> b = LocalDataProvider.getNodeDataList2(id, st, end, len);

        return parseListRtn(b);
    }

    private String getValueBySE(String[] x) throws JSONException,NumberFormatException {

        Long id = Long.parseLong(x[1]);
        long t = System.currentTimeMillis() / 1000;
        long start;
        if(!"default".equals(x[2])) {
            start = Long.parseLong(x[2]);
        }else {
            start = t - 100;
        }

        if(start > t){
            IotLogger.i("DL,fail,chrome");
        }

        List<String> b = LocalDataProvider.getNodeDataList(id, start, t, 100);
        return parseListRtn(b);
    }

    public String usejs() {
        return "var radnorjs = 1; function getpnr(ur){" +
                "jQuery.ajax({'url':ur})" +
                "}";
    }
}

