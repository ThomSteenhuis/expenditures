package com.example.thoms.expenditurestracking;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManagement {

    static final String FILE_NAME = "expenditures.txt";

    static final String HEAD = "Year\tMonth\tDay\tCategory\tAmount";

    static void createNewFile(File file, Context context) {
        try{
            file.createNewFile();

            FileOutputStream fout = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fout);

            writer.write(DataManagement.HEAD);

            writer.close();
            fout.close();

            Toast.makeText(context,"New file created"
                    ,Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage()
                    , Toast.LENGTH_SHORT).show();
        }

    }

    static List<DataEntry> readAndParseFromFile(File file, Context context) {
        return parseData(readFromFile(file, context));
    }

    static String readFromFile(File file, Context context) {
        try{
            FileInputStream fin = new FileInputStream(file);
            InputStreamReader InputRead= new InputStreamReader(fin);

            char[] inputBuffer = new char[100];
            StringBuilder sb = new StringBuilder(100);
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer,0,charRead);
                sb.append(readstring);
            }
            InputRead.close();
            fin.close();
            return sb.toString();
        }
        catch(IOException e)
        {
            Toast.makeText(context, "File could not be found"
                    , Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    static List<DataEntry> parseData(String rawData) {
        if(rawData == null) {
            return null;
        }

        List<DataEntry> parsedData = new ArrayList<>();
        String[] lines = rawData.split("\n");

        for(int idx = 1; idx < lines.length; idx++) {
            parsedData.add(new DataEntry(lines[idx]));
        }

        Collections.sort(parsedData);
        return parsedData;
    }

    static void mergeWithoutDuplicates(List<DataEntry> from, List<DataEntry> to) {
        if (to == null || from == null) {
            return;
        }
        Set<DataEntry> uniqueElements = new HashSet<>(to);
        for (DataEntry entry : from) {
            if (!uniqueElements.contains(entry)) {
                to.add(entry);
            }
        }
        Collections.sort(to);
    }

    static void writeToFile(List<DataEntry> data, File file, Context context) {
        try {
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fout);

            writer.write(HEAD);
            for (DataEntry entry : data) {
                writer.write(entry.toString());
            }

            writer.close();
            fout.close();

            Toast.makeText(context,"File synced."
                    ,Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(context, e.getMessage()
                    , Toast.LENGTH_SHORT).show();
        }
    }

    static double calculateTotal(List<DataEntry> dataEntries, DataPeriod dataPeriod, String category) {
        int total = 0;
        for (DataEntry dataEntry : dataEntries) {
            if (dataEntry.getCategory().equals(category) && dataEntry.isInDataPeriod(dataPeriod)) {
                total += Double.parseDouble(dataEntry.amount);
            }
        }
        return total;
    }

    static double calculateTotal(List<DataEntry> dataEntries, DataPeriod dataPeriod) {
        int total = 0;
        for (DataEntry dataEntry : dataEntries) {
            if (dataEntry.isInDataPeriod(dataPeriod)) {
                total += Double.parseDouble(dataEntry.amount);
            }
        }
        return total;
    }

    static double calculatePercentageOfPeriod(Calendar calendar, DataPeriod dataPeriod) {

        if (!dataPeriod.isInPeriod(calendar)) {
            return 1.0;
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int quarter = (month - 1) / 3 + 1;
        double day = calendar.get(Calendar.DAY_OF_MONTH);

        switch (dataPeriod.getPeriod()) {
            case YEARLY: {
                double days = day;
                for (int m = 1; m < month ; m++) {
                    days += DatePickers.noDaysInMonth(DatePickers.Month.findMonth(m), year);
                }
                return DatePickers.isSchrikkelJaar(year) ? days / 366 : days / 365;
            }
            case QUARTERLY: {
                double days = day;
                double totalDays = 0;
                for (int m = (quarter - 1) * 3 + 1; m < quarter * 3; m++) {
                    totalDays += DatePickers.noDaysInMonth(DatePickers.Month.findMonth(m), year);
                    if (m < month) {
                        days += DatePickers.noDaysInMonth(DatePickers.Month.findMonth(m), year);
                    }
                }
                return days / totalDays;
            }
            case MONTHLY: {
                return day / (double) (DatePickers.noDaysInMonth(DatePickers.Month.findMonth(month), year));
            }
            default: return -1;
        }
    }

    static int maxYear(List<DataEntry> entries) {
        if (entries == null || entries.size() == 0) {
            return -1;
        }

        int max = entries.get(0).getYearInt();
        for (DataEntry entry : entries) {
            if (entry.getYearInt() > max) {
                max = entry.getYearInt();
            }
        }

        return max;
    }

    static int minYear(List<DataEntry> entries) {
        if (entries == null || entries.size() == 0) {
            return -1;
        }

        int min = entries.get(0).getYearInt();
        for (DataEntry entry : entries) {
            if (entry.getYearInt() < min) {
                min = entry.getYearInt();
            }
        }

        return min;
    }

    static class DataLine implements Comparable<DataLine> {
        private CheckBox checkBox;

        private final DataEntry data;

        DataLine(CheckBox checkBox, DataEntry data) {
            this.checkBox = checkBox;
            this.data = data;
        }

        void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

        CheckBox getCheckBox() {
            return checkBox;
        }

        DataEntry getData() {
            return data;
        }

        public int compareTo(DataLine other) {
            return this.getData().compareTo(other.getData());
        }
    }

    static class DataEntry implements Comparable<DataEntry> {

        private final int year;

        private final int month;

        private final int day;

        private final String category;

        private final String amount;

        DataEntry(String rawData) {
            int year = 0;
            int month = 0;
            int day = 0;
            String category = "";
            String amount = "";

            try {
                String[] data = rawData.split("\t");
                if (data.length != 5) {
                    throw new IllegalArgumentException();
                }
                year = Integer.parseInt(data[0]);
                month = Integer.parseInt(data[1]);
                day = Integer.parseInt(data[2]);
                category = data[3];
                amount = data[4];
            } catch (IllegalArgumentException e) {
                year = 0;
                month = 0;
                day = 0;
                category = "";
                amount = "";
            }

            this.year = year;
            this.month = month;
            this.day = day;
            this.category = category;
            this.amount = amount;
        }

        @Override
        public int compareTo(DataEntry other) {
            if (this.getYearInt() != other.getYearInt()) {
                return other.getYearInt() - this.getYearInt();
            }
            if (this.getMonthInt() != other.getMonthInt()) {
                return other.getMonthInt() - this.getMonthInt();
            }
            return other.getDayInt() - this.getDayInt();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DataEntry)) {
                return false;
            }
            DataEntry other = (DataEntry) obj;
            return this.getYearInt() == other.getYearInt() &&
                    this.getMonthInt() == other.getMonthInt() &&
                    this.getDayInt() == other.getDayInt() &&
                    this.getCategory().equals(other.getCategory()) &&
                    this.getAmount().equals(other.getAmount());
        }

        @Override
        public int hashCode() {
            int hash = 33;
            hash = 47 * hash + year;
            hash = 47 * hash + month;
            hash = 47 * hash + day;
            hash = 47 * hash + (category == null ? 0 : category.hashCode());
            hash = 47 * hash + (amount == null ? 0 : amount.hashCode());
            return hash;
        }

        public String toString() {
            return String.format("\n%d\t%d\t%d\t%s\t%s", year, month, day, category, amount);
        }

        boolean isInDataPeriod(DataPeriod dataPeriod) {
            if (year != dataPeriod.getYear()) {
                return false;
            }
            switch (dataPeriod.getPeriod()) {
                case YEARLY: return true;
                case QUARTERLY: return getQuarter() == dataPeriod.getIdx();
                case MONTHLY: return month == dataPeriod.getIdx();
                default: return false;
            }
        }

        String getDate() {
            return String.format("%d-%d-%d", year, month, day);
        }

        String getCategoryForView() {
            if (category.length() > 20) {
                return category.substring(0, 17) + "...";
            }
            StringBuilder sb = new StringBuilder(20);
            sb.append(category);
            while (sb.length() < 20) {
                sb.append(" ");
            }
            return sb.toString();
        }

        String getCategory() {
            return category;
        }

        String getAmount() {
            return amount;
        }

        int getYearInt() {
            return year;
        }

        int getMonthInt() {
            return month;
        }

        int getDayInt() {
            return day;
        }

        int getQuarter() {
            return (month - 1) / 3 + 1;
        }
    }

    static class DataPeriod {

        private final Periodicity period;

        private final int year;

        private final int idx;

        DataPeriod(Periodicity period, int year, int idx) {
            this.period = period;
            this.year = year;
            this.idx = idx;
        }

        Periodicity getPeriod() {
            return period;
        }

        int getYear() {
            return year;
        }

        int getIdx() {
            return idx;
        }

        String getName() {
            switch (period) {
                case YEARLY: return String.format("%d", year);
                case QUARTERLY: return String.format("%02dQ%d", year, idx);
                case MONTHLY: return String.format("%02d-%02d", year, idx);
                default: return null;
            }
        }

        String getNiceName() {
            switch (period) {
                case YEARLY: return String.format("%d", year);
                case QUARTERLY: return String.format("Q%d %02d", idx, year);
                case MONTHLY: return String.format("%s %02d", Months.getMonth(idx).getAbbreviation(), year);
                default: return null;
            }
        }

        DataPeriod previousPeriod() {
            switch (period) {
                case YEARLY: return new DataPeriod(period, year - 1, idx);
                case QUARTERLY:
                    if (idx == 1) {
                        return new DataPeriod(Periodicity.QUARTERLY, year - 1, 4);
                    }
                    return new DataPeriod(Periodicity.QUARTERLY, year, idx - 1);
                case MONTHLY:
                    if (idx == 1) {
                        return new DataPeriod(Periodicity.MONTHLY, year - 1, 12);
                    }
                    return new DataPeriod(Periodicity.MONTHLY, year, idx - 1);
                default: return null;
            }
        }

        DataPeriod previousYear() {
            return new DataPeriod(period, year - 1, idx);
        }

        boolean isInPeriod(Calendar calendar) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int quarter = (month - 1) / 3 + 1;

            if (this.year != year) {
                return false;
            }

            switch (period) {
                case YEARLY: return true;
                case QUARTERLY: return this.idx == quarter;
                case MONTHLY: return this.idx == month;
                default: return false;
            }
        }
    }

    enum Periodicity {
        YEARLY, QUARTERLY, MONTHLY
    }

    enum Months {
        JANUARY(1, "January"),
        FEBRUARY(2, "February"),
        MARCH(3, "March"),
        APRIL(4, "April"),
        MAY(5, "May"),
        JUNE(6, "June"),
        JULY(7, "July"),
        AUGUST(8, "August"),
        SEPTEMBER(9, "September"),
        OCTOBER(10, "October"),
        NOVEMBER(11, "November"),
        DECEMBER(12, "December");

        private final int idx;
        public final String name;

        Months(int idx, String name) {
            this.idx = idx;
            this.name = name;
        }

        public String getAbbreviation() {
            return name.substring(0, 3);
        }

        public static Months getMonth(int idx) {
            for (Months month : Months.values()) {
                if (month.idx == idx) {
                    return month;
                }
            }
            return null;
        }

        public static String[] toStringArray() {
            String[] array = new String[Months.values().length];
            for (Months month : Months.values()) {
                array[month.idx - 1] = month.name;
            }
            return array;
        }
    }

    enum Quarters {
        Q1(1),
        Q2(2),
        Q3(3),
        Q4(4);

        private final int idx;

        Quarters(int idx) {
            this.idx = idx;
        }

        public static Quarters getQuarter(int idx) {
            for (Quarters quarter : Quarters.values()) {
                if (quarter.idx == idx) {
                    return quarter;
                }
            }
            return null;
        }

        public static String[] toStringArray() {
            String[] array = new String[Quarters.values().length];
            for (Quarters quarter : Quarters.values()) {
                array[quarter.idx - 1] = quarter.name();
            }
            return array;
        }
    }
}
