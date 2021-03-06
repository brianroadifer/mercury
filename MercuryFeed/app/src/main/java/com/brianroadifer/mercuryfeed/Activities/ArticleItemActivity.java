package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;
import com.greenfrvr.hashtagview.HashtagView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArticleItemActivity extends AppCompatActivity {
    private boolean isRead= false;
    private Article article;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = pref.getString("article_theme", "Light");
        String justify = pref.getString("article_just", "Left");
        String size = pref.getString("article_size", "Left");
        String fontFamily = pref.getString("article_family", "Sans Serif");
        boolean full = pref.getBoolean("article_full", false);
        decideTheme(themeName);
        setContentView(R.layout.activity_article_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setFullScreen(full);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if(bundle.containsKey("isRead")){
                isRead = bundle.containsKey("isRead");
                article = (Article) bundle.get("Article");
                if(article != null && article.Tags == null){
                    article.Tags = new ArrayList<>();
                }
            }else if(bundle.containsKey("Article")){
                article = (Article) bundle.get("Article");
                if(article != null && article.Tags == null){
                    article.Tags = new ArrayList<>();
                }
            }
        }


        TextView title = findViewById(R.id.article_title);

        TextView content = findViewById(R.id.article_content);
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
        decideJustify(justify,content,title);
        decideSize(size,content,title);
        decideFamily(fontFamily, content,title);
        final HashtagView tags = findViewById(R.id.article_tag_view);
        if(!isRead){
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
                        return new SpannableString(label);
                    }
                });
            }
        }else{
            findViewById(R.id.scrollView).setVisibility(View.GONE);
            findViewById(R.id.article_tag_header).setVisibility(View.GONE);
            findViewById(R.id.article_tag_view).setVisibility(View.GONE);
        }



    }

    private void decideTheme(String themeName) {
        switch (themeName.toLowerCase()){
            case "light":
                getTheme().applyStyle(R.style.Article_Light, true);
                break;
            case "dark":
                getTheme().applyStyle(R.style.Article_Dark, true);
                break;
            case "sepia":
                getTheme().applyStyle(R.style.Article_Sepia, true);
                break;
            case "green":
                getTheme().applyStyle(R.style.Article_Green, true);
                break;
            case "white":
                getTheme().applyStyle(R.style.Article_White, true);
                break;
            case "black":
                getTheme().applyStyle(R.style.Article_Black, true);
                break;
            default:
                getTheme().applyStyle(R.style.Article_Light, true);
        }
    }
    private void decideJustify(String justify, TextView... views) {
        switch (justify){
            case "Left":
                for(TextView view: views) {
                    view.setGravity(Gravity.START);
                }
                break;
            case "Right":
                for(TextView view: views) {
                    view.setGravity(Gravity.START);
                }
                break;
            default:
                for(TextView view: views) {
                    view.setGravity(Gravity.START);
                }
        }
    }
    private void decideSize(String size, TextView... views) {
        switch (size){
            case "8":
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,14f);
                break;
            case "12":
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP,12f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,18f);
                break;
            case "16":
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP,16f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,22f);
                break;
            case "20":
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP,20f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,26f);
                break;
            case "24":
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP,24f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,30f);
                break;
            case "28":
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP,28f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,34f);
                break;
            default:
                views[0].setTextSize(TypedValue.COMPLEX_UNIT_SP,12f);
                views[1].setTextSize(TypedValue.COMPLEX_UNIT_SP,18f);
        }
    }

    private void decideFamily(String family, TextView... views) {
        family = family.toLowerCase();
        family = family.replace(" ","_");

        Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/"+family+".ttf");
        for(TextView view:views) {
            view.setTypeface(typeFace);
        }
    }


    private void createDialog(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View tagDialogView = factory.inflate(R.layout.add_tag_dialog, null);
        final AlertDialog tagDialog = new AlertDialog.Builder(this).create();
        final MultiAutoCompleteTextView edit = tagDialogView.findViewById(R.id.tag_dialog_edit);
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
                for(Tag arTag: article.Tags){
                    articleTags.add(arTag.Name);
                }
                for(String t: tags){
                    if(!articleTags.contains(t)){
                        Tag temp = new Tag();
                        temp.Name = t;
                        temp.ID = UUID.randomUUID().toString();
                        article.Tags.add(temp);
                    }
                }
                ArticleHelper articleHelper = new ArticleHelper(getApplicationContext());
                articleHelper.SaveArticle(article);
                Intent intent = new Intent(ArticleItemActivity.this, ArticleItemActivity.class);
                intent.putExtra("Article", article);
                startActivity(intent);
                tagDialog.dismiss();
                finish();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_share:
                shareItemURL();
                break;
            case R.id.action_second:
                createDialog();
                break;
            case R.id.action_third:
                if(isRead){
                    saveArticle();
                }else{
                    deleteArticle();
                }
                finish();
                break;
            case R.id.action_browser:
                openChrome();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_feed_item, menu);

        if(isRead){
            MenuItem delete = menu.findItem(R.id.action_third);
            delete.setIcon(R.drawable.ic_articles);
            delete.setTitle("Save Article");
            MenuItem tags = menu.findItem(R.id.action_second);
            tags.setIcon(R.drawable.ic_label);
            tags.setTitle("Add Tags");
            tags.setVisible(false);
        }else{
            MenuItem tags = menu.findItem(R.id.action_second);
            tags.setIcon(R.drawable.ic_label);
            tags.setTitle("Add Tags");
            tags.setVisible(true);
            MenuItem delete = menu.findItem(R.id.action_third);
            delete.setIcon(R.drawable.ic_delete_white_48dp);
            delete.setTitle("Delete Article");
        }


        return true;
    }
    private void shareItemURL(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.Title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.URL);
        startActivity(Intent.createChooser(shareIntent,"Share Via"));
    }

    private void saveArticle(){
        ArticleHelper articleHelper = new ArticleHelper(this);
        articleHelper.SaveArticle(article);
        Intent intent = new Intent(ArticleItemActivity.this, ArticleItemActivity.class);
        intent.putExtra("Article", article);
        startActivity(intent);
    }
    private void deleteArticle(){
        ArticleHelper articleHelper = new ArticleHelper(this);
        File file = new File(getFilesDir(), ArticleHelper.FILENAME + article.ID);
        articleHelper.DeleteArticle(file);
        if(ArticleHelper.isExternalStorageReadable() && ArticleHelper.isExternalStorageWritable()){
            file = new File(getExternalFilesDir(null), ArticleHelper.FILENAME + article.ID);
            articleHelper.DeleteArticle(file);
        }
        finish();
    }
    private void openChrome(){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;
        builder.setToolbarColor(color);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + getPackageName()));
        customTabsIntent.launchUrl(this, Uri.parse(article.URL));
    }

    private void setFullScreen(boolean fullScreen){
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if(fullScreen){
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

}
