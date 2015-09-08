package com.japanzai.koroshiya.settings;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.classes.Recent;

import org.json.JSONArray;
import org.json.JSONException;

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

	private final ArrayList<Recent> recent = new ArrayList<>();
	private final ArrayList<String> favorite = new ArrayList<>();
	private final SharedPreferences preferences;
	
	public final static int RECURSION_ALL = 3;

	public static final double AUTO_SIZE = -1d;
	public static final double SCALE_HEIGHT_SIZE = -2d;
	public static final double SCALE_WIDTH_SIZE = -3d;
	public static final double QUARTER_SIZE = 0.25d;
	public static final double HALF_SIZE = 0.5d;
	public static final double TWO_THIRD_SIZE = 0.67d;
	public static final double THREE_QUARTER_SIZE = 0.75d;
	public static final double FULL_SIZE = 1d;
	public static final double FULL_SIZE_HALF = 1.5d;
	public static final double DOUBLE_SIZE = 2d;
	
	
	public SettingsManager(Context context){
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		File home = Environment.getExternalStorageDirectory();
		if (home == null){
			String userHome = System.getProperty("user.home");
			home = new File(userHome);
		}
		defaultHomeDir = home.getAbsolutePath();
		initialize();
	}
	
	public void initialize(){

        fillRecentOld(); //For compatibility with old versions
        fillFavoriteOld();  //For compatibility with old versions
        fillRecent();
        fillFavorite();
		
	}

    private void fillRecent(){
        String json = preferences.getString("recent", "[]");
        try {
            JSONArray js = new JSONArray(json);
            for (int i = 0; i < js.length(); i++) recent.add(Recent.fromString(js.getString(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fillFavorite(){
        String json = preferences.getString("favorite", "[]");
        try {
            JSONArray js = new JSONArray(json);
            for (int i = 0; i < js.length(); i++) favorite.add(js.getString(i));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
	
	@Deprecated
	private void fillFavoriteOld(){

		favorite.clear();
		String s;
        Editor editor = preferences.edit();

		for (int i = 1; i <= 10; i++){
			s = preferences.getString("favorite" + i, "");
            editor.remove("favorite" + i);
			if (s.equals("")){
				break;
			}
			favorite.add(s);
		}

        editor.commit();
		
	}

    @Deprecated
	private void fillRecentOld(){
		
		recent.clear();
		String s;
		int page;
        Editor editor = preferences.edit();
		
		for (int i = 1; i <= 10; i++){
			s = preferences.getString("recent" + i, "");
            editor.remove("recent" + i);
			if (s.equals("")){
				break;
			}
			page = preferences.getInt("recentPage" + i, -1);
            editor.remove("recentPage" + i);
			if (page < 0){
				break;
			}
			recent.add(new Recent(s, page));
		}

        editor.commit();
		
	}
	
	/**
	 * Restores all user settings to their default values.
	 * Does not include history settings, like favorites or recent.
	 * */
	public void restoreDefaultSettings(){
		setHomeDir(System.getProperty("user.home"));
	}

    /**
     * @return If loop mode is enabled, returns true.
     * 			Otherwise, false.
     * */
    public boolean isLoopModeEnabled(){
        return preferences.getBoolean("loopMode", false);
    }

    /**
     * @param loop Boolean indicating whether to enable loop mode or not.
     * */
    public void setLoopMode(boolean loop){
        updateBool("loopMode", loop);
    }

    /**
     * @return If swipe to next mode is enabled, returns true.
     * 			Otherwise, false.
     * */
    public boolean isSwipeToNextModeEnabled(){
        return preferences.getBoolean("swipeToNextMode", true);
    }

    /**
     * @param next Boolean indicating whether to enable swipe to next mode or not.
     * */
    public void setSwipeToNextMode(boolean next){
        updateBool("swipeToNextMode", next);
    }
	
	/**
	 * @return If user has set for their viewing session to be saved,
	 * 			returns true. Otherwise, false.
	 * */
	public boolean saveSession(){
        return preferences.getBoolean("saveSession", true);
	}
	
	/**
	 * @param save Boolean indicating whether to save user viewing sessions or not.
	 * */
	public void setSaveSession(boolean save){
		updateBool("saveSession", save);
	}
	
	public int getDoubleTapIndex(){
        return preferences.getInt("doubleTapIndex", 0);
	}
	
	public void setDoubleTapIndex(int index){
		updateInt("doubleTapIndex", index);
	}
	
	public boolean saveRecent(){
        return preferences.getBoolean("saveRecent", true);
	}
	
	public void setSaveRecent(boolean save){
		updateBool("saveRecent", save);
	}
	
	public int getZoom(){
        return preferences.getInt("zoomIndex", 9);
	}
	
	public void setZoomIndex(int index){
		updateInt("zoomIndex", index);
	}
	
	public double getCurrentZoomRatio(){
        return getZoomRatio(getZoom());
	}
	
	public static double getZoomRatio(int index){
		switch (index){
			case 0:
				return AUTO_SIZE;
			case 1:
				return QUARTER_SIZE;
			case 2:
				return HALF_SIZE;
			case 3:
				return TWO_THIRD_SIZE;
			case 4:
				return THREE_QUARTER_SIZE;
			case 6:
				return FULL_SIZE_HALF;
			case 7:
				return DOUBLE_SIZE;
			case 8:
				return SCALE_HEIGHT_SIZE;
			case 9:
				return SCALE_WIDTH_SIZE;
            case 5:
			default:
				return FULL_SIZE;
		}
	}
	
	public static int getZoomIndex(double ratio){
		if (ratio == AUTO_SIZE){
			return 0;
		}else if (ratio == QUARTER_SIZE){
			return 1;
		}else if (ratio == HALF_SIZE){
			return 2;
		}else if (ratio == TWO_THIRD_SIZE){
			return 3;
		}else if (ratio == THREE_QUARTER_SIZE){
			return 4;
		}else if (ratio == FULL_SIZE_HALF){
			return 6;
		}else if (ratio == DOUBLE_SIZE){
			return 7;
		}else if (ratio == SCALE_HEIGHT_SIZE){
			return 8;
		}else if (ratio == SCALE_WIDTH_SIZE){
			return 9;
		}else{
			return 5;
		}
	}
	
	public int getOrientation(){
        return preferences.getInt("orientationIndex", 0);
	}
	
	public void setOrientationIndex(int index){
		updateInt("orientationIndex", index);
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
	
	public void setArchiveModeIndex(int index){
		updateInt("archiveModeIndex", index);
	}

	public boolean isContextMenuEnabled(){
        return preferences.getBoolean("contextMenuEnabled", true);
	}

	public void setContextMenuEnabled(boolean enabled){
		updateBool("contextMenuEnabled", enabled);
	}
	
	public int getRecursionLevel(){
        return preferences.getInt("recursionLevel", 1);
	}
	
	public void setRecursionLevel(int level){
		updateInt("recursionLevel", level);
	}
	
	public boolean isBacklightAlwaysOn(){
        return preferences.getBoolean("keepBacklightOn", false);
	}
	
	public void setBacklightAlwaysOn(boolean alwaysOn){
		try{
			int flag = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            Window w = MainActivity.getMainActivity().getWindow();
			if (alwaysOn){
				w.addFlags(flag);
			}else{
				w.clearFlags(flag);
			}
		}catch (Exception | Error e){
			e.printStackTrace();
		}finally{
			updateBool("keepBacklightOn", alwaysOn);
		}
	}
	
	public void keepBacklightOn(Window window){
		try{
			if (isBacklightAlwaysOn()){
				int flag = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				window.addFlags(flag);
			}
		}catch (Exception | Error e){
			e.printStackTrace();
		}
	}

    public boolean isCacheOnStart(){
        return preferences.getBoolean("cacheOnStart", true);
    }

    public void setCacheOnStart(boolean enabled){
        updateBool("cacheOnStart", enabled);
    }

    public boolean isCacheForRar(){
        return preferences.getBoolean("cacheRarFiles", false); }

    public void setCacheForRar(boolean enabled){
        updateBool("cacheRarFiles", enabled);
    }
	
	public boolean keepZoomOnPageChange(){
        return preferences.getBoolean("keepZoomOnPageChange", false);
	}
	
	public void setKeepZoomOnPageChange(boolean enabled){
		updateBool("keepZoomOnPageChange", enabled);
	}

    public int getDynamicResizing(){
        return preferences.getInt("dynamicResizing", 2);
    }
	
	public void setDynamicResizing(int index){
		updateInt("dynamicResizing", index);
	}
	
	public void setHomeDir(String path){
		updateString("homeDir", path);
	}
	
	public File getHomeDir(){
		return new File(preferences.getString("homeDir", defaultHomeDir));
	}
	
	public void addRecent(Recent r){

        for (int i = 0; i < recent.size(); i++){
            if (recent.get(i).getPath().equals(r.getPath())){
                recent.remove(i);
                break;
            }
        }

        recent.add(0, r);

        saveRecentList();
		
	}
	
	public void addRecent(String path, int pageNumber){

        Log.i("SettingsManager", "Setting index for " + path + " to " + pageNumber);
        addRecent(new Recent(path, pageNumber));
		
	}

    public ArrayList<Recent> getRecent(){
        return recent;
    }

    public Recent getRecent(File f){
        for (Recent r : recent){
            if (r.getPath().equals(f.getAbsolutePath())){
                return r;
            }
        }
        return null;
    }
	
	public void removeRecent(String toRemove){
        for (Recent r : recent){
            if (r.getPath().equals(toRemove)){
                recent.remove(r);
                break;
            }
        }
		saveRecentList();
	}
	
	private void saveRecentList(){
        JSONArray arr = new JSONArray();
        for (Recent r : recent){
            arr.put(r.toString());
        }
        updateString("recent", arr.toString());
	}
	
	public void clearRecent(){
		recent.clear();
        saveRecentList();
	}
	
	public void addFavorite(String path){
		
		if (new File(path).isDirectory()){
			path += "/";
		}

        for (int i = 0; i < favorite.size(); i++){
            if (favorite.get(i).equals(path)){
                favorite.remove(i);
                break;
            }
        }

        favorite.add(0, path);

        saveFavoriteList();
		
	}
	
	public ArrayList<String> getFavorite(){
		return favorite;
	}
	
	public void removeFavorite(String toRemove){
		
		favorite.remove(toRemove);
		saveFavoriteList();
		
	}
	
	private void saveFavoriteList(){
        JSONArray arr = new JSONArray();
        for (String s : favorite){
            arr.put(s);
        }
        updateString("favorite", arr.toString());
	}
	
	public void clearFavorites(){
		favorite.clear();
        saveFavoriteList();
	}
	
	public void setLastRead(File last, int lastIndex){
		
		if (saveSession()){
			updateString("lastRead", last.getAbsolutePath());
			setLastReadIndex(lastIndex, last);
		}else if (saveRecent()) {
            addRecent(last.getAbsolutePath(), lastIndex);
        }
		
	}

    public void setLastReadIndex(int lastIndex, File tempFile){

        setLastReadIndex(lastIndex, tempFile.getAbsolutePath());

    }

    public void setLastReadIndex(int lastIndex, String tempFile){

        if (saveRecent()) addRecent(tempFile, lastIndex);

    }
	
	public File getLastFileRead(){
        String s = preferences.getString("lastRead", null);
        File f = null;
        if (s != null) f = new File(s);
		return f;
	}

    /**
     * @return True if the warning has never been shown, or it
     * has been at least one week since BatchDelete was last shown.
     **/
    public boolean isShowLastDelete(){
        long millis = System.currentTimeMillis();
        long oldLastDelete = preferences.getLong("lastDelete", -1);
        updateLong("lastDelete", millis);
        return Math.abs(millis - oldLastDelete) >= 604800000; //If it's been at least 1 week
    }

    private void updateString(String key, String value){
        Editor edit = preferences.edit();
        edit.putString(key, value);
        if (android.os.Build.VERSION.SDK_INT < 9){
            edit.commit();
        }else{
            edit.apply();
        }

    }

    private void updateInt(String key, int value){
        Editor edit = preferences.edit();
        edit.putInt(key, value);
        if (android.os.Build.VERSION.SDK_INT < 9){
            edit.commit();
        }else{
            edit.apply();
        }
    }

    private void updateLong(String key, long value){
        Editor edit = preferences.edit();
        edit.putLong(key, value);
        if (android.os.Build.VERSION.SDK_INT < 9){
            edit.commit();
        }else{
            edit.apply();
        }
    }
	
	private void updateBool(String key, boolean value){
		Editor edit = preferences.edit();
		edit.putBoolean(key, value);
        if (android.os.Build.VERSION.SDK_INT < 9){
            edit.commit();
        }else{
            edit.apply();
        }
	}
	
	public static void setFullScreen(Activity act){
		try{
			act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}catch (Exception | Error ex){
			ex.printStackTrace();
		}
	}
	
}
