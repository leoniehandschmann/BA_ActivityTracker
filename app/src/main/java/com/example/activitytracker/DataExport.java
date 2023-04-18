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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class DataExport extends Fragment{

    public static ListView locationListOverview;
    public static ArrayList<String> addressesFromDB;
    public static ArrayList<String> addressesListWODupli;
    public static ArrayAdapter <String> listViewAdapter2;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_export_activity, container, false);
        locationListOverview = view.findViewById(R.id.locations_data_export);
        initLocationOverview();

        return view;
    }


    private void initLocationOverview(){
        location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        addressesFromDB = new ArrayList<String>();
        addressesListWODupli = new ArrayList<String>();


        if(c.moveToFirst()){
            do{
                try {
                    addressesFromDB.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)), getActivity().getApplicationContext()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
        }

        Set<String> set = new HashSet<>();
        for (String s : addressesFromDB) {
            if (set.add(s)) {
                addressesListWODupli.add(s);
            }
        }

        listViewAdapter2 = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1,addressesListWODupli);


        locationListOverview.setAdapter(listViewAdapter2);
    }



}
