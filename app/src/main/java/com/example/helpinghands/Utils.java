package com.example.helpinghands;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {
    private static final String TAG = "Helper Utils";
    private static final String serverKey = "AAAAaJLZpX0:APA91bGefpRpVCrPFtF0UF3kQu4ZXEdRp-Rqu-H2MzNtHcm5JgL6JEHzo8JuA6FSw5kWm-pdGqAhVfGj1jVDkGOmPxAQ-PtZ6m4H8kduGJ0wIyu2JA1wbIZKBX26bb489aEmC6Nx04gE";
    private static final String fcmUrl = "https://fcm.googleapis.com/fcm/send";
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

    public static void noInternetConnectionAlert(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("No Internet Connection");
        builder.setMessage(
                "Internet Connection is required to perform this task.");
        builder.setNegativeButton("Ok", (dialog, which) -> {});
        builder.show();
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
        String locality = myAddress.getLocality();
        String previousLocality = myUser.getLocaleCity();
        myUser.setLocaleCity(locality);
        FirebaseMessaging.getInstance().subscribeToTopic(locality)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to " + locality;
                        if (!task.isSuccessful()) {
                            msg = "Subscribe to " + locality + " failed";
                        }
                        Log.d(TAG, msg);
//                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
        });
        if (!previousLocality.equals("") && !locality.equals(previousLocality)){
            FirebaseMessaging.getInstance().unsubscribeFromTopic(previousLocality)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Unsubscribed to " + previousLocality;
                            if (!task.isSuccessful()) {
                                msg = "Unsubscribe to " + previousLocality + " failed";
                            }
                            Log.d(TAG, msg);
//                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
            });
        }
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

    static void sendFcmNotifications(
            Activity activity, String receiver, JSONObject jsonNotification){
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", receiver);
            jsonObject.put("data", jsonNotification);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, fcmUrl, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(activity, "Success sending notification: "+ response, Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(activity, "Error sending notification: "+ error, Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String > header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + serverKey);
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
