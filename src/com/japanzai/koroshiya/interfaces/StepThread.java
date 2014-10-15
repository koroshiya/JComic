package com.japanzai.koroshiya.interfaces;

import com.japanzai.koroshiya.controls.JBitmapDrawable;

/**
 * Interface that any Thread designed for parsing a Steppable should implement.
 * */
public interface StepThread extends Runnable {

	/**
     * @return Retrieves the next displayable image from the Runnable object
     * */
	public JBitmapDrawable nextBitmap();
    
	/**
     * @return Retrieves the previous displayable image from the Runnable object
     * */
	public JBitmapDrawable previousBitmap();

	/**
     * @return Retrieves the displayable image from the index specified
     * */
	public abstract JBitmapDrawable parseImage(int fIndex);
	
}
