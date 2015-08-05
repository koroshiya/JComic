package com.japanzai.koroshiya.cache;

import android.os.Handler;
import android.os.Looper;

import com.japanzai.koroshiya.controls.JBitmapDrawable;

/**
 * Interface that any Thread designed for parsing a Steppable should implement.
 * */
public class StepThread extends Thread {

    private final Steppable entry;
    private final boolean forward; //Direction of thread.
    private final boolean primary;

    public StepThread (Steppable entry, boolean primary, boolean forward){
        this.entry = entry;
        this.forward = forward;
        this.primary = primary;
    }

    /**
     * @return Gets the currently held Steppable object
     * */
    public Steppable getSteppable(){
        return this.entry;
    }

    public JBitmapDrawable nextBitmap(){

        int cacheIndex = this.entry.getIndex();
        return entry.parseImage(cacheIndex < this.entry.getMax() - 1 ? cacheIndex + 1 : 0);

    }

    public JBitmapDrawable previousBitmap(){

        int cacheIndex = this.entry.getIndex();
        return entry.parseImage(cacheIndex > 0 ? cacheIndex - 1 : this.entry.getMax() - 1);

    }

    @Override
    public void run() {
        final Thread th = this;
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        try {
                            th.interrupt();
                            th.stop();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, 60000);
                Looper.loop();
            }
        }).start();

        JBitmapDrawable jbd = forward ? nextBitmap() : previousBitmap();
        if (primary){
            entry.setCachePrimary(jbd);
        }else{
            entry.setCacheSecondary(jbd);
        }
    }
	
}
