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
        b[0] = 0x27;
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

    public static byte[] assembleWriteReset(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 5];
        b[0] = 0x28;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 1;
        b[dataLength + 4] = 1;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadCompanyName(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x12;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadProductDate(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x13;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadProductModel(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x1A;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadFirmwareVersion(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x15;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleReadMac(String id) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 4];
        b[0] = 0x16;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 0;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteOTAType(String id, int type) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        byte[] b = new byte[dataLength + 5];
        b[0] = 0x2A;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = 1;
        b[dataLength + 4] = (byte) type;
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteHostAndPort(String id, String host, int port) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        int hostLength = host.length();
        byte[] b = new byte[dataLength + 4 + hostLength + 2];
        b[0] = 0x2B;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        byte[] hostAndPortLength = MokoUtils.toByteArray(hostLength + 2, 2);
        b[dataLength + 2] = hostAndPortLength[0];
        b[dataLength + 3] = hostAndPortLength[1];
        byte[] hostBytes = host.getBytes();
        for (int i = 0; i < hostLength; i++) {
            b[dataLength + i + 4] = hostBytes[i];
        }
        byte[] portBytes = MokoUtils.toByteArray(port, 2);
        b[dataLength + hostLength + 4] = portBytes[0];
        b[dataLength + hostLength + 5] = portBytes[1];
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }

    public static byte[] assembleWriteCatalogue(String id, String catalogue) {
        byte[] dataBytes = id.getBytes();
        int dataLength = dataBytes.length;
        int catalogueLength = catalogue.length();
        byte[] b = new byte[dataLength + 4 + catalogueLength];
        b[0] = 0x2C;
        b[1] = (byte) dataLength;
        for (int i = 0; i < dataLength; i++) {
            b[i + 2] = dataBytes[i];
        }
        b[dataLength + 2] = 0;
        b[dataLength + 3] = (byte) catalogueLength;
        byte[] catalogueBytes = catalogue.getBytes();
        for (int i = 0; i < catalogueLength; i++) {
            b[dataLength + i + 4] = catalogueBytes[i];
        }
        LogModule.e("app_to_device--->" + MokoUtils.bytesToHexString(b));
        return b;
    }
}
