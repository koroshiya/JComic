package com.japanzai.koroshiya.controls;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;

public class ResizingGridView extends GridView {
    private int previousFirstVisible;
    private boolean isCentered = false;

    public ResizingGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ResizingGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizingGridView(Context context) {
        super(context);
    }

    public void setCentered(boolean centered){
        this.isCentered = centered;
    }

    @Override
    protected void onLayout ( boolean changed, int leftPos, int topPos, int rightPos, int bottomPos){
        super.onLayout(changed, leftPos, topPos, rightPos, bottomPos);
        setHeights();
    }

    @Override
    protected void onScrollChanged ( int newHorizontal, int newVertical, int oldHorizontal, int oldVertical){
        int firstVisible = getFirstVisiblePosition();
        if (previousFirstVisible != firstVisible) {
            previousFirstVisible = firstVisible;
            setHeights();
        }

        super.onScrollChanged(newHorizontal, newVertical, oldHorizontal, oldVertical);
    }

    private int getNumColumnsCompat() {
        if (Build.VERSION.SDK_INT >= 11) {
            return getNumColumns();
        } else {
            int columns = 0;
            int children = getChildCount();
            if (children > 0) {
                int width = getChildAt(0).getMeasuredWidth();
                if (width > 0) {
                    columns = getWidth() / width;
                }
            }
            return columns > 0 ? columns : AUTO_FIT;
        }
    }

    private void setHeights() {
        ListAdapter adapter = getAdapter();
        int numColumns = getNumColumnsCompat();
        int totalRowHeight = 0;

        if (adapter != null) {
            for (int i = 0; i < getChildCount(); i+=numColumns) {
                int maxHeight = 0;
                for (int j = i; j < i+numColumns; j++) {
                    View view = getChildAt(j);
                    if (view != null && view.getHeight() > maxHeight) {
                        maxHeight = view.getHeight();
                    }
                }

                if (maxHeight > 0) {
                    for (int j = i; j < i+numColumns; j++) {
                        View view = getChildAt(j);
                        if (view != null && view.getHeight() != maxHeight) {
                            view.setMinimumHeight(maxHeight);
                        }
                    }
                }
                totalRowHeight += maxHeight;
            }
        }
        if (isCentered) {
            totalRowHeight = this.getHeight() - totalRowHeight;
            this.setPadding(0, totalRowHeight / 3, 0, 0);
        }
    }
}
