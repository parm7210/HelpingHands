package com.example.helpinghands;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utils {
    private static final String TAG = "Helper Utils";
    public static boolean checkInternetStatus(){
        boolean status = false;
        try {
            final String command = "ping -c 1 google.com";
            status = (Runtime.getRuntime().exec(command).waitFor() == 0);
            Log.v(TAG, "Network Status: " + status);
        } catch (Exception e) {
            Log.e(TAG,"Network Error: "+e.toString());
        }
        return status;
    }

    public static LatLng locationFetch(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        final Location currentLoc;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        currentLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.v(TAG,"Last known location(NETWORK): " + currentLoc);
        if(currentLoc == null){ return  new LatLng(0,0); }
        else{return new LatLng(currentLoc.getLatitude(),currentLoc.getLongitude());}
    }

    public static void setUserLocation(Activity activity, FirebaseFirestore db, User myUser, LatLng myPosition){
        Log.v(TAG,"Setting User location: "+myPosition);
        myUser.setLatitude(Double.toString(myPosition.latitude));
        myUser.setLongitude(Double.toString(myPosition.longitude));
        db.collection("user_details").document(myUser.getUserid())
                .update("latitude",myPosition.latitude, "longitude",myPosition.longitude)
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Error updating location in the database.", Toast.LENGTH_LONG).show();
        });
    }

    public static Address findLocality(Context context, LatLng myPosition){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address myAddress = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(
                    myPosition.latitude, myPosition.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                myAddress = addressList.get(0);
                Log.v(TAG,"Finding Locality: "+myAddress.getLocality());
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to connect to Geocoder(locality error)", e);
        }
        return myAddress;
    }

    public static void updateLocality(FirebaseFirestore db, User myUser,Address myAddress){
        myUser.setLocaleCity(myAddress.getLocality());
        Log.v(TAG,"Setting Locality: "+myAddress.getLocality());
        db.collection("user_details").document(myUser.getUserid()).update(
                "localeCity",myAddress.getLocality());
    }

    static boolean isBGServiceRunning(Activity activity, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isBGServiceRunning", "true");
                return true;
            }
        }
        Log.i ("isBGServiceRunning", "false");
        return false;
    }
}
