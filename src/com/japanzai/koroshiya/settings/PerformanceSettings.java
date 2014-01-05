package com.japanzai.koroshiya.settings;

import com.japanzai.koroshiya.R;
//import com.japanzai.koroshiya.settings.controls.CheckSetting;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.controls.CheckSetting;
import com.japanzai.koroshiya.settings.controls.SpinnerSetting;

import android.annotation.SuppressLint;
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
		
		cacheOnStartup = new CheckSetting(R.string.setting_cache_on_startup, true, true, this.parent);
		archiveMode = new SpinnerSetting(this.parent, R.string.setting_archive, R.array.performance_setting_archive_mode, 0);
		cacheMode = new SpinnerSetting(this.parent, R.string.setting_cache, R.array.performance_setting_cache_mode, 0);
		cacheLevel = new SpinnerSetting(this.parent, R.string.advanced_setting_cache_level, R.array.advanced_setting_cache, 2);
		resizeMode = new SpinnerSetting(this.parent, R.string.setting_resize, R.array.performance_setting_resize_mode, 0);
		
		LinearLayout lLayout = new LinearLayout(this.parent);
		lLayout = (LinearLayout) this.parent.findViewById(R.id.tabGeneralSettings);
		lLayout.removeAllViews();
		
		lLayout.addView(cacheOnStartup);
		lLayout.addView(archiveMode);
		lLayout.addView(cacheMode);
		lLayout.addView(cacheLevel);
		lLayout.addView(resizeMode);
		
	}
	
	public void save(){

		//settings.setExtractModeEnabled(extractMode.getState() == 1);
		settings.setArchiveModeIndex(archiveMode.getState());
		settings.setCacheModeIndex(cacheMode.getState());
		settings.setCacheLevel(cacheLevel.getState());
		settings.setDynamicResizing(resizeMode.getState());
		
	}
	
	public void load(){

		//extractMode.setState(settings.getExtractModeEnabled() ? 1 : 0);
		archiveMode.setState(settings.getArchiveModeIndex());
		cacheMode.setState(settings.getCacheModeIndex());
		cacheLevel.setState(settings.getCacheLevel());
		resizeMode.setState(settings.getDynamicResizing());
		
	}
		
}
