package com.example.activitytracker;


import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class LocationTracking extends Fragment implements OnMapReadyCallback {


    public TextView counter;
    public TextView counterKM;
    private double magnitudePrev = 0;
    public static Integer stepCount = 0;
    public Switch switchKM;
    public boolean permissionGranted;
    public MapView mapView;
    public GoogleMap googleMap;
    public LocationRequest locationRequest;
    public FusedLocationProviderClient locationProviderClient;
    public Location currentLocation;
    public static location_dbHelper location_db;
    public Button viewDB;
    public static Geocoder geocoder;
    public static List <Address> addresses;



    public LocationTracking() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_tracking, container, false);
        location_db = new location_dbHelper(getActivity().getApplicationContext());

        viewDB = view.findViewById(R.id.viewDB);

        counter = view.findViewById(R.id.counter_view);
        counterKM = view.findViewById(R.id.counter_view2);
        switchKM = view.findViewById(R.id.switch_to_km);


        initStepCounter();
        switcherChanged(stepCount);

        mapView = view.findViewById(R.id.mapView);
        checkPermission();

        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


        if (permissionGranted) {
            if (checkGoogleServices()) {
                mapView.getMapAsync(LocationTracking.this);
                mapView.onCreate(savedInstanceState);
                if (permissionGranted) {
                    checkGPS();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Google Services not available", Toast.LENGTH_SHORT).show();
            }
        }

        viewLocationsInDB();

        return view;
    }


    //init step sensor and counter
    private void initStepCounter(){
        SensorManager sensorManager = (SensorManager) getActivity().getApplicationContext().getSystemService(getContext().SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                if(System.currentTimeMillis() == cal.getTimeInMillis()){
                    stepCount = 0;
                };
                if(event != null){
                    float x_acceleration = event.values[0];
                    float y_acceleration = event.values[1];
                    float z_acceleration = event.values[2];

                    double magnitude = Math.sqrt(x_acceleration * x_acceleration + y_acceleration * y_acceleration + z_acceleration * z_acceleration);
                    double magnitudeDelta = magnitude - magnitudePrev;
                    magnitudePrev = magnitude;

                    if(magnitudeDelta > 6){
                        stepCount++;
                    }
                    counter.setText(stepCount.toString());
                    DataExport.stepsOverview.setText(stepCount.toString());
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(stepDetector,sensor,SensorManager.SENSOR_DELAY_NORMAL);

    }



    //get address from Lat and Long Coordinates
    public static String getAddressFromLatLong(Double lat,Double lng,Context context) throws IOException {
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(lat, lng, 1);
        String address = addresses.get(0).getAddressLine(0);
        return address;
    }

    //transfer millis in date to real time
    public static String realTime(long millis)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        Date d = new Date(millis);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String result = sdf.format(d);
        return result;
    }


    //transfer millis in date to real date
    public static String realDate(long millis)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
        Date d = new Date(millis);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String result = sdf.format(d);
        return result;
    }

    //View locations button
    private void viewLocationsInDB(){
        viewDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = location_db.getData();
                if(cursor.getCount()==0){
                    Toast.makeText(getActivity().getApplicationContext(), "empty DB", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while(cursor.moveToNext()){
                    buffer.append("ID: "+cursor.getString(0)+"\n");
                    buffer.append("Latitude: "+cursor.getString(1)+"\n");
                    buffer.append("Longitude: "+cursor.getString(2)+"\n");
                    buffer.append("Timestamp: "+realDate(Long.parseLong(cursor.getString(3))) + " " + realTime(Long.parseLong(cursor.getString(3)))+"\n\n");
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(LocationTracking.this.getContext());
                builder.setCancelable(true);
                builder.setTitle("Last Locations");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
    }


    //steps to km
    private float counterToKM(int steps) {
        float stepsKM = (float) steps / 1400;
        return (float) (Math.round(stepsKM * 100) / 100.);
    }

    //switcher listener for changing steps to km
    private void switcherChanged(int steps) {
        switchKM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    counter.setVisibility(View.GONE);
                    counterKM.setVisibility(View.VISIBLE);
                    counterKM.setText(String.valueOf(counterToKM(stepCount)));
                }else {
                    counterKM.setVisibility(View.GONE);
                    counter.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //step sensor functions
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getActivity().getPreferences(getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount",stepCount);
        editor.apply();
    }

    public void onStop() {
        super.onStop();
        mapView.onStop();

        SharedPreferences sharedPreferences = getActivity().getPreferences(getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount",stepCount);
        editor.apply();
    }

    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getActivity().getPreferences(getContext().MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("stepCount",0);
    }

    //check location permission
    private void checkPermission() {
        Dexter.withContext(getActivity().getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                permissionGranted = true;
                Toast.makeText(getActivity().getApplicationContext(), "Permission for Maps granted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getApplicationContext().getPackageName(), "");
                i.setData(uri);
                startActivity(i);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    //check google services
    private boolean checkGoogleServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int result = apiAvailability.isGooglePlayServicesAvailable(getActivity().getApplicationContext());
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (apiAvailability.isUserResolvableError(result)) {
            Dialog d = apiAvailability.getErrorDialog(getActivity(), result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(getActivity().getApplicationContext(), "User canceled dialog", Toast.LENGTH_SHORT).show();
                }
            });
            d.show();
        }

        return false;
    }


    //check if gps on smartphone is available + send location request every 2 min
    private void checkGPS() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setSmallestDisplacement(1000);
        //jede min --> 60*1000 millisec
        //alle 10 min --> 600.000 oder 60*10.000 millisec
        //alle 30 min --> 1.800.000 oder 60*30.000 millisec
        locationRequest.setInterval(60*30000);
        locationRequest.setFastestInterval(60*2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(getActivity().getApplicationContext()).checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    getCurrentLocationUpdate();
                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(getActivity(), 101);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        Toast.makeText(getActivity().getApplicationContext(), "Setting not available", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }


    //save current location in db + add polyline to maps + update Location Lists in other Fragments
    private void getCurrentLocationUpdate() {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();

                Boolean checkInsert = location_db.insertData(currentLocation.getLatitude(),currentLocation.getLongitude(), System.currentTimeMillis());

                //add polyline from db locations to map
                PolylineOptions options = new PolylineOptions();
                options.color(0xffff0000);
                options.width(5);
                Cursor cur = location_db.getData();
                while(cur.moveToNext()){
                    LatLng location = new LatLng(Double.parseDouble(cur.getString(1)), Double.parseDouble(cur.getString(2)));
                    options.add(location);
                }
                googleMap.addPolyline(options);


                if(checkInsert==true){
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.loc_db_success), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.db_no_success), Toast.LENGTH_SHORT).show();
                }
                try {
                    updateLocationListView(currentLocation,getActivity().getApplicationContext());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }, Looper.getMainLooper());
    }


    //update listview on homescreen and data export --> dont add duplicates to list
    public static void updateLocationListView(Location loc,Context context) throws IOException {
        String checkLoc = getAddressFromLatLong(loc.getLatitude(), loc.getLongitude(), context);
        if(!HomeScreen.addressesList.contains(checkLoc)) {
            try {
                HomeScreen.addressesList.add(getAddressFromLatLong(loc.getLatitude(), loc.getLongitude(), context));
                DataExport.addressesListWODupli.add(getAddressFromLatLong(loc.getLatitude(), loc.getLongitude(), context));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HomeScreen.listViewAdapter.notifyDataSetChanged();
        HomeScreen.locationList.invalidateViews();
        HomeScreen.locationList.refreshDrawableState();

        DataExport.listViewAdapter2.notifyDataSetChanged();
        DataExport.locationListOverview.refreshDrawableState();
    }




    //init google map
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

    }



   //required maps functions
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

