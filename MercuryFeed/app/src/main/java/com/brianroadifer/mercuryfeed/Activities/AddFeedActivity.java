package com.brianroadifer.mercuryfeed.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.DatabaseHelper;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.database.DatabaseException;

import org.jsoup.Jsoup;

public class AddFeedActivity extends AppCompatActivity {
    private final static String TAG = "AddFeedActivity";
    private final DatabaseHelper dh = new DatabaseHelper();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString("app_screen", "Light");
        String primary = pref.getString("app_primary", "Blue Grey");
        String accent = pref.getString("app_accent", "Blue Grey");
        String status = pref.getString("app_status", "Blue Grey");
        String navigation = pref.getString("app_navigation", "Black");
        decideTheme(theme, primary, accent, status, navigation);
        setContentView(R.layout.activity_add_feed);

        final EditText addSearch = findViewById(R.id.add_feed_search);
        final EditText addTitle = findViewById(R.id.add_feed_title);
        final EditText addUrl = findViewById(R.id.add_feed_url);
        Button addButton = findViewById(R.id.add_feed_button);

        pingUpdate();
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feed = addSearch.getText().toString();
                String title = addTitle.getText().toString();
                String url = addUrl.getText().toString();
                if((!feed.isEmpty() || feed.isEmpty())&& (title.isEmpty() || url.isEmpty())){
                    Intent query = new Intent(getApplicationContext(), SearchResultsActivity.class);
                    query.setAction(Intent.ACTION_SEARCH);
                    query.putExtra(SearchManager.QUERY, feed);
                    startActivity(query);
                }else if(!title.isEmpty() && !url.isEmpty()){
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
    private void pingUpdate(){
        try{
            AsyncTask<Void,Void,Void> ping;
            ping = new AsyncTask<Void,Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    Log.d(TAG, "pingUpdate:Connecting to [http://brianroadifer.com/php_cron/index.php]");
                    Jsoup.connect("http://brianroadifer.com/php_cron/index.php").request();
                    Log.d(TAG, "pingUpdate:Connected to [http://brianroadifer.com/php_cron/index.php]");
                    return null;
                }
            };
            ping.execute();
        }catch (Exception e){
            Log.d(TAG, "pingUpdate:Unable to connect to [http://brianroadifer.com/php_cron/index.php]", e);
//            e.printStackTrace();
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
