package com.example.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class steps_dbHelper extends SQLiteOpenHelper {

    public steps_dbHelper(Context context) {
        super(context, "StepsDB", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table steps(id INTEGER PRIMARY KEY AUTOINCREMENT, steps FLOAT, date STRING)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists steps");
    }


    public Boolean insertData(float steps,String date){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("steps",steps);
        content.put("date",date);

        long result = db.insert("steps",null, content);
        return result != -1;

    }



    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from steps", null);
        cursor.moveToFirst();
        return cursor;
    }





}
