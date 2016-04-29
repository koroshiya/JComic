package com.koroshiya.settings.classes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Recent {
	
	private final String path;
	private int pageNumber;
    private final long uuid;

    public static final String JSON_ARG_FILE = "file";
    public static final String JSON_ARG_PAGE = "page";
    public static final String JSON_ARG_UUID = "uuid";

    public Recent(String path, int pageNumber){
        this(path, pageNumber, UUID.randomUUID().getMostSignificantBits());
    }

    public Recent(String path, int pageNumber, long uuid){
        this.path = path;
        this.pageNumber = pageNumber;
        this.uuid = uuid;
    }

    public Recent(JSONObject jsObj) throws JSONException {
        this.path = jsObj.getString(JSON_ARG_FILE);
        this.pageNumber = jsObj.getInt(JSON_ARG_PAGE);
        this.uuid = jsObj.getLong(JSON_ARG_UUID);
        Log.i("Recent", "Recent path: "+path);
    }

	public String getPath(){
		return this.path;
	}
	
	public int getPageNumber(){
		return this.pageNumber;
	}

    public long getUuid(){return this.uuid;}

    public JSONObject toJSON() throws JSONException {
        JSONObject jsObj = new JSONObject();
        jsObj.put(JSON_ARG_FILE, this.path);
        jsObj.put(JSON_ARG_PAGE, this.pageNumber);
        jsObj.put(JSON_ARG_UUID, this.uuid);
        return jsObj;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

}
