package com.cookandroid.moodiaryfinal;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements DiaryAdapter.OnItemClickListener {

    private static final String TAG = "HomeActivity";
    private MyDatabaseHelper dbHelper;
    private RecyclerView recyclerDiary;
    private DiaryAdapter adapter;
    private List<DiaryEntry> diaryEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 퍼미션 유도 코드
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return; // 퍼미션 받고 돌아올 때까지 대기
        }

        // 퍼미션이 이미 있을 경우 → 모델 로딩
        ModelManager.getInstance().loadModel(getApplicationContext());

        dbHelper = new MyDatabaseHelper(this);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom);
            return insets;
        });

        // 버튼 및 뷰 참조
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        ImageButton btnMore = findViewById(R.id.btnMore);
        ImageButton btnAdd = findViewById(R.id.btnAdd);

        TextView titleText = findViewById(R.id.titleText);
        TextView descText = findViewById(R.id.descText);
        ImageView imgEmotions = findViewById(R.id.imgEmotions);
        LinearLayout summaryLayout = findViewById(R.id.summaryLayout);



        TextView tvTotalEntries = findViewById(R.id.tvTotalEntries);
        TextView tvDaysWritten = findViewById(R.id.tvDaysWritten);
        TextView tvYesterdayEmotion = findViewById(R.id.tvYesterdayEmotion);

        titleText.setText("일기 쓰기 시작하기");
        descText.setText("나만의 일기를 작성해 보세요.\n시작하려면 더하기 버튼을 누르세요.");
        // DB에서 데이터 조회 후 UI 업데이트
        updateHomeSummaryUI(titleText, descText, imgEmotions, summaryLayout, tvTotalEntries, tvDaysWritten, tvYesterdayEmotion);
        // 버튼 리스너
        btnProfile.setOnClickListener(v -> Log.d(TAG, "프로필 버튼 클릭됨"));

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, WriteDiaryHomeActivity.class)));

        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, HomeSearchActivity.class)));

        btnMore.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(this).inflate(R.layout.activity_home_popup, null);
            PopupWindow popupWindow = new PopupWindow(popupView, (int) (180 * getResources().getDisplayMetrics().density), LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            popupView.findViewById(R.id.option_emotion_stats).setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, StatisticsActivity.class);
                startActivity(intent);
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.option_calendar).setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
                startActivity(intent);
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.option_settings).setOnClickListener(view -> {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                popupWindow.dismiss();
            });


            popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, 580, 300);
        });

        // RecyclerView 설정
        recyclerDiary = findViewById(R.id.recyclerDiary);
        diaryEntries = new ArrayList<>();
        loadDiaryEntriesFromDatabase();

        adapter = new DiaryAdapter(this, diaryEntries, this); // OnItemClickListener 적용
        recyclerDiary.setLayoutManager(new LinearLayoutManager(this));
        recyclerDiary.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // TODO: 원하는 스와이프 액션 처리
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                float maxDx = viewHolder.itemView.getWidth() * 0.25f;
                dX = Math.max(-maxDx, Math.min(dX, maxDx));

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    Drawable icon = ContextCompat.getDrawable(HomeActivity.this,
                            dX > 0 ? R.drawable.pencil_f : R.drawable.trash_f);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconLeft, iconRight;

                        if (dX > 0) {
                            iconLeft = itemView.getLeft() + iconMargin;
                            iconRight = iconLeft + icon.getIntrinsicWidth();
                        } else {
                            iconRight = itemView.getRight() - iconMargin;
                            iconLeft = iconRight - icon.getIntrinsicWidth();
                        }

                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerDiary);

        setupDateCards();
    }



    private void loadDiaryEntriesFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MyDatabaseHelper.TABLE_NAME + " ORDER BY " + MyDatabaseHelper.COLUMN_SEQ + " DESC", null);

        diaryEntries.clear();

        if (cursor.moveToFirst()) {
            do {
                String seq = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_SEQ));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_TAG));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_STATUS));

                try {
                    Date date = new SimpleDateFormat("yyMMdd", Locale.getDefault()).parse(seq);
                    diaryEntries.add(new DiaryEntry(seq, content, tag, date, status));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }



    // ✅ 어댑터의 이벤트 처리 메서드들
    @Override
    public void onDeleteClick(DiaryEntry entry) {
        // 1. 데이터베이스에서 해당 일기 삭제
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(MyDatabaseHelper.TABLE_NAME, MyDatabaseHelper.COLUMN_SEQ + "=?", new String[]{entry.seq});
        db.close();

        // 2. 삭제 결과 확인 후 리스트 갱신
        if (deletedRows > 0) {
            diaryEntries.remove(entry); // 리스트에서 제거
            adapter.notifyDataSetChanged(); // RecyclerView 갱신

            // 3. 삭제 후 UI 업데이트 (일기가 없으면 안내 문구 보이기)
            TextView titleText = findViewById(R.id.titleText);
            TextView descText = findViewById(R.id.descText);
            ImageView imgEmotions = findViewById(R.id.imgEmotions);
            LinearLayout summaryLayout = findViewById(R.id.summaryLayout);
            TextView tvTotalEntries = findViewById(R.id.tvTotalEntries);
            TextView tvDaysWritten = findViewById(R.id.tvDaysWritten);
            TextView tvYesterdayEmotion = findViewById(R.id.tvYesterdayEmotion);

            updateHomeSummaryUI(titleText, descText, imgEmotions, summaryLayout, tvTotalEntries, tvDaysWritten, tvYesterdayEmotion);

            Toast.makeText(this, "일기가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "삭제 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onEditClick(DiaryEntry entry) {
        Toast.makeText(this, "편집 기능 호출됨: " + entry.seq, Toast.LENGTH_SHORT).show();
        // 편집 화면으로 이동 등 처리
    }

    @Override
    public void onAnalysisClick(DiaryEntry entry) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("selected_date", entry.seq); // 날짜(seq) 전달
        startActivity(intent);
    }

    private void updateHomeSummaryUI(TextView titleText, TextView descText, ImageView imgEmotions,
                                     LinearLayout summaryLayout, TextView tvTotalEntries,
                                     TextView tvDaysWritten, TextView tvYesterdayEmotion) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MyDatabaseHelper.TABLE_NAME, null);

        LinearLayout dateCardLayout = findViewById(R.id.dateCardLayout);

        if (cursor != null && cursor.getCount() > 0) {
            // 데이터 있을 경우 → 안내 문구 및 이미지 숨김, 요약 정보 표시
            titleText.setVisibility(View.GONE);
            descText.setVisibility(View.GONE);
            imgEmotions.setVisibility(View.GONE);
            summaryLayout.setVisibility(View.VISIBLE);
            dateCardLayout.setVisibility(View.VISIBLE);


            // 총 항목 수
            tvTotalEntries.setText(cursor.getCount() + "개");

            // 일기 쓴 일수
            Cursor dayCursor = db.rawQuery("SELECT COUNT(DISTINCT " + MyDatabaseHelper.COLUMN_SEQ + ") FROM " + MyDatabaseHelper.TABLE_NAME, null);
            if (dayCursor.moveToFirst()) {
                tvDaysWritten.setText(dayCursor.getInt(0) + "일");
            }
            dayCursor.close();

            // 어제 감정
            String yesterdaySeq = new SimpleDateFormat("yyMMdd", Locale.getDefault())
                    .format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
            Cursor yCursor = dbHelper.getEmotionBySeq(yesterdaySeq);
            if (yCursor != null && yCursor.moveToFirst()) {
                // 감정 컬럼들 값 읽기
                float anger = yCursor.getFloat(yCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_ANGER));
                float joy = yCursor.getFloat(yCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_JOY));
                float embarrassment = yCursor.getFloat(yCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_EMBARRASSMENT));
                float sadness = yCursor.getFloat(yCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_SADNESS));
                float anxiety = yCursor.getFloat(yCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_ANXIETY));
                float hurt = yCursor.getFloat(yCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_HURT));

                // 최대값과 감정명 찾기
                String topEmotion = "감정없음";
                float maxValue = -1f;
                if (anger > maxValue) { maxValue = anger; topEmotion = "분노"; }
                if (joy > maxValue) { maxValue = joy; topEmotion = "기쁨"; }
                if (embarrassment > maxValue) { maxValue = embarrassment; topEmotion = "당황"; }
                if (sadness > maxValue) { maxValue = sadness; topEmotion = "슬픔"; }
                if (anxiety > maxValue) { maxValue = anxiety; topEmotion = "불안"; }
                if (hurt > maxValue) { maxValue = hurt; topEmotion = "상처"; }

                tvYesterdayEmotion.setText(topEmotion);
                yCursor.close();
            } else {
                tvYesterdayEmotion.setText("-");
            }

        } else {
            // 데이터 없을 경우 → 안내 문구 및 이미지 보이기, 요약 정보 숨김
            titleText.setVisibility(View.VISIBLE);
            descText.setVisibility(View.VISIBLE);
            imgEmotions.setVisibility(View.VISIBLE);
            summaryLayout.setVisibility(View.GONE);
            dateCardLayout.setVisibility(View.GONE);
        }

        if (cursor != null) cursor.close();
        db.close();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadDiaryEntriesFromDatabase();
        adapter.notifyDataSetChanged();

        // 상단 요약 정보도 갱신
        TextView titleText = findViewById(R.id.titleText);
        TextView descText = findViewById(R.id.descText);
        ImageView imgEmotions = findViewById(R.id.imgEmotions);
        LinearLayout summaryLayout = findViewById(R.id.summaryLayout);
        TextView tvTotalEntries = findViewById(R.id.tvTotalEntries);
        TextView tvDaysWritten = findViewById(R.id.tvDaysWritten);
        TextView tvYesterdayEmotion = findViewById(R.id.tvYesterdayEmotion);

        updateHomeSummaryUI(titleText, descText, imgEmotions, summaryLayout,
                tvTotalEntries, tvDaysWritten, tvYesterdayEmotion);

        Intent incomingIntent = getIntent();

        if (incomingIntent != null && incomingIntent.hasExtra("analysis_result")) {
            String result = incomingIntent.getStringExtra("analysis_result");
            String seq = incomingIntent.getStringExtra("seq");

            new AlertDialog.Builder(this)
                    .setTitle("AI 분석 결과")
                    .setMessage(result)
                    .setPositiveButton("확인", null)
                    .show();

            if (seq != null) {
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
                dbHelper.updateDiaryStatus(seq, "done");
            }

            incomingIntent.removeExtra("analysis_result");
        }
    }

    private void setupDateCards() {
        LocalDate today = LocalDate.now();

        int[] dateTextIds = {
                R.id.tvCardDate1, R.id.tvCardDate2, R.id.tvCardDate3,
                R.id.tvCardDate4, R.id.tvCardDate5
        };

        int[] emotionImageIds = {
                R.id.imgCardEmotion1, R.id.imgCardEmotion2, R.id.imgCardEmotion3,
                R.id.imgCardEmotion4, R.id.imgCardEmotion5
        };

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);

        for (int i = 0; i < 5; i++) {
            LocalDate targetDate = today.minusDays(4 - i);
            String seq = String.format("%02d%02d%02d",
                    targetDate.getYear() % 100,
                    targetDate.getMonthValue(),
                    targetDate.getDayOfMonth());

            final String selectedSeq = seq;  // ✅ 람다에서 안전하게 쓰기 위해 final 변수로 지정

            TextView tvDate = findViewById(dateTextIds[i]);
            tvDate.setText(String.valueOf(targetDate.getDayOfMonth()));

            ImageView ivEmotion = findViewById(emotionImageIds[i]);
            String topEmotion = dbHelper.getTopEmotionByDate(seq);

            int resId;
            if (topEmotion != null && !topEmotion.isEmpty()) {
                resId = getEmotionDrawable(topEmotion);
            } else {
                resId = R.drawable.ic_emotion_none;
            }

            if (resId != 0) {
                ivEmotion.setImageResource(resId);
                ivEmotion.setVisibility(View.VISIBLE);
            } else {
                ivEmotion.setVisibility(View.GONE);
            }

            // ✅ 날짜 또는 감정 클릭 시 통계 화면으로 이동
            View.OnClickListener cardClickListener = v -> {
                Intent intent = new Intent(HomeActivity.this, StatisticsActivity.class);
                intent.putExtra("selected_date", selectedSeq);  // 🟢 안전하게 날짜 seq 넘김
                startActivity(intent);
            };

            tvDate.setOnClickListener(cardClickListener);
            ivEmotion.setOnClickListener(cardClickListener);
        }
    }


    public int getEmotionDrawable(String emotion) {
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
