// MiniCalendarAdapter.java
package com.cookandroid.moodiaryfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

public class MiniCalendarAdapter extends RecyclerView.Adapter<MiniCalendarAdapter.DateViewHolder> {

    private List<LocalDate> dateList;
    private MiniCalendarDialog.OnDateSelectedListener listener;

    public MiniCalendarAdapter(List<LocalDate> dateList, MiniCalendarDialog.OnDateSelectedListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_mini_calendar_item, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        LocalDate date = dateList.get(position);
        if (date == null) {
            holder.txtDay.setText("");
            holder.itemView.setOnClickListener(null);
        } else {
            holder.txtDay.setText(String.valueOf(date.getDayOfMonth()));

            // 날짜 기준 스타일 처리
            LocalDate today = LocalDate.now();

            if (date.equals(today)) {
                holder.txtDay.setTextColor(android.graphics.Color.parseColor("#4FA477")); // 오늘
                holder.txtDay.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.txtDay.setTextColor(android.graphics.Color.parseColor("#B3B3B3")); // 미래
                holder.txtDay.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDateSelected(date);
            });

        }
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView txtDay;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDay = itemView.findViewById(R.id.txt_day);
        }
    }
}
