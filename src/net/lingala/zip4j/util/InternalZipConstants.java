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

public interface InternalZipConstants {
	
	/*
     * Header signatures
     */
	// Whenever a new Signature is added here, make sure to add it
	// in Zip4jUtil.getAllHeaderSignatures()
    static long LOCSIG = 0x04034b50L;	// "PK\003\004"
    static long CENSIG = 0x02014b50L;	// "PK\001\002"
    static long ENDSIG = 0x06054b50L;	// "PK\005\006"
    static long DIGSIG = 0x05054b50L;
    static long SPLITSIG = 0x08074b50L;
    static long ZIP64ENDCENDIRLOC = 0x07064b50L;
    static long ZIP64ENDCENDIRREC = 0x06064b50;

    /*
     * Header sizes in bytes (including signatures)
     */
    static final int ENDHDR = 22;	// END header size

    public static final int MODE_UNZIP = 2;

    public static final String READ_MODE = "r";
	
	public static final int BUFF_SIZE = 1024 * 4;

    public static final int UFT8_NAMES_FLAG = 1 << 11;
	
	public static final String CHARSET_UTF8 = "UTF8";
	
	public static final String CHARSET_CP850 = "Cp850";

}
