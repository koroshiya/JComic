package com.japanzai.koroshiya.settings;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Abstract class that Tabs encompassing user settings should extend.
 * */
public abstract class SettingTab extends SherlockFragment{
	
	@Override
	public void onPause() {
		
		save();
		super.onPause();
		
	}
	
	 @Override
	public void onResume() {
		 
		load();
		super.onResume();
		
	}
	
	@Override
	public void onStop() {
		
		save();
		super.onStop();
	
	}
	
	@Override
	public void onDestroy() {
	 
		save();
		super.onDestroy();
		
	}
	
	/**
	 * Saves the states of current user settings
	 * */
	public abstract void save();
	
	/**
	 * Loads the user settings and updates the UI to reflect the states
	 * */
	public abstract void load();
	
}
