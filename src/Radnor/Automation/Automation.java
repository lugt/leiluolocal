package Radnor.Automation;

/**
 * Created by Frapo on 2017/7/10.
 * Version :23
 * Earth - Moudule Radnor.Utils
 */

import iotsampl.iot.core.IotLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Automation extends Thread{

    private boolean permit = false;
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();

    private  static final long TIME_INTERVAL = 100;

    private long lastclaw = 0;

    private static Automation _me = null;

    public Automation(){
        if(_me == null) permit = true;
    }

    public static Automation init(){
        if(_me != null) return _me;
        _me = new Automation();
        _me.start();
        return _me;
    }

    public static Automation getOne(){
        if(_me == null) init();
        return _me;
    }

    private static void parseQueue(String take) {
        if(take == null) return;
        String[] stx = take.split("::");
        if(stx.length < 2)   return;
        String center = stx[1];
        if(center == null) return;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if(!permit) {
            IotLogger.auto("Claw/Pigeon shutdown : without permit.");
            return;
        }

        try {
            System.out.println("--------------Claw/Pigeon处理线程运行了--------------");
            Pigeon.PigeonPrepare();
            while (true) {
                // 如果堵塞队列中存在数据就将其输出
                if (msgQueue.size() > 0) {
                    parseQueue(msgQueue.take());
                }
                Pigeon.PigeonHourTick();
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }

}


/**
 *
 *    计算每日的数据信息
 *    指定Hook的通道
 *
 *    慢速Thread
 *    按小时计算(Hourly Hook)
 *    按天的计算每天一个(Daily Hook)
 *
 *    Daemon检查
 *    要求每个数据区域必须提交报告情况 Start --> System.curr
 *    空闲时执行 Review Process 小时级别 检查Hook的回复情况
 *    一个Hook记录所有已经
 *
 *    Hook 内部：
 *    取区域内的数据
 *    算平均值，最大值，最小值
 *    报给Claw进行确认
 *    入库
 *    记录Hook已经完成相关任务
 *
 *    快速反应 Claw Thread
 *    Claw 内部数据 -- 根据情况判断
 *
 *
 *
 *
 *
 * */
