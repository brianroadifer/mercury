package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener  {
    private static final String TAG = "BaseActivity";
    private static final String ANONYMOUS = "anonymous";
    private String mUsername;
    private String mPhotoUrl;
    private String mEmail;
    private SharedPreferences preferences;


    FirebaseAuth fireAuth;
    private FirebaseUser firebaseUser;

    private final DatabaseReference feedsDB = FirebaseDatabase.getInstance().getReference().child("feeds");
    final DatabaseReference feedsItemDB = FirebaseDatabase.getInstance().getReference().child("feed_items");
    private DatabaseReference userFeedsDB;
    final List<Feed> feeds = new ArrayList<>();
    final List<String> subscribed = new ArrayList<>();
    ArticleHelper articleHelper;
    TagHelper tagHelper;
    private ImageView photo;
    private TextView username;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString("app_screen", "Light");
        String primary = preferences.getString("app_primary", "Blue");
        String accent = preferences.getString("app_accent", "Blue");
        String status = preferences.getString("app_status", "Blue");
        String navigation = preferences.getString("app_navigation", "Black");
        decideTheme(theme, primary, accent, status, navigation);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        articleHelper = new ArticleHelper(this);
        tagHelper = new TagHelper(this);

        mUsername = ANONYMOUS;

        fireAuth = FirebaseAuth.getInstance();
        firebaseUser = fireAuth.getCurrentUser();

        if(firebaseUser == null){
            startActivity( new Intent(this, GoogleSignInActivity.class));
            finish();
            return;
        }
        userFeedsDB = FirebaseDatabase.getInstance().getReference().child("users/"+ firebaseUser.getUid());
        new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API).build();
        userFeedsDB.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"userName:OnDataChange:"+ dataSnapshot.getValue().toString());
                mUsername = dataSnapshot.getValue().toString();
                NavigationView navigationView = findViewById(R.id.nav_view);
                View  ll = navigationView.getHeaderView(0);
                username = ll.findViewById(R.id.user_name);
                username.setText(dataSnapshot.getValue().toString());
                username.setText(mUsername);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        userFeedsDB.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"email:OnDataChange:"+ dataSnapshot.getValue().toString());
                mEmail = dataSnapshot.getValue().toString();
                NavigationView navigationView = findViewById(R.id.nav_view);
                View  ll = navigationView.getHeaderView(0);
                email = ll.findViewById(R.id.user_email);
                email.setText(mEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userFeedsDB.child("profile_picture").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"profilePicture:OnDataChange:"+ dataSnapshot.getValue().toString());
                NavigationView navigationView = findViewById(R.id.nav_view);
                View  ll = navigationView.getHeaderView(0);
                photo = ll.findViewById(R.id.user_img);
                mPhotoUrl = dataSnapshot.getValue().toString();
                Picasso.with(getApplicationContext()).load(mPhotoUrl).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).resize(200,200).into(photo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userFeedsDB.child("/subscribed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    Log.w(TAG,"subscribed"+child.getKey());
                    subscribed.add(child.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        feedsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                dataSnapshot.getKey();
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for(DataSnapshot snap: snapshots) {
                    if (subscribed.contains(snap.getKey())) {
                        Log.w(TAG, "feeds:" + snap.getKey());

                        String url = "";
                        String title = "";
                        String id = "";
                        for (DataSnapshot child : snap.getChildren()) {
                            if (child.getKey().equals("title")) {
                                title = child.getValue(String.class);
                            }
                            if (child.getKey().equals("feedUrl")) {
                                url = child.getValue(String.class);
                            }
                            id = snap.getKey();
                        }
                        feeds.add(new Feed(url, title, id));
                    }
                }
                Log.d(TAG,"onDataChange:value: " + feeds);
                NavigationView navigationView = findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(BaseActivity.this);
                Menu menu = navigationView.getMenu();
                for(final Feed fd : sortAscending()){
                    Log.d(TAG,"onDataChange:title: " + fd.Title);
                    final MenuItem item = menu.add(R.id.nav_feed_group, Menu.FIRST, Menu.FLAG_APPEND_TO_GROUP, fd.Title);
                    item.setIcon(R.drawable.ic_rss);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("URL", fd.FeedUrl);
                    intent.putExtra("Title", fd.Title);
                    intent.putExtra("ID", fd.ID);
                    item.setIntent(intent);
                }
                View  ll = navigationView.getHeaderView(0);
                photo = ll.findViewById(R.id.user_img);
                username = ll.findViewById(R.id.user_name);
                email = ll.findViewById(R.id.user_email);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"getFeeds:onCancelled", databaseError.toException());
            }

        });

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle options_menu view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_all:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.nav_tags:
                startActivity(new Intent(this, TagActivity.class));
                break;
            case R.id.nav_articles:
                startActivity(new Intent(this, ArticleActivity.class));
                break;

            default:
                if(item.getIntent() != null){
                    startActivity(item.getIntent());
                }else{
                    Toast.makeText(this, "Action is not working", Toast.LENGTH_SHORT).show();
                }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private List<Feed> sortAscending(){
        List<Feed> feeds = this.feeds;
        Collections.sort(feeds, new Comparator<Feed>() {
            @Override
            public int compare(Feed f1, Feed f2) {
                return f1.compareTo(f2);
            }
        });
        return feeds;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:"+connectionResult);
    }
}
