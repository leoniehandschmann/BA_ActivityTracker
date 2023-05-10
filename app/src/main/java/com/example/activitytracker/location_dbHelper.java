package com.example.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class location_dbHelper extends SQLiteOpenHelper {
    //public dbHelper(Context context) {super(context, "LocationDataDB", null, 1);}

    public String dbName;



    public location_dbHelper(Context context) {
        super(context, "LocationDB", null, 1);
        //this.dbName = dbName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //LocationDetails
        db.execSQL("create Table locations(activity_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude DOUBLE, longitude DOUBLE, timestamp STRING)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //LocationDetails
        db.execSQL("drop Table if exists locations");
    }


    public Boolean insertData(double latitude,double longitude,String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("latitude",latitude);
        content.put("longitude",longitude);
        content.put("timestamp",timestamp);
        Log.d("dbhelper",content.toString());

        //LocationDetails
        long result = db.insert("locations",null, content);
        return result != -1;

    }



    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        //LocationDetails
        Cursor cursor = db.rawQuery("Select * from locations", null);
        cursor.moveToFirst();
        return cursor;
    }


    public Boolean getDataNotOlderThan24H(){
        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.rawQuery("SELECT ALL locations where timestamp <= date('now','-1 days')",null);
        if(cursor.getCount()>1){

            long result = db.delete("locations","timestamp <= date('now','-1 days')", null);
            return result != -1;

        }else{
            return false;
        }

    }








}
