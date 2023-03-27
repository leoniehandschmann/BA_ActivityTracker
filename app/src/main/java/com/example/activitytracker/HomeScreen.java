package com.example.activitytracker;

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
import java.util.ArrayList;
import java.util.logging.Logger;

public class HomeScreen extends Fragment {

    public static ListView locationList;
    public static ArrayList<String> addresses;
    public static ArrayAdapter <String> listViewAdapter;

    ArrayList <BarEntry> barDataList;

    public HomeScreen() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen, container, false);
        locationList = (ListView) view.findViewById(R.id.location_list);
        dbHelper db = new dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        addresses = new ArrayList<String>();
        listViewAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,addresses);


        /*while(c.moveToNext()){
            for (int i = 0; i < c.getCount(); i++) {
                        try {
                            addresses[i] = LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)));
                            listViewAdapter.notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
            }
        }*/

        if(c.moveToFirst()){
            do{
                try {
                    addresses.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
        }


        locationList.setAdapter(listViewAdapter);


        BarChart stepBarChart = (BarChart) view.findViewById(R.id.step_chart);

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


        return view;
    }

    private ArrayList<BarEntry> getBarData(){
        barDataList = new ArrayList<BarEntry>();
        barDataList.add(new BarEntry(0.5f,10));
        barDataList.add(new BarEntry(1.5f,20));
        barDataList.add(new BarEntry(2.5f,30));
        barDataList.add(new BarEntry(3.5f,40));
        barDataList.add(new BarEntry(4.5f,50));
        barDataList.add(new BarEntry(5.5f,60));
        barDataList.add(new BarEntry(6.6f,70));
        return barDataList;
    }





}
