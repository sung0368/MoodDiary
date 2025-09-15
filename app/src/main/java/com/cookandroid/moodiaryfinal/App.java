package com.cookandroid.moodiaryfinal;

import android.app.Application;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.ProcessLifecycleOwner;

public class App extends Application {

    public static AppLifecycleObserver lifecycleObserver;  // ✅ 어디서든 접근 가능하도록

    @Override
    public void onCreate() {
        super.onCreate();

        // ✅ 앱 백그라운드/포그라운드 상태 감지 Observer 등록
        lifecycleObserver = new AppLifecycleObserver();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);

        // 기존 모델 로딩 로직 유지
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()) {
            ModelManager.getInstance().loadModel(this);
        } else {
            Log.w("App", "⛔ 모델 로딩 생략됨: 외부 저장소 퍼미션 필요");
        }
    }
}
