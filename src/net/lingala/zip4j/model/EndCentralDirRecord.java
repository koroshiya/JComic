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

public class EndCentralDirRecord {
	
	private int noOfThisDisk;
	
	private int totNoOfEntriesInCentralDir;
	
	private long offsetOfStartOfCentralDir;

	public int getNoOfThisDisk() {
		return noOfThisDisk;
	}

	public void setNoOfThisDisk(int noOfThisDisk) {
		this.noOfThisDisk = noOfThisDisk;
	}

	public int getTotNoOfEntriesInCentralDir() {
		return totNoOfEntriesInCentralDir;
	}

	public void setTotNoOfEntriesInCentralDir(int totNoOfEntrisInCentralDir) {
		this.totNoOfEntriesInCentralDir = totNoOfEntrisInCentralDir;
	}

	public long getOffsetOfStartOfCentralDir() {
		return offsetOfStartOfCentralDir;
	}

	public void setOffsetOfStartOfCentralDir(long offSetOfStartOfCentralDir) {
		this.offsetOfStartOfCentralDir = offSetOfStartOfCentralDir;
	}
	
}
