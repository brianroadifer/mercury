package com.brianroadifer.mercuryfeed.Activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

public class GoogleSignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static String TAG="GoogleSignInActivity";
    private static final int RC_SIGN_IN = 12501;
    private SignInButton googleButton;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth auth;
    private  FirebaseAuth.AuthStateListener stateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleButton = (SignInButton) findViewById(R.id.sign_in_button);
        googleButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        auth = FirebaseAuth.getInstance();

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };


    }

    @Override
    public void onStart() {
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

    private void handleFirebaseAuthResult(AuthResult authResult){
        if(authResult != null){
            FirebaseUser user = authResult.getUser();
            Toast.makeText(this, "Welcome "+ user.getEmail(),Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                signInGoogle();
                break;
        }
    }
    private void signInGoogle(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    private void signOutGoogle(){
        auth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(null);
            }
        });

    }
    private void revokeGoogle(){
        auth.signOut();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(null);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }else{
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        Log.d(TAG, "firebaseAuthWithGoogle:"+ account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete"+task.isSuccessful());
                if(!task.isSuccessful()){
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Snackbar.make(getCurrentFocus(), "Authentication Failed.", LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(GoogleSignInActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            startActivity(new Intent(GoogleSignInActivity.this, MainActivity.class));
            finish();
        }else{
            //MAIN PAGE;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Snackbar.make(getCurrentFocus(), "Google Play Services Error", LENGTH_SHORT).show();

    }
}
