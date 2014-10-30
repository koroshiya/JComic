package com.japanzai.koroshiya.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;

/**
 * Purpose: ImageSwitcher that uses JScollViews instead of ImageViews
 * 			This allows for scrollable image viewing without giving
 * 			up the features the ImageSwitcher offers.
 * */
public class JImageSwitcher extends ImageSwitcher{
	
	public JImageSwitcher(Context context) {
		super(context);
		this.addView(new JScrollView(context));
		this.addView(new JScrollView(context));
	}
	
	public JImageSwitcher(Context context, AttributeSet att) {
		super(context, att);
		this.addView(new JScrollView(context));
		this.addView(new JScrollView(context));
	}
	
	@Override
	public void setImageDrawable(Drawable d){
		
		JScrollView scroll = (JScrollView)this.getNextView();
		scroll.setImageDrawable(d);
		showNext();
		
	}
	
	/**
	 * @return The JBitmapDrawable object currently displayed
	 * */
	public JBitmapDrawable getImageDrawable(){
		
		return ((JScrollView)this.getCurrentView()).getImageDrawable();
		
	}
	
	private class ClearThread implements Runnable{
		
		private final JImageSwitcher jis;
		
		public ClearThread(JImageSwitcher jis){
			this.jis = jis;
		}
		
		@Override
		public void run(){
			this.jis.removeAllViews();
			this.jis.addView(new JScrollView(this.jis.getContext()));
			this.jis.addView(new JScrollView(this.jis.getContext()));
		}
		
	}
	
	/**
	 * Clears the views held by this class
	 * */
	public void clear(){
		ClearThread ct = new ClearThread(this);
		((Activity)getContext()).runOnUiThread(ct);
	}
		
}
