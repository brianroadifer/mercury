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

public class ArticleHelper {

    public final static String FILENAME = "mercury_article_";
    private FileOutputStream fos;
    private ObjectOutputStream os;
    private FileInputStream fis;
    private ObjectInputStream is;
    private final Context context;
    private final SharedPreferences preferences;
    private String storage = "Internal";

    public ArticleHelper(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.storage = this.preferences.getString("offline_storage", "Internal");
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
            if(isExternalStorageWritable() && isExternalStorageReadable()) {
                    File file = new File(this.context.getExternalFilesDir(null)+File.separator+ FILENAME + article.ID);
                    String path = file.getAbsolutePath();
                    try {
                        this.fos = new FileOutputStream(file);
                        this.os = new ObjectOutputStream(this.fos);
                        this.os.writeObject(article);
                        this.os.close();
                        this.fos.close();
                        Log.w("Article:Save", article.Title + " was saved successfully");
                        Log.w("Article:Save", path);

                    } catch (IOException e) {
                        Toast.makeText(context, "Could not save article, check storage space", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
            }else{
                storage = "Internal";
                SaveArticle(article);
            }

        }else{
            storage = "Internal";
            SaveArticle(article);
        }

    }

    public void SaveArticles(List<Article> articles) {
        for (Article article : articles) {
            SaveArticle(article);
        }
    }

    private Article LoadArticleInternal(File file) {
        Log.w("Article:Load", "Loading " + file.getName());
        try {
            this.fis = this.context.openFileInput(file.getName());
            this.is = new ObjectInputStream(this.fis);
            Article article = (Article) this.is.readObject();
            this.is.close();
            this.fis.close();
            Log.w("Article:Load", article.Title + "was successfully loaded");
            return article;
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    private Article LoadArticleExternal(File file){
        Log.w("Article:Load", "Loading " + file.getName());
        try {
            this.fis = new FileInputStream(file);
            this.is = new ObjectInputStream(this.fis);
            Article article = (Article) this.is.readObject();
            this.is.close();
            this.fis.close();
            Log.w("Article:Load", article.Title + "was successfully loaded");
            return article;
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Article> LoadArticles() {
        List<Article> articles = new ArrayList<>();
        File[] internal = this.context.getFilesDir().listFiles();
        File[] external = null;
        if(isExternalStorageReadable() && isExternalStorageWritable()){
            external = this.context.getExternalFilesDir(null).listFiles();
        }
        for (File file : internal) {
            if (file.isFile() && file.getName().contains(FILENAME)) {
                Article article = LoadArticleInternal(file);
                if (article != null) {
                    articles.add(article);
                }

            }
        }
        if(external != null){
            for (File file: external)
            {
                if (file.isFile() && file.getName().contains(FILENAME)) {
                    Article article = LoadArticleExternal(file);
                    if (article != null) {
                        articles.add(article);
                    }

                }
            }
        }

        return articles;
    }

    /**
     * Delete a specific article
     *
     * @param file file that will be deleted
     * @return boolean on state of file
     */
    public boolean DeleteArticle(File file) {
        Log.w("Article:Delete", file.getName() + " was successfully deleted");
        return file.delete();

    }

    /**
     * Deletes all articles stored on the device
     */
    public void DeleteArticles() {
        File[] files = context.getFilesDir().listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().contains(FILENAME)) {
                DeleteArticle(file);
            }
        }
        if(isExternalStorageWritable() && isExternalStorageReadable()){
            File[] external = context.getExternalFilesDir(null).listFiles();
            for (File file : external) {
                if (file.isFile() && file.getName().contains(FILENAME)) {
                    DeleteArticle(file);
                }
            }
        }


    }
    public static long getOfflineArticleSize(Context context){
        long offlineSize = 0;
        File[] internal = context.getFilesDir().listFiles();
        File[] external = null;
        if(isExternalStorageReadable() && isExternalStorageWritable()){
            external = context.getExternalFilesDir(null).listFiles();
        }
        for (File file : internal) {
            if (file.isFile() && file.getName().contains(FILENAME)) {
                offlineSize += 1;
            }
        }
        if(external != null){
            for (File file: external)
            {
                if (file.isFile() && file.getName().contains(FILENAME)) {
                    offlineSize += 1;

                }
            }
        }
        return offlineSize;
    }

    public static boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state);
    }
    public static boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equalsIgnoreCase(state);
    }

}
