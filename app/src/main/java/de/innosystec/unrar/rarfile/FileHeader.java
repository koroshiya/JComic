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
import java.util.regex.Pattern;

import de.innosystec.unrar.io.Raw;

/**
 * DOCUMENT ME
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class FileHeader extends BlockHeader {

    private final int fileCRC;

    private byte unpVersion;

    private byte unpMethod;

    private String fileName;

    private long fullPackSize;

    private long fullUnpackSize;

    public FileHeader(BlockHeader bh, byte[] fileHeader) throws IOException {
        super(bh);

        int position = 0;
        long unpSize = Raw.readIntLittleEndian(fileHeader, position);
        position += 5;

        fileCRC = Raw.readIntLittleEndian(fileHeader, position);
        position += 4;

        Raw.readIntLittleEndian(fileHeader, position);
        position += 4;

        unpVersion |= fileHeader[13] & 0xff;
        position++;
        unpMethod |= fileHeader[14] & 0xff;
        position++;
        short nameSize = Raw.readShortLittleEndian(fileHeader, position);
        position += 2;

        Raw.readIntLittleEndian(fileHeader, position);
        position += 4;
        int highPackSize;
        int highUnpackSize;
        if (isLargeBlock()) {
            highPackSize = Raw.readIntLittleEndian(fileHeader, position);
            position += 4;

            highUnpackSize = Raw.readIntLittleEndian(fileHeader, position);
            position += 4;
        } else {
            highPackSize = 0;
            highUnpackSize = 0;
            if (unpSize == 0xffffffff) {

            unpSize = 0xffffffff;
            highUnpackSize = Integer.MAX_VALUE;
            }

        }
        fullPackSize |= highPackSize;
        fullPackSize <<= 32;
        fullPackSize |= getPackSize();

        fullUnpackSize |= highUnpackSize;
        fullUnpackSize <<= 32;
        fullUnpackSize += unpSize;

        nameSize = nameSize > 4 * 1024 ? 4 * 1024 : nameSize;

        byte[] fileNameBytes = new byte[nameSize];
        for (int i = 0; i < nameSize; i++) {
            fileNameBytes[i] = fileHeader[position];
            position++;
        }

        if (isFileHeader()) {
            String fileNameW;
            if (isUnicode()) {
            int length = 0;
            fileName = "";
            fileNameW = "";
            while (length < fileNameBytes.length
                && fileNameBytes[length] != 0) {
                length++;
            }
            byte[] name = new byte[length];
            System.arraycopy(fileNameBytes, 0, name, 0, name.length);
            fileName = new String(name);
            if (length != nameSize) {
                length++;
                fileNameW = FileNameDecoder.decode(fileNameBytes, length);
            }
            } else {
            fileName = new String(fileNameBytes);
            fileNameW = "";
            }

            //added by hoy, 处理中文问题
            if(existZH(fileNameW)){
                fileName = fileNameW;
            }
        }

    }

    private boolean existZH(String str) {
        return Pattern.compile("[\\u4e00-\\u9fa5]").matcher(str).find();
    }

    public int getFileCRC() {
	    return fileCRC;
    }

    public String getFileNameString() {
	    return fileName;
    }

    public byte getUnpMethod() {
	    return unpMethod;
    }

    public byte getUnpVersion() {
	    return unpVersion;
    }

    public long getFullPackSize() {
	    return fullPackSize;
    }

    public long getFullUnpackSize() {
	    return fullUnpackSize;
    }

    /**
     * the file will be continued in the next archive part
     *
     */
    public boolean isSplitAfter() {
	    return (this.flags & BlockHeader.LHD_SPLIT_AFTER) != 0;
    }

    /**
     * this file is compressed as solid (all files handeled as one)
     *
     */
    public boolean isSolid() {
	    return (this.flags & LHD_SOLID) != 0;
    }

    /**
     * the filename is also present in unicode
     *
     */
    public boolean isUnicode() {
	    return (flags & LHD_UNICODE) != 0;
    }

    public boolean isFileHeader() {
	    return UnrarHeadertype.FileHeader.equals(headerType);
    }

    public boolean isLargeBlock() {
	    return (flags & LHD_LARGE) != 0;
    }

}
