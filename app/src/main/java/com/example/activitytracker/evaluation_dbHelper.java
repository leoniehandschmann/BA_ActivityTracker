package com.example.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class evaluation_dbHelper extends SQLiteOpenHelper {



    public evaluation_dbHelper(Context context) {
        super(context, "EvaluationDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table evaluation(activity_id INTEGER PRIMARY KEY AUTOINCREMENT, activity STRING, evaluation INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists evaluation");
    }


    public Boolean insertData(String activity,int evaluation){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("activity",activity);
        content.put("evaluation",evaluation);
        Log.d("dbhelper",content.toString());

        long result = db.insert("evaluation",null, content);
        return result != -1;

    }



    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from evaluation", null);
        cursor.moveToFirst();
        return cursor;
    }










}
