/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
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
package de.innosystec.unrar.rarfile;

import java.io.IOException;

import de.innosystec.unrar.io.Raw;

/**
 * The main header of an rar archive. holds information concerning the whole archive (solid, encrypted etc).
 */
public class MainHeader extends BaseBlock {
	
	public static final short mainHeaderSizeWithEnc = 7;
	public static final short mainHeaderSize = 6;
	
	public MainHeader(BaseBlock bb, byte[] mainHeader) throws IOException{
		super(bb);
		int pos = 0;
		Raw.readShortLittleEndian(mainHeader, pos);
		pos += 2;
		Raw.readIntLittleEndian(mainHeader, pos);
	}
	
	public void print(){
		super.print();
        String str = "posav: \nhighposav: \nhasencversion: \nhasarchcmt: \nisEncrypted: \nisMultivolume: \nisFirstvolume: \nisSolid: \nisLocked: \nisProtected: \nisAV: "+isAV();
        System.out.println(str);
	}
	
	public boolean isAV(){
		return (this.flags&MHD_AV)!=0;
	}
	/**
	 * the numbering format a multivolume archive
	 */
	public boolean isNewNumbering(){
		return (this.flags&MHD_NEWNUMBERING)!=0;
	}
}
