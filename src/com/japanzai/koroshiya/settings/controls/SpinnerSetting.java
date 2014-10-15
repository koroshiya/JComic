package com.japanzai.koroshiya.settings.controls;

import com.japanzai.koroshiya.interfaces.StateBasedSetting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Setting class defined by a Spinner. 
 * Each value the Spinner contains is another option.
 * */
@SuppressLint("ViewConstructor")
public class SpinnerSetting extends LinearLayout implements StateBasedSetting{

	private final Spinner spinner;
	private final TextView label;
	
	public SpinnerSetting(Context context, int nameID, int arrayID, int defaultIndex) {
		
		super(context);
		spinner = new Spinner(context);
		label = new TextView(context);
		
		instantiate(context, nameID, arrayID, defaultIndex);
		
	}
	
	public SpinnerSetting(Context context, AttributeSet attr, int nameID, int arrayID, int defaultIndex) {
		
		super(context, attr);
		spinner = new Spinner(context, attr);
		label = new TextView(context, attr);
		
		instantiate(context, nameID, arrayID, defaultIndex);
		
	}
	
	public SpinnerSetting(Context context, AttributeSet attr, int unknown, int nameID, int arrayID, int defaultIndex) {
		
		super(context, attr);
		spinner = new Spinner(context, attr, unknown);
		label = new TextView(context, attr, unknown);
		
		instantiate(context, nameID, arrayID, defaultIndex);
		
	}
	
	/**
	 * @param context Context within which this class was created
	 * @param nameID ID of the String resource containing this Settings' description
	 * @param arrayID ID of the String Array resource containing the Spinner's options
	 * @param defaultIndex Index to set the Spinner to by default
	 * */
	public void instantiate(Context context, int nameID, int arrayID, int defaultIndex){
		
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
	
	/**
	 * @return Retrieves the Spinner holding this class's options
	 * */
	public Spinner getSpinner(){
		return this.spinner;
	}
	
	/**
	 * @return Retrieves the TextView describing this setting
	 * */
	public TextView getTextView(){
		return this.label;
	}
	
	@Override
	public int getState() {
		return this.spinner.getSelectedItemPosition();
	}

	@Override
	public void setState(int i){
		if (i >= 0 && i < this.spinner.getCount()){
			this.spinner.setSelection(i);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);		
	}

}
