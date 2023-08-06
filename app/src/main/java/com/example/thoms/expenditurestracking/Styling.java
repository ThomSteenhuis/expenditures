package com.example.thoms.expenditurestracking;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Styling {

    public static final String primaryColor = "#3F51B5";

    public static final String secondaryColor = "#E8E8E8";

    public static final String whiteColor = "#FFFFFF";

    public static void applyButtonStyling(Button button) {
        button.setTextColor(Color.parseColor(whiteColor));
        button.setTextSize(11);
        button.setBackgroundColor(Color.parseColor(primaryColor));
    }

    public static void applyCategoryRadioButtonStyling(Context context, TextView radioButton, ClickStatus clickStatus) {
        applyCategoryRadioButtonStyling(context, radioButton);
        applyRadioButtonStyling(radioButton, clickStatus);
    }

    public static void applySummaryRadioButtonStyling(Context context, TextView radioButton, ClickStatus clickStatus) {
        applySummaryRadioButtonStyling(context, radioButton);
        applyRadioButtonStyling(radioButton, clickStatus);
    }

    private static void applyRadioButtonStyling(TextView radioButton, ClickStatus clickStatus) {
        radioButton.setTextColor(Color.parseColor(clickStatus.equals(ClickStatus.DEFAULT) ? whiteColor : primaryColor));
        GradientDrawable background =  new GradientDrawable();
        background.setCornerRadius(radioButton.getLayoutParams().height / 2);
        background.setColor(Color.parseColor(clickStatus.equals(ClickStatus.DEFAULT) ? primaryColor : secondaryColor));
        radioButton.setBackground(background);
    }

    private static void applyCategoryRadioButtonStyling(Context context, TextView radioButton) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                convertDpsToPixels(context, 150), convertDpsToPixels(context, 26));
        lp.setMargins(convertDpsToPixels(context, 5), convertDpsToPixels(context, 7),
                0, convertDpsToPixels(context, 7));
        radioButton.setLayoutParams(lp);
        radioButton.setGravity(Gravity.CENTER);
        radioButton.setClickable(true);
    }

    private static void applySummaryRadioButtonStyling(Context context, TextView radioButton) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                convertDpsToPixels(context, 80), convertDpsToPixels(context, 30));
        lp.setMargins(convertDpsToPixels(context, 6), 0, convertDpsToPixels(context, 6), 0);
        radioButton.setLayoutParams(lp);
        radioButton.setGravity(Gravity.CENTER);
        radioButton.setClickable(true);
    }

    private static int convertDpsToPixels(Context context, float dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    enum ClickStatus {
        DEFAULT, CLICKED;
    }
}
