package com.brianroadifer.mercuryfeed.Models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Brian Roadifer on 6/24/2016.
 */
public class Feed implements Serializable, Comparable<Feed>{
    public String Title;
    public String ID;
    public Feed(){}
    public boolean isSearch;

    public Feed(String feedUrl, String title) {
        Title = title;
        FeedUrl = feedUrl;
    }
    public Feed(String feedUrl, String title, String id) {
        Title = title;
        FeedUrl = feedUrl;
        ID = id;
    }

    public String FeedUrl;

    public List<Item> Items = new ArrayList<>();

    public List<Item> ItemsAsending(){

        List<Item> items = Items;
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return i1.compareTo(i2);
            }
        });
        return items;
    }

    public List<Item> ItemsDesending(){
        List<Item> items = Items;
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return i2.compareTo(i1);
            }
        });
        return items;
    }

    @Override
    public int compareTo(@NonNull Feed feed) {
        return Title.compareTo(feed.Title);
    }
}
