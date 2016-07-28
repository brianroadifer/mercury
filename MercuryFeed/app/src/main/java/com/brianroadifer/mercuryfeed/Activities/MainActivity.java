package com.brianroadifer.mercuryfeed.Activities;

import android.app.Application;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.FeedItemAdapter;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.Models.Item;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private Feed feed = new Feed();

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        user = fireAuth.getCurrentUser();

        if (user != null) {
            Log.d(TAG,"User Signed In");

            Application application = this.getApplication();
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String url = bundle.getString("URL");
                String title = bundle.getString("Title");
                String id = bundle.getString("ID");
                Feed passFeed = (Feed) bundle.get("Feed");
                if(passFeed != null){
                    feed = passFeed;
                    setTitle(feed.Title);
                }else{
                    setTitle(title);
                    feed = new Feed(url, title, id);
                }
            }else{
                setTitle("All");
            }

            feedsItemDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snap : snapshots) {
                        Item item = new Item();
                        String id = (String) dataSnapshot.child(snap.getKey() + "/feed_id").getValue();
                        boolean user_read = false;
                        if (dataSnapshot.child(snap.getKey() + "/user-read/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                            user_read = (boolean) dataSnapshot.child(snap.getKey() + "/user-read/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue();
                        }
                        if (subscribed.contains(id) && !user_read) {
                            item.ID = snap.getKey();
                            item.title = (String) dataSnapshot.child(snap.getKey()).child("title").getValue();
                            item.author = (String) dataSnapshot.child(snap.getKey()).child("author").getValue();
                            item.description = (String) dataSnapshot.child(snap.getKey()).child("description").getValue();
                            item.link = (String) dataSnapshot.child(snap.getKey()).child("link").getValue();
                            item.thumbnailUrl = (String) dataSnapshot.child(snap.getKey()).child("thumbnail").getValue();
                            long unix = (long) dataSnapshot.child(snap.getKey()).child("published").getValue();
                            item.pubDate = new Date(unix);
                            if (id.equalsIgnoreCase(feed.ID)) {
                                if (item != null) {
                                    setTitle(feed.Title);
                                    feed.Items.add(item);
                                }
                            } else if (feed.ID == null) {
                                setTitle("All");
                                feed.Items.add(item);
                            }
                        }
                    }
                   handleFeed(feed);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();


            LeakCanary.install(application);
        } else {
            startActivity(new Intent(this, GoogleSignInActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.action_search:
//                startActivity(new Intent(this, SearchResultsActivity.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void handleFeed(Feed feed){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        FeedItemAdapter feedItemAdapter = new FeedItemAdapter(feed, getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(feedItemAdapter);
    }
}
