/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 29.05.2007
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
package de.innosystec.unrar.crc;

public abstract class RarCRC {
	
	private final static int crcTab[];
    static {
		crcTab = new int[256];
		for (int i = 0; i < 256; i++) {
			int c = i;
			for (int j = 0; j < 8; j++){
				if ((c & 1) !=0) {
					c >>>= 1;
					c ^= 0xEDB88320;
				}
                else{
					c >>>= 1;
				}
			}
			crcTab[i] = c;
		}
    }

	public static int checkCrc(int startCrc, byte[] data, int offset, int count) {
		int size = Math.min(data.length-offset,count);
		
		for (int i = 0; i < size; i++) {
			startCrc=(crcTab[((startCrc ^ (int)data[offset+i]))&0xff]^(startCrc>>>8));
		}
		return (startCrc);
	}

	public static short checkOldCrc(short startCrc, byte[] data, int count) {
        int n = Math.min(data.length, count);
		for (int i = 0; i < n; i++) {
			startCrc = ((short) (startCrc + (short) (data[i]&0x00ff)));
			startCrc = (short) (((startCrc << 1) | (startCrc >>> 15)));
		}
		return (startCrc);
	}
	
}
