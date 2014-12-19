package com.japanzai.koroshiya.io_utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;

import com.japanzai.koroshiya.archive.non_steppable.JArArchive;
import com.japanzai.koroshiya.archive.non_steppable.JTarArchive;
import com.japanzai.koroshiya.archive.steppable.JRarArchive;
import com.japanzai.koroshiya.archive.steppable.JZipArchive;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.interfaces.archive.ReadableArchive;
import com.japanzai.koroshiya.reader.Reader;

import de.innosystec.unrar.exception.RarException;

/**
 * Purpose: Used for testing archives and methods by which to implement them.
 * */
public class ArchiveParser {
		
	private ArchiveParser(){
		//We don't want this class to be instantiated
	}
	
	private static final String[] supportedArchives = {".zip", ".cbz", ".rar", ".cbr", ".tar", ".ar"}; //TODO: implement .tar.gz and other formats
	public static final byte[] BUFFER = new byte[8192];
	public static final int BUFFER_SIZE = 8192;
	
    /**
     * Purpose: Checks to see if archive is of a supported format
     * @param f File to parse
     * @return If the file is supported, returns true. Otherwise, false.
     * */
	public static boolean isSupportedArchive(File f){
    	return isSupported(f.getName(), supportedArchives);
    }
	public static boolean isSupportedZipArchive(String s){
    	return isSupported(s, new String[]{".zip", ".cbz"});
    }
	public static boolean isSupportedRarArchive(String s){
    	return isSupported(s, new String[]{".rar", ".cbr"});
    }
	public static boolean isSupportedMiscArchive(String s){
    	return isSupported(s, new String[]{".tar", ".ar"});
    }
	
	public static boolean isSupported(String name, String[] exts){
		
		name = name.toLowerCase(Locale.getDefault());
    	
    	for (String ext : exts){
    		if (name.endsWith(ext)) return true;
    	}
    	
    	return false;
    	
    }
    
    /**
     * Purpose: Validates an archive
     * @param f File to parse
     * @return Returns an instantiated JArchive object after validation
     * @throws IOException if the archive couldn't be read
     * @throws RarException if a RAR archive couldn't be read
     * */
    public static ReadableArchive parseArchive(File f, Reader r) throws IOException, RarException, ZipException{
    	
    	String s = f.getName().toLowerCase(Locale.getDefault());
    	String fPath = f.getAbsolutePath();
    	
    	if (s.endsWith(".zip") || s.endsWith(".cbz")){
    		return new JZipArchive(fPath, r);
    	}else if (s.endsWith(".rar") || s.endsWith(".cbr")){ //hybrid disk mode
    		JRarArchive arch = new JRarArchive(fPath, r);
    		if (arch.getArchive().isPasswordProtected()){
    			throw new RarException(new Exception("RAR is password protected"));
    		}else{
    			return arch;
    		}
    	}else if (s.endsWith(".ar")){
    		return new JArArchive(f, r);
    	}else if (s.endsWith(".tar")){
    		return new JTarArchive(f, r);
    	}else {
        	return null; //TODO: implement .tar.gz and other formats
    	}    	
    	
    }
    
    /**
     * Purpose: Extracts supported archive contents to disk
     * @param archive Archive to extract the contents of
     * @param pathToExtractTo Path to extract the contents of an archive to
     * @return True is extract was successful, otherwise false.
     * */
    public static boolean extractArchiveToDisk(ReadableArchive archive, File pathToExtractTo, FileChooser parent){
    	
    	return archive.extractContentsToDisk(pathToExtractTo, parent);
    	
    }
    
    /**
     * Purpose: Used for extracting supported contents of an archive to cache
     * @param archive Archive to extract
     * @return Return supported archive contents as an ArrayList of JBitmapDrawable objects
     * */
    public static ArrayList<JBitmapDrawable> extractArchiveToRam(ReadableArchive archive){
    	
    	return archive.extractContentsToArrayList();
    	
    }
    
    /**
     * @param archive Archive to view the contents of
     * @param recursive Indicates whether to search recursively or not
     * @return Returns an ArrayList containing the names of the supported files in the archive
     * */
    public static ArrayList<String> peekAtContents(ReadableArchive archive, boolean recursive){
    	
    	return archive.peekAtContents();
    	
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
    
	/**
	 * @param stream Stream containing the file to extract
	 * @param entry Archive entry that will be extracted. 
	 * 				Used to check for compatibility and the size of the write.
	 * @throws IOException if entry couldn't be extracted
	 * @return Returns a byte array containing the archive entry
	 * */
    public static byte[] parseEntry(ArchiveInputStream stream, ArchiveEntry entry) throws IOException{
		
		if (ImageParser.isSupportedImage(entry.getName())){
				
			byte[] content = new byte[(int) entry.getSize()];
			
			do {
				stream.read(content, 0, content.length);
			}while (stream.getBytesRead() == entry.getSize());
			
			stream.close();
			return content;
		}
		
		return null;
		
    }
    
}
