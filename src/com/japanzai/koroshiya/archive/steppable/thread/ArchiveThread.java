package com.japanzai.koroshiya.archive.steppable.thread;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.interfaces.StepThread;

/**
 * Purpose: Abstract class to be extended by threads 
 * 			for parsing files from an archive.
 * 			Any threads for extracting from an archive
 * 			should extend this class.
 * */
public abstract class ArchiveThread extends Thread implements StepThread{
	
	private final SteppableArchive entry; //SteppableArchive to extract entries from
	private final boolean primary;
	
	public ArchiveThread(SteppableArchive steppable, boolean primary) {
	     this.entry = steppable;
	     this.primary = primary;
	}
	
    @Override
    public synchronized JBitmapDrawable nextBitmap(){
		
    	int cacheIndex = entry.getIndex();
    	
    	cacheIndex = cacheIndex < entry.getMax() - 1 ? cacheIndex + 1 : 0;
    	
    	return parseImage(cacheIndex);
    	
    }
    
	@Override
	public synchronized JBitmapDrawable previousBitmap(){
    	
    	int cacheIndex = entry.getIndex();
    	
    	cacheIndex = cacheIndex > 0 ? cacheIndex - 1 : entry.getMax() - 1;
    		
    	return parseImage(cacheIndex);
    	
    	
    }
		
	/**
	 * @return Returns the archive held by this thread
	 * */
	public SteppableArchive getSteppable(){
		return entry;
	}
	
	/**
	 * @return If this thread is a primary thread, return true. Otherwise, return false.
	 * */
	public boolean isPrimary(){
		return this.primary;
	}
		
}
