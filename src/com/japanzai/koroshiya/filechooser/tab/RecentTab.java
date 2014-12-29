package com.japanzai.koroshiya.filechooser.tab;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.classes.Recent;

/**
 * Tab displaying archives, images and/or directories recently viewed
 * */
@SuppressLint("ValidFragment")
public class RecentTab extends SherlockFragment {

	private final FileChooser fc;

	public RecentTab(FileChooser parent) {
		this.fc = parent;
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
	
	public void instantiate(){
            
            SettingsManager settings = MainActivity.mainActivity.getSettings();

	        ListView lv = new ListView(fc);
	        ItemClickListener icl = new ItemClickListener(fc);
	        fc.setItemClickListener(icl);
            
            ArrayList<String> listItems = new ArrayList<>();
        	
        	List<HashMap<String,String>> aList;
	        
            if (!settings.saveRecent()){
            	listItems.add(getString(R.string.recent_function_disabled));
            	listItems.add(getString(R.string.recent_general_settings));
        		aList = fc.getEmptyHashList(listItems);
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
            		aList = fc.getEmptyHashList(listItems);
                }else{
            		aList = fc.getHashList(listItems);
            	}
            	
            }

	        String[] from = {"image", "name"};
	        int[] to = {R.id.row_image, R.id.row_text};
	        SimpleAdapter itemAdapter = new SimpleAdapter(fc, aList, R.layout.list_item, from, to);
	        
	        fc.reset();
	        
	        lv.setEnabled(true);
	        lv.setAdapter(itemAdapter);
			fc.setListView(lv);
        
    }

}
