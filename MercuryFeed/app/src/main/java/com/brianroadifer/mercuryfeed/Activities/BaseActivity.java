package com.brianroadifer.mercuryfeed.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian Roadifer on 6/28/2016.
 */
public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DatabaseReference database;
    List<Feed> feeds = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                }
                Log.d("onDataChange", "Value: " + feeds);
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(BaseActivity.this);
                Menu menu = navigationView.getMenu();
                for(final Feed fd : feeds){
                    Log.d("onDataChange", "Title: " + fd.toMap().toString());
                    final MenuItem item = menu.add(R.id.nav_feed_group, Menu.NONE, Menu.FLAG_APPEND_TO_GROUP, fd.Title);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("URL", fd.FeedUrl);
                    intent.putExtra("Title", fd.Title);
                    item.setIntent(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("getFeeds:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d("setmeup", id +"");
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if(id == R.id.nav_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if(id == R.id.nav_add_feed){
            startActivity(new Intent(this, AddFeedActivity.class));
        } else if(id == R.id.nav_tags){
            startActivity(new Intent(this, TagActivity.class));
        }else{
            finish();
            startActivity(item.getIntent());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public List<Feed> getFeeds(){
        return feeds;
    }
}
