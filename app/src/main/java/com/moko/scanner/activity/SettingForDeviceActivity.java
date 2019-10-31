package com.moko.scanner.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.utils.SPUtiles;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2019/10/21
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.SettingForDeviceActivity
 */
public class SettingForDeviceActivity extends BaseActivity {

    public static String TAG = "SettingForDeviceActivity";
    @Bind(R.id.tv_host)
    TextView tvHost;
    @Bind(R.id.tv_port)
    TextView tvPort;
    @Bind(R.id.tv_clean_session)
    TextView tvCleanSession;
    @Bind(R.id.tv_user_name)
    TextView tvUserName;
    @Bind(R.id.tv_password)
    TextView tvPassword;
    @Bind(R.id.tv_qos)
    TextView tvQos;
    @Bind(R.id.tv_keep_alive)
    TextView tvKeepAlive;
    @Bind(R.id.tv_client_id)
    TextView tvClientId;
    @Bind(R.id.tv_device_id)
    TextView tvDeviceId;
    @Bind(R.id.tv_connect_mode)
    TextView tvConnectMode;
    @Bind(R.id.tv_subscribe_topic)
    TextView tvSubscribeTopic;
    @Bind(R.id.tv_publish_topic)
    TextView tvPublishTopic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_for_device);
        ButterKnife.bind(this);

        String mqttConfigDeviceStr = SPUtiles.getStringValue(SettingForDeviceActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_DEVICE, "");
        MQTTConfig mqttConfig = new Gson().fromJson(mqttConfigDeviceStr, MQTTConfig.class);

        tvHost.setText(mqttConfig.host);
        tvPort.setText(mqttConfig.port);
        tvCleanSession.setText(mqttConfig.cleanSession ? "ON" : "OFF");
        tvUserName.setText(mqttConfig.username);
        tvPassword.setText(mqttConfig.password);
        tvQos.setText(mqttConfig.qos + "");
        tvKeepAlive.setText(mqttConfig.keepAlive + "");
        tvClientId.setText(mqttConfig.clientId);
        tvDeviceId.setText(mqttConfig.uniqueId);

        if (mqttConfig.connectMode == 0) {
            tvConnectMode.setText(getString(R.string.mqtt_connct_mode_tcp));
        }
        if (mqttConfig.connectMode == 1) {
            tvConnectMode.setText(getString(R.string.mqtt_connct_mode_ssl_one_way));
        }
        if (mqttConfig.connectMode == 3) {
            tvConnectMode.setText(getString(R.string.mqtt_connct_mode_ssl_two_way));
        }
        tvSubscribeTopic.setText(mqttConfig.topicSubscribe);
        tvPublishTopic.setText(mqttConfig.topicPublish);
    }


    public void back(View view) {
        finish();
    }
}
