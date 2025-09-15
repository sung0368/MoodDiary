package com.cookandroid.moodiaryfinal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class StatisticsActivity extends AppCompatActivity {

    private ImageButton btnDay, btnWeek, btnCalendar, btnClose, btnInfo;
    private ImageView infoBubble;
    private TextView textDate, text_day_week;

    private TextView textAngry, textHappy, textConfused, textSad, textAnxious, textHurt;
    private TextView textFeedback;

    private PieChart pieChart;
    private BarChart barChart;

    private boolean isDayMode = true;
    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initViews();
        setupBarChart();
        setupListeners();

        // 1. intent에서 날짜 seq 받아오기
        String seq = getIntent().getStringExtra("selected_date");

        if (seq != null && !seq.isEmpty()) {
            selectedDate = seqToLocalDate(seq);
        } else {
            selectedDate = LocalDate.now();
        }

        updateMode();
    }

    // seq (YYMMDD) -> LocalDate 변환 헬퍼 HomeActivity에서 카드 누르면 넘어가는 코드
    private LocalDate seqToLocalDate(String seq) {
        try {
            int year = 2000 + Integer.parseInt(seq.substring(0, 2));
            int month = Integer.parseInt(seq.substring(2, 4));
            int day = Integer.parseInt(seq.substring(4, 6));
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            e.printStackTrace();
            return LocalDate.now();
        }
    }



    private void initViews() {
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnCalendar = findViewById(R.id.btn_calendar);
        btnClose = findViewById(R.id.btn_close);
        btnInfo = findViewById(R.id.btn_info);
        textDate = findViewById(R.id.text_date);
        text_day_week = findViewById(R.id.text_day_week);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        infoBubble = findViewById(R.id.infoBubble);

        textAngry = findViewById(R.id.text_angry);
        textHappy = findViewById(R.id.text_happy);
        textConfused = findViewById(R.id.text_confused);
        textSad = findViewById(R.id.text_sad);
        textAnxious = findViewById(R.id.text_anxious);
        textHurt = findViewById(R.id.text_hurt);

        textFeedback = findViewById(R.id.text_feedback);  // 추가
    }

    private void setupListeners() {
        btnDay.setOnClickListener(v -> {
            isDayMode = false;
            updateMode();
            text_day_week.setText("주별 통계");
        });

        btnWeek.setOnClickListener(v -> {
            isDayMode = true;
            updateMode();
            text_day_week.setText("일별 통계");
        });

        btnCalendar.setOnClickListener(v -> {
            MiniCalendarDialog dialog = new MiniCalendarDialog();
            dialog.setOnDateSelectedListener(date -> {
                selectedDate = date;
                String displayText;

                if (isDayMode) {
                    String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
                    displayText = String.format("%d월 %d일 %s", date.getMonthValue(), date.getDayOfMonth(), dayOfWeek);
                } else {
                    int weekOfMonth = getWeekOfMonth(date);
                    displayText = String.format("%d월 %s주", date.getMonthValue(), getKoreanWeekLabel(weekOfMonth));
                }

                textDate.setText(displayText);
                updateMode();
            });
            dialog.show(getSupportFragmentManager(), "MiniCalendarDialog");
        });

        btnClose.setOnClickListener(v -> {
            startActivity(new Intent(StatisticsActivity.this, HomeActivity.class));
        });

        btnInfo.setOnClickListener(v -> {
            infoBubble.setVisibility(infoBubble.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
    }

    private void updateMode() {
        String displayText;

        if (isDayMode) {
            btnDay.setVisibility(View.VISIBLE);
            btnWeek.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);

            String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
            displayText = String.format("%d월 %d일 %s", selectedDate.getMonthValue(), selectedDate.getDayOfMonth(), dayOfWeek);

            setupPieChartFromDB(selectedDate.toString());
        } // 🔻 updateMode() 안의 else 블록(주별 모드) 수정
        else {
            btnWeek.setVisibility(View.VISIBLE);
            btnDay.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);

            int weekOfMonth = getWeekOfMonth(selectedDate);
            displayText = String.format("%d월 %s주", selectedDate.getMonthValue(), getKoreanWeekLabel(weekOfMonth));

            // 기존 setupBarChart(); → DB 기반 메서드로 교체
            setupBarChartFromDB(selectedDate);
        }


        textDate.setText(displayText);
    }

    private void setupPieChartFromDB(String date) {
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        String formattedDate = date.substring(2).replace("-", ""); // YYMMDD

        Map<String, Float> emotionMap = dbHelper.getEmotionDataByDate(formattedDate);
        String feedback = dbHelper.getFeedbackByDate(formattedDate);  // 피드백 가져오기

        if (emotionMap == null || emotionMap.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("해당 날짜의 감정 데이터가 없습니다.");
            updateEmotionTextViews(new HashMap<>());
            textFeedback.setText("오늘의 피드백이 없습니다.");
            return;
        }

        Map<String, Integer> emotionColorMap = new HashMap<>();
        emotionColorMap.put("분노", Color.parseColor("#FAB8A0"));
        emotionColorMap.put("기쁨", Color.parseColor("#F5E9B9"));
        emotionColorMap.put("당황", Color.parseColor("#CDDABE"));
        emotionColorMap.put("슬픔", Color.parseColor("#D0D9FA"));
        emotionColorMap.put("불안", Color.parseColor("#E2D0E0"));
        emotionColorMap.put("상처", Color.parseColor("#E5E5E4"));

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Float> entry : emotionMap.entrySet()) {
            float value = entry.getValue();
            if (value > 0f) {
                entries.add(new PieEntry(value, entry.getKey()));
                Integer color = emotionColorMap.get(entry.getKey());
                colors.add(color != null ? color : Color.GRAY);
            }
        }

        updateEmotionTextViews(emotionMap);
        textFeedback.setText(feedback != null && !feedback.isEmpty() ? feedback : "오늘의 피드백이 없습니다.");

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void updateEmotionTextViews(Map<String, Float> emotionMap) {
        textAngry.setText(String.format(Locale.KOREA, "%.0f%%", emotionMap.getOrDefault("분노", 0f)));
        textHappy.setText(String.format(Locale.KOREA, "%.0f%%", emotionMap.getOrDefault("기쁨", 0f)));
        textConfused.setText(String.format(Locale.KOREA, "%.0f%%", emotionMap.getOrDefault("당황", 0f)));
        textSad.setText(String.format(Locale.KOREA, "%.0f%%", emotionMap.getOrDefault("슬픔", 0f)));
        textAnxious.setText(String.format(Locale.KOREA, "%.0f%%", emotionMap.getOrDefault("불안", 0f)));
        textHurt.setText(String.format(Locale.KOREA, "%.0f%%", emotionMap.getOrDefault("상처", 0f)));
    }

    private void setupBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 4));
        entries.add(new BarEntry(1f, 7));
        entries.add(new BarEntry(2f, 7));
        entries.add(new BarEntry(3f, 5));
        entries.add(new BarEntry(4f, 2));
        entries.add(new BarEntry(5f, 6));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FAB8A0"),
                Color.parseColor("#F5E9B9"),
                Color.parseColor("#CDDABE"),
                Color.parseColor("#D0D9FA"),
                Color.parseColor("#E2D0E0"),
                Color.parseColor("#E5E5E4")
        });
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        String[] labels = {"분노", "기쁨", "당황", "슬픔", "불안", "상처"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                return (index >= 0 && index < labels.length) ? labels[index] : "";
            }


        });


        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setTextSize(12f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
    }

    private int getWeekOfMonth(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.KOREA);
        return date.get(weekFields.weekOfMonth());
    }

    private String getKoreanWeekLabel(int weekNum) {
        switch (weekNum) {
            case 1:
                return "첫째";
            case 2:
                return "둘째";
            case 3:
                return "셋째";
            case 4:
                return "넷째";
            case 5:
                return "다섯째";
            default:
                return weekNum + "째";
        }
    }

    // 🔻 StatisticsActivity 끝부분에 추가
    private void setupBarChartFromDB(LocalDate anyDayInWeek) {
        // 1. 주의 시작(월)~끝(일) 날짜 구하기
        LocalDate startOfWeek = anyDayInWeek.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        String startSeq = startOfWeek.toString().substring(2).replace("-", ""); // YYMMDD
        String endSeq = endOfWeek.toString().substring(2).replace("-", "");

        // 2. DB에서 감정별 카운트 가져오기
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        Map<String, Integer> counts = dbHelper.getTopEmotionCountsForWeek(startSeq, endSeq);

        // 3. BarEntry 리스트 생성
        String[] keys = {"anger", "joy", "embarrassment", "sadness", "anxiety", "hurt"};  // 영어 키(디비 저장된 감정명)
        String[] labels = {"분노", "기쁨", "당황", "슬픔", "불안", "상처"};                   // x축에 표시할 한글 라벨

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            entries.add(new BarEntry(i, counts.getOrDefault(keys[i], 0)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FAB8A0"),
                Color.parseColor("#F5E9B9"),
                Color.parseColor("#CDDABE"),
                Color.parseColor("#D0D9FA"),
                Color.parseColor("#E2D0E0"),
                Color.parseColor("#E5E5E4")
        });
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setDrawValues(true);

// 🔻 여기 추가
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.valueOf((int) barEntry.getY());
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        // X축 라벨 셋업
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int idx = (int) value;
                return (idx >= 0 && idx < labels.length) ? labels[idx] : "";
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();

        // 5. 감정 비율을 구해서 텍스트뷰 업데이트
        int total = 0;
        for (String key : keys) {
            total += counts.getOrDefault(key, 0);
        }

        if (total == 0) {
            barChart.clear();
            barChart.setNoDataText("이번 주의 감정 데이터가 없습니다.");
            barChart.invalidate();  // 변경 사항 적용
            updateEmotionTextViewsForWeek(new HashMap<>(), 0);
            return;
        }

        Map<String, Integer> weeklyEmotionCounts = new HashMap<>();
        for (String key : keys) {
            weeklyEmotionCounts.put(key, counts.getOrDefault(key, 0));
        }

        updateEmotionTextViewsForWeek(weeklyEmotionCounts, total);


    }

    private void updateEmotionTextViewsForWeek(Map<String, Integer> countMap, int total) {
        if (total == 0) {
            textAngry.setText("0개");
            textHappy.setText("0개");
            textConfused.setText("0개");
            textSad.setText("0개");
            textAnxious.setText("0개");
            textHurt.setText("0개");
            textFeedback.setText("이번 주의 피드백이 없습니다.");
            return;
        }

        textAngry.setText(String.format(Locale.KOREA, "%d개", countMap.getOrDefault("anger", 0)));
        textHappy.setText(String.format(Locale.KOREA, "%d개", countMap.getOrDefault("joy", 0)));
        textConfused.setText(String.format(Locale.KOREA, "%d개", countMap.getOrDefault("embarrassment", 0)));
        textSad.setText(String.format(Locale.KOREA, "%d개", countMap.getOrDefault("sadness", 0)));
        textAnxious.setText(String.format(Locale.KOREA, "%d개", countMap.getOrDefault("anxiety", 0)));
        textHurt.setText(String.format(Locale.KOREA, "%d개", countMap.getOrDefault("hurt", 0)));

        // top emotion 찾기
        String topEmotion = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topEmotion = entry.getKey();
            }
        }

        if (topEmotion == null) {
            textFeedback.setText("이번 주의 피드백이 없습니다.");
        } else {
            // 감정별 랜덤 조언 불러오기
            String advice = getRandomAdviceForEmotion(topEmotion);
            textFeedback.setText(advice);
        }
    }

    // 감정별 랜덤 조언 예시 함수
    private String getRandomAdviceForEmotion(String emotion) {
        Random random = new Random();
        String[] angerAdvices = {
                "화가 났을 때는 잠시 숨을 깊게 쉬어보세요.",
                "분노를 느낄 때는 운동으로 에너지를 발산해보세요.",
                "감정을 솔직하게 표현하는 것도 중요합니다."
        };
        String[] joyAdvices = {
                "기쁨을 느낄 때는 그 순간을 충분히 즐기세요!",
                "행복한 순간은 주변 사람들과 나누어 보세요.",
                "감사하는 마음을 가져보세요."
        };
        String[] embarrassmentAdvices = {
                "당황스러울 때는 잠시 상황을 객관적으로 바라보세요.",
                "실수를 통해 배우는 점도 많습니다.",
                "자신에게 너무 엄격하지 마세요."
        };
        String[] sadnessAdvices = {
                "슬플 때는 주변 사람과 대화를 나눠보세요.",
                "자신을 돌보는 시간을 가져보세요.",
                "감정을 억누르지 말고 표현하는 것이 좋습니다."
        };
        String[] anxietyAdvices = {
                "불안할 때는 명상이나 심호흡을 시도해보세요.",
                "규칙적인 운동이 불안을 완화하는 데 도움이 됩니다.",
                "긍정적인 생각으로 마음을 다스려보세요."
        };
        String[] hurtAdvices = {
                "상처받았을 때는 시간을 두고 마음을 치유하세요.",
                "필요하면 믿을 수 있는 사람에게 도움을 요청하세요.",
                "자기 자신을 소중히 여기세요."
        };

        switch (emotion) {
            case "anger":
                return angerAdvices[random.nextInt(angerAdvices.length)];
            case "joy":
                return joyAdvices[random.nextInt(joyAdvices.length)];
            case "embarrassment":
                return embarrassmentAdvices[random.nextInt(embarrassmentAdvices.length)];
            case "sadness":
                return sadnessAdvices[random.nextInt(sadnessAdvices.length)];
            case "anxiety":
                return anxietyAdvices[random.nextInt(anxietyAdvices.length)];
            case "hurt":
                return hurtAdvices[random.nextInt(hurtAdvices.length)];
            default:
                return "이번 주의 피드백이 없습니다.";
        }
    }

}
