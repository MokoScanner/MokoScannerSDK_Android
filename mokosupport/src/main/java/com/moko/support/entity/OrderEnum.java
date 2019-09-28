package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 命令枚举
 * @ClassPath com.fitpolo.support.entity.OrderEnum
 */
public enum OrderEnum implements Serializable {
    OPEN_NOTIFY("打开设备通知", 0),
    READ_DEVICE_TYPE("读取产品类型", 0x01),
    READ_COMPANY_NAME("读取公司名称", 0x02),
    READ_MANUFACTURE_DATE("读取生产日期", 0x03),
    READ_MODEL_NAME("读取设备名称", 0x04),
    READ_BLE_FIRMWARE("读取固件版本", 0x05),
    READ_LORA_FIRMWARE("读取Lora固件版本", 0x06),
    READ_GPS("读取GPS数据", 0x16),
    READ_9_AXIS_A("读取9轴A数据", 0x17),
    READ_9_AXIS_G("读取9轴G数据", 0x18),
    READ_9_AXIS_M("读取9轴M数据", 0x19),
    READ_9_AXIS_ANGLE("读取9轴角度数据", 0x1A),
    READ_DEV_ADDR("读取DevAddr", 0x07),
    READ_NWK_SKEY("读取读取NwkSKey", 0x08),
    READ_APP_SKEY("读取AppSKey", 0x09),
    READ_DEV_EUI("读取DevEUI", 0x0A),
    READ_APP_EUI("读取AppEUI", 0x0B),
    READ_APP_KEY("读取AppKey", 0x0C),
    READ_REGION("读取Region", 0x0D),
    READ_CLASS_TYPE("读取Class类型", 0x0E),
    READ_UPLOAD_MODE("读取上传模式", 0x0F),
    READ_UPLOAD_INTERVAL("读取上传间隔", 0x10),
    READ_CONNECT_STATUS("读取连接状态", 0x11),
    READ_CH("读取CH", 0x12),
    READ_DR("读取DR", 0x13),
    READ_POWER("读取Power", 0x14),
    READ_ADR("读取ADR状态", 0x15),
    READ_MCU("读取MCU固件版本", 0x20),
    READ_I2C("读取I2C采集间隔", 0x21),
    READ_RS485("读取RS485采集间隔", 0x22),
    READ_ADC_V("读取ADC_V采集间隔", 0x23),
    READ_ADC_I("读取ADC_I采集间隔", 0x24),
    READ_PT100("读取PT100采集间隔", 0x25),
    READ_RS485_ADR("读取RS485设备个数，地址", 0x26),
    READ_TEMP("读取温度报警使能标志和阈值和当前值（小端模式）", 0x27),
    READ_HUMI("读取湿度报警使能标志和阈值和当前值（小端模式）", 0x28),
    READ_BLE("读取蓝牙开机保持时间", 0x29),

    WRITE_UPLINK_DATA_TEST("设置上行测试数据", 0xBB),
    WRITE_DEV_ADDR("设置DevAddr", 0x37),
    WRITE_NWK_SKEY("设置NwkSKey", 0x38),
    WRITE_APP_SKEY("设置AppSKey", 0x39),
    WRITE_DEV_EUI("设置DevEUI", 0x3A),
    WRITE_APP_EUI("设置AppEUI", 0x3B),
    WRITE_APP_KEY("设置AppKey", 0x3C),
    WRITE_REGION("设置Region", 0x3D),
    WRITE_CLASS_TYPE("设置Class类型", 0x3E),
    WRITE_UPLOAD_MODE("设置上传模式", 0x3F),
    WRITE_UPLOAD_INTERVAL("设置上传间隔", 0x40),
    WRITE_CH("设置CH", 0x42),
    WRITE_DR("设置DR", 0x43),
    WRITE_POWER("设置Power", 0x44),
    WRITE_ADR("设置ADR", 0x45),
    WRITE_CONNECT("设置连接", 0x50),
    WRITE_RESET("设置复位", 0x51),
    WRITE_I2C("设置I2C采集间隔", 0x60),
    WRITE_RS485("设置RS485采集间隔", 0x61),
    WRITE_ADC_V("设置ADC_V采集间隔", 0x62),
    WRITE_ADC_I("设置ADC_I采集间隔", 0x63),
    WRITE_PT100("设置PT100采集间隔", 0x64),
    WRITE_RS485_ADR("设置RS485设备个数，地址", 0x65),
    WRITE_TEMP("设置温度报警使能标志和阈值（小端模式）", 0x66),
    WRITE_HUMI("设置湿度报警使能标志和阈值（小端模式）", 0x67),
    WRITE_BLE("设置蓝牙开机保持时间", 0x68),

    UPGRADE_MCU("升级包概况", 0x41),
    UPGRADE_MCU_DETAIL("升级包详情", 0x42),

    ;


    private String orderName;
    private int orderHeader;

    OrderEnum(String orderName, int orderHeader) {
        this.orderName = orderName;
        this.orderHeader = orderHeader;
    }

    public int getOrderHeader() {
        return orderHeader;
    }

    public String getOrderName() {
        return orderName;
    }
}
