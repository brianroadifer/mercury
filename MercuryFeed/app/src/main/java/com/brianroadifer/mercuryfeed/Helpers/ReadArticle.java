package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;

import com.brianroadifer.mercuryfeed.Models.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 * Created by Brian Roadifer on 6/27/2016.
 */
public class ReadArticle extends AsyncTask<String, Void, Article> {
    String htmlUrl = "http://www.theverge.com/2016/6/27/12040196/president-obama-pardon-edward-snowden-free";
    Article article = new Article();

    public ReadArticle(){
    }

    @Override
    protected Article doInBackground(String... params) {
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
            Element body = data.body();
            Log.d("JSwa", "Title [" + data.title()+"]");
            article.Title = data.title();
            Elements articles = body.getElementsByTag("article");
            for (Element article: articles) {
                Log.d("JSwa", "Article [" + article.toString()+"]");
            }
            Element articlez = articles.get(0);
            article.Content = articlez.toString();
            Elements h1s = articlez.getElementsByTag("h1");
            for (Element h1: h1s) {
                Log.d("JSwa", "H1 [" + h1.html()+"]");
            }
            Elements lis = articlez.getElementsByTag("li");
            for (Element li: lis) {
                Log.d("JSwa", "LI [" + li.html()+"]");
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
