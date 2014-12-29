package com.japanzai.koroshiya.archive.steppable.thread.rar;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Caches the previous file in a rar archive.
 * 			Executed on this thread to allow for 
 * 			processing in the background.
 * */
public class PreviousRarThread extends RarThread{

	public PreviousRarThread(SteppableArchive steppable, boolean primary) {
		super(steppable, primary);
	}

	@Override
	public void run() {
		if (super.isPrimary()){
			super.getSteppable().setCachePrimary(previousBitmap());
		}else{
			super.getSteppable().setCacheSecondary(previousBitmap());
		}
	}
	
}
