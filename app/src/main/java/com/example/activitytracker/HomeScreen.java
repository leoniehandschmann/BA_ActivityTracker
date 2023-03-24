package com.example.activitytracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class HomeScreen extends Fragment {
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

        locationList.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,addresses));

        return view;
    }
}
