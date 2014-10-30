package com.japanzai.koroshiya.archive.non_steppable;

import java.io.File;

import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Represents a Tar archive.
 * */
public class JTarArchive extends ArWrapper {
	
	public JTarArchive(File archive, Reader parent){
		super(archive, parent);
	}

}
