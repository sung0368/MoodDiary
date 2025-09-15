package com.cookandroid.moodiaryfinal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiaryEntry {
    public String seq;
    public String content;
    public String tag;
    public Date date;
    public String status;

    public DiaryEntry(String seq, String content, String tag, Date date, String status) {
        this.seq = seq;
        this.content = content;
        this.tag = tag;
        this.date = date;
        this.status = status;
    }

    public String getSeq() {
        return seq;
    }

    public String getContent() {
        return content;
    }

    public String getTag() {
        return tag;
    }

    public Date getDate() {
        return date;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 E요일", Locale.KOREAN);
        return sdf.format(date);
    }

    public int getEmotionIconResId() {
        switch (status) {
            case "행복":
                return R.drawable.ic_emotion_happy;
            case "슬픔":
                return R.drawable.ic_emotion_sad;
            case "화남":
                return R.drawable.ic_emotion_angry;
            case "혼란":
            case "당황":
                return R.drawable.ic_emotion_confused;
            case "불안":
                return R.drawable.ic_emotion_anxious;
            case "상처":
            case "hurt":
                return R.drawable.ic_emotion_hurt;
            case "없음":
            case "none":
                return R.drawable.ic_emotion_none;
            default:
                return R.drawable.ic_emotion_none;  // 기본 아이콘
        }
    }

}
