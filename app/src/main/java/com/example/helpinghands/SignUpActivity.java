package com.example.helpinghands;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstName;
    private EditText lastName;
    private EditText password;
    private EditText phone;
    private EditText email;
    private EditText age;
    private EditText addressLine1;
    private EditText addressLIne2;
    private EditText city;
    private EditText state;
    private EditText country;
    private EditText postalCode;
    private RadioButton gender;
    private RadioButton gender2;
    private ProgressBar progressBar;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    public boolean validateInputs(){
        boolean validation = true;
        if(!firstName.getText().toString().matches("[a-z A-Z]+")){
            firstName.requestFocus();
            firstName.setError("Please provide appropriate name");
            validation=false;
        }
        if(!lastName.getText().toString().matches("[a-z A-Z]+")){
            lastName.requestFocus();
            lastName.setError("Please provide appropriate name");
            validation=false;
        }
        if(!password.getText().toString().matches("[a-zA-Z0-9]{5}[a-zA-Z0-9]+")){
            password.requestFocus();
            password.setError("Password must be 6 characters long");
            validation=false;
        }
        if(!phone.getText().toString().matches("[0-9]{10}")){
            phone.requestFocus();
            phone.setError("Please provide 10 digit Mobile number");
            validation=false;
        }
        if(addressLine1.getText().toString().length() == 0){
            addressLine1.requestFocus();
            addressLine1.setError("Field is required");
            validation=false;
        }

        if(addressLIne2.getText().toString().length() == 0){
            addressLIne2.requestFocus();
            addressLIne2.setError("Field is required");
            validation=false;
        }
        if(city.getText().toString().length() == 0){
            city.requestFocus();
            city.setError("Field is required");
            validation=false;
        }
        if(state.getText().toString().length() == 0){
            state.requestFocus();
            state.setError("Field is required");
            validation=false;
        }
        if(country.getText().toString().length() == 0){
            country.requestFocus();
            country.setError("Field is required");
            validation=false;
        }
        if(postalCode.getText().toString().length() == 0){
            postalCode.requestFocus();
            postalCode.setError("Field is required");
            validation=false;
        }

        if(!email.getText().toString().matches("^\\w+@[a-zA-Z0-9_]+?\\.[a-zA-Z]{2,3}$")){
            email.requestFocus();
            email.setError("Please provide appropriate email-id");
            validation=false;
        }

        if(!age.getText().toString().matches("[1-9][0-9]")){
            age.requestFocus();
            age.setError("Please provide appropriate age");
            validation=false;
        }
        return validation;
    }

    public boolean checkUserExists(){
        final boolean[] returnVal = new boolean[1];
        db.collection("user_details").whereEqualTo(
                "contact_no", Long.parseLong(phone.getText().toString())).get()
                .addOnCompleteListener((task) -> {
                        if(task.isSuccessful()){
                            if(!task.getResult().isEmpty()){
                                final AlertDialog.Builder builder = new AlertDialog.Builder(
                                        SignUpActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("Account Already Exist");
                                builder.setMessage(
                                        "An account is already associated with given " +
                                        "contact number.");
                                builder.setPositiveButton(
                                        "Login", (DialogInterface dialog, int which) -> {
                                        Intent intent = new Intent(
                                                SignUpActivity.this,
                                                LoginActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(
                                                R.anim.slide_in_left, R.anim.slide_out_right);
                                        });
                                builder.setNegativeButton("cancel", (dialog, which) -> {
                                        progressBar.setVisibility(View.INVISIBLE);
                                });
                                returnVal[0] = true;
                                builder.show();
                            }
                            else{
                                returnVal[0] = false;
                            }

                        }
                        else{
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error connecting to database",
                                    Toast.LENGTH_SHORT).show();
                        }
                });
        return returnVal[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstName = (EditText)findViewById(R.id.loginMobileNo);
        lastName = (EditText)findViewById(R.id.lastName);
        password = (EditText)findViewById(R.id.loginPassword);
        Button submitBtn = (Button) findViewById(R.id.createAccount);
        email = (EditText)findViewById(R.id.emailAddress);
        age = (EditText)findViewById(R.id.editAge);
        addressLine1 = (EditText)findViewById(R.id.addressLine1);
        addressLIne2 = (EditText)findViewById(R.id.addressLine2);
        city = (EditText)findViewById(R.id.editCity);
        country = (EditText)findViewById(R.id.editCountry);
        postalCode = (EditText)findViewById(R.id.postalCode);
        state = (EditText)findViewById(R.id.editState);
        gender = (RadioButton) findViewById(R.id.male);
        gender2 = (RadioButton) findViewById(R.id.female);
        phone = (EditText)findViewById(R.id.phoneNumber);
        CheckBox userType = (CheckBox) findViewById(R.id.volunteerCheckbox);

        submitBtn.setOnClickListener((v) -> {
                boolean validation = validateInputs();
                if(validation){

                    boolean status = false;
                    try{
                        final String command = "ping -c 1 google.com";
                        status = (Runtime.getRuntime().exec(command).waitFor() == 0);
                        Log.v("int",status+"");
                    }
                    catch (Exception e){Log.e("status",e.toString());}

                    if(!status){
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                            SignUpActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle("No Internet Connection");
                        builder.setMessage(
                            "Internet Connection is required to perform the following task.");
                        builder.setNegativeButton("Ok", (dialog, which) -> {
                            onBackPressed();
                        });
                        builder.show();
                    }
                    else{
                        progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.VISIBLE);

                        if(!checkUserExists()){
                            String userGender;
                            if(gender2.isChecked()){userGender = "Female" ;}
                            else{userGender = "Male";}
                            
                            String acc_type="not_initialized";
                            if(userType.isChecked()){acc_type="1";}
                            else{acc_type="0";}

                            Map<String, Object> user = new HashMap<>();
                            user.put("firstName", firstName.getText().toString());
                            user.put("lastName", lastName.getText().toString());
                            user.put("contactNo",Long.parseLong(phone.getText().toString()));
                            user.put("email", email.getText().toString());
                            user.put("password", password.getText().toString());
                            user.put("gender", userGender);
                            user.put("address", 
                                addressLine1.getText().toString()+" "+
                                addressLIne2.getText().toString());
                            user.put("age", age.getText().toString());
                            user.put("city", city.getText().toString());
                            user.put("state", state.getText().toString());
                            user.put("country", country.getText().toString());
                            user.put("postalCode", postalCode.getText().toString());
                            user.put("latitude",0);
                            user.put("longitude",0);
                            user.put("type", acc_type);
                            user.put("lcity","");
                            db.collection("user_details")
                                    .add(user)
                                    .addOnSuccessListener((documentReference) -> {
                                            Log.d(
                                                "msg1", 
                                                "DocumentSnapshot added with ID: " 
                                                        + documentReference.getId());
                                            Map<String, Object> eContactTemplate = new HashMap<>();
                                            eContactTemplate.put("name","");
                                            eContactTemplate.put("relation","Parent");
                                            eContactTemplate.put("contactno","");
                                            String[] eContactList = {
                                                    "eContact1", "eContact2", "eContact3"};
                                            for (String eContact: eContactList){
                                                db.collection("emergency_details")
                                                    .document(documentReference.getId())
                                                    .collection("contacts")
                                                    .document(eContact)
                                                    .set(eContactTemplate);
                                            }
                                            Toast.makeText(
                                                getApplicationContext(),
                                                "ACCOUNT CREATED SUCCESSFULLY",
                                                Toast.LENGTH_SHORT).show();
                                            User persistentUser = new User(
                                                    SignUpActivity.this);
                                            persistentUser.setFName((String) user.get("firstName"));
                                            persistentUser.setLName((String) user.get("lastName"));
                                            persistentUser.setContactnumber(
                                                (Long) user.get("contactNo"));
                                            persistentUser.setUserid(documentReference.getId());
                                            persistentUser.setAge(
                                                Integer.parseInt(
                                                    (String) Objects.requireNonNull(
                                                        user.get("age"))));
                                            persistentUser.setAddress((String) user.get("address"));
                                            persistentUser.setCity((String) user.get("city"));
                                            persistentUser.setCountry((String) user.get("country"));
                                            persistentUser.setPostalCode(
                                                    (String) user.get("postalCode"));
                                            persistentUser.setState(
                                                    (String) user.get("state"));
                                            persistentUser.setGender((String) user.get("gender"));
                                            persistentUser.setEmail((String) user.get("email"));
                                            persistentUser.setType(
                                                Integer.parseInt((String) Objects.requireNonNull(
                                                    user.get("type"))));
                                            persistentUser.setPassword(
                                                    (String) user.get("password"));
                                            persistentUser.setEcon1(Long.parseLong("0"));
                                            persistentUser.setEcon1name("");
                                            persistentUser.setRel1("Parent");
                                            persistentUser.setEcon2(Long.parseLong("0"));
                                            persistentUser.setEcon2name("");
                                            persistentUser.setRel2("Parent");
                                            persistentUser.setEcon3(Long.parseLong("0"));
                                            persistentUser.setEcon3name("");
                                            persistentUser.setRel3("Parent");
                                            progressBar.setVisibility(View.INVISIBLE);
                                            Intent in = new Intent(
                                                SignUpActivity.this,
                                                SplashActivity.class);
                                            startActivity(in);
                                    })
                                    .addOnFailureListener((e) -> {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(
                                            getApplicationContext(),
                                            "FAIL TO CREATE ACCOUNT, TRY AGAIN AFTER SOME TIME",
                                            Toast.LENGTH_SHORT
                                        ).show();
                                    });
                        }
                    }
                }
        });
    }
}