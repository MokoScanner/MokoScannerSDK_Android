package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.db.DBTools;
import com.moko.scanner.dialog.TimerDialog;
import com.moko.scanner.entity.MQTTConfig;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.entity.MsgCommon;
import com.moko.scanner.entity.SetTimer;
import com.moko.scanner.entity.SwitchInfo;
import com.moko.scanner.entity.TimerInfo;
import com.moko.scanner.utils.SPUtiles;
import com.moko.scanner.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.log.LogModule;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Type;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.MokoPlugDetailActivity
 */
public class MokoPlugDetailActivity extends BaseActivity {
    @Bind(R.id.rl_title)
    RelativeLayout rlTitle;
    @Bind(R.id.iv_switch_state)
    ImageView ivSwitchState;
    @Bind(R.id.tv_device_schedule)
    TextView tvDeviceSchedule;
    @Bind(R.id.tv_device_timer)
    TextView tvDeviceTimer;
    @Bind(R.id.tv_device_statistics)
    TextView tvDeviceStatistics;
    @Bind(R.id.ll_bg)
    LinearLayout llBg;
    @Bind(R.id.tv_switch_state)
    TextView tvSwitchState;
    @Bind(R.id.tv_timer_state)
    TextView tvTimerState;
    private MokoDevice mokoDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moko_plug_detail);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            mokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);
            changeSwitchState();
        }
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
        filter.addAction(AppConstants.ACTION_DEVICE_STATE);
        registerReceiver(mReceiver, filter);
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
                String message = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                Type type = new TypeToken<MsgCommon<JsonObject>>() {
                }.getType();
                MsgCommon<JsonObject> msgCommon = new Gson().fromJson(message, type);
                if (mokoDevice.uniqueId.equals(msgCommon.id)) {
                    mokoDevice.isOnline = true;
                    if (msgCommon.msg_id == MokoConstants.MSG_ID_D_2_A_SWITCH_STATE) {
                        Type infoType = new TypeToken<SwitchInfo>() {
                        }.getType();
                        SwitchInfo switchInfo = new Gson().fromJson(msgCommon.data, infoType);
                        String switch_state = switchInfo.switch_state;
                        // 启动设备定时离线，62s收不到应答则认为离线
                        if (!switch_state.equals(mokoDevice.on_off ? "on" : "off")) {
                            mokoDevice.on_off = !mokoDevice.on_off;
                            changeSwitchState();
                            tvSwitchState.setText(mokoDevice.on_off ? R.string.device_detail_switch_on : R.string.device_detail_switch_off);
                        }
                    }
                    if (msgCommon.msg_id == MokoConstants.MSG_ID_D_2_A_TIMER_INFO) {
                        Type infoType = new TypeToken<TimerInfo>() {
                        }.getType();
                        TimerInfo timerInfo = new Gson().fromJson(msgCommon.data, infoType);
                        int delay_hour = timerInfo.delay_hour;
                        int delay_minute = timerInfo.delay_minute;
                        int delay_second = timerInfo.delay_second;
                        String switch_state = timerInfo.switch_state;
                        if (delay_hour == 0 && delay_minute == 0 && delay_second == 0) {
                            tvTimerState.setVisibility(View.GONE);
                        } else {
                            tvTimerState.setVisibility(View.VISIBLE);
                            String timer = String.format("%s after %d:%d:%d", switch_state, delay_hour, delay_minute, delay_second);
                            tvTimerState.setText(timer);
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
                dismissLoadingProgressDialog();
            }
            if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (topic.equals(mokoDevice.topicPublish)) {
                    mokoDevice.isOnline = false;
                    mokoDevice.on_off = false;
                    tvTimerState.setVisibility(View.GONE);
                    changeSwitchState();
                }
            }
            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
                MokoDevice device = DBTools.getInstance(MokoPlugDetailActivity.this).selectDevice(mokoDevice.deviceId);
                mokoDevice.nickName = device.nickName;
            }
        }
    };

    private void changeSwitchState() {
        rlTitle.setBackgroundColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.blue_0188cc : R.color.black_303a4b));
        llBg.setBackgroundColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.grey_f2f2f2 : R.color.black_303a4b));
        ivSwitchState.setImageDrawable(ContextCompat.getDrawable(this, mokoDevice.on_off ? R.drawable.plug_switch_on : R.drawable.plug_switch_off));
        tvSwitchState.setText(mokoDevice.isOnline ? (mokoDevice.on_off ? R.string.device_detail_switch_on : R.string.device_detail_switch_off) : R.string.device_detail_switch_offline);
        tvSwitchState.setTextColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.blue_0188cc : R.color.grey_808080));
        Drawable drawableSchedult = ContextCompat.getDrawable(this, mokoDevice.on_off ? R.drawable.schedule_on : R.drawable.schedule_off);
        drawableSchedult.setBounds(0, 0, drawableSchedult.getMinimumWidth(), drawableSchedult.getMinimumHeight());
        tvDeviceSchedule.setCompoundDrawables(null, drawableSchedult, null, null);
        tvDeviceSchedule.setTextColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.blue_0188cc : R.color.grey_808080));
        Drawable drawableTimer = ContextCompat.getDrawable(this, mokoDevice.on_off ? R.drawable.timer_on : R.drawable.timer_off);
        drawableTimer.setBounds(0, 0, drawableTimer.getMinimumWidth(), drawableTimer.getMinimumHeight());
        tvDeviceTimer.setCompoundDrawables(null, drawableTimer, null, null);
        tvDeviceTimer.setTextColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.blue_0188cc : R.color.grey_808080));
        Drawable drawableStatistics = ContextCompat.getDrawable(this, mokoDevice.on_off ? R.drawable.statistics_on : R.drawable.statistics_off);
        drawableStatistics.setBounds(0, 0, drawableStatistics.getMinimumWidth(), drawableStatistics.getMinimumHeight());
        tvDeviceStatistics.setCompoundDrawables(null, drawableStatistics, null, null);
        tvDeviceStatistics.setTextColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.blue_0188cc : R.color.grey_808080));
        tvTimerState.setTextColor(ContextCompat.getColor(this, mokoDevice.on_off ? R.color.blue_0188cc : R.color.grey_808080));
    }

    public void back(View view) {
        finish();
    }

    public void more(View view) {
        Intent intent = new Intent(this, MoreActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, mokoDevice);
        startActivity(intent);
    }

    public void timerClick(View view) {
        if (isWindowLocked()) {
            return;
        }
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(MokoPlugDetailActivity.this, R.string.network_error);
            return;
        }
        if (!mokoDevice.isOnline) {
            ToastUtils.showToast(MokoPlugDetailActivity.this, R.string.device_offline);
            return;
        }
        TimerDialog dialog = new TimerDialog(this);
        dialog.setData(mokoDevice.on_off);
        dialog.setListener(new TimerDialog.TimerListener() {
            @Override
            public void onConfirmClick(TimerDialog dialog) {
                if (!MokoSupport.getInstance().isConnected()) {
                    ToastUtils.showToast(MokoPlugDetailActivity.this, R.string.network_error);
                    return;
                }
                if (!mokoDevice.isOnline) {
                    ToastUtils.showToast(MokoPlugDetailActivity.this, R.string.device_offline);
                    return;
                }
                showLoadingProgressDialog(getString(R.string.wait));
                MsgCommon<SetTimer> msgCommon = new MsgCommon();
                msgCommon.msg_id = MokoConstants.MSG_ID_A_2_D_SET_TIMER;
                msgCommon.id = mokoDevice.uniqueId;
                SetTimer setTimer = new SetTimer();
                setTimer.delay_hour = dialog.getWvHour();
                setTimer.delay_minute = dialog.getWvMinute();
                msgCommon.data = setTimer;
//                JsonObject json = new JsonObject();
//                json.addProperty("delay_hour", dialog.getWvHour());
//                json.addProperty("delay_minute", dialog.getWvMinute());
                String mqttConfigAppStr = SPUtiles.getStringValue(MokoPlugDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
                MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
                MqttMessage message = new MqttMessage();
                message.setPayload(new Gson().toJson(msgCommon).getBytes());
                message.setQos(appMqttConfig.qos);
                String appTopic;
                if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
                    appTopic = mokoDevice.topicSubscribe;
                } else {
                    appTopic = appMqttConfig.topicPublish;
                }
                try {
                    MokoSupport.getInstance().publish(appTopic, message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void scheduleClick(View view) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(MokoPlugDetailActivity.this, R.string.network_error);
            return;
        }
        if (!mokoDevice.isOnline) {
            ToastUtils.showToast(MokoPlugDetailActivity.this, R.string.device_offline);
            return;
        }
        ToastUtils.showToast(this, R.string.device_detail_schedule_tips);
    }

    public void statisticsClick(View view) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(this, R.string.network_error);
            return;
        }
        if (!mokoDevice.isOnline) {
            ToastUtils.showToast(this, R.string.device_offline);
            return;
        }
        if (mokoDevice != null && "0".equals(mokoDevice.type)) {
            ToastUtils.showToast(this, getString(R.string.device_info_no_statistics));
            return;
        }
        Intent intent = new Intent(this, ElectricityActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, mokoDevice);
        startActivity(intent);
    }

    public void switchClick(View view) {
        if (!MokoSupport.getInstance().isConnected()) {
            ToastUtils.showToast(this, R.string.network_error);
            return;
        }
        if (!mokoDevice.isOnline) {
            ToastUtils.showToast(this, R.string.device_offline);
            return;
        }
        showLoadingProgressDialog(getString(R.string.wait));
        LogModule.i("切换开关");
        MsgCommon<SwitchInfo> msgCommon = new MsgCommon();
        msgCommon.msg_id = MokoConstants.MSG_ID_A_2_D_SWITCH_STATE;
        msgCommon.id = mokoDevice.uniqueId;
        SwitchInfo switchInfo = new SwitchInfo();
        switchInfo.switch_state = mokoDevice.on_off ? "off" : "on";
        msgCommon.data = switchInfo;
//        JsonObject json = new JsonObject();
//        json.addProperty("switch_state", mokoDevice.on_off ? "off" : "on");
        String mqttConfigAppStr = SPUtiles.getStringValue(MokoPlugDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        MqttMessage message = new MqttMessage();
        message.setPayload(new Gson().toJson(msgCommon).getBytes());
        message.setQos(appMqttConfig.qos);
        String appTopic;
        if (TextUtils.isEmpty(appMqttConfig.topicPublish)) {
            appTopic = mokoDevice.topicSubscribe;
        } else {
            appTopic = appMqttConfig.topicPublish;
        }
        try {
            MokoSupport.getInstance().publish(appTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
