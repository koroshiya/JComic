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
import android.view.Window;
import android.view.WindowManager;

import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.classes.Recent;

/**
 * Class for managing all user settings.
 * Responsible for setting default settings, loading user settings from
 * disk, saving settings to disk, etc.
 * */
public class SettingsManager {
	
	/*TODO:
	 * Jump to page
	 * Other long press/menu options, like zoom
	 * Separate activity for resume reading; call finish, pass in param if need be
	
	 * TODO: Faster image animation. Animation for fling?
	 * TODO: PRO: Other animations can be selected
	 * TODO: Check byte[] instead of JBitmapDrawable
	*Double swipe to go to first/last //setting for this instead of pinch-zoom?

		Settings:
		Default location for reading
		fit to height/width, scale, etc. - make wide images fit to height
		Smartscale? (zoom out to certain degree depending on page size)
		RAR password
		Setting for number of favorites/recent – on change, flush overflow
		
	 * 
	 * 
	 * TODO: manga reading mode; zoom, move to the right
	 */
	
	private static final boolean defaultLoopMode = true;
	private static final boolean defaultSaveSession = true;
	private static final boolean defaultSaveRecent = true;
	private static final boolean defaultKeepBacklightOn = false;
	private static final boolean defaultExtractMode = false;
	private static final boolean defaultCacheSafety = true;
	private static final boolean defaultCacheOnStart = true;
	private static final boolean defaultKeepZoomOnPageChange = false;
	private static final boolean defaultContextMenuEnabled = true;
	private static final boolean defaultRarPassword = false;
	private static final int defaultZoomIndex = 0;
	private static final int defaultOrientationIndex = 0;
	private static final int defaultRamModeIndex = 1;
	private static final int defaultArchiveModeIndex = 0;
	private static final int defaultCacheModeIndex = 0;
	private static final int defaultCacheLevel = 2;
	private static final int defaultRecursionLevel = 1;
	private static final int defaultDynamicResizing = 2;
	private static final int defaultDoubleTapIndex = 0;
	private final String defaultHomeDir;
	
	private static boolean loopMode;
	private static boolean saveSession;
	private static boolean saveRecent;
	private static boolean keepBacklightOn;
	private static boolean extractMode;
	private static boolean cacheSafety;
	private static boolean cacheOnStart;
	private static boolean keepZoomOnPageChange;
	private static boolean contextMenuEnabled;
	private static boolean rarPassword;
	private static int zoomIndex = -1;
	private static int orientationIndex = -1;
	private static int ramModeIndex = -1;
	private static int archiveModeIndex = -1;
	private static int cacheModeIndex = -1;
	private static int cacheLevel = -1;
	private static int recursionLevel = -1;
	private static int dynamicResizing = -1;
	private static int doubleTapIndex = 0;
	private static File homeDir = null;
	private static File lastRead = null;
	private static int lastReadIndex = -1;
	private static ArrayList<Recent> recent = new ArrayList<>();
	private static ArrayList<String> favorite = new ArrayList<>();
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
		
		loopMode = preferences.getBoolean("loopMode", defaultLoopMode);
		saveSession = preferences.getBoolean("saveSession", defaultSaveSession);
		saveRecent = preferences.getBoolean("saveRecent", defaultSaveRecent);
		keepBacklightOn = preferences.getBoolean("keepBacklightOn", defaultKeepBacklightOn);
		extractMode = preferences.getBoolean("extractModeIndex", defaultExtractMode);
		cacheSafety = preferences.getBoolean("cacheSafety", defaultCacheSafety);
		cacheOnStart = preferences.getBoolean("cacheOnStart", defaultCacheOnStart);
		keepZoomOnPageChange = preferences.getBoolean("keepZoomOnPageChange", defaultKeepZoomOnPageChange);
		contextMenuEnabled = preferences.getBoolean("contextMenuEnabled", defaultContextMenuEnabled);
		rarPassword = preferences.getBoolean("rarPassword", defaultRarPassword);
		zoomIndex = preferences.getInt("zoomIndex", defaultZoomIndex);
		orientationIndex = preferences.getInt("orientationIndex", defaultOrientationIndex);
		ramModeIndex = preferences.getInt("ramModeIndex", defaultRamModeIndex);
		archiveModeIndex = preferences.getInt("archiveModeIndex", defaultArchiveModeIndex);
		cacheModeIndex = preferences.getInt("cacheModeIndex", defaultCacheModeIndex);
		cacheLevel = preferences.getInt("cacheLevel", defaultCacheLevel);
		recursionLevel = preferences.getInt("recursionLevel", defaultRecursionLevel);
		dynamicResizing = preferences.getInt("dynamicResizing", defaultDynamicResizing);
		doubleTapIndex = preferences.getInt("doubleTapIndex", defaultDoubleTapIndex);
		homeDir = new File(preferences.getString("homeDir", defaultHomeDir));
		try{
			lastRead = new File(preferences.getString("lastRead", null));
		}catch (Exception ex){
			lastRead = null;
		}
		
