package com.brianroadifer.mercuryfeed.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.PicassoImageGetter;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemActivity extends AppCompatActivity {

    String title,url,imageUrl,author,description,date;
    Article article = new Article();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString("app_screen", "Light");
        String primary = pref.getString("app_primary", "Blue Grey");
        String accent = pref.getString("app_accent", "Blue Grey");
        String statusBar = pref.getString("app_status", "Blue Grey");
        String navigation = pref.getString("app_navigation", "Black");
        decideTheme(theme, primary, accent, statusBar, navigation);
        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.item_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        imageUrl = bundle.getString("Image");
        title = bundle.getString("Title");
        author = bundle.getString("Author");
        description = bundle.getString("Description");
        date = formatDate((Timestamp) bundle.get("Date"));
        url = bundle.getString("Link");

        ImageView imageView = (ImageView) findViewById(R.id.item_image);

        if(imageUrl.isEmpty()){
            imageView.setVisibility(View.GONE);
        }else{
            Picasso.with(this).load(imageUrl).placeholder(R.drawable.placeholder).error(R.drawable.error).into(imageView);
        }

        TextView titleView = (TextView) findViewById(R.id.item_title);
        titleView.setText(title);
        TextView descriptionView = (TextView) findViewById(R.id.item_description);
        final PicassoImageGetter imageGetter = new PicassoImageGetter(descriptionView,getResources(), Picasso.with(getApplicationContext()));
        Html.ImageGetter nIm = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                return imageGetter.getDrawable(source);
            }
        };
        description = replaceBadTags(description);
        descriptionView.setText(Html.fromHtml(description, nIm, null));

        descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView infoView = (TextView) findViewById(R.id.item_info);
        infoView.setText("by " + author + " / " + date);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_share:
                shareItemURL();
                break;
            case R.id.action_browser:
                openInAppChrome(url);
                break;
            case R.id.action_second:
                readMode();
                break;
            case R.id.action_third:
                saveArticle();
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
    public void readMode(){
        Intent intent = new Intent(getApplicationContext(), ArticleItemActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("isRead", true);
        startActivity(intent);
        finish();
    }
    public void saveArticle(){
        final ReadArticle readArticle = new ReadArticle();

        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_stat_download);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_download_icon);
        builder.setLargeIcon(bm);
        builder.setContentTitle("Downloading Article");
        builder.setContentText(title);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setGroup("GROUP_ARTICLE_DOWNLOAD");
        builder.setGroupSummary(true);
        builder.setOngoing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = new Date().getTime();
                String tmpStr = String.valueOf(time);
                String last5Str = tmpStr.substring(tmpStr.length() -6);
                int notificationId = Integer.valueOf(last5Str);
                readArticle.execute(url);
                manager.notify(notificationId, builder.build());
                try {
                    article = readArticle.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), ArticleItemActivity.class);
                intent.putExtra("Article", article);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setOngoing(false);
                builder.setContentIntent(pendingIntent);
                builder.setContentTitle("Download complete");
                builder.setAutoCancel(true);
                manager.notify(notificationId, builder.build());
                ArticleHelper ah = new ArticleHelper(getApplicationContext());
                ah.SaveArticle(article);
            }
        }).start();
    }

    public void openInAppChrome(String url){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        TypedValue toolBar = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, toolBar, true);
        int toolBarColor = toolBar.data;
        builder.setToolbarColor(toolBarColor);
        builder.setShowTitle(true);
        builder.enableUrlBarHiding();
        builder.addDefaultShareMenuItem();

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + this.getPackageName()));
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }


    public String formatDate(Timestamp timestamp){
        Date date = new Date(timestamp.getTime());
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

    private void decideTheme(String themeName, String primary, String accent, String status, String navigation) {
        ThemeChanger themeChanger = new ThemeChanger(this);
        themeChanger.screenColor(themeName);
        themeChanger.primaryColor(primary);
        themeChanger.accentColor(accent);
        themeChanger.statusColor(status);
        themeChanger.navigationColor(navigation);
        themeChanger.changeTheme();
    }

    private String replaceBadTags(String html){
        if(html == null){
            return "";
        }
        Pattern p = Pattern.compile("<iframe src");
        Matcher m = p.matcher(html);
        while (m.find())
            html = m.replaceAll("<a href");

        p = Pattern.compile("frameboarder=.*<i/frame>");
        m = p.matcher(html);
        while (m.find());
            html = m.replaceAll(">Watch</a>");
        html= html.replace("a href=\\", "a href=");
        return html;
    }
}
