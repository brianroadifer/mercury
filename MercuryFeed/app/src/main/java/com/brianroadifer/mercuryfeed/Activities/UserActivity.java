package com.brianroadifer.mercuryfeed.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.DatabaseHelper;
import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.api.model.ProviderUserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    Button user,email,pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        user = (Button) findViewById(R.id.button);
        email = (Button) findViewById(R.id.button2);
        pass = (Button) findViewById(R.id.button3);

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserDialog();
            }
        });
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEmailDialog();
            }
        });
        pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPassDialog();
            }
        });

    }

    private void createUserDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.change_username_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final AutoCompleteTextView change = (AutoCompleteTextView) dialogView.findViewById(R.id.editText);

        dialog.setView(dialogView);
        dialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = change.getText().toString();
                if(!text.isEmpty()){
                    FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("username").setValue(text);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(text)
                            .build();
                    firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "updateProfile:DisplayName:"+ task.isSuccessful());

                        }
                    });
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void createEmailDialog() {

        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.change_username_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final AutoCompleteTextView change = (AutoCompleteTextView) dialogView.findViewById(R.id.editText);
        TextView textView = (TextView)findViewById(R.id.textView);
//        textView.setText("Change Email");
        change.setHint("Email");


        dialog.setView(dialogView);
        dialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = change.getText().toString();
                if(isVaildEmail(text)){

                    String hash = generateHash(text.toLowerCase().trim(), "md5");
                    final String image = "https://www.gravatar.com/avatar/"+hash+"?d=identicon";
                    final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(image))
                            .build();
                    firebaseUser.updateEmail(text).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "updateEmail:"+ task.isSuccessful());
                            if(task.isSuccessful()){

                                FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("email").setValue(text);
                                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "updateProfile:PhotoUri:"+ task.isSuccessful());
                                        if(task.isSuccessful()){
                                            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("profile_picture").setValue(image);
                                        }
                                    }
                                });
                            }
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
    private void createPassDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.change_username_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final AutoCompleteTextView change = (AutoCompleteTextView) dialogView.findViewById(R.id.editText);
        TextView textView = (TextView)findViewById(R.id.textView);
        change.setHint("Password");
        change.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);


        dialog.setView(dialogView);
        dialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = change.getText().toString();
                if(isVaildPassword(text)){

                    firebaseUser.updatePassword(text).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "updatePassword:"+ task.isSuccessful());
                        }
                    });
                    dialog.dismiss();
                }else{
                    change.setError("Invalid Password Format");
                }


            }
        });
        dialog.show();
    }

    private void createDeleteDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.change_username_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final AutoCompleteTextView change = (AutoCompleteTextView) dialogView.findViewById(R.id.editText);
        TextView textView = (TextView)findViewById(R.id.textView);
        change.setHint("Password");
        change.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);


        dialog.setView(dialogView);
        dialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "delete:"+ task.isSuccessful());
                            if(task.isSuccessful()){
                                FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).removeValue();
                            }
                        }
                    });
                    dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String generateHash(String message, String algorithm){
        String original = message;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(original.getBytes());
        byte[] digest = md.digest();
        StringBuffer sb = new StringBuffer();
        for(byte b:digest){
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private boolean isVaildEmail(String email){
        String EMAIL_PATTERN = "^(?=[a-zA-Z0-9][a-zA-Z0-9@._%+-]{5,253}+$)[a-zA-Z0-9._%+-]{1,64}+@(?:(?=[a-zA-Z0-9]{1,63}+\\.)[a-zA-Z0-9]++(?:-[a-zA-Z0-9]++)*+\\.){1,8}+[a-zA-Z]{2,63}+$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isVaildPassword(String password){
        if(!password.isEmpty() && (password.length() > 6)){
            return true;
        }
        return false;
    }

}
