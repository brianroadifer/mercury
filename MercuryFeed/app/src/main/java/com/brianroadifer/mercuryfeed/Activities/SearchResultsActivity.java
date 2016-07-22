package com.brianroadifer.mercuryfeed.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.brianroadifer.mercuryfeed.Helpers.SearchAdapter;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    DatabaseReference feedDB = FirebaseDatabase.getInstance().getReference().child("feeds");
    List<Feed> feeds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        feedDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Feed feed = new Feed();
                    feed.Title = snapshot.child("title").getValue().toString();
                    feed.ID =   snapshot.getKey();
                    feed.FeedUrl = snapshot.child("feedUrl").getValue().toString();
                    feeds.add(feed);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent){
        handleIntent(intent);
    }
    private void handleIntent(Intent intent){
        List<Feed> queryFeed = new ArrayList<>();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            for(Feed feed : feeds){
                if(feed.Title.contains(query)){
                    queryFeed.add(feed);
                }
            }
            RecyclerView recyclerView = new RecyclerView(getApplicationContext());
            SearchAdapter searchAdapter =new SearchAdapter(queryFeed, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(searchAdapter);
        }
    }
}
