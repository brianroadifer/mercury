package com.brianroadifer.mercuryfeed.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.DatabaseHelper;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.database.DatabaseException;

import java.net.HttpURLConnection;
import java.net.URL;

public class AddFeedActivity extends AppCompatActivity {
    DatabaseHelper dh = new DatabaseHelper();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);
        final TextView addTitle = (TextView) findViewById(R.id.add_feed_title);
        final TextView addUrl = (TextView) findViewById(R.id.add_feed_url);
        Button addButton = (Button) findViewById(R.id.add_feed_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = addTitle.getText().toString();
                String url = addUrl.getText().toString();
                if((title != null || !title.equals("") && (url != null || url.equals("")))){
                    try{
                        dh.writeFeedToDataBase(url, title);
                        Toast.makeText(getApplicationContext(), "Added " + title, Toast.LENGTH_SHORT).show();
                        pingUpdate();
                        finish();
                    }catch (DatabaseException e){
                        Toast.makeText(getApplicationContext(), "Cannot add " + title, Toast.LENGTH_SHORT).show();
                         e.printStackTrace();
                    }

                }
            }
        });

    }
    protected void pingUpdate(){try {
        URL url = new URL("http://brianroadifer.com/cron_job/index.php");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        String response = connection.getResponseCode()+"";
        Log.d("Response:",response);
    }catch (Exception e){

    }
    }
}
