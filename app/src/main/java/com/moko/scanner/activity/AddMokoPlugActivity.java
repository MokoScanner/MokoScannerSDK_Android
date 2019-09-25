package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.db.DBTools;
import com.moko.scanner.dialog.CustomDialog;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.entity.MsgCommon;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.scanner.utils.Utils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.DeviceResponse;
import com.moko.support.entity.DeviceResult;
import com.moko.support.log.LogModule;
import com.moko.support.service.SocketService;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.AddMokoPlugActivity
 */
public class AddMokoPlugActivity extends BaseActivity {


    @Bind(R.id.not_blinking_tips)
    TextView notBlinkingTips;
    private CustomDialog wifiAlertDialog;
    private CustomDialog mqttConnDialog;
    private DonutProgress donutProgress;
    private SocketService mService;
    private String mWifiSSID;
    private String mWifiPassword;
    private DeviceResult mDeviceResult;
    private MQTTConfig mDeviceMqttConfig;
    private MQTTConfig mAppMqttConfig;
    private boolean isSettingSuccess;
    private boolean isDeviceConnectSuccess;
    private String function;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_moko_plug);
        ButterKnife.bind(this);
        mDeviceMqttConfig = (MQTTConfig) getIntent().getSerializableExtra("mqttConfig");
        function = getIntent().getStringExtra("function");
        notBlinkingTips.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        notBlinkingTips.getPaint().setAntiAlias(true);//抗锯齿
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        mAppMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        bindService(new Intent(this, SocketService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogModule.i("连接服务onServiceConnected...");
            mService = ((SocketService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_AP_CONNECTION);
            filter.addAction(MokoConstants.ACTION_AP_SET_DATA_RESPONSE);
            filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
            filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
            registerReceiver(mReceiver, filter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogModule.i("断开服务onServiceDisconnected...");
            // mMokoService = null;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_AP_CONNECTION.equals(action)) {
                int status = intent.getIntExtra(MokoConstants.EXTRA_AP_CONNECTION, -1);
                if (status == MokoConstants.CONN_STATUS_SUCCESS) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("header", MokoConstants.HEADER_GET_DEVICE_INFO);
                            mService.sendMessage(jsonObject.toString());
                        }
                    }).start();
                } else {
                    dismissLoadingProgressDialog();
                }
            }
            if (MokoConstants.ACTION_AP_SET_DATA_RESPONSE.equals(action)) {
                DeviceResponse response = (DeviceResponse) intent.getSerializableExtra(MokoConstants.EXTRA_AP_SET_DATA_RESPONSE);
                if (response.code == MokoConstants.RESPONSE_SUCCESS) {
                    switch (response.result.header) {
                        case MokoConstants.HEADER_GET_DEVICE_INFO:
                            mDeviceResult = response.result;
                            if ("{device_name}/{device_id}/app_to_device".equals(mDeviceMqttConfig.topicSubscribe)) {
                                mDeviceMqttConfig.topicSubscribe = String.format("%s/%s/app_to_device", mDeviceResult.device_name, mDeviceResult.device_id);
                            }
                            if ("{device_name}/{device_id}/device_to_app".equals(mDeviceMqttConfig.topicPublish)) {
                                mDeviceMqttConfig.topicPublish = String.format("%s/%s/device_to_app", mDeviceResult.device_name, mDeviceResult.device_id);
                            }
                            // 获取设备信息，设置MQTT信息
                            final JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("header", MokoConstants.HEADER_SET_MQTT_INFO);
                            jsonObject.addProperty("host", mDeviceMqttConfig.host);
                            jsonObject.addProperty("port", Integer.parseInt(mDeviceMqttConfig.port));
                            jsonObject.addProperty("connect_mode", mDeviceMqttConfig.connectMode);
                            jsonObject.addProperty("clientid", mDeviceMqttConfig.clientId);
                            jsonObject.addProperty("username", mDeviceMqttConfig.username);
                            jsonObject.addProperty("password", mDeviceMqttConfig.password);
                            jsonObject.addProperty("keepalive", mDeviceMqttConfig.keepAlive);
                            jsonObject.addProperty("qos", mDeviceMqttConfig.qos);
                            jsonObject.addProperty("clean_session", mDeviceMqttConfig.cleanSession ? 1 : 0);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mService.sendMessage(jsonObject.toString());
                                }
                            }).start();
                            break;
                        case MokoConstants.HEADER_SET_MQTT_INFO:
                            // 判断是哪种连接方式，是否需要发送证书文件
                            if (mDeviceMqttConfig.connectMode == 0 || (mDeviceMqttConfig.connectMode > 0 && TextUtils.isEmpty(mDeviceMqttConfig.caPath))) {
                                sendTopic();

                            } else if (mDeviceMqttConfig.connectMode == 1) {
                                // 只发送CA证书
                                sendCA();
                            } else {
                                // 先发送CA证书
                                sendCA();
                            }
                            break;
                        case MokoConstants.HEADER_SET_MQTT_SSL:
                            if (mDeviceMqttConfig.connectMode == 1) {
                                if (mOffset == mSize || mLen == -1) {
                                    sendTopic();
                                    return;
                                }
                                try {
                                    uploadFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (mOffset == mSize || mLen == -1) {
                                    if (mFileType == 1) {
                                        // 发送客户端证书
                                        sendClientCert();
                                        return;
                                    }
                                    if (mFileType == 2) {
                                        // 发送客户端公钥
                                        sendClientKey();
                                        return;
                                    }
                                    if (mFileType == 3) {
                                        sendTopic();
                                        return;
                                    }
                                }
                                try {
                                    uploadFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case MokoConstants.HEADER_SET_TOPIC:
                            sendDeviceId();
                            break;
                        case MokoConstants.HEADER_SET_DEVICE_ID:
                            sendWIFI();
                            break;
                        case MokoConstants.HEADER_SET_WIFI_INFO:
                            // 设置成功，保存数据，网络可用后订阅mqtt主题
                            isSettingSuccess = true;
                            break;
                    }
                } else {
                    ToastUtils.showToast(AddMokoPlugActivity.this, response.message);
                }
            }
            if (action.equals(MokoConstants.ACTION_MQTT_CONNECTION)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
                if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS && isSettingSuccess) {
                    LogModule.i("连接MQTT成功");
                    // 订阅设备主题
//                    String topicSwitchState = mTopicPre + "switch_state";
//                    String topicDelayTime = mTopicPre + "delay_time";
//                    String topicDeleteDevice = mTopicPre + "delete_device";
//                    String topicElectricityInfo = mTopicPre + "electricity_information";
                    // 订阅
                    try {
                        if (TextUtils.isEmpty(mAppMqttConfig.topicSubscribe)) {
                            MokoSupport.getInstance().subscribe(mDeviceMqttConfig.topicPublish, mAppMqttConfig.qos);
                        }
//                        MokoSupport.getInstance().subscribe(topicDelayTime, mAppMqttConfig.qos);
//                        MokoSupport.getInstance().subscribe(topicDeleteDevice, mAppMqttConfig.qos);
//                        MokoSupport.getInstance().subscribe(topicElectricityInfo, mAppMqttConfig.qos);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (action.equals(MokoConstants.ACTION_MQTT_RECEIVE)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                String receive = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                if (TextUtils.isEmpty(topic) || isDeviceConnectSuccess) {
                    return;
                }
                Type type = new TypeToken<MsgCommon<JsonObject>>() {
                }.getType();
                MsgCommon<JsonObject> msgCommon = new Gson().fromJson(receive, type);
                if (!mDeviceMqttConfig.uniqueId.equals(msgCommon.id)) {
                    return;
                }
                if (donutProgress == null)
                    return;
                if (!isDeviceConnectSuccess) {
                    isDeviceConnectSuccess = true;
                    donutProgress.setProgress(100);
                    donutProgress.setText(100 + "%");
                    // 关闭进度条弹框，保存数据，跳转修改设备名称页面
                    notBlinkingTips.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnMqttDialog();
                            MokoDevice mokoDevice = DBTools.getInstance(AddMokoPlugActivity.this).selectDevice(mDeviceResult.device_id);
                            if (mokoDevice == null) {
                                mokoDevice = new MokoDevice();
                                mokoDevice.name = mDeviceResult.device_name;
                                mokoDevice.nickName = mDeviceResult.device_name;
                                mokoDevice.specifications = mDeviceResult.device_specifications;
                                // TODO: 2019/8/14 设备不再传给app，由app区分
                                mokoDevice.function = function;
                                mokoDevice.deviceId = mDeviceResult.device_id;
                                mokoDevice.type = mDeviceResult.device_type;
                                mokoDevice.topicSubscribe = mDeviceMqttConfig.topicSubscribe;
                                mokoDevice.topicPublish = mDeviceMqttConfig.topicPublish;
                                mokoDevice.uniqueId = mDeviceMqttConfig.uniqueId;
                                DBTools.getInstance(AddMokoPlugActivity.this).insertDevice(mokoDevice);
                            } else {
                                mokoDevice.name = mDeviceResult.device_name;
                                mokoDevice.specifications = mDeviceResult.device_specifications;
                                // TODO: 2019/8/14 设备不再传给app，由app区分
                                mokoDevice.function = function;
                                mokoDevice.type = mDeviceResult.device_type;
                                mokoDevice.uniqueId = mDeviceMqttConfig.uniqueId;
                                DBTools.getInstance(AddMokoPlugActivity.this).updateDevice(mokoDevice);
                            }
                            Intent modifyIntent = new Intent(AddMokoPlugActivity.this, ModifyNameActivity.class);
                            modifyIntent.putExtra("mokodevice", mokoDevice);
                            startActivity(modifyIntent);
                        }
                    }, 500);
                }
            }
        }
    };

    private void sendDeviceId() {
        // 获取MQTT信息，设置DeviceId
        final JsonObject deviceId = new JsonObject();
        deviceId.addProperty("header", MokoConstants.HEADER_SET_DEVICE_ID);
        deviceId.addProperty("id", mDeviceMqttConfig.uniqueId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mService.sendMessage(deviceId.toString());
            }
        }).start();
    }

    private void sendWIFI() {
        // 获取MQTT信息，设置WIFI信息
        final JsonObject wifiInfo = new JsonObject();
        wifiInfo.addProperty("header", MokoConstants.HEADER_SET_WIFI_INFO);
        wifiInfo.addProperty("wifi_ssid", mWifiSSID);
        wifiInfo.addProperty("wifi_pwd", mWifiPassword);
        wifiInfo.addProperty("wifi_security", 3);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mService.sendMessage(wifiInfo.toString());
            }
        }).start();
    }

    private void sendTopic() {
        // 设置主题
        final JsonObject topicInfo = new JsonObject();
        topicInfo.addProperty("header", MokoConstants.HEADER_SET_TOPIC);
        topicInfo.addProperty("set_publish_topic", mDeviceMqttConfig.topicPublish);
        topicInfo.addProperty("set_subscibe_topic", mDeviceMqttConfig.topicSubscribe);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mService.sendMessage(topicInfo.toString());
            }
        }).start();
    }

    private void sendClientKey() {
        try {
            mFile = new File(mDeviceMqttConfig.clientKeyPath);
            if (mFile.exists()) {
                mFileType = 3;
                mSize = mFile.length();
                // 判断输入流中的数据是否已经读完的标识
                mLen = 0;
                mOffset = 0;
                mInputSteam = new FileInputStream(mFile);
                mBufferSize = Math.min(mInputSteam.available(), 200);
                // 创建一个缓冲区
                mBuffer = new byte[mBufferSize];
                uploadFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendClientCert() {
        try {
            mFile = new File(mDeviceMqttConfig.clientCertPath);
            if (mFile.exists()) {
                mFileType = 2;
                mSize = mFile.length();
                // 判断输入流中的数据是否已经读完的标识
                mLen = 0;
                mOffset = 0;
                mInputSteam = new FileInputStream(mFile);
                mBufferSize = Math.min(mInputSteam.available(), 200);
                // 创建一个缓冲区
                mBuffer = new byte[mBufferSize];
                uploadFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCA() {
        try {
            mFile = new File(mDeviceMqttConfig.caPath);
            if (mFile.exists()) {
                mFileType = 1;
                mSize = mFile.length();
                // 判断输入流中的数据是否已经读完的标识
                mLen = 0;
                mOffset = 0;
                mInputSteam = new FileInputStream(mFile);
                mBufferSize = Math.min(mInputSteam.available(), 200);
                // 创建一个缓冲区
                mBuffer = new byte[mBufferSize];
                uploadFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File mFile;
    private long mSize;
    private byte[] mBuffer;
    private int mLen;
    private int mOffset;
    private InputStream mInputSteam;
    private int mFileType;
    private int mBufferSize;

    private void uploadFile() throws IOException {
        if ((mLen = mInputSteam.read(mBuffer)) > 0) {
            // 发送buffer，收到应答后继续发送下一段
            final JsonObject sslInfo = new JsonObject();
            sslInfo.addProperty("header", MokoConstants.HEADER_SET_MQTT_SSL);
            sslInfo.addProperty("file_type", mFileType);
            sslInfo.addProperty("file_size", mSize);
            sslInfo.addProperty("offset", mOffset);
            sslInfo.addProperty("current_packet_len", mLen);
            String data = new String(mBuffer);
            sslInfo.addProperty("data", data);
            mOffset += mLen;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.sendMessage(sslInfo.toString());
                }
            }).start();
            mBufferSize = Math.min(mInputSteam.available(), 200);
            mBuffer = new byte[mBufferSize];
        }
    }

    public void back(View view) {
        finish();
    }

    /**
     * @Date 2018/6/12
     * @Author wenzheng.liu
     * @Description 查看打开AP步骤
     * @ClassPath com.moko.scanner.activity.AddMokoPlugActivity
     */
    public void notBlinking(View view) {
        startActivityForResult(new Intent(this, OperationPlugStepsActivity.class), AppConstants.REQUEST_CODE_OPERATION_STEP);
    }

    /**
     * @Date 2018/6/12
     * @Author wenzheng.liu
     * @Description 判断是否连接设备wifi
     */
    public void plugBlinking(View view) {
        isDeviceConnectSuccess = false;
        checkWifiInfo();
    }

    private void checkWifiInfo() {
        if (!isWifiCorrect()) {
            View wifiAlertView = LayoutInflater.from(this).inflate(R.layout.wifi_setting_content, null);
            ImageView iv_wifi_alert = ButterKnife.findById(wifiAlertView, R.id.iv_wifi_alert);
            iv_wifi_alert.setImageResource(R.drawable.plug_wifi_alert);
            wifiAlertDialog = new CustomDialog.Builder(this)
                    .setContentView(wifiAlertView)
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳转系统WIFI页面
                            Intent intent = new Intent();
                            intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                            startActivityForResult(intent, AppConstants.REQUEST_CODE_WIFI_SETTING);
                        }
                    })
                    .create();
            wifiAlertDialog.show();
        } else {
            // 弹出输入WIFI弹框
            showWifiInputDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_WIFI_SETTING) {
            if (isWifiCorrect()) {
                // 弹出输入WIFI弹框
                if (wifiAlertDialog != null && !isFinishing() && wifiAlertDialog.isShowing()) {
                    wifiAlertDialog.dismiss();
                }
                showWifiInputDialog();
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_OPERATION_STEP) {
            if (resultCode == RESULT_OK) {
                checkWifiInfo();
            }
        }
    }

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
            else return null;
        }
    };

    private void showWifiInputDialog() {
        View wifiInputView = LayoutInflater.from(this).inflate(R.layout.wifi_input_content, null);
        final EditText etSSID = ButterKnife.findById(wifiInputView, R.id.et_ssid);
        etSSID.setFilters(new InputFilter[]{filter});
        final EditText etPassword = ButterKnife.findById(wifiInputView, R.id.et_password);
        CustomDialog dialog = new CustomDialog.Builder(this)
                .setContentView(wifiInputView)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWifiSSID = etSSID.getText().toString();
                        // 获取WIFI后，连接成功后发给设备
                        if (TextUtils.isEmpty(mWifiSSID)) {
                            ToastUtils.showToast(AddMokoPlugActivity.this, getString(R.string.wifi_verify_empty));
                            return;
                        }
                        dialog.dismiss();
                        showLoadingProgressDialog(getString(R.string.wait));
                        notBlinkingTips.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgressDialog();
                                if (isWifiCorrect()) {
                                    mWifiPassword = etPassword.getText().toString();
                                    // 弹出加载弹框
                                    showConnMqttDialog();
                                    // 连接设备
                                    mService.startSocket();
                                }
                            }
                        }, 2000);
                    }
                })
                .create();
        dialog.show();
    }

    private int progress;

    private void showConnMqttDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.mqtt_conn_content, null);
        donutProgress = ButterKnife.findById(view, R.id.dp_progress);
        mqttConnDialog = new CustomDialog.Builder(this)
                .setContentView(view)
                .create();
        mqttConnDialog.setCancelable(false);
        mqttConnDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                progress = 0;
                while (progress <= 100 && !isDeviceConnectSuccess) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            donutProgress.setProgress(progress);
                            donutProgress.setText(progress + "%");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress++;
                }
            }
        }).start();
        notBlinkingTips.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDeviceConnectSuccess) {
                    isDeviceConnectSuccess = true;
                    dismissConnMqttDialog();
                    ToastUtils.showToast(AddMokoPlugActivity.this, getString(R.string.mqtt_connecting_timeout));
                }
            }
        }, 90 * 1000);
    }

    private void dismissConnMqttDialog() {
        if (mqttConnDialog != null && !isFinishing() && mqttConnDialog.isShowing()) {
            mqttConnDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    public boolean isWifiCorrect() {
        String ssid = Utils.getWifiSSID(this);
        if (TextUtils.isEmpty(ssid) || !ssid.startsWith("\"MK")) {
            return false;
        } else {
            return true;
        }
    }
}
