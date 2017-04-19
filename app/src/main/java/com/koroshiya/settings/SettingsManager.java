package com.koroshiya.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.koroshiya.R;
import com.koroshiya.archive.steppable.SteppableArchive;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.classes.Recent;
import com.koroshiya.settings.classes.Setting;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Class for managing all user settings.
 * Responsible for setting default settings, loading user settings from
 * disk, saving settings to disk, etc.
 * */
public abstract class SettingsManager {

    public static final int ZOOM_AUTO = 0;
    public static final int ZOOM_FULL = 1;
    public static final int ZOOM_SCALE_HEIGHT = 2;
    public static final int ZOOM_SCALE_WIDTH = 3;

    private SettingsManager(){
    }

    public static File getLastDirectory(Context c){

        String key = c.getString(R.string.st_last_directory);
        String value = Setting.getString(c, key);
        File tmp = null;

        if (value != null){
            tmp = new File(value);
        }

        if (tmp == null || !tmp.exists() || !tmp.isDirectory() || !tmp.canRead()){
            tmp = Environment.getExternalStorageDirectory();
        }

        return tmp;

    }

    private static SharedPreferences getPreferences(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

	public static void setBacklightAlwaysOn(Activity act, boolean alwaysOn){
        alwaysOn = alwaysOn && getPreferences(act).getBoolean(act.getString(R.string.st_backlight),Boolean.parseBoolean(act.getString(R.string.st_backlight_default)));
		try{
			int flag = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            Window w = act.getWindow();
			if (alwaysOn){
				w.addFlags(flag);
			}else{
				w.clearFlags(flag);
			}
		}catch (Exception | Error e){
			e.printStackTrace();
		}
	}

    private static void setFullScreen(Activity act, boolean enableFullscreen){
        enableFullscreen = enableFullscreen && getPreferences(act).getBoolean(act.getString(R.string.st_fullscreen), Boolean.parseBoolean(act.getString(R.string.st_fullscreen_default)));
        try{
            int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            Window w = act.getWindow();
            if (enableFullscreen){
                w.addFlags(flag);
            }else{
                w.clearFlags(flag);
            }
        }catch (Exception | Error e){
            e.printStackTrace();
        }
    }

    private static void setActionBarHidden(Activity act, boolean hideBar, boolean override){

        hideBar = override || (hideBar && getPreferences(act).getBoolean(act.getString(R.string.st_hideactionbar), Boolean.parseBoolean(act.getString(R.string.st_hideactionbar_default))));

        ActionBar actionBar = ((AppCompatActivity)act).getSupportActionBar();
        if (actionBar != null) {

            boolean showing = actionBar.isShowing();

            if (hideBar) {
                if (showing) {
                    actionBar.hide(); //TODO: add setting for this - What to do to go back? Leave back key?
                    //TODO: add setting to hide navigation menu, if soft menu, whereupon tap brings it up
                    //TODO: timeout for soft menu?
                }
            }else if (!showing){
                actionBar.show();
            }

        }
    }

    public static void setImmersiveMode(Activity act, boolean enabled){

        boolean setImmersive = enabled && getPreferences(act).getBoolean(act.getString(R.string.st_immersive), Boolean.parseBoolean(act.getString(R.string.st_immersive_default)));

        View decorView = act.getWindow().getDecorView();
        if (setImmersive) {

            int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                }

            }

            decorView.setSystemUiVisibility(flags); //TODO: on tap, show nav?
            setActionBarHidden(act, enabled, true);
        }else {
            setFullScreen(act, enabled);
            setActionBarHidden(act, enabled, false);
            if (!enabled){
                decorView.setSystemUiVisibility(0);
            }
        }
    }

    public static boolean isCacheOnStart(Context c){
        return getPreferences(c).getBoolean(c.getString(R.string.pref_cache_on_start), Boolean.parseBoolean(c.getString(R.string.pref_cache_on_start_default)));
    }

    public static boolean isCacheForRar(Context c){
        return getPreferences(c).getBoolean(c.getString(R.string.pref_cache_rar_files), Boolean.parseBoolean(c.getString(R.string.pref_cache_rar_files_default))); }
	
	public static boolean keepZoomOnPageChange(Context c){
        return getPreferences(c).getBoolean(c.getString(R.string.pref_keep_zoom_on_page_change), Boolean.parseBoolean(c.getString(R.string.pref_keep_zoom_on_page_change_default)));
	}

    public static int getZoom(Context c){
        return Integer.parseInt(getPreferences(c).getString(c.getString(R.string.pref_zoom_index), Integer.toString(ZOOM_AUTO)));
    }

    public static boolean getDynamicResizing(Context c){
        String key = c.getString(R.string.pref_dynamic_resizing);
        boolean defaultVal = Boolean.parseBoolean(c.getString(R.string.performance_setting_resize_default));
        return getPreferences(c).getBoolean(key, defaultVal);
    }

    public static int getMaxRecent(Context c){
        return Integer.parseInt(getPreferences(c).getString(c.getString(R.string.pref_max_recent), c.getString(R.string.general_setting_max_recent_default)));
    }




    public static void deleteRecent(Context c, Recent recent){
        SharedPreferences prefs = getPreferences(c);
        boolean deleteLessRecent = prefs.getBoolean(c.getString(R.string.pref_delete_less_recent), Boolean.parseBoolean(c.getString(R.string.pref_delete_less_recent_default)));
        boolean onlyDeleteFinishedChapters = prefs.getBoolean(c.getString(R.string.pref_only_delete_finished), Boolean.parseBoolean(c.getString(R.string.pref_only_delete_finished_default)));
        if (deleteLessRecent) {

            boolean canDelete = true;

            if (onlyDeleteFinishedChapters){
                int pageNumber = recent.getPageNumber() + 1; //So it's "length", not index
                int totalPages;
                String path = recent.getPath();
                File f = new File(path);
                if (ArchiveParser.isSupportedArchive(f)){
                    try {
                        SteppableArchive a = ArchiveParser.parseArchive(f, c);
                        totalPages = a.getTotalPages();
                    } catch (IOException e) {
                        e.printStackTrace();
                        totalPages = 0;
                    }
                }else if (ImageParser.isSupportedImage(f)){
                    totalPages = f.getParentFile().list(ImageParser.fnf).length;
                }else if (f.isDirectory()){
                    totalPages = f.getParentFile().list(ImageParser.fnf).length;
                }else{
                    totalPages = 0;
                }
                canDelete = totalPages <= pageNumber;
            }

            if (canDelete) {
                boolean onlyDeleteFiles = prefs.getBoolean(c.getString(R.string.pref_only_delete_folder_images), Boolean.parseBoolean(c.getString(R.string.pref_only_delete_folder_images_default)));
                File f = new File(recent.getPath());
                if (!f.isDirectory() || !onlyDeleteFiles) {
                    deleteFile(f);
                } else {
                    for (File file : f.listFiles()) {
                        if (file.isDirectory()) {
                            if (file.list().length == 0) {
                                deleteFile(file);
                            }
                        } else if (ImageParser.isSupportedImage(file)) {
                            deleteFile(file);
                        }
                    }
                    if (f.list().length == 0) {
                        deleteFile(f);
                    }
                }
            }
        }
        recent.delete(c);
    }

    public static void setLastDirectory(Context c, File lastDir){

        String key = c.getString(R.string.st_last_directory);
        Setting.insertOrUpdate(c, key, lastDir.getAbsolutePath());

    }

    private static void deleteFile(File cFile){
        if (!cFile.delete()){
            String msg = String.format(Locale.getDefault(), "Failed to delete file: %s", cFile.getAbsolutePath());
            Log.d("SettingsManager", msg);
        }
    }

}
