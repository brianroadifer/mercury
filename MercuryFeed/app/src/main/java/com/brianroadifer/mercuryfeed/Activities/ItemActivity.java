package com.brianroadifer.mercuryfeed.Activities;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ShareActionProvider;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ItemActivity extends AppCompatActivity {
    FloatingActionButton fab, fabBrowser, fabRead,fabSave;

    boolean status = false;

    Animation show_fab_browser;
    Animation hide_fab_browser;
    Animation show_fab_read;
    Animation hide_fab_read;
    Animation show_fab_save;
    Animation hide_fab_save;

    String title,url,imageUrl,author,description,date;

    ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        Application application = this.getApplication();

        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.item_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        show_fab_browser = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_browser_show);
        hide_fab_browser = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_browser_hide);
        show_fab_read = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_read_show);
        hide_fab_read = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_read_hide);
        show_fab_save = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_read_show);
        hide_fab_save = AnimationUtils.loadAnimation(getApplication(), R.anim.fab_read_hide);

        imageUrl = bundle.getString("Image");
        title = bundle.getString("Title");
        author = bundle.getString("Author");
        description = bundle.getString("Description");
        date = formatDate((Date) bundle.get("Date"));
        url = bundle.getString("Link");



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabBrowser = (FloatingActionButton) findViewById(R.id.fab_browser);
        fabRead = (FloatingActionButton) findViewById(R.id.fab_read);
        fabSave = (FloatingActionButton) findViewById(R.id.fab_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status){
                    hideFab();
                    status = false;
                }else{
                   showFab();
                    status = true;
                }
            }
        });
        fabBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + activity.getPackageName()));
                customTabsIntent.launchUrl(activity, Uri.parse(url));
                hideFab();
                status = false;
            }
        });
        fabRead.setImageResource(R.drawable.ic_chrome_reader_mode_white_48dp);
        fabRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ArticleItemActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
                hideFab();
                status = false;
            }
        });
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabSave.setImageResource(R.drawable.ic_bookmark_white_48dp);
                ReadArticle readArticle = new ReadArticle();
                Article article = new Article();
                try {
                    article = readArticle.execute(url).get();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                ArticleHelper ah = new ArticleHelper(getApplicationContext());
                ah.SaveArticle(article);
                hideFab();
                status = false;
            }
        });
        ImageView imageView = (ImageView) findViewById(R.id.item_image);

        String thumb = imageUrl.isEmpty()? null: imageUrl;
        Picasso.with(this).load(thumb).placeholder(R.drawable.test).error(R.drawable.test).into(imageView);
        TextView titleView = (TextView) findViewById(R.id.item_title);
        titleView.setText(title);
        TextView descriptionView = (TextView) findViewById(R.id.item_description);
        
        descriptionView.setText(Html.fromHtml(description));
        descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView infoView = (TextView) findViewById(R.id.item_info);
        infoView.setText("by " + author + " / " + date);




        LeakCanary.install(application);
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
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent,"Share Via"));
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
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fabRead.getLayoutParams();
        layoutParams2.rightMargin += (int) (fabRead.getWidth() * 1.5);
        layoutParams2.bottomMargin += (int) (fabRead.getHeight() * 1.5);
        fabRead.setLayoutParams(layoutParams2);
        fabRead.startAnimation(show_fab_read);
        fabRead.setClickable(true);

        //Floating Action Button 3
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fabSave.getLayoutParams();
        layoutParams3.rightMargin += (int) (fabSave.getWidth() * 0.25);
        layoutParams3.bottomMargin += (int) (fabSave.getHeight() * 1.7);
        fabSave.setLayoutParams(layoutParams3);
        fabSave.startAnimation(show_fab_save);
        fabSave.setClickable(true);
    }

    private void hideFab(){
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fabBrowser.getLayoutParams();
        layoutParams.rightMargin -= (int) (fabBrowser.getWidth() * 1.7);
        layoutParams.bottomMargin -= (int) (fabBrowser.getHeight() * 0.25);
        fabBrowser.setLayoutParams(layoutParams);
        fabBrowser.startAnimation(hide_fab_browser);
        fabBrowser.setClickable(false);

        //Floating Action Button 2
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) fabRead.getLayoutParams();
        layoutParams2.rightMargin -= (int) (fabRead.getWidth() * 1.5);
        layoutParams2.bottomMargin -= (int) (fabRead.getHeight() * 1.5);
        fabRead.setLayoutParams(layoutParams2);
        fabRead.startAnimation(hide_fab_read);
        fabRead.setClickable(false);

        //Floating Action Button 3
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) fabSave.getLayoutParams();
        layoutParams3.rightMargin -= (int) (fabSave.getWidth() * 0.25);
        layoutParams3.bottomMargin -= (int) (fabSave.getHeight() * 1.7);
        fabSave.setLayoutParams(layoutParams3);
        fabSave.startAnimation(hide_fab_save);
        fabSave.setClickable(false);
    }

    public String formatDate(Date date){
        SimpleDateFormat format;
        try {
            format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
            return  format.format(date);
        }catch (NullPointerException ne){
            return "";
        }
    }

    private String htmlSetup(String content){
        return "<html><body>"+ content + "</body></html>";
    }

}
