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
    //public dbHelper(Context context) {super(context, "LocationDataDB", null, 1);}
    public dbHelper(Context context) {
        super(context, "testDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //LocationDetails
        db.execSQL("create Table test(activity_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude DOUBLE, longitude DOUBLE, timestamp STRING)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //LocationDetails
        db.execSQL("drop Table if exists tests");
    }


    public Boolean insertData(double latitude,double longitude,String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("latitude",latitude);
        content.put("longitude",longitude);
        content.put("timestamp",timestamp);
        Log.d("dbhelper",content.toString());

        //LocationDetails
        long result = db.insert("test",null, content);
        return result != -1;

    }


    public Boolean deleteData(int id){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("Select * from LocationDetails where activity_id = ?",new String[]{"activity_id"});
        if(cursor.getCount()>1){

            long result = db.delete("LocationDetails","activity_id=?",new String[]{"activity_id"});
            return result != -1;

        }else{
            return false;
        }

    }


    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        //LocationDetails
        Cursor cursor = db.rawQuery("Select * from test", null);
        cursor.moveToFirst();
        return cursor;
    }


    public Boolean deleteDataOlderThan24Hours(){
        SQLiteDatabase db = this.getWritableDatabase();



        Cursor cursor = db.rawQuery("Select * from test where timestamp <= date('now','-1 day')",null);
        if(cursor.getCount()>1){

            long result = db.delete("test","timestamp <= date('now','-1 day')", null);
            return result != -1;

        }else{
            return false;
        }

    }







}
