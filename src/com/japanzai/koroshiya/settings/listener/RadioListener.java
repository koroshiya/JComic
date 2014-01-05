package com.japanzai.koroshiya.settings.listener;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

/**
 * Listener for a RadioSetting setting. 
 * Listens for changes to which RadioButton is selected.
 * */
public class RadioListener implements OnCheckedChangeListener {

	private final RadioButton option1;
	private final RadioButton option2;
	
	public RadioListener(RadioButton option1, RadioButton option2){
		
		this.option1 = option1;
		this.option2 = option2;
		
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if (!isChecked){return;}
		
		RadioButton[] buttons = {option1, option2};
		
		for (RadioButton button : buttons){
			
			button.setChecked(button == buttonView);
			
		}
		
	}
	
}
