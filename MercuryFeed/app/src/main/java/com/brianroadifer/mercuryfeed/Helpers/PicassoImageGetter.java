package com.brianroadifer.mercuryfeed.Helpers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PicassoImageGetter implements Html.ImageGetter {

    private final Picasso picasso;
    private final Resources resources;
    private final TextView textView;

    public PicassoImageGetter(TextView textView, Resources resources, Picasso picasso){
        this.picasso = picasso;
        this.resources = resources;
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(final String source) {
        final BitmapDrawablePlaceholder result = new BitmapDrawablePlaceholder();

        AsyncTask<Void,Void,Bitmap> drawable;
        drawable = new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(final Void... meh) {
                try {
                    return picasso.load(source).get();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                try {
                    final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    result.setDrawable(drawable);
                    result.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    textView.setText(textView.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        drawable.execute((Void) null);
        return result;
    }

    static class BitmapDrawablePlaceholder extends BitmapDrawable{
        Drawable drawable;
        @Override
        public void draw(Canvas canvas) {
            if(drawable != null){
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable){
            this.drawable = drawable;
        }
    }
}

