package Radnor.Reader;

/**
 * Created by Frapo on 2017/7/10.
 * Version :23
 * Earth - Moudule Radnor.Utils
 */

import Radnor.Utils.Byter;
import earth.server.Local;
import gnu.io.*;
import iotsampl.iot.core.IotLogger;
import iotsampl.iot.core.IotManager;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Math.min;

public class Reader extends Thread implements SerialPortEventListener { // SerialPortEventListener
    // 监听器,我的理解是独立开辟一个线程监听串口数据
    static CommPortIdentifier portId; // 串口通信管理类
    static Enumeration<?> portList; // 有效连接上的端口的枚举
    private static List<Integer> HexNode = new ArrayList<>();
    InputStream inputStream; // 从串口来的输入流
    public static OutputStream outputStream;// 向串口输出的流
    static SerialPort serialPort; // 串口的引用
    // 堵塞队列用来存放读到的数据
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();
    private long lastone = 0;
    private final static long TIME_INTERVAL = 3000;

    /**
     * SerialPort EventListene 的方法,持续监听端口上是否有数据流
     */
    @Override
    public void serialEvent(SerialPortEvent event) {//
        String x;
        switch (event.getEventType()) {
            case SerialPortEvent.BI:       case SerialPortEvent.OE:            case SerialPortEvent.FE:
            case SerialPortEvent.PE:       case SerialPortEvent.CD:            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:      case SerialPortEvent.RI:            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:// 当有可用数据时读取数据
                try {
                    int numBytes = inputStream.available();
                    if(numBytes != 29 && numBytes != 24 && numBytes != 23) {
                            IotLogger.o("串口可用数据 : " + numBytes);
                    }

                    byte[] by = new byte[min(numBytes,1000)];
                    int red = inputStream.read(by);
                    if((x = Byter.bytesToHexString(by)) == null) return;
                    if(red != min(numBytes,1000) || x.length() != red * 2) IotLogger.auto("算法可能出错：请检查，定位POI93$a");
                    if(numBytes != 29 && numBytes != 24  && numBytes != 23) {
                        IotLogger.o("正在读取, 可用数据 = "+numBytes + " , 已经读取 = "+red);
                        IotLogger.o("E::"+x);
                    }
                    long t = System.currentTimeMillis()/1000;
                    msgQueue.add(t + "::Evt_R_" + event.getEventType() + " ::" + x);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    static String preread = "";

    /**
     *
     * 通过程序打开COM4串口，设置监听器以及相关的参数
     *
     * @return 返回1 表示端口打开成功，返回 0表示端口打开失败
     */
    public int startComPort() {
        // 通过串口通信管理类获得当前连接上的串口列表

        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            if(Local.DEBUG) {
                System.out.println("设备类型：测试设备");
                return 1;
            }
            // 获取相应串口对象
            portId = (CommPortIdentifier) portList.nextElement();

            System.out.println("设备类型：--->" + portId.getPortType());
            System.out.println("设备名称：---->" + portId.getName());
            // 判断端口类型是否为串口
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                // 判断如果COM*串口存在，就打开该串口
                if (portId.getName().startsWith("COM") && !Objects.equals(portId.getName(), "COM1")) {
                    try {
                        // 打开串口名字为compor_9(名字任意),延迟为2毫秒
                        serialPort = (SerialPort) portId.open("compor_9", 3000);

                    } catch (PortInUseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                    // 设置当前串口的输入输出流
                    try {
                        inputStream = serialPort.getInputStream();
                        outputStream = serialPort.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return 0;
                    }
                    // 给当前串口添加一个监听器
                    try {
                        serialPort.addEventListener(this);
                    } catch (TooManyListenersException e) {
                        e.printStackTrace();
                        return 0;
                    }

                    // 设置监听器生效，即：当有数据时通知
                    serialPort.notifyOnDataAvailable(true);

                    // 设置串口的一些读写参数
                    try {
                        // 比特率、数据位、停止位、奇偶校验位
                        serialPort.setSerialPortParams(9600,
                                SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
                    } catch (UnsupportedCommOperationException e) {
                        e.printStackTrace();
                        return 0;
                    }

                    return 1;
                }
            }
        }
        return 0;
    }

    public void init(){
        // 读取HexList;
        List<Integer> lst = IotManager.getOne().getAllHexNodes();
        if(lst != null) HexNode.addAll(lst);
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            lasttime.put(40000002L,0L);
            lasttime.put(40000001L,0L);
            System.out.println("--------------任务处理线程运行了--------------");
            while (true) {
                // 如果堵塞队列中存在数据就将其输出
                try{
                    while (msgQueue.size() > 0) {
                        parseQueue(msgQueue.take());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    IotLogger.auto("Err occured on Reader.parseQueue : " + e.getMessage());
                }

                if(System.currentTimeMillis() - lastone > TIME_INTERVAL){
                    // expire
                    try {
                        getAnalog();
                        // 这里需要延时等待对方反应 ， 然后才能切换
                    } catch (IOException e) {
                        e.printStackTrace();
                        IotLogger.i("There has been error in reading data (IOExp)");
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        IotLogger.i("There has been error in reading data (Exp)");
                    }
                    lastone = System.currentTimeMillis();
                }
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
    }

    private void parseQueue(String take) {
        /**
         *  Spliter
         * */
        if (take == null) return;
        String[] stx = take.split("::");
        if (stx.length != 3) return;
        String center = stx[2];
        if(center.length() < 23)    return;

        if (Objects.equals("fe04", center.substring(0, 4).toLowerCase())) {
            // 获取了模拟数据, 读取数据
            parseTemp(center,stx);
        } else if ((center.length() == 46 || center.length() == 48 )&&
                Objects.equals("68811210051700", center.substring(8, 22))){

            if(Objects.equals("33343535",center.substring(14*2,18*2))) {
                // 获取了Ampress数据
                parseAmpress(center, stx, 40200001);
            }else if(Objects.equals("33333635",center.substring(14*2,18*2)) ) {
                // 获取了kiloWatt
                parsekW(center, stx, 40100001);
            } else if (Objects.equals("33333433",center.substring(28,36))) {
                // 获取了电能
                parsekWh(center, stx, 40000001);
            }


        } else if ((center.length() == 46 || center.length() == 48) &&
                Objects.equals("68801210051700", center.substring(8, 22))){
            if(Objects.equals("33343535",center.substring(28,36))) {
                // 获取了Ampress数据
                parseAmpress(center, stx, 40200002);
            }else if(Objects.equals("33333635",center.substring(28,36)) ) {
                // 获取了kiloWatt
                parsekW(center, stx, 40100002);
            } else if (Objects.equals("33333433",center.substring(28,36))) {
                // 获取了电能
                parsekWh(center, stx, 40000002);
            }

        }
    }

    private void parseAmpress(String center, String[] stx, int i) {
        long recordTime = Long.parseLong(stx[0]);
        IotLogger.auto("电表电流数据::"+new Date(recordTime)+"::"+ stx[1] + "::" + center);
        try {
            String get = Electric_get_5(center);
            long v = Long.parseLong(get);
            IotManager.getOne().getNode(i).quick_record(Long.toHexString(v), recordTime);
        }catch (NumberFormatException e){
            // 格式错误
            IotLogger.auto("Reader : Am电表数据不合格式");
        }
    }

    private void parsekW(String center, String[] stx, int i) {
        long recordTime = Long.parseLong(stx[0]);
        IotLogger.auto("电表功率数据::"+new Date(recordTime)+"::"+ stx[1] + "::" + center);
        try {
            String get = Electric_get_5(center);
            long v = Long.parseLong(get);
            IotManager.getOne().getNode(i).quick_record(Long.toHexString(v), recordTime);
        }catch (NumberFormatException e){
            // 格式错误
            IotLogger.auto("Reader : kW电表数据不合格式");
        }
    }

    private Map<Long,Long> lasttime = new HashMap<>();

    private void parsekWh(String center, String[] stx, int i) {
        long recordTime = Long.parseLong(stx[0]);
        IotLogger.auto("电表有功数据::"+new Date(recordTime)+"::"+ stx[1] + "::" + center);
        try {
            String get = Electric_get_8(center);
            long v = Long.parseLong(get);
            if(Math.abs(v - lasttime.get((long)i)) > 1000100){
                // 抛棄數據
                lasttime.put((long) i,v);
                IotLogger.auto("Reader : kWh电表数据不合格式 - 距離太大10000kWh - " + lastone );
            }else {
                IotManager.getOne().getNode(i).quick_record(Long.toHexString(v), recordTime);
            }
        }catch (NumberFormatException e){
            // 格式错误
            IotLogger.auto("Reader : kWh电表数据不合格式");
        }
    }

    private void parseTemp(String center, String[] stx) {
        long recordTime = Long.parseLong(stx[0]);
        IotLogger.i("温度传感器::" + new Date(recordTime * 1000) + "::" + stx[1] + "::" + center);
        try {
            String s = center.substring(6, 10);
            s = getTempStringFm(s);
            // 分析正负
            IotManager.getOne().getNode(30304009).quick_record(s, recordTime);

            s = center.substring(10, 14);
            s = getTempStringFm(s);
            IotManager.getOne().getNode(30304010).quick_record(s, recordTime);

            s = center.substring(18, 22);
            s = getTempStringFm(s);
            IotManager.getOne().getNode(30304011).quick_record(s, recordTime);

        } catch (Exception e) {
            if (e instanceof ConnectException || e instanceof SSLHandshakeException || e instanceof ValidationException) {
                // 网路问题
            } else {
                e.printStackTrace();
            }
        }
    }

    private String getTempStringFm(String s) {
        long mp = Long.parseLong(s,16);
        if(mp > 32000){
            mp -= 65535;
            mp = -mp;
            s = "-"+Long.toHexString(mp);
        }
        return s;
    }

    // 44, 5
    private String Electric_get_8(String center) throws NumberFormatException{
        char[] ans = new char[8];
        for (int i = 0; i < 4; i++) {
            int h = Integer.parseInt(center.substring(42-2*i,43-2*i),16);
            ans[i*2] = (char) (h + 45);
            int l = Integer.parseInt(center.substring(43-2*i,44-2*i),16); // 22 的低位
            ans[i*2+1] = (char) (l + 45);
        }
        return new String(ans) + "00";
    }

    private String Electric_get_5(String center) throws NumberFormatException{
        char[] ans = new char[5];
        // 43, 44 = [44]
        /*1  2   3  4  41  42
         1a 1b  2a 2b 21a 21b*/
        int h = Integer.parseInt(center.substring(41,42),16);
        ans[0] = (char) (h + 45);
        for (int i = 0; i < 2; i++) {
            h = Integer.parseInt(center.substring(38-2*i,39-2*i),16);
            ans[i*2+1] = (char) (h + 45);
            int l = Integer.parseInt(center.substring(39-2*i,40-2*i),16); // 22 的低位
            ans[i*2+2] = (char) (l + 45);
        }
        return new String(ans);
    }

    private static int lastid = 1;

    private static void getAnalog() throws IOException {

        if(!Local.DEBUG) {
            if(HexNode == null || HexNode.size() == 0){
                return;
            }

            for(int id: HexNode) {
                String hex = IotManager.getOne().getNode(id).getHexMsg();

                if (id < 40000000) {
                    if(lastid == 2) {
                        lastid = 1;
                        try {
                            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                        } catch (UnsupportedCommOperationException e) {
                            e.printStackTrace();
                        }
                    }
                } else if(id < 50000000) {
                    if(lastid == 1) {
                        lastid = 2;
                        try {
                            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
                        } catch (UnsupportedCommOperationException e) {
                            e.printStackTrace();
                        }
                    }
                }

                sendHex(hex);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }else{
            long t = System.currentTimeMillis() / 1000;

            String s = String.format("%04x" , (int) (Math.random() * 1000000) % 25).toLowerCase();
            IotManager.getOne().getNode(30304009).quick_record(s,t);

            s = String.format("%04x" , (int) (Math.random() * 1000000) % 25).toLowerCase();
            IotManager.getOne().getNode(30304010).quick_record(s,t);

            s = String.format("%04x" , (int) (Math.random() * 1000000) % 25).toLowerCase();
            IotManager.getOne().getNode(30304011).quick_record(s,t);


            long m = System.currentTimeMillis() / 1000000 - 1500009L;
            s = Long.toHexString(5005400 + m);
            IotManager.getOne().getNode(40000001).quick_record(s,t);

            s = Long.toHexString(270300);
            IotManager.getOne().getNode(40000002).quick_record(s,t);

            s = "1630";
            IotManager.getOne().getNode(40100001).quick_record(s,t);
            s = "cba";
            IotManager.getOne().getNode(40200001).quick_record(s,t);

            s = "40";
            IotManager.getOne().getNode(40100002).quick_record(s,t);
            s = "30";
            IotManager.getOne().getNode(40200002).quick_record(s,t);
        }
    }

    private static void sendHex(String str) throws IOException{
        byte[] s = Byter.hexStringToBytes(str);
        if(outputStream != null) outputStream.write(s, 0, s.length);
    }
}
