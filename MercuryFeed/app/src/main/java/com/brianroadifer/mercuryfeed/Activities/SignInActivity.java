package com.brianroadifer.mercuryfeed.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.common.SignInButton;

/**
 * Created by Brian Roadifer on 7/18/2016.
 */
public class SignInActivity extends AppCompatActivity {
    private static final Class[] CLASSES = new Class[]{
            GoogleSignInActivity.class,
            EmailSignInActivity.class
    };


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_sign_in);
//
//        ListView listView = (ListView) findViewById(R.id.list_view);
//        SignInButton googleButton = (SignInButton) findViewById(R.id.sign_in_button);
//        Button emailButton = (Button) findViewById(R.id.email_button);
//        googleButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

    }

}
