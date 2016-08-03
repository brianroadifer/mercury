package com.brianroadifer.mercuryfeed.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.brianroadifer.mercuryfeed.Helpers.ArticleAdapter;
import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;

import java.util.ArrayList;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {
    List<Article> articles = new ArrayList<>();
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
        setContentView(R.layout.activity_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ArticleHelper articleHelper = new ArticleHelper(getApplicationContext());


        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            for(int i = 0; i< bundle.size(); i++){
                articles.add((Article) bundle.get("article"+ i));
            }
            setTitle((String) bundle.get("title"));
        }else {
            articles = articleHelper.LoadArticles();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Delete All Articles?", Snackbar.LENGTH_LONG).setAction("Delete", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        articleHelper.DeleteArticles();
                        Snackbar.make(v, "Articles Deleted", Snackbar.LENGTH_LONG).show();
                        recreate();
                    }
                }).show();
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

        ArticleAdapter articleAdapter = new ArticleAdapter(articles, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.article_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        if(layoutManager == null){
            Log.w("Article:Layout", "LayoutManager is null");
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(articleAdapter);
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
