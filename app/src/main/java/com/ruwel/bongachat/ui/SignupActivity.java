package com.ruwel.bongachat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruwel.bongachat.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.signup_button)
    Button mSignUpButton;
    @BindView(R.id.signup_name)
    TextInputLayout mName;
    @BindView(R.id.signup_email)
    TextInputLayout mEmail;
    @BindView(R.id.signup_password)
    TextInputLayout mPassword;
    @BindView(R.id.signup_confirm_password)
    TextInputLayout mConfirmPassword;
    @BindView(R.id.signup_phone)
    TextInputLayout mPhone;

    private static final String TAG = "SignupActivity";

    //this class responsible for all auth actions we require
    private FirebaseAuth firebaseAuth;
    //auth listener to check the auth of the app at a given time
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressAuthDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        //operations are performed in this one instance to prevent memory leaks
        firebaseAuth = FirebaseAuth.getInstance();
        createAuthStateListener();
        //click listeners
        mSignUpButton.setOnClickListener(this);

        createAuthProgressDialog();
    }
    @Override
    public void onStart() {
        super.onStart();
        //essential for the auth state listener to work
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    public void onClick(View view) {
        if(view == mSignUpButton) {
            createNewUser();
        }
    }

    //method to hold logic for creating new accounts
    private void createNewUser() {
        String name = mName.getEditText().getText().toString().trim();
        String email = mEmail.getEditText().getText().toString().trim();
        String phone = mPhone.getEditText().getText().toString().trim();
        String password = mPassword.getEditText().getText().toString().trim();
        String confirmPassword = mConfirmPassword.getEditText().getText().toString().trim();

        //calling the form validation methods
        boolean validEmail = isValidEmail(email);
        boolean validName = isValidName(name);
        boolean validPassword = isValidPassword(password, confirmPassword);
        if (!validEmail || !validName || !validPassword) return; //this return statements halts createNewUser method and errors are displayed

        mProgressAuthDialog.show();
        //parse users' info into the firebase auth api
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressAuthDialog.dismiss();
                        if (task.isSuccessful()) {
                            Snackbar.make(mSignUpButton, "Successful", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                                    .setActionTextColor(getResources().getColor(R.color.gray))
                                    .show();
                            storeValuesInDB(name, email, phone);
                        } else {
                            //snack bar
                            Snackbar.make(mSignUpButton, "Error! Try again", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                                    .setActionTextColor(getResources().getColor(R.color.gray))
                                    .show();
                        }
                    }
                });
    }

    private void storeValuesInDB(String name, String email, String phone) {
        //we save these values in db for later references
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_name))
                .setValue(name);
        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_email))
                .setValue(email);
        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_phone))
                .setValue(phone);
    }

    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //if there exists an already authenticated account, the user is redirected to the main activity
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

        };
    }

    //progress dialog methods
    private void createAuthProgressDialog() {
        mProgressAuthDialog = new ProgressDialog(this);
        mProgressAuthDialog.setMessage("Getting you set up...");
        mProgressAuthDialog.setCancelable(false);
    }

    //form validation
    private boolean isValidEmail(String email) {
        boolean isGoodEmail =
                (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEmail.setError("Please enter a valid email address");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mName.setError("Please enter your name");
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6) {
            mPassword.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}