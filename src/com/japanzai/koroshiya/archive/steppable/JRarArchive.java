package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.thread.rar.IndexRarThread;
import com.japanzai.koroshiya.archive.steppable.thread.rar.NextRarThread;
import com.japanzai.koroshiya.archive.steppable.thread.rar.PreviousRarThread;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.interfaces.StepThread;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * Purpose: Represents a rar archive.
 * 			Provides methods for manipulating such an archive.
 * */
public class JRarArchive extends SteppableArchive{

	private final Archive rar;
	
	public JRarArchive(String path, Reader parent) throws IOException, RarException {
		
		super(parent, path);
		
		setPrimary(new NextRarThread(this, true));
		//setSecondary(new PreviousRarThread(this, true));

		FileHeader header = null;
		rar = new Archive(new File(path));
		List<FileHeader> heads = rar.getFileHeaders();
		
		for (int i = 0; i < heads.size(); i++){
			
			header = heads.get(i);
			if (ImageParser.isSupportedImage(header.getFileNameString())){
				if (header.getUnpSize() > 0) addImageToCache(i, header.getFileNameString());
			}
			
		}
		
		if (getMax() > 0){
			setIndex(0);
			setMin(0);
		}else {
			IOException ioe = new IOException();
			throw ioe;
		}
		
	}
	
	@Override
	public JBitmapDrawable parseImage(int fIndex) throws IOException{

		int i = (Integer)fIndex;
		FileHeader entry = (FileHeader)getEntry(i);
		String name = entry.getFileNameString();
		name = name.substring(name.lastIndexOf('\\') + 1);
		File f = new File(tempDir + "/" + name);
		InputStream is = null;
		Point p = null;
		JBitmapDrawable temp = null;
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}
		
		if (this.progressive){
			if (!f.exists()){
				extractFileToDisk(fIndex, tempDir, null);
			}

			is = new FileInputStream(f);
			p = ImageParser.getImageSize(is);
			is = new FileInputStream(f);
			
			temp = ImageParser.parseImageFromDisk(is, p.x, p.y, entry.getFileNameString(), parent);
			if (temp == null){
				super.clear();
				is = new FileInputStream(f);
				temp = ImageParser.parseImageFromDisk(is, p.x, p.y, entry.getFileNameString(), parent);
			}
		}else{
			
			try {
				
				is = rar.getInputStream(entry);
				p = ImageParser.getImageSize(is);
				is = rar.getInputStream(entry);
				
				temp = ImageParser.parseImageFromDisk(is, p.x, p.y, entry.getFileNameString(), parent);
				if (temp == null){
					super.clear();
					is = rar.getInputStream(entry);
					temp = ImageParser.parseImageFromDisk(is, p.x, p.y, entry.getFileNameString(), parent);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RarException e) {
				e.printStackTrace();
			} finally {
				if (is != null){
					is.close();
				}
			}
		}
		
		if (is != null){
			is.close();
			is = null;
		}
		return temp;
    	
    }
		
	private void extractFileToDisk(int i, File pathToExtractTo, FileChooser fc){
		
		FileHeader header = (FileHeader) getEntry(i);
		String name = header.getFileNameString();
		name = name.substring(name.lastIndexOf('\\') + 1);
		
		if (fc != null && (thread == null || !thread.isAlive())){
			thread = new SetTextThread(fc.getText(R.string.zip_extract_in_progress) + ": " + name, fc);
			fc.runOnUiThread(thread);
		}
		
		if (ImageParser.isSupportedImage(name)){
			try {
				ArchiveParser.writeStreamToDisk(pathToExtractTo, rar.getInputStream(header), name);
			} catch (RarException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("unsupported");
		}
		
	}
    
	@Override
	public void addImageToCache(Object absoluteFilePath, String name) {
		
		JImage j = new JImage(absoluteFilePath, name);
		addImage(j);
		
	}
	
	@Override
	public boolean extractContentsToDisk(File pathToExtractTo, FileChooser fc) {

		for (int i = 0; i < getImages().size(); i++){
			extractFileToDisk(i, pathToExtractTo, fc);
		}
		
		return true;
		
	}

	@Override
	public ArrayList<JBitmapDrawable> extractContentsToArrayList() {

		InputStream is = null;
		ArrayList<JBitmapDrawable> imageList = new ArrayList<JBitmapDrawable>();
		
		for (int i = 0; i < getMax(); i++){
			
			try {
				imageList.add(parseImage(i));
			} catch (IOException e) {
				e.printStackTrace();
				return imageList;
			} finally {
				if (is != null){
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}

		return imageList;
		
	}

	@Override
	public ArrayList<String> peekAtContents() {
		
		ArrayList<String> names = new ArrayList<String>();
		FileHeader header = null;
		
		for (int i = 0; i < getMax(); i++){
			
			header = (FileHeader)getEntry(i);
			names.add(header.getFileNameString());
			
		}
		
		return names;
		
	}

	@Override
	public Archive getArchive() {
		return this.rar;
	}
	
	@Override
	public StepThread getNextThread(boolean primary) {
		return new NextRarThread(this, primary);
	}

	@Override
	public StepThread getPreviousThread(boolean primary) {
		return new PreviousRarThread(this, primary);
	}
	
	@Override
	public StepThread getImageThread(int index) {
		return new IndexRarThread(this, index);
	}

	@Override
	public Object getEntry(int i){
		if (i < getImages().size()){
			return rar.getFileHeaders().get((Integer)getImages().get(i).getImage());
		}
		return null;
	}
	
	public void close(){
		try {
			rar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
