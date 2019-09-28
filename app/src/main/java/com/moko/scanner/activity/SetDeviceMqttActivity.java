package com.moko.scanner.activity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.dialog.KeepAliveDialog;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.fragment.OnewaySSLFragment;
import com.moko.scanner.fragment.TwowaySSLFragment;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.SetDeviceMqttActivity
 */
public class SetDeviceMqttActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    @Bind(R.id.et_mqtt_host)
    EditText etMqttHost;
    @Bind(R.id.et_mqtt_port)
    EditText etMqttPort;
    @Bind(R.id.iv_clean_session)
    ImageView ivCleanSession;
    @Bind(R.id.et_mqtt_username)
    EditText etMqttUsername;
    @Bind(R.id.et_mqtt_password)
    EditText etMqttPassword;
    @Bind(R.id.tv_qos)
    TextView tvQos;
    @Bind(R.id.tv_keep_alive)
    TextView tvKeepAlive;
    @Bind(R.id.et_mqtt_client_id)
    EditText etMqttClientId;
    @Bind(R.id.et_mqtt_device_id)
    EditText etMqttDeviceId;
    @Bind(R.id.rb_conn_mode_tcp)
    RadioButton rbConnModeTcp;
    @Bind(R.id.rb_conn_mode_ssl_oneway)
    RadioButton rbConnModeSslOneway;
    @Bind(R.id.rb_conn_mode_ssl_twoway)
    RadioButton rbConnModeSslTwoway;
    @Bind(R.id.rg_conn_mode)
    RadioGroup rgConnMode;
    @Bind(R.id.frame_connect_mode)
    FrameLayout frameConnectMode;
    @Bind(R.id.rl_client_id)
    RelativeLayout rlClientId;
    @Bind(R.id.tv_connect_mode)
    TextView tvConnectMode;
    @Bind(R.id.et_topic_subscribe)
    EditText etTopicSubscribe;
    @Bind(R.id.et_topic_publish)
    EditText etTopicPublish;

    private FragmentManager fragmentManager;
    private OnewaySSLFragment onewaySSLFragment;
    private TwowaySSLFragment twowaySSLFragment;


    private String[] mQosArray = new String[]{"0", "1", "2"};


    private MQTTConfig mqttConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_device);
        ButterKnife.bind(this);
        String mqttConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");

        if (TextUtils.isEmpty(mqttConfigStr)) {
            mqttConfig = new MQTTConfig();
        } else {
            Gson gson = new Gson();
            mqttConfig = gson.fromJson(mqttConfigStr, MQTTConfig.class);
            mqttConfig.connectMode = 0;
            mqttConfig.qos = 1;
            mqttConfig.keepAlive = 60;
            mqttConfig.clientId = "";
            mqttConfig.username = "";
            mqttConfig.password = "";
            mqttConfig.caPath = "";
            mqttConfig.clientKeyPath = "";
            mqttConfig.clientCertPath = "";
            mqttConfig.topicPublish = "";
            mqttConfig.topicSubscribe = "";
        }
        fragmentManager = getFragmentManager();
        createFragment();
        initData();
        // 注册广播接收器
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
//        registerReceiver(mReceiver, filter);
    }

    private void createFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        onewaySSLFragment = OnewaySSLFragment.newInstance();
        fragmentTransaction.add(R.id.frame_connect_mode, onewaySSLFragment);
        twowaySSLFragment = TwowaySSLFragment.newInstance();
        fragmentTransaction.add(R.id.frame_connect_mode, twowaySSLFragment);
        fragmentTransaction.hide(onewaySSLFragment).hide(twowaySSLFragment).commit();
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
//                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
//                if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS) {
//                    ToastUtils.showToast(SetDeviceMqttActivity.this, getString(R.string.success));
//                    dismissLoadingProgressDialog();
//                    SetDeviceMqttActivity.this.finish();
//                }
//            }
//        }
//    };

    private void initData() {
        etMqttHost.setText(mqttConfig.host);
        etMqttPort.setText(mqttConfig.port);
        tvQos.setText(mQosArray[mqttConfig.qos]);
        ivCleanSession.setImageDrawable(ContextCompat.getDrawable(this, mqttConfig.cleanSession ? R.drawable.checkbox_open : R.drawable.checkbox_close));
        rgConnMode.setOnCheckedChangeListener(this);
        switch (mqttConfig.connectMode) {
            case 0:
                rbConnModeTcp.setChecked(true);
                break;
            case 1:
                rbConnModeSslOneway.setChecked(true);
                break;
            case 3:
                rbConnModeSslTwoway.setChecked(true);
                break;
        }
        tvKeepAlive.setText(mqttConfig.keepAlive + "");
        etMqttClientId.setText(mqttConfig.clientId);
        etMqttDeviceId.setText(mqttConfig.uniqueId);
        etMqttUsername.setText(mqttConfig.username);
        etMqttPassword.setText(mqttConfig.password);
    }

    public void back(View view) {
        finish();
    }

    public void clearSettings(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Clear All Parameters")
                .setMessage("Please confirm whether to clear all parameters?")
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mqttConfig.reset();
                        initData();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public void checkQos(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setSingleChoiceItems(mQosArray, mqttConfig.qos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mqttConfig.qos = which;
                        tvQos.setText(mQosArray[mqttConfig.qos]);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }


    public void checkKeepAlive(View view) {
        KeepAliveDialog dialog = new KeepAliveDialog();
        dialog.setSelected(mqttConfig.keepAlive);
        dialog.setListener(new KeepAliveDialog.OnDataSelectedListener() {
            @Override
            public void onDataSelected(String data) {
                mqttConfig.keepAlive = Integer.parseInt(data);
                tvKeepAlive.setText(data);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void saveSettings(View view) {
        mqttConfig.host = etMqttHost.getText().toString().replaceAll(" ", "");
        mqttConfig.port = etMqttPort.getText().toString();
        mqttConfig.clientId = etMqttClientId.getText().toString().replaceAll(" ", "");
        mqttConfig.uniqueId = etMqttDeviceId.getText().toString().replaceAll(" ", "");
        mqttConfig.username = etMqttUsername.getText().toString().replaceAll(" ", "");
        mqttConfig.password = etMqttPassword.getText().toString().replaceAll(" ", "");
        mqttConfig.topicSubscribe = etTopicSubscribe.getText().toString().replaceAll(" ", "");
        mqttConfig.topicPublish = etTopicPublish.getText().toString().replaceAll(" ", "");
        if (mqttConfig.isError(this)) {
            return;
        }
        String clientId = etMqttClientId.getText().toString();
        if (TextUtils.isEmpty(clientId)) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_client_id_empty));
            return;
        }
        String deviceId = etMqttDeviceId.getText().toString();
        if (TextUtils.isEmpty(deviceId)) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_device_id_empty));
            return;
        }
        if (rbConnModeSslOneway.isChecked()) {
            mqttConfig.caPath = onewaySSLFragment.getCAFilePath();
        }
        if (rbConnModeSslTwoway.isChecked()) {
            mqttConfig.caPath = twowaySSLFragment.getCAFilePath();
//            if (TextUtils.isEmpty(mqttConfig.caPath)) {
//                ToastUtils.showToast(this, getString(R.string.mqtt_verify_ca));
//                return;
//            }
            mqttConfig.clientKeyPath = twowaySSLFragment.getClientKeyPath();
            if (TextUtils.isEmpty(mqttConfig.clientKeyPath)) {
                ToastUtils.showToast(this, getString(R.string.mqtt_verify_client_key));
                return;
            }
            mqttConfig.clientCertPath = twowaySSLFragment.getClientCertPath();
            if (TextUtils.isEmpty(mqttConfig.clientCertPath)) {
                ToastUtils.showToast(this, getString(R.string.mqtt_verify_client_cert));
                return;
            }
        }
        String topicSubscribe = etTopicSubscribe.getText().toString();
        if (TextUtils.isEmpty(topicSubscribe)) {
            ToastUtils.showToast(this, getString(R.string.mqtt_topic_subscribe));
            return;
        }
        mqttConfig.topicSubscribe = topicSubscribe;
        String topicPublish = etTopicPublish.getText().toString();
        if (TextUtils.isEmpty(topicPublish)) {
            ToastUtils.showToast(this, getString(R.string.mqtt_topic_publish));
            return;
        }
        mqttConfig.topicPublish = topicPublish;
//        if ("iot_plug".equals(function)) {
//            Intent intent = new Intent(this, AddMokoPlugActivity.class);
//            intent.putExtra("function", function);
//            intent.putExtra("mqttConfig", mqttConfig);
//            startActivity(intent);
//        } else if ("iot_wall_switch".equals(function)) {
//            Intent intent = new Intent(this, AddWallSwitchActivity.class);
//            intent.putExtra("function", function);
//            intent.putExtra("mqttConfig", mqttConfig);
//            startActivity(intent);
//        }


//        String mqttConfigStr = new Gson().toJson(mqttConfig, MQTTConfig.class);
//        SPUtiles.setStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, mqttConfigStr);
//        stopService(new Intent(this, MokoService.class));
//        showLoadingProgressDialog(getString(R.string.mqtt_connecting));
//        tvKeepAlive.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startService(new Intent(SetDeviceMqttActivity.this, MokoService.class));
//            }
//        }, 2000);

    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(mReceiver);
//    }

    public void cleanSession(View view) {
        mqttConfig.cleanSession = !mqttConfig.cleanSession;
        ivCleanSession.setImageDrawable(ContextCompat.getDrawable(this, mqttConfig.cleanSession ? R.drawable.checkbox_open : R.drawable.checkbox_close));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (checkedId) {
            case R.id.rb_conn_mode_tcp:
                mqttConfig.connectMode = 0;
                fragmentTransaction.hide(onewaySSLFragment).hide(twowaySSLFragment).commit();
                break;
            case R.id.rb_conn_mode_ssl_oneway:
                mqttConfig.connectMode = 1;
                fragmentTransaction.show(onewaySSLFragment).hide(twowaySSLFragment).commit();
                onewaySSLFragment.setCAFilePath(mqttConfig);
                break;
            case R.id.rb_conn_mode_ssl_twoway:
                mqttConfig.connectMode = 3;
                fragmentTransaction.hide(onewaySSLFragment).show(twowaySSLFragment).commit();
                twowaySSLFragment.setCAFilePath(mqttConfig);
                twowaySSLFragment.setClientKeyPath(mqttConfig);
                twowaySSLFragment.setClientCertPath(mqttConfig);
                break;
        }
    }
}
