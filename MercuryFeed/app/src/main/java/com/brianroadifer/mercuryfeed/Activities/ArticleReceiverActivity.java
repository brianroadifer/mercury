package com.brianroadifer.mercuryfeed.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Helpers.ThemeChanger;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ArticleReceiverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String theme = pref.getString("app_screen", "Light");
            String primary = pref.getString("app_primary", "Blue Grey");
            String accent = pref.getString("app_accent", "Blue Grey");
            String status = pref.getString("app_status", "Blue Grey");
            String navigation = pref.getString("app_navigation", "Black");
            decideTheme(theme, primary, accent, status, navigation);
            setContentView(R.layout.activity_article_receiver);

            Intent receivedIntent = getIntent();
            String recievedAction = receivedIntent.getAction();
            String recievedType = receivedIntent.getType();
            final String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            final String receievedTitle = receivedIntent.getStringExtra(Intent.EXTRA_SUBJECT);

            for (String s : receivedIntent.getExtras().keySet()) {
                Log.d("DataReceiveActivity", "intent_extra_keys: " + s);
            }
            hideWindow();

            if (recievedAction.equalsIgnoreCase(Intent.ACTION_SEND)) {
                if (recievedType.startsWith("text/")) {
                    if (receievedTitle != null && receivedText != null) {

                        createDialog(receievedTitle, receivedText);
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry was unable to launch", Toast.LENGTH_LONG).show();
                        finish();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), recievedType + " is not supported", Toast.LENGTH_LONG).show();
                    finish();
                }

            } else if (recievedAction.equalsIgnoreCase(Intent.ACTION_MAIN)) {

            }
        }else{
            Toast.makeText(this, "Please Login to Save Articles", Toast.LENGTH_SHORT).show();
        }

    }
    private void createDialog(final String receievedTitle, final String receivedText){
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.add_article_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        TextView title = (TextView) dialogView.findViewById(R.id.share_title);
        TextView url = (TextView) dialogView.findViewById(R.id.share_url);
        final MultiAutoCompleteTextView tags = (MultiAutoCompleteTextView) dialogView.findViewById(R.id.tag_maker);
        title.setText(receievedTitle);
        url.setText(receivedText);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.tag_suggestion, getAllTags());
        tags.setAdapter(adapter);
        tags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        tags.setThreshold(1);


        dialog.setView(dialogView);
        dialogView.findViewById(R.id.article_save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadArticle readArticle = new ReadArticle();
                Article article = new Article();
                try {
                    article = readArticle.execute(receivedText).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                article.Title = receievedTitle;
                ArticleHelper ah = new ArticleHelper(getApplicationContext());
                String tag = tags.getText().toString();
                String[] tags = tag.replaceAll("^[,\\s]+", "").split("[,\\s]+");
                List<Tag> tagList = new ArrayList<>();
                for (String t : tags) {
                    Tag temp = new Tag();
                    temp.Name = t;
                    temp.ID = UUID.randomUUID().toString();
                    tagList.add(temp);
                }
                article.Tags = tagList;
                ah.SaveArticle(article);
                Toast.makeText(getApplicationContext(), receievedTitle + " was saved", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
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

    private void decideTheme(String themeName, String primary, String accent, String status, String navigation) {
        ThemeChanger themeChanger = new ThemeChanger(this);
        themeChanger.screenColor(themeName);
        themeChanger.primaryColor(primary);
        themeChanger.accentColor(accent);
        themeChanger.statusColor(status);
        themeChanger.navigationColor(navigation);
        themeChanger.changeTheme();
    }

    private void hideWindow(){
        RelativeLayout coordinatorLayout = (RelativeLayout) findViewById(R.id.article_receiver);
        coordinatorLayout.setVisibility(View.GONE);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0f;
        lp.dimAmount = 0f;
    }
}
