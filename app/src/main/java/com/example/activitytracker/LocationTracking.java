package com.example.activitytracker;


import static android.app.AppOpsManager.MODE_ALLOWED;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
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

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;


public class LocationTracking extends Fragment implements SensorEventListener, OnMapReadyCallback {

    // to do: frage ob location wie lange da
    // abstände zwischen locations beachten
    // datenbank?



    public TextView counter;
    public Switch switchKM;
    public int currentSteps;
    public SensorManager sensorManager;
    public boolean running = false;
    public boolean permissionGranted;
    public MapView mapView;
    public GoogleMap googleMap;
    public LocationRequest locationRequest;
    public FusedLocationProviderClient locationProviderClient;
    public Location currentLocation;
    public static dbHelper db;
    public Button viewDB;
    public static String[] latitudeArray;
    public static String[] longitudeArray;
    public static String[] timestampsArray;
    public static String[] addressArray;

    public static Geocoder geocoder;

    public static List <Address> addresses;



    //public Cursor cursor;


    public LocationTracking() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_tracking, container, false);
        db = new dbHelper(getActivity().getApplicationContext());
        //db.deleteDataOlderThan24Hours();


        viewDB = view.findViewById(R.id.viewDB);

        counter = view.findViewById(R.id.counter_view);


        sensorManager = (SensorManager) getActivity().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        switchKM = view.findViewById(R.id.switch_to_km);
        currentSteps = Integer.parseInt(counter.getText().toString());
        switcherChanged(currentSteps);

        mapView = view.findViewById(R.id.mapView);
        checkPermission();

        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


        if (permissionGranted) {
            if (checkGoogleServices()) {
                mapView.getMapAsync(LocationTracking.this);
                mapView.onCreate(savedInstanceState);
                if (permissionGranted){
                    checkGPS();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Google Services not available", Toast.LENGTH_SHORT).show();
            }
        }else{
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
        }

        viewLocationsInDB();

        return view;
    }

    public static String getAddressFromLatLong(Double lat,Double lng,Context context) throws IOException {
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(lat, lng, 1);
        String address = addresses.get(0).getAddressLine(0);
        return "Addresse: " + address;
    }

    public static String realTime(Date d)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String result = sdf.format(d);
        Log.d("realTime", result);
        return result;
    }


    public static String realDate(Date d)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String result = sdf.format(d);
        Log.d("realDate", result);
        return result;
    }

    //nur zum testen was in db ist???

    private void viewLocationsInDB(){
        viewDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cursor res = db.getReadableDatabase().rawQuery("Select * from LocationDetails", null);
                //res.moveToFirst();
                Cursor cursor = db.getData();
                if(cursor.getCount()==0){
                    Toast.makeText(getActivity().getApplicationContext(), "empty DB", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while(cursor.moveToNext()){
                    buffer.append("ID: "+cursor.getString(0)+"\n");
                    buffer.append("Latitude: "+cursor.getString(1)+"\n");
                    buffer.append("Longitude: "+cursor.getString(2)+"\n");
                    buffer.append("Timestamp: "+cursor.getString(3)+"\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(LocationTracking.this.getContext());
                builder.setCancelable(true);
                builder.setTitle("Last Locations");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
    }

    public static void getDataFromDBInArrays(Context context){
        Cursor c = db.getData();
        latitudeArray =new String[c.getCount()];
        longitudeArray =new String[c.getCount()];
        timestampsArray = new String[c.getCount()];
        addressArray = new String[c.getCount()];

        while(c.moveToNext()){
            for (int i = 0; i < c.getCount(); i++) {
                latitudeArray[i] = c.getString(1);
                longitudeArray[i] = c.getString(2);
                timestampsArray[i] = c.getString(3);
                try {
                    addressArray[i] = getAddressFromLatLong(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2)),context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }





    //switcher von steps zu km
    private float counterToKM(int steps) {
        float stepsKM = (float) steps / 1400;
        return (float) (Math.round(stepsKM * 100) / 100.);
    }

    private void switcherChanged(int steps) {
        switchKM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    counter.setText(String.valueOf(counterToKM(steps)));
                } else {
                    counter.setText(String.valueOf(steps));
                }
            }
        });
    }

    //step sensor funktionen
    @Override
    public void onResume() {
        super.onResume();
        //mapView.onResume();

        running = true;
        Sensor countStepsSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countStepsSensor != null) {
            sensorManager.registerListener(this, countStepsSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Sensor not found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //mapView.onPause();

        running = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running) {
            counter.setText(String.valueOf(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //checkt location permission des users
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

    //checkt ob google services verfügbar
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


    //checkt ob gps am smartphone aktiviert ist
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


    //getcurrentLocation --> update db
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
                Date date = new Date(currentLocation.getTime());
                String currentTime = realTime(date);
                String currentDate = realDate(date);
                String timestamp = currentTime + " " + currentDate;
                Log.d("locTrack","Location: "+ currentLocation.getLatitude() +": "+ currentLocation.getLongitude());
                Boolean checkInsert = db.insertData(currentLocation.getLatitude(),currentLocation.getLongitude(), timestamp);
                getDataFromDBInArrays(getActivity().getApplicationContext());


                PolylineOptions options = new PolylineOptions();
                options.color(0xffff0000);
                options.width(5);


                Cursor cur = db.getData();
                while(cur.moveToNext()){
                    LatLng location = new LatLng(Double.parseDouble(cur.getString(1)), Double.parseDouble(cur.getString(2)));
                    options.add(location);
                }

                googleMap.addPolyline(options);

                Log.d("insertDB",checkInsert.toString());
                Log.d("insertDB", currentLocation.getLatitude() + " " + currentLocation.getLongitude() + " " +timestamp);



                if(checkInsert==true){
                    Toast.makeText(getActivity().getApplicationContext(), "new Location inserted in DB", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "no Location inserted in DB", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(getActivity().getApplicationContext(), "Location:"+locationResult.getLastLocation().getLongitude()+": "+locationResult.getLastLocation().getLatitude(), Toast.LENGTH_SHORT).show();

                try {
                    updateLocationListView(currentLocation,getActivity().getApplicationContext());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }, Looper.getMainLooper());
    }


    //update listview on homescreen --> dont add duplicates
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






    //google map funktion --> init map
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        //init map with zoom,compass etc.

        googleMap = map;
        LatLng latLng = new LatLng(49.015143, 12.101756);
        //LatLng latLng = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Current position");
        markerOptions.position(latLng);
        googleMap.addMarker(markerOptions);*/
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        googleMap.animateCamera(cameraUpdate);



        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);




    }





    //maps funktionen die evtl gar nicht nötig sind?
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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

