package com.brianroadifer.mercuryfeed.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;

import java.util.concurrent.ExecutionException;

public class ArticleItemActivity extends AppCompatActivity {
    Article article;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        String URL;
        if(bundle.containsKey("Article")){
            article = (Article) bundle.get("Article");
        }else if(bundle.containsKey("url")){
            URL = bundle.getString("url");
            ReadArticle readArticle = new ReadArticle();
            try {
                article = readArticle.execute(URL).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        TextView title = (TextView) findViewById(R.id.article_title);
        TextView content = (TextView) findViewById(R.id.article_content);
        title.setText(article.Title);
        content.setText(article.Content);

    }

}
