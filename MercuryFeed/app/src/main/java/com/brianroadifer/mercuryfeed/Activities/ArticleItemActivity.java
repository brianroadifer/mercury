package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;
import com.greenfrvr.hashtagview.HashtagView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
            if(article.Tags == null){
                article.Tags = new ArrayList<>();
            }
        }else if(bundle.containsKey("url")){
            URL = bundle.getString("url");
            ReadArticle readArticle = new ReadArticle();
            try {
                article = readArticle.execute(URL).get();
                if(article.Tags == null){
                    article.Tags = new ArrayList<>();
                }
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
        tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(article);
            }
        });
        tags.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag t = (Tag) item;
                ArticleHelper ah = new ArticleHelper(getApplicationContext());
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                int i = 0;
                for(Article article : ah.LoadArticles()){
                    for(Tag tag: article.Tags) {
                        if(tag.Name.equalsIgnoreCase(t.Name)){
                            intent.putExtra("article" + (i++), article);
                        }
                    }
                }
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

    private void createDialog(final Article art){
        LayoutInflater factory = LayoutInflater.from(this);
        final View tagDialogView = factory.inflate(R.layout.add_tag_dialog, null);
        final AlertDialog tagDialog = new AlertDialog.Builder(this).create();
        tagDialog.setView(tagDialogView);
        tagDialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit = (EditText) tagDialogView.findViewById(R.id.tag_dialog_edit);
                String tag = edit.getText().toString();
                String[] tags = tag.split(",");
                for(String t: tags){
                    Tag temp = new Tag();
                    temp.Name = t;
                    temp.ID = UUID.randomUUID().toString();
                    art.Tags.add(temp);
                }
                ArticleHelper articleHelper = new ArticleHelper(getApplicationContext());
                articleHelper.SaveArticle(art);
                tagDialog.dismiss();
            }
        });
        tagDialogView.findViewById(R.id.tag_dialog_btn_no).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tagDialog.dismiss();
            }
        });

        tagDialog.show();
    }

}
