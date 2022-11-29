package com.example.helpinghands;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Objects;


public class HomeFragment extends Fragment {

    private User user;
    private ImageButton e911Btn;
    private Activity activity;
    private ToggleButton sosBtn;
    private ImageButton broadcastRequestBtn;
    private final String LOGNAME = "HomeFragment";
    int cntFlag= 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void alertContacts(User user) {
        int flag = 0;
        String message = "Emergency Alert\n"+user.getFName()+" "+user.getLName()+" has just triggered an emergency call. You are receiving this as "+user.getFName()+" has listed you as an emergency contact.";
        Log.v(LOGNAME,"Sending emergency message: " + message);
        String lastLocation = "";
        Log.v(LOGNAME,"User LATLNG " + user.getLatitude());
        if(user.getLatitude() != "" && user.getLongitude() != ""){
            flag = 1;
            //https://www.google.com/maps/@43.44395,-80.5214339,18z
            //https://www.google.com/maps/?q=43.44395,-80.5214339
            lastLocation = "Last known location of is in this area :\n";
            lastLocation = lastLocation + "https://www.google.com/maps/?q="+user.getLatitude()+","+user.getLongitude();
            Log.v(LOGNAME, "Message sent: " + lastLocation);
        }
        if(user.getEcon1() != 0){
            Log.v("status","sending message to "+user.getEcon1());
            sendSMS(user.getEcon1().toString(),message);
            Log.v("status","Message sent to "+user.getEcon1());
            if(flag == 1){sendSMS(user.getEcon1().toString(),lastLocation);}
        }
        if(user.getEcon2() != 0){
            Log.v("status","sending message to "+user.getEcon2());
            sendSMS(user.getEcon2().toString(),message);
            Log.v("status","Message sent to "+user.getEcon2());
            if(flag == 1){sendSMS(user.getEcon2().toString(),lastLocation);}
        }
        if(user.getEcon3() != 0){
            Log.v("status","sending message to "+user.getEcon3());
            sendSMS(user.getEcon3().toString(),message);
            Log.v("status","Message sent to "+user.getEcon3());
            if(flag == 1){sendSMS(user.getEcon3().toString(),lastLocation);}
        }
        Toast.makeText(getActivity(),"Emergency contacts have been notified via text message",Toast.LENGTH_SHORT).show();
    }

    public void initiateEmergency(User user){
        final User currUser = user;
        Thread sos = new Thread(){
            public void run(){
                Log.v("SOSThread","Thread Started");
                new CountDownTimer(5000,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if(currUser.getSOSflag() == 0){this.cancel();Log.v("SOSThread","Thread Ended");}
                    }
                    @Override
                    public void onFinish() {
                        if(currUser.getLatitude() != "" && currUser.getLongitude() != ""){
                            String lastLocation = "Last Known Location is in this area :\n";
                            lastLocation = lastLocation + "https://www.google.com/maps/@"+currUser.getLatitude()+","+currUser.getLongitude()+",18z";
                            Log.v(LOGNAME,"Sending Message = "+lastLocation);
                            if(currUser.getEcon1()!=0){sendSMS(currUser.getEcon1().toString(),lastLocation);}
                            if(currUser.getEcon2()!=0){sendSMS(currUser.getEcon2().toString(),lastLocation);}
                            if(currUser.getEcon3()!=0){sendSMS(currUser.getEcon3().toString(),lastLocation);}
                        }
                        Log.v(LOGNAME,"SOS Thread: SOS flag set as "+currUser.getSOSflag());
                        if(currUser.getSOSflag() == 1){this.start();}
                    }
                }.start();
            }
        };
        sos.run();
    }

    public void sendSMS(String number,String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
    }

    public void makeCall(String number){
        Intent callIntent =new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+number));
        startActivity(callIntent);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 120: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity,"Permission Granted", Toast.LENGTH_SHORT).show();
                    e911Btn.callOnClick();
                } else {
                    Toast.makeText(activity,"Permission Rejected", Toast.LENGTH_SHORT).show();
                }
            }

            case 122: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(LOGNAME,"SMS Permission provided.");
                    sosBtn.setChecked(false);
                    Toast.makeText(getActivity(),"SMS Permission provided. You can trigger emergency alert now.",Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(LOGNAME,"SMS Permission rejected.");
                    sosBtn.setChecked(false);
                    Toast.makeText(getActivity(),"SMS permission is not provided yet.",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        activity = requireActivity();
        user = new User(activity);
        e911Btn = root.findViewById(R.id.e911Btn);
        sosBtn = root.findViewById(R.id.triggerSOSBtn);
        broadcastRequestBtn = root.findViewById(R.id.broadcastRequestBtn);
        e911Btn.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE},120);
            }
            else{
                makeCall("2268996632");
            }
        });
        sosBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //Start

            if(isChecked){
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                    Log.v(LOGNAME, "User permission not provided. Asking to grant the permission");
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS},122);
                }
                else{
                    if(user.getEcon1() == 0 && user.getEcon2() == 0 && user.getEcon3() == 0){
                        sosBtn.setChecked(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle("Emergency contacts not found");
                        builder.setMessage("Emergency contacts have not been provided yet! Do you want to add Emergency contacts?");
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getContext(), EmergencyContactsActivity.class);
                                startActivity(intent);
                                ((Activity)getContext()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                return;
                            }
                        });
                        builder.show();
                    }
                    else{
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                                .setTitle("Sending Emergency Message").setMessage("Waiting...").setCancelable(false);
                        dialog.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        cntFlag = 1;
                                        dialog.dismiss();
                                    }
                                });
                        final AlertDialog alert = dialog.create();
                        alert.show();
                        new CountDownTimer(5000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                Log.v("status",(int)((millisUntilFinished+1000)/1000)+"");
                                alert.setMessage("Alerting Emergency Contacts via text message in "+(int)((millisUntilFinished+1000)/1000)+" sec...");
                                if(cntFlag == 1){
                                    cntFlag=0;
                                    this.cancel();
                                    sosBtn.setChecked(false);
                                    Toast.makeText(getActivity(),"SOS Alert is stopped",Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFinish() {
                                alert.dismiss();
                                cntFlag = 0;
                                alertContacts(user);
                                user.setSOSflag(1);
                                initiateEmergency(user);
                            }
                        }.start();
                    }
                }
            }
            else{
                Log.v(LOGNAME,"SOS Alert is stopped");
                Toast.makeText(getActivity(),"SOS Alert is stopped",Toast.LENGTH_SHORT).show();
                user.setSOSflag(0);
            }

            //End
        });
        broadcastRequestBtn.setOnClickListener(v -> {
            Toast.makeText(
                    activity,"This feature is coming in next version.",
                    Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}