package com.japanzai.koroshiya.cache;

import android.view.View;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.reader.MainActivity;

import java.lang.ref.SoftReference;

/**
 * Interface that any Thread designed for parsing a Steppable should implement.
 * */
public class StepThread extends TimedThread {

    private final Steppable entry;
    private final boolean forward; //Direction of thread.
    private final boolean primary;
    protected final int width;
    protected final int resize;

    public StepThread (Steppable entry, boolean primary, boolean forward, int width, int resize){
        this.entry = entry;
        this.forward = forward;
        this.primary = primary;
        this.width = width;
        this.resize = resize;
    }

    /**
     * @return Gets the currently held Steppable object
     * */
    public Steppable getSteppable(){
        return this.entry;
    }

    public SoftReference<JBitmapDrawable> nextBitmap(){

        int cacheIndex = this.entry.getIndex();
        return entry.parseImage(cacheIndex < this.entry.getMax() - 1 ? cacheIndex + 1 : 0, width, resize);

    }

    public SoftReference<JBitmapDrawable> previousBitmap(){

        int cacheIndex = this.entry.getIndex();
        return entry.parseImage(cacheIndex > 0 ? cacheIndex - 1 : this.entry.getMax() - 1, width, resize);

    }

    @Override
    public void run() {

        super.run();

        SoftReference<JBitmapDrawable> jbd = forward ? nextBitmap() : previousBitmap();
        if (primary){
            entry.setCachePrimary(jbd);
        }else{
            entry.setCacheSecondary(jbd);
        }

        this.isFinished = true;
        final MainActivity main = MainActivity.getMainActivity();
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (main.findViewById(R.id.progress) != null) main.findViewById(R.id.progress).setVisibility(View.GONE);
            }
        });
    }
	
}
