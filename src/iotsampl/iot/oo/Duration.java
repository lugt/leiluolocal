package iotsampl.iot.oo;

public enum Duration {
    NONE("0"),
    TWELVE("36"),
    DAILY("72"),
    THREEDAY("216"),
    SIMPLE("3600"),
    ANNUAL("86400");
    int value;
    Duration(String i) {
        value = Integer.valueOf(i);
    }
}
