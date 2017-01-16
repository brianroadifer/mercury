package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brianroadifer.mercuryfeed.Activities.ArticleItemActivity;
import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.R;

import java.io.File;
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String themeName = pref.getString("app_screen", "Light");
        if(current != null){
            holder.Title.setText(current.Title);
        }else if(current == null && articles.size() == 1){
            holder.Title.setText("Articles Related Not Found");
        }else{
            holder.cardView.setVisibility(View.GONE);
        }
        decideTheme(holder, themeName);
    }

    @Override
    public int getItemCount() {
        try{
            return articles.size();
        }catch (NullPointerException e){
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView Title;
        CardView cardView;
        public ViewHolder(View view){
            super(view);
            Title = (TextView) view.findViewById(R.id.row_article_title);
            cardView = (CardView) view.findViewById(R.id.articleview);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Article article = articles.get(getAdapterPosition());
            Intent intent = new Intent(context, ArticleItemActivity.class);
            intent.putExtra("Article", article);
            context.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            Article article = articles.get(getAdapterPosition());
            ArticleHelper articleHelper = new ArticleHelper(context);
            Snackbar snackbar;
            if (v.isActivated()) {
                snackbar = Snackbar.make(v, "Saved Article", Snackbar.LENGTH_LONG);
                View sv = snackbar.getView();
                TextView stv = (TextView) sv.findViewById(android.support.design.R.id.snackbar_text);
                stv.setTextColor(context.getResources().getColor(R.color.article_background_white));
                snackbar.show();
                v.setAlpha(1f);
                v.setActivated(false);
                articleHelper.SaveArticle(article);
            } else {
                snackbar = Snackbar.make(v, "Deleted Article", Snackbar.LENGTH_LONG);
                View sv = snackbar.getView();
                TextView stv = (TextView) sv.findViewById(android.support.design.R.id.snackbar_text);
                stv.setTextColor(context.getResources().getColor(R.color.article_background_white));
                snackbar.show();
                v.setAlpha(0.5f);
                v.setActivated(true);
                File file = new File(context.getFilesDir(), ArticleHelper.FILENAME + article.ID);
                articleHelper.DeleteArticle(file);
                if(ArticleHelper.isExternalStorageReadable() && ArticleHelper.isExternalStorageWritable()){
                    file = new File(context.getExternalFilesDir(null), ArticleHelper.FILENAME + article.ID);
                    articleHelper.DeleteArticle(file);
                }
            }
            return false;
        }
    }

    private void decideTheme(ViewHolder holder, String themeName) {

        switch (themeName.toLowerCase()){
            case "light":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.Title.setTextColor(context.getResources().getColor(R.color.darkTextPrimary));
                break;
            case "dark":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_dark));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_dark));
                holder.Title.setTextColor(context.getResources().getColor(R.color.lightTextPrimary));
                break;
            case "white":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_white));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_white));
                holder.Title.setTextColor(context.getResources().getColor(R.color.darkTextPrimary));
                break;
            case "black":
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_black));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_black));
                holder.Title.setTextColor(context.getResources().getColor(R.color.lightTextPrimary));
                break;
            default:
                holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.app_screen_light));
                holder.Title.setTextColor(context.getResources().getColor(R.color.darkTextPrimary));
        }
    }
}
