package com.japanzai.koroshiya.archive.steppable.thread.rar;

import java.io.IOException;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.archive.steppable.thread.ArchiveThread;
import com.japanzai.koroshiya.controls.JBitmapDrawable;

/**
 * Purpose: Abstract class to be extended by threads 
 * 			for parsing files from a rar archive.
 * */
public abstract class RarThread extends ArchiveThread {

	public RarThread(SteppableArchive steppable, boolean forward) {
		
		super(steppable, forward);
		
	}
	
	public synchronized JBitmapDrawable parseImage(int fIndex){
    	    	
		try {
			return super.getSteppable().parseImage(fIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
    	
    }
	
}
