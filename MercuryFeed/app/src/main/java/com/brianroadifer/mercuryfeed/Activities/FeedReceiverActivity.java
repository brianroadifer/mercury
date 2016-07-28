package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Brian Roadifer on 7/27/2016.
 */
public class FeedReceiverActivity extends AppCompatActivity{
    TextView title, url;
    Button save;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_receiver);
        title = (TextView) findViewById(R.id.share_title);
        url = (TextView) findViewById(R.id.share_url);
        save = (Button) findViewById(R.id.article_save_button);

        Intent receivedIntent = getIntent();
        String recievedAction = receivedIntent.getAction();
        String recievedType = receivedIntent.getType();
        final String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        final String receievedTitle = receivedIntent.getStringExtra(Intent.EXTRA_SUBJECT);

        for(String s:receivedIntent.getExtras().keySet()){
            Log.d("FeedReceiverActivity", "intent_extra_keys: "+ s);
        }


        if (recievedAction.equalsIgnoreCase(Intent.ACTION_SEND)){
            if(recievedType.startsWith("text/")){
                if(receievedTitle != null && receivedText != null){
                    title.setText(receievedTitle);
                    url.setText(receivedText);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(getApplicationContext(),receievedTitle + " was saved", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }else {
                    Toast.makeText(getApplicationContext(),"Sorry was unable to launch", Toast.LENGTH_LONG).show();
                    finish();
                }

            }else{
                Toast.makeText(getApplicationContext(), recievedType + " is not supported", Toast.LENGTH_LONG).show();
                finish();
            }

        }else if(recievedAction.equalsIgnoreCase(Intent.ACTION_MAIN)){

        }

    }
}
