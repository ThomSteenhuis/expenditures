package com.example.thoms.expenditurestracking;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class ShowSummary extends AppCompatActivity {

    private List<DataManagement.DataEntry> dataEntries;

    private int maxYear;

    private int minYear;

    private Calendar rightNow;

    DataManagement.Periodicity periodicity;

    Graphic graphic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_summary);

        dataEntries = DataManagement.readAndParseFromFile(getBaseContext().getFileStreamPath(DataManagement.FILE_NAME), getApplicationContext());
        rightNow = Calendar.getInstance();

        int rightNowYear = rightNow.get(Calendar.YEAR);
        int min = DataManagement.minYear(dataEntries);
        int max = DataManagement.maxYear(dataEntries);

        if (max == -1 || max < rightNowYear) {
            maxYear = rightNowYear;
        } else {
            maxYear = max;
        }

        if (min == -1 || min > rightNowYear) {
            minYear = rightNowYear;
        } else {
            minYear = min;
        }

        configurePeriodSelect();
        configureDatePickers();
        configureGraphicSelect();
        configureBackButton();

        redraw();
    }

    private void configureDatePickers() {

        NumberPicker picker_month = findViewById(R.id.numberpicker_month);
        NumberPicker picker_quarter = findViewById(R.id.numberpicker_quarter);
        NumberPicker picker_year = findViewById(R.id.numberpicker_year);

        picker_month.setMaxValue(12);
        picker_month.setMinValue(1);
        picker_month.setDisplayedValues(DataManagement.Months.toStringArray());

        picker_quarter.setMaxValue(4);
        picker_quarter.setMinValue(1);
        picker_quarter.setDisplayedValues(DataManagement.Quarters.toStringArray());

        picker_year.setMaxValue(maxYear);
        picker_year.setMinValue(minYear);

        resetDatePickers();
    }

    private void resetDatePickers() {

        NumberPicker picker_month = findViewById(R.id.numberpicker_month);
        NumberPicker picker_quarter = findViewById(R.id.numberpicker_quarter);
        NumberPicker picker_year = findViewById(R.id.numberpicker_year);

        switch(periodicity) {
            case YEARLY: {
                picker_month.setVisibility(View.GONE);
                picker_quarter.setVisibility(View.GONE);
                break;
            }
            case QUARTERLY: {
                picker_month.setVisibility(View.GONE);
                picker_quarter.setVisibility(View.VISIBLE);
                break;
            }
            case MONTHLY: {
                picker_month.setVisibility(View.VISIBLE);
                picker_quarter.setVisibility(View.GONE);
                break;
            }
        }

        picker_month.setValue(rightNow.get(Calendar.MONTH) + 1);
        picker_quarter.setValue((rightNow.get(Calendar.MONTH)) / 3 + 1);
        picker_year.setValue(rightNow.get(Calendar.YEAR));

        picker_month.setOnValueChangedListener(numberPickerChangeValueListener());
        picker_quarter.setOnValueChangedListener(numberPickerChangeValueListener());
        picker_year.setOnValueChangedListener(numberPickerChangeValueListener());
    }

    private NumberPicker.OnValueChangeListener numberPickerChangeValueListener() {

        NumberPicker.OnValueChangeListener listener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                graphic = Graphic.NONE;
                setClickStatusGraphicButtons();
                redraw();

                NumberPicker picker_year = findViewById(R.id.numberpicker_year);

                if (i1 == 1 && i != 2) {
                    DatePickers.changeYear(1, picker_year, minYear, maxYear);
                }
                else if (i1 != 2 && i == 1) {
                    DatePickers.changeYear(-1, picker_year, minYear, maxYear);
                }
            }
        };

        return listener;
    }

    private void configurePeriodSelect() {

        TextView periodicityMonthly = (TextView) findViewById(R.id.periodicity_monthly);
        TextView periodicityQuarterly = (TextView) findViewById(R.id.periodicity_quarterly);
        TextView periodicityYearly = (TextView) findViewById(R.id.periodicity_yearly);

        periodicityMonthly.setOnClickListener(periodicyClickListener());
        periodicityQuarterly.setOnClickListener(periodicyClickListener());
        periodicityYearly.setOnClickListener(periodicyClickListener());

        // Set monthly view by default
        periodicity = DataManagement.Periodicity.MONTHLY;
        setClickStatusPeriodicityButtons();
    }

    private View.OnClickListener periodicyClickListener() {

        View.OnClickListener listener = new View.OnClickListener() {

            TextView periodicityMonthly = (TextView) findViewById(R.id.periodicity_monthly);
            TextView periodicityQuarterly = (TextView) findViewById(R.id.periodicity_quarterly);
            TextView periodicityYearly = (TextView) findViewById(R.id.periodicity_yearly);

            @Override
            public void onClick(View view) {
                int clickedId = view.getId();

                if (clickedId == periodicityMonthly.getId()) {
                    periodicity = DataManagement.Periodicity.MONTHLY;
                } else if(clickedId == periodicityQuarterly.getId()) {
                    periodicity = DataManagement.Periodicity.QUARTERLY;
                }else if(clickedId == periodicityYearly.getId()) {
                    periodicity = DataManagement.Periodicity.YEARLY;
                }

                setClickStatusPeriodicityButtons();
                configureDatePickers();
                redraw();
            }
        };

        return listener;
    }

    private void redraw() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.content);
        linearLayout.removeAllViews();

        if (graphic.equals(Graphic.TABLE)) {
            redrawTable();
        } else if (graphic.equals(Graphic.PIE)) {
            redrawPie();
        }
    }

    private void redrawPie() {

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.content);
        DataManagement.DataPeriod dataPeriod = getCurrentDataPeriod();

        PieChartView pieChartView = new PieChartView(getApplicationContext());
        pieChartView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(pieChartView);

        List<SliceValue> pieData = new ArrayList<>();
        double total = DataManagement.calculateTotal(dataEntries, dataPeriod);

        for (Categories.Category category: Categories.Category.toArray()) {
            double subtotal = DataManagement.calculateTotal(dataEntries, dataPeriod, category.name);
            double percentage = total > 0.0 ? 100 * subtotal / total : 0.0;

            pieData.add(new SliceValue((float) subtotal, category.color).
                    setLabel(String.format("%s : %.0f (%.0f%%)", category.name, subtotal, percentage)));
        }

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(8);
        pieChartData.setHasCenterCircle(true).setCenterText1(dataPeriod.getNiceName()).setCenterText1FontSize(20);
        pieChartView.setPieChartData(pieChartData);
    }

    private void redrawTable() {

        DataManagement.DataPeriod dataPeriod = getCurrentDataPeriod();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.content);

        float[] scales = new float[]{(float) 0.3, (float) 0.18, (float) 0.13, (float) 0.13,
                (float) 0.13, (float) 0.13};
        linearLayout.addView(createHeaderRow(dataPeriod, scales));

        // Percentage
        double percentage = DataManagement.calculatePercentageOfPeriod(Calendar.getInstance(), dataPeriod);

        // Categories
        for (Categories.Category category: Categories.Category.toArray()) {
            linearLayout.addView(createDataRow(category.name, percentage, dataPeriod, scales));
        }

        // Totals
        linearLayout.addView(createTotalsRow( percentage, dataPeriod, scales));
    }

    private DataManagement.DataPeriod getCurrentDataPeriod() {
        NumberPicker picker_month = findViewById(R.id.numberpicker_month);
        NumberPicker picker_quarter = findViewById(R.id.numberpicker_quarter);
        NumberPicker picker_year = findViewById(R.id.numberpicker_year);

        if (periodicity.equals(DataManagement.Periodicity.QUARTERLY)) {
            return new DataManagement.DataPeriod(periodicity, picker_year.getValue(), picker_quarter.getValue());
        } else {
            return new DataManagement.DataPeriod(periodicity, picker_year.getValue(), picker_month.getValue());
        }
    }

    private void setClickStatusPeriodicityButtons() {
        TextView periodicityMonthly = (TextView) findViewById(R.id.periodicity_monthly);
        TextView periodicityQuarterly = (TextView) findViewById(R.id.periodicity_quarterly);
        TextView periodicityYearly = (TextView) findViewById(R.id.periodicity_yearly);

        if (periodicity.equals(DataManagement.Periodicity.MONTHLY)) {
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityMonthly, Styling.ClickStatus.CLICKED);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityQuarterly, Styling.ClickStatus.DEFAULT);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityYearly, Styling.ClickStatus.DEFAULT);
        } else if (periodicity.equals(DataManagement.Periodicity.QUARTERLY)) {
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityMonthly, Styling.ClickStatus.DEFAULT);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityQuarterly, Styling.ClickStatus.CLICKED);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityYearly, Styling.ClickStatus.DEFAULT);
        } else if (periodicity.equals(DataManagement.Periodicity.YEARLY)) {
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityMonthly, Styling.ClickStatus.DEFAULT);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityQuarterly, Styling.ClickStatus.DEFAULT);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), periodicityYearly, Styling.ClickStatus.CLICKED);
        }
    }

    private void setClickStatusGraphicButtons() {
        TextView tableView = (TextView) findViewById(R.id.table_view);
        TextView pieView = (TextView) findViewById(R.id.pie_view);

        if (graphic.equals(Graphic.TABLE)) {
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), tableView, Styling.ClickStatus.CLICKED);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), pieView, Styling.ClickStatus.DEFAULT);
        } else if (graphic.equals(Graphic.PIE)) {
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), tableView, Styling.ClickStatus.DEFAULT);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), pieView, Styling.ClickStatus.CLICKED);
        } else if (graphic.equals(Graphic.NONE)) {
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), tableView, Styling.ClickStatus.DEFAULT);
            Styling.applySummaryRadioButtonStyling(getApplicationContext(), pieView, Styling.ClickStatus.DEFAULT);
        }
    }

    private void configureGraphicSelect() {
        TextView tableView = (TextView) findViewById(R.id.table_view);
        TextView pieView = (TextView) findViewById(R.id.pie_view);

        graphic = Graphic.TABLE;
        setClickStatusGraphicButtons();

        tableView.setOnClickListener(viewClickListener());
        pieView.setOnClickListener(viewClickListener());
    }

    private View.OnClickListener viewClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {

            TextView tableView = (TextView) findViewById(R.id.table_view);
            TextView pieView = (TextView) findViewById(R.id.pie_view);

            @Override
            public void onClick(View view) {
                int clickedId = view.getId();

                if (clickedId == tableView.getId()) {
                    graphic = Graphic.TABLE;
                } else if(clickedId == pieView.getId()) {
                    graphic = Graphic.PIE;
                }

                setClickStatusGraphicButtons();
                redraw();
            }
        };

        return listener;
    }

    private void configureBackButton() {
        Button backButton = (Button) findViewById(R.id.button_back);
        Styling.applyButtonStyling(backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowSummary.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private LinearLayout createHeaderRow(DataManagement.DataPeriod dataPeriod, float[] scales) {
        LinearLayout headerRow = initializeTableRow();

        headerRow.addView(createTextView("Category", Typeface.BOLD, scales[0], Color.BLACK));
        headerRow.addView(createTextView(dataPeriod.getName(), Typeface.BOLD, scales[1], Color.BLACK));
        headerRow.addView(createTextView(dataPeriod.previousPeriod().getName(), Typeface.BOLD,
                scales[2] + scales[3], Color.BLACK));

        if (!dataPeriod.getPeriod().equals(DataManagement.Periodicity.YEARLY)) {
            // If yearly, there would otherwise be two columns with the previous year
            headerRow.addView(createTextView(dataPeriod.previousYear().getName(), Typeface.BOLD,
                    scales[4] + scales[5], Color.BLACK));
        }

        return headerRow;
    }

    private LinearLayout initializeTableRow() {
        LinearLayout row = new LinearLayout(getApplicationContext());
        row.setWeightSum(1);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private LinearLayout createDataRow(String category, double percentage, DataManagement.DataPeriod dataPeriod, float[] scales) {
        double totalThisPeriod = DataManagement.calculateTotal(dataEntries, dataPeriod, category);
        double totalPreviousPeriod = DataManagement.calculateTotal(dataEntries, dataPeriod.previousPeriod(), category);

        if (dataPeriod.getPeriod().equals(DataManagement.Periodicity.YEARLY)) {
            return createRow(category, scales, percentage, totalThisPeriod, totalPreviousPeriod);
        } else {
            double totalPreviousYear = DataManagement.calculateTotal(dataEntries, dataPeriod.previousYear(), category);
            return createRow(category, scales, percentage, totalThisPeriod, totalPreviousPeriod, totalPreviousYear);
        }
    }

    private LinearLayout createTotalsRow(double percentage, DataManagement.DataPeriod dataPeriod, float[] scales) {
        double totalThisPeriod = DataManagement.calculateTotal(dataEntries, dataPeriod);
        double totalPreviousPeriod = DataManagement.calculateTotal(dataEntries, dataPeriod.previousPeriod());

        if (dataPeriod.getPeriod().equals(DataManagement.Periodicity.YEARLY)) {
            return createRow("Total", scales, percentage, totalThisPeriod, totalPreviousPeriod);
        } else {
            double totalPreviousYear = DataManagement.calculateTotal(dataEntries, dataPeriod.previousYear());
            return createRow("Total", scales, percentage, totalThisPeriod, totalPreviousPeriod, totalPreviousYear);
        }
    }

    private LinearLayout createRow(String category, float[] scales, double percentage,
                                   double totalThisPeriod, double... totalPreviousPeriods) {

        LinearLayout dataRow = initializeTableRow();

        double expectedBasedOnPreviousPeriod = percentage * totalPreviousPeriods[0];
        double percentageOfPreviousPeriod = Math.abs(expectedBasedOnPreviousPeriod) < 0.01 ? 1000 :
                100 * (totalThisPeriod - expectedBasedOnPreviousPeriod) / expectedBasedOnPreviousPeriod;
        String percentageOfPreviousPeriodS = percentageOfPreviousPeriod > 999.9 ? "" :
                String.format("%+.0f%%", percentageOfPreviousPeriod);
        int percentageColor = percentageOfPreviousPeriod > 0 ? Color.RED : Color.BLACK;

        dataRow.addView(createTextView(category, Typeface.NORMAL, scales[0], Color.BLACK));
        dataRow.addView(createTextView(String.format("%.0f", totalThisPeriod), Typeface.NORMAL, scales[1], Color.BLACK));
        dataRow.addView(createTextView(String.format("%.0f", totalPreviousPeriods[0]), Typeface.NORMAL, scales[2], Color.BLACK));
        dataRow.addView(createTextView(String.format("%s", percentageOfPreviousPeriodS), Typeface.NORMAL, scales[3], percentageColor));

        if (totalPreviousPeriods.length > 1) {
            double expectedBasedOnPreviousYear = percentage * totalPreviousPeriods[1];
            double percentageOfPreviousYear = Math.abs(expectedBasedOnPreviousYear) < 0.01 ? 1000 :
                    100 * (totalThisPeriod - expectedBasedOnPreviousYear) / expectedBasedOnPreviousYear;
            String percentageOfPreviousYearS = percentageOfPreviousYear > 999.9 ? "" :
                    String.format("%+.0f%%", percentageOfPreviousYear);
            percentageColor = percentageOfPreviousYear > 0 ? Color.RED : Color.BLACK;

            dataRow.addView(createTextView(String.format("%.0f", totalPreviousPeriods[1]), Typeface.NORMAL, scales[4], Color.BLACK));
            dataRow.addView(createTextView(String.format("%s", percentageOfPreviousYearS), Typeface.NORMAL, scales[5], percentageColor));
        }

        return dataRow;
    }

    private TextView createTextView(String text, int typeface, float weight, int color) {
        TextView dataview = new TextView(getApplicationContext());
        dataview.setPadding(5,0,5,0);
        dataview.setTypeface(null, typeface);
        dataview.setText(text);
        dataview.setTextColor(color);
        dataview.setTextSize(12);

        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        size.weight = weight;
        dataview.setLayoutParams(size);

        return dataview;
    }

    enum Graphic {
        TABLE, PIE, NONE;
    }
}

