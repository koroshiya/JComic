package com.japanzai.koroshiya.settings;

import com.japanzai.koroshiya.R;
//import com.japanzai.koroshiya.settings.controls.CheckSetting;
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
 * Encompasses settings pertaining to application performance.
 * */
@SuppressLint("ValidFragment")
public class PerformanceSettings extends SettingTab {
	
	private final SettingsView parent;
	private final SettingsManager settings;
	
	private CheckSetting cacheOnStartup;
	private SpinnerSetting archiveMode;
	private SpinnerSetting cacheMode;
	private SpinnerSetting cacheLevel;
	private SpinnerSetting resizeMode;
	
	public PerformanceSettings(SettingsView parent) {
		
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
		
		LinearLayout lLayout = (LinearLayout) this.parent.findViewById(R.id.tabGeneralSettings);
		lLayout.removeAllViews();
		
		try{
			cacheOnStartup = new CheckSetting(R.string.setting_cache_on_startup, true, true, this.parent);
			lLayout.addView(cacheOnStartup);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			cacheOnStartup = null;
		}
		
		try{
			archiveMode = new SpinnerSetting(this.parent, R.string.setting_archive, R.array.performance_setting_archive_mode, 0);
			lLayout.addView(archiveMode);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			archiveMode = null;
		}
		
		try{
			cacheMode = new SpinnerSetting(this.parent, R.string.setting_cache, R.array.performance_setting_cache_mode, 0);
			lLayout.addView(cacheMode);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			cacheMode = null;
		}
		
		try{
			cacheLevel = new SpinnerSetting(this.parent, R.string.advanced_setting_cache_level, R.array.advanced_setting_cache, 2);
			lLayout.addView(cacheLevel);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			cacheLevel = null;
		}
		
		try{
			resizeMode = new SpinnerSetting(this.parent, R.string.setting_resize, R.array.performance_setting_resize_mode, 0);
			lLayout.addView(resizeMode);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			resizeMode = null;
		}
		
	}
	
	public void save(){

		//settings.setExtractModeEnabled(extractMode.getState() == 1);
        if (cacheOnStartup != null) settings.setCacheOnStart(cacheOnStartup.getState() == 1);
		if (archiveMode != null){settings.setArchiveModeIndex(archiveMode.getState());}
		if (cacheMode != null){settings.setCacheModeIndex(cacheMode.getState());}
		if (cacheLevel != null){settings.setCacheLevel(cacheLevel.getState());}
		if (resizeMode != null){settings.setDynamicResizing(resizeMode.getState());}
		
	}
	
	public void load(){

		//extractMode.setState(settings.getExtractModeEnabled() ? 1 : 0);
        if (cacheOnStartup != null) cacheOnStartup.setState(settings.isCacheOnStart() ? 1 : 0);
		if (archiveMode != null){archiveMode.setState(settings.getArchiveModeIndex());}
		if (cacheMode != null){cacheMode.setState(settings.getCacheModeIndex());}
		if (cacheLevel != null){cacheLevel.setState(settings.getCacheLevel());}
		if (resizeMode != null){resizeMode.setState(settings.getDynamicResizing());}
		
	}
		
}
