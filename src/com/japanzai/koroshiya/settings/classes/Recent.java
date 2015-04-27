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

    @Override
    public String toString(){
        return this.path + ";" + this.pageNumber;
    }

    public static Recent fromString(String str){
        int pos = str.lastIndexOf(';');
        String path = str.substring(0, pos);
        int page = Integer.parseInt(str.substring(pos + 1));
        return new Recent(path, page);
    }
	
}
