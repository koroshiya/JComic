package com.japanzai.koroshiya.filechooser.tab;

import java.io.File;
import java.util.ArrayList;

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

/**
 * Tab for displaying images, archives and/or directories a user has
 * set as their "favorites" for quick access.
 * */
@SuppressLint("ValidFragment")
public class FavoriteTab extends SherlockFragment {

	private final FileChooser fc;

	public FavoriteTab(FileChooser parent) {
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

        	for (String favorite : settings.getFavorite()){
        		if ((new File(favorite)).exists()){
        			listItems.add(favorite);
        		}
        	}
        	
        	registerForContextMenu(lv);

	        String[] from = {"image", "name"};
	        int[] to = {R.id.row_image, R.id.row_text};
	        SimpleAdapter itemAdapter = new SimpleAdapter(fc, fc.getHashList(listItems), R.layout.list_item, from, to);
	        
	        fc.reset();
	        
	        lv.setEnabled(true);
	        lv.setAdapter(itemAdapter);
			fc.setListView(lv);
        
    }

}
