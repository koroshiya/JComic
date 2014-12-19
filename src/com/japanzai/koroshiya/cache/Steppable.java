package com.japanzai.koroshiya.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.JImage;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.interfaces.Cacheable;
import com.japanzai.koroshiya.interfaces.StepThread;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Progress;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.reader.ToastThread;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Provides methods for moving through a list of image files.
 * 			Intended for moving through directories, archives, etc.
 * */
public abstract class Steppable implements Cacheable{

	private ArrayList<JImage> images;
	
	private int index;
	private int min;
	private int max;
	private final String path;
	
	private JBitmapDrawable cachePrimary = null;
	private JBitmapDrawable cacheSecondary = null;

	private StepThread primary = null; //Thread for caching the next image
	private StepThread secondary = null; //Thread for caching the previous image
	private ParseCurrentImageThread pcit = null;
	
	protected final Reader parent;
	
	public Steppable(Reader parent, String path){
		this.parent = parent;
		this.path = path;
		images = new ArrayList<>();
	}
	
	/**
	 * Purpose: Parse the image file at the current index
	 * @throws IOException 
	 * */
	public abstract void parseCurrent() throws IOException;
    
	/**
	 * Purpose: Adds an image or image reference to the arraylist
	 * @param image Displayable image, String reference, etc. to
	 * 				be added to the image arraylist.
	 * */
	public void addImage(JImage image){
		this.images.add(image);
		this.max++;
	}
	
    /**
     * Purpose: Moves to the next valid index.
     * @throws InterruptedException Passed up from parse(boolean)
     * */
	public synchronized void next() throws InterruptedException{
    	
    	if (max > 1){
    		
    		int tempIndex = index;
    		
    		if (index < max - 1){
    			index++;
    		}else if (parent.getSettings().isLoopModeEnabled()){
    			index = 0;
    		}else {
        		parent.runOnUiThread(new ToastThread(R.string.end_of_chapter, parent, Toast.LENGTH_SHORT));
    			return;
    		}
    		
    		if (!parse(true)){
    			setIndex(tempIndex);
    		}else{
    			parent.getSettings().setLastReadIndex(index);
    		}
    		
    	}
    	
    }

    public synchronized void goToPage(int i){
    	clear();
    	setIndex(i);
	   	pcit = new ParseCurrentImageThread();
	    pcit.start();
	}

    public synchronized void last(){
    	if (max > 1){
    		goToPage(max - 1);
    	}
    	
    }

    public synchronized void first(){
    	goToPage(0);
    }

    /**
     * Purpose: Moves to the previous valid index.
     * @throws InterruptedException Passed up from parse(boolean)
     * */
	public synchronized void previous() throws InterruptedException{
    	
    	if (max > 1){
    		
    		int tempIndex = index;
    		
    		if (index > 0){
    			index--;
    		}else if (parent.getSettings().isLoopModeEnabled()){
    			index = (max - 1);
    		}else {
        		parent.runOnUiThread(new ToastThread(R.string.start_of_chapter, parent, Toast.LENGTH_SHORT));
    			return;
    		}
    		
    		if (!parse(false)){
    			setIndex(tempIndex);
    		}else{
				parent.getSettings().setLastReadIndex(index);
			}
    		
    	}
    	
    }
    
	/**
     * @param i New value for local index.
     * */
	public void setIndex(int i){
    	
    	if (i >= 0 && i < max){
    		this.index = i;
    		parent.getSettings().setLastReadIndex(i);
    	}
    	
    }
    
	/**
     * @param i Maximum index allowed for this Steppable object.
     * */
	public void setMax(int i){
    	this.max = i;
    }
    
	/**
     * @param i Minimum index allowed for this Steppable object.
     * 			This value is almost always 0.
     * */
	public void setMin(int i){
    	this.min = i;
    }
    
	/**
	 * Purpose: Extracts and parses the file at the specified index.
	 * @param findex The index of the item to extract
	 * @return Returns a displayable image file
	 * @throws IOException if the File could not be read from disk
	 * */
	public abstract JBitmapDrawable parseImage(int findex) throws IOException;
	
	/**
	 * Sorts the JImage objects held by this class.
	 * */
	public void sort(){
		Collections.sort(images);
	}
	
