/*
* Copyright 2010 Srikanth Reddy Lingala  
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at 
* 
* http://www.apache.org/licenses/LICENSE-2.0 
* 
* Unless required by applicable law or agreed to in writing, 
* software distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License. 
*/

package net.lingala.zip4j.model;

import java.util.ArrayList;

public class FileHeader {
	
	private int compressionMethod;
	
	private long crc32;
	
	private long compressedSize;
	
	private long uncompressedSize;
	
	private int extraFieldLength;
	
	private int diskNumberStart;
	
	private long offsetLocalHeader;
	
	private String fileName;
	
	private Zip64ExtendedInfo zip64ExtendedInfo;
	
	private ArrayList extraDataRecords;
	
	private boolean fileNameUTF8Encoded;
	
	public FileHeader() {
		crc32 = 0;
		uncompressedSize = 0;
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}

	public void setCompressionMethod(int compressionMethod) {
		this.compressionMethod = compressionMethod;
	}

	public long getCrc32() {
		return crc32 & 0xffffffffL;
	}

	public void setCrc32(long crc32) {
		this.crc32 = crc32;
	}

	public long getCompressedSize() {
		return compressedSize;
	}

	public void setCompressedSize(long compressedSize) {
		this.compressedSize = compressedSize;
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}

	public void setUncompressedSize(long uncompressedSize) {
		this.uncompressedSize = uncompressedSize;
	}

	public int getExtraFieldLength() {
		return extraFieldLength;
	}

	public void setExtraFieldLength(int extraFieldLength) {
		this.extraFieldLength = extraFieldLength;
	}

	public int getDiskNumberStart() {
		return diskNumberStart;
	}

	public void setDiskNumberStart(int diskNumberStart) {
		this.diskNumberStart = diskNumberStart;
	}

	public long getOffsetLocalHeader() {
		return offsetLocalHeader;
	}

	public void setOffsetLocalHeader(long offsetLocalHeader) {
		this.offsetLocalHeader = offsetLocalHeader;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ArrayList getExtraDataRecords() {
		return extraDataRecords;
	}

	public void setExtraDataRecords(ArrayList extraDataRecords) {
		this.extraDataRecords = extraDataRecords;
	}

	public Zip64ExtendedInfo getZip64ExtendedInfo() {
		return zip64ExtendedInfo;
	}

	public void setZip64ExtendedInfo(Zip64ExtendedInfo zip64ExtendedInfo) {
		this.zip64ExtendedInfo = zip64ExtendedInfo;
	}

	public boolean isFileNameUTF8Encoded() {
		return fileNameUTF8Encoded;
	}

	public void setFileNameUTF8Encoded(boolean fileNameUTF8Encoded) {
		this.fileNameUTF8Encoded = fileNameUTF8Encoded;
	}

	
		
}
