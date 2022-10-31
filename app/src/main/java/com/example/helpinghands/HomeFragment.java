package com.example.helpinghands;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;


public class HomeFragment extends Fragment {

    private User user;
    private ImageButton e911Btn;
    private Activity activity;
    private ToggleButton sosBtn;
    private ImageButton broadcastRequestBtn;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                makeCall("911");
            }
        });
        sosBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(
                    activity,"This feature is coming in next version.",
                    Toast.LENGTH_SHORT).show();
        });
        broadcastRequestBtn.setOnClickListener(v -> {
            Toast.makeText(
                    activity,"This feature is coming in next version.",
                    Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}