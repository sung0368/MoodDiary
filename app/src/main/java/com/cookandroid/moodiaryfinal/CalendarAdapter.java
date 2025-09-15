package com.cookandroid.moodiaryfinal;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

// CalendarAdapter: RecyclerView를 통해 달력의 날짜들을 출력하는 어댑터 클래스
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private List<CalendarDay> days; // 날짜 + 감정 이미지 데이터 목록

    // 생성자: CalendarDay 리스트를 받아 어댑터에 저장
    public CalendarAdapter(List<CalendarDay> days) {
        this.days = days;
    }

    // ViewHolder를 생성할 때 호출됨 (레이아웃 inflate)
    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 각 날짜 아이템 뷰를 불러옴 (activity_calendar_item.xml 사용)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_calendar_item, parent, false);
        return new DayViewHolder(view);
    }

    // 콜백 인터페이스 추가
    public interface OnItemClickListener {
        void onItemClick(CalendarDay day);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder에 데이터를 바인딩할 때 호출됨
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        // 빈 칸 처리 (달력 앞 공백 등)
        if (day.day == 0) {
            holder.textDay.setText("");
            holder.imageEmotion.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
        } else {
            // 날짜 숫자 표시
            holder.textDay.setText(String.valueOf(day.day));

            int todayDay = LocalDate.now().getDayOfMonth();

            if (day.day == todayDay) {
                holder.textDay.setTextColor(Color.parseColor("#4FA477")); // 오늘
                holder.textDay.setTypeface(null, Typeface.BOLD);
            } else if (day.day < todayDay) {
                holder.textDay.setTextColor(Color.parseColor("#171D1B")); // 과거
                holder.textDay.setTypeface(null, Typeface.NORMAL);
            } else {
                holder.textDay.setTextColor(Color.parseColor("#B3B3B3")); // 미래
                holder.textDay.setTypeface(null, Typeface.NORMAL);
            }

            // 감정 이미지가 설정돼 있으면 보이게, 없으면 숨김
            if (day.emotionResId != 0) {
                holder.imageEmotion.setImageResource(day.emotionResId);
                holder.imageEmotion.setVisibility(View.VISIBLE);
            } else {
                holder.imageEmotion.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(day);
                }
            });
        }
    }

    // 아이템 개수 반환
    @Override
    public int getItemCount() {
        return days.size();
    }

    // ViewHolder: 하나의 날짜 아이템 뷰의 구성 요소를 보관
    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;         // 날짜 숫자 표시용
        ImageView imageEmotion;   // 감정 이모지 이미지

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            // itemView에서 ID로 뷰 연결
            textDay = itemView.findViewById(R.id.txt_day);
            imageEmotion = itemView.findViewById(R.id.imageEmotion);
        }
    }
}
