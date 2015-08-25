package com.japanzai.koroshiya.cache;

import android.os.Handler;
import android.os.Looper;

/**
 * Purpose: Thread for parsing the an entry in a directory at the corresponding index.
 * This acts as a secondary thread, but parses the image at a specified index.
 * */
public class IndexThread extends StepThread {
	
	private final int index; //Index of the image to parse

    public IndexThread(Steppable steppable, int index, int width, int resize) {
        super(steppable, false, false, width, resize);
        this.index = index;
    }
	
	@Override
	public void run(){
        final Thread th = this;
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        try {
                            th.interrupt();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, 60000);
                Looper.loop();
            }
        }).start();

        Steppable step = getSteppable();
		step.setCacheSecondary(step.parseImage(index, width, resize));
			
	}
	
}
