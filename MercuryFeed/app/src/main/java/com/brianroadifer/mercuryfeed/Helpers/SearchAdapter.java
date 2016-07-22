package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Activities.MainActivity;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Created by Brian Roadifer on 7/20/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    List<Feed> feeds;
    List<String> subFeeds;
    Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("/users/"+auth.getCurrentUser().getUid()+"/subscribed");

    public SearchAdapter(List<Feed> feeds, Context context){
        this.feeds = feeds;
        this.context = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_search_result, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Feed current = feeds.get(position);
        if(current != null){
            holder.Title.setText(current.Title);
            holder.Subscribe.isActivated();

            holder.searchResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("Feed", current);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        try{
            return feeds.size();
        }catch (NullPointerException e){
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView Title;
        Switch Subscribe;
        RelativeLayout searchResult;
        public ViewHolder(View view){
            super(view);
            Title = (TextView) view.findViewById(R.id.search_title);
            Subscribe = (Switch) view.findViewById(R.id.search_subscribe_switch);
            searchResult = (RelativeLayout) view.findViewById(R.id.search_result);
        }
    }
}
