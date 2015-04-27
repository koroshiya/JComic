package com.japanzai.koroshiya.reader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.cache.FileCache;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Parses directories, image files and archives.
 * 			Essentially initializes the main activity's cache.
 * */
public class Progress extends Activity implements ModalReturn{

    //TODO: when writing changelog - rotate did work in 1.14, but lost current page
	
	private File f;
	private int index;
	private Reader reader;
	private SteppableArchive temp;
	
	public static boolean isVisible = false;
	public static Progress self;
	
	@Override
	public void onPause(){
		super.onPause();
		isVisible = false;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		isVisible = true;
	}
	
	private class ProgressThread extends Thread{
		
		@Override
		public void run(){
			
	    	if (f.isDirectory()) { 
	    		reader.setCache(new FileCache(reader, f.getAbsolutePath()));
	    		parseDir(f, 0);
	    		reader.setCacheIndex(index == -1 ? 0 : index);
	    	}else if (ArchiveParser.isSupportedArchive(f)){
		    	if (!parseArchive()){
		    		reader.clearTempFile();
		    		decline();
		    		//reader.runOnUiThread(new MessageThread(R.string.archive_read_error, reader));
		        	return;
		    	}
	    	}else {
                Log.d("Progress", "Running thread with File");
                File parentDir = new File(f.getParent());
	    		reader.setCache(new FileCache(reader, parentDir.getAbsolutePath()));
	    		
	        	File[] list = parentDir.listFiles();
	        	Arrays.sort(list);

                for (File file : list){
	    			parseFile(file);
                    Log.d("Progress", "Comparing " + file.getName() + " to " + f.getName());
	    			if (file.getName().equals(f.getName())){
                        Log.d("Progress", "FileCache, found file " + f.getName() + ", setting index to " + index);
	    				reader.setCacheIndex(index == -1 ? 0 : index);
	    			}
	    		}
	    	}
	    	if (reader.getCache().getMax() > 0){
	    		finish();
	    	}else{
	    		reader.runOnUiThread(new MessageThread(R.string.no_images, reader));
	    		decline();
	    	}
		}
	}

	@Override
	public void onBackPressed() {
		
		runOnUiThread(new ToastThread(R.string.loading_progress, this));
		//TODO: double tap to cancel

	}
    
    @Override
    public void onCreate(Bundle savedInstanceState){
    	
        super.onCreate(savedInstanceState);
        self = this;
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.progress);
        MainActivity.hideActionBar(this);

        Bundle b = getIntent().getExtras();
        int i = b.getInt("index", 0);
        
	    if (i >= 0){
	        this.index = i;
	        this.f = new File(b.getString("file"));
	        reader = Reader.reader;
	        
	        ProgressThread thread = new ProgressThread();
	        thread.start();
        }
        
    }
    
    /**
     * @param file File to be tested. If supported, the file is processed.
     * */
    public void parseFile(File file){
    	
    	if (ImageParser.isSupportedImage(file)){
    		if (file.length() > 0) reader.addImageToCache(file.getAbsolutePath(), file.getAbsolutePath());
    		//Log.e("New file", file.getAbsolutePath());
    	}else{
    		Log.d("Progress", getString(R.string.unsupported_file) + file.getName());
    	}
    	
    }
    
    /**
     * @param dir Directory to process the contents of
     * */
    public void parseDir(File dir, int curLevel){
    	SettingsManager settings = MainActivity.mainActivity.getSettings();
    	File[] files = dir.listFiles();
    	Arrays.sort(files);
    	for (File f : files){
    		if (f.isFile()){
    			parseFile(f);
    		}else{
    			if (curLevel < settings.getRecursionLevel() || settings.getRecursionLevel() == SettingsManager.RECURSION_ALL){
    				parseDir(f, curLevel + 1);
    			}
    		}
    	}
    }

    public boolean parseArchive(){
    	    	
    	try {
    		
    		this.temp = ArchiveParser.parseArchive(f, reader);
    		
    		if (temp == null){
    			reader.runOnUiThread(new MessageThread(reader.getString(R.string.archive_read_error), reader));
    			return true;
    		}

            reader.setCache(this.temp);
        	
    		reader.setCacheIndex(this.index == -1 ? 0 : this.index);
    		
		} catch (IOException e) {
            reader.runOnUiThread(new MessageThread(reader.getString(R.string.archive_read_error), reader));
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    	    	
    }
    
    @Override
    public void accept(){
    	reader.setCache(this.temp);
    	finish();
    }
    
    @Override
    public void decline(){
    	finish();
    }
    
    /**
     * Called once processing is done. 
     * Has the parent process the temp file already sent to it.
     * */
    public void finish(){
    	reader.clearTempFile();
    	super.finish();
    }
    
    public void oldFinish(){
    	super.finish();
    }
	
}
