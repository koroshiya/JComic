package com.japanzai.koroshiya.archive.steppable.thread.rar;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;

/**
 * Purpose: Caches the next file in a rar archive.
 * 			Executed on this thread to allow for 
 * 			processing in the background.
 * */
public class NextRarThread extends RarThread{

	public NextRarThread(SteppableArchive steppable, boolean primary) {
		super(steppable, primary);
	}

	@Override
	public void run() {
		if (super.isPrimary()){
			super.getSteppable().setCachePrimary(nextBitmap()); 
		}else {
			super.getSteppable().setCacheSecondary(previousBitmap());
		}
	}
	
}
