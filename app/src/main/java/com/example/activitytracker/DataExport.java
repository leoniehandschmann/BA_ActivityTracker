package com.example.activitytracker;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class DataExport extends Fragment{

    public static ListView locationListOverview;
    public static ArrayList<String> addressesFromDB;
    public static ArrayList<String> addressesListWODupli;
    public static ArrayAdapter <String> listViewAdapter2;
    private Button exportBtn;
    private screenTime_dbHelper screenTime_db;
    private steps_dbHelper steps_db;
    private TableLayout table_life;
    private TableLayout table_work;
    public static TextView stepsOverview;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_export_activity, container, false);
        locationListOverview = view.findViewById(R.id.locations_data_export);
        initLocationOverview();

        exportBtn = view.findViewById(R.id.export_btn);
        screenTime_db = new screenTime_dbHelper(getActivity().getApplicationContext());
        steps_db = new steps_dbHelper(getActivity().getApplicationContext());
        saveDataBtnClick();

        stepsOverview = view.findViewById(R.id.tv_steps_export_data);

        table_life = view.findViewById(R.id.overview_screenTime_table_life);
        table_work = view.findViewById(R.id.overview_screenTime_table_work);

        initScreenTimeOverview();

        return view;
    }

    //init overview of chosen work and life apps and usageTime --> update every 30 sec
    private void initScreenTimeOverview(){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    ScreenTimeTracking.setTableRows(ScreenTimeTracking.selectedPackages_Life,table_life,getActivity().getApplicationContext(),true);
                    ScreenTimeTracking.setTableRows(ScreenTimeTracking.selectedPackages_Work,table_work,getActivity().getApplicationContext(),true);
                }
                catch (Exception e) {
                    Log.d("updateTV",getString(R.string.log_no_success));
                }
                finally{
                    handler.postDelayed(this, 30000);
                }
            }
        };
        handler.postDelayed(runnable, 30000);
    }

    //on export btn click current app usage times and current steps saved in DBs
    private void saveDataBtnClick(){
        exportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveScreenTimeInDB();
                saveStepsInDB();
            }
        });

    }

    //method for saving usage time of each chosen app
    private void saveScreenTimeInDB(){
        for(int i=0; i <ScreenTimeTracking.selectedPackages_Life.size();i++){
            Boolean checkInsert = screenTime_db.insertData("life",ScreenTimeTracking.selectedPackages_Life.get(i),ScreenTimeTracking.getAppUsage(getActivity().getApplicationContext(),ScreenTimeTracking.selectedPackages_Life.get(i)));
            if(checkInsert==true){
                Toast.makeText(getActivity().getApplicationContext(), "new ScreenTime life inserted in DB", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), "nothing inserted in DB", Toast.LENGTH_SHORT).show();
            }
        }

        for(int i=0; i <ScreenTimeTracking.selectedPackages_Work.size();i++){
            Boolean checkInsert = screenTime_db.insertData("work",ScreenTimeTracking.selectedPackages_Work.get(i),ScreenTimeTracking.getAppUsage(getActivity().getApplicationContext(),ScreenTimeTracking.selectedPackages_Work.get(i)));
            if(checkInsert==true){
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.screentime_db_success), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.db_no_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //method for saving steps in DB
    private void saveStepsInDB(){

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c.getTime());

        Boolean checkInsert = steps_db.insertData(LocationTracking.stepCount,formattedDate);
        if(checkInsert==true){
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.steps_db_success), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.db_no_success), Toast.LENGTH_SHORT).show();
        }
    }


    //init Overview of all visited Locations
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
