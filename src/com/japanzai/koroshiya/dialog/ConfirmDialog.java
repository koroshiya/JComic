package com.japanzai.koroshiya.dialog;

import com.japanzai.koroshiya.interfaces.ModalReturn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Displays a prompt to the user in the form of a dialog.
 * */
public class ConfirmDialog extends DialogFragment implements OnClickListener{
	
	private final String confirm;
	private final String deny;
	private final String message;
	private final ModalReturn modal;

    public ConfirmDialog(){
        this("", "", "", null);
    }
	
	/**
	 * @param confirm Text for confirmation button
	 * @param deny Text for decline button
	 * @param message Actual question/prompt to display
	 * @param modal ModalReturn object to interact with once a selection has been made
	 * */
	public ConfirmDialog(String confirm, String deny, String message, ModalReturn modal){
		
		this.confirm = confirm;
		this.deny = deny;
		this.message = message;
		this.modal = modal;
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message);
		builder.setPositiveButton(confirm, this);
		builder.setNegativeButton(deny, this);
		builder.setCancelable(false);
		return builder.create();
		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		if (which == -1){
			modal.accept();
		}else{
			modal.decline();
		}
		
	}
	
}
