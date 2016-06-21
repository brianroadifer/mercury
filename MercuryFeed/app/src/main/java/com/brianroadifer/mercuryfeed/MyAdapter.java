package com.brianroadifer.mercuryfeed;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
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
import java.util.Date;
import java.util.Locale;

/**
 * Created by Brian Roadifer on 5/28/2016.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    ArrayList<Feed> feeds;
    Context context;

    public MyAdapter(ArrayList<Feed> feeds, Context context) {
        this.feeds = feeds;
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
        final Feed current = feeds.get(position);
        holder.Title.setText(current.getTitle());
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z", Locale.US);
        Date newDate = null;
        try {
            newDate = format.parse(current.getPubDate());
            format = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
            String date = format.format(newDate);
            holder.Info.setText(current.getHeadTitle() + " / " + date);
        } catch (ParseException e) {
            holder.Info.setText(current.getHeadTitle() + " / "+ current.getPubDate());
        }
        holder.Content.setText(current.getDescription());
        Picasso.with(context).load(current.getThumbnailUrl()).into(holder.Thumbnail);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ArticleWeb.class);
                intent.putExtra("Link", current.getLink());
               context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feeds.size();
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
}
