package com.japanzai.koroshiya.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.classes.Recent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for managing all user settings.
 * Responsible for setting default settings, loading user settings from
 * disk, saving settings to disk, etc.
 * */
public class SettingsManager {
	
	/*TODO:
	 * Other long press/menu options, like zoom
	 *Double swipe to go to first/last //setting for this instead of pinch-zoom?

		Settings:
		Default location for reading
		fit to height/width, scale, etc. - make wide images fit to height
		Smartscale? (zoom out to certain degree depending on page size)
		
	 * 
	 *
	 */

    private final String defaultHomeDir;

	private final ArrayList<Recent> recentAndFavorite = new ArrayList<>();
	private final SharedPreferences preferences;
    private final File cacheDir;

    private static final String RECENT_FILE = "recent.json";

    public static final int ZOOM_AUTO = 0;
    public static final int ZOOM_FULL = 1;
    public static final int ZOOM_SCALE_HEIGHT = 2;
    public static final int ZOOM_SCALE_WIDTH = 3;

    public SettingsManager(Context context, boolean fillRecent){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        File home = Environment.getExternalStorageDirectory();
        if (home == null){
            String userHome = System.getProperty("user.home");
            home = new File(userHome);
        }
        defaultHomeDir = home.getAbsolutePath();
        cacheDir = context.getCacheDir();
        if (fillRecent) fillRecent();
    }

    private void fillRecent(){
        File rFile = new File(cacheDir, RECENT_FILE);

        if (rFile.exists()){
            try {
                FileInputStream fis = new FileInputStream(rFile);
                FileChannel fc = fis.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                String jsonStr = Charset.defaultCharset().decode(bb).toString();

                //Log.i("SM", new String(jsonStr));
                JSONArray jsonArray = new JSONArray(jsonStr);
                int len = jsonArray.length();
                for (int i = 0; i < len; i++){
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Recent r = new Recent(jsonObject);
                        recentAndFavorite.add(r);
                    }catch(JSONException jse){
                        jse.printStackTrace();
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                rFile.delete();
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

            for (File file : cacheDir.listFiles(ImageParser.fnf)){
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
	
	public int getOrientation(){
        return preferences.getInt("orientationIndex", 0);
	}
	
	public void forceOrientation(Activity act){

        int orientation = ActivityInfo.SCREEN_ORIENTATION_USER;
		
		switch(getOrientation()){
			case 1:
                if (android.os.Build.VERSION.SDK_INT >= 9) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
                }else{
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                }
				break;
			case 2:
				if (android.os.Build.VERSION.SDK_INT >= 9) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
				}else{
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				}
				break;
		}

        act.setRequestedOrientation(orientation);
		
	}
	
	public int getArchiveModeIndex(){
        return preferences.getInt("archiveModeIndex", 0);
	}

	public boolean isContextMenuEnabled(){
        return preferences.getBoolean("contextMenuEnabled", true);
	}
	
	public void setBacklightAlwaysOn(Activity act, boolean alwaysOn){
        alwaysOn = alwaysOn && preferences.getBoolean(act.getString(R.string.st_backlight), false);
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

    public void setFullScreen(Activity act, boolean enableFullscreen){
        enableFullscreen = enableFullscreen && preferences.getBoolean(act.getString(R.string.st_fullscreen), false);
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

    public boolean isCacheOnStart(){
        return preferences.getBoolean("cacheOnStart", true);
    }

    public boolean isCacheForRar(){
        return preferences.getBoolean("cacheRarFiles", false); }
	
	public boolean keepZoomOnPageChange(){
        return preferences.getBoolean("keepZoomOnPageChange", false);
	}

    public int getZoom(){
        return Integer.parseInt(preferences.getString("zoomIndex", Integer.toString(ZOOM_AUTO)));
    }

    public int getDynamicResizing(){
        return Integer.parseInt(preferences.getString("dynamicResizing", "2"));
    }

    public int getMaxRecent(){
        return Integer.parseInt(preferences.getString("maxRecent", "10"));
    }

	public void setHomeDir(String path){
		updateString("homeDir", path);
	}
	
	public File getHomeDir(){
		return new File(preferences.getString("homeDir", defaultHomeDir));
	}




    public void addRecentAndFavorite(Recent r){

        boolean isRecent = isRecent(r);

        for (int i = recentAndFavorite.size() - 1; i >= 0; i--){
            Recent recent = recentAndFavorite.get(i);
            if (isRecent(recent) == isRecent && recent.getPath().equals(r.getPath())){
                recentAndFavorite.remove(i);
            }
        }
        recentAndFavorite.add(0, r);

        saveRecentList();

    }

    public ArrayList<Recent> getRecentAndFavorites(boolean isRecent){

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
            saveRecentList();
        }

        return data;
    }

    public Recent getRecentAndFavorite(String path, boolean isRecent){
        for (Recent r : recentAndFavorite){
            if (isRecent(r) == isRecent && r.getPath().equals(path)){
                return r;
            }
        }
        return null;
    }

    private static boolean isRecent(Recent r){
        return r.getPageNumber() >= 0;
    }
	
	private void saveRecentList(){

        JSONArray arr = new JSONArray();
        for (Recent r : recentAndFavorite){
            try {
                arr.put(r.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        File rFile = new File(cacheDir, RECENT_FILE);
        try {
            FileOutputStream fos = new FileOutputStream(rFile);
            fos.write(arr.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

	}





    private void updateString(String key, String value){
        Editor edit = preferences.edit();
        edit.putString(key, value).apply();
    }

	private void updateBool(String key, boolean value){
		Editor edit = preferences.edit();
		edit.putBoolean(key, value).apply();
	}
	
}
