package com.brianroadifer.mercuryfeed.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import com.brianroadifer.mercuryfeed.Helpers.SearchAdapter;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
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
    private static final String TAG = "SearchResultsActivity";
    DatabaseReference feedDB = FirebaseDatabase.getInstance().getReference().child("feeds");
    List<Feed> feeds = new ArrayList<>();
    List<Feed> queryFeed = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString("app_screen", "Light");
        String primary = pref.getString("app_primary", "Blue Grey");
        String accent = pref.getString("app_accent", "Blue Grey");
        String status = pref.getString("app_status", "Blue Grey");
        String navigation = pref.getString("app_navigation", "Black");
        decideTheme(theme, primary, accent, status, navigation);
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        feedDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Feed feed = new Feed();
                    feed.Title = snapshot.child("title").getValue().toString();
                    feed.ID = snapshot.getKey();
                    feed.FeedUrl = snapshot.child("feedUrl").getValue().toString();
                    Log.d(TAG, "onDataChange:Value:Title:" + feed.Title);
                    feeds.add(feed);
                }
                handleIntent(getIntent());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent){
        setIntent(intent);
        handleIntent(intent);
    }
    private void handleIntent(Intent intent){
        Log.d(TAG, "handleIntent:"+ intent.hasExtra(SearchManager.QUERY));
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            for(Feed feed : feeds){
                if(feed.Title.contains(query)){
                    queryFeed.add(feed);
                }
            }
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            SearchAdapter searchAdapter = new SearchAdapter(queryFeed, this);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(searchAdapter);
        }
    }

    private void decideTheme(String themeName, String primary, String accent, String status, String navigation) {
        ThemeChanger themeChanger = new ThemeChanger(this);
        themeChanger.screenColor(themeName);
        themeChanger.primaryColor(primary);
        themeChanger.accentColor(accent);
        themeChanger.statusColor(status);
        themeChanger.navigationColor(navigation);
        themeChanger.changeTheme();
    }
}
