package com.brianroadifer.mercuryfeed.Models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Article implements Serializable, Comparable<Article> {

    public String ID;
    public String Title;
    public String Content;
    public String URL;
    public String ByLine;
    private final long Created;
    public List<Tag> Tags = new ArrayList<>();

    public Article(){
        this.Created = new Date().getTime();
    }

    @Override
    public int compareTo(@NonNull Article article) {
        return Long.compare(Created, article.Created);
    }
}
