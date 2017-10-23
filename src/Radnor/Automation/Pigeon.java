package Radnor.Automation;

import iotsampl.iot.core.IotLogger;
import iotsampl.iot.core.IotManager;
import iotsampl.iot.data.IotSync;
import iotsampl.iot.oo.MiheChannelCacheEntity;
import iotsampl.iot.oo.MiheChannelDataEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pigeon {


    private static final long PIGEON_HOUR = 3600000;

    private static long lastpigeon = 0;

    private static final long PIGEON_TICK = 36000;

    private static long lasttick = 0;


    static List<Integer> PigeonHookList = new ArrayList<>();

    public static void PigeonPrepare() {
        IotSync.init();
        PigeonHookList = IotManager.getOne().getHourList();
    }

    public static void PigeonHourTick() {
        if(System.currentTimeMillis() - lastpigeon > PIGEON_HOUR){
            // expire
            PigeonHook(3600);
            PigeonHook(3600 * 24);
            lastpigeon = System.currentTimeMillis() ;
            lastpigeon -= lastpigeon % 3600000;
        }

        if(System.currentTimeMillis() - lasttick > PIGEON_TICK){
            PigeonHook(36);
            PigeonHook(72);
            PigeonHook(216);
            lasttick = System.currentTimeMillis();
            lasttick -= lasttick % 36000;
        }
    }

    private static void PigeonHook(int duration){
        // Load last check
        // Range
        for (int a: PigeonHookList) {
            long now = System.currentTimeMillis() / 1000;
            long index;
            try {
                String lastcheck = IotSync.localgetOption(duration+"-index-"+a);
                if(lastcheck == null){
                    index = 0;
                }else {
                    index = Long.parseLong(lastcheck);
                }
                // 检查是否已经过期
                if(now - index > duration * 2){
                    // 2个小时以上了-- 中间为何没有检测--可能掉线--记录特殊情况
                    IotLogger.auto("Pigeon last Registered over "+duration/1800+"hrs : " + index + ";");
                    //IotSync.clusterRpt(a,0,now,"120"+duration/1800+"01");
                    //小时级别缓存
                    if(!PigeonExpire(now, index, a, duration)){
                        IotLogger.auto("Pigeon Expire failed some how");
                    }
                }else if(now - index >= duration){
                    //正常小时缓存
                    //小时级别缓存
                    if(!PigeonExpire(now, index, a, duration)){
                        IotLogger.auto("Pigeon Nexpire failed");
                    }
                }

                // 还未超时
            }catch (NumberFormatException | NullPointerException e){
                IotLogger.auto("pigeon hook exception : "+e.getMessage());
            }catch (IOException e){
                e.printStackTrace();
            }

        }

    }

    /**
     * 缓存器
     * 检查是否足够Duration
     * 便开始操作
     *
     * */
    private static boolean PigeonExpire(long now, long index,int a, int duration)
            throws NumberFormatException,  NullPointerException, IOException
    {
        // 检查是否需要增加缓存

        /*index 是当前的最后确认过的位置*/
        // TODO:检查是否存在计算结果，但是index未记录
        //long total = IotSync.localcount(a, index, now);
        // 取得区间内
        long preindex = 0;
        List<MiheChannelDataEntity> single = IotSync.localgetdata(a,index + 1, now, 1, false);
        if(single != null && single.size() == 1) {
            preindex = single.get(0).getStart();
        }else{
            // 从 0 到 now 没有数据 = 完全没有数据
            if(index == 0) return false;
            // 不是从0开始的，OK那可能就是还没到新的一个小时
            preindex = index;
        }
        //调整到小时开头
        preindex -= preindex % duration;
        now -= now % duration;
        for(; preindex<now; preindex += duration){
            // 从preindex 的小时起 挨个duration计算平均值
            // 写到duration对应的数据列表里
            List<MiheChannelCacheEntity> sssx = IotSync.localgetcache(a, preindex - 1, preindex + 1, duration, 1,false);
            if(sssx != null && sssx.size() == 1){
                // preindex统计的数值 已经存在，更新index
                if(!IotSync.setOption(duration+"-index-" + a , Long.toString(preindex))){
                    return false;
                }
                continue;
            }
            // 全新的需要计算的位置
            try {
                if(!PigeonFetchOne(a, preindex, duration)){
                    return false;
                }
                //*    记录Hook已经完成相关任务

                IotSync.setOption(duration+"-index-" + a , Long.toString(preindex));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static boolean PigeonFetchOne(int id, long hourStart, int duration) throws Exception {

        int hmin = -10000,hmax = -10000,havg = -10000, sum = -10000;

        // Math 40000001
        if(id > 40000000 && id < 40200000){
            hmin = -1000000;
            hmax = -1000000;
            havg = -1000000;
            sum  = -1000000;
        }else if(id > 40200000 && id < 40300000){
            hmin = -100000;
            hmax = -100000;
            havg = -100000;
            sum = -100000;
        }

        if(hourStart % duration != 0){
            // Not a Start
            return false;
        }
        boolean first = true;
        // 处理单个节点-单个小时内的数据 1200 左右
        List<MiheChannelDataEntity> hourInner = IotSync.localgetdata(id, hourStart,hourStart + duration - 1, 86400, false);
        if(hourInner != null && hourInner.size() > 0) {
            for (MiheChannelDataEntity df : hourInner) {
                try {
                    //long time = Long.parseLong(df.substring(0, 8), 16);
                    int data = Math.toIntExact(df.getData());
                    if(first){
                        hmax = data;
                        hmin = data;
                        sum = data;
                        first = false;
                    }else {
                        if (data > hmax) hmax = data;
                        if (data < hmin) hmin = data;
                        sum += data;
                    }
                } catch (Exception e) {
                    IotLogger.auto("Pigeon: 计算出错 : " + e.getMessage());
                    return false;
                }
            }

            if(IotManager.getOne().getNode(id).isAvg()) {
                havg = sum / hourInner.size();
            } else if(IotManager.getOne().getNode(id).isSum()){
                // 否则就用sum
                havg = hmax - hmin;
            }
            IotLogger.auto("Pigeon: 有效数据已经记录 on id:" + id + ", hour:" + hourStart);
        }else{
            IotLogger.auto("Pigeon: 空(-100)数据已经记录 on id:" + id + ", hour:" + hourStart);
        }
        // 记录数据信息
        IotSync.localsavecache(id,havg,hmin,hmax,duration, (int) hourStart);
        return true;
    }

}
