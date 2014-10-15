package com.japanzai.koroshiya.archive.steppable.thread.rar;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;

/**
 * Purpose: Caches the file in a rar archive at the specified index.
 * 			Executed on this thread to allow for 
 * 			processing in the background.
 * */
public class IndexRarThread extends RarThread{

	private final int index;
	
	public IndexRarThread(SteppableArchive steppable, int index) {
		super(steppable, false);
		this.index = index;
	}

	@Override
	public void run() {
		super.getSteppable().setCacheSecondary(parseImage(index)); 
	}
	
}
