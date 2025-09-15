package com.cookandroid.moodiaryfinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceRecordActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY_PERMISSION = 1001;
    private static final int REQUEST_PICK_IMAGE = 1002;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2003;

    private boolean isMenuOpen = false;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private ImageView selectedImage;
    private ImageButton btnCloseImage;
    private FrameLayout imageContainer;

    private SpeechRecognizerHelper speechHelper;

    private EditText editText;
    private ImageView voiceWave;
    private TextView recordGuide;

    private Uri selectedImageUri;
    private MyDatabaseHelper dbHelper;
    private String todaySeq;
    private LocalDate selectedDate;
    private SimpleDateFormat sdf;

    private View rootViewer;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_record);

        View rootLayout = findViewById(R.id.rootLayout);
        String savedColor = ColorUtils.getSavedBackgroundColor(this);
        rootLayout.setBackgroundColor(Color.parseColor(savedColor));

        selectedImage = findViewById(R.id.selectedImage);
        btnCloseImage = findViewById(R.id.btnRemoveImage);
        imageContainer = findViewById(R.id.imageContainer);

        editText = findViewById(R.id.voiceGuide);
        voiceWave = findViewById(R.id.voiceWaveGray);
        recordGuide = findViewById(R.id.recordGuide);

        RelativeLayout menuContainer = findViewById(R.id.menuContainer);
        rootViewer = findViewById(R.id.rootLayout);

        dbHelper = new MyDatabaseHelper(this);

        sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String seqFromIntent = getIntent().getStringExtra("seq");

        keyboardListener = () -> {
            Rect rect = new Rect();
            rootViewer.getWindowVisibleDisplayFrame(rect);
            int screenHeight = rootViewer.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // 키보드 올라옴
                menuContainer.setVisibility(View.GONE);
            }
        };

        rootViewer.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);

        if (seqFromIntent != null) {
            todaySeq = seqFromIntent;
            try {
                Date parsedDate = sdf.parse(seqFromIntent);
                selectedDate = parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException e) {
                selectedDate = LocalDate.now();
                todaySeq = sdf.format(new Date());
            }
        } else {
            String dateStr = getIntent().getStringExtra("selectedDate");
            selectedDate = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
            Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            todaySeq = sdf.format(date);
        }

        TextView dateText = findViewById(R.id.dateText);
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
        String displayText = String.format("%d\uC6D4 %d\uC77C %s", selectedDate.getMonthValue(), selectedDate.getDayOfMonth(), dayOfWeek);
        dateText.setText(displayText);


        String dbContent = "";
        Cursor cursor = dbHelper.getDiaryBySeq(todaySeq);
        if (cursor != null && cursor.moveToFirst()) {
            dbContent = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
            String dbImageUri = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_IMAGE_URI));
            if (selectedImageUri == null && dbImageUri != null) {
                selectedImageUri = Uri.parse(dbImageUri);
                selectedImage.setImageURI(selectedImageUri);
                imageContainer.setVisibility(View.VISIBLE);
            }
            cursor.close();
        }

// 2. Intent에서 넘어온 값만 사용



        // 2. Intent에서 넘어온 값만 사용
        String passedContent = getIntent().getStringExtra("existingContent");
        String passedImageUri = getIntent().getStringExtra("existingImageUri");

        if (!TextUtils.isEmpty(passedContent)) {
            editText.setText(passedContent);
        }

        if (!TextUtils.isEmpty(passedImageUri)) {
            selectedImageUri = Uri.parse(passedImageUri);
            selectedImage.setImageURI(selectedImageUri);
            imageContainer.setVisibility(View.VISIBLE);
        }


// 병합: DB내용 + 전달된 내용 (둘 다 있을 경우 이어붙이기)
        // 병합: DB내용 + 전달된 내용 (중복 방지)
        String mergedText;
        if (!TextUtils.isEmpty(dbContent) && !TextUtils.isEmpty(passedContent)) {
            if (dbContent.equals(passedContent)) {
                mergedText = dbContent; // 중복이면 하나만 사용
            } else {
                mergedText = dbContent + "\n" + passedContent; // 둘 다 다르면 이어붙이기
            }
        } else if (!TextUtils.isEmpty(passedContent)) {
            mergedText = passedContent;
        } else {
            mergedText = dbContent;
        }
        editText.setText(mergedText);


