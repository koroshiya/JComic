package com.koroshiya.controls;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;

/**
 * Purpose: ImageView with overridden onMeasure method.
 * 			Used for scaling images outside of the default ImageView algorithm.
 * */
public class JImageView extends android.support.v7.widget.AppCompatImageView {

	private int originalWidth;
	private int originalHeight;
	private int currentWidth = -1;
	private int currentHeight;
	private double currentZoom = -1;
	private double inheritedZoom = 1d;
    private final boolean manualZoom;
    private final Point p;
	
	public JImageView(Context context) {
		this(context, null);
	}

    public JImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JImageView(Context c, AttributeSet attrs, int val1) {
        super(c, attrs, val1);
        if (SettingsManager.keepZoomOnPageChange(c)){
            manualZoom = true;
            this.currentZoom = inheritedZoom;
        }else{
            manualZoom = false;
            this.currentZoom = SettingsManager.getZoom(c);
        }

        p = ImageParser.getScreenSize(c);
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
			
			if (currentWidth == -1 || !SettingsManager.keepZoomOnPageChange(getContext())){
				currentWidth = originalWidth;
				currentHeight = originalHeight;
			}
			
		}
		
		zoom(drawable);
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if (getDrawable() != null){

            if (manualZoom || currentZoom == SettingsManager.ZOOM_FULL){
                setMeasuredDimension((int)Math.floor(currentWidth * currentZoom),
                        (int)Math.floor(currentHeight * currentZoom));
            }else{

                double xRatio = p.x / (double)originalWidth;
                double yRatio = p.y / (double)originalHeight;
                double ratio;

                if (currentZoom == SettingsManager.ZOOM_SCALE_HEIGHT || (currentZoom == SettingsManager.ZOOM_AUTO && yRatio < xRatio)){
                    ratio = yRatio;
                }else if (currentZoom == SettingsManager.ZOOM_SCALE_WIDTH || (currentZoom == SettingsManager.ZOOM_AUTO && xRatio <= yRatio)){
                    ratio = xRatio;
                }else{
                    throw new RuntimeException("Invalid zoom setting");
                }

                setMeasuredDimension((int)Math.floor(((double)originalWidth) * ratio),
                        (int)Math.floor(((double)originalHeight) * ratio));

            }

		}else {
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}
		
	}

	private void zoom(Drawable drawable){

		int zoom = SettingsManager.getZoom(getContext());
		if (SettingsManager.keepZoomOnPageChange(getContext())){
			pseudoZoom(drawable);
		}else{
			zoom(zoom, drawable);
		}


	}
	
	private void zoom(double zoom, Drawable drawable){
		
		if (drawable != null){

			inheritedZoom = zoom;
			currentWidth = originalWidth;
			currentHeight = originalHeight;
			pseudoZoom(drawable);
			
		}
		
	}
	
	private void pseudoZoom(Drawable drawable){
		
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


}