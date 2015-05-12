package com.japanzai.koroshiya.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

/**
 * Displays a message to the user in the form of a dialog.
 * */
@SuppressLint("ValidFragment")
public class MessageDialog extends DialogFragment {
	
	private final String message;
    private final String title;

    /**
     * @param message Message to display
     * */
    public MessageDialog(String message){

        this.message = message;
        this.title = "";

    }

    public MessageDialog(ArrayList<String> arr){
        this(arr, "");
    }

    public MessageDialog(ArrayList<String> arr, String title){

        StringBuilder buffer = new StringBuilder();

        for (String res : arr){
            buffer.append(res).append("\n");
        }

        this.message = buffer.toString();
        this.title = title;

    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message);
        if (title.length() > 0) builder.setTitle(title);
		return builder.create();
		
	}

    public void show(FragmentActivity act){
        super.show(act.getSupportFragmentManager(), "MessageDialog");
    }
	
}
