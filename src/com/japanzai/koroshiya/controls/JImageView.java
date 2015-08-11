package com.japanzai.koroshiya.controls;

import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.settings.SettingsManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Purpose: ImageView with overridden onMeasure method.
 * 			Used for scaling images outside of the default ImageView algorithm.
 * */
public class JImageView extends ImageView {

	private int originalWidth;
	private int originalHeight;
	private static int currentWidth = -1;
	private static int currentHeight;
	private double currentZoom = -1;
	private static double inheritedZoom = 1d;
	
	public JImageView(Context context) {
		super(context);

		SettingsManager settings = MainActivity.getMainActivity().getSettings();
		this.currentZoom = settings.keepZoomOnPageChange() ? inheritedZoom : settings.getCurrentZoomRatio();
		 
	}
	
	
	@Override
	public void setImageDrawable(Drawable drawable){
		
		if (drawable != null){
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			if (drawable instanceof JBitmapDrawable){
				JBitmapDrawable jbm = (JBitmapDrawable) drawable;
				originalWidth = jbm.getWidth();
				originalHeight = jbm.getHeight();
			}else{
				originalWidth = drawable.getIntrinsicWidth();
				originalHeight = drawable.getIntrinsicHeight();
			}
			
			if (currentWidth == -1 || !MainActivity.getMainActivity().getSettings().keepZoomOnPageChange()){
				currentWidth = originalWidth;
				currentHeight = originalHeight;
			}
			
		}
		
		zoom(drawable);
		
	}
	
	private Reader getReader(){
		return (Reader) getContext();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if (getDrawable() != null){
            if (currentZoom == SettingsManager.AUTO_SIZE){
				double ratio = (double)getReader().getWidth() / (double)originalWidth;
				if (ratio >= 1){
					setMeasuredDimension(originalWidth, originalHeight);
				}else{
					setMeasuredDimension((int)Math.floor(((double)originalWidth) * ratio), 
							(int)Math.floor(((double)originalHeight) * ratio));
				}
			}else if (currentZoom == SettingsManager.SCALE_HEIGHT_SIZE){
				double ratio = (double)getReader().getHeight() / (double)originalHeight;
				setMeasuredDimension((int)Math.floor(((double)originalWidth) * ratio), 
						(int)Math.floor(((double)originalHeight) * ratio));
			}else if (currentZoom == SettingsManager.SCALE_WIDTH_SIZE){
				double ratio = (double)getReader().getWidth() / (double)originalWidth;
				setMeasuredDimension((int)Math.floor(((double)originalWidth) * ratio), 
						(int)Math.floor(((double)originalHeight) * ratio));
			}else{
				setMeasuredDimension((int)Math.floor(currentWidth * currentZoom), 
									(int)Math.floor(currentHeight * currentZoom));
			}
		}else {
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}
		
	}
	
	/**
	 * Zooms in on the currently displayed image
	 * @param zoom Degree to which to zoom in
	 * */
	public void zoom(double zoom){
		
		zoom(zoom, getDrawable());
		
	}
	
	public void zoom(Drawable drawable){
		
		SettingsManager settings = MainActivity.getMainActivity().getSettings();
		double zoom = settings.getCurrentZoomRatio();
		if (settings.keepZoomOnPageChange()){
			pseudoZoom(drawable);
		}else{
			zoom(zoom, drawable);
		}
		
		
	}
	
	public void zoom(double zoom, Drawable drawable){
		
		if (drawable != null){

			inheritedZoom = zoom;
			currentWidth = originalWidth;
			currentHeight = originalHeight;
			pseudoZoom(drawable);
			
		}
		
	}
	
	public void pseudoZoom(Drawable drawable){
		
		if (drawable != null){

			super.setImageDrawable(null);
			super.setImageDrawable(drawable);
			 
			currentZoom = inheritedZoom;
		    measure(0, 0);
			
		}
		
	}
	
	public void zoomRatio(double zoom){
		
		Drawable drawable = getDrawable();
		
		if (drawable != null){
			super.setImageDrawable(null);
			super.setImageDrawable(drawable);
			if (zoom > 1){ //zoom in
				if (currentWidth > originalWidth * 5){
					return;
				}else if (currentWidth * zoom < currentWidth + 1){
					currentWidth++;
					currentHeight++;
				}
			}else { //zoom out
				if (currentWidth < originalWidth / 10){
					return;
				}
			}
			currentZoom = 1d;
			inheritedZoom = 1d;
			currentHeight *= zoom;
			currentWidth *= zoom;
			measure(0, 0);
		}
		
	}
	
	/**
	 * @return Double representing the current zoom ratio
	 * */
	public double getZoom(){
		return this.currentZoom;
	}
	
	
	
}