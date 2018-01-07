package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Activities.ItemActivity;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.Models.Item;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FeedItemAdapter extends RecyclerView.Adapter<FeedItemAdapter.ViewHolder> {
    private final DatabaseReference feedItemDB = FirebaseDatabase.getInstance().getReference();
    private final Feed feed;
    private final Context context;
    private final boolean sort;

    public FeedItemAdapter(Feed feed,boolean sort, Context context) {
        this.feed = feed;
        this.context = context;
        this.sort = sort;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_news_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Item current;
        if(sort){
            current = feed.ItemsDescending().get(position);

        }else{
            current = feed.ItemsAscending().get(position);
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String themeName = pref.getString("app_screen", "Light");
        holder.Title.setText(current.title);
        try {
            String byline = "by " + current.author + " : " + Difference(current.timestamp);
            holder.Info.setText(byline);

        }catch (NullPointerException ne){
            holder.Info.setText("");
        }
        if(current.description != null){
            holder.Content.setText(Html.fromHtml(current.description));
        }else {
            holder.Content.setVisibility(View.GONE);
        }

        if(current.thumbnailUrl.isEmpty()){
            holder.Thumbnail.setVisibility(View.GONE);
        }else{
            Picasso.with(context).load(current.thumbnailUrl).placeholder(R.drawable.placeholder).error(R.drawable.error).into(holder.Thumbnail);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ItemActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Link", current.link);
                intent.putExtra("Author", current.author);
                intent.putExtra("Date", current.timestamp);
                intent.putExtra("Image", current.thumbnailUrl);
                intent.putExtra("Description", current.description);
                intent.putExtra("Title", current.title);

                Map<String,Object> read = new HashMap<>();
                if(!feed.isSearch) {
                    read.put("/feed_items/" + current.ID + "/user-read/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), true);
                    feedItemDB.updateChildren(read);
                    v.setActivated(true);
                    v.setAlpha(0.5f);
                }
                context.startActivity(intent);
            }

        });
        if(!feed.isSearch) {
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Map<String, Object> read = new HashMap<>();
                    read.put("/feed_items/" + current.ID + "/user-read/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), !v.isActivated());
                    Snackbar snackbar;
                    if (v.isActivated()) {
                        snackbar = Snackbar.make(v, "Marked as unread", Snackbar.LENGTH_LONG);
                        View sv = snackbar.getView();
                        TextView stv = sv.findViewById(android.support.design.R.id.snackbar_text);
                        stv.setTextColor(context.getResources().getColor(R.color.article_background_white));
                        snackbar.show();
                        v.setAlpha(1f);
                        v.setActivated(false);
                    } else {
                        snackbar = Snackbar.make(v, "Marked as read", Snackbar.LENGTH_LONG);
                        View sv = snackbar.getView();
                        TextView stv = sv.findViewById(android.support.design.R.id.snackbar_text);
                        stv.setTextColor(context.getResources().getColor(R.color.article_background_white));
                        snackbar.show();
                        v.setAlpha(0.5f);
                        v.setActivated(true);
                    }
                    feedItemDB.updateChildren(read);

                    return true;
                }
            });
        }
        decideTheme(holder, themeName);
    }

    @Override
    public int getItemCount() {
        try{
            return feed.Items.size();
        }catch (NullPointerException e){
            return 0;
        }

    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener  {
        final TextView Title;
        final TextView Content;
        final TextView Info;
        final ImageView Thumbnail;
        final CardView cardView;


        public ViewHolder(View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.news_title);
            Content = itemView.findViewById(R.id.news_content);
            Info = itemView.findViewById(R.id.news_info);
            Thumbnail = itemView.findViewById(R.id.news_image);
            cardView = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
            if(!feed.isSearch)
                itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Item item;
            if(sort){
                item = feed.ItemsDescending().get(getAdapterPosition());

            }else{
                item = feed.ItemsAscending().get(getAdapterPosition());
            }
            Intent intent = new Intent(context, ItemActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("Link", item.link);
            intent.putExtra("Author", item.author);
            intent.putExtra("Date", item.timestamp);
            intent.putExtra("Image", item.thumbnailUrl);
            intent.putExtra("Description", item.description);
            intent.putExtra("Title", item.title);

            Map<String,Object> read = new HashMap<>();
            if(!feed.isSearch) {
                read.put("/feed_items/" + item.ID + "/user-read/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), true);
                feedItemDB.updateChildren(read);
                v.setActivated(true);
                v.setAlpha(0.5f);
            }
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            Item item;
            if(sort){
                item = feed.ItemsDescending().get(getAdapterPosition());
            }else{
                item = feed.ItemsAscending().get(getAdapterPosition());
            }
            Map<String, Object> read = new HashMap<>();
            read.put("/feed_items/" + item.ID + "/user-read/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), !v.isActivated());
            Snackbar snackbar;
            if (v.isActivated()) {
                snackbar = Snackbar.make(v, "Marked as unread", Snackbar.LENGTH_LONG);
                View sv = snackbar.getView();
                TextView stv = sv.findViewById(android.support.design.R.id.snackbar_text);
                stv.setTextColor(context.getResources().getColor(R.color.article_background_white));
                snackbar.show();
                v.setAlpha(1f);
                v.setActivated(false);
            } else {
                snackbar = Snackbar.make(v, "Marked as read", Snackbar.LENGTH_LONG);
                View sv = snackbar.getView();
                TextView stv = sv.findViewById(android.support.design.R.id.snackbar_text);
                stv.setTextColor(context.getResources().getColor(R.color.article_background_white));
                snackbar.show();
                v.setAlpha(0.5f);
                v.setActivated(true);
            }
            feedItemDB.updateChildren(read);

            return true;
        }
    }
    private String Difference(Timestamp timestamp){
        Date current = Calendar.getInstance().getTime();
        long difference = current.getTime() - timestamp.getTime();
        int days = (int) (difference/(1000*60*60*24));
        if(days > 1){
            return days + " days ago";
        }else if(days == 1) {
            return days + " day ago";
        }
        int hours = (int) (difference/(1000*60*60));
        if(hours > 1){
            return hours + " hrs ago";
        }else if(hours == 1){
            return hours + " hr ago";
        }
        int min = (int) (difference/(1000*60));
        if(min > 1){
            return min + " mins ago";
        }else if(min == 1){
            return min + " min ago";
        }
        int sec = (int)(difference/(1000));
        if(sec > 1){
            return sec + " secs ago";
        }else if(sec == 1){
            return sec + " sec ago";
        }else {
            return "just now";
        }

    }

    private void decideTheme(ViewHolder holder, String themeName) {

        switch (themeName.toLowerCase()){
            case "light":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.Title.setTextColor(context.getResources().getColor(R.color.darkTextPrimary));
                holder.Info.setTextColor(context.getResources().getColor(R.color.darkTextHint));
                holder.Content.setTextColor(context.getResources().getColor(R.color.darkTextSecondary));
                holder.Content.setLinkTextColor(context.getResources().getColor(R.color.darkTextSecondary));
                break;
            case "dark":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_dark));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_dark));
                holder.Title.setTextColor(context.getResources().getColor(R.color.lightTextPrimary));
                holder.Info.setTextColor(context.getResources().getColor(R.color.lightTextHint));
                holder.Content.setTextColor(context.getResources().getColor(R.color.lightTextSecondary));
                holder.Content.setLinkTextColor(context.getResources().getColor(R.color.lightTextSecondary));
                break;
            case "white":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_white));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_white));
                holder.Title.setTextColor(context.getResources().getColor(R.color.darkTextPrimary));
                holder.Info.setTextColor(context.getResources().getColor(R.color.darkTextHint));
                holder.Content.setTextColor(context.getResources().getColor(R.color.darkTextSecondary));
                holder.Content.setLinkTextColor(context.getResources().getColor(R.color.darkTextSecondary));
                break;
            case "black":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_black));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_black));
                holder.Title.setTextColor(context.getResources().getColor(R.color.lightTextPrimary));
                holder.Info.setTextColor(context.getResources().getColor(R.color.lightTextHint));
                holder.Content.setTextColor(context.getResources().getColor(R.color.lightTextSecondary));
                holder.Content.setLinkTextColor(context.getResources().getColor(R.color.lightTextSecondary));
                break;
            default:
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.Title.setTextColor(context.getResources().getColor(R.color.darkTextPrimary));
                holder.Info.setTextColor(context.getResources().getColor(R.color.darkTextHint));
                holder.Content.setTextColor(context.getResources().getColor(R.color.darkTextSecondary));
                holder.Content.setLinkTextColor(context.getResources().getColor(R.color.darkTextSecondary));
        }
    }

}
