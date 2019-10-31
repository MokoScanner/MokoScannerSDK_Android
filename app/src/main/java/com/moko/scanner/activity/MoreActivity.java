package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.db.DBTools;
import com.moko.scanner.dialog.CustomDialog;
import com.moko.scanner.dialog.RemoveDialog;
import com.moko.scanner.dialog.ResetDialog;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.handler.MQTTMessageAssembler;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2019/10/21
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.MoreActivity
 */
public class MoreActivity extends BaseActivity {

    public static String TAG = "MoreActivity";

    @Bind(R.id.tv_device_name)
    TextView tvDeviceName;
    private MokoDevice mokoDevice;
    private int publishTopic;
    private MQTTConfig appMqttConfig;
    private MoreHandler moreHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            mokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);
            tvDeviceName.setText(mokoDevice.nickName);
        }
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_SUBSCRIBE);
        filter.addAction(MokoConstants.ACTION_MQTT_UNSUBSCRIBE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_DEVICE_STATE);
        registerReceiver(mReceiver, filter);
        moreHandler = new MoreHandler(this);
        String mqttConfigAppStr = SPUtiles.getStringValue(MoreActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
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
                if (mIsDeviceInfoFinished)
                    return;
                if (header == 0x12)// 读取公司名称
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.company_name = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        getProductDate();
                    }
                }
                if (header == 0x13)// 读取生产日期
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.production_date = String.format("%d.%d.%d"
                                , MokoUtils.toInt(Arrays.copyOfRange(receive, 4 + length, 6 + length))
                                , receive[receive.length - 2] & 0xFF
                                , receive[receive.length - 1] & 0xFF);
                        getProductModel();
                    }
                }
                if (header == 0x1A)// 读取设备型号
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.product_model = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        getFirmwareVersion();
                    }
                }
                if (header == 0x15)// 读取固件版本
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mokoDevice.firmware_version = new String(Arrays.copyOfRange(receive, 4 + length, receive.length));
                        getMac();
                    }
                }
                if (header == 0x16)// 读取MAC
                {
                    int length = receive[1] & 0xFF;
                    byte[] id = Arrays.copyOfRange(receive, 2, 2 + length);
                    if (mokoDevice.uniqueId.equals(new String(id))) {
                        mIsDeviceInfoFinished = true;
                        mokoDevice.mac = String.format("%s:%s:%s:%s:%s:%s"
                                , MokoUtils.byte2HexString(receive[receive.length - 6])
                                , MokoUtils.byte2HexString(receive[receive.length - 5])
                                , MokoUtils.byte2HexString(receive[receive.length - 4])
                                , MokoUtils.byte2HexString(receive[receive.length - 3])
                                , MokoUtils.byte2HexString(receive[receive.length - 2])
                                , MokoUtils.byte2HexString(receive[receive.length - 1]));
                        dismissLoadingProgressDialog();
                        Intent i = new Intent(MoreActivity.this, DeviceInfoActivity.class);
                        i.putExtra(AppConstants.EXTRA_KEY_DEVICE, mokoDevice);
                        startActivity(i);
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
                if (state == MokoConstants.MQTT_STATE_SUCCESS) {
                    if (publishTopic == 2) {
                        LogModule.i("重置设备成功");
                        if (TextUtils.isEmpty(appMqttConfig.topicSubscribe)) {
                            // 取消订阅
                            try {
                                MokoSupport.getInstance().unSubscribe(mokoDevice.topicPublish);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                        DBTools.getInstance(MoreActivity.this).deleteDevice(mokoDevice);
                        Intent i = new Intent(AppConstants.ACTION_DELETE_DEVICE);
                        i.putExtra(AppConstants.EXTRA_DELETE_DEVICE_ID, mokoDevice.id);
                        MoreActivity.this.sendBroadcast(i);
                        tvDeviceName.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgressDialog();
                                // 跳转首页，刷新数据
                                Intent intent = new Intent(MoreActivity.this, MainActivity.class);
                                intent.putExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY, TAG);
                                startActivity(intent);
                            }
                        }, 500);
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_SUBSCRIBE.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_UNSUBSCRIBE.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
                dismissLoadingProgressDialog();
            }
            if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (topic.equals(mokoDevice.topicPublish)) {
                    mokoDevice.isOnline = false;
                }
            }
        }
    };

    public void back(View view) {
        finish();
    }

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
            else return null;
        }
    };

    public void modifyName(View view) {
        View content = LayoutInflater.from(this).inflate(R.layout.modify_name, null);
        final EditText etDeviceName = ButterKnife.findById(content, R.id.et_device_name);
        String deviceName = tvDeviceName.getText().toString();
        etDeviceName.setText(deviceName);
        etDeviceName.setSelection(deviceName.length());
        etDeviceName.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
        CustomDialog dialog = new CustomDialog.Builder(this)
                .setContentView(content)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nickName = etDeviceName.getText().toString();
                        if (TextUtils.isEmpty(nickName)) {
                            ToastUtils.showToast(MoreActivity.this, R.string.more_modify_name_tips);
                            return;
                        }
                        mokoDevice.nickName = nickName;
                        DBTools.getInstance(MoreActivity.this).updateDevice(mokoDevice);
                        Intent intent = new Intent(AppConstants.ACTION_MODIFY_NAME);
                        MoreActivity.this.sendBroadcast(intent);
                        tvDeviceName.setText(nickName);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();

        tvDeviceName.postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(etDeviceName);
            }
        }, 300);
    }

    private boolean mIsDeviceInfoFinished;

    public void deviceInfo(View view) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(this, R.string.network_error);
            return;
        }
        if (!mokoDevice.isOnline) {
            ToastUtils.showToast(this, R.string.device_offline);
            return;
        }
        showLoadingProgressDialog(getString(R.string.wait));
        LogModule.i("读取设备信息");
