package com.japanzai.koroshiya.settings.controls;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Setting class defined by a Spinner. 
 * Each value the Spinner contains is another option.
 * */
public class SpinnerSetting extends LinearLayout {

	private final Spinner spinner;
	
	public SpinnerSetting(Context context, int nameID, int arrayID, int defaultIndex) {
		
		super(context);
		spinner = new Spinner(context);
		
		instantiate(context, nameID, arrayID, defaultIndex);
		
	}
	
	/**
	 * @param context Context within which this class was created
	 * @param nameID ID of the String resource containing this Settings' description
	 * @param arrayID ID of the String Array resource containing the Spinner's options
	 * @param defaultIndex Index to set the Spinner to by default
	 * */
	public void instantiate(Context context, int nameID, int arrayID, int defaultIndex){

        final TextView label = new TextView(context);
		label.setText(context.getString(nameID) + "\t");
		label.setTextSize(18);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
				arrayID, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		if (defaultIndex >= 0 && defaultIndex < spinner.getCount()){
			spinner.setSelection(defaultIndex);
		}
		
		this.addView(label);
		this.addView(spinner);
		
	}

	public int getState() {
		return this.spinner.getSelectedItemPosition();
	}

	public void setState(int i){
		if (i >= 0 && i < this.spinner.getCount()){
			this.spinner.setSelection(i);
		}
	}

}
