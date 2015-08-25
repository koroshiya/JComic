package com.japanzai.koroshiya.cache;

import android.graphics.Point;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Cache for images read from disk directly rather than from an archive.
 * eg. A cache pointing to a specific directory holding images within in.
 * */
public class FileCache extends Steppable {

    private final int width;
    private final int resize;
	
	public FileCache(Reader parent, String path){
		
		super(path);

		width = parent.getWidth();
		resize = parent.getSettings().getDynamicResizing();
		
		setPrimary(new StepThread(this, true, true, width, resize));
		setSecondary(new StepThread(this, false, false, width, resize));
		
		setMax();
		
	}

	@Override
	public SoftReference<JBitmapDrawable> parseImage(int findex, int width, int resize) {

        JBitmapDrawable temp = null;

		try{
		
			InputStream is = new FileInputStream((String)getImages().get(findex).getImage());
			Point size = ImageParser.getImageSize(is);

			String name = (String)getImages().get(findex).getImage();
			is = new FileInputStream(name);
			temp = ImageParser.parseImageFromDisk(is, size.x, size.y, width, resize);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new SoftReference<>(temp);
	}

	@Override
	public StepThread getThread(boolean primary, boolean forward) {
		return new StepThread(this, primary, forward, width, resize);
	}
	
	public void close(){}
	
}
