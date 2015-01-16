package com.japanzai.koroshiya.controls;

import java.io.ByteArrayOutputStream;

import com.japanzai.koroshiya.io_utils.ImageParser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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

    public JBitmapDrawable(byte[] bytes) {
        super(null, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
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
