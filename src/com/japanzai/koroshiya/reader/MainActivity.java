package com.japanzai.koroshiya.reader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
<<<<<<< HEAD

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.EllipsizingTextView;
import com.japanzai.koroshiya.controls.ResizingGridView;
=======
import android.widget.GridView;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.EllipsizingTextView;
>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9
import com.japanzai.koroshiya.settings.SettingsManager;

public class MainActivity extends Activity {

	public static MainActivity mainActivity;
	private SettingsManager settings;
	
	public File tempDir;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//this.setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
        SettingsManager.setFullScreen(this);
		instantiate();

	}

	public void instantiate() {

		MainActivity.mainActivity = this;
		settings = new SettingsManager(this);
		settings.keepBacklightOn(getWindow());

		setContentView(R.layout.activity_main);

        ResizingGridView grid = (ResizingGridView) findViewById(R.id.FileChooserPane);
        grid.setCentered(true);

        MainItem[] items = new MainItem[]{
                new MainItem(R.string.description_read, R.drawable.ic_read),
                new MainItem(R.string.description_resume, R.drawable.ic_saved),
                new MainItem(R.string.description_settings, R.drawable.ic_gear),
                new MainItem(R.string.description_help, R.drawable.ic_help),
                new MainItem(R.string.description_credits, R.drawable.ic_credit),
                new MainItem(R.string.error_button, R.drawable.ic_error_report)
        };

        grid.setAdapter(new MainAdapter(this, items));

	}

    public class MainItem{

        private final int str;
        private final int draw;

        public MainItem(int str, int draw){
            this.str = str;
            this.draw = draw;
        }

        public int getStringResource(){
            return str;
        }

        public int getDrawableResource(){
            return draw;
        }
    }

    public class MainAdapter extends BaseAdapter {

        final MainItem[] items;
        final MainActivity c;

        public MainAdapter(MainActivity c, MainItem[] items) {
            this.c = c;
            this.items = items;
        }

        public long getItemId(int position) {
            return 0;
        }

        public MainItem getItem(int position) {
            return items[position];
        }

        public int getCount() {
            return items.length;
        }

        @Override
        public EllipsizingTextView getView(int position, View v, ViewGroup parent) {

            EllipsizingTextView tv;

            if (v == null){
                tv = (EllipsizingTextView)LayoutInflater.from(c).inflate(R.layout.list_item, null);
            }else{
                tv = (EllipsizingTextView) v;
            }
            tv.setMaxLines(2);
            tv.setMinLines(2);

            MainItem p = getItem(position);
            tv.setCompoundDrawablesWithIntrinsicBounds(0, p.getDrawableResource(), 0, 0);
            tv.setText(p.getStringResource());
            tv.setOnClickListener(new MainClickListener(c));

            if (p.getStringResource() == R.string.description_resume) {
                File savedFile = settings.getLastFileRead();
                boolean enabled = savedFile != null && savedFile.exists() && settings.saveSession();
                tv.setEnabled(enabled);
            }

            return tv;

        }
    }

	/**
	 * Called when the "ResumeReading" option is invoked. Gets the last file
	 * read and the page number the reader was on.
	 * */
	public void resumeReading(){
		startReading(settings.getLastFileRead().getAbsolutePath(), settings.getLastFileReadIndex());
	}
	
	public void startReading(String path, int index){
		
		if (!(new File(path).exists())) return;

        if (index < 0) index = 0;
		
		Intent intent = new Intent(this, Reader.class);
		Bundle b = new Bundle();
		b.putString("file", path);
        b.putInt("index", index);
        intent.putExtras(b);
        //settings.setLastRead(new File(path), index);
		startActivity(intent);
		
	}

	@Override
	public void onResume() {
		super.onResume();
		settings.forceOrientation(this);
        instantiate();
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

    public static void hideActionBar(Activity act){
        if (android.os.Build.VERSION.SDK_INT >= 11)
            if (act.getActionBar() != null)
                act.getActionBar().hide();
    }
	
}
