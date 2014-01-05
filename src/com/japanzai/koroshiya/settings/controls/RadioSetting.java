package com.japanzai.koroshiya.settings.controls;

import com.japanzai.koroshiya.interfaces.StateBasedSetting;
import com.japanzai.koroshiya.settings.listener.RadioListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;

/**
 * Setting defined by two RadioButtons. Can have two states.
 * May be deprecated and replaced by CheckSetting in later builds.
 * */
@SuppressLint("ViewConstructor")
public class RadioSetting extends LinearLayout implements StateBasedSetting {

	private final RadioButton firstOption;
	private final RadioButton secondOption;
	
	public RadioSetting(int setting1, int setting2, Context context, boolean checkFirst) {
		
		super(context);
		firstOption = new RadioButton(context);
		secondOption = new RadioButton(context);
		instantiate(setting1, setting2, context, checkFirst);
		
	}
	
	public RadioSetting(int setting1, int setting2, Context context, AttributeSet attr, boolean checkFirst){
		
		super(context, attr);
		firstOption = new RadioButton(context, attr);
		secondOption = new RadioButton(context, attr);
		instantiate(setting1, setting2, context, checkFirst);
		
	}
	
	/**
	 * @param setting1 ID of the string resource to be assigned to the first radiobutton.
	 * @param setting2 ID of the string resource to be assigned to the second radiobutton.
	 * @param context Context within which this setting will be created.
	 * @param checkFirst If true, the first radiobutton is checked. Otherwise, the second one is checked.
	 * */
	public void instantiate(int setting1, int setting2, Context context, boolean checkFirst){
		
		firstOption.setText(context.getString(setting1));
		secondOption.setText(context.getString(setting2));
		
		RadioListener rl = new RadioListener(firstOption, secondOption);
		firstOption.setOnCheckedChangeListener(rl);
		secondOption.setOnCheckedChangeListener(rl);
		
		firstOption.setChecked(checkFirst);
		secondOption.setChecked(!checkFirst);

		this.addView(firstOption);
		this.addView(secondOption);
		
	}
	
	@Override
	public int getState() {
		return firstOption.isChecked() ? 1 : 2;
	}

	@Override
	public void setState(int status){
		boolean first = status == 1;
		this.firstOption.setChecked(first);
		this.secondOption.setChecked(!first);
	}
	
}
