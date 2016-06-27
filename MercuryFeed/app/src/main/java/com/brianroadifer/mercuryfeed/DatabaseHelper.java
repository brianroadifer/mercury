package com.brianroadifer.mercuryfeed;

import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Brian Roadifer on 6/26/2016.
 */
public class DatabaseHelper {
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    public void writeFeedToDataBase(String feedUrl, String title) {
        String key = database.child("feeds").push().getKey();
        Feed feed = new Feed(feedUrl, title);
        Map<String, Object> feedValues = feed.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/feeds/"+key, feedValues);
        database.updateChildren(childUpdates);
    }
}
