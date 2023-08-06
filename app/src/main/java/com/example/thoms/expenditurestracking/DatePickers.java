package com.example.thoms.expenditurestracking;


import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Calendar;

public class DatePickers {

    private static final int YEAR_MIN = 2000;
    private static final int YEAR_MAX = 2050;

    private static NumberPicker numberpicker_year;
    private static NumberPicker numberpicker_month;
    private static NumberPicker numberpicker_day;

    private static NumberPicker numberpicker_endyear;
    private static NumberPicker numberpicker_endmonth;
    private static NumberPicker numberpicker_endday;
    private static TextView textview_enddateselect;
    private static RadioGroup radiogroup_periodselect;
    private static RadioButton radiobutton_monthlyperiods;
    private static RadioButton radiobutton_weeklyperiods;

    public static int period_selected = -1;

    public static void createDatePickers(final NumberPicker yearpicker
            , final NumberPicker monthpicker, final NumberPicker daypicker) {
        Calendar rightnow = Calendar.getInstance();

        numberpicker_year = yearpicker;
        numberpicker_month = monthpicker;
        numberpicker_day = daypicker;

        numberpicker_year.setMinValue(YEAR_MIN);
        numberpicker_year.setMaxValue(YEAR_MAX);
        numberpicker_year.setValue(rightnow.get(Calendar.YEAR));

        numberpicker_month.setMinValue(1);
        numberpicker_month.setMaxValue(12);
        numberpicker_month.setDisplayedValues(DataManagement.Months.toStringArray());
        numberpicker_month.setValue(rightnow.get(Calendar.MONTH) + 1);
        numberpicker_month.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                configureDayPicker(numberpicker_year.getValue(), i1, numberpicker_day);

                if (i1 == 1 && i != 2) changeYear(1, numberpicker_year, YEAR_MIN, YEAR_MAX);
                else if (i1 != 2 && i == 1) changeYear(-1, numberpicker_year, YEAR_MIN, YEAR_MAX);
            }
        });

        numberpicker_day.setMinValue(1);

        numberpicker_day.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (i1 == 1 && i != 2) changeMonth(1, numberpicker_year, numberpicker_month
                        , numberpicker_day);
                else if (i1 != 2 && i == 1) changeMonth(-1, numberpicker_year
                        , numberpicker_month, numberpicker_day);
            }
        });

        configureDayPicker(numberpicker_year.getValue(), numberpicker_month.getValue(), numberpicker_day);
        numberpicker_day.setValue(rightnow.get(Calendar.DAY_OF_MONTH));
    }

    public static void createPeriodicSelect(final NumberPicker yearpicker
            , final NumberPicker monthpicker, final NumberPicker daypicker
            , final CheckBox periodically, final RadioGroup periodselect
            , final RadioButton monthlyperiods, final RadioButton weeklyperiods
            , final TextView enddateselect) {
        radiogroup_periodselect = periodselect;
        radiobutton_monthlyperiods = monthlyperiods;
        radiobutton_weeklyperiods = weeklyperiods;

        textview_enddateselect = enddateselect;
        numberpicker_endyear = yearpicker;
        numberpicker_endmonth = monthpicker;
        numberpicker_endday = daypicker;

        numberpicker_endyear.setMinValue(YEAR_MIN);
        numberpicker_endyear.setMaxValue(YEAR_MAX);

        numberpicker_endmonth.setMinValue(1);
        numberpicker_endmonth.setMaxValue(12);
        numberpicker_endmonth.setDisplayedValues(DataManagement.Months.toStringArray());

        numberpicker_endmonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                configureDayPicker(numberpicker_endyear.getValue(), i1, numberpicker_endday);

                if (i1 == 1 && i != 2) changeYear(1, numberpicker_endyear, YEAR_MIN, YEAR_MAX);
                else if (i1 != 2 && i == 1) changeYear(-1, numberpicker_endyear, YEAR_MIN, YEAR_MAX);
            }
        });

        numberpicker_endday.setMinValue(1);

        numberpicker_endday.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (i1 == 1 && i != 2) changeMonth(1, numberpicker_endyear
                        , numberpicker_endmonth, numberpicker_endday);
                else if (i1 != 2 && i == 1) changeMonth(-1, numberpicker_endyear
                        , numberpicker_endmonth, numberpicker_endday);
            }
        });

        configureDayPicker(numberpicker_endyear.getValue(), numberpicker_endmonth.getValue(), numberpicker_endday);

        periodically.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    radiobutton_monthlyperiods.setChecked(false);
                    radiobutton_weeklyperiods.setChecked(false);
                    period_selected = -1;
                    radiogroup_periodselect.setVisibility(View.VISIBLE);
                } else {
                    hidePeriodicallyParts();
                }
            }
        });

        periodselect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                period_selected = radioGroup.getCheckedRadioButtonId();

                numberpicker_endyear.setValue(numberpicker_year.getValue());
                numberpicker_endmonth.setValue(numberpicker_month.getValue());
                numberpicker_endday.setValue(numberpicker_day.getValue());

                configureDayPicker(numberpicker_endyear.getValue(), numberpicker_endmonth.getValue(), numberpicker_endday);

                textview_enddateselect.setVisibility(View.VISIBLE);
                numberpicker_endmonth.setVisibility(View.VISIBLE);
                numberpicker_endyear.setVisibility(View.VISIBLE);
                numberpicker_endday.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void hidePeriodicallyParts()
    {
        radiobutton_monthlyperiods.setChecked(false);
        radiobutton_weeklyperiods.setChecked(false);
        period_selected = -1;
        radiogroup_periodselect.setVisibility(View.INVISIBLE);
        textview_enddateselect.setVisibility(View.INVISIBLE);
        numberpicker_endmonth.setVisibility(View.INVISIBLE);
        numberpicker_endyear.setVisibility(View.INVISIBLE);
        numberpicker_endday.setVisibility(View.INVISIBLE);
    }

    public static void changeYear(int dif, NumberPicker yearpicker, int year_min, int year_max)
    {
        int year = yearpicker.getValue();

        if(year + dif <= year_max && year + dif >= year_min) yearpicker.setValue(year + dif);
    }

    private static void changeMonth( int dif, NumberPicker yearpicker, NumberPicker monthpicker
        , NumberPicker daypicker )
    {
        int month = monthpicker.getValue();
        int newmonth = month + dif;

        if(newmonth > 12)
        {
            monthpicker.setValue(1);
            changeYear(1, yearpicker, YEAR_MIN, YEAR_MAX);
        }
        else if(newmonth < 1)
        {
            monthpicker.setValue(12);
            changeYear(-1, yearpicker, YEAR_MIN, YEAR_MAX);
        }
        else monthpicker.setValue(newmonth);

        configureDayPicker(yearpicker.getValue(), monthpicker.getValue(), daypicker);
    }

    private static void configureDayPicker(int year, int monthInt, NumberPicker daypicker)
    {
        daypicker.setMaxValue(noDaysInMonth(Month.findMonth(monthInt), year));
    }

    static int noDaysInMonth(Month month, int year) {
        switch (month) {
            case JANUARY: return 31;
            case FEBRUARY: return isSchrikkelJaar(year) ? 29 : 28;
            case MARCH: return 31;
            case APRIL: return 30;
            case MAY: return 31;
            case JUNE: return 30;
            case JULY: return 31;
            case AUGUST: return 31;
            case SEPTEMBER: return 30;
            case OCTOBER: return 31;
            case NOVEMBER: return 30;
            case DECEMBER: return 31;
            default: return -1;
        }
    }

    static boolean isSchrikkelJaar(int year) {
        if (year % 400 == 0) {
            return true;
        }
        if (year % 100 == 0) {
            return false;
        }
        return year % 4 == 0;
    }

    enum Month {
        JANUARY(1), FEBRUARY(2), MARCH(3), APRIL(4), MAY(5), JUNE(6), JULY(7), AUGUST(8),
        SEPTEMBER(9), OCTOBER(10), NOVEMBER(11), DECEMBER(12);

        private final int monthOfYear;

        Month(int monthOfYear) {
            this.monthOfYear = monthOfYear;
        }

        int getMonthOfYear() {
            return monthOfYear;
        }

        static Month findMonth(int monthInt) {
            for (Month m : Month.values()) {
                if (m.getMonthOfYear() == monthInt) {
                    return m;
                }
            }
            return null;
        }
    }
}