//        try {
//            MokoSupport.getInstance().subscribe(mokoDevice.getDeviceTopicFirmwareInfo(), appMqttConfig.qos);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
        mIsDeviceInfoFinished = false;
        moreHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsDeviceInfoFinished) {
                    ToastUtils.showToast(MoreActivity.this, "Get data failed!");
                    mIsDeviceInfoFinished = true;
                    dismissLoadingProgressDialog();
                }
            }
        }, 20000);
//        MsgCommon<Object> msgCommon = new MsgCommon();
//        msgCommon.msg_id = MokoConstants.MSG_ID_A_2_D_DEVICE_INFO;
//        msgCommon.id = mokoDevice.uniqueId;
//        MqttMessage message = new MqttMessage();
//        message.setPayload(new Gson().toJson(msgCommon).getBytes());
//        message.setQos(appMqttConfig.qos);
//        publishTopic = 1;
//        String appTopic;
//        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
//            appTopic = mokoDevice.topicSubscribe;
//        } else {
//            appTopic = appMqttConfig.topicPublish;
//        }
//        try {
//            MokoSupport.getInstance().publish(appTopic, message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
        getCompanyName();
    }

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

    private void getProductDate() {
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        byte[] message = MQTTMessageAssembler.assembleReadProductDate(mokoDevice.uniqueId);
        try {
            MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

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

    public void checkNewFirmware(View view) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(this, R.string.network_error);
            return;
        }
        if (!mokoDevice.isOnline) {
            ToastUtils.showToast(this, R.string.device_offline);
            return;
        }
        Intent intent = new Intent(this, CheckFirmwareUpdateActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, mokoDevice);
        startActivity(intent);
    }

    public void removeDevice(View view) {
        RemoveDialog dialog = new RemoveDialog(this);
        dialog.setListener(new RemoveDialog.RemoveListener() {
            @Override
            public void onConfirmClick(RemoveDialog dialog) {
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(MoreActivity.this, R.string.network_error);
                    return;
                }
                showLoadingProgressDialog(getString(R.string.wait));
                if (TextUtils.isEmpty(appMqttConfig.topicSubscribe)) {
                    // 取消订阅
                    try {
                        MokoSupport.getInstance().unSubscribe(mokoDevice.topicPublish);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                LogModule.i("删除设备");
                DBTools.getInstance(MoreActivity.this).deleteDevice(mokoDevice);
                Intent i = new Intent(AppConstants.ACTION_DELETE_DEVICE);
                i.putExtra(AppConstants.EXTRA_DELETE_DEVICE_ID, mokoDevice.id);
                MoreActivity.this.sendBroadcast(i);
                tvDeviceName.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingProgressDialog();
                        // 跳转首页，刷新数据
                        Intent intent = new Intent(MoreActivity.this, MainActivity.class);
                        intent.putExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY, TAG);
                        startActivity(intent);
                    }
                }, 500);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void resetDevice(View view) {
        ResetDialog dialog = new ResetDialog(this);
        dialog.setListener(new ResetDialog.ResetListener() {
            @Override
            public void onConfirmClick(ResetDialog dialog) {
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(MoreActivity.this, R.string.network_error);
                    return;
                }
                if (!mokoDevice.isOnline) {
                    ToastUtils.showToast(MoreActivity.this, R.string.device_offline);
                    return;
                }
                showLoadingProgressDialog(getString(R.string.wait));
                LogModule.i("重置设备");
                String appTopic;
                if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
                    appTopic = mokoDevice.topicSubscribe;
                } else {
                    appTopic = appMqttConfig.topicPublish;
                }
                publishTopic = 2;
                byte[] message = MQTTMessageAssembler.assembleWriteReset(mokoDevice.uniqueId);
                try {
                    MokoSupport.getInstance().publish(appTopic, message, appMqttConfig.qos);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void mqttSettingForDevice(View view) {
        startActivity(new Intent(this, SettingForDeviceActivity.class));
    }


    //弹出软键盘
    public void showKeyboard(EditText editText) {
        //其中editText为dialog中的输入框的 EditText
        if (editText != null) {
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }

    public class MoreHandler extends BaseMessageHandler<MoreActivity> {

        public MoreHandler(MoreActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MoreActivity activity, Message msg) {

        }
    }
}
