package com.moko.scanner.entity;


import java.io.Serializable;

public class MokoDevice implements Serializable {
    public static final String DEVICE_TOPIC_SWITCH_STATE = "device/switch_state";
    public static final String DEVICE_TOPIC_FIRMWARE_INFO = "device/firmware_infor";
    public static final String DEVICE_TOPIC_DELAY_TIME = "device/delay_time";
    public static final String DEVICE_TOPIC_OTA_UPGRADE_STATE = "device/ota_upgrade_state";
    public static final String DEVICE_TOPIC_DELETE_DEVICE = "device/delete_device";
    public static final String DEVICE_TOPIC_ELECTRICITY_INFORMATION = "device/electricity_information";

    public static final String APP_TOPIC_SWITCH_STATE = "app/switch_state";
    public static final String APP_TOPIC_DELAY_TIME = "app/delay_time";
    public static final String APP_TOPIC_DELAY_TIME_1 = "app/delay_time_01";
    public static final String APP_TOPIC_DELAY_TIME_2 = "app/delay_time_02";
    public static final String APP_TOPIC_DELAY_TIME_3 = "app/delay_time_03";
    public static final String APP_TOPIC_RESET = "app/reset";
    public static final String APP_TOPIC_UPGRADE = "app/upgrade";
    public static final String APP_TOPIC_READ_FIRMWARE_INFOR = "app/read_firmware_infor";

    public int id;
    public String name;
    public String nickName;
    public String uniqueId;
    public boolean on_off;
    public String company_name;
    public String production_date;
    public String product_model;
    public String firmware_version;
    public String topicPublish;
    public String topicSubscribe;
    public boolean isOnline;
}
