package iotsampl.iot.oo;

import java.io.Serializable;

public enum Duration implements Serializable{
    NONE("0"),
    TWELVE("36"),
    DAILY("72"),
    THREEDAY("216"),
    SIMPLE("3600"),
    ANNUAL("86400");
    String value;
    Duration(String i) {
        value = i;
    }
    Duration(int i) {
        value = i + "";
    }
}
