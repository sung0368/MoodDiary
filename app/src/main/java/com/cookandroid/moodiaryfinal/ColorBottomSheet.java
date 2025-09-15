package com.cookandroid.moodiaryfinal;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ColorBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_color, container, false);

        ImageButton btnRed = view.findViewById(R.id.btnRed);
        ImageButton btnYellow = view.findViewById(R.id.btnYellow);
        ImageButton btnBlue = view.findViewById(R.id.btnBlue);
        ImageButton btnGreen = view.findViewById(R.id.btnGreen);
        ImageButton btnPurple = view.findViewById(R.id.btnPurple);
        ImageButton btnWhite = view.findViewById(R.id.btnWhite);

        btnRed.setOnClickListener(v -> changeBackground("#FFCDD2"));
        btnBlue.setOnClickListener(v -> changeBackground("#BBDEFB"));
        btnGreen.setOnClickListener(v -> changeBackground("#C8E6C9"));
        btnYellow.setOnClickListener(v -> changeBackground("#FFF9C4"));
        btnPurple.setOnClickListener(v -> changeBackground("#E1BEE7"));
        btnWhite.setOnClickListener(v -> changeBackground("#FFFFFF"));

        return view;
    }

    private void changeBackground(String colorHex) {
        Activity activity = getActivity();
        if (activity != null) {
            View rootLayout = activity.findViewById(R.id.rootLayout);
            if (rootLayout != null) {
                rootLayout.setBackgroundColor(Color.parseColor(colorHex));
                ColorUtils.saveBackgroundColor(activity, colorHex); // 색상 저장
                dismiss();
            }
        }
    }

}
