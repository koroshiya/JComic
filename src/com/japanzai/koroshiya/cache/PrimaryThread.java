package com.japanzai.koroshiya.cache;

/**
 * Purpose: Thread for parsing the previous or next entry in a directory.
 * As a primary thread, the value returned is to be used in the near future,
 * usually the moment a user moves to the next entry in a Steppable.
 * */
public class PrimaryThread extends CacheThread{
		
	public PrimaryThread(Steppable steppable, boolean forward) {
	     super(steppable, forward);
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
