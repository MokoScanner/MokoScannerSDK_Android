package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
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
import com.moko.support.log.LogModule;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
                dismissLoadingProgressDialog();
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                final String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                String receive = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
//                if (topic.equals("lwz_123")) {
//                    String receive = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
//                    ToastUtils.showToast(MainActivity.this, receive);
//                }
                if (devices.isEmpty()) {
                    return;
                }
//                if (topic.contains(MokoDevice.DEVICE_TOPIC_SWITCH_STATE)) {
//                    JsonObject object = new JsonParser().parse(receive).getAsJsonObject();
                Type type = new TypeToken<MsgCommon<JsonObject>>() {
                }.getType();
                MsgCommon<JsonObject> msgCommon = new Gson().fromJson(receive, type);
                for (final MokoDevice device : devices) {
                    if (device.uniqueId.equals(msgCommon.id)) {
                        device.isOnline = true;
                        if (mHandler.hasMessages(device.id)) {
                            mHandler.removeMessages(device.id);
                        }
                        Message message = Message.obtain(mHandler, new Runnable() {
                            @Override
                            public void run() {
                                device.isOnline = false;
                                device.on_off = false;
                                LogModule.i(device.deviceId + "离线");
                                adapter.notifyDataSetChanged();
                                Intent i = new Intent(AppConstants.ACTION_DEVICE_STATE);
                                i.putExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC, topic);
                                MainActivity.this.sendBroadcast(i);
                            }
                        });
                        message.what = device.id;
                        mHandler.sendMessageDelayed(message, 62 * 1000);
                        if (device.function.equals("iot_plug")) {
                            if (msgCommon.msg_id == MokoConstants.MSG_ID_D_2_A_SWITCH_STATE) {
                                Type infoType = new TypeToken<SwitchInfo>() {
                                }.getType();
                                SwitchInfo switchInfo = new Gson().fromJson(msgCommon.data, infoType);
                                String switch_state = switchInfo.switch_state;
                                // 启动设备定时离线，62s收不到应答则认为离线
                                if (!switch_state.equals(device.on_off ? "on" : "off")) {
                                    device.on_off = !device.on_off;
                                }
                            }

                        } else if (topic.contains("iot_wall_switch")) {
//                            int type = Integer.parseInt(device.type);
//                            String switch_state_1;
//                            String switch_state_2;
//                            String switch_state_3;
//                            switch (type) {
//                                case 1:
//                                    switch_state_1 = object.get("switch_state_01").getAsString();
//                                    device.on_off_1 = "on".equals(switch_state_1);
//                                    break;
//                                case 2:
//                                    switch_state_1 = object.get("switch_state_01").getAsString();
//                                    device.on_off_1 = "on".equals(switch_state_1);
//                                    switch_state_2 = object.get("switch_state_02").getAsString();
//                                    device.on_off_2 = "on".equals(switch_state_2);
//                                    break;
//                                case 3:
//                                    switch_state_1 = object.get("switch_state_01").getAsString();
//                                    device.on_off_1 = "on".equals(switch_state_1);
//                                    switch_state_2 = object.get("switch_state_02").getAsString();
//                                    device.on_off_2 = "on".equals(switch_state_2);
//                                    switch_state_3 = object.get("switch_state_03").getAsString();
//                                    device.on_off_3 = "on".equals(switch_state_3);
//                                    break;
//                            }
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
//                }
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
            if (ModifyNameActivity.TAG.equals(from)
                    || MoreActivity.TAG.equals(from)) {
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
        startActivity(new Intent(this, SelectDeviceTypeActivity.class));
    }

    @Override
    public void deviceDetailClick(MokoDevice device) {
        LogModule.i("跳转详情");
        if ("iot_wall_switch".equals(device.function)) {
            Intent intent = new Intent(this, WallSwitchDetailActivity.class);
            intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, device);
            startActivity(intent);
        } else if ("iot_plug".equals(device.function)) {
            Intent intent = new Intent(this, MokoPlugDetailActivity.class);
            intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, device);
            startActivity(intent);
        }
    }

    @Override
    public void deviceSwitchClick(MokoDevice device) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(this, R.string.network_error);
            return;
        }
        if (!device.isOnline) {
            ToastUtils.showToast(this, R.string.device_offline);
            return;
        }
        showLoadingProgressDialog(getString(R.string.wait));
        LogModule.i("切换开关");
        MsgCommon<SwitchInfo> msgCommon = new MsgCommon();
        msgCommon.msg_id = MokoConstants.MSG_ID_A_2_D_SWITCH_STATE;
        msgCommon.id = device.uniqueId;
        SwitchInfo switchInfo = new SwitchInfo();
        switchInfo.switch_state = device.on_off ? "off" : "on";
        msgCommon.data = switchInfo;
//        JsonObject json = new JsonObject();
//        json.addProperty("switch_state", device.on_off ? "off" : "on");
        String mqttConfigAppStr = SPUtiles.getStringValue(MainActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        MqttMessage message = new MqttMessage();
        message.setPayload(new Gson().toJson(msgCommon).getBytes());
        message.setQos(appMqttConfig.qos);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = device.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        try {
            MokoSupport.getInstance().publish(appTopic, message);
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
