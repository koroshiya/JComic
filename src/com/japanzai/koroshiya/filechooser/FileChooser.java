package com.japanzai.koroshiya.filechooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.dialog.ConfirmDialog;
import com.japanzai.koroshiya.filechooser.tab.FileChooserTab;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.ToastThread;
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.SettingsView;

/**
 * Activity responsible for displaying file chooser options and processing such events.
 * This includes displaying favorites, recently viewed files, navigating through and
 * selecting files from disk, etc.
 * */
@SuppressLint("SdCardPath")
public class FileChooser extends SherlockFragmentActivity {

	private File home = null;
	private ItemClickListener icl;
	private final MainActivity parent = MainActivity.mainActivity;
	private ArrayList<String> tempCommandList;
	private ListView v;
	private Fragment frag = null;
	private ViewFlipper vf;
	
	public FileChooser(){
		
		File smHome = parent.getSettings().getHomeDir();
		
		if (smHome != null && smHome.exists() && smHome.isDirectory()){
			this.home = smHome;
		}else{
			File home = Environment.getExternalStorageDirectory();
			if (home == null){
				home = new File("/sdcard2/");
				if (!home.exists()){
					home = new File("/sdcard/");
				}
			}
			this.home = home;
		}
		
	}
	
	public FileChooser(File targetDir){
		
		File tempDir = parent.getTempDir();

		home = tempDir == null ? targetDir : tempDir;
		
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        parent.getSettings().keepBacklightOn(this.getWindow());
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.activity_file_chooser);
		vf = (ViewFlipper) this.findViewById(R.id.viewFlipper1);
        instantiate();

    }
	
    @Override
    public void onResume(){
    	super.onResume();
    	if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    		this.setRequestedOrientation(SettingsView.get2pointxScreenOrientation(getWindowManager()));
    	} else {
    		MainActivity.mainActivity.getSettings().forceOrientation(this);
    	}
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	int id = item.getItemId();
    	
    	if (id == R.id.menu_cancel){
    		icl.process(R.string.cancel_selection);
            return true;
    	}else if(id == R.id.menu_up){
    		icl.process(R.string.up_directory);
            return true;
    	}else if(id == R.id.menu_home){
    		icl.process(R.string.home_directory);
            return true;
    	}else{
    		return super.onOptionsItemSelected(item);
    	}
    	
    }
        
    /**
     * Called upon creation of this Activity.
     * Sets the default interface up.
     * */
    public void instantiate(){
    	        
        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.removeAllTabs();
        FileChooserTabListener fctl = new FileChooserTabListener(this);
        ActionBar.Tab tab1 = setTab(getString(R.string.tab_recent), bar, fctl);
        ActionBar.Tab tab2 = setTab(getString(R.string.tab_files), bar, fctl);
        ActionBar.Tab tab3 = setTab(getString(R.string.tab_favorite), bar, fctl);
        bar.addTab(tab1);
        bar.addTab(tab2);
        bar.addTab(tab3);
        bar.setDisplayShowTitleEnabled(true);
        
        bar.selectTab(tab2);
        
        LinearLayout lv = (LinearLayout) this.findViewById(R.id.FileChooserScrollView);
        v = new ListView(parent);
    	registerForContextMenu(v);
        lv.addView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.setContentView(vf, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
    }
    
    /**
     * @param text Text to display on the tab being created
     * @param bar ActionBar to assign the tab being created to
     * @return Tab created given the parameters passed in
     * */
    private ActionBar.Tab setTab(String text, ActionBar bar, FileChooserTabListener fctl){
    	ActionBar.Tab tab = bar.newTab();
    	tab.setText(text);
    	tab.setTabListener(fctl);
    	return tab;
    }
    
    /**
     * Swaps from the current tab to another and back.
     * This is desirable when the current tab needs refreshing.
     * */
    public void refreshTab(){
    	
		ActionBar bar = getSupportActionBar();
		ActionBar.Tab tab = bar.getSelectedTab();
		
		int curTab = bar.getTabAt(0) == tab ? 2 : 0;
		
    	bar.selectTab(bar.getTabAt(curTab));
    	bar.selectTab(tab);
    	
    }
    
    /**
     * Removes the current Adapter being displayed
     * */
    public void reset(){
        v.setAdapter(null);
    }
    
    /**
     * @param lv ListView for which to display the items pertaining to
     * */
    public void setListView(ListView lv){
    	v.setAdapter(lv.getAdapter());
    }
    
    /**
     * @param oicl New listener for the items in this Activity's ListView
     * */
    public void setItemClickListener(ItemClickListener oicl){
    	this.icl = oicl;
    	v.setOnItemClickListener(oicl);
    }
    
    /**
     * @return This Activity's ListView, displaying the files to choose from
     * */
    public ListView getListView(){
    	return (ListView) this.v;
    }
    
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	ActionBar bar = getSupportActionBar();
    	ActionBar.Tab tab = bar.getSelectedTab();
    	
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		int position = info.position;
		
		ListView lv = (ListView) v;
		String name = lv.getAdapter().getItem(position).toString();
		
		menu.setHeaderTitle(name);
		if (icl == null){
			return; //Happens if there are no valid items in the list
		}
		int id = icl.processContext(name);
		if (id == -1){
			return; //Unknown selection
		}
		tempCommandList = new ArrayList<String>();
		for (String s : (getResources().getStringArray(id))){
			tempCommandList.add(s);
		}
    	if (tab == bar.getTabAt(0)){
    		tempCommandList.add(getResources().getString(R.string.file_remove_recent));
    		tempCommandList.add(getResources().getString(R.string.file_clear_recent));
    	} else if (tab == bar.getTabAt(2)){
    		tempCommandList.remove(tempCommandList.size() - 1); //removes the add to favorites command
    		tempCommandList.add(getResources().getString(R.string.file_remove_favorite));
    		tempCommandList.add(getResources().getString(R.string.file_clear_favorite));
    	}
		for (int i = 0; i < tempCommandList.size(); i++){
			menu.add(Menu.NONE, i, i, tempCommandList.get(i));
		}
    	
    }
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item){
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	String s = (String)(getListView().getAdapter().getItem(info.position));
    	String command = tempCommandList.get(item.getItemId());
    	
    	try {
			icl.process(s, command);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return true;
    	
    }
    
    /**
     * @param f File to check if supported
     * @return Returns true if the file is supported, otherwise false
     * */
    public boolean isSupportedFile(File f){
    	
    	if (f.isDirectory()){return true;}
    	
    	return ArchiveParser.isSupportedArchive(f) || ImageParser.isSupportedImage(f);
    	
    }
        
    /**
     * @param dir Path to the directory to display the contents of
     * */
    public void reset(String dir){
    	
    	try{
    		reset(new File(dir));
    	}catch (NullPointerException npe){
    		parent.runOnUiThread(new ToastThread(R.string.cant_go_up, parent, Toast.LENGTH_SHORT));
    	}
    	
    }
    
    /**
     * @param newDir Directory to display the contents of
     * */
    public void reset(File newDir){
    	
    	this.home = newDir;
    	
    	if (frag != null){
    		if (frag instanceof FileChooserTab){
    			FileChooserTab fct = (FileChooserTab) frag;
    			fct.reset(newDir);
    		}else{
    			parent.getSettings().setHomeDir(newDir.getAbsolutePath());
    			ActionBar bar = getSupportActionBar();
    			bar.selectTab(bar.getTabAt(1));
    		}
    	}
    	
    }
    
    /**
     * @param frag Fragment to set as the displayed fragment
     * */
    public void setFrag(Fragment frag){
    	this.frag = frag;
    }
    
    /**
     * @return Returns path to currently displayed directory or file
     * */
    public String getHome(){
    	return this.home.getAbsolutePath();
    }
    
    /**
     * @param value File to return to the calling Activity
     * */
    public void returnValue(File value, int index){
    	
    	parent.setTempFile(value, index);
    	this.finish();
    	
    }
		
    /**
     * @param smHome File to set as this Activity's current directory.
     * 				If the file isn't a directory, its parent is saved instead.
     * */
    public void setHome(File smHome) {
    	
    	this.home = smHome.isFile() ? smHome.getParentFile() : smHome;
    	parent.getSettings().setHomeDir(this.home.getAbsolutePath());
    	
	}
    
    /**
     * @return Currently displayed directory
     * */
    public File getHomeAsFile(){
    	return home;
    }
	    
    /**
     * @param resIdConfirm ID of the string resource to be displayed as a confirm option
     * @param resIdDecline ID of the string resource to be displayed as a decline option
     * @param message Actual prompt/question to be displayed
     * @param target ModalReturn to target with the prompt created
     * */
	public void confirm(int resIdConfirm, int resIdDecline, String message, ModalReturn target){
		ConfirmDialog confirm = new ConfirmDialog(getString(resIdConfirm), 
				getString(resIdDecline), 
				message, 
				target);
		confirm.show(getSupportFragmentManager(), "MainActivity");
    }
    
	public MainActivity getMainActivity(){
		return this.parent;
	}
	
	/**
	 * Swaps this class's ViewFlipper to the next view
	 * */
	public void showNext() {
		runOnUiThread(new ViewFlipThreadForward());
	}

	/**
	 * Swaps this class's ViewFlipper to the previous view
	 * */
	public void showPrevious() {
		runOnUiThread(new ViewFlipThreadBackward());
	}
	
	private class ViewFlipThreadForward implements Runnable{

		@Override
		public void run() {
			flipView(true);
		}

	}
	
	private class ViewFlipThreadBackward implements Runnable{
		
		@Override
		public void run() {
			flipView(false);
		}

	}
	
	public synchronized void flipView(boolean forward) {

		if (forward) {
			vf.showNext();
		} else {
			vf.showPrevious();
		}

	}
	
	@Override
	public void onBackPressed(){
		if (icl.isThreadRunning()){
			parent.runOnUiThread(new ToastThread(R.string.zip_operation_running, parent, Toast.LENGTH_SHORT));
		}else{
			super.onBackPressed();
		}
	}
	
}
