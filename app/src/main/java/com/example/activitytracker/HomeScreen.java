package com.example.activitytracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
    private steps_dbHelper stepsDB;

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

        if(stepsDB == null){
            initStepBarChart(LocationTracking.barDataList);
        }else{
            initStepBarChart(getBarData());
        }

        initLocationListView();

        return view;
    }



    private void initStepBarChart(ArrayList <BarEntry> data){

        BarDataSet barDataSet = new BarDataSet(data,"Steps BarChart");
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

        //getLocationsFromDBWODupli(getActivity().getApplicationContext(),addresses,addressesList);

        /*location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        db.getDataWODuplicates(getActivity().getApplicationContext(),addresses,addressesList);*/

        listViewAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,addressesList);


        locationList.setAdapter(listViewAdapter);
    }



    private ArrayList<BarEntry> getBarData(){
        barDataList = new ArrayList<BarEntry>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy", Locale.getDefault());
        String today = df.format(c.getTime());


        Cursor cursor = stepsDB.getData();
        while(cursor.moveToNext()){
            Integer valueFromDB = Integer.valueOf(cursor.getString(1));
            String dateFromDB = cursor.getString(2);

            //check if date in db is not older than 7 days
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate from = LocalDate.parse(dateFromDB);
                LocalDate to = LocalDate.parse(today);
                if( ChronoUnit.DAYS.between(from,to) <= 7 ){
                    String day = DateFormat.format("EEEE",Date.parse(dateFromDB)).toString();
                    if(day == "Montag"){
                        barDataList.add(new BarEntry(0.5f,valueFromDB));
                    }else if (day == "Dienstag"){
                        barDataList.add(new BarEntry(1.5f,valueFromDB));
                    } else if (day == "Mittwoch"){
                        barDataList.add(new BarEntry(2.5f,valueFromDB));
                    }else if (day == "Donnerstag"){
                        barDataList.add(new BarEntry(3.5f,valueFromDB));
                        Log.d("leines", String.valueOf(valueFromDB));
                    }else if (day == "Freitag"){
                        barDataList.add(new BarEntry(4.5f,valueFromDB));
                    }else if (day == "Samstag"){
                        barDataList.add(new BarEntry(5.5f,valueFromDB));
                    }else if (day == "Sonntag"){
                        barDataList.add(new BarEntry(6.6f,valueFromDB));
                    }
                }
            }




        }

        return barDataList;
    }









}
