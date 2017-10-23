package earth.server;

import Radnor.Utils.LocalDataProvider;
import earth.server.sz.Nanshan;
import iotsampl.iot.core.IotLogger;
import iotsampl.Constant;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frapo on 2017/1/22.
 */
public class Local {
    public static boolean DEBUG = false;

    //private static final SessionFactory ourSessionFactory;
    //private static final ServiceRegistry serviceRegistry;

    public static void main(String[] args){

        if(args.length > 0){
            for (String a:args) {
                if(Objects.equals(a, "-debug")){
                    DEBUG = true;
                }
            }
        }

        try{    LocalDataProvider.startIotSync();  } catch (Exception e) {
            IotLogger.i("iotSync 服务启动失败 " +e.getMessage());
        }

        try {    LocalDataProvider.startAutomation();  } catch (Exception e) {
            IotLogger.i("Automate 服务启动失败 " +e.getMessage());
        }

        /*try{LocalDataProvider.startAsService();}catch (Exception e){
            IotLogger.i("Reader 服务启动失败" +e.getMessage());
        }*/

        try {
            Nanshan.main(9011);
        } catch (Exception e) {
            IotLogger.i(" Http 服务器启动失败 " + e.getMessage());
        }

        /**
         * 数据服务器 -- 文件上传
         *       *  FileServer.newBuild().start();
         */


        try {
            while (true) {
                // 监测其他进程是否正常
                //if (!Constant.isSessionAlive()) {
                 //   Monitor.error("小时提示 - 主线程 : sessionFactory 尚未创建 / 创建异常 / 已关闭.");
                //}
                TimeUnit.SECONDS.sleep(3600);
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } finally {
            Constant.close();
        }
    }
}
