package com.japanzai.koroshiya.controls;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Class extending BitmapDrawable to provide a displayable image that remembers
 * its original dimensions. This is so it can be scaled according to the dimensions
 * it had when originally parsed.
 * */
public class JBitmapDrawable extends BitmapDrawable {

	private int originalWidth;
	private int originalHeight;

    public JBitmapDrawable(Bitmap decodeStream) {
        super(null, decodeStream);
    }

	/**
	 * @param height Original height of this image
	 * @param width Original width of this image
	 * */
	public void setDimensions(int width, int height){
		this.originalWidth = width;
		this.originalHeight = height;
	}
	
	/**
	 * @return Original width of this image
	 * */
	public int getWidth(){
		return this.originalWidth;
	}
	
	/**
	 * @return Original height of this image
	 * */
	public int getHeight(){
		return this.originalHeight;
	}
	
}
