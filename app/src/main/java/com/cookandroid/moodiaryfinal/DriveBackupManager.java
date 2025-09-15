package com.cookandroid.moodiaryfinal;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.util.Collections;
import java.util.concurrent.Executors;

public class DriveBackupManager {

    private final Context context;
    private Drive driveService;

    public DriveBackupManager(Context context) {
        this.context = context;
    }

    public void initDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton("https://www.googleapis.com/auth/drive.file"));
        credential.setSelectedAccount(account.getAccount());

        driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential
        ).setApplicationName("Moodiary").build();
    }

    public void uploadDatabase(String dbName) {
        if (driveService == null) {
            Toast.makeText(context, "Drive 서비스가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        java.io.File dbFile = context.getDatabasePath(dbName);
        if (!dbFile.exists()) {
            Toast.makeText(context, "DB 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Task<String> task = Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            FileContent mediaContent = new FileContent("application/octet-stream", dbFile);
            File metadata = new File().setName("moodiary_backup.db");

            File uploadedFile = driveService.files().create(metadata, mediaContent)
                    .setFields("id")
                    .execute();

            return uploadedFile.getId();
        });

        task.addOnSuccessListener(fileId ->
                Toast.makeText(context, "Drive 업로드 성공", Toast.LENGTH_SHORT).show()
        );

        task.addOnFailureListener(e -> {
            Log.e("DriveBackup", "업로드 실패", e);
            Toast.makeText(context, "Drive 업로드 실패: " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        });
    }
    public void uploadDatabase(String dbName, Runnable onSuccess) {
        if (driveService == null) return;

        java.io.File dbFile = context.getDatabasePath(dbName);
        if (!dbFile.exists()) return;

        Task<String> task = Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            FileContent mediaContent = new FileContent("application/octet-stream", dbFile);
            File metadata = new File().setName("moodiary_backup.db");
            File uploadedFile = driveService.files().create(metadata, mediaContent)
                    .setFields("id")
                    .execute();
            return uploadedFile.getId();
        });

        task.addOnSuccessListener(fileId -> {
            Toast.makeText(context, "Drive 업로드 성공", Toast.LENGTH_SHORT).show();
            if (onSuccess != null) onSuccess.run();  // 🔹 콜백 실행
        });

        task.addOnFailureListener(e -> {
            Toast.makeText(context, "Drive 업로드 실패: " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        });
    }

}
