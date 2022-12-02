package com.example.helpinghands;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestItemView extends Dialog {

    RequestItem requestItem;
    Button okBtn;

    public RequestItemView(@NonNull Context context, RequestItem requestItem) {
        super(context);
        this.requestItem = requestItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.request_item_view);
        ((TextView) findViewById(R.id.itemRequestId)).setText(requestItem.getRequestId());
        ((TextView) findViewById(R.id.itemCity)).setText(requestItem.getCity());
        ((TextView) findViewById(R.id.itemLatitude)).setText(requestItem.getLatitude());
        ((TextView) findViewById(R.id.itemLongitude)).setText(requestItem.getLongitude());
        ((TextView) findViewById(R.id.itemStatus)).setText(requestItem.getStatus());
        ((TextView) findViewById(R.id.itemTimestamp)).setText(requestItem.getTimestamp().toDate().toString());
        ((TextView) findViewById(R.id.itemVolunteerNo)).setText(requestItem.getVolunteerNo());
        okBtn = findViewById(R.id.itemOkBtn);
        okBtn.setOnClickListener(v -> {
            dismiss();
        });
    }
}