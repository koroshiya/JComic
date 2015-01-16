package com.japanzai.koroshiya.filechooser.tab;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockFragment;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.filechooser.ItemClickListener;
import com.japanzai.koroshiya.reader.MainActivity;

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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		return inflater.inflate(R.layout.general_settings, group, false);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instantiate();
    }
    
    public void instantiate(){
    	            	
        if (fc.getHomeAsFile().list() != null){

            ArrayList<String> listItems = new ArrayList<>();
            
            ArrayList<String> tempList = new ArrayList<>();
	        for (File s : fc.getHomeAsFile().listFiles()){
	        	if (FileChooser.isSupportedFile(s) && !s.isHidden()){tempList.add(s.getName());}
	        }
	        Object[] tempArray = tempList.toArray();
	        Arrays.sort(tempArray);
	        for (Object obj : tempArray){
	        	listItems.add(obj.toString());
	        }

	        String[] from = {"image", "name"};
	        int[] to = {R.id.row_image, R.id.row_text};
	        SimpleAdapter itemAdapter = new SimpleAdapter(fc, fc.getHashList(listItems), R.layout.list_item, from, to);
	        
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
