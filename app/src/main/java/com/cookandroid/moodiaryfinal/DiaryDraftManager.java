package com.cookandroid.moodiaryfinal;

import android.net.Uri;

public class DiaryDraftManager {

    private static DiaryDraftManager instance;

    private String draftText = "";
    private Uri imageUri = null;

    private DiaryDraftManager() {}

    public static synchronized DiaryDraftManager getInstance() {
        if (instance == null) {
            instance = new DiaryDraftManager();
        }
        return instance;
    }

    public void setDraftText(String text) {
        this.draftText = text;
    }

    public String getDraftText() {
        return draftText;
    }

    public void setImageUri(Uri uri) {
        this.imageUri = uri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void clear() {
        draftText = "";
        imageUri = null;
    }
}
