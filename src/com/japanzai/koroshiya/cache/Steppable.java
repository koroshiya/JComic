package com.japanzai.koroshiya.cache;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.JImage;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.reader.ToastThread;

/**
 * Purpose: Provides methods for moving through a list of image files.
 * 			Intended for moving through directories, archives, etc.
 * */
public abstract class Steppable {

	public final ArrayList<JImage> images = new ArrayList<>();

	protected int index;
	private int max;
    protected final String path;

	private SoftReference<JBitmapDrawable> cachePrimary = new SoftReference<>(null);
	private SoftReference<JBitmapDrawable> cacheSecondary = new SoftReference<>(null);

    protected StepThread primary = null; //Thread for caching the next image
    protected StepThread secondary = null; //Thread for caching the previous image
	private ParseCurrentImageThread pcit = null;

	public Steppable(String path){
		this.path = path;
	}

    /**
     * Purpose: Caches an image and/or its filepath
     * @param name String name of file. Used for sorting.
     * */
    public void addImageToCache(Object absoluteFilePath, String name) {

        JImage j = new JImage(absoluteFilePath, name);
        images.add(j);
        max++;

    }

    /**
     * Purpose: Moves to the next valid index.
     * */
	public synchronized void next(Reader reader) {

    	if (max > 1){

    		int tempIndex = index;

    		if (index < max - 1){
    			index++;
    		}else if (reader.getSettings().isLoopModeEnabled()){
                setIndex(0, reader);
    		}else {
                nextChapter(reader);
    			return;
    		}

    		if (!parse(true, reader)){
    			setIndex(tempIndex, reader);
    		}else{
                reader.getSettings().setLastReadIndex(index);
    		}

    	}else if (!reader.getSettings().isLoopModeEnabled()){
			nextChapter(reader);
		}

    }

