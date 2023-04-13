package com.example.activitytracker;

import static android.app.AppOpsManager.MODE_ALLOWED;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScreenTimeTracking extends Fragment{

    private TextView tv_screenTime;
    private TextView firstCell;
    private TextView secCell;
    private Long usageTimeMillis;
    private TableLayout appTable;
    private List<String> appsPackList;

    public ScreenTimeTracking(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screentime_tracking, container, false);
        tv_screenTime = view.findViewById(R.id.tv_usage);
        appTable = view.findViewById(R.id.app_table);
        appsPackList = new ArrayList<>();

        getinstalledApps();

        if(checkUsagePermission(getActivity().getApplicationContext())){
            updateAppUsage();
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


    private void updateAppUsage() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    int count = appTable.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = appTable.getChildAt(i);
                        if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                    }
                setTableRows();
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

    private long getAppUsage(Context c,String appPackage){
        UsageStatsManager usageStatsManager = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        long beginIntervalMillis = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000;
        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(beginIntervalMillis,System.currentTimeMillis());
        usageTimeMillis = usageStatsMap.get(appPackage).getTotalTimeInForeground();
        return usageTimeMillis;
    }

    private void setTableRows(){
        for(int i = 0; i < appsPackList.size(); i++){
            Log.d("maus", String.valueOf(appsPackList.size()));
            Log.d("maus",appsPackList.get(i));
            TableRow tr = new TableRow(getActivity().getApplicationContext());
            TextView t1 = new TextView(getActivity().getApplicationContext());
            t1.setText(cutPackageName(appsPackList.get(i)));
            tr.addView(t1);
            TextView t2 = new TextView(getActivity().getApplicationContext());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(getAppUsage(getActivity().getApplicationContext(),appsPackList.get(i)));
            long seconds = TimeUnit.MILLISECONDS.toSeconds(getAppUsage(getActivity().getApplicationContext(),appsPackList.get(i))) - TimeUnit.MINUTES.toSeconds(minutes);
            t2.setText(String.valueOf(minutes) + " Minuten  " + String.valueOf(seconds) + " Sekunden");
            t2.setGravity(Gravity.RIGHT);
            tr.addView(t2);
            appTable.addView(tr);
        }

    }

    public void getinstalledApps() {

        PackageManager pm = getActivity().getApplicationContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_GIDS);


        for (ApplicationInfo app : apps) {
            if(pm.getLaunchIntentForPackage(app.packageName) != null) {
                // apps with launcher intent
                if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                // updated system apps
                Log.d("appnam","updated system: " + app.packageName);
                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    // system apps
                    Log.d("appnam","system: " + app.packageName);
                    appsPackList.add(app.packageName);
                } else {
                    // user installed apps
                    Log.d("appnam","user installed: " + app.packageName);
                    appsPackList.add(app.packageName);
                }
            }
        }
        Log.d("pooh", String.valueOf(appsPackList.size()));

    }

    private String cutPackageName(String packName){
        if(packName.contains("com.google.android.")){
            return packName.substring(19);
        }else if (packName.contains("com.google.")) {
            return packName.substring(11);
        }else if (packName.contains("com.app")){
            return packName.substring(8);
        }else if (packName.contains("com.")){
            return packName.substring(4);
        }else if (packName.contains("apps.")) {
            return packName.substring(5);
        }
        return packName;
    }


}
