package com.japanzai.koroshiya.archive.extract_only;

import java.io.File;

import com.japanzai.koroshiya.interfaces.archive.PseudoArchive;

/**
 * Purpose: Abstract class to be extended by threads 
 * 			for parsing archives containing within them
 * 			tar archives.
 * 			These include, but are not limited to, gzip and bzip2 archives.
 * 			Such archives can be extracted to disk, but current android devices
 * 			don't have enough memory to extract the archive's contents
 * 			directly into memory, and extracting from the inner tar archive
 * 			would be very slow.
 * */
public abstract class TarWrapper implements PseudoArchive {
	
	private final File archive;
	private final String[] supportedArchives = {".tar.gz", ".tar.bz2", ".tar.xz"};
	
	public TarWrapper(File file){
		this.archive = file;
	}
	
	/**
	 * Purpose: Checks if the file encompassed really is a tar archive
	 * @return If the file encompasses a tar archive, returns true. Otherwise, false.
	 * */
	public boolean isTarArchive(){
		
		String name = this.archive.getName();
		
		for (String extension : supportedArchives){
			if (name.endsWith(extension)){
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * @return Returns the archive that's yet to be extracted
	 * */
	public File getArchive(){
		return this.archive;
	}
	
}
