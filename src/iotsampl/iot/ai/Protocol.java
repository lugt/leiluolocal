package iotsampl.iot.ai;

import java.util.ArrayList;
import java.util.List; /**
 * Created by Frapo on 2017/8/13.
 * Version :22
 * Earth - Moudule Radnor
 */
public class Protocol {

    // Protocol Error Id
    public int echo;


    // 类型
    public int type;

    //在此区域中
    public long avgmin;
    public long avgmax;

    public long maxTop;
    public long minLow;

    // 要求时间长度
    public int period;

    // 处于危险期中的时间
    public long elapsed;

    // 附加的设备
    public long connectId;

    // 设备对应的Protocol
    public Protocol connPt;

    // 当前状态
    public int status;

    public static Protocol parse(String[] k) throws ArrayIndexOutOfBoundsException{
        if(k.length < 2) return null;
        Protocol pt = new Protocol();
        pt.type = Integer.parseInt(k[0]);
        pt.echo = Integer.parseInt(k[1]);
        switch (pt.type){
            case 1:     // 单次数据范围
                pt.avgmin = Long.parseLong(k[2]);
                pt.avgmax = Long.parseLong(k[3]);
                // 超范围一次报警
                break;
            case 2:    // 一定时间报警
                pt.avgmin = Long.parseLong(k[2]);
                pt.avgmax = Long.parseLong(k[3]);
                pt.period = Integer.parseInt(k[4]); // 单位：s
                break;
            case 3:    // 小时检测
                pt.avgmin = Long.parseLong(k[2]);
                pt.avgmax = Long.parseLong(k[3]);
                pt.maxTop = Long.parseLong(k[4]);
                pt.minLow = Long.parseLong(k[5]);
                break;
            case 4:    // 按天检测
                pt.avgmin = Long.parseLong(k[2]);
                pt.avgmax = Long.parseLong(k[3]);
                pt.maxTop = Long.parseLong(k[4]);
                pt.minLow = Long.parseLong(k[5]);
                break;
            case 7:    // 小时检测 空数据
                break;
            case 8:    // 天检测 空数据
                break;
            case 9:    // 长度检测空数据
                pt.period = Integer.parseInt(k[2]);
                break;
            case 501: // 联合其它内容的情况
                break;
        }
        return pt;
    }

    public static List<Integer> test_single(List<String> ans, Protocol pt) {
        List<Integer> errid = new ArrayList<>();
        for (int i = ans.size() - 1 ; i >= 0   ; i--) {
            String data = ans.get(i);
            if(data == null || data.length() <= 8){
                return errid;
            }
            // 处理单条数据
            long time = Long.parseLong(data.substring(0,8),16);
            long havg = Long.parseLong(data.substring(8),16);
            // Protocol 逐个轮询
            int ex = 0;
            if (havg >= pt.avgmax) {
                ex = 10;
            } else if (havg <= pt.avgmin) {
                ex = 11;
            }
            if (ex != 0) {
                if (pt.type == 1) {
                    if (pt.status == 0) {
                        errid.add(ex);
                        pt.status = 30;
                    }
                } else if (pt.type == 2) {
                    if (pt.elapsed == 0) {
                        pt.elapsed = time;
                        pt.status = 30;
                    } else if (pt.status == 30) {
                        if ((time - pt.elapsed) > pt.period) {
                            errid.add(ex);
                            pt.status = 50;
                        } else {
                            // 还没到时间
                            //pt.status = 30;
                        }
                    } // 可能是50 - 已经记录过，就不继续记录了
                }
            } else {
                if (pt.status == 30 || pt.status == 50) {
                    pt.elapsed = 0;
                    if(pt.status == 50)  errid.add(99);
                    pt.status = 0;
                }
            }
        }
        return errid;
    }
}
