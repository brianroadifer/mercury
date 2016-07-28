package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.DatabaseHelper;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian Roadifer on 6/28/2016.
 */
public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener  {
    public static final String TAG = "BaseActivity";
    public static final String ANONYMOUS = "anonymous";
    String mUsername;
    String mPhotoUrl;
    String mEmail;
    private SharedPreferences preferences;


    FirebaseAuth fireAuth;
    FirebaseUser firebaseUser;

    private GoogleApiClient apiClient;

    DatabaseReference feedsDB = FirebaseDatabase.getInstance().getReference().child("feeds");
    DatabaseReference feedsItemDB = FirebaseDatabase.getInstance().getReference().child("feed_items");
    DatabaseReference userFeedsDB;
    public List<Feed> feeds = new ArrayList<>();
    public List<String> subscribed = new ArrayList<>();
    ArticleHelper articleHelper = new ArticleHelper(this);
    TagHelper tagHelper = new TagHelper(this);
    ImageView photo;
    TextView username, email;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
//        View  ll = nav.getHeaderView();
//        photo = (ImageView) ll.findViewById(R.id.user_img);
//        username = (TextView) ll.findViewById(R.id.user_name);
//        email = (TextView) ll.findViewById(R.id.user_email);


        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;

        fireAuth = FirebaseAuth.getInstance();
        firebaseUser = fireAuth.getCurrentUser();

        if(firebaseUser == null){
            startActivity( new Intent(this, GoogleSignInActivity.class));
            finish();
            return;
        }
        userFeedsDB = FirebaseDatabase.getInstance().getReference().child("users/"+ firebaseUser.getUid());
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API).build();
        userFeedsDB.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"userName:OnDataChange:"+ dataSnapshot.getValue().toString());
                mUsername = dataSnapshot.getValue().toString();

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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userFeedsDB.child("profile_picture").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"profilePicture:OnDataChange:"+ dataSnapshot.getValue().toString());
                mPhotoUrl = dataSnapshot.getValue().toString();
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
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setNavigationItemSelectedListener(BaseActivity.this);
                Menu menu = navigationView.getMenu();
                for(final Feed fd : feeds){
                    Log.d(TAG,"onDataChange:title: " + fd.Title);
                    final MenuItem item = menu.add(R.id.nav_feed_group, Menu.FIRST, Menu.FLAG_APPEND_TO_GROUP, fd.Title);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("URL", fd.FeedUrl);
                    intent.putExtra("Title", fd.Title);
                    intent.putExtra("ID", fd.ID);
                    item.setIntent(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"getFeeds:onCancelled", databaseError.toException());
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle options_menu view item clicks here.
        int id = item.getItemId();
        Log.d("setmeup", id +"");
        switch (id) {
            case R.id.nav_all:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case R.id.nav_add_feed:
                startActivity(new Intent(this, AddFeedActivity.class));
//                createDialog();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public List<Feed> getFeeds(){
        return feeds;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:"+connectionResult);
    }

    private void createDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View feedDialogView = factory.inflate(R.layout.add_feed_dialog, null);
        final AlertDialog feedDialog = new AlertDialog.Builder(this).create();
        final AutoCompleteTextView addTitle = (AutoCompleteTextView) feedDialogView.findViewById(R.id.feed_dialog_title);
        final AutoCompleteTextView addUrl = (AutoCompleteTextView) feedDialogView.findViewById(R.id.feed_dialog_url);
        final DatabaseHelper dh = new DatabaseHelper();

        feedDialog.setView(feedDialogView);
        feedDialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = addTitle.getText().toString();
                String url = addUrl.getText().toString();
                if ((title != null || !title.equals("") && (url != null || url.equals("")))) {
                    try {
                        dh.writeFeedToDataBase(url, title);
                        Toast.makeText(getApplicationContext(), "Added " + title, Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (DatabaseException e) {
                        Toast.makeText(getApplicationContext(), "Cannot add " + title, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
                feedDialog.dismiss();
            }
        });
        feedDialog.findViewById(R.id.tag_dialog_btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedDialog.dismiss();
            }
        });
        feedDialog.findViewById(R.id.tag_dialog_btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchResultsActivity.class);
                startActivity(intent);
            }
        });
    }
}
