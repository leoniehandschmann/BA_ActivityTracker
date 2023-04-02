package com.example.activitytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import android.app.Fragment;

import androidx.fragment.app.Fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    final Fragment fragment1 = new HomeScreen();
    final Fragment fragment2 = new LocationTracking();
    final Fragment fragment3 = new ScreenTimeTracking();
    final Fragment fragment4 = new Evaluation();
    final Fragment fragment5 = new DataExport();
    final androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragment1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //setupNavigationView();

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.rootLayout, fragment5, "5").hide(fragment5).commit();
        fm.beginTransaction().add(R.id.rootLayout, fragment4, "4").hide(fragment4).commit();
        fm.beginTransaction().add(R.id.rootLayout, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.rootLayout, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.rootLayout,fragment1, "1").commit();



    }

    private final BottomNavigationView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home_nav:
                    fm.beginTransaction().hide(active).show(fragment1).commit();
                    active = fragment1;
                    return true;

                case R.id.location_nav:
                    fm.beginTransaction().hide(active).show(fragment2).commit();
                    active = fragment2;
                    return true;

                case R.id.screen_activity_nav:
                    fm.beginTransaction().hide(active).show(fragment3).commit();
                    active = fragment3;
                    return true;

                case R.id.evaluation_nav:
                    fm.beginTransaction().hide(active).show(fragment4).commit();
                    active = fragment4;
                    return true;

                case R.id.export_nav:
                    fm.beginTransaction().hide(active).show(fragment5).commit();
                    active = fragment5;
                    return true;
            }
            return false;
        }
    };


}