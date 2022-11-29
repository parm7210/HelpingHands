package com.example.helpinghands;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.noInternetConnectionAlert;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    private Activity activity;
    private View promptsViewChangePass;
    private View promptsViewDeleteAccount;
    private EditText oldPass;
    private EditText newPass;
    private EditText deletePass;

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_profile, container, false);
        activity = requireActivity();
        LayoutInflater layoutInflater =LayoutInflater.from(getContext());
        user = new User(activity);
        ListView listView = root.findViewById(R.id.profileList);
        final String[] options = new String[]{
                "My Profile","Emergency Contacts","Change Credentials",
                "Deactivate Account","Log out","Help & Feedback"};
        final String[] info = new String[]{
                "Edit profile details","Manage Emergency Contacts","Change/Forgot password",
                "Account will be deleted permanently","Session will be destroyed",
                "FAQs & Feedback option"};
        final Integer[] imageArray = new Integer[]{
                R.drawable.baseline_person_24,R.drawable.ic_contacts_24px,
                R.drawable.ic_security_24px, R.drawable.ic_cancel_24px,
                R.drawable.ic_exit_to_app_24px,R.drawable.ic_help_24px};

        ProfileListAdapter profileListAdapter = new ProfileListAdapter(
                activity, options, info, imageArray);
        listView.setAdapter(profileListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("listview",options[position]);
                switch (position){
                    case 0:{
                        Intent intent = new Intent(activity, EditProfileActivity.class);
                        startActivity(intent);
                        activity.overridePendingTransition(
                                R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    }
                    case 1:{
                        Intent intent = new Intent(activity, EmergencyContactsActivity.class);
                        int b = 2;
                        startActivity(intent);
                        activity.overridePendingTransition(
                                R.anim.slide_in_right,R.anim.slide_out_left);
                        break;
                    }
                    case 2:{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setCancelable(true);
                        builder.setTitle("Change Account Password");
                        promptsViewChangePass = layoutInflater.inflate(
                                R.layout.dialog_reset_pass, null);
                        builder.setView(promptsViewChangePass);
                        oldPass = promptsViewChangePass.findViewById(R.id.oldPass);
                        newPass = promptsViewChangePass.findViewById(R.id.newPass);
                        builder.setNegativeButton("cancel", (dialog, which) -> {});
                        builder.setPositiveButton("Change password", (dialog, which) -> {
                            changePassword();
                        });
                        builder.show();
                        break;
                    }
                    case 3:{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setCancelable(true);
                        builder.setTitle("Deactivate Account");
                        builder.setMessage(
                            "This will delete your account along with all your data. " +
                            "This can't be undone");

                        promptsViewDeleteAccount = layoutInflater.inflate(
                                R.layout.dialog_format, null);
                        builder.setView(promptsViewDeleteAccount);
                        deletePass = promptsViewDeleteAccount.findViewById(R.id.passFormat);

                        builder.setNegativeButton("cancel", (dialog, which) -> {});
                        builder.setPositiveButton("Deactivate my Account", (dialog, which) -> {
                            deleteAccount();
                        });
                        builder.show();

                        break;
                    }
                    case 4:{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setCancelable(true);
                        builder.setTitle("Logout");
                        builder.setMessage("Are you sure you want to log out?");
                        builder.setNegativeButton("cancel", (dialog, which) -> {});
                        builder.setPositiveButton("Log out", (dialog, which) -> {
                            user.removeUser();
                            Intent in = new Intent(activity, LoginActivity.class);
                            startActivity(in);
                            activity.overridePendingTransition(
                                    R.anim.slide_in_left,R.anim.slide_out_right);

                        });
                        builder.show();
                        break;
                    }
                }
            }
        });

        return root;
    }

    private void deleteAccount() {

        if (!checkInternetStatus()) {
            noInternetConnectionAlert(activity);
        }
        else{
            if(deletePass.getText().toString().equals(user.getPassword())){
                final ProgressDialog progressBar;
                progressBar = new ProgressDialog(promptsViewDeleteAccount.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage("Deleting Account Data...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.show();
                db.collection("user_details").document(user.getUserid()).delete().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.v("status","Success user_details");
                        user.removeUser();
                        Toast.makeText(getActivity(),"Account deactivated successfully",Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                        Intent in = new Intent(activity, LoginActivity.class);
                        startActivity(in);
                        activity.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    }
                    else{
                        Toast.makeText(getActivity(),"Error while performing action",Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                });
                Log.v("status","id is "+user.getUserid());

            }
            else{
                Toast.makeText(getActivity(),"Password does not match",Toast.LENGTH_SHORT).show();
            }}
    }

    private void changePassword() {
        if (!checkInternetStatus()) {
            noInternetConnectionAlert(activity);
        }
        else{
            if(!newPass.getText().toString().matches("[a-zA-Z0-9@#$.]{5}[a-zA-Z0-9@#$.]+")){
                Toast.makeText(getActivity(),"New Password must be 6 characters long",Toast.LENGTH_SHORT).show();
            }
            else{
                if(oldPass.getText().toString().equals(user.getPassword())){
                    final ProgressDialog progressBar;
                    progressBar = new ProgressDialog(promptsViewChangePass.getContext());
                    progressBar.setCancelable(false);
                    progressBar.setMessage("Updating New password...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressBar.setProgress(0);
                    progressBar.setMax(100);
                    progressBar.show();
                    db.collection("user_details").document(user.getUserid()).update("password", newPass.getText().toString()).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Log.v("status","Success");
                            user.setPassword(newPass.getText().toString());
                            Toast.makeText(getActivity(),"Password is changed successfully",Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();
                        }
                        else{
                            Toast.makeText(getActivity(),"Error while performing action",Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(),"Password does not match",Toast.LENGTH_SHORT).show();
                }}
        }
    }
}
