package com.example.activitytracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HomeScreen extends Fragment {

    public static ListView locationList;
    public static ArrayList<String> addresses;
    public static ArrayList<String> addressesList;
    public static ArrayAdapter <String> listViewAdapter;
    private BarChart stepBarChart;

    ArrayList <BarEntry> barDataList;

    public HomeScreen() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen, container, false);
        locationList = view.findViewById(R.id.location_list);
        stepBarChart = view.findViewById(R.id.step_chart);

        initStepBarChart();
        initLocationListView();

        return view;
    }



    private void initStepBarChart(){

        BarDataSet barDataSet = new BarDataSet(getBarData(),"Steps BarChart");
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

        //check if db is empty
        // if db is not empty use db data
        // if db is empty check current date

        steps_dbHelper stepDB = new steps_dbHelper(getActivity().getApplicationContext());
        Cursor cursor = stepDB.getData();

        if(stepDB == null){
            Calendar c = Calendar.getInstance();
            String dayToday = android.text.format.DateFormat.format("EEEE", c).toString();
            if(dayToday == "Montag"){
                barDataList.add(new BarEntry(0.5f,10));
            }else if (dayToday == "Dienstag"){
                barDataList.add(new BarEntry(1.5f,20));
            } else if (dayToday == "Mittwoch"){
                barDataList.add(new BarEntry(2.5f,30));
            }else if (dayToday == "Donnerstag"){
                barDataList.add(new BarEntry(3.5f,40));
            }else if (dayToday == "Freitag"){
                barDataList.add(new BarEntry(4.5f,50));
            }else if (dayToday == "Samstag"){
                barDataList.add(new BarEntry(5.5f,60));
            }else if (dayToday == "Sonntag"){
                barDataList.add(new BarEntry(6.6f,70));
            }
        }else if (stepDB != null){

        }


        return barDataList;
    }









}
