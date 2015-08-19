package com.japanzai.koroshiya.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.JImage;
import com.japanzai.koroshiya.archive.steppable.JRarArchive;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Progress;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.reader.ToastThread;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Provides methods for moving through a list of image files.
 * 			Intended for moving through directories, archives, etc.
 * */
public abstract class Steppable {

	private final ArrayList<JImage> images;
	
	protected int index;
	private int min;
	private int max;
    protected final String path;
	
	private JBitmapDrawable cachePrimary = null;
	private JBitmapDrawable cacheSecondary = null;

    protected StepThread primary = null; //Thread for caching the next image
    protected StepThread secondary = null; //Thread for caching the previous image
	private ParseCurrentImageThread pcit = null;
	
	protected final Reader parent;
	
	public Steppable(Reader parent, String path){
		this.parent = parent;
		this.path = path;
		images = new ArrayList<>();
	}
	
	/**
	 * Purpose: Parse the image file at the current index
	 * */
	public void parseCurrent() {
        getParent().setImage(parseImage(getIndex()));

        SettingsManager settings = parent.getSettings();
        if (settings.isCacheOnStart()){
            cache(settings.getCacheModeIndex() != 2);
        }
    }

    /**
     * Purpose: Caches an image and/or its filepath
     * @param name String name of file. Used for sorting.
     * */
    public void addImageToCache(Object absoluteFilePath, String name) {

        JImage j = new JImage(absoluteFilePath, name);
        this.images.add(j);
        this.max++;

    }
	
    /**
     * Purpose: Moves to the next valid index.
     * */
	public synchronized void next() {
    	
    	if (max > 1){
    		
    		int tempIndex = index;
    		
    		if (index < max - 1){
    			index++;
    		}else if (parent.getSettings().isLoopModeEnabled()){
                setIndex(0);
    		}else {
                nextChapter();
    			return;
    		}
    		
    		if (!parse(true)){
    			setIndex(tempIndex);
    		}else{
    			parent.getSettings().setLastReadIndex(index);
    		}
    		
    	}else if (!parent.getSettings().isLoopModeEnabled()){
			nextChapter();
		}
    	
    }

    private void goToApplicableChapter(boolean forward){
        if (parent.getSettings().isSwipeToNextModeEnabled()) {
            File curFile = new File(path);
            File curDir;
            Log.i("Steppable", "Starting at path: " + curFile.getName());
            if (curFile.isDirectory()){
                Log.i("Steppable", "File is directory");
                FileCache fCache = ((FileCache)this);
                if (curFile.list().length - 1 == fCache.getMax()){
                    Log.i("Steppable", "File is now parent");
                }else{
                    curFile = new File(fCache.getImages().get(fCache.getIndex()).getName());
                    Log.i("Steppable", "File is now indexed file");
                }
            }
            curDir = curFile.getParentFile();
            File[] contents = curDir.listFiles();
            if (forward){
                Arrays.sort(contents);
            }else {
                Arrays.sort(contents, Collections.reverseOrder());
            }
            boolean found = false;
            File replacement = null;
            Log.i("Steppable", "Compare file: " + curFile.getName());
            for (File f : contents) {
                Log.i("Steppable", "Testing file: " + f.getName());
                if (found) {
                    if (ArchiveParser.isSupportedArchive(f) || ImageParser.isSupportedDirectory(f)) {
                        replacement = f;
                        break;
                    }
                } else if (f.getName().equals(curFile.getName())) {
                    Log.i("Steppable", "Found file");
                    found = true;
                }
            }
            if (replacement == null || replacement.getName().equals(curFile.getName())) {
                parent.runOnUiThread(new ToastThread(R.string.no_more_chapters_found, parent));
            } else {
                this.parent.finish(); //TODO: look at replacing instead of finishing and restarting
                MainActivity.getMainActivity().runOnUiThread(new ToastThread(forward ? R.string.chapter_next : R.string.chapter_previous, parent));
				MainActivity.getMainActivity().startReading(replacement.getAbsolutePath(), 0);
            }
        }else {
            parent.runOnUiThread(new ToastThread(forward ? R.string.end_of_chapter : R.string.start_of_chapter, parent));
            //TODO: context menu entry
        }
    }

    public void nextChapter(){
        goToApplicableChapter(true);
    }

    public void previousChapter(){
        goToApplicableChapter(false);
    }

    public synchronized void goToPage(int i){
    	clear();
    	setIndex(i);
	   	pcit = new ParseCurrentImageThread();
	    pcit.start();
	}

    public synchronized void last(){
        if (index == max - 1){
            Log.i("Steppable", "Already on last page");
        }else if (max > 1){
			goToPage(max - 1);
		}else{
			Log.i("Steppable", "One-page chapter");
		}
    }

    public synchronized void first(){
    	if (index != 0){
            goToPage(0);
        }else{
            Log.i("Steppable", "Already on first page");
        }
    }

