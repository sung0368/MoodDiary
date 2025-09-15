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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class WritingButtonActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY_PERMISSION = 1001;
    private static final int REQUEST_PICK_IMAGE = 1002;

    private boolean isMenuOpen = false;

    private ImageView selectedImage;
    private ImageButton btnCloseImage;
    private FrameLayout imageContainer;
    private EditText editContent;
    private Uri selectedImageUri;

    private String todaySeq;
    private LocalDate selectedDate;
    private SimpleDateFormat sdf;

    private View rootViewer;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writingbutton);

        ImageButton btnToggleMenu = findViewById(R.id.btnToggleMenu);
        View rootView = findViewById(android.R.id.content);
        View rootLayout = findViewById(R.id.rootLayout);

        String savedColor = ColorUtils.getSavedBackgroundColor(this);
        rootLayout.setBackgroundColor(Color.parseColor(savedColor));

        selectedImage = findViewById(R.id.selectedImage);
        btnCloseImage = findViewById(R.id.btnRemoveImage);
        imageContainer = findViewById(R.id.imageContainer);
        editContent = findViewById(R.id.editTextContent);
        Button btnDone = findViewById(R.id.btnDone);

        RelativeLayout menuContainer = findViewById(R.id.menuContainer);
        rootViewer = findViewById(R.id.rootLayout);

        ImageButton btnPalette = findViewById(R.id.btnPalette);
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);

        String seqFromIntent = getIntent().getStringExtra("seq");
        sdf = new SimpleDateFormat("yyMMdd", Locale.US);

        String passedContent = getIntent().getStringExtra("voiceResult");
        String passedImageUri = getIntent().getStringExtra("existingImageUri");

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

        MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        String dbContent = null;
        String dbImageUri = null;
        Cursor cursor = dbHelper.getDiaryBySeq(todaySeq);
        if (cursor != null && cursor.moveToFirst()) {
            dbContent = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
            dbImageUri = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_IMAGE_URI));
            cursor.close();
        }

        String finalText = "";
        if (!TextUtils.isEmpty(passedContent)) {
            finalText = passedContent;
        } else if (!TextUtils.isEmpty(dbContent)) {
            finalText = dbContent;
        }
        editContent.setText(finalText);

        if (!TextUtils.isEmpty(passedImageUri)) {
            selectedImageUri = Uri.parse(passedImageUri);
        } else if (!TextUtils.isEmpty(dbImageUri)) {
            selectedImageUri = Uri.parse(dbImageUri);
        }
        if (selectedImageUri != null) {
            selectedImage.setImageURI(selectedImageUri);
            imageContainer.setVisibility(View.VISIBLE);
        }

        if (WriteDiaryHomeActivity.isPhotoPending) {
            WriteDiaryHomeActivity.isPhotoPending = false;
            requestGalleryPermission();
        }

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            btnToggleMenu.setVisibility(keypadHeight > screenHeight * 0.15 ? View.GONE : View.VISIBLE);
        });

        btnToggleMenu.setOnClickListener(v -> {
            isMenuOpen = !isMenuOpen;
            menuContainer.setVisibility(isMenuOpen ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.btnPen).setOnClickListener(v -> {
            menuContainer.setVisibility(View.GONE);
            isMenuOpen = false;
        });

        findViewById(R.id.btnMic).setOnClickListener(v -> {
            DiaryDraftManager.getInstance().setDraftText(editContent.getText().toString());
            DiaryDraftManager.getInstance().setImageUri(selectedImageUri);
            Intent intent = new Intent(WritingButtonActivity.this, VoiceRecordActivity.class);
            intent.putExtra("selectedDate", selectedDate.toString());
            intent.putExtra("existingContent", editContent.getText().toString());
            if (selectedImageUri != null) {
                intent.putExtra("existingImageUri", selectedImageUri.toString());
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            menuContainer.setVisibility(View.GONE);
        });

        findViewById(R.id.btnPhoto).setOnClickListener(v -> {
            requestGalleryPermission();
            menuContainer.setVisibility(View.GONE);
        });

        btnPalette.setOnClickListener(v -> {
            new ColorBottomSheet().show(getSupportFragmentManager(), "colorSheet");
            menuContainer.setVisibility(View.GONE);
        });

        btnCloseImage.setOnClickListener(v -> {
            selectedImage.setImageDrawable(null);
            imageContainer.setVisibility(View.GONE);
            selectedImageUri = null;
        });

        btnDone.setOnClickListener(v -> {
            String content = editContent.getText().toString();
            String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : null;
            Matcher matcher = Pattern.compile("#(\\w+)").matcher(content);
            List<String> tags = new ArrayList<>();
            while (matcher.find()) {
                tags.add(matcher.group(1));
            }
            String tagsString = TextUtils.join(",", tags);
            dbHelper.insertOrUpdateDiary(todaySeq, content, imageUriStr, tagsString, "in_progress");

            if (ModelManager.getInstance().isModelReady()) {
                Toast.makeText(this, "\uD83E\uDDE0 AI 분석 중입니다...", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    try {
                        String result = ModelManager.getInstance().inferWithPrompt(content);
                        ModelManager.getInstance().saveEmotionAnalysisToDB(this, todaySeq, result);
                        dbHelper.updateDiaryStatus(todaySeq, "done");
                    } catch (Exception e) {
                        Log.e("AI_ERROR", "❌ 모델 분석 실패", e);
                    }
                }).start();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "⚠️ 모델이 아직 로딩되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCalendar.setOnClickListener(v -> {
            MiniCalendarAtWriteDiary dialog = new MiniCalendarAtWriteDiary();
            dialog.setOnDateSelectedListener(date -> {
                selectedDate = date;
                String displayText2 = String.format("%d월 %d일 %s", date.getMonthValue(), date.getDayOfMonth(), date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN));
                dateText.setText(displayText2);
                Date dateForSeq = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                todaySeq = sdf.format(dateForSeq);
                Cursor newCursor = dbHelper.getDiaryBySeq(todaySeq);
                if (newCursor != null && newCursor.moveToFirst()) {
                    String existingContent = newCursor.getString(newCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
                    String existingImageUri = newCursor.getString(newCursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_IMAGE_URI));
                    editContent.setText(existingContent);
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
                    editContent.setText("");
                    selectedImage.setImageDrawable(null);
                    imageContainer.setVisibility(View.GONE);
                    selectedImageUri = null;
                }
            });
            dialog.show(getSupportFragmentManager(), "MiniCalendarAtWriteDiary");
        });
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_GALLERY_PERMISSION);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            selectedImage.setImageURI(selectedImageUri);
            imageContainer.setVisibility(View.VISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_GALLERY_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "사진 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }
}   