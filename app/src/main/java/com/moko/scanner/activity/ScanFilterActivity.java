package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.db.DBTools;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.handler.MQTTMessageAssembler;
import com.moko.support.log.LogModule;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanFilterActivity extends BaseActivity {

    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";

    @Bind(R.id.tv_device_name)
    TextView tvDeviceName;
    @Bind(R.id.sb_rssi)
    SeekBar sbRssi;
    @Bind(R.id.tv_rssi)
    TextView tvRssi;
    @Bind(R.id.et_filter_name)
    EditText etFilterName;
    private MokoDevice mMokoDevice;
    private int mPublishType;
    private int mFilterRSSI;
    private String mFilterName;
    private MQTTConfig appMqttConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);

        mMokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);
        tvDeviceName.setText(mMokoDevice.nickName);

        sbRssi.setProgress(Math.abs(-100 - mFilterRSSI));
        sbRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFilterRSSI = progress - 100;
                tvRssi.setText(getString(R.string.scan_filter_rssi, mFilterRSSI));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        tvRssi.setText(getString(R.string.scan_filter_rssi, mFilterRSSI));
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etFilterName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11), inputFilter});
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
        filter.addAction(AppConstants.ACTION_DEVICE_STATE);
        registerReceiver(mReceiver, filter);
        showLoadingProgressDialog(getString(R.string.wait));
        mHandler = new MessageHandler(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissLoadingProgressDialog();
                ScanFilterActivity.this.finish();
            }
        }, 30 * 1000);
        getFilterRSSI();
        getFilterName();
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
                if (mPublishType == 0) {
                    if (state == MokoConstants.MQTT_STATE_SUCCESS) {
                        setFilterName();
                    }
                }
                if (mPublishType == 1) {
                    if (state == MokoConstants.MQTT_STATE_SUCCESS) {
                        dismissLoadingProgressDialog();
                        ToastUtils.showToast(ScanFilterActivity.this, "Succeed");
                        mHandler.removeMessages(1);
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                final String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                byte[] receive = intent.getByteArrayExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                int header = receive[0] & 0xFF;
                if (header == 0x19)// 蓝牙过滤RSSI
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mMokoDevice.uniqueId.equals(new String(id))) {
                        mFilterRSSI = receive[receive.length - 1];
                        sbRssi.setProgress(Math.abs(-100 - mFilterRSSI));
                        tvRssi.setText(getString(R.string.scan_filter_rssi, mFilterRSSI));
                    }
                }
                if (header == 0x20)// 过滤扫描名称
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mMokoDevice.uniqueId.equals(new String(id))) {
                        dismissLoadingProgressDialog();
                        mFilterName = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        etFilterName.setText(mFilterName);
                        mHandler.removeMessages(0);
                    }
                }
            }
            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
                MokoDevice device = DBTools.getInstance(ScanFilterActivity.this).selectDevice(mMokoDevice.uniqueId);
                mMokoDevice.nickName = device.nickName;
            }
            if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (topic.equals(mMokoDevice.topicPublish)) {
                    mMokoDevice.isOnline = false;
                    finish();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void back(View view) {
        finish();
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_confirm:
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(this, R.string.network_error);
                    return;
                }
                if (!mMokoDevice.isOnline) {
                    ToastUtils.showToast(this, R.string.device_offline);
                    return;
                }
                // 发送设置的过滤RSSI和名字
                mFilterName = etFilterName.getText().toString();
                Message message = Message.obtain(mHandler, new Runnable() {
                    @Override
                    public void run() {
                       dismissLoadingProgressDialog();
                    }
                });
                message.what = 1;
                mHandler.sendMessageDelayed(message, 30 * 1000);
                showLoadingProgressDialog(getString(R.string.wait));
                setFilterRSSI();
                break;
        }
    }

    private void setFilterRSSI() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = 0;
        byte[] message = MQTTMessageAssembler.assembleWriteFilterRSSI(mMokoDevice.uniqueId, mFilterRSSI);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void setFilterName() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = 1;
        byte[] message = MQTTMessageAssembler.assembleWriteFilterName(mMokoDevice.uniqueId, mFilterName);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getFilterRSSI() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = -1;
        byte[] message = MQTTMessageAssembler.assembleReadFilterRSSI(mMokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void getFilterName() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mMokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        mPublishType = -1;
        byte[] message = MQTTMessageAssembler.assembleReadFilterName(mMokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MessageHandler mHandler;

    public class MessageHandler extends BaseMessageHandler<ScanFilterActivity> {

        public MessageHandler(ScanFilterActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(ScanFilterActivity activity, Message msg) {
        }
    }
}
