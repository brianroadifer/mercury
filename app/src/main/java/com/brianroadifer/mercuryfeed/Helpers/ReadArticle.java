package com.brianroadifer.mercuryfeed.Helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.brianroadifer.mercuryfeed.Models.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by Brian Roadifer on 6/27/2016.
 */
public class ReadArticle extends AsyncTask<String, Integer, Article> {
    Article article = new Article();
    String url;

    public ReadArticle(){
    }

    @Override
    protected Article doInBackground(String... params) {
        url = params[0];
        ProcessHTML(GetData(params[0]));
        return article;
    }

    @Override
    protected void onProgressUpdate(Integer...progress){

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Article aVoid) {
        super.onPostExecute(aVoid);
    }

    public void ProcessHTML(Document data) {
        if(data != null){
            try {
                Readability readability = new Readability(data){
                    @Override
                    protected void debug(String message){
                        Log.d("READ:B", message);
                    }
                    @Override
                    protected void debug(String message, Throwable throwable){
                        Log.d("READ:B", message , throwable);
                    }
                };
                article = readability.parse();
                article.ID = UUID.randomUUID().toString();
                article.URL = url;
                article.Tags = new ArrayList<>();
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Document GetData(String rssUrl){
        try {
            StringBuffer buffer = new StringBuffer();
            Document document;
            try{
                Log.d("JSwa", "Connecting to ["+ rssUrl+ "]");
                document = Jsoup.connect(rssUrl).get();
                Log.d("JSwa", "Connected to ["+ rssUrl+ "]");
                return document;
            }catch (Exception e){
                Log.d("JSwa", "Unable to connect to ["+ rssUrl+ "]");
                e.printStackTrace();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
