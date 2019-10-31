package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.adapter.DeviceAdapter;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.db.DBTools;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.entity.MsgCommon;
import com.moko.scanner.entity.SwitchInfo;
import com.moko.scanner.service.MokoService;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.handler.MQTTMessageAssembler;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description 设备列表
 * @ClassPath com.moko.scanner.activity.MainActivity
 */
public class MainActivity extends BaseActivity implements DeviceAdapter.AdapterClickListener {

    @Bind(R.id.rl_empty)
    RelativeLayout rlEmpty;
    @Bind(R.id.lv_device_list)
    ListView lvDeviceList;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    private ArrayList<MokoDevice> devices;
    private DeviceAdapter adapter;
    private boolean mScanSwitch;
    private int mScanInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        devices = DBTools.getInstance(this).selectAllDevice();
        adapter = new DeviceAdapter(this);
        adapter.setListener(this);
        adapter.setItems(devices);
        lvDeviceList.setAdapter(adapter);
        if (devices.isEmpty()) {
            rlEmpty.setVisibility(View.VISIBLE);
            lvDeviceList.setVisibility(View.GONE);
        } else {
            lvDeviceList.setVisibility(View.VISIBLE);
            rlEmpty.setVisibility(View.GONE);
        }
        mHandler = new OfflineHandler(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_SUBSCRIBE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
        filter.addAction(AppConstants.ACTION_DELETE_DEVICE);
        registerReceiver(mReceiver, filter);
        startService(new Intent(this, MokoService.class));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
                String title = "";
                if (state == MokoConstants.MQTT_CONN_STATUS_LOST) {
                    title = getString(R.string.mqtt_connecting);
                } else if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS) {
                    title = getString(R.string.guide_center);
                } else if (state == MokoConstants.MQTT_CONN_STATUS_FAILED) {
                    title = getString(R.string.mqtt_connect_failed);
                }
                tvTitle.setText(title);
                if (state == 1) {
//                    try {
//                        MokoSupport.getInstance().subscribe("lwz_123", 1);
//                    } catch (MqttException e) {
//                        e.printStackTrace();
//                    }
                    String mqttConfigAppStr = SPUtiles.getStringValue(MainActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
                    MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
                    // 订阅
                    try {
                        if (!TextUtils.isEmpty(appMqttConfig.topicSubscribe)) {
                            MokoSupport.getInstance().subscribe(appMqttConfig.topicSubscribe, appMqttConfig.qos);
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    if (devices.isEmpty()) {
                        return;
                    }
                    for (MokoDevice device : devices) {
                        // 订阅
//                        for (String topic : device.getDeviceTopics()) {
                        try {
                            if (TextUtils.isEmpty(appMqttConfig.topicSubscribe)) {
                                MokoSupport.getInstance().subscribe(device.topicPublish, appMqttConfig.qos);
                            }
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
//                        }
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_SUBSCRIBE.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                final String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                byte[] receive = intent.getByteArrayExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                if (devices.isEmpty()) {
                    return;
                }
                int header = receive[0] & 0xFF;
                if (header == 0x24)// 设备联网状态
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    for (final MokoDevice device : devices) {
                        if (device.uniqueId.equals(new String(id))) {
                            device.isOnline = true;
                            if (mHandler.hasMessages(device.id)) {
                                mHandler.removeMessages(device.id);
                            }
                            Message message = Message.obtain(mHandler, new Runnable() {
                                @Override
                                public void run() {
                                    device.isOnline = false;
                                    LogModule.i(device.uniqueId + "离线");
                                    adapter.notifyDataSetChanged();
                                    Intent i = new Intent(AppConstants.ACTION_DEVICE_STATE);
                                    i.putExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC, topic);
                                    MainActivity.this.sendBroadcast(i);
                                }
                            });
                            message.what = device.id;
                            mHandler.sendMessageDelayed(message, 62 * 1000);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
                if (header == 0x17)// 开关状态
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    MokoDevice device = DBTools.getInstance(MainActivity.this).selectDevice(new String(id));
                    if (device == null) {
                        return;
                    }
                    mScanSwitch = (receive[receive.length - 1] & 0xFF) == 1;
                    getScanInterval(device);
                }
                if (header == 0x18)// 扫描时长
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    MokoDevice device = DBTools.getInstance(MainActivity.this).selectDevice(new String(id));
                    if (device == null) {
                        return;
                    }
                    dismissLoadingProgressDialog();
                    device.isOnline = true;
                    byte[] dataLengthBytes = Arrays.copyOfRange(receive, 2 + length, 4 + length);
                    int dataLength = MokoUtils.toInt(dataLengthBytes);
                    mScanInterval = MokoUtils.toInt(Arrays.copyOfRange(receive, receive.length - dataLength, receive.length));
                    Intent i = new Intent(MainActivity.this, DeviceDetailActivity.class);
                    i.putExtra(AppConstants.EXTRA_KEY_DEVICE, device);
                    i.putExtra(AppConstants.EXTRA_KEY_SCAN_SWITCH, mScanSwitch);
                    i.putExtra(AppConstants.EXTRA_KEY_SCAN_INTERVAL, mScanInterval);
                    startActivity(i);
                }

            }
            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
                devices.clear();
                devices.addAll(DBTools.getInstance(MainActivity.this).selectAllDevice());
                adapter.notifyDataSetChanged();
            }
            if (AppConstants.ACTION_DELETE_DEVICE.equals(action)) {
                int id = intent.getIntExtra(AppConstants.EXTRA_DELETE_DEVICE_ID, -1);
                if (id > 0 && mHandler.hasMessages(id)) {
                    mHandler.removeMessages(id);
                }
            }
        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogModule.i("onNewIntent...");
        setIntent(intent);
        if (getIntent().getExtras() != null) {
            String from = getIntent().getStringExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY);
            if (ModifyNameActivity.TAG.equals(from)) {
                devices.clear();
                devices.addAll(DBTools.getInstance(this).selectAllDevice());
                adapter.notifyDataSetChanged();
                if (!devices.isEmpty()) {
                    lvDeviceList.setVisibility(View.VISIBLE);
                    rlEmpty.setVisibility(View.GONE);
                } else {
                    lvDeviceList.setVisibility(View.GONE);
                    rlEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopService(new Intent(this, MokoService.class));
    }

    public void setAppMqttConfig(View view) {
        startActivity(new Intent(this, SetAppMqttActivity.class));
    }

    public void mainAddDevices(View view) {
//        MqttMessage message = new MqttMessage();
//        message.setPayload("332211".getBytes());
//        message.setQos(1);
//        try {
//            MokoSupport.getInstance().publish("lwz_321", message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }

        String mqttAppConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//        String mqttDeviceConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG, "");
//        if (TextUtils.isEmpty(mqttDeviceConfigStr)) {
//            startActivity(new Intent(this, SetDeviceMqttActivity.class));
//            return;e
//        }
        if (TextUtils.isEmpty(mqttAppConfigStr)) {
            startActivity(new Intent(this, SetAppMqttActivity.class));
            return;
        }
        MQTTConfig mqttConfig = new Gson().fromJson(mqttAppConfigStr, MQTTConfig.class);
        if (TextUtils.isEmpty(mqttConfig.host)) {
            startActivity(new Intent(this, SetAppMqttActivity.class));
            return;
        }
        startActivity(new Intent(this, ScannerDeviceActivity.class));
    }

    @Override
    public void deviceDetailClick(MokoDevice device) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(this, R.string.network_error);
            return;
        }
        if (!device.isOnline) {
            ToastUtils.showToast(this, R.string.device_offline);
            return;
        }
        showLoadingProgressDialog(getString(R.string.wait));
        getScanSwitch(device);
    }

    private void getScanSwitch(MokoDevice device) {
        String mqttConfigAppStr = SPUtiles.getStringValue(MainActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = device.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadScanSwitch(device.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getScanInterval(MokoDevice device) {
        String mqttConfigAppStr = SPUtiles.getStringValue(MainActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = device.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadScanInterval(device.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void about(View view) {
        // 关于
        startActivity(new Intent(this, AboutActivity.class));
    }

    public OfflineHandler mHandler;

    public class OfflineHandler extends BaseMessageHandler<MainActivity> {

        public OfflineHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, Message msg) {
        }
    }
}
