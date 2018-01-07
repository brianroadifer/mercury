package com.brianroadifer.mercuryfeed.Helpers;

import android.util.Log;

import com.brianroadifer.mercuryfeed.Models.Feed;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final List<Feed> feeds = new ArrayList<>();
    public void writeFeedToDataBase(String feedUrl, String title) {
        String key = database.child("feeds").push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/feeds/"+key+"/feedUrl/",feedUrl);
        childUpdates.put("/feeds/"+key+"/title/", title);
        childUpdates.put("/users/"+auth.getCurrentUser().getUid()+"/subscribed/"+key, true);
        database.updateChildren(childUpdates);
    }

    public void loadDatabase(){
        database = FirebaseDatabase.getInstance().getReference().child("feeds");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for(DataSnapshot snap: snapshots){
                    String url = "";
                    String title = "";
                    for(DataSnapshot child : snap.getChildren()){
                        if(child.getKey().equals("title")){
                            title = child.getValue(String.class);
                        }
                        if(child.getKey().equals("feedUrl")){
                            url = child.getValue(String.class);
                        }
                    }
                    feeds.add(new Feed(url, title));
                    Log.d("onDataChange", "Value: " + feeds);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFeeds:onCancelled", databaseError.toException());
            }
        });
    }
}
