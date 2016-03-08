package com.japanzai.koroshiya.controls;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: ImageView with overridden onMeasure method.
 * 			Used for scaling images outside of the default ImageView algorithm.
 * */
public class JImageView extends AppCompatImageView {

	private int originalWidth;
	private int originalHeight;
	private int currentWidth = -1;
	private int currentHeight;
	private double currentZoom = -1;
	private double inheritedZoom = 1d;
    private boolean manualZoom;
    private SettingsManager settings;
    private Point p;
	
	public JImageView(Context context) {
		super(context);
        setup(context);
	}

    public JImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public JImageView(Context context, AttributeSet attrs, int val1) {
        super(context, attrs, val1);
        setup(context);
    }

    private void setup(Context c){
        settings = new SettingsManager(c, false);

        if (settings.keepZoomOnPageChange()){
            manualZoom = true;
            this.currentZoom = inheritedZoom;
        }else{
            manualZoom = false;
            this.currentZoom = settings.getZoom();
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
			
			if (currentWidth == -1 || !settings.keepZoomOnPageChange()){
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

	public void zoom(Drawable drawable){

		int zoom = settings.getZoom();
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