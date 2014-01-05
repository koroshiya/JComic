package com.japanzai.koroshiya.settings.listener;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.GeneralSettings;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ResetListener implements OnClickListener{

	private GeneralSettings parent;
	
	public ResetListener(GeneralSettings parent){
		this.parent = parent;
	}
	
	@Override
	public void onClick(View v) {
		
		if (v instanceof Button){
			Button btn = (Button)v;
			if (btn.getText().toString().equals(parent.getString(R.string.setting_reset))){
				MainActivity.mainActivity.getSettings().restoreDefaultSettings();
				parent.load();
			}
		}
		
	}

}
