package com.japanzai.koroshiya.archive.steppable.thread.zip;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;

/**
 * Purpose: Caches the next file in a zip archive.
 * 			Executed on this thread to allow for 
 * 			processing in the background.
 * */
public class NextZipThread extends ZipThread {
	
	public NextZipThread(SteppableArchive steppable, boolean primary) {
		
		super(steppable, primary);
		
	}
	
	@Override
	public void run() {
		if (super.isPrimary()){
			super.getSteppable().setCachePrimary(nextBitmap());
		}else{
			super.getSteppable().setCacheSecondary(nextBitmap());
		}
	}
	
}
