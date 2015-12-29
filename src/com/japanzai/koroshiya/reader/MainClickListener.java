package com.japanzai.koroshiya.reader;

import com.japanzai.koroshiya.ErrorReport;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.About;
import com.japanzai.koroshiya.Credits;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.io_utils.StorageHelper;
import com.japanzai.koroshiya.settings.SettingsView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Used to handle button clicks on the main screen
 * */
public class MainClickListener implements OnClickListener {

	private final MainActivity parent;

    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 100;
	
	public MainClickListener(MainActivity parent){
		this.parent = parent;
	}

	@Override
	public void onClick(View v) {process(((TextView)v).getText().toString());}
	
	/**
	 * Processes a view's ID to figure out which event should be triggered
	 * @param desc Text of the clicked item
	 * */
	public void process(String desc){
		
		Intent i;
		
		if (desc.equals(parent.getString(R.string.description_settings))) {
            i = new Intent(parent, SettingsView.class);
        }else if (desc.equals(parent.getString(R.string.description_read))){
            if (StorageHelper.isExternalStorageReadable() &&
                    ContextCompat.checkSelfPermission(parent, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        parent,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION
                );
                return;

            }else {
                i = new Intent(parent, FileChooser.class);
            }
        }else if (desc.equals(parent.getString(R.string.description_help))){
			i = new Intent(parent, About.class);
        }else if (desc.equals(parent.getString(R.string.description_credits))){
			i = new Intent(parent, Credits.class);
        }else if (desc.equals(parent.getString(R.string.description_resume))){
			parent.resumeReading();
            return;
        }else if (desc.equals(parent.getString(R.string.error_button))){
			i = new Intent(parent, ErrorReport.class);
		}else{
            return;
        }

		parent.startActivity(i);
		
	}

}
