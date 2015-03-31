package com.japanzai.koroshiya.settings.controls;

import com.japanzai.koroshiya.interfaces.StateBasedSetting;

import android.content.Context;
import android.widget.CheckBox;

/**
 * StateBasedSetting with two states. Defined by a CheckBox.
 * */
public class CheckSetting extends CheckBox implements StateBasedSetting{
	
	public CheckSetting (Context context){
		this("Test setting", false, context);
	}
	
	public CheckSetting(int setting, boolean enabledByDefault, Context context){

		this(context.getString(setting), enabledByDefault, context);
		
	}
	
	/**
	 * @param setting Name of this setting. 
	 * 				This text will be displayed next to the setting.
	 * @param enabledByDefault If true, the setting is checked by default.
	 * @param context The context within which this class was instantiated.
	 * */
	public CheckSetting(String setting, boolean enabledByDefault, Context context){

		super(context);
		super.setChecked(enabledByDefault);
		super.setText(setting);
		
	}

	@Override
	public int getState() {
		return this.isChecked() ? 1 : 0;
	}
	
	@Override
	public void setState(int state){
		this.setChecked(state == 1);
	}
	
}
