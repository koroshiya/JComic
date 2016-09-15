/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 14.06.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 * 
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package de.innosystec.unrar.unpack.ppm;

public abstract class Pointer{

	protected final byte[] mem;
	protected int pos;

	/**
	 * Initialize the object with the array (may be null)
	 * @param mem the byte array
	 */
	public Pointer(byte[] mem){
		this.mem = mem;
        this.pos = 0;
	}

	/**
	 * returns the position of this object in the byte[]
	 * @return the address of this object
	 */
	public int getAddress(){
		return pos;
	}

	/**
	 * needs to set the fields of this object to the values in the byte[]
	 * at the given position.
	 * be aware of the byte order
	 * @param pos the position this object should point to
	 */
	public void setAddress(int pos) {
        if (pos >= 0 && pos < mem.length) this.pos = pos;
    }
}
