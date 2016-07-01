package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Brian Roadifer on 6/29/2016.
 */
public class FabScroll extends FloatingActionButton.Behavior {
    public FabScroll(Context context, AttributeSet attrs){
        super();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxCpmsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed){
        super.onNestedScroll(coordinatorLayout, child, target,dxCpmsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if(dyConsumed > 0){
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int fabBottomMargin = layoutParams.bottomMargin;
            child.animate().translationY(child.getHeight() + fabBottomMargin).setInterpolator(new LinearInterpolator()).start();
        }else if(dyConsumed < 0){
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }
    }

    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes){
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}

