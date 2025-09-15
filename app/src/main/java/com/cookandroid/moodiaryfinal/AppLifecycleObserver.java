package com.cookandroid.moodiaryfinal;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AppLifecycleObserver implements LifecycleObserver {

    private boolean isInBackground = false;

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onBackground() {
        isInBackground = true;
        Log.d("AppState", "앱이 백그라운드로 이동됨");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onForeground() {
        isInBackground = false;
        Log.d("AppState", "앱이 포그라운드로 돌아옴");
    }

    public boolean isInBackground() {
        return isInBackground;
    }
}
