package com.brianroadifer.mercuryfeed.Activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private final DatabaseReference feedDB = FirebaseDatabase.getInstance().getReference().child("feeds");
    private final List<Feed> feeds = new ArrayList<>();
    private final List<Feed> queryFeed = new ArrayList<>();
    private ProgressDialog progressDialog;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Search Results");
        progressDialog = new ProgressDialog(this, R.style.AppTheme_ProgressDialog);
        progressDialog.setMessage("Searching Feeds");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
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
        MenuItem search = menu.findItem(R.id.action_one);
        search.setIcon(R.drawable.ic_search_white_48dp);
        search.setTitle("Search");
        menu.findItem(R.id.action_two).setVisible(false);
        menu.findItem(R.id.action_three).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_one:
                createDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
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
            Log.d(TAG, "handleIntent:query-"+query);
            for(Feed feed : feeds){
                if(feed.Title.toLowerCase().contains(query.toLowerCase())){
                    queryFeed.add(feed);
                }
            }
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            SearchAdapter searchAdapter = new SearchAdapter(queryFeed, this);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(searchAdapter);
            progressDialog.dismiss();
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

    private void createDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View feedDialogView = factory.inflate(R.layout.add_feed_dialog, null);
        final AlertDialog feedDialog = new AlertDialog.Builder(this).create();
        final EditText addSearch = feedDialogView.findViewById(R.id.feed_dialog_search);
        final AutoCompleteTextView addTitle = feedDialogView.findViewById(R.id.feed_dialog_title);
        final AutoCompleteTextView addUrl = feedDialogView.findViewById(R.id.feed_dialog_url);
        Button button = feedDialogView.findViewById(R.id.tag_dialog_btn_yes);
        button.setText("Search");
        TextView textView = feedDialogView.findViewById(R.id.textView2);
        textView.setText("Search Feeds");
        addTitle.setVisibility(View.GONE);
        addUrl.setVisibility(View.GONE);

        feedDialog.setView(feedDialogView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed = addSearch.getText().toString();
                Intent query = new Intent(SearchResultsActivity.this, SearchResultsActivity.class);
                query.setAction(Intent.ACTION_SEARCH);
                query.putExtra(SearchManager.QUERY, feed);
                finish();
                startActivity(query);
                feedDialog.dismiss();
            }
        });
        feedDialog.show();
    }
}
