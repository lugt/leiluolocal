package iotsampl.iot.data;

import earth.server.Monitor;
import iotsampl.DataService;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.oo.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frapo on 2017/8/8.
 * Version :17
 * Earth - Moudule iotsampl.iot
 */
public class IotSync {

    private static List<DataBean> waitList = new ArrayList<>();

    /**
     *
     * 负责同步数据信息
     * 2017/08/08
     *
     * */

    public static boolean inis = false;
    public static void init(){
        // TODO:读取本地同步情况
        if(inis)return;
        inis = true;
        try {
            DataService.setUp();
            IotLogger.i("启动数据服务已经完毕");
        } catch (Exception e) {
            Monitor.error("Hibernate 服务启动失败" +e.getMessage());
            System.exit(0);
        }
    }

    // @param 数据节点Id / 数据
    public static void addSyncIntent(int id, DataBean b){
       // id / b
        b.setSyncId(id);
        try {
            localSave(id,b.time,b.getData());
        } catch (IOException e) {
            IotLogger.o(e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void localSave(int chid, long time, long data) throws ArithmeticException,IOException {
        // Check Status

        if(DataService.isSessionAlive()) {
            localsavedata(chid, data, Math.toIntExact(time));
            /*long m = rdc.zadd("ch" + id, time, data);
            if(m <= 0){
                throw new IOException("Redis zadd fail -- " + id + " :: "+ time +" :: "+ data + " //// " + m);
            }*/
        }
    }

    /*
    public static List<String> localget(long id, long start, long end, int len){
        List<String> x =  rdc.zrange("ch" + id, start, end, len);
        return x;
    }

    public static List<String> localvget(long id, long start, long end, int len){
        List<String> x =  rdc.zrevrange("ch" + id, start, end, len);
        return x;
    }



    public static void localzadd(String id, long score, String data) {
        // Check Status
        if(rdc != null) {
            long m = rdc.zadd(id, score , data);
            if(m <= 0){
                throw new IOException("Redis zadd fail -- " + id + " :: "+ score +" :: "+ data + " //// " + m);
            }
        }
    }*/
    public static List<MiheWarnsEntity> localgetwarn(int chid, long start, long end, int len,boolean rev){
        try {
            Session session = DataService.getSession();
            Transaction tx = DataService.getTransact(session);
            String xtr;
            if(rev) {
                xtr = "order by start desc";
            }else{
                xtr = "order by start";
            }
            Query q = session.createQuery("from MiheWarnsEntity where chid = :chp and start >= :st and start <= :en " + xtr);
            q.setMaxResults(len);
            q.setParameter("chp", chid);
            q.setParameter("st", start);
            q.setParameter("en", end);
            //MiheChannelDataEntity udE = (MiheChannelDataEntity) q.uniqueResult();
            List<MiheWarnsEntity> x = q.getResultList();
            tx.commit();
            // rdc.zrange(id, start, end, len);
            return x;
        }catch (Exception e){
            return null;
        }
    }

    public static List<MiheChannelDataEntity> localgetdata(int chid, long start, long end, int len, boolean rev){
        try {
            Session session = DataService.getSession();
            Transaction tx = DataService.getTransact(session);
            String xtr;
            if(rev) {
                xtr = "order by start desc";
            }else{
                xtr = "order by start";
            }
            Query q = session.createQuery("from MiheChannelDataEntity where chid = :chp and start >= :st and start <= :en " + xtr);
            q.setMaxResults(len);
            q.setParameter("chp", chid);
            q.setParameter("st", start);
            q.setParameter("en", end);
            //MiheChannelDataEntity udE = (MiheChannelDataEntity) q.uniqueResult();
            List<MiheChannelDataEntity> x = q.getResultList();
            tx.commit();
            // rdc.zrange(id, start, end, len);
            return x;
        }catch (Exception e){
            return null;
        }
    }

    public static List<MiheChannelCacheEntity> localgetcache(int chid, long start, long end, int duration, int len,boolean rev){

        try {
            Session session = DataService.getSession();
            Transaction tx = DataService.getTransact(session);
            String xtr;
            if(rev) {
                xtr = "order by start desc";
            }else{
                xtr = "order by start";
            }
            Query q = session.createQuery("from MiheChannelCacheEntity where chid = :chp and duration = :dur and start >= :st and start <= :en " + xtr);
            q.setMaxResults(len);
            q.setParameter("chp", chid);
            //Duration dr =  Duration.ANNUAL;
            q.setParameter( "dur" , translateDuration(duration));
            q.setParameter("st", start);
            q.setParameter("en", end);
            //MiheChannelDataEntity udE = (MiheChannelDataEntity) q.uniqueResult();*/
            List<MiheChannelCacheEntity> x = q.getResultList();
            tx.commit();
            // rdc.zrange(id, start, end, len);
            return x;
        }catch (Exception e){
            IotLogger.i("POIT2911");
            e.printStackTrace();
            return null;
        }
    }

    public static String localgetOption(String id) {
        String val = null;
        Transaction tx;
        Session session;
        try {
            session = DataService.getSession();
            tx = DataService.getTransact(session);
        }catch (Exception e){
            return val;
        }
        try {
            Query q = session.createQuery("from MiheOptionEntity where name = :cid");
            q.setParameter("cid", id);
            MiheOptionEntity x = (MiheOptionEntity) q.uniqueResult();
            session.getTransaction().commit();
            //String x =  rdc.getValue(id);
            if(x != null) {
                val =x.getVal();
            }
        }catch (Exception e){
            IotLogger.i("POIT2014 -- " + e.getMessage());
        }

        return val;
    }

    public static boolean localsavedata(int chid, long data,int start){
        try {
            Session session = DataService.getSession();
            DataService.getTransact(session);
            MiheChannelDataEntity v = new MiheChannelDataEntity();
            v.setChid(chid);
            v.setData(data);
            v.setStart(start);
            session.save(v);
            session.getTransaction().commit();
            return true; //rdc.setValue(s,value);
        }catch (NullPointerException e){
            e.printStackTrace();
            IotLogger.i("NullPointer - POI@@32");
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean localsavecache(int chid, int avg,int min, int max,int duration,int start){
        try {
            Session session = DataService.getSession();
            DataService.getTransact(session);
            MiheChannelCacheEntity v = new MiheChannelCacheEntity();
            v.setChid(chid);
            v.setDuration(translateDuration(duration));
            v.setStart((long) start);
            v.setAvg((long) avg);
            v.setMax((long) max);
            v.setMin((long) min);
            session.save(v);
            session.getTransaction().commit();
            return true; //rdc.setValue(s,value);
        }catch (NullPointerException e){
            e.printStackTrace();
            IotLogger.i("NullPointer - POI@@32");
            return false;
        }catch (Exception e){
            IotLogger.i("NullPointer - POI290 " + e.getMessage());
            return false;
        }
    }

    private static int translateDuration(int duration) {
        switch (duration){
            case 36:
                return 1;
            case 72:
                return 2;
            case 216:
                return 3;
            case 3600:
                return 4;
            case 86400:
                return 5;
            default:
                return 0;
        }
    }

    public static boolean setOption(String s, String value){
        try {
            Session session = DataService.getSession();
            DataService.getTransact(session);
            Query q = session.createQuery("from MiheOptionEntity where name = :cid");
            q.setParameter("cid", s);
            MiheOptionEntity v = (MiheOptionEntity) q.uniqueResult();
            if (v == null) {
                v = new MiheOptionEntity();
                v.setName(s);
            }
            v.setVal(value);
            session.save(v);
            session.getTransaction().commit();
            return true; //rdc.setValue(s,value);
        }catch (NullPointerException e){
            e.printStackTrace();
            IotLogger.i("NullPointer - POI@@32");
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getConnected() {
        if(!DataService.isSessionAlive())return false;
        try{
            Session session = DataService.getSession();
            Transaction tx = DataService.getTransact(session);
            Query q = session.createQuery("from MiheOptionEntity where name = :cid");
            q.setParameter("cid","lastrun");
            q.uniqueResult();
            tx.commit();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void clusterRpt(int clust, int id, int situationId) throws IOException {
        long time = System.currentTimeMillis();
        localsavewarn(clust,  situationId);
        System.currentTimeMillis();
        localsavewarn(id, situationId);
    }

    public static void localsavewarn(int id, int data) {
        /**/
        MiheWarnsEntity mwe = new MiheWarnsEntity();
        mwe.setChid(id);
        mwe.setWarn(data);
        mwe.setStart(System.currentTimeMillis());
        try {
            Session session = DataService.getSession();
            DataService.getTransact(session);
            session.save(mwe);
            session.getTransaction().commit();
            Thread.sleep(10);
        }catch (Exception e){
            IotLogger.i(e.getMessage());
        }
    }

    public static long removeNotify(int chid, long start) {
        try {
            Session session = DataService.getSession();
            DataService.getTransact(session);
            Query q = session.createQuery("from MiheWarnsEntity where chid = :cid and start = :st");
            q.setParameter("cid", chid);
            q.setParameter("st", start);
            MiheWarnsEntity v = (MiheWarnsEntity) q.uniqueResult();
            if (v != null) {
                session.delete(v);
                //提交事务.把内存的改变提交到数据库上.
                session.getTransaction().commit();
                return 1;
            }else{
                session.getTransaction().commit();
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        //return rdc.zremoveScore(n,id,id);
    }

    public static long removeClustNotify(int chid, Long id) {
        try {
            Session session = DataService.getSession();
            DataService.getTransact(session);
            Query q = session.createQuery("from MiheWarnsEntity where chid = :cid and start = :st");
            q.setParameter("cid", chid);
            q.setParameter("st", id);
            MiheWarnsEntity v = (MiheWarnsEntity) q.uniqueResult();
            if (v != null) {
                session.delete(v);
                session.getTransaction().commit();
                return 1;
            }else{
                session.getTransaction().commit();
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
