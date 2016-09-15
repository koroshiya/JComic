package com.koroshiya.archive.steppable;

import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.Locale;

/**
 * Represents an image file, NOT a Drawable.
 * This class may hold an actual File object, a String reference, etc.
 * The type of object held depends on the Steppable in use.
 * */
public class JImage implements Comparable<JImage>{
	
	private final String name;
	private final Object image;
	private final static Collator myCollator = Collator.getInstance(Locale.getDefault());
	
	public JImage(Object image, String name){
		
		this.image = image;
		this.name = name;
		
	}
	
	/**
	 * @return Name of the image held
	 * */
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return Image being held. 
	 * The Object returned may be a String reference, a File object, etc.
	 * */
	public Object getImage(){
		return this.image;
	}

	@Override
	public int compareTo(@NonNull JImage another) {
		return myCollator.compare(this.name, another.getName());
	}
	
}
