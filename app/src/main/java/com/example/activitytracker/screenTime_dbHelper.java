package com.example.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class screenTime_dbHelper extends SQLiteOpenHelper {


    public screenTime_dbHelper(Context context) {
        super(context, "ScreenTimeDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table screenTimes(activity_id INTEGER PRIMARY KEY AUTOINCREMENT, appName STRING, usageTime DOUBLE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists screenTimes");
    }


    public Boolean insertData(String appName,Double usageTime){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("appName",appName);
        content.put("usageTime",usageTime);
        Log.d("dbhelper",content.toString());

        long result = db.insert("screenTimes",null, content);
        return result != -1;

    }



    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from screenTimes", null);
        cursor.moveToFirst();
        return cursor;
    }








}
