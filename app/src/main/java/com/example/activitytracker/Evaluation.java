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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button submit_btn;
    private TextView tv;
    private LinearLayout parent;
    private ImageButton deleteBtn;
    private View custom;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        parent = (LinearLayout) inflater.inflate(R.layout.activity_evaluation, null);


        evaluation_db = new evaluation_dbHelper(getActivity().getApplicationContext());

        /*deleteBtn = custom.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("hihi","clickicklick");
            }
        });*/


        getLocationData();
        if(locWODupli.size() != 0){
            for (int i = 0; i < locWODupli.size(); i++) {
                custom = inflater.inflate(R.layout.seekbar_template, null);
                tv = (TextView) custom.findViewById(R.id.activity_name);
                tv.setText(locWODupli.get(i));
                parent.addView(custom);
            }
        }


        initBtn();
        SubmitAndSaveInDB();
        initDeleteBtn();

        return parent;
    }

    private void initBtn(){
        submit_btn = new Button(getActivity().getApplicationContext());
        submit_btn.setText("Submit");
        submit_btn.setAllCaps(true);
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        submit_btn.setLayoutParams(params);

        parent.addView(submit_btn);

    }

    private void initDeleteBtn(){
        LinearLayout mainLL = parent.findViewById(R.id.main_eval_layout);
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
                                        parent.removeView(view);
                                    }
                                });
                            }
                        }

                    }
                }

            }


        }

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


    private int getRealVal(int val){
        if(val == 0){
            val = -5;
        }else if( val == 1){
            val = -4;
        }else if( val == 2){
            val = -3;
        }else if( val == 3){
            val = -2;
        }else if( val == 4){
            val = -1;
        }else if( val == 5){
            val = 0;
        }else if( val == 6){
            val = 1;
        }else if( val == 7){
            val = 2;
        }else if( val == 8){
            val = 3;
        }else if( val == 9){
            val = 4;
        }else if( val == 10){
            val = 5;
        }
        return val;
    }

    private void SubmitAndSaveInDB(){
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout mainLL = parent.findViewById(R.id.main_eval_layout);

                for (int i = 0; i < mainLL.getChildCount(); i++) {
                    View view = mainLL.getChildAt(i);
                    if(view.getId() == R.id.evaluation_layout){
                        LinearLayout ll = (LinearLayout) view;
                        for(int j=0;j<ll.getChildCount();j++){
                            View view2 = ll.getChildAt(j);
                            if(view2 instanceof SeekBar){
                                evaluationVal = ((SeekBar) view2).getProgress();
                                evaluationVal = getRealVal(evaluationVal);
                                Log.d("handsch", String.valueOf(evaluationVal));
                            }else if(view2.getId() == R.id.activity_name_layout){
                                LinearLayout ll2 = (LinearLayout) view2;
                                for(int k=0;k<ll2.getChildCount();k++){
                                    View view3 = ll2.getChildAt(k);
                                    if(view3 instanceof TextView){
                                        activityName = (String) ((TextView) view3).getText();
                                        Log.d("handsch", activityName);
                                    }
                                }

                            }
                        }

                        Boolean checkInsert = evaluation_db.insertData(activityName,evaluationVal);
                        if(checkInsert==true){
                            Toast.makeText(getActivity().getApplicationContext(), "new Evaluation inserted in DB", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(), "nothing inserted in DB", Toast.LENGTH_SHORT).show();
                        }
                    }


                }

            }
        });


    }









}