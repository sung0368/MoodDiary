package com.cookandroid.moodiaryfinal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 2;  // ✅ 버전 증가

    // Diary Table
    public static final String TABLE_NAME = "DiaryTable";
    public static final String COLUMN_SEQ = "seq";  // 날짜 기반 PK
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_TAG = "tag";
    public static final String COLUMN_STATUS = "status";

    // Emotion Table
    public static final String EMOTION_TABLE = "EmotionTable";
    public static final String COLUMN_EMOTION_SEQ = "seq"; // FK
    public static final String COLUMN_ANGER = "anger";
    public static final String COLUMN_JOY = "joy";
    public static final String COLUMN_EMBARRASSMENT = "embarrassment";
    public static final String COLUMN_SADNESS = "sadness";
    public static final String COLUMN_ANXIETY = "anxiety";
    public static final String COLUMN_HURT = "hurt";
    public static final String COLUMN_FEEDBACK = "feedback";
    public static final String COLUMN_TOP_EMOTION = "top_emotion";


    private static final String TAG = "MyDBHelper";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DIARY_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_SEQ + " TEXT PRIMARY KEY, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_IMAGE_URI + " TEXT, " +
                COLUMN_TAG + " TEXT, " +
                COLUMN_STATUS + " TEXT)";
        db.execSQL(CREATE_DIARY_TABLE);

        String CREATE_EMOTION_TABLE = "CREATE TABLE " + EMOTION_TABLE + " (" +
                COLUMN_EMOTION_SEQ + " TEXT PRIMARY KEY, " +
                COLUMN_ANGER + " TEXT, " +
                COLUMN_JOY + " TEXT, " +
                COLUMN_EMBARRASSMENT + " TEXT, " +
                COLUMN_SADNESS + " TEXT, " +
                COLUMN_ANXIETY + " TEXT, " +
                COLUMN_HURT + " TEXT, " +
                COLUMN_FEEDBACK + " TEXT, " +
                COLUMN_TOP_EMOTION + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_EMOTION_SEQ + ") REFERENCES " +
                TABLE_NAME + "(" + COLUMN_SEQ + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_EMOTION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ✅ 두 테이블 모두 삭제 후 재생성
        db.execSQL("DROP TABLE IF EXISTS " + EMOTION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);  // 외래 키 제약 조건 활성화
    }

    // 일기 삽입 또는 수정
    public void insertOrUpdateDiary(String seq, String content, String imageUri, String tag, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SEQ, seq);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_TAG, tag);
        values.put(COLUMN_STATUS, status);

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_SEQ + "=?", new String[]{seq}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            db.update(TABLE_NAME, values, COLUMN_SEQ + "=?", new String[]{seq});
        } else {
            db.insert(TABLE_NAME, null, values);
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    // 일기 삭제
    public void deleteDiaryBySeq(String seq) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_SEQ + "=?", new String[]{seq});
        db.close();
    }

    // 특정 일기 조회
    public Cursor getDiaryBySeq(String seq) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, COLUMN_SEQ + "=?", new String[]{seq}, null, null, null);
    }

    // 감정 분석 삽입 또는 수정
    public void insertOrUpdateEmotion(
            String seq, String anger, String joy, String embarrassment,
            String sadness, String anxiety, String hurt, String feedback, String top_emotion) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_EMOTION_SEQ, seq);
        values.put(COLUMN_ANGER, anger);
        values.put(COLUMN_JOY, joy);
        values.put(COLUMN_EMBARRASSMENT, embarrassment);
        values.put(COLUMN_SADNESS, sadness);
        values.put(COLUMN_ANXIETY, anxiety);
        values.put(COLUMN_HURT, hurt);
        values.put(COLUMN_FEEDBACK, feedback);
        values.put(COLUMN_TOP_EMOTION, top_emotion);

        Cursor cursor = db.query(EMOTION_TABLE, null, COLUMN_EMOTION_SEQ + "=?", new String[]{seq}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            db.update(EMOTION_TABLE, values, COLUMN_EMOTION_SEQ + "=?", new String[]{seq});
        } else {
            db.insert(EMOTION_TABLE, null, values);
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    public void updateDiaryStatus(String seq, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, newStatus);
        db.update(TABLE_NAME, values, COLUMN_SEQ + "=?", new String[]{seq});
        db.close();
    }
    public String getDiaryStatus(String seq) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_STATUS}, COLUMN_SEQ + "=?", new String[]{seq}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
            cursor.close();
            return status;
        }

        return null;
    }
    public String getYesterdayTopEmotion(String yesterdaySeq) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(EMOTION_TABLE,
                new String[]{COLUMN_ANGER, COLUMN_JOY, COLUMN_EMBARRASSMENT, COLUMN_SADNESS, COLUMN_ANXIETY, COLUMN_HURT},
                COLUMN_EMOTION_SEQ + "=?", new String[]{yesterdaySeq}, null, null, null);

        String topEmotion = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 감정별 값 읽기 (문자열이라면 숫자로 변환 필요)
            // 예: 컬럼이 텍스트(숫자문자열)일 경우
            float anger = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANGER)));
            float joy = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOY)));
            float embarrassment = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMBARRASSMENT)));
            float sadness = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SADNESS)));
            float anxiety = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANXIETY)));
            float hurt = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HURT)));

            // 감정과 값을 맵으로 저장
            Map<String, Float> emotions = new HashMap<>();
            emotions.put("anger", anger);
            emotions.put("joy", joy);
            emotions.put("embarrassment", embarrassment);
            emotions.put("sadness", sadness);
            emotions.put("anxiety", anxiety);
            emotions.put("hurt", hurt);

            // 가장 큰 값 찾기
            float maxValue = -1;
            for (Map.Entry<String, Float> entry : emotions.entrySet()) {
                if (entry.getValue() > maxValue) {
                    maxValue = entry.getValue();
                    topEmotion = entry.getKey();
                }
            }
            cursor.close();
        }
        db.close();
        return topEmotion;
    }

    // 특정 날짜(seq)의 감정 정보 반환
    public Cursor getEmotionBySeq(String seq) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + EMOTION_TABLE + " WHERE " + COLUMN_SEQ + "=?", new String[]{seq});
    }

    public String getTopEmotionByDate(String seq) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                EMOTION_TABLE,
                new String[]{COLUMN_TOP_EMOTION},
                COLUMN_EMOTION_SEQ + "=?",
                new String[]{seq},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String emotion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOP_EMOTION));
            cursor.close();
            return emotion;
        }

        return null;
    }

    // 감정 분석 삽입 또는 수정
    public void insertOrUpdateEmotion(
            String seq, String anger, String joy, String embarrassment,
            String sadness, String anxiety, String hurt, String feedback) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_EMOTION_SEQ, seq);
        values.put(COLUMN_ANGER, anger);
        values.put(COLUMN_JOY, joy);
        values.put(COLUMN_EMBARRASSMENT, embarrassment);
        values.put(COLUMN_SADNESS, sadness);
        values.put(COLUMN_ANXIETY, anxiety);
        values.put(COLUMN_HURT, hurt);
        values.put(COLUMN_FEEDBACK, feedback);

        Cursor cursor = db.query(EMOTION_TABLE, null, COLUMN_EMOTION_SEQ + "=?", new String[]{seq}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            db.update(EMOTION_TABLE, values, COLUMN_EMOTION_SEQ + "=?", new String[]{seq});
        } else {
            db.insert(EMOTION_TABLE, null, values);
        }

        if (cursor != null) cursor.close();
        db.close();
    }





    public Map<String, Float> getEmotionDataByDate(String date) {
        Map<String, Float> emotionData = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT anger, joy, embarrassment, sadness, anxiety, hurt FROM EmotionTable WHERE seq = ?", new String[]{date});

        if (cursor.moveToFirst()) {
            emotionData.put("분노", cursor.getFloat(0));
            emotionData.put("기쁨", cursor.getFloat(1));
            emotionData.put("당황", cursor.getFloat(2));
            emotionData.put("슬픔", cursor.getFloat(3));
            emotionData.put("불안", cursor.getFloat(4));
            emotionData.put("상처", cursor.getFloat(5));
        }

        cursor.close();
        db.close();
        return emotionData;
    }

    public String getFeedbackByDate(String seq) {
        SQLiteDatabase db = this.getReadableDatabase();
        String feedback = "";

        Cursor cursor = db.rawQuery("SELECT feedback FROM EmotionTable WHERE seq = ?", new String[]{seq});
        if (cursor.moveToFirst()) {
            feedback = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return feedback;
    }


    // 🔻 클래스 마지막 부분에 추가(기존 메서드 뒤 아무 데나 OK)
    public Map<String, Integer> getTopEmotionCountsForWeek(String startSeq, String endSeq) {
        // 기본 0으로 초기화
        Map<String, Integer> counts = new HashMap<>();
        counts.put("anger", 0);
        counts.put("joy", 0);
        counts.put("embarrassment", 0);
        counts.put("sadness", 0);
        counts.put("anxiety", 0);
        counts.put("hurt", 0);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_TOP_EMOTION + ", COUNT(*) AS cnt " +
                        "FROM " + EMOTION_TABLE +
                        " WHERE " + COLUMN_EMOTION_SEQ + " BETWEEN ? AND ? " +
                        "GROUP BY " + COLUMN_TOP_EMOTION,
                new String[]{startSeq, endSeq});

        while (cursor.moveToNext()) {
            String emo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOP_EMOTION));
            int cnt = cursor.getInt(cursor.getColumnIndexOrThrow("cnt"));
            counts.put(emo, cnt);      // 해당 감정 카운트 반영
        }

        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            Log.d(TAG, "▶ " + e.getKey() + " = " + e.getValue());
        }

        cursor.close();
        db.close();
        return counts;
    }


}
