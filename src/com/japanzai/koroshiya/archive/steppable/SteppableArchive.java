package com.japanzai.koroshiya.archive.steppable;

import android.content.Context;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.settings.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;

public abstract class SteppableArchive {
	
	protected final File tempDir;
	protected final boolean progressive;
    protected final ArrayList<JImage> cache;
	
	public SteppableArchive(Context c){

        this.tempDir = new File(c.getCacheDir() + "/JComic/");

        if (this.tempDir.exists() && this.tempDir.isDirectory()) {
            deleteFile(this.tempDir);
        }
        this.tempDir.mkdirs();

        this.progressive = this instanceof JRarArchive;

        cache = new ArrayList<>();
		
	}
	
	private boolean deleteFile(File file){
		
		boolean completeSuccess = true;
		
		if (file.isDirectory()){
			for (File f : file.listFiles()){
				if (!deleteFile(f)){
					completeSuccess = false;
				}
			}
			if (!file.delete()){
				completeSuccess = false;
			}
		}else {
			if (!file.delete()){
				completeSuccess = false;
			}
		}
		
		return completeSuccess;
		
	}

    protected void addImageToCache(Object obj, String name){
        this.cache.add(new JImage(obj, name));
    }

    public int getTotalPages(){
        return cache.size();
    }

    public void sort(){
        Collections.sort(this.cache);
    }

    /**
     * @return Returns the names of files within.
     * */
    public abstract ArrayList<String> peekAtContents();

    public abstract SoftReference<JBitmapDrawable> parseImage(int i, int width, int resizeMode);

    public abstract void close();

    public abstract InputStream getStream(int i) throws IOException;
	
}
