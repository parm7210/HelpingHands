package com.example.helpinghands;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText age;
    private EditText addressLine;
    private EditText city;
    private EditText state;
    private EditText country;
    private EditText postalCode;
    private Button updateBtn;
    private CheckBox type;
    private RadioButton gender;
    private RadioButton gender2;
    private EditText contactNo;

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
            validation = false;
        }
        if(!lastName.getText().toString().matches("[a-z A-Z]+")){
            lastName.requestFocus();
            lastName.setError("Please provide appropriate name");
            validation = false;
        }
        if(addressLine.getText().toString().length() == 0){
            addressLine.requestFocus();
            addressLine.setError("Field is required");
            validation = false;
        }

        if(city.getText().toString().length() == 0){
            city.requestFocus();
            city.setError("Field is required");
            validation = false;
        }
        if(state.getText().toString().length() == 0){
            state.requestFocus();
            state.setError("Field is required");
            validation = false;
        }
        if(country.getText().toString().length() == 0){
            country.requestFocus();
            country.setError("Field is required");
            validation = false;
        }
        if(postalCode.getText().toString().length() == 0){
            postalCode.requestFocus();
            postalCode.setError("Field is required");
            validation = false;
        }
        if(!email.getText().toString().matches(
                "^[a-zA-Z0-9._]+@[a-zA-Z]+?\\.[a-zA-Z]{2,5}$")){
            email.requestFocus();
            email.setError("Please provide appropriate email-id");
            validation = false;
        }

        if(!age.getText().toString().matches("[1-9][0-9]")){
            age.requestFocus();
            age.setError("Please provide appropriate age");
            validation = false;
        }
        return validation;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        User user = new User(this);

        contactNo = findViewById(R.id.editContact);
        updateBtn = findViewById(R.id.updateProfileBtn);
        email = findViewById(R.id.editEmail);
        age = findViewById(R.id.editAge);
        addressLine = findViewById(R.id.editAddressLine1);
        firstName = findViewById(R.id.editFirstName);
        lastName = findViewById(R.id.editLastName);
        city = findViewById(R.id.editCity);
        state = findViewById(R.id.editState);
        country = findViewById(R.id.editCountry);
        postalCode = findViewById(R.id.editPostalCode);
        type = findViewById(R.id.editVolunteer);
        gender = findViewById(R.id.editMale);
        gender2 = findViewById(R.id.editFemale);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        contactNo.setText(Long.toString(user.getContactnumber()));
        email.setText(user.getEmail());
        age.setText(Integer.toString(user.getAge()));
        addressLine.setText(user.getAddress());
        city.setText(user.getCity());
        state.setText(user.getState());
        firstName.setText(user.getFName());
        lastName.setText(user.getLName());
        country.setText(user.getCountry());
        postalCode.setText(user.getPostalCode());
        if(user.getGender().equals("Female")){gender2.setChecked(true);}
        else{gender.setChecked(true);}
        if(user.getType() == 1){type.setChecked(true);}

        updateBtn.setOnClickListener(v -> {
            if(validateInputs()){
                boolean status = false;
                try {
                    final String command = "ping -c 1 google.com";
                    status = (Runtime.getRuntime().exec(command).waitFor() == 0);
                    Log.v("int", status + "");
                } catch (Exception e) {
                    Log.e("status", e.toString());
                }
                if (!status) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(true);
                    builder.setTitle("No Internet Connection");
                    builder.setMessage(
                            "Internet Connection is required to perform the following task.");
                    builder.setNegativeButton("Ok", (dialog, which) -> onBackPressed());
                    builder.show();
                }
                else {
                    String userType, userGender;
                    if (gender2.isChecked()) {
                        userGender = "Female";
                    } else {
                        userGender = "Male";
                    }
                    if (type.isChecked()) {
                        userType = "1";
                    } else {
                        userType = "0";
                    }
                    db.collection("user_details").document(user.getUserid())
                        .update(
                            "email", email.getText().toString(),
                            "age", age.getText().toString(),
                            "address", addressLine.getText().toString(),
                            "city", city.getText().toString(),
                            "country", country.getText().toString(),
                            "postalCode", postalCode.getText().toString(),
                            "firstName", firstName.getText().toString(),
                            "lastName", lastName.getText().toString(),
                            "gender", userGender,
                            "state", state.getText().toString(),
                            "type", userType
                    );
                    user.setFName(firstName.getText().toString());
                    user.setLName(lastName.getText().toString());
                    user.setAge(Integer.parseInt(age.getText().toString()));
                    user.setAddress(addressLine.getText().toString());
                    user.setCity(city.getText().toString());
                    user.setCountry(country.getText().toString());
                    user.setPostalCode(postalCode.getText().toString());
                    user.setState(state.getText().toString());
                    user.setGender(userGender);
                    user.setEmail(email.getText().toString());
                    user.setType(Integer.parseInt(userType));
                    Toast.makeText(
                            this,
                            "PROFILE UPDATED SUCCESSFULLY",
                            Toast.LENGTH_SHORT
                    ).show();
                    onBackPressed();
                }
            }
        });
    }
}