package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.graphics.Point;
import android.util.Log;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.cache.StepThread;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.reader.ToastThread;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

/**
 * Purpose: Represents a standard Zip archive.
 * 			Also used for cbz archives.
 * 			Could be used for Jar archives, if implemented later.
 * */
public class JZipArchive extends SteppableArchive{
	
	private final ZipFile zip;

	public JZipArchive(String path, Reader parent) throws IOException {
		
		super(parent, path);
		
		setPrimary(new StepThread(this, true, true));
		setSecondary(new StepThread(this, true, false));
		
		zip = new ZipFile(path);

        if (!zip.isValidZipFile()) throw new IOException();

        String name;
        FileHeader zipEntry;

        for (Object o : zip.getFileHeaders()) {

            zipEntry = (FileHeader) o;
            name = zipEntry.getFileName();

            if (ImageParser.isSupportedImage(name) && zipEntry.getCompressedSize() > 0) {
                addImageToCache(zipEntry, name);
            }

        }

        super.sort();
		
		if (getMax() > 0){
			setIndex(0);
			setMin();
		}else {
			throw new IOException();
		}
		
	}
	
	@Override
	public JBitmapDrawable parseImage(int i){

        JImage j = getImages().get(i);
        FileHeader zipEntry = (FileHeader)j.getImage();
        String name = j.getName();
		File f = new File(tempDir + "/" + name);
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}

        JBitmapDrawable temp = null;

        try {

            if (this.progressive) {
                if (f.exists() || ArchiveParser.writeStreamToDisk(this.tempDir, zip.getInputStream(zipEntry), name)) {
                    temp = ImageParser.parseImageFromDisk(f, parent);
                    if (temp == null) {
                        super.clear();
                        temp = ImageParser.parseImageFromDisk(f, parent);
                    }
                }
            } else {
                InputStream is = null;
                try {
                    is = zip.getInputStream(zipEntry);
                    Point p = ImageParser.getImageSize(is);
                    is = zip.getInputStream(zipEntry);

                    temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
                    if (temp == null) {
                        super.clear();
                        is = zip.getInputStream(zipEntry);
                        temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
                    }
                    is.close();
                    is = null;

                } catch (IOException e) {
                    e.printStackTrace();
                    new ToastThread(R.string.archive_read_error, parent).start();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }

        } catch (IOException e) {
            return null;
        }
    	
    	return temp;
    	
    }

	@Override
	public ArrayList<String> peekAtContents() {

		ArrayList<String> names = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			String name = getImages().get(i).getName();
			names.add(name);
            Log.d("JZipArchive", "Zip entry " + i + ": " + name);
        }
		
		return names;
		
	}
	
	public void close(){}
	
}
