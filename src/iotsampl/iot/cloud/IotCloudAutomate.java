package iotsampl.iot.cloud;

import iotsampl.iot.core.IotLogger;
import iotsampl.iot.core.IotManager;
import iotsampl.iot.core.IotNode;
import iotsampl.iot.data.IotSync;
import iotsampl.iot.oo.DataBean;
import iotsampl.iot.oo.MiheChannelDataEntity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Frapo on 2017/8/8.
 * Version :17
 * Earth - Moudule iotsampl.iot
 */
public class IotCloudAutomate extends Thread{

    private static List<DataBean> waitList = new ArrayList<>();
    private static boolean connected = false;

    // 配置信息
    //  Chid : original
    //  Chid : duration-cache
    //  chid : warn

    public static boolean getConnected() {
        return connected;
    }

    private boolean permit = false;

    static IotCloudAutomate io = null;

    public static IotCloudAutomate getOne(){
        if(io != null) return io;
        io = new IotCloudAutomate();
        io.permit = true;
        return io;
    }

    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();

    private  static final long TIME_INTERVAL = 10000;

    private long lastclaw = 0;

    private static void parseQueue(String take) {

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if(!permit) {
            IotLogger.auto("Cloud / Claw shutdown : without permit.");
            return;
        }

        try {
            System.out.println("--------------Cloud / Claw 已经开始运行--------------");
            ClawPrepare();
            while (true) {
                // 如果堵塞队列中存在数据就将其输出
                if (msgQueue.size() > 0) {
                    parseQueue(msgQueue.take());
                }
                ClawTick();
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }

    List<Integer> ClawList;

    private void ClawPrepare() {
        // 读取Claw的定时器列表
        ClawList = new ArrayList<>();
        List<Integer> lst = IotManager.getOne().getClawList();
        ClawList.addAll(lst);
        for(int a:ClawList){
            ikas.put(a,0);
        }
    }

    private void ClawTick() {
        if(System.currentTimeMillis() - lastclaw > TIME_INTERVAL){
            // expire
            ClawExec();
            //TODO: 交给Manager自动检测数据失联
            lastclaw = System.currentTimeMillis();
            //OK

        }
    }

    Map<Integer,Integer> ikas = new HashMap<>();

    private void ClawExec() {
        // 有一个标志告诉我们上一次结束是在什么时候
        for (int a: ClawList) {
            long now = System.currentTimeMillis() / 1000;
            long index;
            try {
                String lastcheck = IotSync.localgetOption("cI-"+Long.toHexString(a));
                if(lastcheck == null){
                    if(ikas.get(a) == 0) {
                        IotLogger.auto("Claw : index was reset on id-" + a);
                        ikas.put(a,10);
                    }
                    index = 0;
                }else {
                    index = Long.parseLong(lastcheck);
                }
                // 检查是否已经过期
                if(now - index > 120){
                    // 2个小时以上了-- 中间为何没有检测--可能掉线--记录特殊情况
                    if(ikas.get(a) == 0) {
                        ikas.put(a,10);
                        IotLogger.auto("Claw : 上一次执行是 : " + index + " , id =" +a);
                        IotSync.clusterRpt(getCluster(a), a,  170001);
                    }

                    // TODO:warn
                    //缓存
                    if(!ClawExpire(now, index, a)){
                        if(ikas.get(a) == 0) {
                            ikas.put(a,10);
                            IotLogger.auto("Claw : 执行Expire出错 !");
                            IotSync.clusterRpt(getCluster(a),a,170003);
                        }
                        return;
                    }else {
                        if(now - index < 120) {
                            ikas.put(a, 0);
                            IotSync.clusterRpt(getCluster(a),a, 170099);
                        }
                    }
                }else if(now - index >= 1){
                    //正常小时缓存
                    //小时级别缓存
                    if(ikas.get(a) != 0){
                        ikas.put(a,0);
                        IotSync.clusterRpt(getCluster(a),a,170099);
                    }

                    if(!ClawExpire(now, index, a)){
                        long t = System.currentTimeMillis();
                        IotLogger.auto("Claw : 执行Expire出错,但是index和now非常接近 - POI3A@4");
                        IotSync.clusterRpt(getCluster(a),a,170005);
                    }
                }

            }catch (Exception e){
                index = 0;
                e.printStackTrace();
            }

        }

    }

    private int getCluster(long a) {
        return 0;
    }

    private boolean ClawExpire(long now, long index, int id) {
        List<MiheChannelDataEntity> pm = IotSync.localgetdata(id,index + 1,now, 62000, false);
        //List<String> single = IotQueryDataProvider.getNodeOrig();
        //List<String> single = IotSync.localgetscore("gh"+id,index + 1, now,62000);
        long t = 0;
        if(pm != null && pm.size() > 0) {
            for (MiheChannelDataEntity first: pm) {
                try {

                    if(ClawNext(id, first)) {
                        t = first.getStart();
                    }
                } catch (NumberFormatException e){
                    IotLogger.auto("claw expire : number format error POINT35@a");
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            // 沒有執行任何一个操作，不应该的
            if(t == 0) return false;

            //*    记录Hook已经完成相关任务
            IotSync.setOption("cI-" + Long.toHexString(id), Long.toString(t));
            return true;

        }else{

            // 从 0 到 now 没有数据 = 完全没有数据
            if(index == 0) return false;
            return true;
        }
    }

    private boolean ClawNext(int chid, MiheChannelDataEntity mh) {
        try {
            IotManager.getOne().getNode(chid).report_data(mh.getStart(), mh.getData());
            return true;
        } catch (IOException e) {
            IotLogger.auto("Claw : report data failed - "+e.getMessage());
            return false;
        }
    }

    public static void startIotSync() {
        IotSync.init();
        if(!IotSync.getConnected()){
            IotLogger.i("Data Service conn failed , aboting. ");
            return;
        }
    }


    public static void ClawCheckonDuration(int id, long hourStart, long hmin, long hmax, long havg,int duration) {
        /// Nothing Major
        // 按照规则进行检查
        // 读取规则
        IotNode node = IotManager.getOne().getNode(id);
        try {
            node.report_duration(hourStart,hmin,hmax,havg,duration);
        } catch (IOException e) {
            IotLogger.auto("Claw : duration record fail -- "+duration+" s-- id:"+id+" min:"+hmin+" max:"+hmax+" avg:" + havg);
            e.printStackTrace();
        }
    }


}
