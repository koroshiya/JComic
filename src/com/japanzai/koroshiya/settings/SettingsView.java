package com.japanzai.koroshiya.settings;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.listener.SettingsTabListener;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Activity for displaying different settings tabs
 * */
public class SettingsView extends SherlockFragmentActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    SettingsManager.setFullScreen(this);
	    setContentView(R.layout.general_settings);
	    MainActivity.mainActivity.getSettings().keepBacklightOn(getWindow());
	    instantiate();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	//Used as a workaround for a bug with Android 2.x and ActionBarSherlock
    	//The problem renders the tabs unusable on orientation change, so we don't want the orientation
    	//to change if the user is susceptible.
    	//Android 3.x is untested, so, until it has been confirmed to work, Android 3.x will be
    	//treated in the same manner.
    	if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    		this.setRequestedOrientation(get2pointxScreenOrientation(getWindowManager()));
    	} else {
    		MainActivity.mainActivity.getSettings().forceOrientation(this);
    	}
    	
    }
    
    /**
     * Retrieves the current screen rotation; needed for the Android 2.x workaround in onResume()
     * */
    public static int get2pointxScreenOrientation(WindowManager wm) {
        int rotation = wm.getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
            (rotation == Surface.ROTATION_90
                || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e("SettingsView", "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;              
            }
        } else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e("SettingsView", "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;              
            }
        }

        return orientation;
    }
	
    public void instantiate(){
    	        
        ActionBar bar = getSupportActionBar();
        bar.removeAllTabs();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab tab1 = setTab(getString(R.string.setting_general), bar);
        ActionBar.Tab tab2 = setTab(getString(R.string.setting_performance), bar);
        ActionBar.Tab tab3 = setTab(getString(R.string.setting_pro), bar);
        bar.addTab(tab1);
        bar.addTab(tab2);
        bar.addTab(tab3);
        bar.setDisplayShowTitleEnabled(true);
    	        
    }
    
    /**
     * @param text Text to display on the tab being set
     * @param bar ActionBar to attach the created tab to
     * @return Returns the tab created by this method
     * */
    private ActionBar.Tab setTab(String text, ActionBar bar){
    	ActionBar.Tab tab = bar.newTab();
    	tab.setText(text);
    	tab.setTabListener(new SettingsTabListener(this));
    	return tab;
    }
	
}