    /**
     * Purpose: Moves to the previous valid index.
     * */
	public synchronized void previous() {
    	
    	if (max > 1){
    		
    		int tempIndex = index;
    		
    		if (index > 0){
    			index--;
    		}else if (parent.getSettings().isLoopModeEnabled()){
                setIndex(max - 1);
    		}else {
                previousChapter();
    			return;
    		}
    		
    		if (!parse(false)){
    			setIndex(tempIndex);
    		}else{
				parent.getSettings().setLastReadIndex(index);
			}
    		
    	}else if (!parent.getSettings().isLoopModeEnabled()){
			previousChapter();
		}
    	
    }
    
	/**
     * @param i New value for local index.
     * */
	public void setIndex(int i){
    	
    	if (i >= 0 && i < max){
    		this.index = i;
    		if (parent != null) parent.getSettings().setLastReadIndex(i);
    	}else{
            Log.d("Steppable", "Index " + i + " is < 0 or > " + max);
        }
    	
    }
    
	/**  */
	public void setMax(){
    	this.max = 0;
    }
    
	/**  */
	public void setMin(){
    	this.min = 0;
    }
    
	/**
	 * Purpose: Extracts and parses the file at the specified index.
	 * @param findex The index of the item to extract
	 * @return Returns a displayable image file
	 * */
	public abstract JBitmapDrawable parseImage(int findex);
	
	/**
	 * Sorts the JImage objects held by this class.
	 * */
	public void sort(){
		Collections.sort(images);
	}
	
	private class ParseCurrentImageThread extends TimedThread{
		
		@Override
		public void run(){

			super.run();

			clear();

            JBitmapDrawable temp = parseImage(index);
            if (temp != null) {
                parent.setImage(temp);
                cache(parent.getSettings().getCacheModeIndex() != 2);
            }
			this.isFinished = true;
            if (Progress.self != null && Progress.self.isVisible){
                Progress.self.oldFinish();
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
	 * */
	private void cacheHandler(boolean forward) {
		
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
        if (parent.getSettings().getCacheLevel() != 0 &&
                ((this instanceof JRarArchive && parent.getSettings().isCacheForRar()) ||
                !(this instanceof JRarArchive))){
			cachePrimary = null;
			primary = getThread(true, forward);
            primary.start();
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
        secondary.start();
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
			primary = getThread(true, true);
            primary.start();
		}else{
			cachePrimary = (cacheLevel == 2 && cacheSecondary != null) ? parent.getImage() : null;
	    	parent.setImage(cacheSecondary);
	    	cacheSecondary = null;
			secondary = getThread(false, false);
            secondary.start();
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
	 * */
	private void cache(boolean directionSetting, boolean forward, int level) {
		
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
				if (MainActivity.getMainActivity().getSettings().isLoopModeEnabled()){
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
		
		private final Activity cont;
		
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
		
		int cacheModeIndex = parent.getSettings().getCacheModeIndex();
		
		if (cacheModeIndex == 0 && !forward){
			
			if (isThreadAlive(secondary)){
				if (isThreadAlive(primary)){
                    primary.interrupt();
					primary = null;
					cachePrimary = null;
				}
				return true;
			}
			
		}else{
			
			if (cacheModeIndex != 2 == forward){
				if (isThreadAlive(primary)){
					if (isThreadAlive(secondary)){
                        secondary.interrupt();
						secondary = null;
						cacheSecondary = null;
					}
					return true;
				}
			}else if (isThreadAlive(primary)){
                primary.interrupt();
				primary = null;
				cachePrimary = null;
				return true;
			}
			
		}
		
		return false;
		
	}
	
	private boolean isThreadAlive(StepThread thread){
		 return thread != null && thread.isAlive() && !thread.isFinished && !thread.isInterrupted();
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
			return !bitmapExists(cacheSecondary);
		}else{
            return !bitmapExists(cacheModeIndex != 2 == forward ? cachePrimary : cacheSecondary);
		}
		
	}

    /**
     * Purpose: Generates a thread to replace the used-up thread preceding it.
     * @return Returns a Thread for caching the next/previous image in a Steppable object.
     * */
    public abstract StepThread getThread(boolean primary, boolean forward);
	
	/**
	 * Purpose: Generates a thread to replace the used-up thread preceding it.
	 * @return Returns a Thread for caching an image in a Steppable object at 
	 * 			the specified index.
	 * */
	public abstract StepThread getImageThread(int index);
	
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
		Log.w("cacheSecondary", "setCacheSecondary"); //TODO: UI of credits and about/report page; see tablet for guide
    	this.cacheSecondary = previous;
    }

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
	 * @return Returns the current arraylist of files to parse
	 * */
    public ArrayList<JImage> getImages(){		
		return this.images;		
	}
    
    public String getPath(){
    	return this.path;
    }
    
}