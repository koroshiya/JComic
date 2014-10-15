package com.japanzai.koroshiya.archive.steppable.thread.zip;

import java.io.IOException;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.archive.steppable.thread.ArchiveThread;
import com.japanzai.koroshiya.controls.JBitmapDrawable;

/**
 * Purpose: Abstract class to be extended by threads 
 * 			for parsing files from a zip archive.
 * */
public abstract class ZipThread extends ArchiveThread {

	public ZipThread(SteppableArchive steppable, boolean primary) {
		
		super(steppable, primary);
		
	}
	
	public JBitmapDrawable parseImage(int fIndex){
    	
		try {
			return super.getSteppable().parseImage(fIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	
	}
	
}
