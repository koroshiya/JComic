package com.japanzai.koroshiya.archive.steppable.thread.zip;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;

/**
 * Purpose: Caches the previous file in a zip archive.
 * 			Executed on this thread to allow for 
 * 			processing in the background.
 * */
public class PreviousZipThread extends ZipThread{

	public PreviousZipThread(SteppableArchive steppable, boolean primary) {
		super(steppable, primary);
	}

	@Override
	public void run() {
		if (super.isPrimary()){
			super.getSteppable().setCachePrimary(previousBitmap());
		}else {
			super.getSteppable().setCacheSecondary(previousBitmap());
		}
	}
	
}
