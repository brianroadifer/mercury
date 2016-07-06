package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;
import com.greenfrvr.hashtagview.HashtagView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TagActivity extends BaseActivity {
    List<Tag> tagList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        HashtagView hashtagView = (HashtagView) findViewById(R.id.tag_view);
        List<Article> articles = articleHelper.LoadArticles();
        final List<Tag> tagList = new ArrayList<>();
        for (Article article : articles) {
            if(article.Tags != null) {
                for (Tag tag : article.Tags) {
                    tagList.add(tag);
                }
            }
        }
        for(Tag tag : tagHelper.LoadTags()){
            tagList.add(tag);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        HashtagView.DataStateTransform<Tag> stateTransform = new HashtagView.DataStateTransform<Tag>() {
            @Override
            public CharSequence prepareSelected(Tag item) {
                String label = item.Name;
                SpannableString spannableString = new SpannableString(label);
                return spannableString;
            }

            @Override
            public CharSequence prepare(Tag item) {
                String label = item.Name;
                SpannableString spannableString = new SpannableString(label);
                return spannableString;
            }
        };
        hashtagView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag t = (Tag) item;
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                intent.putExtra("title", t.Name);
                ArticleHelper ah = new ArticleHelper(getApplicationContext());
                int i = 0;
                for(Article article : ah.LoadArticles()){
                    for(Tag tag: article.Tags) {
                        if(tag.Name.equalsIgnoreCase(t.Name)){
                            intent.putExtra("article"+ (i++), article);
                        }
                    }
                }
                startActivity(intent);
            }
        });
        hashtagView.addOnTagSelectListener(new HashtagView.TagsSelectListener() {
            @Override
            public void onItemSelected(Object item, boolean selected) {
                Tag t = (Tag) item;
                tagHelper.DeleteTag(TagHelper.FILENAME + t.ID);
            }
        });
        hashtagView.setTransformer(stateTransform);
        hashtagView.setData(tagList);
    }

   private void createDialog(){
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
               List<Tag> tagList = new ArrayList<>();
               for(String t: tags){
                   Tag temp = new Tag();
                   temp.Name = t;
                   temp.ID = UUID.randomUUID().toString();
                   tagList.add(temp);
               }
               TagHelper tagHelper = new TagHelper(getApplicationContext());
               tagHelper.SaveTags(tagList);
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

