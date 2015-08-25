package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.japanzai.koroshiya.cache.IndexThread;
import com.japanzai.koroshiya.cache.StepThread;
import com.japanzai.koroshiya.cache.Steppable;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Represents an archive that can be moved through sequentially and randomly.
 * 			Non-steppable archives can only be moved through sequentially.
 * */
public abstract class SteppableArchive extends Steppable {
	
	protected final File tempDir;
	protected final boolean progressive;
    protected final int width;
    protected final int extractMode;
	
	public SteppableArchive(Reader reader, String path){
		
		super(path);

        if (reader != null) {
            this.tempDir = new File(reader.getCacheDir() + "/JComic/");
            this.width = reader.getWidth();
            this.extractMode = reader.getSettings().getDynamicResizing();

            if (this.tempDir.exists() && this.tempDir.isDirectory()) {
                deleteFile(this.tempDir);
            }
            this.tempDir.mkdirs();
        }else{
            tempDir = null;
            this.width = 1;
            this.extractMode = 0;
        }

		int mode = MainActivity.getMainActivity().getSettings().getCacheModeIndex();
		if (this instanceof JRarArchive){
			this.progressive = (mode == 0 || mode == 2);
		}else{
			this.progressive = mode == 2;
		}
		
	}
	
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

    /**
     * @return Returns the names of files within.
     * */
    public abstract ArrayList<String> peekAtContents();

    @Override
    public StepThread getThread(boolean primary, boolean forward) {
        return new StepThread(this, primary, forward, width, extractMode);
    }
	
}
