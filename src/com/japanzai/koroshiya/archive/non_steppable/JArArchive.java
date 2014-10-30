package com.japanzai.koroshiya.archive.non_steppable;

import java.io.File;

import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Represents an Ar archive.
 * */
public class JArArchive extends ArWrapper {
	
	public JArArchive(File archive, Reader parent){
		super(archive, parent);
	}

}
