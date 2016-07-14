package com.brianroadifer.mercuryfeed.Activities;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.brianroadifer.mercuryfeed.Helpers.FeedItemAdapter;
import com.brianroadifer.mercuryfeed.Helpers.ReadRss;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.R;
import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {
    final private String TAG = "FeedDbEvent";
    private Feed feed = new Feed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        Application application = this.getApplication();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String url = bundle.getString("URL");
            String title = bundle.getString("Title");
            setTitle(title);
            feed = new Feed(url, title);
        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ReadRss readRss = new ReadRss(this, recyclerView);
        if(bundle == null){
            feed = AllFeed(readRss);
        }else{
            try {
                feed = readRss.execute(feed).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        FeedItemAdapter feedItemAdapter = new FeedItemAdapter(feed, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(feedItemAdapter);
        LeakCanary.install(application);
    }


    public Feed AllFeed(ReadRss readRss){
        Feed allFeed = new Feed();
        allFeed.Title = "All";
        for(Feed fed : getFeeds()){
            try {
                allFeed.Items.addAll(readRss.execute(fed).get().Items);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        Log.w("ALLFEED", "all");
        return allFeed;
    }
}
