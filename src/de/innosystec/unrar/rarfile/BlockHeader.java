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
 * Base class of headers that contain data
 */
public class BlockHeader extends BaseBlock{
	public static final short blockHeaderSize = 4;
	
	private int dataSize;
	private int packSize;
    
    public BlockHeader(BlockHeader bh) throws IOException{
    	super(bh);
    	this.packSize = bh.getDataSize();
    	this.dataSize = packSize;
    	this.positionInFile = bh.getPositionInFile();
    }
    
    public BlockHeader(BaseBlock bb, byte[] blockHeader) throws IOException{
    	super(bb);
    	
    	this.packSize = Raw.readIntLittleEndian(blockHeader, 0);
    	this.dataSize  = this.packSize;
    }
    
	public int getDataSize() {
		return dataSize;
	}
	
	public int getPackSize() {
		return packSize;
	}

}
