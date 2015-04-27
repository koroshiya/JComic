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

package net.lingala.zip4j.util;

import java.io.File;
import java.io.UnsupportedEncodingException;

import net.lingala.zip4j.exception.ZipException;

public class Zip4jUtil {
	
	public static boolean isStringNotNullAndNotEmpty(String str) {
        return str != null && str.length() > 0;

    }
	
	public static boolean checkFileReadAccess(String path) throws ZipException {
		if (!isStringNotNullAndNotEmpty(path)) {
			throw new ZipException("path is null");
		}
		
		if (!checkFileExists(path)) {
			throw new ZipException("file does not exist: " + path);
		}
		
		try {
			File file = new File(path);
			return file.canRead();
		} catch (Exception e) {
			throw new ZipException("cannot read zip file");
		}
	}
	
	public static boolean checkFileExists(String path) throws ZipException {
		if (!isStringNotNullAndNotEmpty(path)) {
			throw new ZipException("path is null");
		}
		
		File file = new File(path);
		return checkFileExists(file);
	}
	
	public static boolean checkFileExists(File file) throws ZipException {
		if (file == null) {
			throw new ZipException("cannot check if file exists: input file is null");
		}
		return file.exists();
	}

    /**
	 * Decodes file name based on encoding. If file name is UTF 8 encoded
	 * returns an UTF8 encoded string, else return Cp850 encoded String. If 
	 * appropriate charset is not supported, then returns a System default 
	 * charset encoded String
	 * @return String
	 */
	public static String decodeFileName(byte[] data, boolean isUTF8) {
		if (isUTF8) {
			try {
				return new String(data, InternalZipConstants.CHARSET_UTF8);
			} catch (UnsupportedEncodingException e) {
				return new String(data);
			}
		} else {
			return getCp850EncodedString(data);
		}
	}
	
	/**
	 * Returns a string in Cp850 encoding from the input bytes.
	 * If this encoding is not supported, then String with the default encoding is returned.
	 * @return String
	 */
	public static String getCp850EncodedString(byte[] data) {
		try {
			return new String(data, InternalZipConstants.CHARSET_CP850);
		} catch (UnsupportedEncodingException e) {
			return new String(data);
		}
	}

}
