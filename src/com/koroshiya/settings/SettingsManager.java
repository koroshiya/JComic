package com.koroshiya.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.koroshiya.R;
import com.koroshiya.archive.steppable.SteppableArchive;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.classes.Recent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Class for managing all user settings.
 * Responsible for setting default settings, loading user settings from
 * disk, saving settings to disk, etc.
 * */
public abstract class SettingsManager {

	private static final ArrayList<Recent> recentAndFavorite = new ArrayList<>();

    private static final String CACHE_FILE = "cache.json";

    public static final int ZOOM_AUTO = 0;
    public static final int ZOOM_FULL = 1;
    public static final int ZOOM_SCALE_HEIGHT = 2;
    public static final int ZOOM_SCALE_WIDTH = 3;

    private SettingsManager(){
    }

    private static JSONObject getJSONCache(Context c){

        File cFile = new File(c.getCacheDir(), CACHE_FILE);

        if (cFile.exists() && cFile.length() > 0){
            try {
                FileInputStream fis = new FileInputStream(cFile);
                FileChannel fc = fis.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                String jsonStr = Charset.defaultCharset().decode(bb).toString();

                //Log.i("SM", new String(jsonStr));
                return new JSONObject(jsonStr);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                cFile.delete();
            }

        }

        return new JSONObject();

    }

    private static void writeJSONCache(Context c, JSONObject jsonObject){

        File rFile = new File(c.getCacheDir(), CACHE_FILE);

        try {
            FileOutputStream fos = new FileOutputStream(rFile);
            fos.write(jsonObject.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static File getLastDirectory(Context c){

        JSONObject jsonObject = getJSONCache(c);
        File tmp = null;

        if (jsonObject != null){
            try {
                String key = c.getString(R.string.st_last_directory);
                if (jsonObject.has(key)){
                    tmp = new File(jsonObject.getString(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (tmp == null || !tmp.exists() || !tmp.isDirectory() || !tmp.canRead()){
            tmp = Environment.getExternalStorageDirectory();
        }

        return tmp;

    }

    private static void fillRecent(Context c){

        JSONObject root = getJSONCache(c);

        if (root != null){
            try {
                if (root.has("recent")) {
                    JSONArray jsonArray = root.getJSONArray("recent");
                    int len = jsonArray.length();
                    for (int i = 0; i < len; i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Recent r = new Recent(jsonObject);
                            recentAndFavorite.add(r);
                        } catch (JSONException jse) {
                            jse.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayList<Long> uuids = new ArrayList<>();
            int totalRecents = recentAndFavorite.size();
            Recent r;
            File f;
            boolean found;

            for (int i = totalRecents - 1; i >= 0; i--){
                r = recentAndFavorite.get(i);
                f = new File(r.getPath());
                if (!ImageParser.isSupportedFile(f)){
                    recentAndFavorite.remove(i);
                }else{
                    uuids.add(r.getUuid());
                }
            }

            for (File file : c.getCacheDir().listFiles(ImageParser.fnf)){
                if (file.getName().endsWith(".webp")){
                    found = false;
                    for (long i : uuids){
                        if (file.getName().equals(Long.toString(i) + ".webp")){
                            found = true;
                            break;
                        }
                    }
                    if (!found) file.delete();
                }
            }

        }
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

    public static void setFullScreen(Activity act, boolean enableFullscreen){
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

    public static void setActionBarHidden(Activity act, boolean hideBar){

        hideBar = hideBar && getPreferences(act).getBoolean(act.getString(R.string.st_hideactionbar), Boolean.parseBoolean(act.getString(R.string.st_hideactionbar_default)));

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

    public static void setImmersiveMode(Activity act, boolean setImmersive){

        setImmersive = setImmersive && getPreferences(act).getBoolean(act.getString(R.string.st_immersive), Boolean.parseBoolean(act.getString(R.string.st_immersive_default)));

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
        }else{
            decorView.setSystemUiVisibility(0);
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




    public static void addRecentAndFavorite(Context c, Recent r){

        boolean isRecent = isRecent(r);
        int totalRecent = 0;
        int max = getMaxRecent(c);

        for (int i = recentAndFavorite.size() - 1; i >= 0; i--){
            Recent recent = recentAndFavorite.get(i);
            if (isRecent(recent) == isRecent && recent.getPath().equals(r.getPath())){
                recentAndFavorite.remove(i);
            }else if (isRecent(recent)){
                if (totalRecent < max)
                    totalRecent++;
                else
                    deleteRecent(i, c, recent);
            }
        }
        recentAndFavorite.add(0, r);

        saveRecentList(c);

    }

    private static void deleteRecent(int i, Context c, Recent recent){
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
                if (ArchiveParser.isSupportedArchive(path)){
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
                    f.delete();
                } else {
                    for (File file : f.listFiles()) {
                        if (file.isDirectory()) {
                            if (file.list().length == 0) {
                                file.delete();
                            }
                        } else if (ImageParser.isSupportedImage(file)) {
                            file.delete();
                        }
                    }
                    if (f.list().length == 0) {
                        f.delete();
                    }
                }
            }
        }
        recentAndFavorite.remove(i);
    }

    public static ArrayList<Recent> getRecentAndFavorites(Context c, boolean isRecent){

        int totalRecents = recentAndFavorite.size();
        ArrayList<Recent> data = new ArrayList<>();
        Recent r;
        File f;

        for (int i = totalRecents - 1; i >= 0; i--){
            r = recentAndFavorite.get(i);
            f = new File(r.getPath());
            if (!ImageParser.isSupportedFile(f)){
                recentAndFavorite.remove(i);
            }else{
                if (r.getPageNumber() >= 0 == isRecent){
                    data.add(r);
                }
            }
        }

        if (totalRecents != data.size()){
            saveRecentList(c);
        }

        return data;
    }

    public static Recent getRecentAndFavorite(Context c, String path, boolean isRecent){
        if (recentAndFavorite.size() == 0){
            fillRecent(c);
        }
        for (Recent r : recentAndFavorite){
            if (isRecent(r) == isRecent && r.getPath().equals(path)){
                return r;
            }
        }
        return null;
    }

    /**
     * Determine whether r is a recently read comic, or a favorite.
     * */
    private static boolean isRecent(Recent r){
        return r.getPageNumber() >= 0;
    }
	
	private static void saveRecentList(Context c){

        JSONObject jsonObject = getJSONCache(c);

        String key = c.getString(R.string.st_recent);
        if (jsonObject.has(key)){
            jsonObject.remove(key);
        }

        if (recentAndFavorite.size() > 0){

            JSONArray arr = new JSONArray();
            for (Recent r : recentAndFavorite) {
                try {
                    arr.put(r.toJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                jsonObject.put(key, arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        writeJSONCache(c, jsonObject);

	}

    public static void setLastDirectory(Context c, File lastDir){

        JSONObject obj = getJSONCache(c);
        String key = c.getString(R.string.st_last_directory);

        if (obj.has(key)) {
            obj.remove(key);
        }

        try {
            obj.put(key, lastDir.getAbsolutePath());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        writeJSONCache(c, obj);

    }
	
}
