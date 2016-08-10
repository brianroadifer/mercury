package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Models.Article;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brian Roadifer on 7/3/2016.
 */
public class ArticleHelper {

    public final static String FILENAME = "meurcury_article_";
    FileOutputStream fos;
    ObjectOutputStream os;
    FileInputStream fis;
    ObjectInputStream is;
    Context context;
//    SharedPreferences preferences;
    String storage = "Internal";

    public ArticleHelper(Context context) {
        this.context = context;
//        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        storage = preferences.getString("offline_storage", "Internal");
    }

    /**
     * Save a single article to read offline later
     *
     * @param article Article that is saved to the device
     */
    public void SaveArticle(Article article) {
        if(storage.equalsIgnoreCase("Internal")) {
            try {
                this.fos = this.context.openFileOutput(FILENAME + article.ID, Context.MODE_PRIVATE);
                this.os = new ObjectOutputStream(this.fos);
                this.os.writeObject(article);
                this.os.close();
                this.fos.close();
                Log.w("Article:Save", article.Title + " was saved successfully");
            } catch (IOException e) {
                Toast.makeText(context, "Could not save article, check storage space", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else if(storage.equalsIgnoreCase("External")){
            if(isExternalStroageWritable()) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/mercury_feed/data/articles");
                if (dir.mkdirs()) {
                    File file = new File(dir, FILENAME + article.ID);
                    try {
                        this.fos = this.context.openFileOutput(file.getAbsolutePath(), Context.MODE_PRIVATE);
                        this.os = new ObjectOutputStream(this.fos);
                        this.os.writeObject(article);
                        this.os.close();
                        this.fos.close();
                        Log.w("Article:Save", article.Title + " was saved successfully");
                    } catch (IOException e) {
                        Toast.makeText(context, "Could not save article, check storage space", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }else{
                storage = "Internal";
                SaveArticle(article);
            }

        }

    }

    public void SaveArticles(List<Article> articles) {
        for (Article article : articles) {
            SaveArticle(article);
        }
    }

    public Article LoadArticle(String fileName) {
        Log.w("Article:Load", "Loading " + fileName);
        try {
            this.fis = this.context.openFileInput(fileName);
            this.is = new ObjectInputStream(this.fis);
            Article article = (Article) this.is.readObject();
            this.is.close();
            this.fis.close();
            Log.w("Article:Load", article.Title + "was successfully loaded");
            return article;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Article> LoadArticles() {
        List<Article> articles = new ArrayList<>();
        File[] files = this.context.getFilesDir().listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().contains(FILENAME)) {
                Article article = LoadArticle(file.getName());
                if (article != null) {
                    articles.add(article);
                }

            }
        }
        return articles;
    }

    /**
     * Delete a specific article
     *
     * @param filename file that will be deleted
     * @return
     */
    public boolean DeleteArticle(String filename) {
        Log.w("Article:Delete", filename + " was successfully deleted");
        return context.deleteFile(FILENAME + filename);

    }

    /**
     * Deletes all articles stored on the device
     */
    public void DeleteArticles() {
        File[] files = context.getFilesDir().listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Log.w("Article:Delete", file.getName() + " was successfully deleted");
                context.deleteFile(file.getName());
            }
        }

    }

    private boolean isExternalStroageWritable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state);
    }
    private boolean isExternalStroageReadable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equalsIgnoreCase(state);
    }
}
