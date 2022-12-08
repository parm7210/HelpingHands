package com.example.helpinghands;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.noInternetConnectionAlert;
import static com.example.helpinghands.Utils.sendFcmNotifications;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {

    static FirebaseFirestore db;
    private User user;
    private ImageButton e911Btn;
    private Activity activity;
    private ToggleButton sosBtn;
    private ImageButton broadcastRequestBtn;
    private final String LOGNAME = "HomeFragment";
    int cntFlag= 0;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void alertContacts(User user) {
        int flag = 0;
        String message = getString(R.string.Emergency_Alert)+user.getFName()+" "+user.getLName()+getString(R.string.triggered_an_emergency_call)+user.getFName()+getString(R.string.listed_you_as_an_emergency_contact);
        Log.v(LOGNAME,"Sending emergency message: " + message);
        String lastLocation = "";
        Log.v(LOGNAME,"User LATLNG " + user.getLatitude());
        if(user.getLatitude() != "" && user.getLongitude() != ""){
            flag = 1;
            //https://www.google.com/maps/@43.44395,-80.5214339,18z
            //https://www.google.com/maps/?q=43.44395,-80.5214339
            lastLocation = getString(R.string.Last_known_location_of)+user.getFName()+":\n";
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
        Toast.makeText(getActivity(), R.string.Emergency_contacts_have_been_notified_via_text_message,Toast.LENGTH_SHORT).show();
    }

    public void initiateEmergency(User user){
        final User currUser = user;
        Thread sos = new Thread(){
            public void run(){
                Log.v("SOSThread","Thread Started");
                new CountDownTimer(20000,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if(currUser.getSOSflag() == 0)
                        {
                            this.cancel();
                            Log.v("SOSThread","Thread Ended");
                        }
                    }
                    @Override
                    public void onFinish() {
                        if(currUser.getLatitude() != "" && currUser.getLongitude() != ""){
                            String lastLocation = getString(R.string.Last_Known_Location_is_in_this_area);
                            lastLocation = lastLocation + "https://www.google.com/maps/q?"+currUser.getLatitude()+","+currUser.getLongitude();
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
                    Toast.makeText(activity, R.string.Permission_Granted, Toast.LENGTH_SHORT).show();
                    e911Btn.callOnClick();
                } else {
                    Toast.makeText(activity, R.string.Permission_Rejected, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case 122: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(LOGNAME,"SMS Permission provided.");
                    sosBtn.setChecked(false);
                    Toast.makeText(getActivity(), R.string.SMS_Permission_provided,Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(LOGNAME,"SMS Permission rejected.");
                    sosBtn.setChecked(false);
                    Toast.makeText(getActivity(), R.string.SMS_permission_is_not_provided_yet,Toast.LENGTH_SHORT).show();
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
        db = FirebaseFirestore.getInstance();
        e911Btn.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE},120);
            }
            else{
                makeCall("911");
            }
        });
        sosBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                    Log.v(LOGNAME, "User permission not provided. Asking to grant the permission");
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS},122);
                }
                else{
                    if(user.getEcon1() == 0 && user.getEcon2() == 0 && user.getEcon3() == 0){
                        sosBtn.setChecked(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setTitle(R.string.Emergency_contacts_not_found);
                        builder.setMessage(R.string.Emergency_contacts_have_not_been_provided_yet);
                        builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getContext(), EmergencyContactsActivity.class);
                                startActivity(intent);
                                ((Activity)getContext()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                            }
                        });
                        builder.show();
                    }
                    else{
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.Sending_Emergency_Message).setMessage(R.string.Waiting).setCancelable(false);
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
                                alert.setMessage(getString(R.string.Alerting_Emergency_Contacts_via_text_message)+(int)((millisUntilFinished+1000)/1000)+getString(R.string.sec));
                                if(cntFlag == 1){
                                    cntFlag=0;
                                    this.cancel();
                                    sosBtn.setChecked(false);
                                    Toast.makeText(getActivity(), R.string.SOS_Alert_is_stopped,Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(),R.string.SOS_Alert_is_stopped,Toast.LENGTH_SHORT).show();
                user.setSOSflag(0);
            }
        });
        broadcastRequestBtn.setOnClickListener(v -> {
            if(user.getLatitude() != "" && user.getLongitude() != "") {
                if (!checkInternetStatus(requireContext())) { noInternetConnectionAlert(requireActivity()); }
                else {
                    final User user = new User(requireActivity());
                    db.collection("emergency_requests").whereEqualTo("userId",user.getUserid()).whereEqualTo("status","Active").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.getResult().size() > 0){
                                Toast.makeText(getActivity(), R.string.Emergency_signal_is_already_started, Toast.LENGTH_SHORT).show();
                            }   
                            else {
                                Map<String, Object> ERequest = new HashMap<>();
                                ERequest.put("contactNo", user.getContactNumber());
                                ERequest.put("type", user.getType());
                                ERequest.put("status", "Active");
                                ERequest.put("volunteerID", "");
                                ERequest.put("volunteerNo", 0);
                                ERequest.put("latitude", user.getLatitude());
                                ERequest.put("longitude", user.getLongitude());
                                ERequest.put("localeCity", user.getLocaleCity());
                                ERequest.put("userId", user.getUserid());
                                ERequest.put("created", FieldValue.serverTimestamp());
                                db.collection("emergency_requests").add(ERequest).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(final DocumentReference documentReference) {
                                        ERequest.put("Type", "ERequest");
                                        JSONObject jsonObject = new JSONObject(ERequest);
                                        sendFcmNotifications(requireActivity(), "/topics/"+user.getLocaleCity(), jsonObject);
                                        Toast.makeText(getActivity(), R.string.Emergency_Request_Broadcasted, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });

                }
            }
            else{
                Intent in = new Intent(getActivity(),MainActivity.class);
                startActivity(in);
                ((Activity)getContext()).overridePendingTransition(0,0);
            }
        });

        return root;
    }
}