package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroadifer.mercuryfeed.Activities.ArticleItemActivity;
import com.brianroadifer.mercuryfeed.Activities.ItemActivity;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Brian Roadifer on 7/1/2016.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    List<Article> articles;
    Context context;

    public ArticleAdapter(List<Article> articles, Context context){
        this.articles = articles;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_article_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Article current = articles.get(position);
        if(current != null){
            holder.Title.setText(current.Title);
            holder.articleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ArticleItemActivity.class);
                    intent.putExtra("Article", current);
                    context.startActivity(intent);
                }
            });
            holder.articleView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(v.getContext(), "Press and hold to delete article", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }else if(current == null && articles.size() == 1){
            holder.Title.setText("Articles Related Not Found");
        }else{
            holder.Title.setText("");
        }
    }

    @Override
    public int getItemCount() {
        try{
            return articles.size();
        }catch (NullPointerException e){
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView Title;
        RelativeLayout articleView;
        public ViewHolder(View view){
            super(view);
            Title = (TextView) view.findViewById(R.id.row_article_title);
            articleView = (RelativeLayout) view.findViewById(R.id.articleview);
        }
    }
}
