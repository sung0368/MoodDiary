package com.cookandroid.moodiaryfinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private final List<DiaryEntry> diaryList;
    private final Context context;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onDeleteClick(DiaryEntry entry);
        void onEditClick(DiaryEntry entry);
        void onAnalysisClick(DiaryEntry entry);
    }

    public DiaryAdapter(Context context, List<DiaryEntry> diaryList, OnItemClickListener listener) {
        this.context = context;
        this.diaryList = diaryList;
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiaryMainText, tvDiaryDate;
        ImageButton btnMore;
        ImageView imgStatusCombined;
        RelativeLayout frontLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDiaryMainText = itemView.findViewById(R.id.tvDiaryMainText);
            tvDiaryDate = itemView.findViewById(R.id.tvDiaryDate);
            btnMore = itemView.findViewById(R.id.btnMore);
            imgStatusCombined = itemView.findViewById(R.id.imgStatusCombined);
            frontLayout = itemView.findViewById(R.id.frontLayout);
        }
    }

    @Override
    public DiaryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiaryAdapter.ViewHolder holder, int position) {
        DiaryEntry entry = diaryList.get(position);

        if (entry.tag != null && !entry.tag.trim().isEmpty()) {
            holder.tvDiaryMainText.setText("#" + entry.tag.replace(",", " #"));
        } else {
            holder.tvDiaryMainText.setText(entry.content.length() > 30 ?
                    entry.content.substring(0, 30) + "…" : entry.content);
        }

        if ("done".equals(entry.status)) {
            holder.imgStatusCombined.setImageResource(R.drawable.ic_status_done);
        } else {
            holder.imgStatusCombined.setImageResource(R.drawable.ic_status_in_progress);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 E요일", Locale.KOREAN);
        holder.tvDiaryDate.setText(sdf.format(entry.date));

        // 더보기 버튼 팝업
        holder.btnMore.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(context).inflate(R.layout.diary_item_popup, null);
            PopupWindow popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            TextView popupEdit = popupView.findViewById(R.id.popup_edit);
            TextView popupAnalysis = popupView.findViewById(R.id.popup_analysis);
            TextView popupDelete = popupView.findViewById(R.id.popup_delete);

            popupEdit.setOnClickListener(view -> {
                Intent editIntent = new Intent(context, WritingButtonActivity.class);
                editIntent.putExtra("seq", entry.seq);
                context.startActivity(editIntent);
                popupWindow.dismiss();
            });

            popupAnalysis.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onAnalysisClick(entry);
                } else {
                    Toast.makeText(context, "분석 기능은 홈 화면에서 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                }
                popupWindow.dismiss();
            });

            popupDelete.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onDeleteClick(entry);
                } else {
                    Toast.makeText(context, "삭제 기능은 홈 화면에서 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                }
                popupWindow.dismiss();
            });

            popupWindow.showAsDropDown(holder.btnMore, -50, 0);
        });

        // 카드 클릭 시 작성 화면으로 이동
        holder.frontLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, WritingButtonActivity.class);
            intent.putExtra("seq", entry.seq);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }
}
