package com.moko.scanner.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.moko.scanner.R;
import com.moko.scanner.base.BaseActivity;
import com.moko.scanner.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.AboutActivity
 */
public class AboutActivity extends BaseActivity {


    @Bind(R.id.tv_app_version)
    TextView tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        tvAppVersion.setText(Utils.getVersionInfo(this));

    }

    public void back(View view) {
        finish();
    }
}
