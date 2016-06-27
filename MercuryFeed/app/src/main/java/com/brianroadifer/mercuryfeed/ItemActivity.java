package com.brianroadifer.mercuryfeed;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemActivity extends AppCompatActivity {

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

        String imageUrl = bundle.getString("Image");
        String title = bundle.getString("Title");
        String author = bundle.getString("Author");
        String description = bundle.getString("Description");
        String date = formatDate(bundle.getString("Date"));
        final String url = bundle.getString("Link");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(Intent.URI_ANDROID_APP_SCHEME + "//" + activity.getPackageName()));
                customTabsIntent.launchUrl(activity, Uri.parse(url));
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.item_image);
        Picasso.with(this).load(imageUrl).into(imageView);
        TextView titleView = (TextView) findViewById(R.id.item_title);
        titleView.setText(title);
        TextView descriptionView = (TextView) findViewById(R.id.item_description);
        descriptionView.setText(description);
        TextView infoView = (TextView) findViewById(R.id.item_info);
        infoView.setText("by " + author + " / " + date);

        WebView feedWeb = (WebView) findViewById(R.id.item_web);
        feedWeb.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                return false;
            }
        });
        feedWeb.getSettings().setJavaScriptEnabled(true);
        feedWeb.loadData(htmlSetup(description), "text/html", "utf-8");

        LeakCanary.install(application);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.item_settings:
                Toast.makeText(this, "Settings Not Enabled", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    public String formatDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z", Locale.US);
        Date newDate;
        try {
            newDate = format.parse(date);
            format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
            return  format.format(newDate);
        } catch (ParseException e) {
            return date;
        }
    }

    private String htmlSetup(String content){
        return "<html><body>"+ content + "</body></html>";
    }

}
