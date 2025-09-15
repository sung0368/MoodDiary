package com.cookandroid.moodiaryfinal;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class WriteDiaryHomeActivity extends AppCompatActivity {

    private boolean isMenuOpen = false;
    public static boolean isPhotoPending = false;
    public static LocalDate selectedDate;
    TextView dateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writediary_home);

        View rootLayout = findViewById(R.id.rootLayout);
        String savedColor = ColorUtils.getSavedBackgroundColor(this);
        rootLayout.setBackgroundColor(Color.parseColor(savedColor));

        ImageButton btnToggleMenu = findViewById(R.id.btnToggleMenu);
        RelativeLayout menuContainer = findViewById(R.id.menuContainer);
        ImageButton btnPalette = findViewById(R.id.btnPalette);
        View dimView = findViewById(R.id.dimView);
        TextView guideText = findViewById(R.id.guideText);
        TextView guideText2 = findViewById(R.id.guideText2);
        dateText = findViewById(R.id.dateText);
        ImageButton btnPen = findViewById(R.id.btnPen);
        ImageButton btnMic = findViewById(R.id.btnMic);
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);

        selectedDate = LocalDate.now();

        updateMode();

        btnToggleMenu.setOnClickListener(v -> {
            if (isMenuOpen) {
                menuContainer.setVisibility(View.GONE);
                isMenuOpen = false;
            } else {
                menuContainer.setVisibility(View.VISIBLE);
                isMenuOpen = true;
            }
        });

        findViewById(R.id.btnPen).setOnClickListener(v -> {
            // WritingButtonActivity로 이동
            Intent intent = new Intent(WriteDiaryHomeActivity.this, WritingButtonActivity.class);
            intent.putExtra("selectedDate", selectedDate.toString());
            startActivity(intent);

            // 부드러운 페이드 전환 효과 (선택)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

//            menuContainer.setVisibility(View.GONE);
//            isMenuOpen = false;
        });

        findViewById(R.id.btnMic).setOnClickListener(v -> {

            Intent intent = new Intent(WriteDiaryHomeActivity.this, VoiceRecordActivity.class);
            intent.putExtra("selectedDate", selectedDate.toString());
            startActivity(intent);

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

//            menuContainer.setVisibility(View.GONE);
//            isMenuOpen = false;
        });

        findViewById(R.id.btnPhoto).setOnClickListener(v -> {

            // 화면 어둡게 & 안내 텍스트 표시
            dimView.setVisibility(View.VISIBLE);
            guideText.setVisibility(View.GONE);
            guideText2.setVisibility(View.VISIBLE);

            animateAttentionLoop(btnPen);
            animateAttentionLoop(btnMic);

            isPhotoPending = true;

            // 클릭 시 닫기
            dimView.setOnClickListener(dv -> {
                btnPen.animate().cancel();
                btnMic.animate().cancel();
                dimView.setVisibility(View.GONE);
                guideText2.setVisibility(View.GONE);
                guideText.setVisibility(View.VISIBLE);
            });

        });

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            startActivity(new Intent(WriteDiaryHomeActivity.this, HomeActivity.class));
        });

        btnPalette.setOnClickListener(v -> {
            ColorBottomSheet sheet = new ColorBottomSheet();
            sheet.show(getSupportFragmentManager(), sheet.getTag());
            menuContainer.setVisibility(View.GONE);
        });

        btnCalendar.setOnClickListener(v -> {
            MiniCalendarAtWriteDiary dialog = new MiniCalendarAtWriteDiary();
            dialog.setOnDateSelectedListener(date -> {
                String displayText;
                selectedDate = date;

                String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN); // 예: 토요일
                displayText = String.format("%d월 %d일 %s", date.getMonthValue(), date.getDayOfMonth(), dayOfWeek);

                dateText.setText(displayText);
            });
            dialog.show(getSupportFragmentManager(), "MiniCalendarAtWriteDiary");
        });

    }

    private void updateMode() {
        LocalDate today = LocalDate.now();
        String displayText;

        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
        displayText = String.format("%d월 %d일 %s", today.getMonthValue(), today.getDayOfMonth(), dayOfWeek);

        dateText.setText(displayText);
    }

    private void animateAttentionLoop(View view) {
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .withEndAction(() -> animateAttentionLoop(view)) // 다시 반복
                        .start()
                )
                .start();
    }

    public void setBackgroundColor(String colorHex) {
        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(Color.parseColor(colorHex));
    }


}




