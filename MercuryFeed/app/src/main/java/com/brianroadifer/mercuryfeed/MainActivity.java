package com.brianroadifer.mercuryfeed;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DatabaseReference database;
    final private String TAG = "FeedDbEvent";
    private Feed feed = new Feed();
    private List<Feed> feeds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        Application application = this.getApplication();
        final Context context = this;
        setContentView(R.layout.activity_main);
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
                navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        return false;
                    }
                });
                Menu menu = navigationView.getMenu();
                for(final Feed fd : feeds){
                    Log.d("onDataChange", "Title: " + fd.toMap().toString());
                    final MenuItem item = menu.add(R.id.nav_feed_group, Menu.NONE, Menu.NONE, fd.Title);
                    Intent intent = new Intent(context, MainActivity.class);
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
        database.push();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String url = bundle.getString("URL");
            String title = bundle.getString("Title");
            setTitle(title);
            feed = new Feed(url, title);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("Settings Open", "Settings oppen");
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(new Intent(getApplicationContext(), AddFeedActivity.class));
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ReadRss readRss = new ReadRss(this, recyclerView);
        if(bundle == null){
            AllFeed(recyclerView, readRss);
        }else{
            readRss.execute(feed);
        }

        LeakCanary.install(application);
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
        getMenuInflater().inflate(R.menu.navigation, menu);
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

        } else if(id == R.id.nav_add_feed){
            startActivity(new Intent(this, AddFeedActivity.class));
        } else{
            Log.w("Settings Open", "Settings oppen");
            startActivity(item.getIntent());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void AllFeed(RecyclerView recyclerView, ReadRss readRss){
        Feed allFeed = new Feed();
        allFeed.Title = "All";
        for(Feed fed : feeds){
            Log.w("ALLFEED", fed.Title);
            readRss.ProcessXml(readRss.GetData(fed.FeedUrl));
            for(Item item : readRss.feedItems){
                allFeed.Items.add(item);
                Log.w("ALLFEED", item.getTitle());
            }
        }
        MyAdapter adapter = new MyAdapter(allFeed, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        Log.w("ALLFEED", "all");
    }
}
