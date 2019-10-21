package com.moko.scanner.entity;


import java.io.Serializable;

public class ScanDevice implements Serializable {

    public String name;
    public String mac;
    public int rssi;
    public String rawData;
}
