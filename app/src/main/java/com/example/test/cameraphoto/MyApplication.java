package com.example.test.cameraphoto;

import android.app.Application;

/**
 * Created by Edison on 2018/11/7.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
