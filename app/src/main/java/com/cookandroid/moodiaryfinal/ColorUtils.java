package com.cookandroid.moodiaryfinal;

import android.content.Context;
import android.content.SharedPreferences;

public class ColorUtils {
    private static final String PREF_NAME = "ColorPrefs";
    private static final String KEY_COLOR = "backgroundColor";

    public static void saveBackgroundColor(Context context, String colorHex) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_COLOR, colorHex).apply();
    }

    public static String getSavedBackgroundColor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COLOR, "#FFFFFF"); // 기본값: 흰색
    }
}
