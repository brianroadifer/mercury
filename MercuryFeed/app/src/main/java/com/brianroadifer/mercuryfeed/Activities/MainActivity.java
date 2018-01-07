package com.brianroadifer.mercuryfeed.Activities;

import android.app.Application;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.DatabaseHelper;
import com.brianroadifer.mercuryfeed.Helpers.FeedItemAdapter;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.Models.Item;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.ValueEventListener;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private Feed feed = new Feed();

    private boolean isSearch;

    private ProgressDialog progressDialog;
    private boolean sort = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString("app_screen", "Light");
        String primary = pref.getString("app_primary", "Blue");
        String accent = pref.getString("app_accent", "Blue");
        String status = pref.getString("app_status", "Blue");
        String navigation = pref.getString("app_navigation", "Black");
        String dateTime = pref.getString("offline_time", "-1");
        long dateDelete = pref.getLong("offline_date", -1);
        int limit = pref.getInt("offline_limit", 0);
        decideTheme(theme, primary, accent, status, navigation);
        FirebaseUser user = fireAuth.getCurrentUser();

        if (user != null) {
            Log.d(TAG,"User Signed In");

            Application application = this.getApplication();
            setContentView(R.layout.activity_main);
            View parentLayout = findViewById(R.id.drawer_layout);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String url = bundle.getString("URL");
                String title = bundle.getString("Title");
                String id = bundle.getString("ID");
                Feed passFeed = (Feed) bundle.get("Feed");
                isSearch = bundle.containsKey("Search");
                if(passFeed != null){
                    feed = passFeed;
                    setTitle(feed.Title);
                }else{
                    setTitle(title);
                    feed = new Feed(url, title, id);
                }
                feed.isSearch = isSearch;
            }else{
                setTitle("All");
            }
            String feedName = (getTitle() == null)? "All" : getTitle().toString();
            progressDialog = new ProgressDialog(this, R.style.AppTheme_ProgressDialog);
            progressDialog.setMessage("Loading " + feedName + " Feed");
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();


            if(ArticleHelper.getOfflineArticleSize(getApplicationContext()) >= limit && limit != 0){
                createWarningDialog(limit);
            }
            if(dateTime.equalsIgnoreCase("-1")){
                Log.d(TAG, "autoDeleteCheck:DateDelete" + new Date(dateDelete).toString());
                if(Long.compare(dateDelete, Calendar.getInstance().getTimeInMillis()) >= 0){
                    final ArticleHelper articleHelper = new ArticleHelper(getApplicationContext());
                    final List<Article> toBeDeleted = articleHelper.LoadArticles();
                    articleHelper.DeleteArticles();
                    Snackbar snackbar = Snackbar.make(parentLayout, "Auto Deleted Articles", Snackbar.LENGTH_INDEFINITE)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    articleHelper.SaveArticles(toBeDeleted);
                                    Snackbar snackbar1 = Snackbar.make(view, "Restored Articles", Snackbar.LENGTH_SHORT);
                                    View s1v = snackbar1.getView();
                                    TextView s1tv = s1v.findViewById(android.support.design.R.id.snackbar_text);
                                    s1tv.setTextColor(getResources().getColor(R.color.article_background_white));
                                    snackbar1.show();
                                }
                            });
                    int add = Integer.parseInt(dateTime);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DATE, add);
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = settings.edit();
                    Log.d(TAG, calendar.getTime().toString());
                    editor.putLong("offline_date", calendar.getTimeInMillis());
                    editor.apply();
                    View sv = snackbar.getView();
                    TextView stv = sv.findViewById(android.support.design.R.id.snackbar_text);
                    stv.setTextColor(getResources().getColor(R.color.article_background_white));
                    snackbar.show();
                }
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
                        if(isSearch){
                            user_read = !isSearch;
                        }
                        if (subscribed.contains(id) && !user_read) {
                            item.ID = snap.getKey();
                            item.title = (String) dataSnapshot.child(snap.getKey()).child("title").getValue();
                            item.author = (String) dataSnapshot.child(snap.getKey()).child("author").getValue();
                            item.description = (String) dataSnapshot.child(snap.getKey()).child("description").getValue();
                            item.link = (String) dataSnapshot.child(snap.getKey()).child("link").getValue();
                            item.thumbnailUrl = (String) dataSnapshot.child(snap.getKey()).child("thumbnail").getValue();
                            long unix = (long) dataSnapshot.child(snap.getKey()).child("published").getValue();
                            item.timestamp = new Timestamp(unix * 1000L);
                            if (id != null && id.equalsIgnoreCase(feed.ID)) {
                                for(Feed fd: feeds){
                                    if(fd.ID.equalsIgnoreCase(id)){
                                        feed.Title = fd.Title;
                                    }
                                }
                                setTitle(feed.Title);
                                feed.Items.add(item);
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

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = drawer.findViewById(R.id.nav_view);
            View  ll = navigationView.getHeaderView(0);
            ll.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            });

            LeakCanary.install(application);
        } else {
            startActivity(new Intent(this, GoogleSignInActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem sortFeed = menu.findItem(R.id.action_three);
        sortFeed.setTitle("Sort");
        sortFeed.setIcon(R.drawable.ic_sort);
        MenuItem addFeed = menu.findItem(R.id.action_one);
        addFeed.setTitle("Add Feed");
        addFeed.setIcon(R.drawable.ic_add_black_24dp);
        MenuItem signOut = menu.findItem(R.id.action_two);
        if(getTitle() == null || getTitle().toString().equalsIgnoreCase("All")){
            signOut.setTitle("Sign Out");
            signOut.setIcon(R.drawable.ic_logout);
        }else{
            signOut.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_one:
                createDialog();
                break;
            case R.id.action_two:
                fireAuth.signOut();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();
                recreate();
                break;
            case R.id.action_three:
                sort = !sort;
                handleFeed(feed);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleFeed(Feed feed){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        FeedItemAdapter feedItemAdapter = new FeedItemAdapter(feed, sort, getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(feedItemAdapter);
        progressDialog.dismiss();
    }

    private void decideTheme(String themeName, String primary, String accent, String status, String navigation) {
        ThemeChanger themeChanger = new ThemeChanger(this);
        themeChanger.screenColor(themeName);
        themeChanger.primaryColor(primary);
        themeChanger.accentColor(accent);
        themeChanger.statusColor(status);
        themeChanger.navigationColor(navigation);
        themeChanger.changeTheme();

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.getItem(0).setIcon(R.drawable.ic_home);
        menu.getItem(1).setIcon(R.drawable.ic_articles);
        menu.getItem(2).setIcon(R.drawable.ic_label);


    }

    private void createDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View feedDialogView = factory.inflate(R.layout.add_feed_dialog, null);
        final AlertDialog feedDialog = new AlertDialog.Builder(this).create();
        final EditText addSearch = feedDialogView.findViewById(R.id.feed_dialog_search);
        final AutoCompleteTextView addTitle = feedDialogView.findViewById(R.id.feed_dialog_title);
        final AutoCompleteTextView addUrl = feedDialogView.findViewById(R.id.feed_dialog_url);
        final DatabaseHelper dh = new DatabaseHelper();

        feedDialog.setView(feedDialogView);
        feedDialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed = addSearch.getText().toString();
                String title = addTitle.getText().toString();
                String url = addUrl.getText().toString();
                if((!feed.isEmpty() || feed.isEmpty())&& (title.isEmpty() || url.isEmpty())){
                    Intent query = new Intent(getApplicationContext(), SearchResultsActivity.class);
                    query.setAction(Intent.ACTION_SEARCH);
                    query.putExtra(SearchManager.QUERY, feed);
                    startActivity(query);
                }else if(!title.isEmpty() && !url.isEmpty()){
                    try{
                        dh.writeFeedToDataBase(url, title);
                        Toast.makeText(getApplicationContext(), "Added " + title, Toast.LENGTH_SHORT).show();
                        finish();
                    }catch (DatabaseException e){
                        Toast.makeText(getApplicationContext(), "Cannot add " + title, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
                feedDialog.dismiss();
            }
        });
        feedDialog.show();
    }

    private void createWarningDialog(final int limit) {
        String message = "Cannot save anymore articles.\nDelete older articles to save more?";
        AlertDialog warningDialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle("Article Offline Limit")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArticleHelper articleHelper = new ArticleHelper(getApplicationContext());

                        while (ArticleHelper.getOfflineArticleSize(getApplicationContext()) >= limit) {
                            List<Article> articleList = articleHelper.LoadArticles();
                            Article deleteAgent = new Article();

                            for (Article article : articleList) {
                                if (article.compareTo(deleteAgent) < 0) {
                                    deleteAgent = article;
                                }
                            }
                            File file = new File(getFilesDir(), ArticleHelper.FILENAME + deleteAgent.ID);
                            articleHelper.DeleteArticle(file);
                            if (ArticleHelper.isExternalStorageReadable() && ArticleHelper.isExternalStorageWritable()) {
                                file = new File(getExternalFilesDir(null), ArticleHelper.FILENAME + deleteAgent.ID);
                                articleHelper.DeleteArticle(file);
                            }
                        }
                    }
                }).setIcon(R.drawable.ic_warning)
                .create();
        warningDialog.show();
    }
}
