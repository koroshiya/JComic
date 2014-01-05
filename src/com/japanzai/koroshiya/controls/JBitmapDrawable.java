package com.japanzai.koroshiya.controls;

import java.io.ByteArrayOutputStream;

import com.japanzai.koroshiya.io_utils.ImageParser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
	
	@SuppressLint("NewApi")
	public byte[] getByteArray(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CompressFormat fmt;
		if (android.os.Build.VERSION.SDK_INT >= 14){
			fmt = Bitmap.CompressFormat.WEBP;
		}else{
			fmt = Bitmap.CompressFormat.JPEG;
		}
		this.getBitmap().compress(fmt, 100, baos);
		return ImageParser.compress(baos.toByteArray());
	}

	public void closeBitmap(){
		if (getBitmap() != null){
			getBitmap().recycle();
		}
	}
	
}
