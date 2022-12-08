package com.example.helpinghands;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.noInternetConnectionAlert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EmergencyContactsActivity extends AppCompatActivity {

    private Spinner choice1;
    private EditText name1;
    private EditText contact1;
    private Spinner choice2;
    private EditText name2;
    private EditText contact2;
    private Spinner choice3;
    private EditText name3;
    private EditText contact3;
    private Button button;

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        choice1 = findViewById(R.id.spinner1);
        name1 = findViewById(R.id.eCon1Name);
        contact1 = findViewById(R.id.eCon1Num);
        choice2 = findViewById(R.id.spinner2);
        name2 = findViewById(R.id.eCon2Name);
        contact2 = findViewById(R.id.econ2Num);
        choice3 = findViewById(R.id.spinner3);
        name3 = findViewById(R.id.eCon3Name);
        contact3 = findViewById(R.id.eCon3Num);
        button = findViewById(R.id.eConSave);
        String options[] = new String[]{
                getString(R.string.parent), getString(R.string.child), getString(R.string.sibling), getString(R.string.Husband_Wife),getString(R.string.Relative),
                getString(R.string.Neighbour), getString(R.string.Friend), getString(R.string.Other)};
        ArrayAdapter adapter = new ArrayAdapter(
                this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choice1.setAdapter(adapter);
        choice2.setAdapter(adapter);
        choice3.setAdapter(adapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User user =new User(this);

        name1.setText(user.getEcon1name());
        if(user.getEcon1() == 0){contact1.setText("");}
        else{contact1.setText(user.getEcon1().toString());}
        choice1.setSelection(Arrays.asList(options).indexOf(user.getRel1()));

        name2.setText(user.getEcon2name());
        if(user.getEcon2() == 0){contact2.setText("");}
        else {contact2.setText(user.getEcon2().toString());}
        choice2.setSelection(Arrays.asList(options).indexOf(user.getRel2()));

        name3.setText(user.getEcon3name());
        if(user.getEcon3() == 0){contact3.setText("");}
        else {contact3.setText(user.getEcon3().toString());}
        choice3.setSelection(Arrays.asList(options).indexOf(user.getRel3()));

        button.setOnClickListener(v -> {
            boolean validation = true;
            Spinner[] choices = {choice1, choice2, choice3};
            EditText[] names = {name1, name2, name3};
            for (EditText name: names){
                if(name.getText().toString().length() == 0){
                    name.requestFocus();
                    name.setError(getString(R.string.Name_is_required));
                    validation = false;
                }
            }
            EditText[] contacts = {contact1, contact2, contact3};
            for (EditText contact: contacts){
                if(!contact.getText().toString().matches("[0-9]{10}")){
                    contact.requestFocus();
                    contact.setError(getString(R.string.Contact_number_is_required));
                    validation = false;
                }
            }
            if(validation){

                if(!checkInternetStatus(getApplicationContext())){
                    noInternetConnectionAlert(this);
                }
                else {

                    for (int i = 0; i < 3; i++){
                        Map<String, Object> eContact = new HashMap<>();
                        eContact.put("name", names[i].getText().toString());
                        eContact.put("contactNo", contacts[i].getText().toString());
                        eContact.put("relation", choices[i].getSelectedItem().toString());
                        db.collection("emergency_details")
                            .document(user.getUserid().toString())
                            .collection("contacts")
                            .document("eContact" + (i+1))
                            .update(eContact);

                    }

                    if (contact1.getText().toString().length() == 0) {
                        user.setEcon1(Long.parseLong("0"));
                    } else {
                        user.setEcon1(Long.parseLong(contact1.getText().toString()));
                    }
                    user.setEcon1name(name1.getText().toString());
                    user.setRel1(choice1.getSelectedItem().toString());

                    if (contact2.getText().toString().length() == 0) {
                        user.setEcon2(Long.parseLong("0"));
                    } else {
                        user.setEcon2(Long.parseLong(contact2.getText().toString()));
                    }
                    user.setEcon2name(name2.getText().toString());
                    user.setRel2(choice2.getSelectedItem().toString());

                    if (contact3.getText().toString().length() == 0) {
                        user.setEcon3(Long.parseLong("0"));
                    } else {
                        user.setEcon3(Long.parseLong(contact3.getText().toString()));
                    }
                    user.setEcon3name(name3.getText().toString());
                    user.setRel3(choice3.getSelectedItem().toString());
                    Toast.makeText(
                            EmergencyContactsActivity.this,
                            getString(R.string.EMERGENCY_CONTACTS_UPDATED_SUCCESSFULLY),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}