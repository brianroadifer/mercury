package com.brianroadifer.mercuryfeed.Services;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseService extends IntentService {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public DatabaseService(){
        super("DatabaseService");
    }
    public DatabaseService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String child = intent.getStringExtra("child");
        DatabaseReference data = database.getReference().child(child);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
