package com.japanzai.koroshiya.archive.steppable.thread.zip;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;

/**
 * Purpose: Caches the file in a zip archive at the specified index.
 * 			Executed on this thread to allow for 
 * 			processing in the background.
 * */
public class IndexZipThread extends ZipThread {

	private final int index;
	
	public IndexZipThread(SteppableArchive steppable, int index) {
		super(steppable, false);
		this.index = index;
	}

	@Override
	public void run() {
		super.getSteppable().setCacheSecondary(parseImage(index)); 
	}

}