	private class ParseCurrentImageThread extends Thread{
		
		@Override
		public void run(){
			JBitmapDrawable temp;
			clear();
			try {
				temp = parseImage(index);
	    		parent.setImage(temp);
	    		cache(parent.getSettings().getCacheModeIndex() != 2);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (Progress.isVisible && Progress.self != null){
					Progress.self.oldFinish();
				}
			}
		}
		
	}
	
	/**
	 * Purpose: Parses the next or previous reference in the Steppable object.
	 * 			Also replaces the cached values accordingly.
	 * 			eg. If moving forward, cacheForward is displayed,
	 * 			the previously displayed image is assigned to cacheBackward, 
	 * 			and a new cacheForward is parsed.
     * @param forward If true, parse the next object. If false, parse the previous one.
     * */
	public synchronized boolean parse(boolean forward) {
		
		try{
			Log.w("curIndex", "Current Index: " + Integer.toString(index));
    		if (pcit != null && pcit.isAlive()){
    			Log.w("cache1", "cache1");
    			caching();
    			return false;
    		}
    		pcit = null;
    		if (threadAlive(forward)){
    			Log.w("load1", "load1");
    			loading();
    			return false;
    		}else if (cacheEmpty(forward)){
    			Log.w("cache2", "cache2");
    			clear();
	    		System.gc();
	    		caching();
	    		pcit = new ParseCurrentImageThread();
	    		pcit.start();
	    		return true;
			}
			System.out.println("cacheHandler");
    		cacheHandler(forward);
    	
		}catch (OutOfMemoryError oom){
			oom.printStackTrace();
			clear();
			System.gc();
			pcit = new ParseCurrentImageThread();
	    	pcit.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
		
	}
	
	/**
	 * Clears all cached displayable images, NOT the JImage references
	 * */
	public void clear(){
		primary = null;
		secondary = null;
		pcit = null;
        cachePrimary = null;
        cacheSecondary = null;
		parent.setImage(null);
		System.gc();
	}
	
	public abstract void close();
	
	/**
	 * Directs this class's caching mechanism depending on the direction,
	 * type and level of the cache settings.
	 * @param forward If true, the user is moving forward through the list.
	 * Otherwise, they're moving backward through it.
	 * @exception IOException if the image at the current index couldn't be parsed.
	 * */
	private void cacheHandler(boolean forward) throws IOException{
		
		int cacheLevel = parent.getSettings().getCacheLevel();
		
		if (cacheLevel == 0){
			caching();
    		pcit = new ParseCurrentImageThread();
    		pcit.start();
		}
		
		int cacheMode = parent.getSettings().getCacheModeIndex();
		
		if (cacheMode == 0){
			cacheBidirectional(forward, cacheLevel);
		}else {
			cache(cacheMode == 1, forward, cacheLevel);
		}
		
	}
	
	/**
	 * Used for caching a single image in either direction.
	 * @param forward If true, the next image is cached. 
	 * Otherwise, the previous image is cached.
	 * */
	protected void cache(boolean forward){
		if (parent.getSettings().getCacheLevel() != 0){
			cachePrimary = null;
			primary = forward ? getNextThread(true) : getPreviousThread(true);
	    	((Thread) primary).start();
		}
	}
	
	/**
	 * This method caches another image back/forward, defined by the index passed in.
	 * The secondary cache is filled, not the primary cache.
	 * This means that the image cached won't be used directly; it will be passed 
	 * into the primary cache when needed, which will later be used from there.
	 * @param index Index of the image to parse
	 */
	private void cacheIndex(int index){
		cacheSecondary = null;
		secondary = getImageThread(index);
    	((Thread) secondary).start();   
	}
	
	/**
	 * Handles bidirectional caching. ie. Caching forward and backward simultaneously.
	 * @param forward If true, the user is moving forward through the Steppable.
	 * Otherwise, the user is moving backward.
	 * */
	private void cacheBidirectional(boolean forward, int cacheLevel){
		
    	Log.w("cacheBiInit", "Page: " + Integer.toString(this.getIndex()));
		
		if (forward){
			cacheSecondary = (cacheLevel == 2 && cachePrimary != null) ? parent.getImage() : null;
	    	parent.setImage(cachePrimary);
	    	cachePrimary = null;
			primary = getNextThread(true);
	    	((Thread) primary).start(); 
		}else{
			cachePrimary = (cacheLevel == 2 && cacheSecondary != null) ? parent.getImage() : null;
	    	parent.setImage(cacheSecondary);
	    	cacheSecondary = null;
			secondary = getPreviousThread(false);
	    	((Thread) secondary).start(); 
		}
		
	}
	
	/**
	 * Handles caching in a particular direction.
	 * @param directionSetting User-defined direction of the cache.
	 * If true, user wants to prioritize caching forward. 
	 * Otherwise, the user wants to prioritize caching backward.
	 * @param forward Represents the direction the cache is moving in.
	 * If true, the user is moving forward. If false, they're moving backward.
	 * @param level Integer representing the level (aggression) of caching to perform.
	 * @exception IOException if the image at the current index couldn't be parsed
	 * */
	private void cache(boolean directionSetting, boolean forward, int level) throws IOException {
		
		if (level == 1){
			if (directionSetting == forward){
				parent.setImage(cachePrimary);
				cache(forward);
			}else{
				cachePrimary = parent.getImage();
				loading();
				parent.setImage(parseImage(index));
			}
		}else if (level == 2){
			
			if (directionSetting != forward){
				cacheSecondary = cachePrimary;
				cachePrimary = parent.getImage();
				loading();
				parent.setImage(parseImage(index));
				return;
			}
			
			parent.setImage(cachePrimary);
			
			if (bitmapExists(cacheSecondary)){
				cachePrimary = cacheSecondary;
			}else{
				cache(forward);
			}
			
			int tmpIndex;
			
			if (forward && index < max){
				tmpIndex = index + 1;
			}else if (!forward && index > min){
				tmpIndex = index - 1;
			}else{
				if (MainActivity.mainActivity.getSettings().isLoopModeEnabled()){
					tmpIndex = forward ? min : max;
				}else{
					return;
				}
			}
			
			cacheIndex(tmpIndex);
			
		}
		
	}
	
	/**
	 * Creates a thread to display a loading message while an image is being parsed
	 * */
	private void loading(){
		showProgressUi(parent);
		//parent.runOnUiThread(new ToastThread(R.string.caching, parent, Toast.LENGTH_SHORT));
	}
	
	private void caching(){ //TODO: run Progress on UI thread instead
		showProgressUi(parent);
		//parent.runOnUiThread(new ToastThread(R.string.loading_next, parent, Toast.LENGTH_SHORT));
	}
	
	private class ProgressThread extends Thread{
		
		private Activity cont;
		
		private ProgressThread(Activity cont){
			this.cont = cont;
		}
		
		public void run(){
			Intent intent = new Intent(cont, Progress.class);
			Bundle ba = new Bundle();
			ba.putInt("index", -1);
			intent.putExtras(ba);
			cont.startActivity(intent);
		}
	}
	
	private void showProgressUi(Activity cont){
		cont.runOnUiThread(new ProgressThread(cont));
	}
	
	/**
	 * Purpose: Checks if an image parsing thread is currently running.
	 * @param forward Direction the cache is moving in.
	 * If true, the user is caching forward. Otherwise, the user is caching backward.
	 * @return If an image parsing thread is running, returns true. Otherwise, false.
	 * */
	private boolean threadAlive(boolean forward){
		
		SettingsManager sm = parent.getSettings();
		if (sm.getCacheSafety()){
			return (isThreadAlive(primary) || isThreadAlive(secondary));
		}
		
		int cacheModeIndex = sm.getCacheModeIndex();
		
		if (cacheModeIndex == 0 && !forward){
			
			if (isThreadAlive(secondary)){
				if (isThreadAlive(primary)){
					((Thread) primary).interrupt();
					primary = null;
					cachePrimary = null;
				}
				return true;
			}
			
		}else{
			
			if (cacheModeIndex != 2 == forward){
				if (isThreadAlive(primary)){
					if (isThreadAlive(secondary)){
						((Thread) secondary).interrupt();
						secondary = null;
						cacheSecondary = null;
					}
					return true;
				}
			}else if (isThreadAlive(primary)){
				((Thread) primary).interrupt();
				primary = null;
				cachePrimary = null;
				return true;
			}
			
		}
		
		return false;
		
	}
	
	private boolean isThreadAlive(StepThread thread){
		 return thread != null && ((Thread)thread).isAlive();
	}
	
	private boolean bitmapExists(JBitmapDrawable bitmap){
		return bitmap != null && bitmap.getBitmap() != null;
	}
	
	/**
	 * Checks if the cache is empty or not
	 * @param forward Boolean value indicating which thread direction to check.
	 * Its use depends on the kind of cache a user has defined.
	 * @return If the cache is empty, returns true. Otherwise, false.
	 * */
	private boolean cacheEmpty(boolean forward){
		
		int cacheModeIndex = parent.getSettings().getCacheModeIndex();

		if (cacheModeIndex == 0 && !forward){
			if (bitmapExists(cacheSecondary)){
				return false;
			}
		}else{
			if (cacheModeIndex != 2 == forward){
				if (bitmapExists(cachePrimary)){
					return false;
				}
			}else if (bitmapExists(cacheSecondary)){
				return false;
			}
		}
		
		return true;
		
	}
	
	/**
	 * Purpose: Generates a thread to replace the used-up thread preceding it.
	 * @param primary Boolean indicating whether the thread to be retrieved will
	 * 			be a primary thread or not.
	 * @return Returns a Thread for caching the next image in a Steppable object.
	 * 			eg. NextRarThread, NextZipThread, etc.
	 * */
	public abstract StepThread getNextThread(boolean primary);
	
	/**
	 * Purpose: Generates a thread to replace the used-up thread preceding it.
	 * @return Returns a Thread for caching an image in a Steppable object at 
	 * 			the specified index.
	 * */
	public abstract StepThread getImageThread(int index);
	
	/**
	 * Purpose: Generates a thread to replace the used-up thread preceding it.
	 * @param primary Boolean indicating whether the thread to be retrieved will
	 * 			be a primary thread or not.
	 * @return Returns a Thread for caching the previous image in a Steppable object.
	 * 			eg. PreviousRarThread, PreviousZipThread, etc.
	 * */
	public abstract StepThread getPreviousThread(boolean primary);
	
    /**
     * Purpose: Caches the jBitmapDrawable for the next index
     * @param next The JBitmapDrawable object to be cached
     * */
	public synchronized void setCachePrimary(JBitmapDrawable next){
		Log.w("cachePrimary", "setCachePrimary");
    	this.cachePrimary = next;
    }
    
	/**
     * Purpose: Caches the JBitmapDrawable for the previous index
     * @param previous The JBitmapDrawable object to be cached
     * */
	public synchronized void setCacheSecondary(JBitmapDrawable previous){
		Log.w("cacheSecondary", "setCacheSecondary");
    	this.cacheSecondary = previous;
    }
        
    @Override
	public void emptyCache(){
    	images.clear();
    }

    /**
     * @return Returns the MainActivity object responsible for this object's creation
     * */
    public Reader getParent(){
    	return this.parent;
    }
    
    /**
     * @return Returns the stored primary Thread
     * */
    public StepThread getPrimary(){
    	return this.primary;
    }
    
    /**
     * @return Returns the stored secondary Thread
     * */
    public StepThread getSecondary(){
    	return this.secondary;
    }

    /**
     * @param primary New primary image parsing Thread
     * */
    public void setPrimary(StepThread primary){
    	this.primary = primary;
    }
    
    /**
     * @param secondary New secondary image parsing Thread
     * */
    public void setSecondary(StepThread secondary){
    	this.secondary = secondary;
    }
    
    /**
     * @return Current index of the Steppable object
     * */
    public int getIndex(){		
		return this.index;		
	}

	/**
     * @return Maximum index of the Steppable object
     * */
    public int getMax() {		
		return this.max;		
	}
	
	/**
     * @return Minimum index of the Steppable object.
     * 			This value should almost always be 0
     * */
    public int getMin(){
		return this.min;
	}
	
	/**
	 * @return Returns the current arraylist of files to parse
	 * */
    public ArrayList<JImage> getImages(){		
		return this.images;		
	}
    
    public String getPath(){
    	return this.path;
    }
    
}