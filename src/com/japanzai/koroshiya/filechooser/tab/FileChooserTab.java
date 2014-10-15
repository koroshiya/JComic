package com.japanzai.koroshiya.filechooser.tab;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.actionbarsherlock.app.SherlockFragment;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.filechooser.ItemClickListener;
import com.japanzai.koroshiya.reader.MainActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Tab for displaying a list of files and directories to select/navigate
 * */
@SuppressLint("ValidFragment")
public class FileChooserTab extends SherlockFragment {

	private final MainActivity parent = MainActivity.mainActivity;
	private final FileChooser fc;
	
	public FileChooserTab(FileChooser fc){
		
		this.fc = fc;
		File smHome = parent.getSettings().getHomeDir();
		
		if (smHome != null && smHome.exists() && smHome.isDirectory()){
			fc.setHome(smHome);
		}else{
			File home = Environment.getExternalStorageDirectory();
			if (home == null){
				String userHome = System.getProperty("user.home");
				home = new File(userHome);
			}
			fc.setHome(home);
		}
		
	}
	
	public FileChooserTab(File targetDir, FileChooser fc){
		
		this.fc = fc;
		File tempDir = parent.getTempDir();

		fc.setHome(tempDir == null ? targetDir : tempDir);
		
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
	
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }*/
    
    public void instantiate(){
    	            	
        if (fc.getHomeAsFile().list() != null){

            ArrayList<String> listItems = new ArrayList<String>();

            listItems.add(getString(R.string.cancel_selection));
            listItems.add(getString(R.string.home_directory));
            listItems.add(getString(R.string.up_directory));
            
            ArrayList<String> tempList = new ArrayList<String>();
	        for (File s : fc.getHomeAsFile().listFiles()){
	        	if (fc.isSupportedFile(s) && !s.isHidden()){tempList.add(s.getName());}
	        }
	        Object[] tempArray = tempList.toArray();
	        Arrays.sort(tempArray);
	        for (Object obj : tempArray){
	        	listItems.add(obj.toString());
	        }
	
	        ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(fc, 
	                android.R.layout.simple_list_item_1,
	                listItems);
	        
	        fc.reset();
	        
	        ListView lv = new ListView(fc);
	        lv.setEnabled(true);
	        lv.setAdapter(itemAdapter);
	        ItemClickListener icl = new ItemClickListener(fc);
	        fc.setItemClickListener(icl);	        

			fc.setListView(lv);
	        
        }else {
        	fc.reset("/");
        }
        
    }
    
    /**
     * @param newDir Directory to display
     * */
    public void reset(File newDir){
    	
    	instantiate();
    	fc.setHome(newDir);
    	
    }
    
}
