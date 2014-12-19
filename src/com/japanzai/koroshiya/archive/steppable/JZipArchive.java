package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import android.graphics.Point;

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

/**
 * Purpose: Represents a standard Zip archive.
 * 			Also used for cbz archives.
 * 			Could be used for Jar archives, if implemented later.
 * */
public class JZipArchive extends SteppableArchive{
	
	private final ZipFile zip;

	public JZipArchive(String path, Reader parent) throws IOException, ZipException {
		
		super(parent, path);
		
		setPrimary(new NextZipThread(this, true));
		setSecondary(new PreviousZipThread(this, true));
		
		zip = new ZipFile(path);
		ZipArchiveEntry zipEntry;
		Enumeration<ZipArchiveEntry> zips = zip.getEntries();
		
		while (zips.hasMoreElements()){
			
			zipEntry = zips.nextElement();
			
			if (ImageParser.isSupportedImage(zipEntry.getName())){
				if (zipEntry.getSize() > 0) addImageToCache(zipEntry, zipEntry.getName());
			}
			
		}
		
		if (getMax() > 0){
			setIndex(0);
			setMin(0);
		}else {
			throw new IOException();
		}
		
	}
	
	@Override
	public JBitmapDrawable parseImage(int i) throws IOException{

		ZipArchiveEntry entry = (ZipArchiveEntry)getImages().get(i).getImage();
		File f = new File(tempDir + "/" + entry.getName());
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}
		
		if (this.progressive){
			if (!f.exists()){
				if (!ArchiveParser.writeStreamToDisk(this.tempDir, zip.getInputStream(entry), entry.getName())){
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
				is = zip.getInputStream(entry);
				Point p = ImageParser.getImageSize(is);
				is = zip.getInputStream(entry);
				
				JBitmapDrawable temp = ImageParser.parseImageFromDisk(is, p.x, p.y, entry.getName(), parent);
				if (temp == null){
					super.clear();
					is = zip.getInputStream(entry);
					temp = ImageParser.parseImageFromDisk(is, p.x, p.y, entry.getName(), parent);
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
	    
	    Enumeration<ZipArchiveEntry> zips = zip.getEntries();
		ZipArchiveEntry zipEntry;
		String name;
		while (zips.hasMoreElements()){
			
			zipEntry = zips.nextElement();
			name = zipEntry.getName();
			if (name.contains("/")){
				name = name.substring(name.lastIndexOf('/') + 1);
			}
			
			if (fc != null && (thread == null || !thread.isAlive())){
				thread = new SetTextThread(fc.getText(R.string.zip_extract_in_progress) + ": " + name, fc);
				fc.runOnUiThread(thread);
			}
			
			if (ImageParser.isSupportedImage(zipEntry.getName())){
				addImageToCache(zipEntry, zipEntry.getName());
				try {
					System.out.println("Extracting: " + zipEntry.getName());
					ArchiveParser.writeStreamToDisk(pathToExtractTo, zip.getInputStream(zipEntry), name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return true;
		
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
		
		ZipArchiveEntry entry;
		ArrayList<String> names = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			
			entry = (ZipArchiveEntry)getImages().get(i).getImage();
			names.add(entry.getName());
			
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
	
	@Override
	public Object getEntry(int i){
		return zip.getEntry((String) getImages().get(i).getImage());
	}
	
	public void close(){
		try {
			zip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
