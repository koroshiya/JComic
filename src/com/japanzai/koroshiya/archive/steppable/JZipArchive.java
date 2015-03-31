package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.graphics.Point;
import android.util.Log;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.thread.zip.IndexZipThread;
import com.japanzai.koroshiya.archive.steppable.thread.zip.NextZipThread;
import com.japanzai.koroshiya.archive.steppable.thread.zip.PreviousZipThread;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.interfaces.StepThread;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
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
		
		setPrimary(new NextZipThread(this, true));
		setSecondary(new PreviousZipThread(this, true));
		
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
			setMin(0);
		}else {
			throw new IOException();
		}
		
	}
	
	@Override
	public JBitmapDrawable parseImage(int i) throws IOException{

        JImage j = getImages().get(i);
        FileHeader zipEntry = (FileHeader)j.getImage();
        String name = j.getName();
		File f = new File(tempDir + "/" + name);
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}
		
		if (this.progressive){
			if (!f.exists()){
				if (!ArchiveParser.writeStreamToDisk(this.tempDir, zip.getInputStream(zipEntry), name)){
					return null;
				}
			}
			JBitmapDrawable temp = ImageParser.parseImageFromDisk(f, parent);
			if (temp == null){
				super.clear();
				temp = ImageParser.parseImageFromDisk(f, parent);
			}
			return temp;
		}else{
			InputStream is = null;
			try {
				is = zip.getInputStream(zipEntry);
				Point p = ImageParser.getImageSize(is);
				is = zip.getInputStream(zipEntry);
				
				JBitmapDrawable temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
				if (temp == null){
					super.clear();
					is = zip.getInputStream(zipEntry);
					temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
				}
				is.close();
				is = null;
				return temp;
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null){
					is.close();
				}
			}
		}
    	
    	return null;
    	
    }
    
	@Override
	public void addImageToCache(Object absoluteFilePath, String name) {
		
		JImage j = new JImage(absoluteFilePath, name);
		addImage(j);
		
	}

	@Override
	public boolean extractContentsToDisk(File pathToExtractTo, FileChooser fc) {

        String name;
        FileHeader zipEntry;

        try {
            for (Object o : zip.getFileHeaders()) {

                zipEntry = (FileHeader) o;
                name = zipEntry.getFileName();
                if (name.contains("/")){
                    name = name.substring(name.lastIndexOf('/') + 1);
                }

                if (fc != null && (thread == null || !thread.isAlive())){
                    thread = new SetTextThread(fc.getText(R.string.zip_extract_in_progress) + ": " + name, fc);
                    fc.runOnUiThread(thread);
                }

                if (ImageParser.isSupportedImage(name)){
                    addImageToCache(zipEntry, name);
                    try {
                        System.out.println("Extracting: " + name);
                        ArchiveParser.writeStreamToDisk(pathToExtractTo, zip.getInputStream(zipEntry), name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            return true;

        } catch (ZipException e) {
            e.printStackTrace();
            return false;
        }
		
	}

	@Override
	public ArrayList<JBitmapDrawable> extractContentsToArrayList() {
		
		ArrayList<JBitmapDrawable> imageList = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			
			try {
				imageList.add(parseImage(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return imageList;
		
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
	
	@Override
	public ZipFile getArchive() {
		
		return this.zip;
		
	}

	@Override
	public StepThread getNextThread(boolean primary) {
		return new NextZipThread(this, primary);
	}

	@Override
	public StepThread getPreviousThread(boolean primary) {
		return new PreviousZipThread(this, primary);
	}
	
	@Override
	public StepThread getImageThread(int index) {
		return new IndexZipThread(this, index);
	}
	
	public void close(){}
	
}
