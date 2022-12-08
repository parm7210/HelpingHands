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
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils{
    private static final String TAG = "Helper Utils";
    private static final String serverKey = "AAAAZEPRIeY:APA91bHx8ZzLLO9X20u0B98WEwPmn0RuYK8DJaMiiAVvqDrQHFgzku9Hn20eWcMY5d6BJHc28cPIpArk7Oy6gfSBzPk5M_8lDb2NYf1T2x2SA5KUN7fLMtGllbl5x0EuxzV8w6oz00HR";
    private static final String fcmUrl = "https://fcm.googleapis.com/fcm/send";
    public static boolean checkInternetStatus(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        return nInfo != null && nInfo.isConnected();
    }

    public static void noInternetConnectionAlert(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.No_Internet_Connection));
        builder.setMessage(context.getString(R.string.Internet_Connection_is_required));
        builder.setNegativeButton(context.getString(R.string.Ok), (dialog, which) -> {});
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

    public static void setUserLocation(Context activity, FirebaseFirestore db, User myUser, LatLng myPosition){
        Log.v(TAG,"Setting User location: "+myPosition);
        myUser.setLatitude(Double.toString(myPosition.latitude));
        myUser.setLongitude(Double.toString(myPosition.longitude));
        if (myUser.getUserid().equals("")){
            Log.v(TAG, "Found Null user ID");
            return;
        }
        db.collection("user_details").document(myUser.getUserid())
                .update("latitude",myPosition.latitude, "longitude",myPosition.longitude)
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, R.string.Error_updating_location_in_the_database, Toast.LENGTH_LONG).show();
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
        } catch (Exception e) {
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
                    Toast.makeText(activity, activity.getString(R.string.Success_sending_notification)+ response, Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(activity, activity.getString(R.string.Error_sending_notification)+ error, Toast.LENGTH_LONG).show();
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
