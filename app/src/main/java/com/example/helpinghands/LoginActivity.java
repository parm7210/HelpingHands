package com.example.helpinghands;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.noInternetConnectionAlert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText mobileNo;
    private EditText password;


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public boolean validateInputs(){
        boolean validation = true;
        if(!mobileNo.getText().toString().matches("[0-9]{10}")){
            mobileNo.requestFocus();
            mobileNo.setError(getString(R.string.Please_provide_10_digit_Mobile_number));
            validation = false;
        }
        if(mobileNo.getText().toString().length() == 0){
            mobileNo.requestFocus();
            mobileNo.setError(getString(R.string.Field_can_not_be_empty));
            validation = false;
        }
        if(password.getText().toString().length() == 0){
            password.requestFocus();
            password.setError(getString(R.string.Field_can_not_be_empty));
            validation = false;
        }
        return validation;
    }

    public void setPersistentUser(QueryDocumentSnapshot document){
        final User user = new User(LoginActivity.this);
        user.setFName(document.get("firstName").toString());
        user.setLName(document.get("lastName").toString());
        user.setContactNumber(Long.parseLong(document.get("contactNo").toString()));
        user.setUserid(document.getId());
        user.setAge(Integer.parseInt(document.get("age").toString()));
        user.setAddress(document.get("address").toString());
        user.setCity(document.get("city").toString());
        user.setCountry(document.get("country").toString());
        user.setPostalCode(document.get("postalCode").toString());
        user.setState(document.get("state").toString());
        user.setGender(document.get("gender").toString());
        user.setEmail(document.get("email").toString());
        user.setType(Integer.parseInt(document.get("type").toString()));
        user.setPassword(password.getText().toString());
        Task<DocumentSnapshot> sp = db.collection("emergency_details").document(user.getUserid())
                .collection("contacts").document("eContact1").get().addOnCompleteListener(task1 -> {
                    DocumentSnapshot spd = task1.getResult();
                    if (spd.get("contactNo").toString().length() == 0) {
                        user.setEcon1(Long.parseLong("0"));
                    } else {
                        user.setEcon1(Long.parseLong(spd.get("contactNo").toString()));
                    }
                    user.setEcon1name(spd.get("name").toString());
                    user.setRel1(spd.get("relation").toString());
                });
        Task<DocumentSnapshot> sp1 = db.collection("emergency_details").document(user.getUserid())
                .collection("contacts").document("eContact2").get().addOnCompleteListener(task12 -> {
                    DocumentSnapshot spd = task12.getResult();
                    if (spd.get("contactNo").toString().length() == 0) {
                        user.setEcon2(Long.parseLong("0"));
                    } else {
                        user.setEcon2(Long.parseLong(spd.get("contactNo").toString()));
                    }
                    user.setEcon2name(spd.get("name").toString());
                    user.setRel2(spd.get("relation").toString());
                });
        Task<DocumentSnapshot> sp2 = db.collection("emergency_details").document(user.getUserid())
                .collection("contacts").document("eContact3").get().addOnCompleteListener(task13 -> {
                    DocumentSnapshot spd = task13.getResult();
                    if (spd.get("contactNo").toString().length() == 0) {
                        user.setEcon3(Long.parseLong("0"));
                    } else {
                        user.setEcon3(Long.parseLong(spd.get("contactNo").toString()));
                    }
                    user.setEcon3name(spd.get("name").toString());
                    user.setRel3(spd.get("relation").toString());
                });
    }

    @Override
    public void onBackPressed(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mobileNo = (EditText) findViewById(R.id.loginMobileNo);
        password = (EditText) findViewById(R.id.loginPassword);

        Button loginBtn = (Button) findViewById(R.id.loginButton);
        Button createAccountBtn = (Button) findViewById(R.id.createAccountButton);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        createAccountBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        });

        loginBtn.setOnClickListener(v -> {
            if(validateInputs()) {

                if (!checkInternetStatus(getApplicationContext())) {
                    noInternetConnectionAlert(LoginActivity.this);
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection("user_details").whereEqualTo(
                        "contactNo", Long.parseLong(mobileNo.getText().toString()))
                            .get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().getDocuments().toString().equals("[]")) {
                                        Toast.makeText(
                                            getApplicationContext(),
                                            "Account does not exist",
                                            Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("status", document.getId() + " => " + document.getData());

                                            if (password.getText().toString().equals(document.get("password"))) {
                                                Toast.makeText(getApplicationContext(), R.string.Login_Successfully, Toast.LENGTH_SHORT).show();
                                                setPersistentUser(document);
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Intent in = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(in);
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                            } else {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(getApplicationContext(), R.string.Invalid_Login_Credentials, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, getString(R.string.Error_getting_documents) + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }

        });
    }
}