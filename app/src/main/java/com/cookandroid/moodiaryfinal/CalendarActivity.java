package com.cookandroid.moodiaryfinal;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// CalendarActivity: 캘린더 화면을 표시하는 메인 액티비티
public class CalendarActivity extends AppCompatActivity {

    private RecyclerView calendarRecyclerView; // 날짜 리스트를 보여주는 RecyclerView
    private TextView txtMonth;                 // 상단에 현재 월 표시
    private ImageButton btnPrev, btnNext, btnClose;      // 이전/다음 달로 넘기는 버튼
    private TextView diaryText;                // 일기 내용 표시
    private ImageView diaryImageView;          // 일기 이미지 표시
    private LocalDate currentMonth; // 현재 보고 있는 월 (LocalDate로 처리)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar); // 전체 화면 레이아웃 연결

        // View ID 연결
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        txtMonth = findViewById(R.id.txt_month);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        diaryText = findViewById(R.id.diaryText);
        diaryImageView = findViewById(R.id.diaryImageView); // 여기서 findViewById 호출
        btnClose = findViewById(R.id.btn_close);

        // 캘린더 RecyclerView는 7열 (월~일)
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));

        // 현재 월을 1일로 초기화
        currentMonth = LocalDate.now().withDayOfMonth(1);

        // 이전 달 버튼 클릭 시
        btnPrev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1); // 한 달 빼기
            updateCalendar(); // 화면 갱신
        });

        // 다음 달 버튼 클릭 시
        btnNext.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1); // 한 달 더하기
            updateCalendar(); // 화면 갱신
        });

        btnClose.setOnClickListener(v -> {
            startActivity(new Intent(CalendarActivity.this, HomeActivity.class));
        });

        // 최초 화면 갱신
        updateCalendar();
    }

    private void updateCalendar() {
        // 년/월 텍스트 갱신 (예: 2025년 4월)
        txtMonth.setText(currentMonth.getYear() + "년 " + currentMonth.getMonthValue() + "월");

        List<CalendarDay> dayList = new ArrayList<>();

        int firstDayOfWeek = currentMonth.getDayOfWeek().getValue(); // 현재 월의 1일 요일 (월=1 ~ 일=7)
        int daysInMonth = currentMonth.lengthOfMonth(); // 해당 월의 총 날짜 수

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);

        // 1일 전까지 빈 칸 채움
        for (int i = 1; i < firstDayOfWeek; i++) {
            dayList.add(new CalendarDay(0, 0)); // day=0 → 빈 날짜
        }

        for (int i = 1; i <= daysInMonth; i++) {
            String seq = String.format("%02d%02d%02d", currentMonth.getYear() % 100, currentMonth.getMonthValue(), i);
            String topEmotion = dbHelper.getTopEmotionByDate(seq);
            int emotionResId = getEmotionDrawable(topEmotion);

            Log.d("CalendarDebug", "📅 " + seq + " → 감정: " + topEmotion + ", drawableId: " + emotionResId);

            dayList.add(new CalendarDay(i, emotionResId));
        }


        // 어댑터 설정
        CalendarAdapter adapter = new CalendarAdapter(dayList);
        adapter.setOnItemClickListener(day -> {
            if (day.day == 0) return;

            int year = currentMonth.getYear() % 100;
            int month = currentMonth.getMonthValue();
            int date = day.day;

            String seq = String.format("%02d%02d%02d", year, month, date);
            Cursor cursor = dbHelper.getDiaryBySeq(seq);

            if (cursor != null && cursor.moveToFirst()) {
                String content = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
                String imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_IMAGE_URI));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_TAG));

                diaryText.setText(content);

                if (imageUriStr != null && !imageUriStr.isEmpty()) {
                    Uri imageUri = Uri.parse(imageUriStr);
                    diaryImageView.setImageURI(imageUri);
                    diaryImageView.setVisibility(View.VISIBLE);
                } else {
                    diaryImageView.setVisibility(View.GONE);
                }

                cursor.close();
            } else {
                diaryText.setText(day.day + "일에는 작성된 일기가 없습니다.");
                diaryImageView.setVisibility(View.GONE);
            }
        });

        // 적용
        calendarRecyclerView.setAdapter(adapter);
    }


    private int getEmotionDrawable(String emotion) {
        if (emotion == null) return 0;  // ✅ 여기 추가해야 null 안전

        switch (emotion) {
            case "joy": return R.drawable.ic_emotion_happy;
            case "anger": return R.drawable.ic_emotion_angry;
            case "sadness": return R.drawable.ic_emotion_sad;
            case "anxiety": return R.drawable.ic_emotion_anxious;
            case "hurt": return R.drawable.ic_emotion_hurt;
            case "embarrassment": return R.drawable.ic_emotion_confused;
            default: return 0;
        }
    }

}
