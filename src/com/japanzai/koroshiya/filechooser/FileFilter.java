package com.japanzai.koroshiya.filechooser;

import java.io.File;
import java.io.FilenameFilter;

import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;

/**
 * Purpose: Used to make sure the FileChooser class 
 * 			only displays directories and supported files.
 * */
public class FileFilter implements FilenameFilter {
		
	@Override
	public boolean accept(File dir, String filename) {
		
		return dir.isDirectory() || acceptedExtension(dir);
		
	}
	
	/**
	 * @param f File to check if accepted
	 * @return Returns true if file is supported, otherwise false.
	 * */
	public boolean acceptedExtension(File f){
		
    	return ArchiveParser.isSupportedArchive(f) || ImageParser.isSupportedImage(f);
		
	}

}
