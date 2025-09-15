package com.cookandroid.moodiaryfinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.*;

public class HomeSearchActivity extends AppCompatActivity implements DiaryAdapter.OnItemClickListener {

    private SearchView searchView;
    private Button btnCancel;
    private TextView btnClear;
    private ListView listRecent;

    private SharedPreferences preferences;
    private static final String PREFS_NAME = "search_history";
    private static final String KEY_HISTORY = "keywords";

    private MyDatabaseHelper dbHelper;

    private RecyclerView recyclerSearchResults;
    private DiaryAdapter diaryAdapter;
    private List<DiaryEntry> filteredEntries;

    private ArrayList<String> recentSearchList;
    private ArrayAdapter<String> recentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_search);

        searchView = findViewById(R.id.search_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnClear = findViewById(R.id.btn_clear);
        listRecent = findViewById(R.id.list_recent);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dbHelper = new MyDatabaseHelper(this);

        recentSearchList = new ArrayList<>();
        recentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recentSearchList);
        listRecent.setAdapter(recentAdapter);
        loadSearchHistory();

        recyclerSearchResults = findViewById(R.id.recycler_search_result);
        filteredEntries = new ArrayList<>();
        diaryAdapter = new DiaryAdapter(this, filteredEntries, this);
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(diaryAdapter);

        // 처음 들어왔을 때는 검색 결과 숨김
        recyclerSearchResults.setVisibility(View.GONE);
        listRecent.setVisibility(View.VISIBLE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    saveSearchHistory(query.trim());
                    performSearch(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // ✅ 클리어 버튼 눌렀을 때: 검색 결과 초기화, 리스트 복원
                    filteredEntries.clear();
                    diaryAdapter.notifyDataSetChanged();
                    recyclerSearchResults.setVisibility(View.GONE);
                    listRecent.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });


        listRecent.setOnItemClickListener((parent, view, position, id) -> {
            String selectedKeyword = recentSearchList.get(position);
            searchView.setQuery(selectedKeyword, false); // 검색바에 텍스트 설정 (submit 안 됨)
            performSearch(selectedKeyword); // 명시적으로 검색 수행
        });

        btnClear.setOnClickListener(v -> {
            preferences.edit().remove(KEY_HISTORY).apply();
            recentSearchList.clear();
            recentAdapter.notifyDataSetChanged();
            Toast.makeText(this, "최근 검색어가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveSearchHistory(String query) {
        String history = preferences.getString(KEY_HISTORY, "");
        List<String> keywords = new ArrayList<>(Arrays.asList(history.split(";")));

        keywords.remove(query); // 중복 제거
        keywords.add(0, query);

        if (keywords.size() > 10) {
            keywords = keywords.subList(0, 10);
        }

        preferences.edit().putString(KEY_HISTORY, String.join(";", keywords)).apply();
        loadSearchHistory();
    }

    private void loadSearchHistory() {
        String history = preferences.getString(KEY_HISTORY, "");
        recentSearchList.clear();

        if (!history.isEmpty()) {
            String[] keywords = history.split(";");
            for (String keyword : keywords) {
                if (!keyword.trim().isEmpty()) {
                    recentSearchList.add(keyword);
                }
            }
        }

        recentAdapter.notifyDataSetChanged();
    }

    private void performSearch(String keyword) {
        filteredEntries.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MyDatabaseHelper.TABLE_NAME + " WHERE " +
                MyDatabaseHelper.COLUMN_CONTENT + " LIKE ? OR " +
                MyDatabaseHelper.COLUMN_TAG + " LIKE ?", new String[]{"%" + keyword + "%", "%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                String seq = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_SEQ));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_CONTENT));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_TAG));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_STATUS));

                try {
                    Date date = new SimpleDateFormat("yyMMdd", Locale.getDefault()).parse(seq);
                    filteredEntries.add(new DiaryEntry(seq, content, tag, date, status));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        diaryAdapter.notifyDataSetChanged();
        listRecent.setVisibility(View.GONE);
        recyclerSearchResults.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeleteClick(DiaryEntry entry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete(MyDatabaseHelper.TABLE_NAME, MyDatabaseHelper.COLUMN_SEQ + "=?", new String[]{entry.seq});
        db.close();

        if (deletedRows > 0) {
            filteredEntries.remove(entry);
            diaryAdapter.notifyDataSetChanged();
            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(DiaryEntry entry) {
        // 필요 시 구현
    }

    @Override
    public void onAnalysisClick(DiaryEntry entry) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("selected_date", entry.seq);
        startActivity(intent);
    }
}
