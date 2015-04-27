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

public class LocalFileHeader {
	
	private int compressionMethod;
	
	private long crc32;
	
	private long compressedSize;
	
	private long uncompressedSize;
	
	private int extraFieldLength;
	
	private long offsetStartOfData;
	
	private boolean isEncrypted;
	
	private ArrayList extraDataRecords;
	
	private boolean fileNameUTF8Encoded;
	
	public LocalFileHeader() {
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
		return crc32;
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

	public long getOffsetStartOfData() {
		return offsetStartOfData;
	}

	public void setOffsetStartOfData(long offsetStartOfData) {
		this.offsetStartOfData = offsetStartOfData;
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}

	public void setEncrypted() {
		this.isEncrypted = true;
	}

	public ArrayList getExtraDataRecords() {
		return extraDataRecords;
	}

	public void setExtraDataRecords(ArrayList extraDataRecords) {
		this.extraDataRecords = extraDataRecords;
	}

	public boolean isFileNameUTF8Encoded() {
		return fileNameUTF8Encoded;
	}

	public void setFileNameUTF8Encoded(boolean fileNameUTF8Encoded) {
		this.fileNameUTF8Encoded = fileNameUTF8Encoded;
	}
	
}
