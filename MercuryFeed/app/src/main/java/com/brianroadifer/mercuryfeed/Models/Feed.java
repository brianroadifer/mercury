package com.brianroadifer.mercuryfeed.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Brian Roadifer on 6/24/2016.
 */
@IgnoreExtraProperties
public class Feed implements Serializable{
    public String Title;

    public Feed(){}

    public Feed(String feedUrl, String title) {
        Title = title;
        FeedUrl = feedUrl;
    }

    public String FeedUrl;

    public ArrayList<Item> Items;

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("feedUrl", FeedUrl);
        result.put("title", Title);
        return result;
    }
}
