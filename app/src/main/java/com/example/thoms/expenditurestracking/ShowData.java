package com.example.thoms.expenditurestracking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowData extends AppCompatActivity {

    private final String[] header = { "Date", "Category", "Amount" };

    private List<DataManagement.DataLine> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        createView();

        configureBackButton();
        configureResetFileButton();
        configureDeleteButton();
        configureEmailFileButton();
    }

    private void createView() {

        File file = getBaseContext().getFileStreamPath(DataManagement.FILE_NAME);
        if(!file.exists())
        {
            DataManagement.createNewFile(file, getApplicationContext());
        }

        List<DataManagement.DataEntry> parsedData =
                DataManagement.readAndParseFromFile(file, getApplicationContext());

        if (parsedData != null)
        {
            data = new ArrayList<>();

            for(DataManagement.DataEntry entry : parsedData) {
                data.add(new DataManagement.DataLine(new CheckBox(getApplicationContext()), entry));
            }

            drawView();
        }
    }

    private void configureBackButton()
    {
        Button backButton = (Button) findViewById(R.id.button_back);
        Styling.applyButtonStyling(backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowData.this, MainActivity.class);

                startActivity(intent);

            }
        });
    }

    private File getTargetFile(File[] files) {
        if (files == null) {
            return null;
        }
        List<File> targetFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith("DataExpendituresTracking")) {
                targetFiles.add(file);
            }
        }
        if (targetFiles.size() == 0) {
            return null;
        }
        return Collections.max(targetFiles, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                Date date1 = Date.valueOf(file1.getName().split("_")[1]);
                Date date2 = Date.valueOf(file2.getName().split("_")[1]);
                return date1.compareTo(date2);
            }
        });
    }

    private void configureResetFileButton()
    {
        Button resetfilebutton = (Button) findViewById(R.id.button_reset);
        Styling.applyButtonStyling(resetfilebutton);

        resetfilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    FileOutputStream fout = openFileOutput(DataManagement.FILE_NAME, Context.MODE_PRIVATE);
                    fout.write(DataManagement.HEAD.getBytes());
                    fout.close();

                    Toast.makeText( getApplicationContext(),"File was successfully reset",
                            Toast.LENGTH_SHORT ).show();
                }
                catch(Exception e)
                {
                    Toast.makeText( getApplicationContext(),e.getMessage(),
                            Toast.LENGTH_SHORT ).show();
                }

                createView();
            }
        });
    }

    private void configureDeleteButton()
    {
        Button deletebutton = (Button) findViewById(R.id.button_delete);
        Styling.applyButtonStyling(deletebutton);

        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int noRecordsDelected = writeData();
                drawView();
                if (noRecordsDelected != -1) {
                    Toast.makeText(getApplicationContext()
                            , String.format("Deleted %d records.", noRecordsDelected)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void configureEmailFileButton()
    {
        Button emailfilebutton = (Button) findViewById(R.id.button_emailfile);
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

    private int writeData() {

        File file = getBaseContext().getFileStreamPath(DataManagement.FILE_NAME);
        try{
            List<DataManagement.DataLine> filteredData = new ArrayList<>();
            int noRecordsDeleted = 0;

            file.createNewFile();
            FileOutputStream fout = openFileOutput(DataManagement.FILE_NAME, Context.MODE_PRIVATE);
            fout.write(DataManagement.HEAD.getBytes());
            for (DataManagement.DataLine dataLine : data) {
                if (!dataLine.getCheckBox().isChecked()) {
                    filteredData.add(dataLine);
                    fout.write(dataLine.getData().toString().getBytes());
                } else {
                    noRecordsDeleted++;
                }
            }
            fout.close();

            data = filteredData;
            return noRecordsDeleted;
        } catch(IOException e) {
            Toast.makeText(getApplicationContext(), "File could not be found"
                    , Toast.LENGTH_SHORT).show();
            return -1;
        }
    }

    private void drawView() {

        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout_data);
        tableLayout.removeAllViews();
        List<TableRow> tableRows = new ArrayList<>();
        TableRow headerRow = new TableRow(getApplicationContext());
        TableRow.LayoutParams lp
                = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        headerRow.setLayoutParams(lp);

        for (String datafield : this.header) {
            TextView dataview = new TextView(getApplicationContext());
            dataview.setTypeface(null, Typeface.BOLD);
            dataview.setText(datafield);
            headerRow.addView(dataview);
        }

        tableRows.add(headerRow);

        int cnt = 0;
        for (DataManagement.DataLine dataLine : this.data) {

            if (dataLine.getCheckBox().isChecked()) {
                continue;
            }

            cnt++;
            TableRow datarow = new TableRow(getApplicationContext());
            datarow.setLayoutParams(lp);

            TextView dataviewDate = new TextView(getApplicationContext());
            dataviewDate.setText(dataLine.getData().getDate());
            datarow.addView(dataviewDate);

            TextView dataviewCategory = new TextView(getApplicationContext());
            dataviewCategory.setText(dataLine.getData().getCategoryForView());
            datarow.addView(dataviewCategory);

            TextView dataviewAmount = new TextView(getApplicationContext());
            dataviewAmount.setText(dataLine.getData().getAmount());
            datarow.addView(dataviewAmount);

            dataLine.setCheckBox(new CheckBox(getApplicationContext()));
            datarow.addView(dataLine.getCheckBox());
            dataLine.getCheckBox().setId(cnt);
            tableRows.add(datarow);
        }

        for (TableRow tableRow : tableRows) {
            tableLayout.addView(tableRow);
        }
    }
}
