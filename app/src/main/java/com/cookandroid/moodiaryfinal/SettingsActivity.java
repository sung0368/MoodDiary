package com.cookandroid.moodiaryfinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_SETTINGS = 1001;
    private static final int RC_SIGN_IN = 9001;

    private DriveBackupManager driveBackupManager;

    private Switch switchDriveSync, switchNotification;
    private TextView textSyncStatus, textLastSync, textGoogleAccount;

    private GoogleSignInClient googleSignInClient;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchDriveSync   = findViewById(R.id.switchDriveSync);
        switchNotification = findViewById(R.id.switchNotification);
        textSyncStatus     = findViewById(R.id.textSyncStatus);
        textLastSync       = findViewById(R.id.textLastSync);
        textGoogleAccount  = findViewById(R.id.textGoogleAccount);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        boolean driveSyncEnabled = prefs.getBoolean("drive_sync_enabled", false);
        boolean notificationEnabled = prefs.getBoolean("notification_enabled", false);
        switchDriveSync.setChecked(driveSyncEnabled);
        switchNotification.setChecked(notificationEnabled);

        String lastTime = prefs.getString("last_sync_time", null);
        if (lastTime != null) {
            updateLastSyncTime(lastTime);
        } else {
            textLastSync.setVisibility(View.GONE);
        }

        if (driveSyncEnabled) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                updateSyncStatus(account.getEmail(), true);
                textGoogleAccount.setText(account.getEmail());
                textGoogleAccount.setVisibility(View.VISIBLE);
            } else {
                updateSyncStatus("동기화 안됨", false);
                textGoogleAccount.setVisibility(View.GONE);
            }
        } else {
            updateSyncStatus("동기화 안됨", false);
            textGoogleAccount.setVisibility(View.GONE);
        }

        switchDriveSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("drive_sync_enabled", isChecked).apply();
            if (isChecked) {
                startGoogleLogin();
            } else {
                updateSyncStatus("동기화 안됨", false);
                textGoogleAccount.setVisibility(View.GONE);
                textLastSync.setVisibility(View.GONE);
            }
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notification_enabled", isChecked).apply();
            if (isChecked) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivityForResult(intent, REQUEST_NOTIFICATION_SETTINGS);
            }
        });

        findViewById(R.id.tvPermission).setOnClickListener(
                v -> Toast.makeText(this, "권한 정보는 추후 구현됩니다.", Toast.LENGTH_SHORT).show());

        findViewById(R.id.tvAppInfo).setOnClickListener(
                v -> Toast.makeText(this, "앱 정보는 추후 구현됩니다.", Toast.LENGTH_SHORT).show());
    }

    private void updateSyncStatus(String text, boolean success) {
        textSyncStatus.setText(text);
        int colorRes = success ? android.R.color.holo_green_dark : android.R.color.black;
        textSyncStatus.setTextColor(getResources().getColor(colorRes));
    }

    public void updateLastSyncTime(String time) {
        textLastSync.setText("최종 동기화: " + time);
        textLastSync.setVisibility(View.VISIBLE);
        prefs.edit().putString("last_sync_time", time).apply();
    }

    private void startGoogleLogin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String email = account.getEmail();
                    updateSyncStatus(email, true);
                    textGoogleAccount.setText(email);
                    textGoogleAccount.setVisibility(View.VISIBLE);

                    driveBackupManager = new DriveBackupManager(this);
                    driveBackupManager.initDriveService(account);
                    driveBackupManager.uploadDatabase("diary.db", () -> {
                        String currentTime = new SimpleDateFormat("M월 d일 HH:mm", Locale.KOREA).format(new Date());
                        updateLastSyncTime(currentTime);
                    });
                }
            } catch (ApiException e) {
                updateSyncStatus("동기화 안됨", false);
                switchDriveSync.setChecked(false);
                prefs.edit().putBoolean("drive_sync_enabled", false).apply();
                textGoogleAccount.setVisibility(View.GONE);
                textLastSync.setVisibility(View.GONE);
                Toast.makeText(this, "로그인 실패: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}