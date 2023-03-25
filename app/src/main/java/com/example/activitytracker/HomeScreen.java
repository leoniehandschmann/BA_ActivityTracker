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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.util.ArrayList;

public class HomeScreen extends Fragment {

    ArrayList barDataList;

    public HomeScreen() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.homescreen, container, false);
        ListView locationList = (ListView) view.findViewById(R.id.location_list);
        dbHelper db = new dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        String addresses[] = new String[c.getCount()];

        while(c.moveToNext()){
            for (int i = 0; i < c.getCount(); i++) {
                        try {
                            addresses[i] = LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
            }
        }

        //locationList.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,addresses));

        BarChart stepBarChart = (BarChart) view.findViewById(R.id.step_chart);
        getBarData();
        BarDataSet barDataSet = new BarDataSet(barDataList,"Steps BarChart");
        BarData barData = new BarData();
        stepBarChart.setData(barData);
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        stepBarChart.getDescription().setEnabled(true);

        return view;
    }

    private void getBarData(){
        barDataList = new ArrayList();
        barDataList.add(new BarEntry(2f,10));
        barDataList.add(new BarEntry(3f,20));
        barDataList.add(new BarEntry(4f,30));
        barDataList.add(new BarEntry(5f,40));
        barDataList.add(new BarEntry(6f,50));
        barDataList.add(new BarEntry(7f,60));
        barDataList.add(new BarEntry(8f,70));
    }





}
