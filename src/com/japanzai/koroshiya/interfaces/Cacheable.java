package com.japanzai.koroshiya.interfaces;

/**
 * Purpose: Provides methods that file-caching classes should implement
 * */
public interface Cacheable {
	
	/**
	 * Purpose: Caches an image and/or its filepath
	 * @param name String name of file. Used for sorting.
	 * */
	public void addImageToCache(Object absoluteFile, String name);

	
}
