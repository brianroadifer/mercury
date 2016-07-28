package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by Brian Roadifer on 7/20/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "SearchAdapter";
    List<Feed> feeds;
    List<String> subFeeds;
    Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("/users/"+auth.getCurrentUser().getUid()+"/subscribed");

    public SearchAdapter(List<Feed> feeds, Context context){
        this.feeds = feeds;
        this.context = context;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:"+ parent.getChildCount());
        View view = LayoutInflater.from(context).inflate(R.layout.row_search_result, parent, false);
        return new SearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Feed current = feeds.get(position);
        Log.d(TAG, "onBindViewHolder:"+current.Title);
        if(current != null){
            holder.Title.setText(current.Title);
            holder.Subscribe.isActivated();
            holder.Subscribe.setText("Subscribed");
            holder.Subscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Subscrible

                    //Unsubscribe
                }
            });

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
            return feeds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
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