// 이미지 URI는 전달된 게 우선
        if (passedImageUri != null) {
            selectedImageUri = Uri.parse(passedImageUri);
            selectedImage.setImageURI(selectedImageUri);
            imageContainer.setVisibility(View.VISIBLE);
        }


        ImageButton btnToggleMenu = findViewById(R.id.btnToggleMenu);
        ImageButton btnPalette = findViewById(R.id.btnPalette);
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);
        LinearLayout voiceBottomSheet = findViewById(R.id.voiceBottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(voiceBottomSheet);
        View rootView = findViewById(android.R.id.content);

        ImageButton btnVoiceRecord = findViewById(R.id.btnVoiceRecord);
        Button btnDone = findViewById(R.id.btnDone);

        btnDone.setOnClickListener(v -> {
            Log.d("DEBUG", "\u2705 btnDone \uD074\uB9AD\uB428");

            String content = editText.getText().toString();
            String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : null;

            Matcher matcher = Pattern.compile("#(\\w+)").matcher(content);
            List<String> tags = new ArrayList<>();
            while (matcher.find()) {
                tags.add(matcher.group(1));
            }

            String tagsString = TextUtils.join(",", tags);
            String status = "in_progress";

            dbHelper.insertOrUpdateDiary(todaySeq, content, imageUriStr, tagsString, status);
            Log.d("DEBUG", "\u2705 DB \uC800\uC7A5 \uC644\uB8CC");

            if (ModelManager.getInstance().isModelReady()) {
                Log.d("DEBUG", "\u2705 \uBAA8\uB378 \uC900\uBE44\uB428");
                Toast.makeText(this, "\uD83E\uDDE0 AI \uBD84\uC11D \uC911\uC785\uB2C8\uB2E4...", Toast.LENGTH_SHORT).show();

                new Thread(() -> {
                    try {
                        String result = ModelManager.getInstance().inferWithPrompt(content);
                        Log.d("AI_RESULT", "\uD83E\uDDE0 \uBD84\uC11D \uACB0\uACFC:\n" + result);

                        ModelManager.getInstance().saveEmotionAnalysisToDB(this, todaySeq, result);
                        dbHelper.updateDiaryStatus(todaySeq, "done");
                        Log.d("DEBUG", "\u2705 \uC0C1\uD0DC \uC5C5\uB370\uC774\uD2B8 \uC644\uB8CC");
                    } catch (Exception e) {
                        Log.e("AI_ERROR", "\u274C \uBAA8\uB378 \uBD84\uC11D \uC2E4\uD328", e);
                    }
                }).start();

                Intent intent = new Intent(VoiceRecordActivity.this, HomeActivity.class);
                intent.putExtra("selectedDate", selectedDate.toString());
                startActivity(intent);
                finish();
            } else {
                Log.d("DEBUG", "\u26A0\uFE0F \uBAA8\uB378\uC774 \uC900\uBE44\uB418\uC9C0 \uC54A\uC74C");
                Toast.makeText(this, "\u26A0\uFE0F \uBAA8\uB378\uC774 \uC544\uC9C1 \uB85C\uB529\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCalendar.setOnClickListener(v -> {
            MiniCalendarAtWriteDiary dialog = new MiniCalendarAtWriteDiary();
            dialog.setOnDateSelectedListener(date -> {
                selectedDate = date;
                String dayOfWeek2 = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
                String displayText2 = String.format("%d월 %d일 %s", date.getMonthValue(), date.getDayOfMonth(), dayOfWeek2);
                dateText.setText(displayText2);
                Date dateForSeq = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                todaySeq = sdf.format(dateForSeq);

                Cursor newCursor = dbHelper.getDiaryBySeq(todaySeq);
                if (newCursor != null && newCursor.moveToFirst()) {
                    String existingContent = newCursor.getString(newCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
                    String existingImageUri = newCursor.getString(newCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_IMAGE_URI));
                    editText.setText(existingContent);

                    if (existingImageUri != null) {
                        selectedImageUri = Uri.parse(existingImageUri);
                        selectedImage.setImageURI(selectedImageUri);
                        imageContainer.setVisibility(View.VISIBLE);
                    } else {
                        selectedImage.setImageDrawable(null);
                        imageContainer.setVisibility(View.GONE);
                        selectedImageUri = null;
                    }
                    newCursor.close();
                } else {
                    editText.setText("");
                    selectedImage.setImageDrawable(null);
                    imageContainer.setVisibility(View.GONE);
                    selectedImageUri = null;
                }
            });
            dialog.show(getSupportFragmentManager(), "MiniCalendarAtWriteDiary");
        });
        findViewById(R.id.btnPen).setOnClickListener(v -> {
            String diaryText = editText.getText().toString();
            Intent intent = new Intent(this, WritingButtonActivity.class);
            intent.putExtra("voiceResult", diaryText);
            intent.putExtra("selectedDate", selectedDate.toString());

            // ✅ 이미지 URI 전달 추가
            if (selectedImageUri != null) {
                intent.putExtra("existingImageUri", selectedImageUri.toString());
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            menuContainer.setVisibility(View.GONE);
            isMenuOpen = false;
        });


        findViewById(R.id.btnMic).setOnClickListener(v -> {
            menuContainer.setVisibility(View.GONE);
            isMenuOpen = false;
        });

        findViewById(R.id.btnPhoto).setOnClickListener(v -> {
            requestGalleryPermission();
            menuContainer.setVisibility(View.GONE);
            isMenuOpen = false;
        });

        btnPalette.setOnClickListener(v -> {
            new ColorBottomSheet().show(getSupportFragmentManager(), null);
            menuContainer.setVisibility(View.GONE);
        });

        btnCloseImage.setOnClickListener(v -> {
            selectedImage.setImageDrawable(null);
            selectedImageUri = null;
            imageContainer.setVisibility(View.GONE);
        });

        btnToggleMenu.setOnClickListener(v -> {
            menuContainer.setVisibility(isMenuOpen ? View.GONE : View.VISIBLE);
            isMenuOpen = !isMenuOpen;
        });

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            btnToggleMenu.setVisibility(keypadHeight > screenHeight * 0.15 ? View.GONE : View.VISIBLE);
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight(60);
        bottomSheetBehavior.setHideable(false);

        voiceBottomSheet.post(() -> {
            int sheetHeight = voiceBottomSheet.getHeight();
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {}
                @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    editText.setPadding(0, 0, 0, (int) (sheetHeight * slideOffset));
                }
            });
        });

        btnVoiceRecord.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                startVoiceRecognition();
                recordGuide.setText("듣는 중...");
                voiceWave.setImageResource(R.drawable.ic_wave_green);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            initSpeechRecognizer();
        }

        if (WriteDiaryHomeActivity.isPhotoPending) {
            WriteDiaryHomeActivity.isPhotoPending = false;
            requestGalleryPermission();
        }
    }

    private void startVoiceRecognition() {
        if (speechHelper == null) {
            initSpeechRecognizer();
        }
        Toast.makeText(this, "🎤 음성 인식 시작", Toast.LENGTH_SHORT).show();
        speechHelper.startListening();
    }

    private void initSpeechRecognizer() {
        speechHelper = new SpeechRecognizerHelper(this, result -> {
            runOnUiThread(() -> {
                String currentText = editText.getText().toString();
                String updatedText = currentText + result + " ";
                editText.setText(updatedText);
                editText.setSelection(updatedText.length());
                recordGuide.setText("눌러서 말하세요");
                voiceWave.setImageResource(R.drawable.ic_wave_gray);
            });
        });
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_GALLERY_PERMISSION);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_GALLERY_PERMISSION);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechHelper != null) speechHelper.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "사진 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSpeechRecognizer();
                startVoiceRecognition();
            } else {
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageContainer.setVisibility(View.VISIBLE);
            selectedImage.setImageURI(selectedImageUri);
        }
    }
}