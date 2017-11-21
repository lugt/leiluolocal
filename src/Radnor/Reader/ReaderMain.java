package Radnor.Reader;

import Radnor.Utils.LocalDataProvider;
import earth.server.Local;
import iotsampl.DataService;
import iotsampl.iot.core.IotLogger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frapo on 2017/1/22.
 *  串口通信专用程序启动器（Main函数）
 */
public class ReaderMain {

    //private static final SessionFactory ourSessionFactory;
    //private static final ServiceRegistry serviceRegistry;

    public static void main(String[] args){

        if(args.length > 0){
            for (String a:args) {
                if(Objects.equals(a, "-debug")){
                    Local.DEBUG = true;
                }
            }
        }

        try{
            LocalDataProvider.startAsService();
        }catch (Exception e){
            IotLogger.i("Reader 服务启动失败" +e.getMessage());
        }

        /**
         * 数据服务器 -- 文件上传
         *       *  FileServer.newBuild().start();
         */


        try {
            while (true) {
                // 监测其他进程是否正常
                TimeUnit.SECONDS.sleep(10);
                IotLogger.i("主线程正在运行");
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } finally {
            DataService.close();
        }
    }
}
