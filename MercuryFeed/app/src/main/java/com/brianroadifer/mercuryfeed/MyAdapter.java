package com.brianroadifer.mercuryfeed;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Brian Roadifer on 5/28/2016.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    Feed feed;
    Context context;

    public MyAdapter(Feed feed, Context context) {
        this.feed = feed;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_news_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Item current = feed.Items.get(position);
        holder.Title.setText(current.getTitle());
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z", Locale.US);
        Date newDate;
        try {
            newDate = format.parse(current.getPubDate());
            format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
            holder.Info.setText(feed.Title + " / by " +current.getAuthor()+ " / " + Difference(newDate));
        } catch (ParseException e) {
            holder.Info.setText(feed.Title + " / "+ current.getPubDate());
        }
        holder.Content.setText(current.getDescription());
        Picasso.with(context).load(current.getThumbnailUrl()).into(holder.Thumbnail);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ItemActivity.class);
                intent.putExtra("Link", current.getLink());
                intent.putExtra("Author", current.getAuthor());
                intent.putExtra("Date", current.getPubDate());
                intent.putExtra("Image", current.getThumbnailUrl());
                intent.putExtra("Description", current.getDescription());
                intent.putExtra("Title", current.getTitle());
               context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        try{
            return feed.Items.size();
        }catch (NullPointerException e){
            return 0;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView Title, Content, Info;
        ImageView Thumbnail;
        GridLayout cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            Title = (TextView)itemView.findViewById(R.id.news_title);
            Content = (TextView) itemView.findViewById(R.id.news_content);
            Info = (TextView)itemView.findViewById(R.id.news_info);
            Thumbnail = (ImageView)itemView.findViewById(R.id.news_image);
            cardView = (GridLayout) itemView.findViewById(R.id.card_view);
        }
    }
    public String Difference(Date pastDate){
        Date current = Calendar.getInstance().getTime();
        long difference = current.getTime() - pastDate.getTime();
        int days = (int) (difference/(1000*60*60*24));
        if(days > 1){
            return days + " days ago";
        }else if(days == 1) {
            return days + " day ago";
        }
        int hours = (int) (difference/(1000*60*60));
        if(hours > 1){
            return hours + " hours ago";
        }else if(hours == 1){
            return hours + " hour ago";
        }
        int min = (int) (difference/(1000*60));
        if(min > 1){
            return min + " minutes ago";
        }else if(min == 1){
            return min + " minute ago";
        }else{
            return "Few seconds ago";
        }

    }
}
