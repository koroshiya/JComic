package com.koroshiya.archive.steppable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.koroshiya.controls.JBitmapDrawable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public abstract class SteppableArchive {
	
	final File tempDir;
	final boolean progressive;
    final ArrayList<JImage> cache;
	
	SteppableArchive(Context c){

        this.tempDir = new File(c.getCacheDir(), "JComic");

        if (this.tempDir.exists()) {
            deleteFile(this.tempDir);
        }
        createTempDir();

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

    void addImageToCache(Object obj, String name){
        this.cache.add(new JImage(obj, name));
    }

    public int getTotalPages(){
        return cache.size();
    }

    void sort(){
        Collections.sort(this.cache);
    }

    @Nullable
    public abstract SoftReference<JBitmapDrawable> parseImage(int i, int width, boolean resize);

    public abstract void close();

    public abstract InputStream getStream(int i) throws IOException;

    void createTempDir(){

        if (!this.tempDir.mkdirs()){
            String msg = String.format(Locale.getDefault(), "Failed to create directory: %s", this.tempDir.getAbsolutePath());
            Log.d("SteppableArchive", msg);
        }

    }
	
}
