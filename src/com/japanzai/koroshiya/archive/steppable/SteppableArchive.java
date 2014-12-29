package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;

import android.widget.TextView;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.cache.Steppable;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.interfaces.archive.ReadableArchive;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Represents an archive that can be moved through sequentially and randomly.
 * 			Non-steppable archives can only be moved through sequentially.
 * */
public abstract class SteppableArchive extends Steppable implements ReadableArchive{
	
	protected final File tempDir;
	protected final boolean progressive;
	protected SetTextThread thread = null;
	
	public SteppableArchive(Reader parent, String path){
		
		super(parent, path);
        if (parent != null) {
            File tmp = parent.getCacheDir();
            this.tempDir = new File(tmp + "/JComic/");
            System.out.println(this.tempDir);

            if (this.tempDir.exists() && this.tempDir.isDirectory()) {
                deleteFile(this.tempDir);
            }
            this.tempDir.mkdirs();
        }else{
            tempDir = null;
        }

		int mode = MainActivity.mainActivity.getSettings().getCacheModeIndex();
		if (this instanceof JRarArchive){
			this.progressive = (mode == 0 || mode == 2);
		}else{
			this.progressive = mode == 2;
		}
		
	}
	
	@Override
	public void parseCurrent() throws IOException  {

    	getParent().setImage(parseImage(getIndex()));
    	
    	SettingsManager settings = super.parent.getSettings();
    	if (settings.isCacheOnStart()){
    		super.cache(settings.getCacheModeIndex() != 2);
    	}
		
	}
	
	/**
	 * @return Returns the archive concealed within.
	 * */
	public abstract Object getArchive();

    /**
     * @param i Index of the entry to return
     * @return Object extracted from the Steppable at the index parsed in.
     * 			May be a String reference, a RAR file header, etc.
     * */
    public abstract Object getEntry(int i);
	
	private boolean deleteFile(File file){
		
		boolean completeSuccess = true;
		
		if (file.isDirectory()){
			for (File f : file.listFiles()){
				if (!deleteFile(f)){
					completeSuccess = false;
				}
			}
			if (!file.delete()){
				completeSuccess = false;
			}
		}else {
			if (!file.delete()){
				completeSuccess = false;
			}
		}
		
		return completeSuccess;
		
	}
	
	protected class SetTextThread extends Thread{
		
		private final String text;
		private final FileChooser fc;
		
		public SetTextThread(String text, FileChooser fc){
			this.text = text;
			this.fc = fc;
		}
		
		@Override
		public void run(){
			TextView tv = (TextView)fc.findViewById(R.id.progressText);
			tv.setText(text);
		}
		
	}
	
}
