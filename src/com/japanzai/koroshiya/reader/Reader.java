package com.japanzai.koroshiya.reader;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.JImage;
import com.japanzai.koroshiya.archive.steppable.JRarArchive;
import com.japanzai.koroshiya.cache.FileCache;
import com.japanzai.koroshiya.cache.StepThread;
import com.japanzai.koroshiya.cache.Steppable;
import com.japanzai.koroshiya.cache.TimedThread;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.controls.JScrollView;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Used to display information about this application
 * */
public class Reader extends FragmentActivity {
	
	private Steppable cache = null;
	private JScrollView imgPanel;

	private File tempFile;

	private int width;
	private int height;

	public boolean parsed = false;
	
	private SettingsManager settings;
	private AlertDialog alert;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.activity_reader);
        //findViewById(R.id.progress).setVisibility(View.VISIBLE);
        MainActivity.hideActionBar(this);
        
        Bundle b = getIntent().getExtras();
        this.tempFile = new File(b.getString("file"));

        settings = new SettingsManager(this);
        settings.setHomeDir(tempFile.getParent());

        int index;
        if (settings.getLastFileRead() != null && settings.getLastFileRead().equals(this.tempFile)){
            index = settings.getLastFileReadIndex();
            Log.d("Reader", "Last read index is "+index);
        }else{
            index = b.getInt("index");
            settings.setLastReadIndex(index);
            Log.d("Reader", "Starting from index " + index);
        }

        Progress p = new Progress(this, this.tempFile, index);

        //if (this.tempFile.isFile()) this.tempFile = this.tempFile.getParentFile();
        MainActivity.getMainActivity().tempDir = this.tempFile.getParentFile();

		if (settings.saveRecent()) settings.addRecent(tempFile.getAbsolutePath(), index);
		if (settings.saveSession()) settings.setLastRead(tempFile, index);

		imgPanel = (JScrollView) findViewById(R.id.imgPanel);

        Log.d("Reader", "Starting at index " + index);
        p.execute();

    }

	/**
	 * @return SettingsManager responsible for storing this application's
	 *         settings.
	 * */
	public SettingsManager getSettings() {
		return this.settings;
	}
	
    @Override
    public void onResume(){

		super.onResume();

        try {
            if (parsed) {
                if (cache != null && cache.getMax() != 0) {
                    this.cache.sort();
                    parseCurrent();
                } else {
                    runOnUiThread(new ToastThread(R.string.no_images, this));
                    finish();
                }
            } else {
                settings.forceOrientation(this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
	
	public void showContextMenu(){
		String[] items = getResources().getStringArray(R.array.array_context_menu);
        final Reader reader = this;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.setting_context_menu_head);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			    if (id == 0){
			    	cache.first(reader);
			    }else if(id == 1){
			    	cache.last(reader);
			    }else if (id == 2){
			    	show();
			    }else if (id == 3){
                    cache.nextChapter(reader);
                }else if (id == 4){
                    cache.previousChapter(reader);
                }
		    }
		});
		alert = builder.create();
		alert.show();
	}

    public boolean isAlertShowing(){
        return alert != null && alert.isShowing();
    }

	public void show(){
		
		if (android.os.Build.VERSION.SDK_INT >= 11){
			try{
				final Dialog d = new Dialog(this);
                final Reader reader = this;
		        d.setTitle(R.string.setting_context_menu_heading);
		        d.setContentView(R.layout.dialog);
		        Button b1 = (Button) d.findViewById(R.id.button1);
		        Button b2 = (Button) d.findViewById(R.id.button2);
		        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
		        np.setMaxValue(cache.getMax());
		        np.setMinValue(1);
                np.setValue(cache.getIndex() + 1);
		        np.setWrapSelectorWheel(false);
		        b1.setOnClickListener(new OnClickListener(){
		          @Override
		          public void onClick(View v) {
		              cache.goToPage(np.getValue() - 1, reader);
		              d.dismiss();
		           }
		          });
		         b2.setOnClickListener(new OnClickListener(){
		          @Override
		          public void onClick(View v) {
		              d.dismiss();
		           }
		          });
		        d.show();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}else{

			String items[] = new String[cache.getMax()];
			for(int i = 1; i <= cache.getMax(); i++){
				items[i-1] = (Integer.toString(i));
			}
            final Reader reader = this;

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.setting_context_menu_heading);
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
			    	cache.goToPage(id, reader);
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
		}

    }

	public static Point getScreenDimensions(Activity act) {
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Display display = act.getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			return size;
		} else {
			Display display = act.getWindowManager().getDefaultDisplay();
			return new Point(display.getWidth(), display.getHeight());
		}
	}
	
	/**
	 * Purpose: Clears this class's temporary file and gets this Activity ready
	 * to begin reading
	 * */
	public void clearTempFile(Activity act) {

		Point size = getScreenDimensions(this);
		width = size.x;
		height = size.y;
		
		this.parsed = true;
		
		this.tempFile = null;
		imgPanel.clearCache(act);
		
		if (cache != null && cache.getMax() != 0) {
			this.cache.sort();

            parseCurrent();
            // vf.showNext();
		} else {
			runOnUiThread(new ToastThread(R.string.no_images, this));
		}
	}

	/**
	 * @param cache
	 *            The object this class will read images from
	 * */
	public void setCache(Steppable cache) {
		this.cache = cache;
		this.cache.sort();
	}

	/**
	 * @return Returns the Steppable object this class reads images from
	 * */
	public Steppable getCache() {
		return this.cache;
	}

	/**
	 * @param i
	 *            Set the index of the Steppable's cache
	 * */
	public void setCacheIndex(int i) {
		
		if (this.cache != null){
			this.cache.setIndex(i, this);
		}else{
            Log.e("Reader", "Cache is null");
        }
		
	}

	/**
	 * @param absoluteFilePath
	 *            Path to add to Steppable's cache
	 * @param name
	 *            Name of the file to add to cache
	 * */
	public void addImageToCache(Object absoluteFilePath, String name) {
		cache.addImageToCache(absoluteFilePath, name);
		Log.d("Reader", "Adding to cache image: "+name);
	}

	/**
	 * @param d
	 *            Image for this Activity to display
	 * */
	public void setImage(SoftReference<JBitmapDrawable> d) {

		if ((cache != null)) {
			runOnUiThread(new SetImageThread(d));
		}

	}

	/**
	 * Used so that the image can be changed from another thread. Useful when
	 * processing an image in the background before displaying it
	 * */
	private class SetImageThread extends TimedThread {

		private final SoftReference<JBitmapDrawable> d;

		public SetImageThread(SoftReference<JBitmapDrawable> d) {
			this.d = d;
		}

		@Override
		public void run() {

            super.run();

            if (d.get() == null) Log.e("Reader", "Setting null image");

			imgPanel.setImageDrawable(d.get());

            this.isFinished = true;
		}

	}

	/**
	 * @return Returns the JBitmapDrawable displayed
	 * */
	public JBitmapDrawable getImage() {

		return imgPanel.getImageDrawable();

	}

	/**
	 * @return Screen width
	 * */
	public int getWidth() {
		return this.width;
	}

	/**
	 * @return Screen height
	 * */
	public int getHeight() {
		return this.height;
	}

    @Override
    public void onPause(){
        super.onPause();
        if (cache != null) cache.clear();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (settings.saveRecent()) settings.addRecent(cache.getPath(), cache.getIndex());
        if (settings.saveSession()) settings.setLastRead(new File(cache.getPath()), cache.getIndex());
        cache.emptyCache();
        cache.clear();
        cache.close();
        cache = null;
    }

    /**
     * Purpose: Parse the image file at the current index
     * */
    public void parseCurrent() {
        setImage(cache.parseImage(cache.getIndex(), getWidth(), getSettings().getDynamicResizing()));

        if (settings.isCacheOnStart()){
            cache.cacheBidirectional();
        }
    }

}
