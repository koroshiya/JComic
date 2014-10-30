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
	
	public ToastThread(String message, Activity parent, int duration){
		this.message = message;
		this.parent = parent;
		this.duration = duration;
	}
	
	public ToastThread(int messageID, Activity parent, int duration){
		this.message = parent.getString(messageID);
		this.parent = parent;
		this.duration = duration;
	}
	
	@Override
	public void run(){
		Toast.makeText(parent, message, duration).show();
	}
	
}
