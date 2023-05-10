package com.example.activitytracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class annotations_dbHelper extends SQLiteOpenHelper {



    public annotations_dbHelper(Context context) {
        super(context, "AnnotationsDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table annotations(activity_id INTEGER PRIMARY KEY AUTOINCREMENT, note STRING, timestamp STRING)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists annotations");
    }


    public Boolean insertData(String note,String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put("note",note);
        content.put("timestamp",timestamp);

        long result = db.insert("annotations",null, content);
        return result != -1;

    }



    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from annotations", null);
        cursor.moveToFirst();
        return cursor;
    }










}
