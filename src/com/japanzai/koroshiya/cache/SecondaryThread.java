package com.japanzai.koroshiya.cache;

/**
 * Purpose: Thread for parsing the previous or next entry in a directory.
 * As a secondary thread, the value returned is not needed immediately
 * and won't be used until after being passed to a primary thread later on.
 * */
public class SecondaryThread extends CacheThread{
		
	public SecondaryThread(Steppable steppable, boolean forward) {
	     super(steppable, forward);
	}

	@Override
	public void run(){
		
		if (super.getDirection()){
			getEntry().setCacheSecondary(nextBitmap());
		}else{
			getEntry().setCacheSecondary(previousBitmap());
		}
		     	
	}
		
}
