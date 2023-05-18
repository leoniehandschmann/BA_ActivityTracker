package com.example.activitytracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HomeScreen extends Fragment {

    public static ListView locationList;
    public static ArrayList<String> addresses;
    public static ArrayList<String> addressesList;
    public static ArrayAdapter <String> listViewAdapter;
    private BarChart stepBarChart;
    public static ArrayList <BarEntry> barDataList;
    public static ArrayList <BarEntry> barDataList2;
    private steps_dbHelper stepsDB;
    private ArrayList<Integer>mo;
    private ArrayList<Integer>di;
    private ArrayList<Integer>mi;
    private ArrayList<Integer>don;
    private ArrayList<Integer>fr;
    private ArrayList<Integer>sa;
    private ArrayList<Integer>so;

    ArrayList <String> latNotOlderThan24H;
    ArrayList <String> longNotOlderThan24H;
    ArrayList <String> timeStampNotOlderThan24H;


    public HomeScreen() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen, container, false);
        locationList = view.findViewById(R.id.location_list);
        stepBarChart = view.findViewById(R.id.step_chart);
        stepsDB = new steps_dbHelper(getActivity().getApplicationContext());

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    if(stepsDB == null){
                        initStepBarChart(getBarDataWithEmptyDB());
                    }else{
                        initDaysValues();
                        initStepBarChart(testBarData());
                    }
                }
                catch (Exception e) {
                    Log.d("updateTV","not successful");
                }
                finally{
                    handler.postDelayed(this, 10000);
                }
            }
        };
        handler.postDelayed(runnable, 10000);

        locDataNotOlderThan24();
        try {
            initLocationListView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return view;
    }



    private void initStepBarChart(ArrayList <BarEntry> data){

        BarDataSet barDataSet = new BarDataSet(data,"");
        BarData barData = new BarData();
        barData.addDataSet(barDataSet);
        stepBarChart.setData(barData);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        XAxis xAxis = stepBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(6);
        xAxis.setCenterAxisLabels(true);

        String[] daysOfTheWeek = {"MO", "DI", "MI", "DO", "FR", "SA", "SO"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Log.d("WeeklyDistance", "value = " + ((int) value));
                if (((int) value) > -1 && ((int) value) < 7) {
                    return daysOfTheWeek[((int) value)];
                }
                return "";
            }
        });

        stepBarChart.getXAxis().setAxisMinimum(0);
    }



    private void initLocationListView() throws IOException {
        location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        addresses = new ArrayList<String>();
        addressesList = new ArrayList<String>();

        if(latNotOlderThan24H!=null){
            for(int i =0;i<latNotOlderThan24H.size();i++){
                String ad = LocationTracking.getAddressFromLatLong(Double.parseDouble(latNotOlderThan24H.get(i)),Double.parseDouble(longNotOlderThan24H.get(i)), getActivity().getApplicationContext());
                addresses.add(ad);
            }
        }else {
            if (c.moveToFirst()) {
                do {
                    try {
                        String ad = LocationTracking.getAddressFromLatLong(Double.parseDouble(latNotOlderThan24H.get(1)),Double.parseDouble(longNotOlderThan24H.get(2)), getActivity().getApplicationContext());
                        addresses.add(ad);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }


        Set<String> set = new HashSet<>();
        for (String s : addresses) {
            if (set.add(s)) {
                addressesList.add(s);
            }
        }

        listViewAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,addressesList);


        locationList.setAdapter(listViewAdapter);
    }



    private ArrayList<BarEntry> getBarData(){
        barDataList = new ArrayList<BarEntry>();

        barDataList.add(new BarEntry(0.5f,mo.get(0)));
        barDataList.add(new BarEntry(1.5f,di.get(0)));
        barDataList.add(new BarEntry(2.5f,mi.get(0)));
        barDataList.add(new BarEntry(3.5f,don.get(0)));
        barDataList.add(new BarEntry(4.5f,fr.get(0)));
        barDataList.add(new BarEntry(5.5f,sa.get(0)));
        barDataList.add(new BarEntry(6.6f,so.get(0)));

        Log.d("link", String.valueOf(barDataList));
        return barDataList;
    }

    private void initDaysValues() throws ParseException {
        mo = new ArrayList<Integer>();
        di = new ArrayList<Integer>();
        mi = new ArrayList<Integer>();
        don = new ArrayList<Integer>();
        fr = new ArrayList<Integer>();
        sa = new ArrayList<Integer>();
        so = new ArrayList<Integer>();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = df.format(c.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date tdy = format.parse(today);
        String todayDay = DateFormat.format("EEEE", tdy).toString();


        Cursor cursor = stepsDB.getData();
        cursor.moveToFirst();
        while(cursor.moveToNext()) {

            Integer valueFromDB = Integer.valueOf(cursor.getString(1));
            String dateFromDB = cursor.getString(2);


            //check if date in db is not older than 7 days
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate from = LocalDate.parse(dateFromDB);
                LocalDate to = LocalDate.parse(today);
                if (ChronoUnit.DAYS.between(from, to) <= 7) {
                    try {
                        Date date = format.parse(dateFromDB);
                        String day = DateFormat.format("EEEE", date).toString();

                        if(day.equals("Montag")){
                            mo.add(valueFromDB);
                        }else if (day.equals("Dienstag")){
                            di.add(valueFromDB);
                        } else if (day.equals("Mittwoch")){
                            mi.add(valueFromDB);
                        }else if (day.equals("Donnerstag")){
                            don.add(valueFromDB);
                        }else if (day.equals("Freitag")){
                            fr.add(valueFromDB);
                        }else if (day.equals("Samstag")){
                            sa.add(valueFromDB);
                        }else if (day.equals("Sonntag")){
                            so.add(valueFromDB);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(mo.isEmpty()){
            if(todayDay.equals("Montag")){
                mo.add(LocationTracking.stepCount);
            }else{
                mo.add(0);
            }
        }
        if(di.isEmpty()){
            if(todayDay.equals("Dienstag")){
                di.add(LocationTracking.stepCount);
            }else{
                di.add(0);
            }
        }
        if(mi.isEmpty()){
            if(todayDay.equals("Mittwoch")){
                mi.add(LocationTracking.stepCount);
            }else{
                mi.add(0);
            }
        }
        if(don.isEmpty()){
            if(todayDay.equals("Donnerstag")){
                don.add(LocationTracking.stepCount);
            }else{
                don.add(0);
            }
        }
        if(fr.isEmpty()){
            if(todayDay.equals("Freitag")){
                fr.add(LocationTracking.stepCount);
            }else{
                fr.add(0);
            }
        }
        if(sa.isEmpty()){
            if(todayDay.equals("Samstag")){
                sa.add(LocationTracking.stepCount);
            }else{
                sa.add(0);
            }
        }
        if(so.isEmpty()){
            if(todayDay.equals("Sonntag")){
                so.add(LocationTracking.stepCount);
            }else{
                so.add(0);
            }
        }

    }

    public static ArrayList<BarEntry> getBarDataWithEmptyDB(){
        barDataList2 = new ArrayList<BarEntry>();
        Calendar c = Calendar.getInstance();
        String dayToday = android.text.format.DateFormat.format("EEEE", c).toString();
        Log.d("link",dayToday);
        if(dayToday.equals("Montag")){
            Log.d("link","i am in loop");
            barDataList2.add(new BarEntry(0.5f,LocationTracking.stepCount));
            barDataList2.add(new BarEntry(1.5f,0));
            barDataList2.add(new BarEntry(2.5f,0));
            barDataList2.add(new BarEntry(3.5f,0));
            barDataList2.add(new BarEntry(4.5f,0));
            barDataList2.add(new BarEntry(5.5f,0));
            barDataList2.add(new BarEntry(6.6f,0));
        }
        if (dayToday.equals("Dienstag")){
            barDataList2.add(new BarEntry(0.5f,0));
            barDataList2.add(new BarEntry(1.5f,LocationTracking.stepCount));
            barDataList2.add(new BarEntry(2.5f,0));
            barDataList2.add(new BarEntry(3.5f,0));
            barDataList2.add(new BarEntry(4.5f,0));
            barDataList2.add(new BarEntry(5.5f,0));
            barDataList2.add(new BarEntry(6.6f,0));

        }
        if (dayToday.equals("Mittwoch")){
            barDataList2.add(new BarEntry(0.5f,0));
            barDataList2.add(new BarEntry(1.5f,0));
            barDataList2.add(new BarEntry(2.5f,LocationTracking.stepCount));
            barDataList2.add(new BarEntry(3.5f,0));
            barDataList2.add(new BarEntry(4.5f,0));
            barDataList2.add(new BarEntry(5.5f,0));
            barDataList2.add(new BarEntry(6.6f,0));
        }
        if (dayToday.equals("Donnerstag")){
            barDataList2.add(new BarEntry(0.5f,0));
            barDataList2.add(new BarEntry(1.5f,0));
            barDataList2.add(new BarEntry(2.5f,0));
            barDataList2.add(new BarEntry(3.5f,LocationTracking.stepCount));
            barDataList2.add(new BarEntry(4.5f,0));
            barDataList2.add(new BarEntry(5.5f,0));
            barDataList2.add(new BarEntry(6.6f,0));
        }
        if (dayToday.equals("Freitag")){
            barDataList2.add(new BarEntry(0.5f,0));
            barDataList2.add(new BarEntry(1.5f,0));
            barDataList2.add(new BarEntry(2.5f,0));
            barDataList2.add(new BarEntry(3.5f,0));
            barDataList2.add(new BarEntry(4.5f,LocationTracking.stepCount));
            barDataList2.add(new BarEntry(5.5f,0));
            barDataList2.add(new BarEntry(6.6f,0));
        }
        if (dayToday.equals("Samstag")){
            barDataList2.add(new BarEntry(0.5f,0));
            barDataList2.add(new BarEntry(1.5f,0));
            barDataList2.add(new BarEntry(2.5f,0));
            barDataList2.add(new BarEntry(3.5f,0));
            barDataList2.add(new BarEntry(4.5f,0));
            barDataList2.add(new BarEntry(5.5f,LocationTracking.stepCount));
            barDataList2.add(new BarEntry(6.6f,0));
        }
        if (dayToday.equals("Sonntag")){
            barDataList2.add(new BarEntry(0.5f,0));
            barDataList2.add(new BarEntry(1.5f,0));
            barDataList2.add(new BarEntry(2.5f,0));
            barDataList2.add(new BarEntry(3.5f,0));
            barDataList2.add(new BarEntry(4.5f,0));
            barDataList2.add(new BarEntry(5.5f,0));
            barDataList2.add(new BarEntry(6.6f,LocationTracking.stepCount));
        }

        return barDataList2;
    }

    private void locDataNotOlderThan24 (){
        location_dbHelper db2 = new location_dbHelper(getActivity().getApplicationContext());
        latNotOlderThan24H = new ArrayList<>();
        longNotOlderThan24H = new ArrayList<>();
        timeStampNotOlderThan24H = new ArrayList<>();

        Cursor curs = db2.getData();
        if (curs.moveToFirst()){
            do {

                if(!(Long.parseLong(curs.getString(3)) <= System.currentTimeMillis() - 60*60*24*1000)){
                    latNotOlderThan24H.add(curs.getString(1));
                    longNotOlderThan24H.add(curs.getString(2));
                    timeStampNotOlderThan24H.add(curs.getString(3));
                }
            } while(curs.moveToNext());
        }
        curs.close();
        db2.close();
    }

    private ArrayList<BarEntry> testBarData(){
        barDataList = new ArrayList<BarEntry>();

        barDataList.add(new BarEntry(0.5f,1200));
        barDataList.add(new BarEntry(1.5f,200));
        barDataList.add(new BarEntry(2.5f,4000));
        barDataList.add(new BarEntry(3.5f,LocationTracking.stepCount));
        barDataList.add(new BarEntry(4.5f,2300));
        barDataList.add(new BarEntry(5.5f,500));
        barDataList.add(new BarEntry(6.6f,5000));

        return barDataList;
    }




}
