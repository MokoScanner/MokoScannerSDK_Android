package com.moko.scanner;

import android.app.Application;

import com.moko.support.MokoSupport;

import es.dmoral.toasty.Toasty;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MokoSupport.getInstance().init(getApplicationContext());
        Toasty.Config.getInstance().apply();
    }
}
