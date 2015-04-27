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

import java.io.DataInput;
import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;

public class Raw
{
	public static long readLongLittleEndian(byte[] array){
		long temp = 0;
		temp |= array[7]&0xff;
		temp <<=8;
		temp |= array[6]&0xff;
		temp <<=8;
		temp |= array[5]&0xff;
		temp <<=8;
		temp |= array[4]&0xff;
		temp <<=8;
		temp |= array[3]&0xff;
		temp <<=8;
		temp |= array[2]&0xff;
		temp <<=8;
		temp |= array[1]&0xff;
		temp <<=8;
		temp |= array[0]&0xff;
		return temp;
	}
	
	public static int readLeInt(DataInput di, byte[] b) throws ZipException{
		try {
			di.readFully(b, 0, 4);
		} catch (IOException e) {
			throw new ZipException();
		}
	    return ((b[0] & 0xff) | (b[1] & 0xff) << 8)
		    | ((b[2] & 0xff) | (b[3] & 0xff) << 8) << 16;
	}

	public static int readShortLittleEndian(byte[] b, int off){
	    return (b[off] & 0xff) | (b[off+1] & 0xff) << 8;
	}
	
	public static short readShortBigEndian(byte[] array, int pos) {
		short temp = 0;
		temp |= array[pos] & 0xff;
		temp <<= 8;
		temp |= array[pos + 1] & 0xff;
		return temp;
	}

	public static int readIntLittleEndian(byte[] b){
	    return ((b[0] & 0xff) | (b[1] & 0xff) << 8)
		    | ((b[2] & 0xff) | (b[3] & 0xff) << 8) << 16;
	}

}
