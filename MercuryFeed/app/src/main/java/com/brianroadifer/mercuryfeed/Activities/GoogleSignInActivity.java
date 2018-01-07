package com.brianroadifer.mercuryfeed.Activities;

import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

public class GoogleSignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG="GoogleSignInActivity";
    private static final int RC_SIGN_IN = 12501;
    private TextView emailText, passwordText;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth auth;

    private  FirebaseAuth.AuthStateListener stateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignInButton googleButton = findViewById(R.id.sign_in_button);
        googleButton.setOnClickListener(this);
        Button signIn = findViewById(R.id.sign_button);
        signIn.setOnClickListener(this);
        Button register = findViewById(R.id.reg_button);
        register.setOnClickListener(this);
        emailText = (EditText) findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);

        Button reset = findViewById(R.id.button4);
        reset.setOnClickListener(this);



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
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        switch (v.getId()){
            case R.id.sign_in_button:
                signInGoogle();
                break;
            case R.id.sign_button:
                if(isPasswordNotValid(password) && !isValidEmail(email)){
                    emailText.setError("Invalid Email");
                    passwordText.setError("Invalid Password");
                }
                else if(!isValidEmail(email)){
                    emailText.setError("Invalid Email");
                }else if (isPasswordNotValid(password)){
                    passwordText.setError("Invalid Password");
                }else{
                    signInUser(email, password);
                }
                break;
            case R.id.reg_button:
                if(isPasswordNotValid(password) && !isValidEmail(email)){
                    emailText.setError("Invalid Email");
                    passwordText.setError("Invalid Password");
                }
                else if(!isValidEmail(email)){
                    emailText.setError("Invalid Email");
                }else if (isPasswordNotValid(password)){
                    passwordText.setError("Invalid Password");
                }else{
                    registerUser(email,password);
                }
                break;
            case R.id.button4:
                createResetDialog();
        }
    }

    private void registerUser(final String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    if(getCurrentFocus() != null){
                        Snackbar.make(getCurrentFocus(), task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }else{
                    String uid = task.getResult().getUser().getUid();
                    DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("users");

                    String hash = generateHash(email.toLowerCase().trim());
                    String image = "https://www.gravatar.com/avatar/"+hash+"?d=identicon";

                    Map<String, Object> users = new HashMap<>();
                    Map<String, Object> data = new HashMap<>();
                    data.put("email",email);
                    data.put("profile_picture", image);
                    data.put("username", email);
                    users.put(uid,data);
                    userDB.updateChildren(users);
                }
            }
        });


    }
    private void signInUser(String email, String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signUserInWithEmail:onComplete:" + task.isSuccessful());
                if(!task.isSuccessful()){
                    Log.d(TAG, "signInUserWithEmail", task.getException());
                    if(getCurrentFocus()!= null)
                        Snackbar.make(getCurrentFocus(), "Authentication Failed with Email and/or Password", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
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

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account){
        Log.d(TAG, "firebaseAuthWithGoogle:"+ account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete"+task.isSuccessful());
                if(!task.isSuccessful()){
                    Log.w(TAG, "signInWithCredential", task.getException());
                    if(getCurrentFocus()!=null){
                        Snackbar.make(getCurrentFocus(), "Authentication Failed.", LENGTH_SHORT).show();
                    }
                }else{
                    final String uid = task.getResult().getUser().getUid();
                    final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("users");

                    userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.hasChild(uid)){
                                String image = Uri.decode(String.valueOf(account.getPhotoUrl()));

                                Map<String, Object> users = new HashMap<>();
                                Map<String, Object> data = new HashMap<>();
                                data.put("email",account.getEmail());
                                data.put("profile_picture", image);
                                data.put("username", account.getDisplayName());
                                users.put(uid,data);
                                userDB.updateChildren(users);
                            }
                            startActivity(new Intent(GoogleSignInActivity.this, MainActivity.class));
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }



    private void updateUI(FirebaseUser user){
        if(user != null){
            startActivity(new Intent(GoogleSignInActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        if(getCurrentFocus()!= null)
            Snackbar.make(getCurrentFocus(), "Google Play Services Error", LENGTH_SHORT).show();

    }

    private boolean isValidEmail(String email){
        String EMAIL_PATTERN = "^(?=[a-zA-Z0-9][a-zA-Z0-9@._%+-]{5,253}+$)[a-zA-Z0-9._%+-]{1,64}+@(?:(?=[a-zA-Z0-9]{1,63}+\\.)[a-zA-Z0-9]++(?:-[a-zA-Z0-9]++)*+\\.){1,8}+[a-zA-Z]{2,63}+$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isPasswordNotValid(String password){
        return password.isEmpty() || (password.length() <= 6);
    }

    private String generateHash(String message){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(message.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for(byte b:digest){
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }
        return "";
    }

    private void createResetDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.change_username_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final AutoCompleteTextView change = dialogView.findViewById(R.id.editText);
        TextView textView = dialogView.findViewById(R.id.textView);
        textView.setText("Forgotten password?");
        change.setHint("Enter account email to reset");


        dialog.setView(dialogView);
        dialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = change.getText().toString();
                if(isValidEmail(text)){
                    auth.sendPasswordResetEmail(text).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "sendPasswordResetEmail"+task.isSuccessful());
                        }
                    });
                    dialog.dismiss();
                }else{
                    change.setError("Invalid Email Format");
                }
            }
        });
        dialog.show();
    }
}
