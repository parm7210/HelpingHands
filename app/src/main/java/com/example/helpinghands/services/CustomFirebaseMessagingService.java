package com.example.helpinghands.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.helpinghands.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
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
            Log.e("FirebaseMessaging", "User does not exist.");
        }
    }
}
