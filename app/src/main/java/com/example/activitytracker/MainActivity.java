package com.example.activitytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import android.app.Fragment;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.nio.Buffer;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    final Fragment fragment1 = new HomeScreen();
    final Fragment fragment2 = new LocationTracking();
    final Fragment fragment3 = new ScreenTimeTracking();
    final Fragment fragment4 = new Evaluation();
    final Fragment fragment5 = new DataExport();
    final androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragment1;
    private StringBuffer infoBuffer;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bitte erlaube der App alle notwendigen Permissions.", Toast.LENGTH_SHORT).show();
            MainActivity.this.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
            return;
        }
        createNotificationChannel();
        initReminderNotification();

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.rootLayout, fragment5, "5").hide(fragment5).commit();
        fm.beginTransaction().add(R.id.rootLayout, fragment4, "4").hide(fragment4).commit();
        fm.beginTransaction().add(R.id.rootLayout, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.rootLayout, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.rootLayout,fragment1, "1").commit();

        infoBuffer = new StringBuffer();
        builder = new AlertDialog.Builder(this);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_info_navigation_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Wir prüfen, ob Menü-Element mit der ID "action_daten_aktualisieren"
        // ausgewählt wurde und geben eine Meldung aus
        int id = item.getItemId();
        if (id == R.id.info_icon) {
            if(active == fragment1){
                infoBuffer.delete(0,infoBuffer.capacity());
                infoBuffer.append(getString(R.string.info_homescreen));
                makeInfoText(infoBuffer);
            }else if(active == fragment2){
                infoBuffer.delete(0,infoBuffer.capacity());
                infoBuffer.append(getString(R.string.info_location));
                makeInfoText(infoBuffer);
            }
            else if(active == fragment3){
                infoBuffer.delete(0,infoBuffer.capacity());
                infoBuffer.append(getString(R.string.info_screenTime));
                makeInfoText(infoBuffer);
            }
            else if(active == fragment4){
                infoBuffer.delete(0,infoBuffer.capacity());
                infoBuffer.append(getString(R.string.info_evaluation));
                makeInfoText(infoBuffer);
            }
            else if(active == fragment5){
                infoBuffer.delete(0,infoBuffer.capacity());
                infoBuffer.append(getString(R.string.info_export));
                makeInfoText(infoBuffer);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeInfoText(StringBuffer buffer){
        builder.setCancelable(true);
        builder.setTitle("Informationen über diese Seite");
        builder.setMessage(buffer.toString());
        builder.show();
    }

    //create Notification at 8pm
    private void initReminderNotification() {
        Intent intent = new Intent(MainActivity.this,NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,20);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pendingIntent);

    }

    //Navigationbar selected Listener
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

    //create NotificationChannel
    private void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = null;
            notificationChannel = new NotificationChannel("notifyDataExport",getString(R.string.notification_channel_name),importance);
            notificationChannel.setDescription(getString(R.string.notification_channel_descript));

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }


}