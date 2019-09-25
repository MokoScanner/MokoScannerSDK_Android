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
public class SettingsActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

    }

    public void back(View view) {
        finish();
    }

    public void settingForDevice(View view) {
        startActivity(new Intent(this, SetDeviceMqttActivity.class));
    }

    public void settingForAPP(View view) {
        startActivity(new Intent(this, SetAppMqttActivity.class));
    }
}
