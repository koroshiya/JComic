package com.japanzai.koroshiya.settings;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.controls.CheckSetting;
import com.japanzai.koroshiya.settings.controls.SpinnerSetting;

import android.annotation.SuppressLint;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Encompasses settings not advised for general users.
 * */
@SuppressLint("ValidFragment")
public class AdvancedSettings extends SettingTab {
	
	private final SettingsView parent;
	private final SettingsManager settings;

	private SpinnerSetting recursion;
	private CheckSetting cacheSafety;
	
	public AdvancedSettings(SettingsView parent) {
		
		this.parent = parent;
		this.settings = MainActivity.mainActivity.getSettings();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		return inflater.inflate(R.layout.general_settings, group, false);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);                
        instantiate();
        
    }
	
	private void instantiate(){

		LinearLayout lLayout = new LinearLayout(this.parent);
		lLayout = (LinearLayout) this.parent.findViewById(R.id.tabGeneralSettings);
		lLayout.removeAllViews();
		
		try{
			cacheSafety = new CheckSetting(getString(R.string.advanced_cache_safety), settings.getCacheSafety(), true, this.parent);
			lLayout.addView(cacheSafety);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			cacheSafety = null;
		}
		
		try{
			recursion = new SpinnerSetting(this.parent, R.string.advanced_setting_recursion_level, R.array.advanced_setting_recursion, 0);
			lLayout.addView(recursion);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			recursion = null;
		}
		
	}
	
	public void save(){

		if (recursion != null){settings.setRecursionLevel(recursion.getState());}
		if (cacheSafety != null){settings.setCacheSafety(cacheSafety.getState() == 1);}
		
	}
	
	public void load(){

		if (recursion != null){recursion.setState(settings.getRecursionLevel());}
		if (cacheSafety != null){cacheSafety.setState(settings.getCacheSafety() ? 1 : 0);}
		
	}
	
}
