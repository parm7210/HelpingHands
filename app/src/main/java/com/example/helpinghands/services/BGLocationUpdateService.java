package com.example.helpinghands.services;

import static androidx.constraintlayout.widget.Constraints.TAG;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.locationFetch;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.helpinghands.MainActivity;
import com.example.helpinghands.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class BGLocationUpdateService extends Service {
    static int counter;
    FirebaseFirestore db;

    @Override
    public void onCreate() {
        counter = 0;
        FirebaseApp.initializeApp(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build();
        db.setFirestoreSettings(settings);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.v("BackgroundService","Background service running");
        ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {
            int x = 0;
            while (true){
                Toast.makeText(getApplicationContext(), "Background Service" + x, Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                User user = new User(getApplicationContext());
                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                if (!user.getFName().equals("")
                        && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                        && checkInternetStatus()
                        && ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(
                        getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LatLng location = locationFetch(getApplicationContext());
                    if (location != null) {
                        user.setLatitude(Double.toString(location.latitude));
                        user.setLongitude(Double.toString(location.longitude));
                        db.collection("user_details").document(user.getUserid()).update("latitude", location.latitude, "longitude", location.longitude);
                        counter++;
                    }
                    else{Log.v("BackgroundService","Something went wrong");}
                }
            }
        });
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.v("BackgroundService","Destroyed");
        counter--;
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, BGLocationUpdateServiceRestart.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
