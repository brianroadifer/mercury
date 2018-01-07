package com.brianroadifer.mercuryfeed.Models;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public final List<Item> Items = new ArrayList<>();

    public List<Item> ItemsAscending(){

        List<Item> items = Items;
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return i1.compareTo(i2);
            }
        });
        return items;
    }

    public List<Item> ItemsDescending(){
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
