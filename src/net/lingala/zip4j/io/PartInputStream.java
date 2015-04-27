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

package net.lingala.zip4j.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.lingala.zip4j.unzip.UnzipEngine;

public class PartInputStream extends BaseInputStream
{
	private RandomAccessFile raf;
	private long bytesRead;
    private final long length;
	private final UnzipEngine unzipEngine;
	private final byte[] oneByteBuff = new byte[1];

    public PartInputStream(RandomAccessFile raf, long len, UnzipEngine unzipEngine) {
	    this.raf = raf;
	    this.unzipEngine = unzipEngine;
	    this.bytesRead = 0;
	    this.length = len;
	}
  
	public int available() {
		long amount = length - bytesRead;
		if (amount > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		return (int) amount;
	}
  
	public int read() throws IOException {
		if (bytesRead >= length)
			return -1;

        return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xff;
	}
	
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
		if (len > length - bytesRead) {
			len = (int) (length - bytesRead);
			if (len == 0) {
				return -1;
			}
		}

        int count;
        synchronized (raf) {
			count = raf.read(b, off, len);
			if ((count < len) && unzipEngine.getZipModel().isSplitArchive()) {
				raf.close();
				raf = unzipEngine.startNextSplitFile();
				if (count < 0) count = 0;
				int newlyRead = raf.read(b, count, len- count);
				if (newlyRead > 0)
					count += newlyRead;
			}
		}
		
		if (count > 0) {
			bytesRead += count;
		}
		
		return count;
	}

	public long skip(long amount) throws IOException {
		if (amount < 0)
			throw new IllegalArgumentException();
		if (amount > length - bytesRead)
			amount = length - bytesRead;
		bytesRead += amount;
		return amount;
	}
  
	public void close() throws IOException {
		raf.close();
	}
	
	public UnzipEngine getUnzipEngine() {
		return this.unzipEngine;
	} 
}
