package com.japanzai.koroshiya.reader;

import com.japanzai.koroshiya.ErrorReport;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.About;
import com.japanzai.koroshiya.Credits;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.settings.SettingsView;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Used to handle button clicks on the main screen
 * */
public class MainClickListener implements OnClickListener {

	private final MainActivity parent;
	
	public MainClickListener(MainActivity parent){
		this.parent = parent;
	}
	
	@Override
	public void onClick(View v) {process(v.getId());}
	
	/**
	 * Processes a view's ID to figure out which event should be triggered
	 * @param resourceID ID of the control clicked on
	 * */
	public void process(int resourceID){
		
		Intent i = null;
		
		if (resourceID == R.id.btnSettings){
			i = new Intent(parent, SettingsView.class);
		}else if (resourceID == R.id.btnInitiate){
			i = new Intent(parent, FileChooser.class);
		}else if (resourceID == R.id.btnHelp){
			i = new Intent(parent, About.class);
		}else if (resourceID == R.id.btnCredits){
			i = new Intent(parent, Credits.class);
		}else if (resourceID == R.id.btnResumeReading){
			parent.resumeReading();
		}else if (resourceID == R.id.btnErrorReporting){
			i = new Intent(parent, ErrorReport.class);
		}
		
		if (i != null){
			parent.startActivity(i);
		}
		
	}

}
