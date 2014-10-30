package com.japanzai.koroshiya.reader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.settings.SettingsManager;

public class MainActivity extends SherlockFragmentActivity {

	public static MainActivity mainActivity;
	private SettingsManager settings;
	private MainClickListener scl;
	
	public File tempDir;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		
		try{
			this.getSupportActionBar().show();
		}catch(NoClassDefFoundError ncdfe){
			Log.e("main", ncdfe.getLocalizedMessage());
		}
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
        SettingsManager.setFullScreen(this);
		instantiate();

	}

	/**
	 * Called when the "ResumeReading" option is enabled, and a file to resume
	 * reading has been saved.
	 * */
	private void enableResumeReading() {
		File savedFile = settings.getLastFileRead();
		boolean enabled = savedFile != null && savedFile.exists() && settings.saveSession();
		ImageButton imgBtn = (ImageButton) findViewById(R.id.btnResumeReading);
		// imgBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
		imgBtn.setEnabled(enabled);
	}

	public void instantiate() {

		MainActivity.mainActivity = this;
		settings = new SettingsManager(this);
		settings.keepBacklightOn(getWindow());

		setContentView(R.layout.activity_main);
		
		scl = new MainClickListener(this);

		int[] ids = {R.id.btnInitiate, R.id.btnSettings, R.id.btnHelp, R.id.btnCredits, R.id.btnResumeReading, R.id.btnErrorReporting};
		for (int id : ids){
			((ImageButton) findViewById(id)).setOnClickListener(scl);
		}

	}

	/**
	 * Called when the "ResumeReading" option is invoked. Gets the last file
	 * read and the page number the reader was on.
	 * */
	public void resumeReading(){
		startReading(settings.getLastFileRead().getAbsolutePath(), settings.getLastFileReadIndex(), this);		
	}
	
	public static void startReading(String path, int index, Activity act){
		
		if (!(new File(path).exists())) return;
		
		Intent intent = new Intent(act, Reader.class);
		Bundle b = new Bundle();
		b.putString("file", path);
		b.putInt("index", index < 0 ? 0 : index);
		intent.putExtras(b);
		act.startActivity(intent);
		
	}

	@Override
	public void onResume() {
		super.onResume();
		settings.forceOrientation(this);
		enableResumeReading();
	}

	/**
	 * @return SettingsManager responsible for storing this application's
	 *         settings.
	 * */
	public SettingsManager getSettings() {
		return this.settings;
	}

	/**
	 * @return Directory of the current tempFile.
	 * */
	public File getTempDir() {
		return this.tempDir;
	}

	/**
	 * Formats a time in milliseconds to the specified format. Also performs
	 * localization.
	 * 
	 * @param timeMillis
	 *            Time in milliseconds to format
	 * @return Returns the time passed in, formatted as "hh:mma  dd/MM/yyyy"
	 * */
	public static String formatDate(Long timeMillis) {
		SimpleDateFormat fmtDate = new SimpleDateFormat("hh:mma  dd/MM/yyyy", Locale.getDefault());
		String pattern = fmtDate.toLocalizedPattern();
		fmtDate.applyPattern(pattern);
		return fmtDate.format(timeMillis);
	}
	
}
