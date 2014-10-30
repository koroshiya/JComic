package com.japanzai.koroshiya.cache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.graphics.Point;

import com.japanzai.koroshiya.archive.steppable.JImage;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.interfaces.StepThread;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Cache for images read from disk directly rather than from an archive.
 * eg. A cache pointing to a specific directory holding images within in.
 * */
public class FileCache extends Steppable {
	
	public FileCache(Reader parent, String path){
		
		super(parent, path);
		
		setPrimary(new PrimaryThread(this, true, parent));
		setSecondary(new SecondaryThread(this, false, parent));
		
		setMax(0);
		setMin(0);
		
	}
	
	@Override
	public void addImageToCache(Object absoluteFilePath, String name){
		
		JImage j = new JImage(absoluteFilePath, name);
		addImage(j);
		
	}
		
	@Override
	public void parseCurrent()  {
    	
		getParent().setImage(parseImage(getIndex()));
		
    	SettingsManager settings = super.parent.getSettings();
    	if (settings.isCacheOnStart()){
    		super.cache(settings.getCacheModeIndex() != 2);
    	}
		
	}

	@Override
	public JBitmapDrawable parseImage(int findex) {
		
		try{
		
			InputStream is = new FileInputStream((String)getImages().get(findex).getImage());
			Point size = ImageParser.getImageSize(is);
			String name = (String)getImages().get(findex).getImage();
			is = new FileInputStream(name);
			
			JBitmapDrawable temp = ImageParser.parseImageFromDisk(is, size.x, size.y, name, parent);
			if (temp == null){
				super.clear();
				is = new FileInputStream((String)getImages().get(findex).getImage());
				temp = ImageParser.parseImageFromDisk(is, size.x, size.y, name, parent);
			}
			return temp;
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public StepThread getNextThread(boolean primary) {
		return primary ? new PrimaryThread(this, true, parent) : new SecondaryThread(this, true, parent);
	}

	@Override
	public StepThread getPreviousThread(boolean primary) {
		return primary ? new PrimaryThread(this, false, parent) : new SecondaryThread(this, false, parent);
	}
	
	@Override
	public StepThread getImageThread(int index) {
		return new IndexThread(this, index, parent);
	}
	
	public void close(){
		return;
	}
	
}
