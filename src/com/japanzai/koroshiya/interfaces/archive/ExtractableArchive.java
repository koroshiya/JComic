package com.japanzai.koroshiya.interfaces.archive;

import java.io.File;
import java.util.ArrayList;

import com.japanzai.koroshiya.filechooser.FileChooser;

/**
 * Represents an archive that can be extracted, but not necessarily
 * read from like a directory or have images extracted from it.
 * */
public interface ExtractableArchive {

	/**
	 * @param pathToExtractTo Where to extract the contents of this archive.
	 * @param parent 
	 * @return If the content can be extracted, true. Otherwise, false.
	 * */
	public boolean extractContentsToDisk(File pathToExtractTo, FileChooser parent);
	
	/**
	 * @return Returns the names of files within.
	 * */
	public ArrayList<String> peekAtContents();

}
