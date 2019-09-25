package com.moko.scanner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.moko.scanner.AppConstants;
import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.entity.MokoDevice;
import com.moko.scanner.entity.MsgCommon;
import com.moko.scanner.entity.PowerInfo;
import com.moko.support.MokoConstants;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.ElectricityActivity
 */
public class ElectricityActivity extends BaseActivity {


    @Bind(R.id.tv_current)
    TextView tvCurrent;
    @Bind(R.id.tv_voltage)
    TextView tvVoltage;
    @Bind(R.id.tv_power)
    TextView tvPower;

    private MokoDevice mokoDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electricity_manager);
        ButterKnife.bind(this);
        mokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
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
                    if (msgCommon.msg_id == MokoConstants.MSG_ID_D_2_A_POWER_INFO) {
                        Type infoType = new TypeToken<PowerInfo>() {
                        }.getType();
                        PowerInfo powerInfo = new Gson().fromJson(msgCommon.data, infoType);
                        int voltage = powerInfo.voltage;
                        int current = powerInfo.current;
                        int power = powerInfo.power;
                        tvCurrent.setText(current + "");
                        tvVoltage.setText(new DecimalFormat().format(voltage * 0.1));
                        tvPower.setText(power + "");
                    }
                }
            }
        }
    };

    public void back(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
