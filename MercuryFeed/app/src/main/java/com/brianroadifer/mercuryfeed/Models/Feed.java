package com.brianroadifer.mercuryfeed.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Brian Roadifer on 6/24/2016.
 */
@IgnoreExtraProperties
public class Feed implements Serializable{
    public String Title;
    public String ID;
    public Feed(){}

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

}
