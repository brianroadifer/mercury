package com.brianroadifer.mercuryfeed.Activities;

import android.app.Application;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;

import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.Models.Item;
import com.brianroadifer.mercuryfeed.Helpers.FeedItemAdapter;
import com.brianroadifer.mercuryfeed.R;
import com.brianroadifer.mercuryfeed.Helpers.ReadRss;

import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    final private String TAG = "FeedDbEvent";
    private Feed feed = new Feed();
    private List<Feed> feeds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        Application application = this.getApplication();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        feeds = getFeeds();
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
                Snackbar.make(view, "Feature not complete", Snackbar.LENGTH_SHORT).show();
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
        FeedItemAdapter adapter = new FeedItemAdapter(allFeed, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        Log.w("ALLFEED", "all");
    }
}
