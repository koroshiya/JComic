package com.koroshiya.io_utils;

import android.content.Context;

import com.koroshiya.archive.steppable.JRarArchive;
import com.koroshiya.archive.steppable.JZipArchive;
import com.koroshiya.archive.steppable.SteppableArchive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Purpose: Used for testing archives and methods by which to implement them.
 * */
public abstract class ArchiveParser {

	private static final byte[] BUFFER = new byte[8192];
    private static final String[] supportedArchives = {".zip", ".cbz", ".rar", ".cbr"};

    public static boolean isSupportedArchive(File f){
        return f.isFile() && isSupported(f, supportedArchives) && f.length() > 0;
    }

    public static boolean isSupportedRarArchive(File f){
    	return isSupported(f, new String[]{".rar", ".cbr"});
    }
	
	private static boolean isSupported(File f, String[] exts){
		
		String name = f.getName().toLowerCase(Locale.getDefault());
    	for (String ext : exts) if (name.endsWith(ext)) return true;
    	return false;
    	
    }
    
    /**
     * Purpose: Validates an archive
     * @param f File to parse
     * @return Returns an instantiated JArchive object after validation, or null
     * @throws IOException if the archive couldn't be read
     * */
    public static SteppableArchive parseArchive(File f, Context c) throws IOException {
    	
    	String s = f.getName().toLowerCase(Locale.getDefault());
    	String fPath = f.getAbsolutePath();
    	
    	if (s.endsWith(".zip") || s.endsWith(".cbz")){
            return new JZipArchive(fPath, c);
    	}else if (s.endsWith(".rar") || s.endsWith(".cbr")){ //hybrid disk mode
    		JRarArchive arch = new JRarArchive(fPath, c);
    		if (arch.getArchive().isPasswordProtected(c.getCacheDir())){
    			throw new IOException("RAR is password protected");
    		}else{
    			return arch;
    		}
    	}else {
        	throw new IOException("Unsupported archive");
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