		lastReadIndex = preferences.getInt("lastReadIndex", -1);
		fill("recent", "recentPage", recent);
		fill("favorite", favorite);
		this.setBacklightAlwaysOn(keepBacklightOn);
		
	}
	
	/**
	 * @param key Settings key used to link array resources.
	 * eg. a key of "recent" will look for recent1, recent2, etc.
	 * @param array Array of Strings to add extracted settings to
	 * */
	private void fill(String key, ArrayList<String> array){
		
		array.clear();
		String s;
		
		for (int i = 1; i <= 10; i++){
			s = preferences.getString(key + i, "");
			if (s.equals("")){
				break;
			}
			array.add(s);
		}
		
	}
	
	private void fill(String keyName, String keyValue, ArrayList<Recent> array){
		
		array.clear();
		String s;
		int page;
		
		for (int i = 1; i <= 10; i++){
			s = preferences.getString(keyName + i, "");
			if (s.equals("")){
				break;
			}
			page = preferences.getInt(keyValue + i, -1);
			if (page < 0){
				break;
			}
			array.add(new Recent(s, page));
		}
		
	}
	
	/**
	 * Restores all user settings to their default values.
	 * Does not include history settings, like favorites or recent.
	 * */
	public void restoreDefaultSettings(){
		setLoopMode(defaultLoopMode);
		setSaveSession(defaultSaveSession);
		setSaveRecent(defaultSaveRecent);
		setBacklightAlwaysOn(defaultKeepBacklightOn);
		setCacheOnStart(defaultCacheOnStart);
		setKeepZoomOnPageChange(defaultKeepZoomOnPageChange);
		setContextMenuEnabled(defaultContextMenuEnabled);
		setRarPasswordEnabled(defaultRarPassword);
		setZoomIndex(defaultZoomIndex);
		setOrientationIndex(defaultOrientationIndex);
		setRamModeIndex(defaultRamModeIndex);
		setArchiveModeIndex(defaultArchiveModeIndex);
		setCacheSafety(defaultCacheSafety);
		setDoubleTapIndex(defaultDoubleTapIndex);
		setCacheModeIndex(defaultCacheModeIndex);
		setExtractModeEnabled(defaultExtractMode);
		setCacheLevel(defaultCacheLevel);
		setRecursionLevel(defaultRecursionLevel);
		setDynamicResizing(defaultDynamicResizing);
		setHomeDir(System.getProperty("user.home"));
	}
	
	/**
	 * @return If loop mode is enabled, returns true.
	 * 			Otherwise, false.
	 * */
	public boolean isLoopModeEnabled(){
		return loopMode;
	}
	
	/**
	 * @param loop Boolean indicating whether to enable loop mode or not.
	 * */
	public void setLoopMode(boolean loop){
		loopMode = loop;
		updateBool("loopMode", loop);
	}
	
	/**
	 * @return If user has set for their viewing session to be saved,
	 * 			returns true. Otherwise, false.
	 * */
	public boolean saveSession(){
		return saveSession;
	}
	
	/**
	 * @param save Boolean indicating whether to save user viewing sessions or not.
	 * */
	public void setSaveSession(boolean save){
		saveSession = save;
		updateBool("saveSession", save);
	}
	
	public int getDoubleTapIndex(){
		return doubleTapIndex;
	}
	
	public void setDoubleTapIndex(int index){
		doubleTapIndex = index;
		updateInt("doubleTapIndex", index);
	}
	
	public boolean saveRecent(){
		return saveRecent;
	}
	
	public void setSaveRecent(boolean save){
		saveRecent = save;
		updateBool("saveRecent", save);
	}
	
	public int getZoom(){
		return zoomIndex;
	}
	
	public void setZoomIndex(int index){
		zoomIndex = index;
		updateInt("zoomIndex", index);
	}
	
	public double getCurrentZoomRatio(){
		return getZoomRatio(zoomIndex);
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
		return orientationIndex;
	}
	
	public void setOrientationIndex(int index){
		orientationIndex = index;
		updateInt("orientationIndex", index);
	}
	
	public void forceOrientation(Activity act){
		
		switch(orientationIndex){
			case 1:
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			case 2:
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			default:
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
				break;
		}
		
	}
	
	public int getRamModeIndex(){
		return ramModeIndex;
	}
	
	public void setRamModeIndex(int index){
		ramModeIndex = index;
		updateInt("ramModeIndex", index);
	}
	
	public boolean isRamMode(){
		return ramModeIndex == 0; //TODO: turn RAM mode into cache type; streams, images, etc.
	}
	
	public int getArchiveModeIndex(){
		return archiveModeIndex;
	}
	
	public void setArchiveModeIndex(int index){
		archiveModeIndex = index;
		updateInt("archiveModeIndex", index);
	}
	
	public boolean getExtractModeEnabled(){
		return extractMode;
	}
	
	public void setExtractModeEnabled(boolean enabled){
		extractMode = enabled;
		updateBool("extractModeIndex", enabled);
	}

	public boolean isContextMenuEnabled(){
		return contextMenuEnabled;
	}

	public void setContextMenuEnabled(boolean enabled){
		contextMenuEnabled = enabled;
		updateBool("contextMenuEnabled", enabled);
	}
	
	public boolean isRarPasswordEnabled(){
		return rarPassword;
	}
	
	public void setRarPasswordEnabled(boolean enabled){
		rarPassword = enabled;
		updateBool("rarPassword", enabled);
	}
	
	public boolean getCacheSafety(){
		return cacheSafety;
	}
	
	public void setCacheSafety(boolean safe){
		cacheSafety = safe;
	}
	
	public int getCacheModeIndex(){
		return cacheModeIndex;
	}
	
	public void setCacheModeIndex(int index){
		cacheModeIndex = index;
		updateInt("cacheModeIndex", index);
	}
	
	public int getCacheLevel(){
		return cacheLevel;
	}

	public void setCacheLevel(int level){
		cacheLevel = level;
		updateInt("cacheLevel", level);
	}
	
	public int getRecursionLevel(){
		return recursionLevel;
	}
	
	public void setRecursionLevel(int level){
		recursionLevel = level;
		updateInt("recursionLevel", level);
	}
	
	public boolean isBacklightAlwaysOn(){
		return keepBacklightOn;
	}
	
	public void setBacklightAlwaysOn(boolean alwaysOn){
		try{
			keepBacklightOn = alwaysOn;
			int flag = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			if (alwaysOn){
				MainActivity.mainActivity.getWindow().addFlags(flag);			
			}else{
				MainActivity.mainActivity.getWindow().clearFlags(flag);			
			}
		}catch (Exception e){
			e.printStackTrace();
		}catch (Error e){
			e.printStackTrace();
		}finally{
			updateBool("keepBacklightOn", alwaysOn);
		}
	}
	
	public void keepBacklightOn(Window window){
		try{
			if (keepBacklightOn){
				int flag = android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				window.addFlags(flag);
			}
		}catch (Exception e){
			e.printStackTrace();
		}catch (Error e){
			e.printStackTrace();
		}
	}
	
	public boolean isCacheOnStart(){
		return cacheOnStart;
	}
	
	public void setCacheOnStart(boolean enabled){
		cacheOnStart = enabled;
		updateBool("cacheOnStart", enabled);
	}
	
	public boolean keepZoomOnPageChange(){
		return keepZoomOnPageChange;
	}
	
	public void setKeepZoomOnPageChange(boolean enabled){
		keepZoomOnPageChange = enabled;
		updateBool("keepZoomOnPageChange", enabled);
	}
	
	public int getDynamicResizing(){
		return dynamicResizing;
	}
	
	public void setDynamicResizing(int index){
		dynamicResizing = index;
		updateInt("dynamicResizing", index);
	}
	
	public void setHomeDir(String path){
		homeDir = new File(path);
		updateString("homeDir", path);
	}
	
	public File getHomeDir(){
		return homeDir;
	}
	
	public void addRecent(Recent r){

		addList(recent, r);
		
	}
	
	public void addRecent(String path, int pageNumber){

		addRecent(new Recent(path, pageNumber));
		
	}
	
	public ArrayList<Recent> getRecent(){
		return recent;
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
		saveList(recent);
	}
	
	public void clearRecent(){
		clear(recent);
	}
	
	public void addFavorite(String path){
		
		if (new File(path).isDirectory()){
			path += "/";
		}
		
		addList(favorite, "favorite", path);
		
	}
	
	public ArrayList<String> getFavorite(){
		return favorite;
	}
	
	public void removeFavorite(String toRemove){
		
		favorite.remove(toRemove);
		saveFavoriteList();
		
	}
	
	private void saveFavoriteList(){
		saveList(favorite, "favorite");
	}
	
	public void clearFavorites(){
		clear(favorite, "favorite");
	}
	
	private void addList(ArrayList<String> list, String type, String path){
		
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).equals(path)){
				list.remove(i);
				break;
			}
		}
		
		list.add(0, path);
		if (list.size() > 10){
			list.remove(list.size() - 1);
		}
		
		saveList(list, type);
		
	}
	
	private void addList(ArrayList<Recent> list, Recent r){
		
		for (int i = 0; i < list.size(); i++){
			if (list.get(i).getPath().equals(r.getPath())){
				list.remove(i);
				break;
			}
		}
		
		list.add(0, r);
		if (list.size() > 10){
			list.remove(list.size() - 1);
		}
		
		saveList(list);
		
	}
	
	private void saveList(ArrayList<String> list, String type){
		for (int i = 1; i <= list.size(); i++){
			updateString(type + i, list.get(i - 1));
		}
		if (list.size() + 1 <= 10){
			for (int i = list.size() + 1; i <= 10; i++){
				updateString(type + i, "");
			}
		}
	}
	
	private void saveList(ArrayList<Recent> list){
		for (int i = 1; i <= list.size(); i++){
			updateString("recent" + i, list.get(i - 1).getPath());
			updateInt("recentPage" + i, list.get(i - 1).getPageNumber());
		}
		if (list.size() + 1 <= 10){
			for (int i = list.size() + 1; i <= 10; i++){
				updateString("recent" + i, "");
				updateInt("recentPage" + i, -1);
			}
		}
	}
	
	private void clear(ArrayList<String> list, String type){
		list.clear();
		saveList(list, type);
	}
	
	private void clear(ArrayList<Recent> list){
		list.clear();
		saveList(list);
	}
	
	public void setLastRead(File last, int lastIndex){
		
		if (saveSession){
		
			lastRead = last;
			lastReadIndex = lastIndex;
			updateString("lastRead", lastRead.getAbsolutePath());
			updateInt("lastReadIndex", lastReadIndex);
		
		}
		
	}
	
	public void setLastReadIndex(int lastIndex){
		
		if (saveSession && lastRead != null){
			lastReadIndex = lastIndex;
			updateInt("lastReadIndex", lastReadIndex);
		}
		
	}
	
	public File getLastFileRead(){
		return lastRead;
	}
	
	public int getLastFileReadIndex(){
		return lastReadIndex;
	}
	
	public void clearLastRead(){
		lastRead = null;
		lastReadIndex = -1;
	}
	
	private void updateString(String key, String value){
		Editor edit = preferences.edit();
		edit.putString(key, value);
	    edit.apply();
	}
	
	private void updateInt(String key, int value){
		Editor edit = preferences.edit();
		edit.putInt(key, value);
	    edit.apply();
	}
	
	private void updateBool(String key, boolean value){
		Editor edit = preferences.edit();
		edit.putBoolean(key, value);
	    edit.apply();
	}
	
	public static void setFullScreen(Activity act){
		try{
			act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}catch (Exception | Error ex){
			ex.printStackTrace();
		}
	}
	
}