    private void goToApplicableChapter(boolean forward, Reader reader){
        if (reader.getSettings().isSwipeToNextModeEnabled()) {
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
                reader.runOnUiThread(new ToastThread(R.string.no_more_chapters_found, reader));
            } else {
                reader.finish(); //TODO: look at replacing instead of finishing and restarting
                MainActivity.getMainActivity().runOnUiThread(new ToastThread(forward ? R.string.chapter_next : R.string.chapter_previous, reader));
				MainActivity.getMainActivity().startReading(replacement.getAbsolutePath(), 0);
            }
        }else {
            reader.runOnUiThread(new ToastThread(forward ? R.string.end_of_chapter : R.string.start_of_chapter, reader));
            //TODO: context menu entry
        }
    }

    public void nextChapter(Reader reader){
        goToApplicableChapter(true, reader);
    }

    public void previousChapter(Reader reader){
        goToApplicableChapter(false, reader);
    }

    public synchronized void goToPage(int i, Reader reader){
    	clear();
    	setIndex(i, reader);
	   	pcit = new ParseCurrentImageThread(reader);
	    pcit.start();
	}

    public synchronized void last(Reader reader){
        if (index == max - 1){
            Log.i("Steppable", "Already on last page");
        }else if (max > 1){
			goToPage(max - 1, reader);
		}else{
			Log.i("Steppable", "One-page chapter");
		}
    }

    public synchronized void first(Reader reader){
    	if (index != 0){
            goToPage(0, reader);
        }else{
            Log.i("Steppable", "Already on first page");
        }
    }

    /**
     * Purpose: Moves to the previous valid index.
     * */
	public synchronized void previous(Reader reader) {

    	if (max > 1){

    		int tempIndex = index;

    		if (index > 0){
    			index--;
    		}else if (reader.getSettings().isLoopModeEnabled()){
                setIndex(max - 1, reader);
    		}else {
                previousChapter(reader);
    			return;
    		}

    		if (!parse(false, reader)){
    			setIndex(tempIndex, reader);
    		}else{
                reader.getSettings().setLastReadIndex(index);
			}

    	}else if (!reader.getSettings().isLoopModeEnabled()){
			previousChapter(reader);
		}

    }

	/**
     * @param i New value for local index.
     * */
	public void setIndex(int i, Reader reader){

    	if (i >= 0 && i < max){
    		this.index = i;
    		reader.getSettings().setLastReadIndex(i);
    	}else{
            Log.d("Steppable", "Index " + i + " is < 0 or > " + max);
        }

    }

	/**  */
	public void setMax(){
    	this.max = 0;
    }

	/**
	 * Purpose: Extracts and parses the file at the specified index.
	 * @param findex The index of the item to extract
	 * @return Returns a displayable image file
	 * */
	public abstract SoftReference<JBitmapDrawable> parseImage(int findex, int width, int resizeMode);

	/**
	 * Sorts the JImage objects held by this class.
	 * */
	public void sort(){
		Collections.sort(images);
	}

    private class ParseCurrentImageThread extends TimedThread{

        private final Reader reader;

        private ParseCurrentImageThread(Reader reader){
            this.reader = reader;
        }

        @Override
        public void run(){

            super.run();

            SoftReference<JBitmapDrawable> temp = parseImage(index, reader.getWidth(), reader.getSettings().getDynamicResizing());
            if (temp != null) {
                reader.setImage(temp);
            }
            this.isFinished = true;
            reader.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    reader.findViewById(R.id.progress).setVisibility(View.GONE);
                }
            });
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
	public synchronized boolean parse(boolean forward, Reader reader) {

		try{
			Log.w("curIndex", "Current Index: " + Integer.toString(index));
    		if (pcit != null && pcit.isAlive()){
    			Log.w("cache1", "cache1");
    			caching(reader);
    			return false;
    		}
    		pcit = null;
    		if (threadAlive(forward)){
    			Log.w("load1", "load1");
    			loading(reader);
    			return false;
    		}else if (cacheEmpty(forward)){
    			Log.w("cache2", "cache2");
	    		caching(reader);
	    		pcit = new ParseCurrentImageThread(reader);
	    		pcit.start();
                cacheHandler(forward, reader, false);
            }else{
                cacheHandler(forward, reader, true);
            }
            System.out.println("cacheHandler");

		}catch (OutOfMemoryError oom){
			oom.printStackTrace();
			clear();
			System.gc();
			pcit = new ParseCurrentImageThread(reader);
	    	pcit.start();
		}

        return true;

	}

    private void clear(){
        if (primary != null){
            primary.interrupt();
            primary = null;
        }
        if (secondary != null){
            secondary.interrupt();
            secondary = null;
        }
        this.cachePrimary.clear();
        this.cacheSecondary.clear();
    }

	public abstract void close();

	/**
	 * Directs this class's caching mechanism depending on the direction,
	 * type and level of the cache settings.
	 * @param forward If true, the user is moving forward through the list.
	 * Otherwise, they're moving backward through it.
	 * */
	private void cacheHandler(boolean forward, Reader reader, boolean moving) {

        cacheBidirectional(forward, reader, moving);

	}


    public void cacheBidirectional(){
        primary = getThread(true, true);
        primary.start();
        secondary = getThread(false, false);
        secondary.start();
    }

	/**
	 * Handles bidirectional caching. ie. Caching forward and backward simultaneously.
	 * @param forward If true, the user is moving forward through the Steppable.
	 * Otherwise, the user is moving backward.
	 * */
	private void cacheBidirectional(boolean forward, Reader reader, boolean moving){

        Log.w("cacheBiInit", "Page: " + Integer.toString(this.getIndex()));

		if (forward){
            if (moving){
                cacheSecondary = new SoftReference<>(reader.getImage());
                reader.setImage(cachePrimary);
            }else{
                cacheSecondary = new SoftReference<>(null);
            }
	    	//cachePrimary.clear();
			primary = getThread(true, true);
            primary.start();
		}else{
            if (moving){
                cachePrimary = new SoftReference<>(reader.getImage());
                reader.setImage(cacheSecondary);
            }else{
                cachePrimary = new SoftReference<>(null);
            }
            //cachePrimary.clear();
            secondary = getThread(false, false);
            secondary.start();
		}

	}

	/**
	 * Creates a thread to display a loading message while an image is being parsed
	 * */
	private void loading(Reader reader){
		showProgressUi(reader);
		//parent.runOnUiThread(new ToastThread(R.string.caching, parent, Toast.LENGTH_SHORT));
	}

	private void caching(Reader reader){ //TODO: run Progress on UI thread instead
		showProgressUi(reader);
		//parent.runOnUiThread(new ToastThread(R.string.loading_next, parent, Toast.LENGTH_SHORT));
	}

    private void showProgressUi(final Activity cont){
        cont.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cont.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            }
        });
    }

	/**
	 * Purpose: Checks if an image parsing thread is currently running.
	 * @param forward Direction the cache is moving in.
	 * If true, the user is caching forward. Otherwise, the user is caching backward.
	 * @return If an image parsing thread is running, returns true. Otherwise, false.
	 * */
	private boolean threadAlive(boolean forward){

		if (forward){

            if (isThreadAlive(secondary)){
                secondary.interrupt();
                secondary = null;
                //cacheSecondary.clear();
            }

            if (isThreadAlive(primary)){
                return true;
            }

		}else{

            if (isThreadAlive(primary)){
                primary.interrupt();
                primary = null;
                //cachePrimary.clear();
            }

            if (isThreadAlive(secondary)){
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

        return !bitmapExists(forward ? cachePrimary.get() : cacheSecondary.get());

	}

    /**
     * Purpose: Generates a thread to replace the used-up thread preceding it.
     * @return Returns a Thread for caching the next/previous image in a Steppable object.
     * */
    public abstract StepThread getThread(boolean primary, boolean forward);

    /**
     * Purpose: Caches the jBitmapDrawable for the next index
     * @param next The JBitmapDrawable object to be cached
     * */
	public synchronized void setCachePrimary(SoftReference<JBitmapDrawable> next){
		Log.w("cachePrimary", "setCachePrimary");
    	this.cachePrimary = next;
    }

	/**
     * Purpose: Caches the JBitmapDrawable for the previous index
     * @param previous The JBitmapDrawable object to be cached
     * */
	public synchronized void setCacheSecondary(SoftReference<JBitmapDrawable> previous){
		Log.w("cacheSecondary", "setCacheSecondary"); //TODO: UI of credits and about/report page; see tablet for guide
    	this.cacheSecondary = previous;
    }

	public void emptyCache(){
    	images.clear();
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