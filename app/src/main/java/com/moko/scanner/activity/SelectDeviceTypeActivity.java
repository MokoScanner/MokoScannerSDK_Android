package com.moko.scanner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;

import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.SelectDeviceTypeActivity
 */
public class SelectDeviceTypeActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_type);
        ButterKnife.bind(this);

    }

    public void back(View view) {
        finish();
    }

    public void addMokoPlug(View view) {
        Intent intent = new Intent(this, SetDeviceMqttActivity.class);
        intent.putExtra("function", "iot_plug");
        startActivity(intent);
//        startActivity(new Intent(this, AddMokoPlugActivity.class));
    }

    public void addMokoWallSwitch(View view) {
//        Intent intent = new Intent(this, SetDeviceMqttActivity.class);
//        intent.putExtra("function", "iot_wall_switch");
//        startActivity(intent);
//        startActivity(new Intent(this, AddWallSwitchActivity.class));
    }
}
