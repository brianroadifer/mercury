package com.brianroadifer.mercuryfeed.Models;

import android.support.annotation.NonNull;

import java.sql.Timestamp;


public class Item implements Comparable<Item> {
    public String title;
    public String description;
    public Timestamp timestamp;
    public String thumbnailUrl;
    public String author;
    public String link;
    public String ID;

    @Override
    public int compareTo(@NonNull Item item) {
        Timestamp compareTimestamp = item.timestamp;
        return timestamp.compareTo(compareTimestamp);
    }
}
