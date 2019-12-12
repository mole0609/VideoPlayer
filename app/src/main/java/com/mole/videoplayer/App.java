package com.mole.videoplayer;

import android.app.Application;

public class App extends Application {

    public static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
