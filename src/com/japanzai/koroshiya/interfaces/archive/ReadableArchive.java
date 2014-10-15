package com.japanzai.koroshiya.interfaces.archive;

import java.util.ArrayList;

import com.japanzai.koroshiya.controls.JBitmapDrawable;

/**
 * Purpose: Represents an archive that can be read through sequentially,
 * 			but not necessarily read randomly.
 * 			Should be implemented directly by non-steppable archives,
 * 			indirectly from steppable archives.
 * */
public interface ReadableArchive extends ExtractableArchive{
	
	/**
	 * @return Returns the contents of the archive as displayable objects.
	 * */
	public ArrayList<JBitmapDrawable> extractContentsToArrayList();
	
}
