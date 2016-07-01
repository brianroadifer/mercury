package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Activities.ArticleItemActivity;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;
import com.squareup.picasso.Picasso;


/**
 * Created by Brian Roadifer on 7/1/2016.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    Article article;
    Context context;

    public ArticleAdapter(Article article, Context context){
        this.article = article;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.Title.setText(article.Title);
        holder.Content.setText(article.Content);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView Title, Content;
        public ViewHolder(View view){
            super(view);
            Title = (TextView) view.findViewById(R.id.article_title);
            Content = (TextView) view.findViewById(R.id.article_content);
        }
    }
}
