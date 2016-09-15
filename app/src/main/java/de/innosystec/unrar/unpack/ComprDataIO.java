/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 31.05.2007
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
package de.innosystec.unrar.unpack;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.Volume;
import de.innosystec.unrar.crc.RarCRC;
import de.innosystec.unrar.io.ReadOnlyAccessInputStream;
import de.innosystec.unrar.rarfile.FileHeader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ComprDataIO {

	private final Archive archive;

	private long unpPackedSize;

	private boolean testMode;

	private boolean skipUnpCRC;

	private InputStream inputStream;

	private OutputStream outputStream;


	private FileHeader subHead;

	private long unpFileCRC, packedCRC;

	public ComprDataIO(Archive arc) {
		this.archive = arc;
	}

	public void init(OutputStream outputStream) {
		this.outputStream = outputStream;
		unpPackedSize = 0;
		testMode = false;
		skipUnpCRC = false;
		unpFileCRC = packedCRC = 0xffffffff;
		subHead = null;
	}

    public void init(FileHeader hd) throws IOException {
        long startPos = hd.getPositionInFile() + hd.getHeaderSize();
        unpPackedSize = hd.getFullPackSize();
        inputStream = new ReadOnlyAccessInputStream(archive.getRof(), startPos, startPos + unpPackedSize);
		subHead = hd;
        packedCRC = 0xFFffFFff;
    }

    public int unpRead(byte[] addr, int offset, int count)
            throws IOException {
        int retCode=0, totalRead=0;
        while (count > 0) {
            int readSize = (count > unpPackedSize) ? (int)unpPackedSize : count;
            retCode = inputStream.read(addr, offset, readSize);
            if (retCode < 0) {
                throw new EOFException();
            }
            if (subHead.isSplitAfter()){
                packedCRC = RarCRC.checkCrc(
                        (int)packedCRC, addr, offset, retCode);
            }

            totalRead += retCode;
            offset += retCode;
            count -= retCode;
            unpPackedSize -= retCode;
            archive.bytesReadRead(retCode);
            if (unpPackedSize == 0 && subHead.isSplitAfter()) {
                if (!Volume.mergeArchive(archive, this)) {
                    return -1;
                }
            }
            else {
                break;
            }
        }

        if (retCode != -1) {
            retCode = totalRead;
        }
        return retCode;


	}

	public void unpWrite(byte[] addr, int offset, int count)
            throws IOException {
		if (!testMode) {
			// DestFile->Write(Addr,Count);
			outputStream.write(addr, offset, count);
		}

		if (!skipUnpCRC){
			if (archive.isOldFormat()){
				unpFileCRC = RarCRC.checkOldCrc(
                        (short)unpFileCRC, addr, count);
			}
			else{
				unpFileCRC = RarCRC.checkCrc(
                        (int)unpFileCRC, addr,offset, count);
			}
		}
	}

	public long getPackedCRC() {
		return packedCRC;
	}

	public long getUnpFileCRC()
	{
		return unpFileCRC;
	}

	public void setUnpFileCRC(long unpFileCRC)
	{
		this.unpFileCRC = unpFileCRC;
	}

	public FileHeader getSubHeader()
	{
		return subHead;
	}

}
