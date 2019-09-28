package com.moko.support.entity;

import java.io.Serializable;

public enum DeviceTypeEnum implements Serializable {
    LW001_BG(0),
    LW002_TH(1),
    LW003_B(2),
    ;


    private int deviceType;

    DeviceTypeEnum(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getDeviceType() {
        return deviceType;
    }
}
