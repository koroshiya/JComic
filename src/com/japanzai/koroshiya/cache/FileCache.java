package com.japanzai.koroshiya.cache;

import android.graphics.Point;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Cache for images read from disk directly rather than from an archive.
 * eg. A cache pointing to a specific directory holding images within in.
 * */
public class FileCache extends Steppable {
	
	public FileCache(Reader parent, String path){
		
		super(parent, path);
		
		setPrimary(new StepThread(this, true, true));
		setSecondary(new StepThread(this, false, false));
		
		setMax();
		setMin();
		
	}

	@Override
	public JBitmapDrawable parseImage(int findex) {
		
		try{
		
			InputStream is = new FileInputStream((String)getImages().get(findex).getImage());
			Point size = ImageParser.getImageSize(is);
			String name = (String)getImages().get(findex).getImage();
			is = new FileInputStream(name);
			
			JBitmapDrawable temp = ImageParser.parseImageFromDisk(is, size.x, size.y, parent);
			if (temp == null){
				super.clear();
				is = new FileInputStream((String)getImages().get(findex).getImage());
				temp = ImageParser.parseImageFromDisk(is, size.x, size.y, parent);
			}
			return temp;
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public StepThread getThread(boolean primary, boolean forward) {
		return new StepThread(this, primary, forward);
	}
	
	@Override
	public StepThread getImageThread(int index) {
		return new IndexThread(this, index);
	}
	
	public void close(){}
	
}
