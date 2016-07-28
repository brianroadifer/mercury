package com.brianroadifer.mercuryfeed.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Helpers.ArticleHelper;
import com.brianroadifer.mercuryfeed.Helpers.ReadArticle;
import com.brianroadifer.mercuryfeed.Helpers.TagHelper;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Tag;
import com.brianroadifer.mercuryfeed.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ArticleReceiverActivity extends AppCompatActivity {
    TextView title,url;
    MultiAutoCompleteTextView tags;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_receiver);
        title = (TextView) findViewById(R.id.share_title);
        url = (TextView) findViewById(R.id.share_url);
        tags = (MultiAutoCompleteTextView) findViewById(R.id.tag_maker);
        save = (Button) findViewById(R.id.article_save_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.tag_suggestion, getAllTags());
        tags.setAdapter(adapter);
        tags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        tags.setThreshold(1);

        Intent receivedIntent = getIntent();
        String recievedAction = receivedIntent.getAction();
        String recievedType = receivedIntent.getType();
        final String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
        final String receievedTitle = receivedIntent.getStringExtra(Intent.EXTRA_SUBJECT);

        for(String s:receivedIntent.getExtras().keySet()){
            Log.d("DataReceiveActivity", "intent_extra_keys: "+ s);
        }


        if (recievedAction.equalsIgnoreCase(Intent.ACTION_SEND)){
            if(recievedType.startsWith("text/")){
                if(receievedTitle != null && receivedText != null){
                    title.setText(receievedTitle);
                    url.setText(receivedText);

                    save.setOnClickListener(new View.OnClickListener() {
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
                            for(String t: tags){
                                Tag temp = new Tag();
                                temp.Name = t;
                                temp.ID = UUID.randomUUID().toString();
                                tagList.add(temp);
                            }
                            article.Tags = tagList;
                            ah.SaveArticle(article);
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
}
