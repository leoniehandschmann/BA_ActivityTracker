package com.example.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class dbHelper extends SQLiteOpenHelper {
    public dbHelper(Context context) {
        super(context, "LocationDataDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table LocationDetails(activity_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude DOUBLE, longitude DOUBLE, timestamp STRING)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists LocationDetails");
    }


    public Boolean insertData(double latitude,double longitude,String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("latitude",latitude);
        content.put("longitude",longitude);
        content.put("timestamp",timestamp);
        Log.d("dbhelper",content.toString());

        long result = db.insert("LocationDetails",null, content);
        if(result == -1){
            return false;
        }else{
            return true;
        }

    }


    public Boolean deleteData(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select * from LocationDetails where activity_id = ?",new String[]{"activity_id"});
        if(cursor.getCount()>1){

            long result = db.delete("LocationDetails","activity_id=?",new String[]{"activity_id"});
            if(result == -1){
                return false;
            }else{
                return true;
            }

        }else{
            return false;
        }

    }


    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from LocationDetails", null);
        cursor.moveToFirst();
        return cursor;
    }





}
