package com.japanzai.koroshiya.interfaces;

/**
 * Purpose: Provides methods that file-caching classes should implement
 * */
public interface Cacheable {
	
	/**
	 * Purpose: Caches an image and/or its filepath
	 * @param absoluteFilePath Absolute path of the image to be cached.
	 * 			May also be a reference. eg. An archive FileHeader
	 * @param name String name of file. Used for sorting.
	 * */
	public void addImageToCache(Object absoluteFile, String name);
	
	/**
	 * Purpose: Empties the cache
	 * */
	public void emptyCache();
	
}
