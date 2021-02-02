package com.example.lavaggiostrade;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Math.sqrt;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, View.OnClickListener {

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    static private Location originLocation;
    private String near;
    private int notificationId = 1;

    private Button button;

    public static Location getLocation() {
        return originLocation;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("Message", "Ciao!");


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        String[] parts = near.split(",");
        String s = parts[0];
        String e = parts[1];
        String d = parts[2];
        String w = parts[3];

        s = s.replace("Start=", "");
        e = e.replace("End=", "");
        d = d.replace("Day=", "");
        w = w.replace("Week=", "");


        int hourStart = Integer.parseInt(s) - 2; //Allarme due ore prima
        ArrayList<Integer> day = new ArrayList<>();
        ArrayList<Integer> week = new ArrayList<>();
        if (d.contains("Dom")) {
            day.add(1);
        }
        if (d.contains("Lun")) {
            day.add(2);
        }
        if (d.contains("Mar")) {
            day.add(3);
        }
        if (d.contains("Mer")) {
            day.add(4);
        }
        if (d.contains("Gio")) {
            day.add(5);
        }
        if (d.contains("Ven")) {
            day.add(6);
        }
        if (d.contains("Sab")) {
            day.add(7);
        }
        if (w.contains("1")) {
            week.add(1);
        }
        if (w.contains("2")) {
            week.add(2);
        }
        if (w.contains("3")) {
            week.add(3);
        }
        if (w.contains("4")) {
            week.add(4);
        }
        for (int mese = Calendar.getInstance().get(Calendar.MONTH); mese < 12; mese++) {
            for (Integer we : week) {
                for (Integer da : day) {
                    if (Calendar.getInstance().get(Calendar.WEEK_OF_MONTH) < we || Calendar.getInstance().get(Calendar.MONTH) != mese) {
                        final int id = (int) System.currentTimeMillis();
                        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_ONE_SHOT);
                        Calendar startTime = Calendar.getInstance();
                        startTime.set(Calendar.MONTH, mese);
                        startTime.set(Calendar.WEEK_OF_MONTH, we);
                        startTime.set(Calendar.DAY_OF_WEEK, da);
                        startTime.set(Calendar.HOUR_OF_DAY, hourStart);
                        startTime.set(Calendar.MINUTE, 0);
                        startTime.set(Calendar.SECOND, 0);
                        long alarmStartTime = startTime.getTimeInMillis();
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);
                    }
                }
            }
        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, "Alert active!", duration);
        toast.show();
    }

    @Override
    @SuppressWarnings("MissingPermission")
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        button = (Button) findViewById(R.id.button);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        findViewById(R.id.setBtn).setOnClickListener(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity2();
            }
        });
    }

    public void openMainActivity2() {
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        enableLocation();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MarkerOptions options = new MarkerOptions();
                double dist = Double.POSITIVE_INFINITY;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String[] parts = snapshot1.getKey().split("-");
                    String part1 = parts[0].replace(",", ".");
                    String part2 = parts[1].replace(",", ".");
                    options.position(new LatLng(Float.parseFloat(part1), Float.parseFloat(part2)));
                    String title = snapshot1.getValue().toString().replace("{", "");
                    title = title.replace("}", "");
                    double meter = sqrt((originLocation.getLatitude() - Float.parseFloat(part1)) * (originLocation.getLatitude() - Float.parseFloat(part1)) + (originLocation.getLongitude() - Float.parseFloat(part2)) * (originLocation.getLongitude() - Float.parseFloat(part2)));
                    if (meter <= dist) {
                        dist = meter;
                        near = title;
                    }
                    title = title.replace("=", " ");
                    title = title.replace("Start", "Ora inizio:");
                    title = title.replace("End", "Ora fine:");
                    title = title.replace("Week", "Numero settimana:");
                    title = title.replace("Lun", "Lunedì");
                    title = title.replace("Mar", "Martedì");
                    title = title.replace("Mer", "Mercoledì");
                    title = title.replace("Gio", "Giovedì");
                    title = title.replace("Ven", "Venerdì");
                    title = title.replace("Sab", "Sabato");
                    title = title.replace("Dom", "Domenica");
                    title = title.replace("Day", "Giorno:");
                    options.title(title);
                    mapboxMap.addMarker(options);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //present toast or dialog
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13.0));
    }

}