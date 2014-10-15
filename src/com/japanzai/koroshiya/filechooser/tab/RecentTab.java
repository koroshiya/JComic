package com.japanzai.koroshiya.filechooser.tab;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
            
            ArrayList<String> listItems = new ArrayList<String>();
	        
            if (!settings.saveRecent()){
            	listItems.add(getString(R.string.recent_function_disabled));
            	listItems.add(getString(R.string.recent_general_settings));
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
                }
            	
            }
	
	        ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(fc, 
	                android.R.layout.simple_list_item_1,
	                listItems);
	        
	        fc.reset();
	        
	        lv.setEnabled(true);
	        lv.setAdapter(itemAdapter);
			fc.setListView(lv);
        
    }

}
