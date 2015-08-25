package com.japanzai.koroshiya.reader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.cache.FileCache;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Parses directories, image files and archives.
 * 			Essentially initializes the main activity's cache.
 * */
public class Progress extends AsyncTask{
	
	private final File f;
	private final int index;
    private final Reader reader;
    private boolean success = true;

	public Progress(Reader reader, File f, int index){
		this.reader = reader;
        this.f = f;
        this.index = index;
	}

	@Override
	protected Object doInBackground(Object[] params) {

        if (f.isDirectory()) {
            reader.setCache(new FileCache(reader, f.getAbsolutePath()));
            parseDir(f, 0);
            reader.setCacheIndex(index == -1 ? 0 : index);
        }else if (ArchiveParser.isSupportedArchive(f)){
            if (!parseArchive(reader)){
                reader.clearTempFile(reader);
                success = false;
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
        if (reader.getCache().getMax() == 0){
            success = false;
        }
        return success;
	}

    @Override
    protected void onPostExecute(Object result) {
        reader.findViewById(R.id.progress).setVisibility(View.GONE);
        if (success){
            reader.clearTempFile(reader);
        }else{
            new MessageThread(R.string.no_images, reader).start();
        }
    }
    
    /**
     * @param file File to be tested. If supported, the file is processed.
     * */
    public void parseFile(File file){
    	
    	if (ImageParser.isSupportedImage(file)){
    		if (file.length() > 0) reader.addImageToCache(file.getAbsolutePath(), file.getAbsolutePath());
    	}else{
    		Log.d("Progress", reader.getString(R.string.unsupported_file) + file.getName());
    	}
    	
    }
    
    /**
     * @param dir Directory to process the contents of
     * */
    public void parseDir(File dir, int curLevel){
    	SettingsManager settings = MainActivity.getMainActivity().getSettings();
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

    public boolean parseArchive(Reader reader){
    	    	
    	try {

            SteppableArchive temp = ArchiveParser.parseArchive(f, reader);
    		
    		if (temp == null){
    			reader.runOnUiThread(new MessageThread(reader.getString(R.string.archive_read_error), reader));
    			return true;
    		}

            reader.setCache(temp);
        	
    		reader.setCacheIndex(this.index == -1 ? 0 : this.index);
    		
		} catch (IOException e) {
            reader.runOnUiThread(new MessageThread(reader.getString(R.string.archive_read_error), reader));
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    	    	
    }
	
}
