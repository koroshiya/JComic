package com.japanzai.koroshiya.dialog;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockDialogFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

/**
 * Displays a message to the user in the form of a dialog.
 * */
@SuppressLint("ValidFragment")
public class MessageDialog extends SherlockDialogFragment{
	
	private final String message;
	
	/**
	 * @param message Message to display
	 * */
	public MessageDialog(String message){
		
		this.message = message;
		
	}
	
	/**
	 * @param message Message to display
	 * */
	public MessageDialog(ArrayList<String> message){

        StringBuilder buffer = new StringBuilder();
		
		for (String line : message){
			buffer.append(line).append("\n");
		}
		
		this.message = buffer.toString();
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message);
		return builder.create();
		
	}
	
}
