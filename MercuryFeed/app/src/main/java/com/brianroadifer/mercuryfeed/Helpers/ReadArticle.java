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
public class ReadArticle extends AsyncTask<String, Void, Article> {
    String htmlUrl = "http://www.theverge.com/2016/6/27/12040196/president-obama-pardon-edward-snowden-free";
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
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Article aVoid) {
        super.onPostExecute(aVoid);
    }

    public void ProcessHTML(Document data) {
        if(data != null){
//            Element body = data.body();
//            Log.d("JSwa", "Title [" + data.title()+"]");
//            article.ID = UUID.randomUUID().toString();
//            article.Title = data.title();
//            Elements articles = body.getElementsByTag("article");
//            for (Element article: articles) {
//                Log.d("JSwa", "Article [" + article.toString()+"]");
//            }
//            Element articlez = articles.get(0);
//            article.Content = articlez.html();
//            Elements h1s = articlez.getElementsByTag("h1");
//            for (Element h1: h1s) {
//                Log.d("JSwa", "H1 [" + h1.html()+"]");
//            }
//            Elements lis = articlez.getElementsByTag("li");
//            for (Element li: lis) {
//                Log.d("JSwa", "LI [" + li.html()+"]");
//            }
//            article.Tags = new ArrayList<>();
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
