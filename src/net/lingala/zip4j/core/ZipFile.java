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

package net.lingala.zip4j.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.unzip.Unzip;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jUtil;

/**
 * Base class to handle zip files. Some of the operations supported
 * in this class are:<br>
 * <ul>
 * 		<li>Create Zip File</li>
 * 		<li>Add files to zip file</li>
 * 		<li>Add folder to zip file</li>
 * 		<li>Extract files from zip files</li>
 * 		<li>Remove files from zip file</li>
 * </ul>
 *
 */

public class ZipFile {
	
	private String file;
	private int mode;
	private ZipModel zipModel;
	
	/**
	 * Creates a new Zip File Object with the given zip file path.
	 * If the zip file does not exist, it is not created at this point. 
	 * @throws ZipException
	 */
	public ZipFile(String zipFile) throws ZipException {
		this(new File(zipFile));
	}
	
	/**
	 * Creates a new Zip File Object with the input file.
	 * If the zip file does not exist, it is not created at this point.
	 * @throws ZipException
	 */
	public ZipFile(File zipFile) throws ZipException {
		if (zipFile == null) {
			throw new ZipException("Input zip file parameter is not null"
            );
		}
		
		this.file = zipFile.getPath();
		this.mode = InternalZipConstants.MODE_UNZIP;
	}
	
	/**
	 * Reads the zip header information for this zip file. If the zip file
	 * does not exist, then this method throws an exception.<br><br>
	 * <b>Note:</b> This method does not read local file header information
	 * @throws ZipException
	 */
	private void readZipInfo() throws ZipException {
		
		if (!Zip4jUtil.checkFileExists(file)) {
			throw new ZipException("zip file does not exist");
		}
		
		if (!Zip4jUtil.checkFileReadAccess(this.file)) {
			throw new ZipException("no read access for the input zip file");
		}
		
		if (this.mode != InternalZipConstants.MODE_UNZIP) {
			throw new ZipException("Invalid mode");
		}
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(new File(file), InternalZipConstants.READ_MODE);
			
			if (zipModel == null) {
				
				HeaderReader headerReader = new HeaderReader(raf);
				zipModel = headerReader.readAllHeaders();
				if (zipModel != null) {
					zipModel.setZipFile(file);
				}
			}
		} catch (FileNotFoundException e) {
			throw new ZipException();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
	}
	
	/**
	 * Returns the list of file headers in the zip file. Throws an exception if the 
	 * zip file does not exist
	 * @return list of file headers
	 * @throws ZipException
	 */
	public List getFileHeaders() throws ZipException {
		readZipInfo();
		if (zipModel == null || zipModel.getCentralDirectory() == null) {
			return null;
		}
		return zipModel.getCentralDirectory().getFileHeaders();
	}
	
	/**
	 * Loads the zip model if zip model is null and if zip file exists.
	 * @throws ZipException
	 */
	private void checkZipModel() throws ZipException {
		if (this.zipModel == null) {
			if (Zip4jUtil.checkFileExists(file)) {
				readZipInfo();
			} else {
				createNewZipModel();
			}
		}
	}
	
	/**
	 * Creates a new instance of zip model
	 */
	private void createNewZipModel() {
		zipModel = new ZipModel();
		zipModel.setZipFile(file);
	}
	
	/**
	 * Returns an input stream for reading the contents of the Zip file corresponding
	 * to the input FileHeader. Throws an exception if the FileHeader does not exist
	 * in the ZipFile
	 * @return ZipInputStream
	 * @throws ZipException
	 */
	public ZipInputStream getInputStream(FileHeader fileHeader) throws ZipException {
		if (fileHeader == null) {
			throw new ZipException("FileHeader is null, cannot get InputStream");
		}
		
		checkZipModel();
		
		if (zipModel == null) {
			throw new ZipException("zip model is null, cannot get inputstream");
		}
		
		Unzip unzip = new Unzip(zipModel);
		return unzip.getInputStream(fileHeader);
	}
	
	/**
	 * Checks to see if the input zip file is a valid zip file. This method
	 * will try to read zip headers. If headers are read successfully, this
	 * method returns true else false 
	 * @return boolean
	 * @since 1.2.3
	 */
	public boolean isValidZipFile() {
		try {
			readZipInfo();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
