package com.japanzai.koroshiya.io_utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.japanzai.koroshiya.archive.steppable.JRarArchive;
import com.japanzai.koroshiya.archive.steppable.JZipArchive;
import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Used for testing archives and methods by which to implement them.
 * */
public abstract class ArchiveParser {

	public static final byte[] BUFFER = new byte[8192];
	
    /**
     * Purpose: Checks to see if archive is of a supported format
     * @param f File to parse
     * @return If the file is supported, returns true. Otherwise, false.
     * */
	public static boolean isSupportedArchive(File f){
        String s = f.getName();
    	return (isSupportedZipArchive(s) || isSupportedRarArchive(s)) && f.length() > 0;
    }
	public static boolean isSupportedZipArchive(String s){
    	return isSupported(s, new String[]{".zip", ".cbz"});
    }
	public static boolean isSupportedRarArchive(String s){
    	return isSupported(s, new String[]{".rar", ".cbr"});
    }
	
	public static boolean isSupported(String name, String[] exts){
		
		name = name.toLowerCase(Locale.getDefault());
    	for (String ext : exts) if (name.endsWith(ext)) return true;
    	return false;
    	
    }
    
    /**
     * Purpose: Validates an archive
     * @param f File to parse
     * @return Returns an instantiated JArchive object after validation, or null
     * @throws IOException if the archive couldn't be read
     * */
    public static SteppableArchive parseArchive(File f, Reader r) throws IOException {
    	
    	String s = f.getName().toLowerCase(Locale.getDefault());
    	String fPath = f.getAbsolutePath();
    	
    	if (s.endsWith(".zip") || s.endsWith(".cbz")){
            return new JZipArchive(r, fPath);
    	}else if (s.endsWith(".rar") || s.endsWith(".cbr")){ //hybrid disk mode
    		JRarArchive arch = new JRarArchive(fPath, r);
    		if (arch.getArchive().isPasswordProtected()){
    			throw new IOException("RAR is password protected");
    		}else{
    			return arch;
    		}
    	}else {
        	return null;
    	}    	
    	
    }

    /**
     * @param pathToExtractTo Path to extract the InputStream's contents to
     * @param inFile InputStream to extract the contents of
     * @param fileName Name of the file to write
     * @return If the writing is successful, returns true. Otherwise, false.
     * */
    public static boolean writeStreamToDisk(File pathToExtractTo, InputStream inFile, String fileName){
		
		FileOutputStream fos;
		BufferedOutputStream writtenFile;
	    int l;
			
		try {
							
			fos = new FileOutputStream(pathToExtractTo + File.separator + fileName);
			writtenFile = new BufferedOutputStream(fos);
				
			while ((l = inFile.read(BUFFER)) >= 0) {
				writtenFile.write(BUFFER, 0, l);
			}
			writtenFile.close();
				
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
    }
    
}
