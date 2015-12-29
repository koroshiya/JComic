package com.japanzai.koroshiya.filechooser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.japanzai.koroshiya.DrawerActivity;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.ResizingGridView;
import com.japanzai.koroshiya.dialog.ConfirmDialog;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.io_utils.StorageHelper;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.SettingsView;
import com.japanzai.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Activity responsible for displaying file chooser options and processing such events.
 * This includes displaying favorites, recently viewed files, navigating through and
 * selecting files from disk, etc.
 * */
public class FileChooser extends DrawerActivity {

	private File home = null;
	private final ItemClickListener icl = new ItemClickListener(this);
	private final MainActivity parent = MainActivity.getMainActivity();
	protected ResizingGridView v;

    public final static int FILES = 100;
    public final static int FAVORITE = 200;
    public final static int RECENT = 300;
    private int type = FILES;
	
	public FileChooser(){
		
		File smHome = parent.getSettings().getHomeDir();
		
		if (smHome != null && smHome.exists() && smHome.isDirectory()){
			this.home = smHome;
		}else{
			this.home = Environment.getExternalStorageDirectory();
		}
		
	}
	
	public FileChooser(File targetDir){
		
		File tempDir = parent.getTempDir();

		home = tempDir == null ? targetDir : tempDir;
		
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ItemClickListener.READ_EXTERNAL_STORAGE_PERMISSION:
                String location = icl.getTmpLocation();
                icl.setTmpLocation(null);

                if (location != null && new File(location).canRead()){
                    icl.onClick(location);
                }
                break;
        }
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        parent.getSettings().keepBacklightOn(this.getWindow());
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.activity_file_chooser);
        v = (ResizingGridView) findViewById(R.id.FileChooserPane);
        instantiateParent();
        instantiate();

    }
	
    @Override
    public void onResume(){
    	super.onResume();
    	if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    		this.setRequestedOrientation(SettingsView.get2pointxScreenOrientation(getWindowManager()));
    	} else {
    		parent.getSettings().forceOrientation(this);
    	}
        instantiate();
    }
        
    /**
     * Called upon creation of this Activity.
     * Sets the default interface up.
     * */
    public void instantiateParent(){

        final String[] items = new String[]{
                getString(R.string.back),
                getString(R.string.tab_recent),
                getString(R.string.tab_files),
                getString(R.string.tab_favorite)
        };

        findViewById(R.id.btn_home).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                icl.processItemAfter(Environment.getExternalStorageDirectory().getAbsolutePath(), false);
            }
        });
        findViewById(R.id.btn_up).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				icl.processItemAfter(getHome().equals("/") ? "/" : getHomeAsFile().getParent(), false);
			}
		});
        findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				refreshTab();
			}
		});

        findViewById(R.id.btn_bulk_delete).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(parent, BulkDelete.class));
            }
        });

        findViewById(R.id.btn_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instantiateDrawer(items);
            }
        });
        
    }

    public void drawerClick(int i){

        Log.e("SettingsView", Integer.toString(i));

        int type;
        switch (i){
            case 0:
                finish();
                return;
            case 1:
            case RECENT:
                type = RECENT;
                break;
            case 2:
            case FILES:
                type = FILES;
                break;
            default:
                type = FAVORITE;
                break;
        }
        setAdapter(type);

    }

    /**
     * Swaps from the current tab to another and back.
     * This is desirable when the current tab needs refreshing.
     * */
    public void refreshTab(){

        instantiate();

    }
    
    /**
     * @param f File to check if supported
     * @return Returns true if the file is supported, otherwise false
     * */
    public static boolean isSupportedFile(File f){
    	
    	return f.isDirectory() || f.length() > 0 && (ArchiveParser.isSupportedArchive(f) || ImageParser.isSupportedImage(f));
    	
    }
    
    /**
     * @param newDir Directory to display the contents of
     * */
    public void reset(File newDir){

        setHome(newDir);
        if (this.type == FILES){
            //instantiate();
            setAdapter(this.type);
        }else if (this.type == FAVORITE){
            drawerClick(FILES);
        }
    	
    }

    public int getType(){
        return this.type;
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
    	
    	Log.d("FileChooser", "return value of " + value.getAbsolutePath());
		
		Intent intent = new Intent(this, Reader.class);
		Bundle b = new Bundle();
		b.putString("file", value.getAbsolutePath());
		b.putInt("index", index < 0 ? 0 : index);
        Log.d("FileChooser", "Starting at index: " + index);
		intent.putExtras(b);
		startActivity(intent);
    	
    }

    private File getSafeStorage(){
        if (StorageHelper.isExternalStorageReadable()){
            return Environment.getExternalStorageDirectory();
        }else{
            String userHome = System.getProperty("user.home");
            return new File(userHome);
        }
    }

    public void instantiate(){
        if (this.type == FILES){
            File smHome = parent.getSettings().getHomeDir();

            if (smHome != null && smHome.exists() && smHome.isDirectory() && smHome.canRead()){
                setHome(smHome);
            }else{
                setHome(getSafeStorage());
            }

            if (getHomeAsFile().list() != null){
                setAdapter(this.type);
            }else {
                reset(new File("/"));

                setButtons(true);
            }
        }else {
            setAdapter(this.type);
        }
    }

    public void setAdapter(int type){

        SettingsManager settings = parent.getSettings();
        ArrayList<String> listItems = new ArrayList<>();
        FileItemAdapter aList;

        switch (type){
            case FAVORITE:

                for (String favorite : settings.getFavorite()){
                    if ((new File(favorite)).exists()){
                        listItems.add(favorite);
                    }
                }

                aList = new FileItemAdapter(this, FileItem.getHashList(listItems, this), icl, icl);
                break;
            case RECENT:

                if (!settings.saveRecent()){
                    listItems.add(getString(R.string.recent_function_disabled));
                    listItems.add(getString(R.string.recent_general_settings));
                    aList = new FileItemAdapter(this, getEmptyHashList(listItems), icl, icl);
                }else{

                    String line;
                    for (Recent recent : settings.getRecent()){
                        line = recent.getPath();
                        if ((new File(line)).exists()){
                            listItems.add(line);
                        }
                    }

                    if (listItems.size() == 0){
                        listItems.add(getString(R.string.recent_no_recent_files));
                        aList = new FileItemAdapter(this, getEmptyHashList(listItems), icl, icl);
                    }else{
                        aList = new FileItemAdapter(this, FileItem.getHashList(listItems, this), icl, icl);
                    }

                }
                break;
            case FILES:
            default:

                ArrayList<String> tempList = new ArrayList<>();
                for (File s : getHomeAsFile().listFiles()){
                    if (FileChooser.isSupportedFile(s) && !s.isHidden()){tempList.add(s.getName());}
                }
                Object[] tempArray = tempList.toArray();
                Arrays.sort(tempArray);
                for (Object obj : tempArray){
                    listItems.add(obj.toString());
                }

                aList = new FileItemAdapter(this, FileItem.getHashList(listItems, this), icl, icl);
                break;
        }

        this.type = type;

        v.setAdapter(aList);

        setButtons(type == FILES);

    }

    private void setButtons(boolean visible){
        int[] affectedButtons = {R.id.btn_home, R.id.btn_up, R.id.btn_refresh, R.id.btn_bulk_delete};
        for (int id : affectedButtons){
            findViewById(id).setVisibility(visible ? View.VISIBLE : View.GONE);
        }
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
     * @param message Actual prompt/question to be displayed
     * @param target ModalReturn to target with the prompt created  */
	public void confirm(String message, ModalReturn target){
		ConfirmDialog confirm = new ConfirmDialog(getString(R.string.file_confirm),
				getString(R.string.file_deny),
				message,
				target);
		confirm.show(getSupportFragmentManager(), "MainActivity");
    }

	public ArrayList<FileItem> getEmptyHashList(ArrayList<String> listItems){
        
        ArrayList<FileItem> aList = new ArrayList<>();

        for (String s : listItems){
            FileItem hm = new FileItem(s, R.drawable.transparent, this);
            aList.add(hm);
        }
        
        return aList;
	}
	
}
