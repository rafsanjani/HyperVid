package com.foreverrafs.hypervid;

import android.app.Application;

import timber.log.Timber;

public class HyperVidApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}