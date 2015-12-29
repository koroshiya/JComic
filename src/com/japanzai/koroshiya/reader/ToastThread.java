package com.japanzai.koroshiya.reader;

import android.app.Activity;
import android.widget.Toast;

/**
 * Used to display messages from another thread.
 * Should always be called via "runOnUiThread(Thread)"
 * */
public class ToastThread extends Thread{
	
	private final String message;
	private final Activity parent;
	private final int duration;

    public ToastThread(String message, Activity parent){
        this(message, parent, false);
    }

    public ToastThread(String message, Activity parent, boolean isLong){
        this.message = message;
        this.parent = parent;
        this.duration = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
    }
	
	public ToastThread(int messageID, Activity parent){
		this.message = parent.getString(messageID);
		this.parent = parent;
		this.duration = Toast.LENGTH_SHORT;
	}
	
	@Override
	public void run(){
		Toast.makeText(parent, message, duration).show();
	}
	
}
