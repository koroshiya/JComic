package com.japanzai.koroshiya.reader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.japanzai.koroshiya.Progress;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.cache.Steppable;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.controls.JImageSwitcher;
import com.japanzai.koroshiya.controls.thread.ViewFlipThreadBackward;
import com.japanzai.koroshiya.controls.thread.ViewFlipThreadForward;
import com.japanzai.koroshiya.dialog.ConfirmDialog;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

public class MainActivity extends SherlockFragmentActivity {

	private JImageSwitcher imgPanel;
	private ViewFlipper vf;

	private Steppable cache = null;
	private Progress progressThread;

	public static MainActivity mainActivity;
	private SettingsManager settings;
	private MainClickListener scl;

	private File tempFile;
	private File tempDir;

	private int width;
	private int height;
	
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
	
	public void showContextMenu(){
		final CharSequence[] items = getResources().getStringArray(R.array.array_context_menu);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.setting_context_menu_head);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			    if (id == 0){
			    	cache.first();
			    }else if(id == 1){
			    	cache.last();
			    }else{
			    	show();
			    }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	@SuppressLint("NewApi")
	public void show(){
		
		if (android.os.Build.VERSION.SDK_INT >= 11){
			try{
				final Dialog d = new Dialog(this);
		        d.setTitle(R.string.setting_context_menu_heading);
		        d.setContentView(R.layout.dialog);
		        Button b1 = (Button) d.findViewById(R.id.button1);
		        Button b2 = (Button) d.findViewById(R.id.button2);
		        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);
		        np.setMaxValue(cache.getMax());
		        np.setMinValue(1);
		        np.setWrapSelectorWheel(false);
		        b1.setOnClickListener(new OnClickListener(){
		          @Override
		          public void onClick(View v) {
		              cache.goToPage(np.getValue() - 1);
		              d.dismiss();
		           }    
		          });
		         b2.setOnClickListener(new OnClickListener(){
		          @Override
		          public void onClick(View v) {
		              d.dismiss();
		           }
		          });
		        d.show();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}else{

			String items[] = new String[cache.getMax()];
			for(int i = 1; i <= cache.getMax(); i++){
				items[i-1] = (Integer.toString(i));
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.setting_context_menu_heading);
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
			    	cache.goToPage(id);
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
		}

    }

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static Point getScreenDimensions() {
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Display display = MainActivity.mainActivity.getWindowManager()
					.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			return size;
		} else {
			Display display = MainActivity.mainActivity.getWindowManager()
					.getDefaultDisplay();
			return new Point(display.getWidth(), display.getHeight());
		}
	}

	/**
	 * Purpose: Clears this class's temporary file and gets this Activity ready
	 * to begin reading
	 * */
	public void clearTempFile() {

		Point size = getScreenDimensions();
		width = size.x;
		height = size.y;
		
		this.tempFile = null;
		imgPanel.clear();
		
		if (cache != null && cache.getMax() != 0) {
			// imgPanel.setOnTouchListener(swipe);
			this.cache.sort();
			
			try {
				cache.parseCurrent();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// vf.showNext();
		} else {
			runOnUiThread(new ToastThread(R.string.no_images, this,
					Toast.LENGTH_SHORT));
		}
	}

	@Override
	public void onBackPressed() {

		if (progressThread != null && progressThread.isAlive()) {
			runOnUiThread(new ToastThread(R.string.loading_progress, this,
					Toast.LENGTH_SHORT));
		} else if (cache != null) {
			if (settings.saveRecent()) {
				settings.addRecent(cache.getPath(), cache.getIndex());
			}
			cache.emptyCache();
			cache.clear();
			imgPanel.getImageDrawable().closeBitmap();
			cache.close();
			cache = null;
			progressThread = null;
			enableResumeReading();
			vf.showNext();
			if (settings.isBackToFileChooser()){
				scl.process(R.id.btnInitiate);
			}
		} else {
			super.onBackPressed();
		}

	}

	/**
	 * Called when the "ResumeReading" option is enabled, and a file to resume
	 * reading has been saved.
	 * */
	private void enableResumeReading() {
		File savedFile = settings.getLastFileRead();
		boolean enabled = savedFile != null && savedFile.exists()
				&& settings.saveSession();
		ImageButton imgBtn = (ImageButton) findViewById(R.id.btnResumeReading);
		// imgBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
		imgBtn.setEnabled(enabled);
	}

	public void instantiate() {

		MainActivity.mainActivity = this;
		settings = new SettingsManager(this);
		settings.keepBacklightOn(getWindow());

		setContentView(R.layout.activity_main);

		ImageButton btnStart = (ImageButton) findViewById(R.id.btnInitiate);
		ImageButton btnSetting = (ImageButton) findViewById(R.id.btnSettings);
		ImageButton btnHelp = (ImageButton) findViewById(R.id.btnHelp);
		ImageButton btnCredits = (ImageButton) findViewById(R.id.btnCredits);
		ImageButton btnSaved = (ImageButton) findViewById(R.id.btnResumeReading);
		ImageButton btnError = (ImageButton) findViewById(R.id.btnErrorReporting);
		imgPanel = (JImageSwitcher) findViewById(R.id.imgPanel);
		vf = (ViewFlipper) findViewById(R.id.vwFlip);
		
		scl = new MainClickListener(this);

		btnStart.setOnClickListener(scl);
		btnSetting.setOnClickListener(scl);
		btnHelp.setOnClickListener(scl);
		btnCredits.setOnClickListener(scl);
		btnSaved.setOnClickListener(scl);
		btnError.setOnClickListener(scl);

		// imgPanel.setInAnimation(AnimationUtils.loadAnimation(this,
		// android.R.anim.accelerate_interpolator));
		// imgPanel.setOutAnimation(AnimationUtils.loadAnimation(this,
		// android.R.anim.slide_in_left));
		// TODO: look into speeding up ImageSwitcher animations

	}

	/**
	 * Called when the "ResumeReading" option is invoked. Gets the last file
	 * read and the page number the reader was on.
	 * */
	public void resumeReading() {

		this.tempFile = settings.getLastFileRead();

		if (this.tempFile.exists()) {
			if (ImageParser.isSupportedImage(this.tempFile) || this.tempFile.isDirectory()) {
				this.onResume();
			} else if (ArchiveParser.isSupportedArchive(this.tempFile)) {
				this.getSupportActionBar().hide();
				vf.showNext();
				int index = settings.getLastFileReadIndex();
				
				progressThread = new Progress(this.tempFile, this, index);
				progressThread.start();
			}
		}

	}

	/**
	 * @param cache
	 *            The object this class will read images from
	 * */
	public void setCache(Steppable cache) {
		this.cache = cache;
		this.cache.sort();
	}

	/**
	 * @return Returns the Steppable object this class reads images from
	 * */
	public Steppable getCache() {
		return this.cache;
	}

	/**
	 * @return Returns the file this class is temporarily storing
	 * */
	public File getTempFile() {
		return this.tempFile;
	}

	/**
	 * @param f
	 *            File to store in this class temporarily for parsing
	 * */
	public void setTempFile(File f, int index) {

		settings.setHomeDir(f.getParent());
		settings.setLastReadIndex(index);

		if (settings.saveRecent()) {
			settings.addRecent(f.getAbsolutePath(), index);
		}

		this.tempFile = f;
		this.tempDir = f.getParentFile();
	}

	@Override
	public void onResume() {
		super.onResume();

		settings.forceOrientation(this);
		enableResumeReading();
		if (this.tempFile != null) {

			this.getSupportActionBar().hide();
			vf.showNext();
			int index = settings.getLastFileReadIndex();
			settings.setLastRead(this.tempFile, index);
			progressThread = new Progress(this.tempFile, this, index);
			progressThread.start();

		}
	}

	/**
	 * Swaps this class's ViewFlipper to the next view
	 * */
	public void showNext() {
		runOnUiThread(new ViewFlipThreadForward(this));
	}

	/**
	 * Swaps this class's ViewFlipper to the previous view
	 * */
	public void showPrevious() {
		runOnUiThread(new ViewFlipThreadBackward(this));
	}

	/**
	 * @param forward
	 *            If true, next view of this class's ViewFlipper. If false,
	 *            shows its previous view.
	 * */
	public synchronized void flipView(boolean forward) {

		if (forward) {
			vf.showNext();
		} else {
			vf.showPrevious();
		}

	}

	/**
	 * @param i
	 *            Set the index of the Steppable's cache
	 * */
	public void setCacheIndex(int i) {
		
		if (this.cache != null){
			this.cache.setIndex(i);
		}
		
	}

	/**
	 * @param absoluteFilePath
	 *            Path to add to Steppable's cache
	 * @param name
	 *            Name of the file to add to cache
	 * */
	public void addImageToCache(Object absoluteFilePath, String name) {
		this.cache.addImageToCache(absoluteFilePath, name);
	}

	/**
	 * @param d
	 *            Image for this Activity to display
	 * */
	public void setImage(JBitmapDrawable d) {

		if ((cache != null)) {
			runOnUiThread(new SetImageThread(d));
		}

	}

	/**
	 * Used so that the image can be changed from another thread. Useful when
	 * processing an image in the background before displaying it
	 * */
	private class SetImageThread extends Thread {

		private final JBitmapDrawable d;

		public SetImageThread(JBitmapDrawable d) {
			this.d = d;
		}

		@Override
		public void run() {
			if (imgPanel.getImageDrawable() != null){
				//imgPanel.getImageDrawable().closeBitmap();
				imgPanel.setImageDrawable(null);
			}
			imgPanel.setImageDrawable(d);
		}

	}

	/**
	 * @return Returns the JBitmapDrawable displayed
	 * */
	public JBitmapDrawable getImage() {

		return imgPanel.getImageDrawable();

	}

	/**
	 * @return Directory of the current tempFile.
	 * */
	public File getTempDir() {
		return this.tempDir;
	}

	/**
	 * @return SettingsManager responsible for storing this application's
	 *         settings.
	 * */
	public SettingsManager getSettings() {
		return this.settings;
	}

	/**
	 * @return Screen width
	 * */
	public int getWidth() {
		return this.width;
	}

	/**
	 * @return Screen height
	 * */
	public int getHeight() {
		return this.height;
	}

	/**
	 * @param resIdConfirm
	 *            ID of the string resource to be displayed as a confirm option
	 * @param resIdDecline
	 *            ID of the string resource to be displayed as a decline option
	 * @param message
	 *            Actual prompt/question to be displayed
	 * @param target
	 *            ModalReturn to target with the prompt created
	 * */
	public void confirm(int resIdConfirm, int resIdDecline, String message,
			ModalReturn target) {
		ConfirmDialog confirm = new ConfirmDialog(getString(resIdConfirm),
				getString(resIdDecline), message, target);
		confirm.show(getSupportFragmentManager(), "MainActivity");
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
		SimpleDateFormat fmtDate = new SimpleDateFormat("hh:mma  dd/MM/yyyy",
				Locale.getDefault());

		String pattern = fmtDate.toLocalizedPattern();
		fmtDate.applyPattern(pattern);
		return fmtDate.format(timeMillis);
	}

	/*
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	//ActionBar bar = getSupportActionBar();
    	//ActionBar.Tab tab = bar.getSelectedTab();
    	
		//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		//int position = info.position;
		
		//ListView lv = (ListView) v;
		//String name = lv.getAdapter().getItem(position).toString();
		
		//menu.setHeaderTitle(name);
		menu.add(Menu.NONE, 0, 0, "Jump to page");
    	
    }
	
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item){
    	
    	//AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	//String s = info.toString();
    	//cache.setIndex(71); //TODO: Prompt for page to jump to; Validate against getMax and getMin
    	//cache.clear();
    	
    	return true;
    	
    }*/
	
	
	
}
