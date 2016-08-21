package com.brianroadifer.mercuryfeed.Models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Brian Roadifer on 6/30/2016.
 */
public class Article implements Serializable, Comparable<Article> {

    public String ID;
    public String Title;
    public String Content;
    public String URL;
    public String ByLine;
    public String Dir;
    public String Excerpt;
    public final long Created;
    public List<Tag> Tags = new ArrayList<>();

    public Article(){
        this.Created = new Date().getTime();
    }

    @Override
    public int compareTo(@NonNull Article article) {
        return Long.compare(Created, article.Created);
    }
}
