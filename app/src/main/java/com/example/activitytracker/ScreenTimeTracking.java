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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class ScreenTimeTracking extends Fragment{

    private TextView tv_choose_life_apps;
    private TextView tv_choose_work_apps;
    private TableLayout appTable_Life;
    private TableLayout appTable_Work;
    private List<String> appsPackList;
    private List<Drawable> appsIconsList;
    private ArrayList<Integer> selectedAppsList_Life;
    public static List<String> selectedPackages_Life;
    private boolean[] selectedApps_Life;
    private ArrayList<Integer> selectedAppsList_Work;
    public static List<String> selectedPackages_Work;
    private boolean[] selectedApps_Work;
    public static screenTime_dbHelper screenTime_db;

    public ScreenTimeTracking(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.screentime_tracking, container, false);
        init(view);
        checkPermission();

        try {
            getinstalledApps();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        initAppChooser(tv_choose_life_apps,selectedApps_Life,selectedAppsList_Life,selectedPackages_Life);

        initAppChooser(tv_choose_work_apps,selectedApps_Work,selectedAppsList_Work,selectedPackages_Work);

        return view;
    }

    //init all views and lists
    private void init(View view){
        tv_choose_life_apps = view.findViewById(R.id.choose_life);
        tv_choose_work_apps = view.findViewById(R.id.choose_work);
        appTable_Life = view.findViewById(R.id.app_table_life);
        appTable_Work = view.findViewById(R.id.app_table_work);
        appsPackList = new ArrayList<>();
        appsIconsList = new ArrayList<>();
        selectedAppsList_Life = new ArrayList<>();
        selectedPackages_Life = new ArrayList<>();
        selectedAppsList_Work = new ArrayList<>();
        selectedPackages_Work = new ArrayList<>();
        screenTime_db = new screenTime_dbHelper(getActivity().getApplicationContext());
        selectedApps_Life = new boolean[appsPackList.size()];
        selectedApps_Work = new boolean[appsPackList.size()];
    }

    //check permission for app usage time
    private void checkPermission(){
        if(checkUsagePermission(getActivity().getApplicationContext())){
            updateAppUsage();
        }else{
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private boolean checkUsagePermission(Context c){
        AppOpsManager appOps = (AppOpsManager)
                c.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), c.getPackageName());
        return mode == MODE_ALLOWED;
    }


    //check app usage time every 30 sec and update listviews
    private void updateAppUsage() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    setTableRows(selectedPackages_Life,appTable_Life,getActivity().getApplicationContext(),true);
                    setTableRows(selectedPackages_Work,appTable_Work,getActivity().getApplicationContext(),true);
                }
                catch (Exception e) {
                    Log.d("updateTV",getString(R.string.log_no_success));
                }
                finally{
                    handler.postDelayed(this, 30000);
                }
            }
        };
        handler.postDelayed(runnable, 30000);

    }

    //get app usage time and format
    public static long getAppUsage(Context c,String appPackage){
        UsageStatsManager usageStatsManager = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(cal.getTimeInMillis(),System.currentTimeMillis());
        long usageTimeMillis = usageStatsMap.get(appPackage).getTotalTimeInForeground();
        return usageTimeMillis;
    }

    //set table layout rows
    public static void setTableRows(List<String> packageList,TableLayout table,Context c,Boolean initLogo) throws PackageManager.NameNotFoundException {

        table.removeAllViews();

        for(int i = 0; i < packageList.size(); i++){
            TableRow tr = new TableRow(c);
            if(initLogo){
                Drawable icon = c.getPackageManager().getApplicationIcon(packageList.get(i));
                ImageView imageView = new ImageView(c);
                imageView.setImageDrawable(icon);
                tr.addView(imageView);
            }
            TextView t1 = new TextView(c);
            t1.setText(cutPackageName(packageList.get(i)));
            t1.setTextSize(15);
            t1.setPadding(50,100,0,0);
            tr.addView(t1);

            TextView t2 = new TextView(c);
            t2.setText(calcMillis(getAppUsage(c,packageList.get(i))));
            t2.setGravity(Gravity.RIGHT);
            tr.addView(t2);
            table.addView(tr);
        }

    }

    //transfer millis
    public static String calcMillis(Long millis){
        int seconds = (int) (millis / 1000) % 60 ;
        int minutes = (int) (millis / (1000*60)) % 60;
        int hours   = (int) (millis / (1000*60*60)) % 24;
        return hours + ":" +minutes + ":" + seconds;
    }

    //get all installed apps on smartphone
    public void getinstalledApps() throws PackageManager.NameNotFoundException {

        PackageManager pm = getActivity().getApplicationContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_GIDS);

        for (ApplicationInfo app : apps) {
            if(pm.getLaunchIntentForPackage(app.packageName) != null) {
                // apps with launcher intent
                if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                // updated system apps
                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    // system apps
                    appsPackList.add(app.packageName);
                    Drawable dr = pm.getApplicationIcon(app.packageName);
                    Bitmap bitmap = getBitmapFromDrawable(dr);
                    Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 1, 1, true));
                    appsIconsList.add(d);
                } else {
                    // user installed apps
                    appsPackList.add(app.packageName);
                    Drawable dr = pm.getApplicationIcon(app.packageName);
                    Bitmap bitmap = getBitmapFromDrawable(dr);
                    Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 1, 1, true));
                    appsIconsList.add(d);
                }
            }
        }

    }


    //get app icon
    private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    //cut package name of app to just app name
    public static String cutPackageName(String packName){
        if (packName.contains("com.google.android.apps.")) {
            return packName.substring(24);
        }else if(packName.contains("com.google.android.")) {
            return packName.substring(19);
        }else if (packName.contains("com.android.")) {
                return packName.substring(12);
        }else if (packName.contains("com.example.")) {
            return packName.substring(12);
        }else if (packName.contains("com.google.")) {
            return packName.substring(11);
        }else if (packName.contains("com.app.")){
            return packName.substring(8);
        }else if (packName.contains("com.")){
            return packName.substring(4);
        }else if (packName.contains("apps.")) {
            return packName.substring(5);
        }
        return packName;
    }

    //init chooser for work and life apps
    private void initAppChooser(TextView tv,boolean[]selectedApps,ArrayList<Integer> selectedAppsList,List<String> selectedPackages){
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

                builder.setMultiChoiceItems(allAppNames, selectedApps, new DialogInterface.OnMultiChoiceClickListener() {
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
                            if(!selectedPackages.contains(appsPackList.get(selectedAppsList.get(i)))){
                                selectedPackages.add(appsPackList.get(selectedAppsList.get(i)));
                                if(i != selectedAppsList.size()-1){
                                    stringBuilder.append(", ");
                                }
                            }
                        }
                        tv.setText(stringBuilder.toString());
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
                        for(int i=0;i<selectedApps.length;i++){
                            selectedApps[i] = false;
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
