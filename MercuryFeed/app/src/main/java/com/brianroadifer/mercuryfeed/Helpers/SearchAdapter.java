package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Brian Roadifer on 7/20/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "SearchAdapter";
    List<Feed> feeds;
    Map<String, Boolean> subscribed = new HashMap<>();
    Context context;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("/users/"+auth.getCurrentUser().getUid()+"/subscribed");

    public SearchAdapter(List<Feed> feeds, Context context){
        this.feeds = feeds;
        this.context = context;
    }
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:"+ parent.getChildCount());
        View view = LayoutInflater.from(context).inflate(R.layout.row_search_result, parent, false);
        return new SearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Feed current = feeds.get(position);
        Log.d(TAG, "onBindViewHolder:"+current.Title);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.w(TAG, "subscribed:" + child.getKey() + ":value:"+dataSnapshot.child(child.getKey()).getValue());
                    subscribed.put(child.getKey(), (boolean) dataSnapshot.child(child.getKey()).getValue());
                }

                if (current != null) {
                    holder.Title.setText(current.Title);
                    if(subscribed.containsKey(current.ID)) {
                        holder.Subscribe.setChecked(subscribed.get(current.ID));
                        holder.Subscribe.setText("Unsubscribe");
                    }else {
                        holder.Subscribe.setChecked(false);
                        holder.Subscribe.setActivated(false);
                        holder.Subscribe.setText("Subscribe");
                    }
                }
                holder.Subscribe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Map<String, Object>  maps = new HashMap<>();
                        maps.put(current.ID, isChecked);
                        if(isChecked){
                            reference.updateChildren(maps);
                        }else{
                            reference.child(current.ID).removeValue();
                        }
                        holder.Subscribe.setActivated(isChecked);
                        String text = isChecked ? "Unsubscribe":"Subscribe";
                        holder.Subscribe.setText(text);
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
