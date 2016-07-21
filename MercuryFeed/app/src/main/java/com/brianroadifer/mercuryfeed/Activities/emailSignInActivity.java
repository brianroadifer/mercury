package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Brian Roadifer on 7/18/2016.
 */
public class EmailSignInActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "EmailSignInActivity";
    private EditText emailField;
    private EditText passwordField;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener stateListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign);
        emailField = (EditText) findViewById(R.id.email);
        passwordField = (EditText) findViewById(R.id.password);
        findViewById(R.id.sign_button).setOnClickListener(this);
        findViewById(R.id.reg_button).setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    Log.d(TAG, "onAuthStateChange:signed_in: "+ user.getUid());
                }else {
                    Log.d(TAG, "onAuthStateChange:signed_out: "+ user.getUid());
                }
                updateUI(user);

            }
        };
    }
    @Override
    public void onStart(){
        super.onStart();
        auth.addAuthStateListener(stateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (stateListener != null) {
            auth.removeAuthStateListener(stateListener);
        }
    }

    private void createAccount(String email, String password){
        if(!validateForm()){
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmail:onComplete: "+ task.isSuccessful());

                if(!task.isSuccessful()){
                    Snackbar.make(getCurrentFocus(), "Authentication Failed", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void signIn(String email, String password){
        Log.d(TAG,"signIn: "+email);

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete: "+ task.isSuccessful());
                if(!task.isSuccessful()){
                    Snackbar.make(getCurrentFocus(), "Authentication Failed", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signOut(){
        auth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
        boolean vaild = true;
        String email = emailField.getText().toString();
        if(TextUtils.isEmpty(email)){
            emailField.setError("Required");
            vaild = false;
        }else{
            emailField.setError(null);
        }
        String password = passwordField.getText().toString();
        if(TextUtils.isEmpty(password)){
           passwordField.setError("Required");
            vaild = false;
        }else{
            passwordField.setError(null);
        }
        return vaild;
    }

    public void updateUI(FirebaseUser user){
        if(user != null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_button:
                createAccount(emailField.getText().toString(), passwordField.getText().toString());
                break;
            case R.id.sign_button:
                signIn(emailField.getText().toString(), passwordField.getText().toString());
                break;
        }
    }
}
