package com.example.thoms.expenditurestracking;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    private static final String ERROR_INPUT_INCORRECT = "err01";

    public static Categories.Category categorySelected = Categories.Category.NONE;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Categories.initializeCategories(getApplicationContext(), (LinearLayout) findViewById(R.id.categoryselect));
        DatePickers.createDatePickers( (NumberPicker) findViewById(R.id.numberpicker_year)
                , (NumberPicker) findViewById(R.id.numberpicker_month)
                , (NumberPicker) findViewById(R.id.numberpicker_day) );
        DatePickers.createPeriodicSelect( (NumberPicker) findViewById(R.id.numberpicker_endyear)
                , (NumberPicker) findViewById(R.id.numberpicker_endmonth)
                , (NumberPicker) findViewById(R.id.numberpicker_endday)
                , (CheckBox) findViewById(R.id.checkbox_periodically)
                , (RadioGroup) findViewById(R.id.radiogroup_periodselect)
                , (RadioButton) findViewById(R.id.radiobutton_monthlyperiods)
                , (RadioButton) findViewById(R.id.radiobutton_weeklyperiods)
                , (TextView) findViewById(R.id.textview_enddateselect) );

        configureSaveButton();
        configureSyncFileButton();
        configureEmailFileButton();
        configureShowDataButton();
        configureShowSummaryButton();

        //createDummyData();
    }

    private void configureShowSummaryButton()
    {
        Button showSummaryButton = (Button) findViewById(R.id.button_show_summary);
        Styling.applyButtonStyling(showSummaryButton);

        showSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShowSummary.class);
                startActivity(intent);
            }
        });
    }

    private void configureShowDataButton()
    {
        Button showDataButton = (Button) findViewById(R.id.button_show_data);
        Styling.applyButtonStyling(showDataButton);

        showDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShowData.class);
                startActivity(intent);
            }
        });
    }

    private void configureEmailFileButton()
    {
        Button emailfilebutton = (Button) findViewById(R.id.button_uploadfile);
        Styling.applyButtonStyling(emailfilebutton);

        emailfilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String s = DataManagement.readFromFile(getBaseContext().getFileStreamPath(DataManagement.FILE_NAME), getApplicationContext());

                    if(s != null)
                    {
                        String subject = "DataExpendituresTracking_";
                        Calendar rightnow = Calendar.getInstance();
                        subject += rightnow.getTime();

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("vnd.android.cursor.dir/email");
                        String to[] = {"thom.steenhuis@gmail.com"};
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, s);
                        startActivity(Intent.createChooser(emailIntent , "Send email..."));
                    }
                }
                catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext()
                            , "There are no email clients installed."
                            , Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void configureSyncFileButton()
    {
        Button syncfilebutton = (Button) findViewById(R.id.button_syncfile);
        Styling.applyButtonStyling(syncfilebutton);

        syncfilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File externalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                        + File.separator +  "expenditures.txt");
                if (!externalFile.exists()) {
                    Toast.makeText(getApplicationContext(), "File (expenditures.txt) does not exist. Data cannot be synced.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<DataManagement.DataEntry> externalParsedData =
                        DataManagement.readAndParseFromFile(externalFile, getApplicationContext());

                File internalFile = getBaseContext().getFileStreamPath(DataManagement.FILE_NAME);
                if(!internalFile.exists())
                {
                    DataManagement.createNewFile(internalFile, getApplicationContext());
                }

                List<DataManagement.DataEntry> internalParsedData =
                        DataManagement.readAndParseFromFile(internalFile, getApplicationContext());

                DataManagement.mergeWithoutDuplicates(externalParsedData, internalParsedData);
                DataManagement.writeToFile(internalParsedData, internalFile, getApplicationContext());
            }
        });
    }

    private void configureSaveButton()
    {
        Button savebutton = (Button) findViewById(R.id.button_save);
        Styling.applyButtonStyling(savebutton);

        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String line = parseData();

                try{
                    if(!line.equals(ERROR_INPUT_INCORRECT))
                    {
                        File file = getBaseContext().getFileStreamPath(DataManagement.FILE_NAME);
                        if(!file.exists())
                        {
                            DataManagement.createNewFile(file, getApplicationContext());
                        }

                        FileOutputStream fout = openFileOutput(DataManagement.FILE_NAME, Context.MODE_APPEND);
                        fout.write(line.getBytes());
                        fout.close();

                        Toast.makeText(getApplicationContext(),"Saved data succesfully"
                                ,Toast.LENGTH_SHORT).show();

                        EditText edittext_amount
                                = (EditText) findViewById(R.id.edittext_amountenter);
                        edittext_amount.setText("");
                        CheckBox checkbox_periodselect
                                = (CheckBox) findViewById(R.id.checkbox_periodically);
                        checkbox_periodselect.setChecked(false);
                        DatePickers.hidePeriodicallyParts();
                    }
                } catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(), e.getMessage()
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createDummyData() {

        String line1 = "\n2018\t3\t14\tGroceries\t33.00";
        String line2 = "\n2019\t11\t30\tInsurance\t8.00";
        String line3 = "\n2019\t11\t30\tVeeeeeery laaaaaaaaaaarge category name\t8.00";
        String line = "\n2017\t2\t28\tHoliday\t600.00";
        File file = getBaseContext().getFileStreamPath(DataManagement.FILE_NAME);

        try {
            file.createNewFile();
            FileOutputStream fout = openFileOutput(DataManagement.FILE_NAME, Context.MODE_PRIVATE);

            fout.write(DataManagement.HEAD.getBytes());
            fout.write(line1.getBytes());
            fout.write(line2.getBytes());
            for (int idx = 0; idx < 50; idx++) {
                fout.write(line.getBytes());
            }
            fout.close();

            Toast.makeText(getApplicationContext(),"New file created"
                    ,Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage()
                    , Toast.LENGTH_SHORT).show();
        }
    }

    private String parseData()
    {
        boolean inputerror = false;

        if(categorySelected == Categories.Category.NONE)
        {
            inputerror = true;

            Toast.makeText( getApplicationContext(), "Category is not selected"
                    , Toast.LENGTH_SHORT).show();
        }
        else
        {
            try{
                String amount_text = ( (EditText) findViewById(R.id.edittext_amountenter) )
                        .getText().toString();
                double amount = Double.parseDouble(amount_text);


                CheckBox periodically = (CheckBox) findViewById(R.id.checkbox_periodically);

                if(periodically.isChecked())
                {
                    RadioGroup radiogroup_periods
                            = (RadioGroup) findViewById(R.id.radiogroup_periodselect);
                    RadioButton radiobutton_weekly
                            = (RadioButton) findViewById(R.id.radiobutton_weeklyperiods);
                    RadioButton radiobutton_monthly
                            = (RadioButton) findViewById(R.id.radiobutton_monthlyperiods);
                    int end_year = ((NumberPicker) findViewById(R.id.numberpicker_endyear))
                            .getValue();
                    int end_month = ((NumberPicker) findViewById(R.id.numberpicker_endmonth))
                            .getValue();
                    int end_day = ((NumberPicker) findViewById(R.id.numberpicker_endday))
                            .getValue();
                    int current_year = ((NumberPicker) findViewById(R.id.numberpicker_year))
                            .getValue();
                    int current_month = ((NumberPicker) findViewById(R.id.numberpicker_month))
                            .getValue();
                    int current_day = ((NumberPicker) findViewById(R.id.numberpicker_day))
                            .getValue();
                    String data = "";

                    if( DatePickers.period_selected == radiobutton_monthly.getId() )
                    {
                        int saved_day = current_day;
                        while( !dateLarger( current_year, current_month, saved_day
                                , end_year, end_month, end_day ) )
                        {
                            data += String.format( "\n%d\t%d\t%d\t%s\t%.0f", current_year
                                    , current_month, saved_day
                                    , categorySelected.name, amount );

                            saved_day = current_day;

                            if(current_month == 12)
                            {
                                current_month = 1;
                                current_year += 1;
                            }
                            else
                                current_month += 1;

                            if( current_day > 30 && ( current_month == 4 || current_month == 6
                                    || current_month == 9 || current_month == 11 ) )
                            {
                                saved_day = 30;
                            }
                            else if( current_day > 28 && current_month == 2
                                    && (current_year % 4) > 0)
                            {
                                saved_day = 28;
                            }
                            else if( current_day > 29 && current_month == 2 )
                            {
                                saved_day = 29;
                            }
                        }

                        return data;
                    }
                    else if( DatePickers.period_selected == radiobutton_weekly.getId() )
                    {
                        while( !dateLarger( current_year, current_month, current_day
                                , end_year, end_month, end_day ) ) {
                            data += String.format("\n%d\t%d\t%d\t%s\t%.0f", current_year
                                    , current_month, current_day
                                    , categorySelected.name, amount);

                            current_day += 7;

                            if( current_day > 30 && ( current_month == 4
                                    || current_month == 6 || current_month == 9
                                    || current_month == 11 ) )
                            {
                                current_month += 1;
                                current_day -= 30;
                            }
                            else if (current_day > 29 && current_month == 2)
                            {
                                current_month += 1;
                                current_day -= 29;
                            }
                            else if (current_day > 28 && current_month == 2
                                    && (current_year % 4) != 0)
                            {
                                current_month += 1;
                                current_day -= 28;
                            }
                            else if (current_day > 31)
                            {
                                current_month += 1;
                                current_day -= 31;

                                if (current_month == 13)
                                {
                                    current_month = 1;
                                    current_year += 1;
                                }
                            }
                        }

                        return data;
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext()
                                , "No time interval is selected"
                                , Toast.LENGTH_SHORT ).show();
                    }
                }
                else
                {
                    int year = ( (NumberPicker) findViewById(R.id.numberpicker_year) ).getValue();
                    int month = ( (NumberPicker) findViewById(R.id.numberpicker_month) ).getValue();
                    int day = ( (NumberPicker) findViewById(R.id.numberpicker_day) ).getValue();

                    return "\n" + year + "\t" + month + "\t" + day + "\t"
                            + categorySelected.name + "\t"
                            + String.format( "%.0f", amount );
                }
            }
            catch(NumberFormatException e)
            {
                Toast.makeText( getApplicationContext(), "No amount entered"
                        , Toast.LENGTH_SHORT).show();
            }
        }

        return ERROR_INPUT_INCORRECT;
    }

    private static boolean dateLarger( int y1, int m1, int d1, int y2, int m2, int d2 )
    {
        if( y1 > y2 ) return true;
        if( y1 < y2 ) return false;
        if( m1 > m2 ) return true;
        if( m1 < m2 ) return false;
        if( d1 > d2 ) return true;
        return false;
    }

}
