package com.brianroadifer.mercuryfeed;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Brian Roadifer on 5/28/2016.
 */
public class VerticalSpace extends RecyclerView.ItemDecoration {
    int Space;
    public VerticalSpace(int space){
        this.Space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = Space;
        if(parent.getChildLayoutPosition(view)==0){
            outRect.top = Space;
        }
    }
}
