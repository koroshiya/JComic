package com.japanzai.koroshiya.filechooser;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.filechooser.tab.FavoriteTab;
import com.japanzai.koroshiya.filechooser.tab.FileChooserTab;
import com.japanzai.koroshiya.filechooser.tab.RecentTab;

/**
 * Class responsible for handling the tabs of an Activity using
 * FileChooserTab, FavoriteTab and RecentTab
 * */
public class FileChooserTabListener implements TabListener {

	private final FileChooser parent;
	
	public FileChooserTabListener(FileChooser fc) {
		
		this.parent = fc;
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		
		Fragment frag;
		boolean visible = false;
		
		if (tab.getPosition() == 0){
			frag = new RecentTab(parent);
		}else if (tab.getPosition() == 1) {
			frag = new FileChooserTab(parent);
			visible = true;
		}else {
			frag = new FavoriteTab(parent);
		}
		
		int[] affectedButtons = {R.id.btn_home, R.id.btn_up, R.id.btn_refresh};
		for (int id : affectedButtons){
			parent.findViewById(id).setVisibility(visible ? View.VISIBLE : View.GONE);
		}
		
		parent.setFrag(frag);
		ft.replace(android.R.id.content, frag);
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}

}
