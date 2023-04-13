package com.example.activitytracker;

import static android.app.AppOpsManager.MODE_ALLOWED;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
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

    private TextView tv_choose_life_apps;
    private Long usageTimeMillis;
    private TableLayout appTable;
    private List<String> appsPackList;
    private List<Drawable> appsIconsList;
    private ArrayList<Integer> selectedAppsList;
    private List<String> selectedPackages;
    private boolean[] selectedLifeApps;

    public ScreenTimeTracking(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screentime_tracking, container, false);
        tv_choose_life_apps = view.findViewById(R.id.choose_life);
        appTable = view.findViewById(R.id.app_table);
        appsPackList = new ArrayList<>();
        appsIconsList = new ArrayList<>();
        selectedAppsList = new ArrayList<>();
        selectedPackages = new ArrayList<>();

        try {
            getinstalledApps();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        selectedLifeApps = new boolean[appsPackList.size()];
        initAppChooser(tv_choose_life_apps);

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
                setTableRows(selectedPackages);
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

    private void setTableRows(List<String> packageList) throws PackageManager.NameNotFoundException {
        for(int i = 0; i < packageList.size(); i++){
            TableRow tr = new TableRow(getActivity().getApplicationContext());
            Drawable icon = getActivity().getApplicationContext().getPackageManager().getApplicationIcon(packageList.get(i));
            ImageView imageView = new ImageView(getActivity().getApplicationContext());
            imageView.setImageDrawable(icon);
            imageView.setMaxHeight(1);
            imageView.setMaxWidth(1);

            tr.addView(imageView);
            TextView t1 = new TextView(getActivity().getApplicationContext());
            t1.setText(cutPackageName(packageList.get(i)));
            tr.addView(t1);

            TextView t2 = new TextView(getActivity().getApplicationContext());
            int seconds = (int) (getAppUsage(getActivity().getApplicationContext(),packageList.get(i)) / 1000) % 60 ;
            int minutes = (int) ((getAppUsage(getActivity().getApplicationContext(),packageList.get(i)) / (1000*60)) % 60);
            int hours   = (int) ((getAppUsage(getActivity().getApplicationContext(),packageList.get(i)) / (1000*60*60)) % 24);
            t2.setText(String.valueOf(hours) + ":" + String.valueOf(minutes) + ":" + String.valueOf(seconds));
            t2.setGravity(Gravity.RIGHT);
            tr.addView(t2);
            appTable.addView(tr);
        }

    }

    public void getinstalledApps() throws PackageManager.NameNotFoundException {

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
                    appsIconsList.add(pm.getApplicationIcon(app.packageName));
                } else {
                    // user installed apps
                    Log.d("appnam","user installed: " + app.packageName);
                    appsPackList.add(app.packageName);
                    appsIconsList.add(pm.getApplicationIcon(app.packageName));
                }
            }
        }
        Log.d("pooh", String.valueOf(appsPackList));

    }

    private String cutPackageName(String packName){
        if(packName.contains("com.google.android.")){
            return packName.substring(19);
        }else if (packName.contains("com.google.")) {
            return packName.substring(11);
        }else if (packName.contains("com.app.")){
            return packName.substring(8);
        }else if (packName.contains("com.")){
            return packName.substring(4);
        }else if (packName.contains("apps.")) {
            return packName.substring(5);
        }else if (packName.contains("com.google.android.apps.")) {
            return packName.substring(24);
        }else if (packName.contains("com.android.")) {
            return packName.substring(12);
        }else if (packName.contains("com.example.")) {
            return packName.substring(12);
        }
        return packName;
    }

    private void initAppChooser(TextView tv){
        String [] allAppNames = new String[appsPackList.size()];
        for(int i = 0; i < allAppNames.length; i++){
            allAppNames[i] = cutPackageName(appsPackList.get(i));
        }
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select Apps");
                builder.setCancelable(false);

                builder.setMultiChoiceItems(allAppNames, selectedLifeApps, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            selectedAppsList.add(which);
                        }else{
                            selectedAppsList.remove(which);
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for(int i = 0; i<selectedAppsList.size();i++){
                            stringBuilder.append(allAppNames[selectedAppsList.get(i)]);
                            selectedPackages.add(appsPackList.get(selectedAppsList.get(i)));
                            if(i != selectedAppsList.size()-1){
                                stringBuilder.append(", ");
                            }
                        }
                        tv.setText(stringBuilder.toString());
                        Log.d("diego", String.valueOf(selectedPackages));
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i=0;i<selectedLifeApps.length;i++){
                            selectedLifeApps[i] = false;
                            selectedAppsList.clear();
                            selectedPackages.clear();
                            tv.setText("");
                        }
                    }
                });
                builder.show();
            }
        });
    }

}
