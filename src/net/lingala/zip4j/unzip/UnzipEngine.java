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

package net.lingala.zip4j.unzip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

import net.lingala.zip4j.core.HeaderReader;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.InflaterInputStream;
import net.lingala.zip4j.io.PartInputStream;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;

public class UnzipEngine {
	
	private ZipModel zipModel;
	private FileHeader fileHeader;
	private int currSplitFileCounter = 0;
	private LocalFileHeader localFileHeader;
	private CRC32 crc;
	
	public UnzipEngine(ZipModel zipModel, FileHeader fileHeader) throws ZipException {
		if (zipModel == null || fileHeader == null) {
			throw new ZipException("Invalid parameters passed to StoreUnzip. One or more of the parameters were null");
		}
		
		this.zipModel = zipModel;
		this.fileHeader = fileHeader;
		this.crc = new CRC32();
	}
	
	public ZipInputStream getInputStream() throws ZipException {
		if (fileHeader == null) {
			throw new ZipException("file header is null, cannot get inputstream");
		}
		
		RandomAccessFile raf = null;
		try {
			raf = createFileHandler();
			String errMsg = "local header and file header do not match";
			//checkSplitFile();
			
			if (!checkLocalHeader())
				throw new ZipException(errMsg);
			
			init();
			
			long comprSize = localFileHeader.getCompressedSize();
			long offsetStartOfData = localFileHeader.getOffsetStartOfData();
			
			if (localFileHeader.isEncrypted()) {
                throw new ZipException("invalid decryptor when trying to calculate " +
                        "compressed size for AES encrypted file: " + fileHeader.getFileName());
			}
			
			int compressionMethod = fileHeader.getCompressionMethod();
			raf.seek(offsetStartOfData);
			switch (compressionMethod) {
			case Zip4jConstants.COMP_STORE:
				return new ZipInputStream(new PartInputStream(raf, comprSize, this));
			case Zip4jConstants.COMP_DEFLATE:
				return new ZipInputStream(new InflaterInputStream(raf, comprSize, this));
			default:
				throw new ZipException("compression type not supported");
			}
		} catch (ZipException e) {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e1) {
					//ignore
				}
			}
			throw e;
		} catch (Exception e) {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException ignored) {
				}
			}
			throw new ZipException();
		}
		
	}
	
	private void init() throws ZipException {
		
		if (localFileHeader == null) {
			throw new ZipException("local file header is null, cannot initialize input stream");
		}
		
		try {
			initDecrypter();
		} catch (ZipException e) {
			throw e;
		} catch (Exception e) {
			throw new ZipException();
		}
	}
	
	private void initDecrypter() throws ZipException {
		if (localFileHeader == null) {
			throw new ZipException("local file header is null, cannot init decrypter");
		}
		
		if (localFileHeader.isEncrypted()) {
            throw new ZipException("unsupported encryption method");
		}
	}
	
	private boolean checkLocalHeader() throws ZipException {
		RandomAccessFile rafForLH = null;
		try {
			rafForLH = checkSplitFile();
			
			if (rafForLH == null) {
				rafForLH = new RandomAccessFile(new File(this.zipModel.getZipFile()), InternalZipConstants.READ_MODE);
			}
			
			HeaderReader headerReader = new HeaderReader(rafForLH);
			this.localFileHeader = headerReader.readLocalFileHeader(fileHeader);
			
			if (localFileHeader == null) {
				throw new ZipException("error reading local file header. Is this a valid zip file?");
			}
			
			//TODO Add more comparision later
            return localFileHeader.getCompressionMethod() == fileHeader.getCompressionMethod();

        } catch (FileNotFoundException e) {
			throw new ZipException();
		} finally {
			if (rafForLH != null) {
				try {
					rafForLH.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private RandomAccessFile checkSplitFile() throws ZipException {
		if (zipModel.isSplitArchive()) {
			int diskNumberStartOfFile = fileHeader.getDiskNumberStart();
			currSplitFileCounter = diskNumberStartOfFile + 1;
			String curZipFile = zipModel.getZipFile();
			String partFile;
			if (diskNumberStartOfFile == zipModel.getEndCentralDirRecord().getNoOfThisDisk()) {
				partFile = zipModel.getZipFile();
			} else {
				if (diskNumberStartOfFile >= 9) {
					partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z" + (diskNumberStartOfFile+ 1);
				} else{
					partFile = curZipFile.substring(0, curZipFile.lastIndexOf(".")) + ".z0" + (diskNumberStartOfFile+ 1);
				}
			}
			
			try {
				RandomAccessFile raf = new RandomAccessFile(partFile, InternalZipConstants.READ_MODE);
				
				if (currSplitFileCounter == 1) {
					byte[] splitSig = new byte[4];
					raf.read(splitSig);
					if (Raw.readIntLittleEndian(splitSig) != InternalZipConstants.SPLITSIG) {
						throw new ZipException("invalid first part split file signature");
					}
				}
				return raf;
			} catch (IOException e) {
				throw new ZipException();
			}
		}
		return null;
	}
	
	private RandomAccessFile createFileHandler() throws ZipException {
		if (this.zipModel == null || !Zip4jUtil.isStringNotNullAndNotEmpty(this.zipModel.getZipFile())) {
			throw new ZipException("input parameter is null in getFilePointer");
		}
		
		try {
			RandomAccessFile raf;
			if (zipModel.isSplitArchive()) {
				raf = checkSplitFile();
			} else {
				raf = new RandomAccessFile(new File(this.zipModel.getZipFile()), InternalZipConstants.READ_MODE);
			}
			return raf;
		} catch (Exception e) {
			throw new ZipException();
		}
	}
	
	public RandomAccessFile startNextSplitFile() throws IOException {
		String currZipFile = zipModel.getZipFile();
		String partFile;
		if (currSplitFileCounter == zipModel.getEndCentralDirRecord().getNoOfThisDisk()) {
			partFile = zipModel.getZipFile();
		} else {
			if (currSplitFileCounter >= 9) {
				partFile = currZipFile.substring(0, currZipFile.lastIndexOf(".")) + ".z" + (currSplitFileCounter + 1);
			} else {
				partFile = currZipFile.substring(0, currZipFile.lastIndexOf(".")) + ".z0" + (currSplitFileCounter + 1);
			}
		}
		currSplitFileCounter++;
		try {
			if(!Zip4jUtil.checkFileExists(partFile)) {
				throw new IOException("zip split file does not exist: " + partFile);
			}
		} catch (ZipException e) {
			throw new IOException(e.getMessage());
		}
		return new RandomAccessFile(partFile, InternalZipConstants.READ_MODE);
	}
	
	public void updateCRC(int b) {
		crc.update(b);
	}
	
	public void updateCRC(byte[] buff, int offset, int len) {
		if (buff != null) {
			crc.update(buff, offset, len);
		}
	}

	public FileHeader getFileHeader() {
		return fileHeader;
	}

	public ZipModel getZipModel() {
		return zipModel;
	}
}
