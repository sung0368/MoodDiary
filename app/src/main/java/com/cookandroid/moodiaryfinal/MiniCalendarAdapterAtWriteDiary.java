package com.cookandroid.moodiaryfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

public class MiniCalendarAdapterAtWriteDiary extends RecyclerView.Adapter<MiniCalendarAdapterAtWriteDiary.DateViewHolder> {
    private List<LocalDate> dateList;
    private MiniCalendarAtWriteDiary.OnDateSelectedListener listener;

    public MiniCalendarAdapterAtWriteDiary(List<LocalDate> dateList, MiniCalendarAtWriteDiary.OnDateSelectedListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MiniCalendarAdapterAtWriteDiary.DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_mini_calendar_item, parent, false);
        return new MiniCalendarAdapterAtWriteDiary.DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniCalendarAdapterAtWriteDiary.DateViewHolder holder, int position) {
        LocalDate date = dateList.get(position);
        if (date == null) {
            holder.txtDay.setText("");
            holder.itemView.setOnClickListener(null);
        } else {
            holder.txtDay.setText(String.valueOf(date.getDayOfMonth()));
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
