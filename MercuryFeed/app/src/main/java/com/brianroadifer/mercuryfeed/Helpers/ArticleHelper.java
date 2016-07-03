package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.util.Log;

import com.brianroadifer.mercuryfeed.Models.Article;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Brian Roadifer on 7/3/2016.
 */
public class ArticleHelper {
    
    FileOutputStream fos;
    ObjectOutputStream os;
    FileInputStream fis;
    ObjectInputStream is;
    Context context;

    public ArticleHelper(Context context){
        this.context = context;
    }

    /**
     * Save a single article to read offline later
     * @param article Article that is saved to the device
     */
    public void SaveArticle(Article article){
        UUID uuid = UUID.randomUUID();
        try {
            this.fos = this.context.openFileOutput(uuid.toString(), Context.MODE_PRIVATE);
            this.os = new ObjectOutputStream(this.fos);
            this.os.writeObject(article);
            this.os.close();
            this.fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.w("Article:Save", article.Title + " was saved successfully as " + uuid.toString() );
    }

    public void SaveArticles(List<Article> articles){
        for(Article article: articles){
            SaveArticle(article);
        }
    }

    public Article LoadArticle(String fileName){
        Log.w("Article:Load", "Loading " + fileName);
        try{
            this.fis = this.context.openFileInput(fileName);
            this.is = new ObjectInputStream(this.fis);
            Article article = (Article) this.is.readObject();
            this.is.close();
            this.fis.close();
            Log.w("Article:Load", article.Title + "was successfully loaded");
            return article;
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    public List<Article> LoadArticles(){
        List<Article> articles = new ArrayList<>();
        File[] files = this.context.getFilesDir().listFiles();
        for (File file: files) {
            if (file.isFile()){
                Article article = LoadArticle(file.getName());
                if(article != null){
                    articles.add(article);
                }

            }
        }
        return  articles;
    }

    /**
     * Delete a specific article
     * @param filename file that will be deleted
     * @return
     */
    public boolean DeleteArticle(String filename){
        Log.w("Article:Delete", filename + " was successfully deleted" );
        return context.deleteFile(filename);

    }

    /**
     * Deletes all articles stored on the device
     *
     */
    public void DeleteArticles(){
        File[] files = context.getFilesDir().listFiles();
        for (File file: files) {
            if(file.isFile()){
                Log.w("Article:Delete", file.getName() + " was successfully deleted" );
                context.deleteFile(file.getName());
            }
        }

    }
}
