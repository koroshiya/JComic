package com.japanzai.koroshiya.settings;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.controls.CheckSetting;
import com.japanzai.koroshiya.settings.controls.SpinnerSetting;
import com.japanzai.koroshiya.settings.listener.ResetListener;

import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Encompasses basic user settings.
 * */
public class GeneralSettings extends SettingTab {
	
	private final SettingsView parent;
	private final SettingsManager settings;
	
	private CheckSetting loop;
	private CheckSetting previousSession;
	private CheckSetting recent;
	private CheckSetting backlightOn;
	private CheckSetting keepZoom;
	private CheckSetting contextMenu;
	private CheckSetting backToFileChooser;
	private SpinnerSetting defaultZoom;
	private SpinnerSetting orientation;
	private SpinnerSetting doubleTap;
	
	public GeneralSettings(SettingsView parent) {
		
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
			loop = new CheckSetting(getString(R.string.setting_loop), settings.isLoopModeEnabled(), true, this.parent);
			lLayout.addView(loop);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			loop = null;
		}
		
		try{
			previousSession = new CheckSetting(getString(R.string.setting_session), settings.saveSession(), true, this.parent);
			lLayout.addView(previousSession);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			previousSession = null;
		}
		
		try{
			recent = new CheckSetting(getString(R.string.setting_recent), settings.saveRecent(), true, this.parent);
			lLayout.addView(recent);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			recent = null;
		}
		
		try{
			backlightOn = new CheckSetting(getString(R.string.setting_backlight), settings.isBacklightAlwaysOn(), true, this.parent);
			lLayout.addView(backlightOn);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			backlightOn = null;
		}
		
		try{
			keepZoom = new CheckSetting(getString(R.string.setting_keep_zoom_page_change), settings.keepZoomOnPageChange(), true, this.parent);
			lLayout.addView(keepZoom);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			keepZoom = null;
		}
		
		try{
			contextMenu = new CheckSetting(getString(R.string.setting_context_menu), settings.isContextMenuEnabled(), true, this.parent);
			lLayout.addView(contextMenu);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			contextMenu = null;
		}
		
		try{
			backToFileChooser = new CheckSetting(getString(R.string.setting_back_to_file_chooser), settings.isBackToFileChooser(), true, this.parent);
			lLayout.addView(backToFileChooser);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			backToFileChooser = null;
		}
		
		try{
			defaultZoom = new SpinnerSetting(this.parent, R.string.setting_zoom, R.array.general_setting_default_zoom, settings.getZoom());
			lLayout.addView(defaultZoom);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			defaultZoom = null;
		}
		
		try{
			orientation = new SpinnerSetting(this.parent, R.string.setting_orientation, R.array.general_setting_orientation, settings.getOrientation());
			lLayout.addView(orientation);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			orientation = null;
		}
		
		try{
			doubleTap = new SpinnerSetting(this.parent, R.string.setting_double_tap, R.array.array_double_tap, settings.getDoubleTapIndex());
			lLayout.addView(doubleTap);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			doubleTap = null;
		}
		
		try{
			Button resetAll = new Button(this.parent);
			resetAll.setText(getString(R.string.setting_reset));
			resetAll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			resetAll.setOnClickListener(new ResetListener(this));
			lLayout.addView(resetAll);
		}catch (NotFoundException nfe){
			nfe.printStackTrace();
			doubleTap = null;
		}

		lLayout.invalidate();
		this.parent.findViewById(R.id.tabGeneralSettingsScrollView).invalidate();
		
	}
	
	public void save(){

		if (loop != null){settings.setLoopMode(loop.getState() == 1);}
		if (previousSession != null){settings.setSaveSession(previousSession.getState() == 1);}
		if (recent != null){settings.setSaveRecent(recent.getState() == 1);}
		if (backlightOn != null){settings.setBacklightAlwaysOn(backlightOn.getState() == 1);}
		if (keepZoom != null){settings.setKeepZoomOnPageChange(keepZoom.getState() == 1);}
		if (contextMenu != null){settings.setContextMenuEnabled(contextMenu.getState() == 1);}
		if (backToFileChooser != null){settings.setBackToFileChooserEnabled(backToFileChooser.getState() == 1);}
		if (defaultZoom != null){settings.setZoomIndex(defaultZoom.getState());}
		if (orientation != null){settings.setOrientationIndex(orientation.getState());}
		if (doubleTap != null){settings.setDoubleTapIndex(doubleTap.getState());}
		
	}
	
	public void load(){

		if (loop != null){loop.setState(settings.isLoopModeEnabled() ? 1 : 0);}
		if (previousSession != null){previousSession.setState(settings.saveSession() ? 1 : 0);}
		if (recent != null){recent.setState(settings.saveRecent() ? 1 : 0);}
		if (backlightOn != null){backlightOn.setState(settings.isBacklightAlwaysOn() ? 1 : 0);}
		if (keepZoom != null){keepZoom.setState(settings.keepZoomOnPageChange() ? 1 : 0);}
		if (contextMenu != null){contextMenu.setState(settings.isContextMenuEnabled() ? 1 : 0);}
		if (backToFileChooser != null){backToFileChooser.setState(settings.isBackToFileChooser() ? 1 : 0);}
		if (defaultZoom != null){defaultZoom.setState(settings.getZoom());}
		if (orientation != null){orientation.setState(settings.getOrientation());}
		if (doubleTap != null){doubleTap.setState(settings.getDoubleTapIndex());}
		
	}
	
}
