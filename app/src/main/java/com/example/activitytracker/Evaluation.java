package com.example.activitytracker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Inflater;

public class Evaluation extends Fragment{

    private ArrayList<String> locationsFromDB;
    private ArrayList<String> locWODupli;
    private evaluation_dbHelper evaluation_db;
    private String activityName;
    private int evaluationVal;
    private Button submit_btn;
    private TextView tv_loc;
    private TextView tv_life;
    private TextView tv_work;
    private LinearLayout parent;
    private View locationEvalView;
    private View screenTimeEvalView_life;
    private View screenTimeEvalView_work;
    private LinearLayout mainLL;
    private ArrayList <String> latNotOld;
    private ArrayList <String> longNotOld;
    private ScrollView scroll_main;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        parent = (LinearLayout) inflater.inflate(R.layout.activity_evaluation, null);
        scroll_main = parent.findViewById(R.id.scroll_main);
        mainLL = scroll_main.findViewById(R.id.main_eval_layout);
        evaluation_db = new evaluation_dbHelper(getActivity().getApplicationContext());
        locDataNotOlderThan24();

        //init location evaluations
        initLocations(inflater);

        //init app usage screenTime evaluations
        initUsageRows(inflater);

        //init all Buttons
        initDeleteBtn();
        initBtn();
        submitAndSaveInDB();

