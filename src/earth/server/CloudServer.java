package earth.server;

import earth.server.sz.Nanshan;
import iotsampl.iot.cloud.IotCloudAutomate;
import iotsampl.iot.core.IotLogger;

import java.util.concurrent.TimeUnit;
import iotsampl.Constant;

/**
 * Created by Frapo on 2017/1/22.
 */
public class CloudServer {


    public static void main(String[] args){


        try{
            IotCloudAutomate.startIotSync();
        } catch (Exception e) {
            IotLogger.i("iotSync 服务启动失败 " +e.getMessage());
        }

        try{
            IotCloudAutomate.getOne().start();
        }catch (Exception e){
            e.printStackTrace();
            IotLogger.i("iotCloudAuto 服务启动失败 " +e.getMessage());
        }

        /*try{LocalDataProvider.startAsService();}catch (Exception e){
            IotLogger.i("Reader 服务启动失败" +e.getMessage());
        }*/

        try {Nanshan.main(9015);} catch (Exception e) {
            IotLogger.i(" Http 服务器启动失败 " + e.getMessage());
        }

        try {
            while (true) {
                TimeUnit.SECONDS.sleep(3600);
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } finally {
            Constant.close();
        }
    }
}
