package com.japanzai.koroshiya.settings;

import com.japanzai.koroshiya.DrawerActivity;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.dialog.ConfirmDialog;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.controls.CheckSetting;
import com.japanzai.koroshiya.settings.controls.SpinnerSetting;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Activity for displaying different settings tabs
 * */
public class SettingsView extends DrawerActivity implements ModalReturn {

    protected SettingsManager settings;
    protected LinearLayout lLayout;

    private static final int GENERAL = 1;
    private static final int PERFORMANCE = 2;
    private static final int ADVANCED = 3;
    private int type = GENERAL;

    //General settings
    private CheckSetting loop;
    private CheckSetting swipeToNext;
    private CheckSetting previousSession;
    private CheckSetting recent;
    private CheckSetting backlightOn;
    private CheckSetting keepZoom;
    private CheckSetting contextMenu;
    private SpinnerSetting defaultZoom;
    private SpinnerSetting orientation;
    private SpinnerSetting doubleTap;

    //Performance settings
    private CheckSetting cacheOnStartup;
    private CheckSetting cacheRarFiles;
    private SpinnerSetting archiveMode;
    private SpinnerSetting cacheMode;
    private SpinnerSetting cacheLevel;
    private SpinnerSetting resizeMode;

    //Advanced settings
    private SpinnerSetting recursion;

	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    SettingsManager.setFullScreen(this);
	    setContentView(R.layout.general_settings);
	    MainActivity.getMainActivity().getSettings().keepBacklightOn(getWindow());
        this.settings = MainActivity.getMainActivity().getSettings();
        instantiateParent();
        instantiate();
    }
    
    @Override
    public void onResume(){
        load();
    	super.onResume();
        MainActivity.getMainActivity().getSettings().forceOrientation(this);
    }
    
    /**
     * Retrieves the current screen rotation; needed for the Android 2.x workaround in onResume()
     * */
    public static int get2pointxScreenOrientation(WindowManager wm) {
        int rotation = android.os.Build.VERSION.SDK_INT >= 8 ? wm.getDefaultDisplay().getRotation() : wm.getDefaultDisplay().getOrientation();
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
            (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    return android.os.Build.VERSION.SDK_INT >= 9 ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_270:
                    return android.os.Build.VERSION.SDK_INT >= 9 ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    Log.e("SettingsView", "Unknown screen orientation. Defaulting to portrait.");
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    return android.os.Build.VERSION.SDK_INT >= 9 ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_270:
                    return android.os.Build.VERSION.SDK_INT >= 9 ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                default:
                    Log.e("SettingsView", "Unknown screen orientation. Defaulting to landscape.");
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        }

    }
	
    public void instantiateParent(){

        lLayout = (LinearLayout) this.findViewById(R.id.tabGeneralSettings);

        final String[] items = new String[]{
                getString(R.string.back),
                getString(R.string.setting_general),
                getString(R.string.setting_performance),
                getString(R.string.setting_pro)
        };

        final ModalReturn mr = this;

        (findViewById(R.id.btn_reset)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm(getString(R.string.setting_reset_prompt), mr);
            }
        });

        findViewById(R.id.btn_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instantiateDrawer(items);
            }
        });
    	        
    }

    /**
     * @param message Actual prompt/question to be displayed
     * @param target ModalReturn to target with the prompt created
     **/
    public void confirm(String message, ModalReturn target){
        ConfirmDialog confirm = new ConfirmDialog(getString(R.string.file_confirm),
                getString(R.string.file_deny),
                message,
                target);
        confirm.show(getSupportFragmentManager(), "GeneralSettings");
    }

    @Override
    public void accept(){
        MainActivity.getMainActivity().getSettings().restoreDefaultSettings();
        load();
    }

    public void save(){

        //General settings
        if (loop != null){settings.setLoopMode(loop.getState() == 1);}
        if (swipeToNext != null){settings.setSwipeToNextMode(swipeToNext.getState() == 1);}
        if (previousSession != null){settings.setSaveSession(previousSession.getState() == 1);}
        if (recent != null){settings.setSaveRecent(recent.getState() == 1);}
        if (backlightOn != null){settings.setBacklightAlwaysOn(backlightOn.getState() == 1);}
        if (keepZoom != null){settings.setKeepZoomOnPageChange(keepZoom.getState() == 1);}
        if (contextMenu != null){settings.setContextMenuEnabled(contextMenu.getState() == 1);}
        if (defaultZoom != null){settings.setZoomIndex(defaultZoom.getState());}
        if (orientation != null){settings.setOrientationIndex(orientation.getState());}
        if (doubleTap != null){settings.setDoubleTapIndex(doubleTap.getState());}

        //Performance settings
        if (cacheOnStartup != null) settings.setCacheOnStart(cacheOnStartup.getState() == 1);
        if (cacheRarFiles != null) settings.setCacheForRar(cacheRarFiles.getState() == 1);
        if (archiveMode != null){settings.setArchiveModeIndex(archiveMode.getState());}
        if (cacheMode != null){settings.setCacheModeIndex(cacheMode.getState());}
        if (cacheLevel != null){settings.setCacheLevel(cacheLevel.getState());}
        if (resizeMode != null){settings.setDynamicResizing(resizeMode.getState());}

        //Advanced settings
        if (recursion != null){settings.setRecursionLevel(recursion.getState());}

    }

    public void load(){

        //General settings
        if (loop != null){loop.setState(settings.isLoopModeEnabled() ? 1 : 0);}
        if (swipeToNext != null){swipeToNext.setState(settings.isSwipeToNextModeEnabled() ? 1 : 0);}
        if (previousSession != null){previousSession.setState(settings.saveSession() ? 1 : 0);}
        if (recent != null){recent.setState(settings.saveRecent() ? 1 : 0);}
        if (backlightOn != null){backlightOn.setState(settings.isBacklightAlwaysOn() ? 1 : 0);}
        if (keepZoom != null){keepZoom.setState(settings.keepZoomOnPageChange() ? 1 : 0);}
        if (contextMenu != null){contextMenu.setState(settings.isContextMenuEnabled() ? 1 : 0);}
        if (defaultZoom != null){defaultZoom.setState(settings.getZoom());}
        if (orientation != null){orientation.setState(settings.getOrientation());}
        if (doubleTap != null){doubleTap.setState(settings.getDoubleTapIndex());}

        //Performance settings
        if (cacheOnStartup != null) cacheOnStartup.setState(settings.isCacheOnStart() ? 1 : 0);
        if (cacheRarFiles != null) cacheRarFiles.setState(settings.isCacheForRar() ? 1 : 0);
        if (archiveMode != null){archiveMode.setState(settings.getArchiveModeIndex());}
        if (cacheMode != null){cacheMode.setState(settings.getCacheModeIndex());}
        if (cacheLevel != null){cacheLevel.setState(settings.getCacheLevel());}
        if (resizeMode != null){resizeMode.setState(settings.getDynamicResizing());}

        //Advanced settings
        if (recursion != null){recursion.setState(settings.getRecursionLevel());}

    }

    @Override
    public void onPause() {

        save();
        super.onPause();

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

    @Override
    public void decline(){}

    public void instantiate(){

        try{
            loop = new CheckSetting(getString(R.string.setting_loop), settings.isLoopModeEnabled(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            loop = null;
        }

        try{
            swipeToNext = new CheckSetting(getString(R.string.setting_swipe_next), settings.isSwipeToNextModeEnabled(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            swipeToNext = null;
        }

        try{
            previousSession = new CheckSetting(getString(R.string.setting_session), settings.saveSession(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            previousSession = null;
        }

        try{
            recent = new CheckSetting(getString(R.string.setting_recent), settings.saveRecent(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            recent = null;
        }

        try{
            backlightOn = new CheckSetting(getString(R.string.setting_backlight), settings.isBacklightAlwaysOn(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            backlightOn = null;
        }

        try{
            keepZoom = new CheckSetting(getString(R.string.setting_keep_zoom_page_change), settings.keepZoomOnPageChange(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            keepZoom = null;
        }

        try{
            contextMenu = new CheckSetting(getString(R.string.setting_context_menu), settings.isContextMenuEnabled(), this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            contextMenu = null;
        }

        try{
            defaultZoom = new SpinnerSetting(this, R.string.setting_zoom, R.array.general_setting_default_zoom, settings.getZoom());
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            defaultZoom = null;
        }

        try{
            orientation = new SpinnerSetting(this, R.string.setting_orientation, R.array.general_setting_orientation, settings.getOrientation());
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            orientation = null;
        }

        try{
            doubleTap = new SpinnerSetting(this, R.string.setting_double_tap, R.array.array_double_tap, settings.getDoubleTapIndex());
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            doubleTap = null;
        }

        try{
            cacheOnStartup = new CheckSetting(R.string.setting_cache_on_startup, this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            cacheOnStartup = null;
        }

        try{
            cacheRarFiles = new CheckSetting(R.string.setting_cache_rar, this);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            cacheRarFiles = null;
        }

        try{
            archiveMode = new SpinnerSetting(this, R.string.setting_archive, R.array.performance_setting_archive_mode, 0);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            archiveMode = null;
        }

        try{
            cacheMode = new SpinnerSetting(this, R.string.setting_cache, R.array.performance_setting_cache_mode, 0);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            cacheMode = null;
        }

        try{
            cacheLevel = new SpinnerSetting(this, R.string.advanced_setting_cache_level, R.array.advanced_setting_cache, 2);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            cacheLevel = null;
        }

        try{
            resizeMode = new SpinnerSetting(this, R.string.setting_resize, R.array.performance_setting_resize_mode, 0);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            resizeMode = null;
        }

        try{
            recursion = new SpinnerSetting(this, R.string.advanced_setting_recursion_level, R.array.advanced_setting_recursion, 0);
        }catch (Resources.NotFoundException nfe){
            nfe.printStackTrace();
            recursion = null;
        }

        reinstantiate();

    }

    private void reinstantiate(){

        lLayout.removeAllViews();

        if (type == PERFORMANCE){
            lLayout.addView(cacheOnStartup);
            lLayout.addView(cacheRarFiles);
            TextView tv = new TextView(this);
            tv.setText(getString(R.string.setting_cache_rar_warning));
            lLayout.addView(tv);
            lLayout.addView(archiveMode);
            lLayout.addView(cacheMode);
            lLayout.addView(cacheLevel);
            lLayout.addView(resizeMode);
        }else if (type == ADVANCED) {
            lLayout.addView(recursion);
        }else{
            lLayout.addView(loop);
            lLayout.addView(swipeToNext);
            lLayout.addView(previousSession);
            lLayout.addView(recent);
            lLayout.addView(backlightOn);
            lLayout.addView(keepZoom);
            lLayout.addView(contextMenu);
            lLayout.addView(defaultZoom);
            lLayout.addView(orientation);
            lLayout.addView(doubleTap);
        }

        lLayout.invalidate();
        this.findViewById(R.id.tabGeneralSettingsScrollView).invalidate();

    }

    public void drawerClick(int i){
        switch (i){
            case 0:
                finish();
                return;
            case 1:
                this.type = GENERAL;
                break;
            case 2:
                this.type = PERFORMANCE;
                break;
            default:
                this.type = ADVANCED;
                break;
        }
        reinstantiate();
        //mDrawerLayout.closeDrawer(android.os.Build.VERSION.SDK_INT >= 14 ? Gravity.START : Gravity.LEFT);
    }
	
}
