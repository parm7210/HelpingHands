package com.example.helpinghands.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.helpinghands.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessaging";
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
                    Toast.makeText(CustomFirebaseMessagingService.this, "Message data payload: " + remoteMessage.getData(), Toast.LENGTH_LONG).show();

                }
            });
        }
    }
}