        return parent;
    }

    //load every 30 sec locations from db for evaluation in view(with template)
    private void initLocations(LayoutInflater infl) {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    mainLL.removeAllViews();
                    getLocationData();
                    if(locWODupli.size() != 0){
                        for (int i = 0; i < locWODupli.size(); i++) {
                            locationEvalView = infl.inflate(R.layout.seekbar_template, null);
                            tv_loc = (TextView) locationEvalView.findViewById(R.id.activity_name);
                            tv_loc.setText(locWODupli.get(i));
                            mainLL.addView(locationEvalView);
                        }
                    }

                }
                catch (Exception e) {
                    Log.d("updateTV", getString(R.string.log_no_success));
                }
                finally{
                    handler.postDelayed(this, 10000);
                }
            }
        };
        handler.postDelayed(runnable, 10000);
    }


    //load every 30 sec chosen Apps with UsageTime for evaluation in view (with template)
    private void initUsageRows(LayoutInflater inflater){
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    mainLL.removeView(screenTimeEvalView_life);
                    if(ScreenTimeTracking.selectedPackages_Life.size() != 0 ){
                        for (int i = 0; i < ScreenTimeTracking.selectedPackages_Life.size(); i++) {
                            screenTimeEvalView_life = inflater.inflate(R.layout.seekbar_template, null);
                            tv_life = (TextView) screenTimeEvalView_life.findViewById(R.id.activity_name);
                            String s = "App: " + ScreenTimeTracking.cutPackageName(ScreenTimeTracking.selectedPackages_Life.get(i)) + "\n" + "Heutige Nutzung: " + ScreenTimeTracking.calcMillis(ScreenTimeTracking.getAppUsage(getActivity().getApplicationContext(),ScreenTimeTracking.selectedPackages_Life.get(i)));
                            tv_life.setText(s);
                            mainLL.addView(screenTimeEvalView_life);
                        }
                    }
                    mainLL.removeView(screenTimeEvalView_work);
                    if(ScreenTimeTracking.selectedPackages_Work.size() != 0){
                        for (int i = 0; i < ScreenTimeTracking.selectedPackages_Work.size(); i++) {
                            screenTimeEvalView_work = inflater.inflate(R.layout.seekbar_template, null);
                            tv_work = (TextView) screenTimeEvalView_work.findViewById(R.id.activity_name);
                            String s = "App: " + ScreenTimeTracking.cutPackageName(ScreenTimeTracking.selectedPackages_Work.get(i)) + "\n" + "Heutige Nutzung: " + ScreenTimeTracking.calcMillis(ScreenTimeTracking.getAppUsage(getActivity().getApplicationContext(),ScreenTimeTracking.selectedPackages_Work.get(i)));
                            tv_work.setText(s);
                            mainLL.addView(screenTimeEvalView_work);
                        }
                    }
                    mainLL.removeView(submit_btn);
                    initBtn();
                    initDeleteBtn();
                    submitAndSaveInDB();


                }
                catch (Exception e) {
                    Log.d("updateTV",getString(R.string.log_no_success));
                }
                finally{
                    handler.postDelayed(this, 10000);
                }
            }
        };
        handler.postDelayed(runnable, 10000);
    }


    // init submit button
    private void initBtn(){
        submit_btn = new Button(getActivity().getApplicationContext());
        submit_btn.setText("Submit");
        submit_btn.setAllCaps(true);
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        submit_btn.setLayoutParams(params);

        mainLL.addView(submit_btn);

    }

    // init delete trash cans --> if location or app is redundant for user it is possible to delete from evaluation
    private void initDeleteBtn(){

        //activity evaluation layout
        for (int i = 0; i < mainLL.getChildCount(); i++) {
            View view = mainLL.getChildAt(i);
            //seekbar template layout
            if(view.getId() == R.id.evaluation_layout){
                LinearLayout ll = (LinearLayout) view;
                for(int j=0;j<ll.getChildCount();j++){
                    View view2 = ll.getChildAt(j);
                    //Linearlayout of activity name and delete BTN
                    if(view2.getId() == R.id.activity_name_layout){
                        LinearLayout ll2 = (LinearLayout) view2;
                        for(int k=0;k<ll2.getChildCount();k++){
                            View view3 = ll2.getChildAt(k);
                            //delete BTN
                            if(view3 instanceof ImageButton){
                                view3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mainLL.removeView(view);
                                    }
                                });
                            }
                        }

                    }
                }

            }


        }

    }


    //get location data from LocationDB --> without duplicate Locations
    private void getLocationData() throws IOException {
        location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        locationsFromDB = new ArrayList<String>();
        locWODupli = new ArrayList<String>();


        if(latNotOld!=null){
            for(int i =0;i<latNotOld.size();i++){
                locationsFromDB.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(latNotOld.get(i)),Double.parseDouble(longNotOld.get(i)), getActivity().getApplicationContext()));
            }
        }else {
            if(c.moveToFirst()){
                do{
                    try {
                        locationsFromDB.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)), getActivity().getApplicationContext()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        Set<String> set = new HashSet<>();
        for (String s : locationsFromDB) {
            if (set.add(s)) {
                locWODupli.add(s);
            }
        }
    }

    private void locDataNotOlderThan24 (){
        location_dbHelper db2 = new location_dbHelper(getActivity().getApplicationContext());
        latNotOld = new ArrayList<>();
        longNotOld = new ArrayList<>();


        Cursor curs = db2.getData();
        //Cursor curs = db2.getWritableDatabase().rawQuery("SELECT latitude,longitude FROM locations where timestamp < date('now','-24 hours') ", null);
        if (curs.moveToFirst()){
            do {

                if(!(Long.parseLong(curs.getString(3)) <= System.currentTimeMillis() - 60*60*24*1000)){
                    String column1 = curs.getString(1);
                    String column2 = curs.getString(2);
                    latNotOld.add(column1);
                    longNotOld.add(column2);
                }
                // Passing values
                // Do something Here with values
            } while(curs.moveToNext());
        }
        curs.close();
        db2.close();
    }


    // transfer value of scrollbar in wanted evaluation value --> evaluate from -3 to +3
    private int getRealVal(int val){
        if(val == 0){
            val = -3;
        }else if( val == 1){
            val = -2;
        }else if( val == 2){
            val = -1;
        }else if( val == 3){
            val = 0;
        }else if( val == 4){
            val = 1;
        }else if( val == 5){
            val = 2;
        }else if( val == 6){
            val = 3;
        }
        return val;
    }


    // click Listener for submit btn --> save evaluation in DB
    private void submitAndSaveInDB(){
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LinearLayout mainLL = parent.findViewById(R.id.main_eval_layout);
                LinearLayout mainLL = scroll_main.findViewById(R.id.main_eval_layout);

                for (int i = 0; i < mainLL.getChildCount(); i++) {
                    View view = mainLL.getChildAt(i);
                    if(view.getId() == R.id.evaluation_layout){
                        LinearLayout ll = (LinearLayout) view;
                        for(int j=0;j<ll.getChildCount();j++){
                            View view2 = ll.getChildAt(j);
                            if(view2 instanceof SeekBar){
                                evaluationVal = ((SeekBar) view2).getProgress();
                                evaluationVal = getRealVal(evaluationVal);
                            }else if(view2.getId() == R.id.activity_name_layout){
                                LinearLayout ll2 = (LinearLayout) view2;
                                for(int k=0;k<ll2.getChildCount();k++){
                                    View view3 = ll2.getChildAt(k);
                                    if(view3 instanceof TextView){
                                        activityName = (String) ((TextView) view3).getText();
                                    }
                                }

                            }
                        }

                        Boolean checkInsert = evaluation_db.insertData(activityName,evaluationVal);
                        if(checkInsert==true){
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.eval_db_success), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.db_no_success), Toast.LENGTH_SHORT).show();
                        }
                    }


                }

            }
        });


    }









}