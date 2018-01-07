package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.R;

public class FeedReceiverActivity extends AppCompatActivity{
    private TextView title;
    private TextView url;
    private Button save;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString("app_screen", "Light");
        String primary = pref.getString("app_primary", "Blue Grey");
        String accent = pref.getString("app_accent", "Blue Grey");
        String status = pref.getString("app_status", "Blue Grey");
        String navigation = pref.getString("app_navigation", "Black");
        decideTheme(theme, primary, accent, status, navigation);
        setContentView(R.layout.activity_feed_receiver);
        title = findViewById(R.id.share_title);
        url = findViewById(R.id.share_url);
        save = findViewById(R.id.article_save_button);

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();
        final String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        final String receivedTitle = receivedIntent.getStringExtra(Intent.EXTRA_SUBJECT);

        for(String s:receivedIntent.getExtras().keySet()){
            Log.d("FeedReceiverActivity", "intent_extra_keys: "+ s);
        }


        if (receivedAction != null && receivedAction.equalsIgnoreCase(Intent.ACTION_SEND)){
            if(receivedType != null && receivedType.startsWith("text/")){
                if(receivedTitle != null && receivedText != null){
                    title.setText(receivedTitle);
                    url.setText(receivedText);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(getApplicationContext(),receivedTitle + " was saved", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }else {
                    Toast.makeText(getApplicationContext(),"Sorry was unable to launch", Toast.LENGTH_LONG).show();
                    finish();
                }

            }else{
                Toast.makeText(getApplicationContext(), receivedType + " is not supported", Toast.LENGTH_LONG).show();
                finish();
            }

        }

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
}
