package com.example.activitytracker;

import static android.app.AppOpsManager.MODE_ALLOWED;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScreenTimeTracking extends Fragment{

    private TextView tv_screenTime;
    private Long usageTimeMillis;

    public ScreenTimeTracking(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screentime_tracking, container, false);
        tv_screenTime = view.findViewById(R.id.tv_usage);

        if(checkUsagePermission(getActivity().getApplicationContext())){
            showAppUsage();
        }else{
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }


        return view;
    }

    private boolean checkUsagePermission(Context c){
        AppOpsManager appOps = (AppOpsManager)
                c.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), c.getPackageName());
        return mode == MODE_ALLOWED;
    }


    private void showAppUsage() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    getAppUsage(getActivity().getApplicationContext());
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(usageTimeMillis);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(usageTimeMillis)
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    tv_screenTime.setText(String.valueOf(minutes) + " Minuten  " + String.valueOf(seconds) + " Sekunden");
                }
                catch (Exception e) {
                    Log.d("updateTV","not successful");
                }
                finally{
                    handler.postDelayed(this, 30000);
                }
            }
        };
        handler.postDelayed(runnable, 30000);

    }

    private long getAppUsage(Context c){
        UsageStatsManager usageStatsManager = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        long beginIntervalMillis = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000;
        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(beginIntervalMillis,System.currentTimeMillis());
        usageTimeMillis = usageStatsMap.get("com.google.android.calendar").getTotalTimeInForeground();
        return usageTimeMillis;
    }


}
