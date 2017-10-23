package earth.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Frapo on 2017/1/23.
 */
public class Monitor {

    static {
    }

    private static Log log = LogFactory.getLog(Monitor.class);

    public static void logger(int i) {
        // 记录数值
        log.info(i);
    }

    public static void alert(String msg) {
        log.warn(msg);
        // Alert;
    }

    public static void debug(String s) {
        log.info(s);
    }

    public static void logger(String s) {
        log.info(s);
    }

    public static void access(String s) {
        log.trace("{" + s + "}");
    }

    public static void response(String res) {
        log.trace("[" + res + "]");
    }

    public static void exp(Exception e, Class s) {
        Log logs = LogFactory.getLog(s);
        logs.info(e);
    }

    public static void error(String s) {
        log.error(s);
    }

    public static void logger(String tag, String s) {
        log.info(tag + s);
    }

    public static void feedback(String s) {
        System.out.println("::" + s);
    }
}
