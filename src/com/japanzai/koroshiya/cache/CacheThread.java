package com.japanzai.koroshiya.cache;

import android.graphics.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.interfaces.StepThread;
import com.japanzai.koroshiya.io_utils.ImageParser;

/**
 * Purpose: Class to subclass for any Threads used to step through
 * 			and parse images from a directory (as opposed to an archive).
 * */
public abstract class CacheThread extends Thread implements StepThread {
	
	private final Steppable entry;
	private final boolean direction; //Direction of thread. 
	//Used by subclasses to determine which direction to cache
	
	public CacheThread(Steppable entry, boolean forward){
		this.entry = entry;
		this.direction = forward;
	}
	
	@Override
	public JBitmapDrawable parseImage(int fIndex){
    	
		try{
		
			int i = fIndex;
			String path = (String)entry.getImages().get(i).getImage();			
			InputStream is = new FileInputStream(path);
			Point p = ImageParser.getImageSize(is);
			is = new FileInputStream(path);
			
			JBitmapDrawable temp = ImageParser.parseImageFromDisk(is, p.x, p.y, path);
			if (temp == null){
				entry.clear();
				is = new FileInputStream(path);
				temp = ImageParser.parseImageFromDisk(is, p.x, p.y, path);
			}
			
			return temp;
		
		}catch (IOException ex){
			ex.printStackTrace();
		}
		
		return null;
    	
    }

	/**
	 * @return Gets the currently held Steppable object
	 * */
	public Steppable getEntry(){
		return this.entry;
	}
	
	/**
	 * @return Returns true for forward, false for backward
	 * */
	public boolean getDirection(){
		return direction;
	}
	
	@Override
	public JBitmapDrawable nextBitmap(){
		
    	int cacheIndex = this.entry.getIndex();
    	
    	if (cacheIndex < this.entry.getMax() - 1){
    		cacheIndex++;
    	}else {
    		cacheIndex = 0;
    	}
    	
    	return parseImage(cacheIndex);
    	
	}
	
	@Override
	public JBitmapDrawable previousBitmap(){
		
    	int cacheIndex = this.entry.getIndex();
    	
    	if (cacheIndex > 0){
    		cacheIndex--;
    	}else {
    		cacheIndex = this.entry.getMax() - 1;
    	}
    	
    	return parseImage(cacheIndex);
    	
	}
		
}
