package com.example.activitytracker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Evaluation extends Fragment{

    private ArrayList<String> locationsFromDB;
    private ArrayList<String> locWODupli;

    private evaluation_dbHelper evaluation_db;
    private String activityName;
    private int evaluationVal;

    private TextView tv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        evaluation_db = new evaluation_dbHelper(getActivity().getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        LinearLayout parent = (LinearLayout) inflater.inflate(R.layout.activity_evaluation, null);



        getLocationData();
        if(locWODupli.size() != 0){
            for (int i = 0; i < locWODupli.size(); i++) {
                View custom = inflater.inflate(R.layout.seekbar_template, null);
                tv = (TextView) custom.findViewById(R.id.activity_name);
                tv.setText(locWODupli.get(i));
                parent.addView(custom);
            }
        }

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        Button submit_btn = new Button(getActivity().getApplicationContext());
        submit_btn.setText("Submit");
        submit_btn.setAllCaps(true);
        submit_btn.setLayoutParams(params);

        parent.addView(submit_btn);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Boolean checkInsert = evaluation_db.insertData(tv.getText(),);

                LinearLayout mainLL = parent.findViewById(R.id.main_eval_layout);
                //LinearLayout ll = parent.findViewById(R.id.evaluation_layout);
                for (int i = 0; i < mainLL.getChildCount(); i++) {
                    View view = mainLL.getChildAt(i);
                    if(view.getId() == R.id.evaluation_layout){
                        LinearLayout ll = (LinearLayout) view;
                        for(int j=0;j<ll.getChildCount();j++){
                            View view2 = ll.getChildAt(j);
                            if(view2 instanceof SeekBar){
                                evaluationVal = ((SeekBar) view2).getProgress();
                            }else if(view instanceof TextView && view.getId() == R.id.activity_name){
                                activityName = (String) ((TextView) view).getText();
                            }
                        }

                    }

                    Log.d("handsch", String.valueOf(evaluationVal));
                    Log.d("handsch", activityName);

                }




            }
        });

        /*for (int i = 0; i < 3; i++) {
            View custom = inflater.inflate(R.layout.seekbar_template, null);
            parent.addView(custom);
        }*/



        return parent;
    }

    private void getLocationData(){
        location_dbHelper db = new location_dbHelper(getActivity().getApplicationContext());
        Cursor c = db.getData();
        locationsFromDB = new ArrayList<String>();
        locWODupli = new ArrayList<String>();


        if(c.moveToFirst()){
            do{
                try {
                    locationsFromDB.add(LocationTracking.getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)), getActivity().getApplicationContext()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
        }

        Set<String> set = new HashSet<>();
        for (String s : locationsFromDB) {
            if (set.add(s)) {
                locWODupli.add(s);
            }
        }
    }
}