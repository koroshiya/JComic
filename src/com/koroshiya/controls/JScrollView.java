package com.koroshiya.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.koroshiya.R;

/**
 * Purpose: Turns a ScrollView into a scrollable ImageView.
 * */
public class JScrollView extends TwoDScrollView {


    public JScrollView(Context context) {
        super(context);
    }

    public JScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JScrollView(Context context, AttributeSet attrs, int val1) {
        super(context, attrs, val1);
    }

    public JImageView getJImageView(){
        return (JImageView) findViewById(R.id.read_fragment_jimageview);
    }





	/**
	 * @param drawable
	 *            Object to display on the JImageView this object contains
	 * */
	public void setImageDrawable(final Drawable drawable) {

        getJImageView().setImageDrawable(drawable);
        scrollTo(0, 0);
		
	}

	/**
	 * @return JBitmapDrawable currently displayed
	 * */
	public JBitmapDrawable getImageDrawable() {

		return (JBitmapDrawable) getJImageView().getDrawable();

	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			
        int viewWidth = getJImageView().getMeasuredWidth();
        int viewHeight = getJImageView().getMeasuredHeight();
        int scrollWidth = this.getMeasuredWidth();
        int scrollHeight = this.getMeasuredHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewWidth,viewHeight);

        params.leftMargin = (scrollWidth > viewWidth) ? (scrollWidth - viewWidth) / 2 : 0;
        params.topMargin = (scrollHeight > viewHeight) ? (scrollHeight - viewHeight) / 2 : 0;

        getJImageView().setLayoutParams(params);

	}

	public void clearCache(){
        getJImageView().setImageDrawable(null);
	}
	
}
