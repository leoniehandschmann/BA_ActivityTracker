package com.example.activitytracker;

import static android.app.AppOpsManager.MODE_ALLOWED;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class ScreenTimeTracking extends Fragment{

    TextView tv_screenTime;
    public ScreenTimeTracking(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screentime_tracking, container, false);
        tv_screenTime = view.findViewById(R.id.tv_usage);

        if(checkUsagePermission(getActivity().getApplicationContext())){
            showAppUsage(getActivity().getApplicationContext());
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


    private void showAppUsage(Context c) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        long beginIntervalMillis = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000;
        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(beginIntervalMillis,System.currentTimeMillis());
        long totalTimeUsageInMillis = usageStatsMap.get("com.google.android.calendar").getTotalTimeInForeground();
        tv_screenTime.setText(String.valueOf(totalTimeUsageInMillis/60000));
    }


}
