package iotsampl.iot.core;

import iotsampl.iot.ai.Protocol;
import iotsampl.iot.data.IotSync;
import iotsampl.iot.oo.DataBean;
import iotsampl.iot.oo.DataManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frapo on 2017/8/8.
 * Version :15
 * Earth - Moudule iotsampl.iot
 */
public class IotNode {

    /**
     * 本地ID
     */
    private int id;
    private int cluster = 0 ;
    /**
     * 在线ID
     */
    private long globalId;

    public IotNode(int i) {
        id = i;
    }

    private List<Protocol> ptc_list = new ArrayList<>();
    private List<Protocol> ins_list = new ArrayList<>();
    private List<Protocol> duration_lst = new ArrayList<>();

    public void quick_record(String data, long time) {
        // 先转DataBean
        long rf = Long.parseLong(data,16);
        DataBean b = DataManager.prepare(rf, time);
        IotSync.addSyncIntent(id, b);
        IotLogger.i("Saving - "+id+" : " + data);
    }

    public boolean isAvg() {
        if (id > 30300000 && id < 30310000) {
            return true;
        } else if (id > 40100000 && id < 40300000) {
            return true;
        } else {
            return false;
        }
    }

    public void report_data(long time, Long data) throws IndexOutOfBoundsException, IOException, NumberFormatException {
        // 处理单条数据
        long havg = data;
        // Protocol 逐个轮询
        // get - Protocols
        for (Protocol pt : ins_list) {
            // 判断最大，最小值，如果超过，就记录elapsed
            int ex = 0;
            if (havg >= pt.avgmax) {
                ex = 10;
            } else if (havg <= pt.avgmin) {
                ex = 11;
            }
            if (ex != 0) {
                if (pt.type == 1) {
                    if (pt.status == 0) {
                        long t = System.currentTimeMillis();
                        IotSync.clusterRpt(cluster,id, pt.echo * 100 + ex);
                        pt.status = 50;
                    }
                } else if (pt.type == 2) {
                    if (pt.elapsed == 0) {
                        pt.elapsed = time;
                        pt.status = 30;
                    } else if (pt.status == 30) {
                        if ((time - pt.elapsed) > pt.period) {
                            IotSync.clusterRpt(cluster,id, pt.echo * 100 + ex);
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
                    if (pt.status == 50) {
                        IotSync.clusterRpt(cluster, id, pt.echo * 100 + 99);
                    }
                    pt.status = 0;
                }
            }
        }
    }

    public boolean init() throws NumberFormatException, NullPointerException {
        // Get Protocols;
        // 获取设定的数据
        String v = IotSync.localgetOption("pR-" + id);
        if (v != null) {
            String[] x = v.split(";;");
            for (String y : x) {
                String[] k = y.split("@");
                Protocol pt = Protocol.parse(k);
                if (pt != null) {
                    if (pt.type == 1 || pt.type == 2) {
                        ins_list.add(pt);
                    } else if (pt.type == 3 || pt.type == 4 || pt.type == 7) {
                        duration_lst.add(pt);
                    } else {
                        ptc_list.add(pt);
                    }
                } else {
                    IotLogger.auto("Invalid Protocol string unparsed on id:" + id);
                    return false;
                }
            }
        }
        return true;
    }


    // this is local
    public void report_duration(long time, long hmin, long hmax, long havg, int duration) throws IOException {
        // Protocol 逐个轮询
        for (Protocol pt : duration_lst) {
            // 判断最大，最小值，如果超过，就记录elapsed
            int ex = 0;
            if (havg >= pt.avgmax) {
                ex = 10;
            } else if (havg <= pt.avgmin) {
                ex = 11;
            } else if (hmin <= pt.minLow) {
                ex = 13;
            } else if (hmax >= pt.maxTop) {
                ex = 14;
            } else if (havg == hmax && hmax == hmin && hmax == 0 && pt.type == 7) {
                // 没有数据
                ex = 18;
            }

            if (ex != 0) {
                //数据异常
                if (pt.type == 3 && duration == 3600) {
                    IotSync.clusterRpt(0, id, pt.echo * 100 + ex);
                } else if (pt.type == 4 && duration == 3600 * 24) {
                    IotSync.clusterRpt(0, id, pt.echo * 100 + ex);
                }
            }
        }
    }

    public String getHexMsg() {
        if (id == 30304009) {
            return "FE040000000CE400";//  请求温度
        } else if (id == 40000001) {
            // 电表ID:        811210051700 <--> 001705101281
            return "FEFEFEFE68811210051700681104333334337116"; //  请求电能
        }else if(id == 40100001){
            return "FEFEFEFE68811210051700681104333336357516"; //  请求kW
        }else if(id == 40200001){
            return "FEFEFEFE68811210051700681104333435357516"; // A
        } else if (id == 40000002) {
            // 电表ID:        811210051700 <--> 001705101281
            return "FEFEFEFE68801210051700681104333334337016"; //  请求电能
        }else if(id == 40100002){
            return "FEFEFEFE68801210051700681104333336357416";//"FEFEFEFE68811210051700681104333435357516"; //  请求kW
        }else if(id == 40200002){
            return "FEFEFEFE68801210051700681104333435357416";//"FEFEFEFE68811210051700681104333336357516"; //  请求A


        } else {
            return "0000";
        }
    }

    public boolean isSum() {
        if (id > 40000000 && id < 40100000) {
            // 电量数据
            return true;
        } else {
            return false;
        }
    }
}
