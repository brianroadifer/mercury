package com.brianroadifer.mercuryfeed.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;
import com.greenfrvr.hashtagview.HashtagView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ArticleItemActivity extends AppCompatActivity {
    FloatingActionButton fab, fabBrowser, fabDelete, fabTag;

    Animation show_fab_browser;
    Animation hide_fab_browser;
    Animation show_fab_tag;
    Animation hide_fab_tag;
    Animation show_fab_delete;
    Animation hide_fab_delete;

    boolean status = false;

    Article article;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
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

        show_fab_browser = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_browser_show);
        hide_fab_browser = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_browser_hide);
        show_fab_tag = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_read_show);
        hide_fab_tag = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_read_hide);
        show_fab_delete = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_save_show);
        hide_fab_delete = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_save_hide);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fabBrowser = (FloatingActionButton) findViewById(R.id.fab_browser);
        fabTag = (FloatingActionButton) findViewById(R.id.fab_read);
        fabDelete = (FloatingActionButton) findViewById(R.id.fab_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status) {
                    hideFab();
                    status = false;
                }else {
                    showFab();
                    status = true;
                }
            }
        });
        fabBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + getPackageName()));
                customTabsIntent.launchUrl(activity, Uri.parse("http://www.facebook.com"));
                hideFab();
                status = false;
            }
        });
        fabTag.setImageResource(R.drawable.ic_label_white_48dp);
        fabTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(article);
                hideFab();
                status = false;
            }
        });
        fabDelete.setImageResource(R.drawable.ic_delete_white_48dp);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArticleHelper articleHelper = new ArticleHelper(activity);
                articleHelper.DeleteArticle(ArticleHelper.FILENAME + article.ID);
                finish();
            }
        });


        TextView title = (TextView) findViewById(R.id.article_title);

        TextView content = (TextView) findViewById(R.id.article_content);
        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                Bitmap image = null;
                try {
                    image = Picasso.with(getApplicationContext()).load(source).error(R.drawable.test).placeholder(R.drawable.test).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BitmapDrawable bitDraw = new BitmapDrawable(getResources(),image);
                if(image != null){
                    return bitDraw.getCurrent();
                }
                return null;
            }
        };
        content.setText(Html.fromHtml(article.Content, imageGetter, null));
        title.setText(article.Title);
        content.setText(article.Content);
        final HashtagView tags = (HashtagView) findViewById(R.id.article_tag_view);
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
                intent.putExtra("title", t.Name.toLowerCase());
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
        final MultiAutoCompleteTextView edit = (MultiAutoCompleteTextView) tagDialogView.findViewById(R.id.tag_dialog_edit);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.tag_suggestion, getAllTags());
        edit.setAdapter(adapter);
        edit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        edit.setThreshold(1);

        tagDialog.setView(tagDialogView);
        tagDialogView.findViewById(R.id.tag_dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = edit.getText().toString();
                String[] tags = tag.replaceAll("^[,\\s]+", "").split("[,\\s]+");
                List<String> articleTags = new ArrayList<>();
                for(Tag arTag: art.Tags){
                    articleTags.add(arTag.Name);
                }
                for(String t: tags){
                    if(!articleTags.contains(t)){
                        Tag temp = new Tag();
                        temp.Name = t;
                        temp.ID = UUID.randomUUID().toString();
                        art.Tags.add(temp);
                    }
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
    private String[] getAllTags(){
        Map<String, Tag> unq = new HashMap<>();
        ArticleHelper ah = new ArticleHelper(getApplicationContext());
        TagHelper th = new TagHelper(getApplicationContext());
        List<Tag> tagList = new ArrayList<>();
        for (Article article : ah.LoadArticles()) {
            if(article.Tags != null) {
                for (Tag tag : article.Tags) {
                    unq.put(tag.Name.toLowerCase(), tag);
                }
            }
        }
        for(Tag tag : th.LoadTags()){
            unq.put(tag.Name.toLowerCase(), tag);
        }

        tagList.addAll(unq.values());
        List<String> names = new ArrayList<>();
        for(Tag tag: tagList){
            names.add(tag.Name);
        }
        String[] tags = new String[tagList.size()];
        for(int i = 0; i < tagList.size(); i++){
            tags[i] = names.get(i);
        }
        return tags;
    }

    private void showFab() {

        //Floating Action Button 1
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fabBrowser.getLayoutParams();
        layoutParams.rightMargin += (int) (fabBrowser.getWidth() * 1.7);
        layoutParams.bottomMargin += (int) (fabBrowser.getHeight() * 0.25);
        fabBrowser.setLayoutParams(layoutParams);
        fabBrowser.startAnimation(show_fab_browser);
        fabBrowser.setClickable(true);

        //Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fabTag.getLayoutParams();
        layoutParams2.rightMargin += (int) (fabTag.getWidth() * 1.5);
        layoutParams2.bottomMargin += (int) (fabTag.getHeight() * 1.5);
        fabTag.setLayoutParams(layoutParams2);
        fabTag.startAnimation(show_fab_tag);
        fabTag.setClickable(true);

        //Floating Action Button 3
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fabDelete.getLayoutParams();
        layoutParams3.rightMargin += (int) (fabDelete.getWidth() * 0.25);
        layoutParams3.bottomMargin += (int) (fabDelete.getHeight() * 1.7);
        fabDelete.setLayoutParams(layoutParams3);
        fabDelete.startAnimation(show_fab_delete);
        fabDelete.setClickable(true);
    }

    private void hideFab(){
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fabBrowser.getLayoutParams();
        layoutParams.rightMargin -= (int) (fabBrowser.getWidth() * 1.7);
        layoutParams.bottomMargin -= (int) (fabBrowser.getHeight() * 0.25);
        fabBrowser.setLayoutParams(layoutParams);
        fabBrowser.startAnimation(hide_fab_browser);
        fabBrowser.setClickable(false);

        //Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fabTag.getLayoutParams();
        layoutParams2.rightMargin -= (int) (fabTag.getWidth() * 1.5);
        layoutParams2.bottomMargin -= (int) (fabTag.getHeight() * 1.5);
        fabTag.setLayoutParams(layoutParams2);
        fabTag.startAnimation(hide_fab_tag);
        fabTag.setClickable(false);

        //Floating Action Button 3
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fabDelete.getLayoutParams();
        layoutParams3.rightMargin -= (int) (fabDelete.getWidth() * 0.25);
        layoutParams3.bottomMargin -= (int) (fabDelete.getHeight() * 1.7);
        fabDelete.setLayoutParams(layoutParams3);
        fabDelete.startAnimation(hide_fab_delete);
        fabDelete.setClickable(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_share:
                shareItemURL();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_feed_item, menu);
        return true;
    }
    public void shareItemURL(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.Title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.URL);
        startActivity(Intent.createChooser(shareIntent,"Share Via"));
    }

}
