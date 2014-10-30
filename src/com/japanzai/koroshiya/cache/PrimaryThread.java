package com.japanzai.koroshiya.cache;

import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Thread for parsing the previous or next entry in a directory.
 * As a primary thread, the value returned is to be used in the near future,
 * usually the moment a user moves to the next entry in a Steppable.
 * */
public class PrimaryThread extends CacheThread{
		
	public PrimaryThread(Steppable steppable, boolean forward, Reader r) {
	     super(steppable, forward, r);
	}

	@Override
	public void run(){
		
		if (super.getDirection()){
			getEntry().setCachePrimary(nextBitmap()); 
		}else{
			getEntry().setCachePrimary(previousBitmap()); 
		}
			
	}
		
}
