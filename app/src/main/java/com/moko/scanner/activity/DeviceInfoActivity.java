package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.handler.MQTTMessageAssembler;
import com.moko.support.utils.MokoUtils;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2019/10/30
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.DeviceInfoActivity
 */
public class DeviceInfoActivity extends BaseActivity {

    @Bind(R.id.tv_company_name)
    TextView tvCompanyName;
//    @Bind(R.id.tv_device_date)
//    TextView tvDeviceDate;
    @Bind(R.id.tv_device_name)
    TextView tvDeviceName;
    @Bind(R.id.tv_device_version)
    TextView tvDeviceVersion;
    @Bind(R.id.tv_device_mac)
    TextView tvDeviceMac;
    private MokoDevice mokoDevice;
    private MQTTConfig appMqttConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        mokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);

        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(AppConstants.ACTION_DEVICE_STATE);
        registerReceiver(mReceiver, filter);

        showLoadingProgressDialog(getString(R.string.wait));
        mHandler = new MessageHandler(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showToast(DeviceInfoActivity.this, "Get data failed!");
                dismissLoadingProgressDialog();
                finish();
            }
        }, 30 * 1000);
        getCompanyName();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                byte[] receive = intent.getByteArrayExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                int header = receive[0] & 0xFF;
                if (header == 0x12)// 读取公司名称
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.company_name = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        tvCompanyName.setText(mokoDevice.company_name);
                        getProductModel();
                    }
                }
//                if (header == 0x13)// 读取生产日期
//                {
//                    int length = receive[1] & 0xFF;
//                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
//                    if (mokoDevice.uniqueId.equals(new String(id))) {
//                        mokoDevice.production_date = String.format("%d.%d.%d"
//                                , MokoUtils.toInt(Arrays.copyOfRange(receive, 4 + length, 6 + length))
//                                , receive[receive.length - 2] & 0xFF
//                                , receive[receive.length - 1] & 0xFF);
//                        tvDeviceDate.setText(mokoDevice.production_date);
//                        getProductModel();
//                    }
//                }
                if (header == 0x1A)// 读取设备型号
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.product_model = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        tvDeviceName.setText(mokoDevice.product_model);
                        getFirmwareVersion();
                    }
                }
                if (header == 0x15)// 读取固件版本
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.firmware_version = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        tvDeviceVersion.setText(mokoDevice.firmware_version);
                        getMac();
                    }
                }
                if (header == 0x16)// 读取MAC
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.mac = String.format("%s:%s:%s:%s:%s:%s"
                                , MokoUtils.byte2HexString(receive[receive.length - 6])
                                , MokoUtils.byte2HexString(receive[receive.length - 5])
                                , MokoUtils.byte2HexString(receive[receive.length - 4])
                                , MokoUtils.byte2HexString(receive[receive.length - 3])
                                , MokoUtils.byte2HexString(receive[receive.length - 2])
                                , MokoUtils.byte2HexString(receive[receive.length - 1]));
                        tvDeviceMac.setText(mokoDevice.mac);
                        dismissLoadingProgressDialog();
                        mHandler.removeMessages(0);
                    }
                }
            }
            if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (topic.equals(mokoDevice.topicPublish)) {
                    boolean isOnline = intent.getBooleanExtra(MokoConstants.EXTRA_MQTT_RECEIVE_STATE, false);
                    mokoDevice.isOnline = isOnline;
                    if (!isOnline) {
                        finish();
                    }
                }
            }
        }
    };

    private void getCompanyName() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadCompanyName(mokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

//    private void getProductDate() {
//        String appTopic;
//        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
//            appTopic = mokoDevice.topicSubscribe;
//        } else {
//            appTopic = appMqttConfig.topicPublish;
//        }
//        byte[] message = MQTTMessageAssembler.assembleReadProductDate(mokoDevice.uniqueId);
//        try {
//            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }

    private void getProductModel() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadProductModel(mokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getFirmwareVersion() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadFirmwareVersion(mokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getMac() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadMac(mokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void back(View view) {
        finish();
    }

    public MessageHandler mHandler;

    public class MessageHandler extends BaseMessageHandler<DeviceInfoActivity> {

        public MessageHandler(DeviceInfoActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(DeviceInfoActivity activity, Message msg) {
        }
    }
}
