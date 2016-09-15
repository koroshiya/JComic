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
 * Base class of all rar headers
 */
public class BaseBlock{
	
	public static final short BaseBlockSize = 7;

    public static final short MHD_NEWNUMBERING = 0x0010;
	public static final short MHD_AV = 0x0020;
	public static final short MHD_PASSWORD = 0x0080;
    public static final short MHD_ENCRYPTVER = 0x0200;


    public static final short LHD_SPLIT_AFTER  =  0x0002;
    public static final short LHD_SOLID        =  0x0010;

    public static final short LHD_LARGE        =  0x0100;
	public static final short LHD_UNICODE      =  0x0200;

    public static final short EARC_DATACRC     =  0x0002;
	public static final short EARC_VOLNUMBER   =  0x0008;
	
	
	protected long positionInFile;
	
	protected short headCRC = 0;
	protected byte headerType = 0;
	protected short flags = 0;
	protected short headerSize = 0 ;

    public static final int avHeaderSize = 7;
    public static final short commentHeaderSize = 6;
    public static final short signHeaderSize = 8;
    public static final short MacInfoHeaderSize = 8;
    public static final short endArcArchiveDataCrcSize = 4;
    public static final short endArcVolumeNumberSize = 2;
    public static final short EAHeaderSize = 10;
	
	public BaseBlock(BaseBlock bb) throws IOException {
		this.flags = bb.getFlags();
    	this.headCRC = bb.getHeadCRC();
    	this.headerType = bb.getHeaderType().getHeaderByte();
    	this.headerSize = bb.getHeaderSize();
    	this.positionInFile = bb.getPositionInFile();

        if ((this.flags & MHD_PASSWORD)!=0){
            throw new IOException("Encrypted block");
        }
	}
	public BaseBlock(byte[] baseBlockHeader) throws IOException{
		
		int pos = 0;
		this.headCRC = Raw.readShortLittleEndian(baseBlockHeader, pos);
		pos+=2;
		this.headerType |= baseBlockHeader[pos]&0xff;
		pos++;
		this.flags = Raw.readShortLittleEndian(baseBlockHeader, pos);
		pos+=2;
		this.headerSize = Raw.readShortLittleEndian(baseBlockHeader, pos);

        if ((this.flags & MHD_PASSWORD)!=0){
            throw new IOException("Encrypted block");
        }
	}
	
	
	public boolean hasArchiveDataCRC(){
		return (this.flags & EARC_DATACRC)!=0;
	}
	
	public boolean hasVolumeNumber(){
		return (this.flags & EARC_VOLNUMBER)!=0;
	}
	
	public boolean hasEncryptVersion(){
		return (flags & MHD_ENCRYPTVER)!=0;
	}

    public long getPositionInFile() {
		return positionInFile;
	}

	public short getFlags() {
		return flags;
	}

	public short getHeadCRC() {
		return headCRC;
	}
	
	public short getHeaderSize() {
		return headerSize;
	}

	public UnrarHeadertype getHeaderType() {
		return UnrarHeadertype.findType(headerType);
	}
	
	public void setPositionInFile(long positionInFile) {
		this.positionInFile = positionInFile;
	}
	
	public void print(){
        String str = "HeaderType: "+getHeaderType()+"\nHeadCRC: "+Integer.toHexString(getHeadCRC())+
		            "\nFlags: "+Integer.toHexString(getFlags())+"\nHeaderSize: "+getHeaderSize()+
		            "\nPosition in file: "+getPositionInFile();
        System.out.println(str);
	}
}
