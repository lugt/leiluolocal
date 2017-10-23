package iotsampl.iot.core;

public class IotIds {
    public static int extractDuration(long id) {
        if(id > 100000000000L && id < 100100000000L){
            return 0;
        }else if (id < 100200000000L) {
            return 36;
        }else if (id < 100300000000L) {
            return 72;
        }else if (id < 100400000000L) {
            return 216;
        }else if (id < 100500000000L) {
            return 3600;
        }else if (id < 100600000000L) {
            return 86400;
        }else {
            return 9999;
        }
    }

    public static int extractShorId(long id) {
        return (int) (id % 100000000L);
    }


    public static Long getPrefix(int duration) {
        switch (duration){
            case 36:
                return 100100000000L;
            case 72:
                return 100200000000L;
            case 216:
                return 100300000000L;
            case 3600:
                return 100400000000L;
            case 86400:
                return 100500000000L;
        }
        return 100000000000L;
    }

    public static String getsLong(long data) {
        if(data >= 0) {
            return Long.toHexString(data);
        }else{
            data = -data;
            return "-" + Long.toHexString(data);
        }
    }
}
