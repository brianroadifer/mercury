package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;
import com.greenfrvr.hashtagview.HashtagView;

import java.util.ArrayList;
import java.util.List;
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
        final HashtagView tags = (HashtagView) findViewById(R.id.article_tag_view);
        tags.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag t = (Tag) item;
                List<Article> articles = new ArrayList<>();
                ArticleHelper ah = new ArticleHelper(getApplicationContext());
                for(Article article : ah.LoadArticles()){
                    for(Tag tag: article.Tags) {
                        if(tag.Name.equalsIgnoreCase(t.Name)){
                            articles.add(article);
                        }
                    }
                }
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("ARTICLES", article);
                startActivity(intent);
            }
        });
        if(article.Tags != null) {
            tags.setData(article.Tags, new HashtagView.DataStateTransform<Tag>() {
                @Override
                public CharSequence prepareSelected(Tag item) {
                    return null;
                }

                @Override
                public CharSequence prepare(Tag item) {
                    String label = item.Name;
                    SpannableString spannableString = new SpannableString(label);
                    return spannableString;
                }
            });
        }

    }

}
