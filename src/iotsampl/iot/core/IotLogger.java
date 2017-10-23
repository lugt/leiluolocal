package iotsampl.iot.core;

import java.util.Date;

/**
 * Created by Frapo on 2017/8/8.
 * Version :16
 * Earth - Moudule iotsampl.iot
 */
public class IotLogger {
    public static void i(String s){
        System.out.println(new Date() + "--" + s);
    }

    public static void o(String s) {
        System.out.println(s);
    }

    public static void auto(String message) {
        System.out.println(message);
        // 保存到文件
    }

    public static void sync(String message) {
        System.out.println(message);
        // 保存到文件
    }

    public static void accessLog(String acc){
        System.out.println(acc);

    }
}
