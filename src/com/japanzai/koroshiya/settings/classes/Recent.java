package com.japanzai.koroshiya.settings.classes;

public class Recent {
	
	private final String path;
	private final int pageNumber;
	
	public Recent(String path, int pageNumber){
		this.path = path;
		this.pageNumber = pageNumber;
	}
	
	public String getPath(){
		return this.path;
	}
	
	public int getPageNumber(){
		return this.pageNumber;
	}
	
}
