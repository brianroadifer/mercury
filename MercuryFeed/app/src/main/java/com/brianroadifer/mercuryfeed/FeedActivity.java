package com.brianroadifer.mercuryfeed;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;

        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


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
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(activity, Uri.parse(url));
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.feed_image);
        Picasso.with(this).load(imageUrl).into(imageView);
        TextView titleView = (TextView) findViewById(R.id.feed_title);
        titleView.setText(title);
        TextView descriptionView = (TextView) findViewById(R.id.feed_description);
        descriptionView.setText(description);
        TextView infoView = (TextView) findViewById(R.id.feed_info);
        infoView.setText("by " + author + " / " + date);
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

}
