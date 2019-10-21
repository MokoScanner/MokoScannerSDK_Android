package com.moko.support.handler;

import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

public class MQTTMessageAssembler {
    public static byte[] assembleReadScanSwitch(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x17;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadScanInterval(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x18;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteScanSwitch(String id, boolean scanSwitch) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 5];
        b[0] = 0x26;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 1;
        b[dataLength + 4] = (byte) (scanSwitch ? 1 : 0);
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteScanInterval(String id, int interval) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 6];
        b[0] = 0x26;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 2;
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        b[dataLength + 4] = intervalBytes[0];
        b[dataLength + 5] = intervalBytes[1];
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadFilterRSSI(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x19;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }


    public static byte[] assembleReadFilterName(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x20;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteFilterRSSI(String id, int rssi) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 5];
        b[0] = 0x25;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 1;
        b[dataLength + 4] = (byte) rssi;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteFilterName(String id, String name) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        int nameLength = name.length();
        byte[] b = new byte[dataLength + 4 + nameLength];
        b[0] = 0x29;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = (byte) nameLength;
        byte[] nameBytes = name.getBytes();
        for (int i = 0; i < nameLength; i++) {
            b[dataLength + i + 4] = nameBytes[i];
        }
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }
}
