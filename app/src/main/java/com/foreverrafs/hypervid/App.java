package com.foreverrafs.hypervid;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}