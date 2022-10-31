package com.example.helpinghands;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final User user = new User(SplashActivity.this);
        if(user.getFName() == ""){
            Log.v("Init","Checking Initially");
            Intent in = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(in);
        }
        else{
            Intent in = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(in);
        }
    }
}