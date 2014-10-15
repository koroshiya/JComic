package com.japanzai.koroshiya.reader;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.japanzai.koroshiya.dialog.MessageDialog;

/**
 * Used to display messages from another thread.
 * Should always be called via "runOnUiThread(Thread)"
 * */
public class MessageThread extends Thread{

	private final String message;
	private final ArrayList<String> messages;
	private final SherlockFragmentActivity parent;
	
	public MessageThread(String message, SherlockFragmentActivity parent){
		this.message = message;
		this.messages = null;
		this.parent = parent;
	}
	
	public MessageThread(ArrayList<String> messages, SherlockFragmentActivity parent){
		this.message = null;
		this.messages = messages;
		this.parent = parent;
	}
	
	public MessageThread(int messageID, SherlockFragmentActivity parent){
		this.message = parent.getString(messageID);
		this.messages = null;
		this.parent = parent;
	}
	
	@Override
	public void run(){
		if (message != null){
			print(message);
		}else{
			print(messages);
		}
	}

    /**
     * @param message Message to display as a Dialog
     * */
    public void print(String message){
    	MessageDialog md = new MessageDialog(message);
		md.show(parent.getSupportFragmentManager(), "MainActivity");
    }
    
    /**
     * @param message Message to display as a Dialog
     * */
    public void print(ArrayList<String> message){
    	MessageDialog md = new MessageDialog(message);
		md.show(parent.getSupportFragmentManager(), "MainActivity");
    }
	
}
