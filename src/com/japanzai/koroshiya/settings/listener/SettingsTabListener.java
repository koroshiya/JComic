package com.japanzai.koroshiya.settings.listener;

import com.actionbarsherlock.app.ActionBar; 
import com.actionbarsherlock.app.ActionBar.Tab;
import com.japanzai.koroshiya.settings.AdvancedSettings;
import com.japanzai.koroshiya.settings.GeneralSettings;
import com.japanzai.koroshiya.settings.PerformanceSettings;
import com.japanzai.koroshiya.settings.SettingsView;

import android.support.v4.app.Fragment; 
import android.support.v4.app.FragmentTransaction;

/**
 * Listener for settings tabs. 
 * Used to swap between different categories of settings.
 * */
public class SettingsTabListener implements ActionBar.TabListener{
	
	private final SettingsView parent;
	
	public SettingsTabListener(SettingsView settingsView) {
		
		this.parent = settingsView;
		
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		
		Fragment frag;
		
		if (tab.getPosition() == 0){
			frag = new GeneralSettings(parent);
		}else if (tab.getPosition() == 1) {
			frag = new PerformanceSettings(parent);
		}else {
			frag = new AdvancedSettings(parent);
		}
		
		ft.replace(android.R.id.content, frag);
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
	
}
