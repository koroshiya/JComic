package com.japanzai.koroshiya.archive.steppable;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

		FileHeader header;
		rar = new Archive(new File(path));
		List<FileHeader> heads = rar.getFileHeaders();
		
		for (int i = 0; i < heads.size(); i++){
			
			header = heads.get(i);
			if (ImageParser.isSupportedImage(header.getFileNameString())){
				if (header.getUnpSize() > 0) addImageToCache(i, header.getFileNameString());
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

		FileHeader entry = (FileHeader)getEntry(i);
		String name = entry.getFileNameString();
		name = name.substring(name.lastIndexOf('\\') + 1);
		File f = new File(tempDir + "/" + name);
		InputStream is = null;
		Point p;
		JBitmapDrawable temp = null;
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}
		
		if (this.progressive){
			if (!f.exists()){
				extractFileToDisk(i, tempDir, null);
			}

			p = ImageParser.getImageSize(f);
			is = new FileInputStream(f);
			
			temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
			if (temp == null){
				super.clear();
				is = new FileInputStream(f);
				temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
			}
		}else{
			
			try {

				is = rar.getInputStream(entry);
				p = ImageParser.getImageSize(is);
				is = rar.getInputStream(entry);
				
				temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
				if (temp == null){
					super.clear();
					is = rar.getInputStream(entry);
					temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
				}
			} catch (IOException | RarException e) {
				e.printStackTrace();
			}
		}
		
		if (is != null){
			is.close();
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
			} catch (RarException | IOException e) {
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

		ArrayList<JBitmapDrawable> imageList = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			
			try {
				imageList.add(parseImage(i));
			} catch (IOException e) {
				e.printStackTrace();
				return imageList;
			}
			
		}

		return imageList;
		
	}

	@Override
	public ArrayList<String> peekAtContents() {
		
		ArrayList<String> names = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			names.add(((FileHeader)getEntry(i)).getFileNameString());
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
