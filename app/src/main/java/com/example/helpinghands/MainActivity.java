package com.example.helpinghands;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.findLocality;
import static com.example.helpinghands.Utils.isBGServiceRunning;
import static com.example.helpinghands.Utils.locationFetch;
import static com.example.helpinghands.Utils.setUserLocation;
import static com.example.helpinghands.Utils.updateLocality;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.helpinghands.services.BGLocationUpdateService;
import com.example.helpinghands.services.BGLocationUpdateServiceRestart;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private static final String TAG = "Main Activity";
    static  LatLng currentPosition;
    static FirebaseFirestore db;
    static Address address;
    static User user;
    Intent bgLocationServiceIntent;

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Log.v("position","NewMethod Here");
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("FragmentName")){
                String str = extras.getString("FragmentName");
                Log.v("position","NewMethod" + str);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Location permission granted");
                getLocation();
                Intent in = new Intent(this, MainActivity.class);
                startActivity(in);
                this.overridePendingTransition(0, 0);
            } else {
                Log.v(TAG, "Location permission rejected");
            }
        }
    }

    public void getLocation(){

        if(checkInternetStatus()){
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
            else{
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    currentPosition = locationFetch(getApplicationContext());
                    assert currentPosition != null;
                    if(currentPosition.latitude == 0){

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                locationManager.removeUpdates(this);
                                Log.v(TAG,"current location: "+location);
                                currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
                                setUserLocation(MainActivity.this, db, user, currentPosition);
                                address = findLocality(getApplicationContext(), currentPosition);
                                updateLocality(db, user, address);
                            }
                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {}
                            @Override
                            public void onProviderEnabled(String provider) {}
                            @Override
                            public void onProviderDisabled(String provider) {}
                        });
                    }
                    else{
                        setUserLocation(MainActivity.this, db, user, currentPosition);
                        address = findLocality(getApplicationContext(), currentPosition);
                        updateLocality(db, user, address);
                    }

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (currentPosition.latitude == location.getLatitude() && currentPosition.longitude == location.getLongitude()) {
                                Log.v(TAG, "Location updated (SAME VALUE)");
                            } else {
                                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                setUserLocation(MainActivity.this, db, user, currentPosition);
                                Log.v(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                            }
                        }
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}
                        @Override
                        public void onProviderEnabled(String provider) {}
                        @Override
                        public void onProviderDisabled(String provider) {}
                    });
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(
                R.id.bottomNavigationView);
        // Passing each menu ID in the set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.mapFragment, R.id.requestsFragment,
                R.id.profileFragment
        ).build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        NavigationUI.setupActionBarWithNavController(
                this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        String FragmentName = getIntent().getStringExtra("FragmentName");
        Log.v("position","First" + FragmentName);

        db = FirebaseFirestore.getInstance();
        user = new User(this);
        this.runOnUiThread(this::getLocation);

        Log.v(TAG, "Starting background service");
        if (!isBGServiceRunning(this, BGLocationUpdateService.class)) {
            bgLocationServiceIntent = new Intent(this, BGLocationUpdateService.class);
            startService(bgLocationServiceIntent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(bgLocationServiceIntent);

    }
}