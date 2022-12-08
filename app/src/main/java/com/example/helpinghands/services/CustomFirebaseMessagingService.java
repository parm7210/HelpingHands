package com.example.helpinghands.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.helpinghands.MainActivity;
import com.example.helpinghands.R;
import com.example.helpinghands.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessaging";

    static double toRadians(double angleIn10thOfaDegree) {
        return (angleIn10thOfaDegree * Math.PI) / 180;
    }

    static double distance(LatLng point1, LatLng point2){
        double lon1 = toRadians(point1.longitude);
        double lon2 = toRadians(point2.longitude);
        double lat1 = toRadians(point1.latitude);
        double lat2 = toRadians(point2.latitude);
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371;
        return (c * r);
    }

    public static void notifyUser(Context context, String title, String message, String channelName){

        Date currentTime = Calendar.getInstance().getTime();
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"notify_001")
                .setSmallIcon(R.drawable.ic_helpinghands)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(notifyPendingIntent);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "notify_001";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(channel);
        builder.setChannelId(channelId);

        nm.notify(Integer.parseInt(currentTime.getMinutes()+""+currentTime.getSeconds()), builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User myUser = new User(this);
        if (!myUser.getUserid().equals("")) {
            db.collection("user_details")
                    .document(myUser.getUserid()).update("firebaseToken", token);
        }
        else {
            Log.e(TAG, "User does not exist.");
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    User myUser = new User(getApplicationContext());
                    LatLng userLatLng = new LatLng(Double.parseDouble(myUser.getLatitude()), Double.parseDouble(myUser.getLongitude()));
                    JSONObject jsonObject = new JSONObject(remoteMessage.getData());
                    User user = new User(CustomFirebaseMessagingService.this);
                    try {
                        String type = jsonObject.getString("Type");
                        switch (type) {
                            case "ERequest":
                                if(!jsonObject.getString("userId").equals(user.getUserid()) && user.getType() == 1) {
                                    LatLng latLng = new LatLng(Double.parseDouble(jsonObject.getString("latitude")), Double.parseDouble(jsonObject.getString("longitude")));
                                    if(distance(latLng, userLatLng) < 2.5){
                                            notifyUser(getApplicationContext(), getString(R.string.incoming_request), getString(R.string.someone_within_your_area_needs_your_help), "Incoming Request Notification");
                                    }
//                                    Toast.makeText(CustomFirebaseMessagingService.this, "Message data payload: " + jsonObject.toString(), Toast.LENGTH_LONG).show();
                                }
                                break;
                            case "RequestAccept":
                                myUser.setVolunteerId(jsonObject.getString("vid"));
                                notifyUser(getApplicationContext(), getString(R.string.help_is_coming), getString(R.string.a_volunteer_in_your_area_has_accepted_your_request), "Incoming Help Notification");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });
        }
    }
}
