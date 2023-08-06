package com.example.thoms.expenditurestracking;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Categories {

    private static Map<Category, TextView> categoryRadioButtons;

    public static void initializeCategories(Context context, LinearLayout linearLayout) {
        categoryRadioButtons = new HashMap<>();

        for (Category category: Category.toArray()) {
            TextView radioButton = new TextView(context);
            categoryRadioButtons.put(category, radioButton);

            Styling.applyCategoryRadioButtonStyling(context, radioButton, Styling.ClickStatus.DEFAULT);
            radioButton.setText(category.name);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.categorySelected = category;
                    for (Map.Entry<Category, TextView> mapEntry: categoryRadioButtons.entrySet()) {
                        Styling.applyCategoryRadioButtonStyling(context, mapEntry.getValue(),
                                mapEntry.getKey().equals(category) ? Styling.ClickStatus.CLICKED : Styling.ClickStatus.DEFAULT);
                    }
                }
            });
            linearLayout.addView(radioButton);
        }
    }

    enum Category {
        NONE(-1, null, -1),
        GROCERIES(0, "Groceries", Color.parseColor("#138086")),
        CLOTHING(1, "Clothing", Color.parseColor("#534666")),
        SPORT_LEISURE(2, "Sports & Leisure", Color.parseColor("#56C596")),
        HOLIDAY(3, "Holiday", Color.parseColor("#F9C449")),
        BANK_INSURANCE(4, "Bank & Insurance", Color.parseColor("#A7D676")),
        HOUSING(5, "Housing", Color.parseColor("#35BBCA")),
        MOVABLES(6, "Movables", Color.parseColor("#FD8F52")),
        UTILITIES(7, "Utilities", Color.parseColor("#FF9CDA")),
        TRANSPORTATION(8, "Transportation", Color.parseColor("#EA4492")),
        MEDICAL(9, "Medical", Color.parseColor("#264D59")),
        GIFTS(10, "Gifts", Color.parseColor("#C6A477")),
        MISCELANEOUS(11, "Miscelaneous", Color.parseColor("#7E9680"));

        private final int idx;
        public final String name;
        public final int color;

        Category(int idx, String name, int color) {
            this.idx = idx;
            this.name = name;
            this.color = color;
        }

        public static Category getCategory(int idx) {
            for (Category category : Category.values()) {
                if (category.idx == idx) {
                    return category;
                }
            }
            return null;
        }

        public static Category[] toArray() {
            Category[] array = new Category[Category.values().length - 1];
            for (Category category : Category.values()) {
                if (category.equals(Category.NONE)) {
                    continue;
                }
                array[category.idx] = category;
            }
            return array;
        }
    }
}

