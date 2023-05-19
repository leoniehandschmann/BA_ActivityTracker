package com.example.activitytracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
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
import android.widget.EditText;
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

    public static ArrayList<String> addressesFromDB;
    public static ArrayList<String> addressesListWODupli;
    public static ArrayAdapter <String> listViewAdapter2;
    private Button exportBtn;
    private screenTime_dbHelper screenTime_db;
    private steps_dbHelper steps_db;
    private TableLayout table_life;
    private TableLayout table_work;
    public static TextView stepsOverview;
    private annotations_dbHelper annotations_db;
    private EditText annotation_field;
    private ArrayList <String> latNotOld;
    private ArrayList <String> longNotOld;
    private TableLayout overview_time_locs_exp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_export_activity, container, false);
        overview_time_locs_exp = view.findViewById(R.id.overview_stayTime_locs_export);
        locDataNotOlderThan24();
        try {
            initLocationOverview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        exportBtn = view.findViewById(R.id.export_btn);
        screenTime_db = new screenTime_dbHelper(getActivity().getApplicationContext());
        steps_db = new steps_dbHelper(getActivity().getApplicationContext());
        annotation_field = view.findViewById(R.id.comment_export);
        annotations_db = new annotations_dbHelper(getActivity().getApplicationContext());
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
                    initTableStayTimeLocs(getActivity().getApplicationContext());
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
                saveAnnotations();
            }
        });

    }

    //method for saving usage time of each chosen app
    private void saveScreenTimeInDB(){
        for(int i=0; i <ScreenTimeTracking.selectedPackages_Life.size();i++){
            Boolean checkInsert = screenTime_db.insertData("life",ScreenTimeTracking.selectedPackages_Life.get(i),ScreenTimeTracking.getAppUsage(getActivity().getApplicationContext(),ScreenTimeTracking.selectedPackages_Life.get(i)));
            if(checkInsert==true){
                Toast.makeText(getActivity().getApplicationContext(), R.string.screentime_db_success, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity().getApplicationContext(), R.string.db_no_success, Toast.LENGTH_SHORT).show();
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

    private void saveAnnotations(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = df.format(c.getTime());

        Boolean checkInsert = annotations_db.insertData(String.valueOf(annotation_field.getText()),formattedDate);
        if(checkInsert==true){
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.annotations_db_success), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.db_no_success), Toast.LENGTH_SHORT).show();
        }

    }


    //init Overview of all visited Locations
    private void initLocationOverview() throws IOException {
        location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        addressesFromDB = new ArrayList<String>();
        addressesListWODupli = new ArrayList<String>();

        if(latNotOld!=null){
            for(int i =0;i<latNotOld.size();i++){
                addressesFromDB.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(latNotOld.get(i)),Double.parseDouble(longNotOld.get(i)), getActivity().getApplicationContext()));
            }
        }else {
            if(c.moveToFirst()){
                do{
                    try {
                        addressesFromDB.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)), getActivity().getApplicationContext()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        Set<String> set = new HashSet<>();
        for (String s : addressesFromDB) {
            if (set.add(s)) {
                addressesListWODupli.add(s);
            }
        }
    }

    private void initTableStayTimeLocs(Context c){
        overview_time_locs_exp.removeAllViews();

        for(int i = 0; i < LocationTracking.mapStayTime.size(); i++){
            TableRow tr = new TableRow(c);
            TextView t1 = new TextView(c);
            t1.setText(addressesListWODupli.get(i));
            t1.setTextSize(15);
            t1.setTypeface(null, Typeface.BOLD_ITALIC);
            t1.setPadding(50,100,0,0);
            tr.addView(t1);

            TextView t2 = new TextView(c);
            t2.setText("~ " + LocationTracking.mapStayTime.get(addressesListWODupli.get(i)) + " Min");
            t2.setGravity(Gravity.RIGHT);
            tr.addView(t2);
            overview_time_locs_exp.addView(tr);
        }
    }

    private void locDataNotOlderThan24 (){
        location_dbHelper db2 = new location_dbHelper(getActivity().getApplicationContext());
        latNotOld = new ArrayList<>();
        longNotOld = new ArrayList<>();


        Cursor curs = db2.getData();
        if (curs.moveToFirst()){
            do {

                if(!(Long.parseLong(curs.getString(3)) <= System.currentTimeMillis() - 60*60*24*1000)){
                    String column1 = curs.getString(1);
                    String column2 = curs.getString(2);
                    latNotOld.add(column1);
                    longNotOld.add(column2);
                }
            } while(curs.moveToNext());
        }
        curs.close();
        db2.close();
    }


}
