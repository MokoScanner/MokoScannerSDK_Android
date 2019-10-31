package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.adapter.ScanDeviceAdapter;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.db.DBTools;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.entity.ScanDevice;
import com.moko.scanner.service.MokoService;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.handler.MQTTMessageAssembler;
import com.moko.support.utils.MokoUtils;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceDetailActivity extends BaseActivity {
    @Bind(R.id.tv_device_name)
    TextView tvDeviceName;
    @Bind(R.id.iv_scan_switch)
    ImageView ivScanSwitch;
    @Bind(R.id.tv_scan_device_total)
    TextView tvScanDeviceTotal;
    @Bind(R.id.rv_devices)
    RecyclerView rvDevices;
    @Bind(R.id.et_scan_interval)
    EditText etScanInterval;
    @Bind(R.id.ll_scan_interval)
    LinearLayout llScanInterval;
    private boolean mScanSwitch;
    private int mScanInterval;
    private MokoDevice mMokoDevice;
    private ScanDeviceAdapter mAdapter;
    private ArrayList<ScanDevice> mScanDevices;
    private int mPublishType;
    private int mFilterRSSI;
    private String mFilterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        mScanSwitch = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY_SCAN_SWITCH, false);
        mScanInterval = getIntent().getIntExtra(AppConstants.EXTRA_KEY_SCAN_INTERVAL, 0);
        mMokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);
        tvDeviceName.setText(mMokoDevice.nickName);
        ivScanSwitch.setImageResource(mScanSwitch ? R.drawable.checkbox_open : R.drawable.checkbox_close);
        tvScanDeviceTotal.setText(getString(R.string.scan_device_total, 0));
        tvScanDeviceTotal.setVisibility(mScanSwitch ? View.VISIBLE : View.GONE);
        llScanInterval.setVisibility(mScanSwitch ? View.VISIBLE : View.GONE);
        rvDevices.setVisibility(mScanSwitch ? View.VISIBLE : View.GONE);
        etScanInterval.setText(mScanInterval + "");
        etScanInterval.setEnabled(mScanSwitch);
        mScanDevices = new ArrayList<>();
        mAdapter = new ScanDeviceAdapter();
        mAdapter.openLoadAnimation();
        mAdapter.replaceData(mScanDevices);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(mAdapter);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
        filter.addAction(AppConstants.ACTION_DELETE_DEVICE);
        registerReceiver(mReceiver, filter);
        startService(new Intent(this, MokoService.class));
    }

    @OnClick({R.id.rl_edit_filter, R.id.tv_save, R.id.iv_scan_switch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_edit_filter:
                // 获取扫描过滤
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(DeviceDetailActivity.this, R.string.network_error);
                    return;
                }
                if (!mMokoDevice.isOnline) {
                    ToastUtils.showToast(DeviceDetailActivity.this, R.string.device_offline);
                    return;
                }
                getFilterRSSI();
                break;
            case R.id.iv_scan_switch:
                // 切换扫描开关
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(DeviceDetailActivity.this, R.string.network_error);
                    return;
                }
                if (!mMokoDevice.isOnline) {
                    ToastUtils.showToast(DeviceDetailActivity.this, R.string.device_offline);
                    return;
                }
                mScanSwitch = !mScanSwitch;
                showLoadingProgressDialog(getString(R.string.wait));
                ivScanSwitch.setImageResource(mScanSwitch ? R.drawable.checkbox_open : R.drawable.checkbox_close);
                tvScanDeviceTotal.setVisibility(mScanSwitch ? View.VISIBLE : View.GONE);
                llScanInterval.setVisibility(mScanSwitch ? View.VISIBLE : View.GONE);
                rvDevices.setVisibility(mScanSwitch ? View.VISIBLE : View.GONE);
                etScanInterval.setEnabled(mScanSwitch);
                mScanDevices.clear();
                mAdapter.replaceData(mScanDevices);
                setScanSwitch();
                break;
            case R.id.tv_save:
                // 设置扫描间隔
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(DeviceDetailActivity.this, R.string.network_error);
                    return;
                }
                if (!mMokoDevice.isOnline) {
                    ToastUtils.showToast(DeviceDetailActivity.this, R.string.device_offline);
                    return;
                }
                String interval = etScanInterval.getText().toString();
                if (TextUtils.isEmpty(interval)) {
                    ToastUtils.showToast(this, "Scan Time is empty!");
                    return;
                }
                mScanInterval = Integer.parseInt(interval);
                if (mScanInterval < 1 || mScanInterval > 65535) {
                    ToastUtils.showToast(this, "Scan Time range is 1~65535!");
                    return;
                }
                setScanInterval();
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
                if (mPublishType == 1) {
                    if (state == MokoConstants.MQTT_STATE_SUCCESS) {
                        ToastUtils.showToast(DeviceDetailActivity.this, "Succeed");
                    } else {
                        ToastUtils.showToast(DeviceDetailActivity.this, "Failed");
                        etScanInterval.setText("");
                    }
                }
                dismissLoadingProgressDialog();
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                final String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                byte[] receive = intent.getByteArrayExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                int header = receive[0] & 0xFF;
                if (header == 0x21)// 蓝牙广播数据
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    int deviceSize = receive[2 + length] & 0xff;
                    byte[] deviceBytes = Arrays.copyOfRange(receive, 3 + length, receive.length);
                    if (mMokoDevice.uniqueId.equals(new String(id)) && mScanSwitch) {
                        for (int i = 0, l = deviceBytes.length; i < l; ) {
                            ScanDevice scanDevice = new ScanDevice();
                            int deviceLength = deviceBytes[i] & 0xff;
                            i++;
                            String mac = MokoUtils.bytesToHexString(Arrays.copyOfRange(deviceBytes, i, i + 6));
                            scanDevice.mac = mac;
                            i += 6;
                            int rssi = deviceBytes[i];
                            scanDevice.rssi = rssi;
                            i++;
                            int dataLength = deviceBytes[i] & 0xff;
                            i++;
                            String rawData = MokoUtils.bytesToHexString(Arrays.copyOfRange(deviceBytes, i, i + dataLength));
                            scanDevice.rawData = rawData;
                            i += dataLength;
                            int nameLength = deviceLength - 8 - dataLength;
                            if (nameLength > 0) {
                                String name = new String(Arrays.copyOfRange(deviceBytes, i, i + nameLength));
                                scanDevice.name = name;
                            } else {
                                scanDevice.name = "";
                            }
                            i += nameLength;
                            mScanDevices.add(scanDevice);
                        }
                        tvScanDeviceTotal.setText(getString(R.string.scan_device_total, mScanDevices.size()));
                        mAdapter.replaceData(mScanDevices);
                    }
                }
                if (header == 0x19)// 蓝牙过滤RSSI
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mMokoDevice.uniqueId.equals(new String(id))) {
                        mFilterRSSI = receive[receive.length - 1];
                        getFilterName();
                    }
                }
                if (header == 0x20)// 过滤扫描名称
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mMokoDevice.uniqueId.equals(new String(id))) {
                        mFilterName = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        Intent i = new Intent(DeviceDetailActivity.this, ScanFilterActivity.class);
                        i.putExtra(AppConstants.EXTRA_KEY_DEVICE, mMokoDevice);
                        i.putExtra(AppConstants.EXTRA_KEY_FILTER_RSSI, mFilterRSSI);
                        i.putExtra(AppConstants.EXTRA_KEY_FILTER_NAME, mFilterName);
                        startActivity(i);
                    }
                }
            }
            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
                MokoDevice device = DBTools.getInstance(DeviceDetailActivity.this).selectDevice(mMokoDevice.uniqueId);
                mMokoDevice.nickName = device.nickName;
                tvDeviceName.setText(mMokoDevice.nickName);
            }
            if (AppConstants.ACTION_DELETE_DEVICE.equals(action)) {
                if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
                    String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                    if (topic.equals(mMokoDevice.topicPublish)) {
                        mMokoDevice.isOnline = false;
                    }
                }
            }
        }
    };

    private void setScanSwitch() {
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = 0;
        byte[] message = MQTTMessageAssembler.assembleWriteScanSwitch(mMokoDevice.uniqueId, mScanSwitch);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setScanInterval() {
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = 1;
        byte[] message = MQTTMessageAssembler.assembleWriteScanInterval(mMokoDevice.uniqueId, mScanInterval);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void getFilterRSSI() {
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = 0;
        byte[] message = MQTTMessageAssembler.assembleReadFilterRSSI(mMokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getFilterName() {
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = 0;
        byte[] message = MQTTMessageAssembler.assembleReadFilterName(mMokoDevice.uniqueId);
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

    public void more(View view) {
        Intent intent = new Intent(this, MoreActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, mMokoDevice);
        startActivity(intent);
    }

    public void back(View view) {
        finish();
    }
}
