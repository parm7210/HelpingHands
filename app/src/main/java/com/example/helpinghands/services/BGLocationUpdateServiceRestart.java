package com.example.helpinghands.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BGLocationUpdateServiceRestart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Restarter", "Service Tried to stop");
        context.startService(new Intent(context, BGLocationUpdateService.class));
    }
}
