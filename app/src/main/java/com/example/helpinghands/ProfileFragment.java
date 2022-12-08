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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    private Activity activity;
    private View promptsViewChangePass;
    private View promptsViewDeleteAccount;
    private View profileFragmentRoot;
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
        profileFragmentRoot = root;
        ListView listView = root.findViewById(R.id.profileList);
        final String[] options = new String[]{
                getString(R.string.My_Profile),getString(R.string.Emergency_Contacts),getString(R.string.Change_Credentials),
                getString(R.string.Deactivate_Account),getString(R.string.Log_out),getString(R.string.Help_About)};
        final String[] info = new String[]{
                getString(R.string.Edit_profile_details),getString(R.string.Manage_Emergency_Contacts),getString(R.string.Change_Forgot_password),
                getString(R.string.Account_deleted_permanently),getString(R.string.Session_destroyed),
                getString(R.string.Creators_info_and_Help)};
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
                        builder.setTitle(R.string.Change_Account_Password);
                        promptsViewChangePass = layoutInflater.inflate(
                                R.layout.dialog_reset_pass, null);
                        builder.setView(promptsViewChangePass);
                        oldPass = promptsViewChangePass.findViewById(R.id.oldPass);
                        newPass = promptsViewChangePass.findViewById(R.id.newPass);
                        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {});
                        builder.setPositiveButton(R.string.Change_password, (dialog, which) -> {
                            changePassword();
                        });
                        builder.show();
                        break;
                    }
                    case 3:{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setCancelable(true);
                        builder.setTitle(getString(R.string.Deactivate_Account));
                        builder.setMessage(
                            R.string.This_will_delete_your_account);

                        promptsViewDeleteAccount = layoutInflater.inflate(
                                R.layout.dialog_format, null);
                        builder.setView(promptsViewDeleteAccount);
                        deletePass = promptsViewDeleteAccount.findViewById(R.id.passFormat);

                        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {});
                        builder.setPositiveButton(R.string.Deactivate_my_Account, (dialog, which) -> {
                            deleteAccount();
                        });
                        builder.show();

                        break;
                    }
                    case 4:{
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setCancelable(true);
                        builder.setTitle(R.string.Logout);
                        builder.setMessage(R.string.Are_you_sure_you_want_to_log_out);
                        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {});
                        builder.setPositiveButton(getString(R.string.Log_out), (dialog, which) -> {
                            user.removeUser();
                            Intent in = new Intent(activity, LoginActivity.class);
                            startActivity(in);
                            activity.overridePendingTransition(
                                    R.anim.slide_in_left,R.anim.slide_out_right);

                        });
                        builder.show();
                        break;
                    }
                    case 5:{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setCancelable(true);
                        promptsViewChangePass = layoutInflater.inflate(
                                R.layout.help_dialogue, null);
                        builder.setView(promptsViewChangePass);
                        builder.setPositiveButton(R.string.got_it, (dialog, which) -> {
                            dialog.dismiss();
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

        if (!checkInternetStatus(requireContext())) {
            noInternetConnectionAlert(activity);
        }
        else{
            if(deletePass.getText().toString().equals(user.getPassword())){
                final ProgressDialog progressBar;
                progressBar = new ProgressDialog(promptsViewDeleteAccount.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage(getString(R.string.Deleting_Account_Data));
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.show();
                db.collection("user_details").document(user.getUserid()).delete().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.v("status","Success user_details");
                        user.removeUser();
                        Toast.makeText(getActivity(), R.string.Account_deactivated_successfully,Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                        Intent in = new Intent(activity, LoginActivity.class);
                        startActivity(in);
                        activity.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    }
                    else{
                        Toast.makeText(getActivity(), R.string.Error_while_performing_action,Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                });
                Log.v("status","id is "+user.getUserid());

            }
            else{
                Toast.makeText(getActivity(), R.string.Password_does_not_match,Toast.LENGTH_SHORT).show();
            }}
    }

    private void changePassword() {
        if (!checkInternetStatus(requireContext())) {
            noInternetConnectionAlert(activity);
        }
        else{
            if(!newPass.getText().toString().matches("[a-zA-Z0-9@#$.]{5}[a-zA-Z0-9@#$.]+")){
                Toast.makeText(getActivity(), R.string.NewPassword_must_be_6_characters_long,Toast.LENGTH_SHORT).show();
            }
            else{
                if(oldPass.getText().toString().equals(user.getPassword())){
                    final ProgressDialog progressBar;
                    progressBar = new ProgressDialog(promptsViewChangePass.getContext());
                    progressBar.setCancelable(false);
                    progressBar.setMessage(getString(R.string.Updating_New_password));
                    progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressBar.setProgress(0);
                    progressBar.setMax(100);
                    progressBar.show();
                    db.collection("user_details").document(user.getUserid()).update("password", newPass.getText().toString()).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Log.v("status","Success");
                            user.setPassword(newPass.getText().toString());
                            Snackbar.make(profileFragmentRoot, R.string.Password_is_changed_successfully, Snackbar.LENGTH_LONG).show();
                            progressBar.dismiss();
                        }
                        else{
                            Snackbar.make(profileFragmentRoot, getString(R.string.Error_while_performing_action), Snackbar.LENGTH_LONG).show();
                            progressBar.dismiss();
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(),R.string.Password_does_not_match,Toast.LENGTH_SHORT).show();
                }}
        }
    }
}
