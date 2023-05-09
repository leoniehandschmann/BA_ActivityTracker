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
                        initStepBarChart(getBarData());
                    }
                }
                catch (Exception e) {
                    Log.d("updateTV","not successful");
                }
                finally{
                    handler.postDelayed(this, 30000);
                }
            }
        };
        handler.postDelayed(runnable, 30000);




        initLocationListView();

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



    private void initLocationListView(){
        location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        addresses = new ArrayList<String>();
        addressesList = new ArrayList<String>();


        if(c.moveToFirst()){
            do{
                try {
                    addresses.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)), getActivity().getApplicationContext()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
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
                            if(day.equals(todayDay)){
                                mo.add(LocationTracking.stepCount);
                            }else{
                                mo.add(valueFromDB);
                            }
                        }else if (day.equals("Dienstag")){
                            if(day.equals(todayDay)){
                                di.add(LocationTracking.stepCount);
                            }else{
                                di.add(valueFromDB);
                            }
                        } else if (day.equals("Mittwoch")){
                            if(day.equals(todayDay)){
                                mi.add(LocationTracking.stepCount);
                            }else{
                                mi.add(valueFromDB);
                            }
                        }else if (day.equals("Donnerstag")){
                            if(day.equals(todayDay)){
                                don.add(LocationTracking.stepCount);
                            }else{
                                don.add(valueFromDB);
                            }
                        }else if (day.equals("Freitag")){
                            if(day.equals(todayDay)){
                                fr.add(LocationTracking.stepCount);
                            }else{
                                fr.add(valueFromDB);
                            }
                        }else if (day.equals("Samstag")){
                            if(day.equals(todayDay)){
                                sa.add(LocationTracking.stepCount);
                            }else{
                                sa.add(valueFromDB);
                            }
                        }else if (day.equals("Sonntag")){
                            if(day.equals(todayDay)){
                                so.add(LocationTracking.stepCount);
                            }else{
                                so.add(valueFromDB);
                            }
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










}
