// MiniCalendarDialogFragment.java
package com.cookandroid.moodiaryfinal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MiniCalendarDialog extends DialogFragment {

    private RecyclerView recyclerView;
    private TextView txtMonth;
    private ImageButton btnPrev, btnNext;
    private LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_mini_calendar, container, false);

        recyclerView = view.findViewById(R.id.recyclerCalendar);
        txtMonth = view.findViewById(R.id.txtMonthYear);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));

        btnPrev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });

        updateCalendar();
        return view;
    }

    private void updateCalendar() {
        txtMonth.setText(currentMonth.getYear() + "년 " + currentMonth.getMonthValue() + "월");
        List<LocalDate> dateList = new ArrayList<>();

        int firstDayOfWeek = currentMonth.getDayOfWeek().getValue();
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int i = 1; i < firstDayOfWeek; i++) {
            dateList.add(null);
        }

        for (int i = 1; i <= daysInMonth; i++) {
            dateList.add(currentMonth.withDayOfMonth(i));
        }

        recyclerView.setAdapter(new MiniCalendarAdapter(dateList, selectedDate -> {
            if (listener != null) listener.onDateSelected(selectedDate);
            dismiss(); // ✅ 선택 시 팝업 닫기
        }));

    }
}
