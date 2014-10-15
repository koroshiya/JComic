package com.japanzai.koroshiya;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.cache.FileCache;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.interfaces.archive.ReadableArchive;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.MessageThread;
import com.japanzai.koroshiya.settings.SettingsManager;

import de.innosystec.unrar.exception.RarException;

/**
 * Purpose: Parses directories, image files and archives.
 * 			Essentially initializes the main activity's cache.
 * */
public class Progress extends Thread implements ModalReturn{
	
	private final File f;
	private final MainActivity parent;
	private final int index;
	private ReadableArchive temp;
	
    public Progress(File f, MainActivity parent) {
        this(f, parent, -1);
    }
    
    public Progress(File f, MainActivity parent, int index){
        this.f = f;
        this.parent = parent;
        this.index = index;
    }
	
    @Override
	public void run(){
		System.out.println(f.getName());
    	if (f.isDirectory()) { 
    		parent.setCache(new FileCache(parent, f.getAbsolutePath()));
    		parseDir(f, 0);
    		parent.setCacheIndex(this.index == -1 ? 0 : this.index);
    	}else if (ArchiveParser.isSupportedArchive(f)){
	    	if (!parseArchive()){
	    		parent.clearTempFile();
	    		decline();
	    		parent.runOnUiThread(new MessageThread(R.string.archive_read_error, parent));
	        	return;
	    	}
    	}else { 
    		parent.setCache(new FileCache(parent, f.getAbsolutePath()));
    		File parentDir = new File(f.getParent());
    		File[] list = parentDir.listFiles();
    		
    		for (int i = 0; i < list.length; i++){
    			parseFile(list[i]);
    			if (list[i].getName().equals(f.getName())){
    				parent.setCacheIndex(this.index == -1 ? 0 : this.index);
    			}
    		}
    	}
    	if (parent.getCache().getMax() > 0){
    		finish();
    	}else{
    		parent.runOnUiThread(new MessageThread(R.string.no_images, parent));
    		decline();
    	}
	}
    
    /**
     * @param file File to be tested. If supported, the file is processed.
     * */
    public void parseFile(File file){
    	
    	if (ImageParser.isSupportedImage(file)){
    		if (file.length() > 0) parent.addImageToCache(file.getAbsolutePath(), file.getAbsolutePath());
    		//Log.e("New file", file.getAbsolutePath());
    	}else{
    		System.out.println(R.string.unsupported_file + file.getName());
    	}
    	
    }
    
    /**
     * @param dir Directory to process the contents of
     * */
    public void parseDir(File dir, int curLevel){
    	SettingsManager settings = MainActivity.mainActivity.getSettings();
    	for (File f : dir.listFiles()){
    		if (f.isFile()){
    			parseFile(f);
    		}else{
    			if (curLevel < settings.getRecursionLevel() || settings.getRecursionLevel() == SettingsManager.RECURSION_ALL){
    				parseDir(f, curLevel + 1);
    			}
    		}
    	}
    }
    
    /**
     * @param file File to parse; tests if the file is a supported archive
     * */
    public boolean parseArchive(){
    	    	
    	try {
    		
    		this.temp = ArchiveParser.parseArchive(f);
    		int archiveIndex = parent.getSettings().getArchiveModeIndex(); //0 = do as I please, 1 = Index only, 2 = progressive
    		
    		if (temp == null){
    			parent.runOnUiThread(new MessageThread(parent.getString(R.string.archive_read_error), parent));
    			return true;
    		}
    		
    		if (temp instanceof SteppableArchive){
    			parent.setCache((SteppableArchive) this.temp);
    		}else if (archiveIndex == 1){
    			parent.confirm(R.string.file_confirm, R.string.file_deny, 
    					parent.getString(R.string.archive_index_error) + "\n" + 
    					parent.getString(R.string.archive_index_error2), this);
    			return false;
    		}else{
    			extract(temp);
    		}
        	
    		parent.setCacheIndex(this.index == -1 ? 0 : this.index);
    		
		} catch (ZipException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (RarException e) {
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    	    	
    }
    
    @Override
    public void accept(){
    	parent.setCache((SteppableArchive) this.temp);
    	finish();
    }
    
    @Override
    public void decline(){
    	parent.showNext();
    	parent.showNext();
    }
    
    /**
     * Called once processing is done. 
     * Has the parent process the temp file already sent to it.
     * */
    private void finish(){
    	parent.clearTempFile();
    	parent.showNext();
    }
    
    /**
     * @param temp Archive to extract
     * */
    private void extract(ReadableArchive temp){
    	
    	File tmpDir = parent.getCacheDir();
    	File jTmp = new File(tmpDir + File.separator + "JComic");
    	if (jTmp.exists()){
    		for (File f : jTmp.listFiles()){
        		f.delete();
        	}
    	}else{
    		jTmp.mkdirs();
    	}
    	
    	temp.extractContentsToDisk(jTmp, null);
    	for (File f : jTmp.listFiles()){
    		f.deleteOnExit();
    	}
    	jTmp.deleteOnExit();
    	parent.setCache(new FileCache(parent, f.getAbsolutePath()));
    	parseDir(jTmp, 0);
    	
    }
	
}
