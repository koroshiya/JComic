package com.japanzai.koroshiya.filechooser;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.io_utils.StorageHelper;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.MessageThread;
import com.japanzai.koroshiya.reader.ToastThread;
import com.japanzai.koroshiya.settings.SettingsView;
import com.japanzai.koroshiya.settings.classes.Recent;

/**
 * Listener for OnItemClick events pertaining to items listed by the FileChooser tree of classes.
 * ie. It listens for clicks on files displayed for reading, deletion, navigation, etc.
 * */
public class ItemClickListener implements View.OnClickListener, View.OnLongClickListener, ModalReturn {

	private final FileChooser parent;
	private File tempFile = null;
    private String tmpLocation = null;

    public static final int READ_EXTERNAL_STORAGE_PERMISSION = 100;
	
	public ItemClickListener(FileChooser parent){
		
		this.parent = parent;
		
	}

    public String getTmpLocation(){return this.tmpLocation;}
    public void setTmpLocation(String tmpLocation){this.tmpLocation = tmpLocation;}

    private boolean promptExternal(String location){
        if (!new File(location).canRead()){
            if (StorageHelper.isExternalStorageReadable() &&
                    ContextCompat.checkSelfPermission(parent, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                tmpLocation = location;
                ActivityCompat.requestPermissions(
                        parent,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION
                );
            }else{
                parent.runOnUiThread(new ToastThread("Cannot read - Permission denied", parent));
            }
            return false;
        }else{
            return true;
        }
    }
	
	@Override
	public void onClick(View arg1) {
        String location = arg1.getContentDescription().toString();
        if (!location.startsWith("/")) {
            String home = parent.getHome();
            if (!home.endsWith("/")) home += "/";
            location = home + location;
        }
		onClick(location);
	}

    public void onClick(String location){
        if (location.equals(getString(R.string.recent_function_disabled)) || location.equals(getString(R.string.recent_no_recent_files))){
            return;
        }
        if (!promptExternal(location)){
            return;
        }

        boolean processed = false;
        if (parent.getType() == FileChooser.RECENT){
            File file = new File(location);
            if (!file.exists()){
                processItem(false, location);
                processed = true;
            }
            for (Recent recent : MainActivity.getMainActivity().getSettings().getRecent()){
                if (recent.getPath().equals(location)){
                    parent.returnValue(file, recent.getPageNumber());
                    return;
                }
            }
        }
        if (!processed) processItem(false, location);
    }

    @Override
    public boolean onLongClick(View v){
        final String name = v.getContentDescription().toString();

        if (name.equals(getString(R.string.recent_function_disabled)) ||
                name.equals(getString(R.string.recent_general_settings)) ||
                name.equals(getString(R.string.recent_no_recent_files))){
            Log.d("FileChooser", "Failed to instantiate context menu");
            return false;
        }

        final Dialog dialog = new Dialog(parent);
        dialog.setContentView(R.layout.list);
        dialog.setCancelable(true);

        if (name.contains("/")){
            int inx = name.lastIndexOf('/');
            if (inx == name.length() - 1){
                String str = name.substring(0, inx);
                inx = str.contains("/") ? str.lastIndexOf('/') : -1;
            }
            dialog.setTitle(name.substring(inx + 1));
        }else {
            dialog.setTitle(name);
        }


        int id = processContext(name);
        if (id == -1){
            return false; //File has been deleted
        }

        ArrayList<HashMap<String, String>> map = new ArrayList<>();

        final ArrayList<String> tempCommandList = new ArrayList<>();
        tempCommandList.addAll(Arrays.asList(v.getResources().getStringArray(id)));

        if (parent.getType() == FileChooser.RECENT){
            tempCommandList.add(parent.getResources().getString(R.string.file_remove_recent));
            tempCommandList.add(parent.getResources().getString(R.string.file_clear_recent));
        } else if (parent.getType() == FileChooser.FAVORITE){
            tempCommandList.remove(tempCommandList.size() - 1); //removes the add to favorites command
            tempCommandList.add(parent.getResources().getString(R.string.file_remove_favorite));
            tempCommandList.add(parent.getResources().getString(R.string.file_clear_favorite));
        }

        ListView lv = (ListView) dialog.findViewById(R.id.lv);
        for (int i = 0; i < tempCommandList.size(); i++){
            HashMap<String, String> hm = new HashMap<>();
            hm.put("name", tempCommandList.get(i));
            map.add(hm);
        }

        SimpleAdapter itemAdapter = new SimpleAdapter(parent, map, R.layout.context_list_item, new String[]{"name"}, new int[]{R.id.row_text});
        lv.setAdapter(itemAdapter);
        lv.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView av, View v, int i, long l){
                process(name, tempCommandList.get(i));
                dialog.dismiss();
            }
        });

        dialog.show();
        return false;
    }
	
	/**
	 * @param name String value of the file we want to process
	 * @return Returns the ID of an array resource to populate the context menu
	 * */
	public int processContext(String name){
		
		String location = name.startsWith("/") ? name : parent.getHome() + File.separator + name;
		File file = new File(location);
		
		if (!file.exists()){
			return -1;
		}

		int id;
		boolean writeable = file.canWrite();
		
		if (file.isDirectory()){
			id = writeable ? R.array.array_context_dir_rw : R.array.array_context_dir_r;
		}else if (ArchiveParser.isSupportedArchive(file)){
			id = writeable ? R.array.array_context_file_rw : R.array.array_context_file_r;
		}else{
			id = writeable ? R.array.array_context_file_rw : R.array.array_context_file_r;
		}
		
		return id;
		
	}
	
	/**
	 * @param name String representing the location of a file to process
	 * @param command Command to perform on the file represented by 'name'
	 * */
	public void process(String name, String command) {
		
		String location = name.startsWith("/") ? name : parent.getHome() + File.separator + name;
		File file = new File(location);
		String[] options = parent.getResources().getStringArray(R.array.array_context_dir_rw);
		
		if (command.equals(options[0])){ //Open
			
			processItem(false, name);
			
		}else if (command.equals(options[1])){ //Read
			
			processItem(true, name);
			
		}else if (command.equals(options[2])){ //Delete
			
			delete(file);
			
		}else if (command.equals(options[3])){ //Properties
			
			properties(file);
			
		}else if (command.equals(options[4])){ //Scan
			
			scan(file);
			
		}else if (command.equals(options[5])){ //Favorite
			
			favorite(file);
			
		}else if (command.equals(getString(R.string.file_remove_favorite))){
			
			MainActivity.getMainActivity().getSettings().removeFavorite(name);
			parent.refreshTab();
			
		}else if (command.equals(getString(R.string.file_remove_recent))){
			
			MainActivity.getMainActivity().getSettings().removeRecent(name);
			parent.refreshTab();
			
		}else if (command.equals(getString(R.string.file_clear_favorite))){
			
			MainActivity.getMainActivity().getSettings().clearFavorites();
			parent.refreshTab();
			
		}else if (command.equals(getString(R.string.file_clear_recent))){
			
			MainActivity.getMainActivity().getSettings().clearRecent();
			parent.refreshTab();
			
		}
		
	}
	
	/**
	 * Displays a confirmation dialog for whether or not to delete a file
	 * @param file File to prompt for the deletion of
	 * */
	private void delete(File file){
		
		this.tempFile = file;
		boolean dir = file.isDirectory();
		String type = getString(dir ? R.string.file_folder_and_contents : R.string.file_file);
		parent.confirm(
                getString(R.string.file_delete_prompt) + " " + type + "?", this);
		
	}
	
	@Override
	public void accept(){
		
		String message;
		if (tempFile.exists() && deleteFile(tempFile)){
			parent.setHome(tempFile.getParentFile());
			message = getString(R.string.file_delete_successful);
			parent.refreshTab();
		}else{
			message = getString(tempFile.isDirectory() ? 
								R.string.file_folder_delete_fail : 
								R.string.file_file_delete_fail);
		}
		tempFile = null;
		parent.runOnUiThread(new MessageThread(message, parent));
	}
	
	@Override
	public void decline(){
		tempFile = null;
	}
	
	/**
	 * Adds a file to the list of favorites
	 * @param f File to add to favorites
	 * */
	private void favorite(File f){
		MainActivity.getMainActivity().getSettings().addFavorite(f.getAbsolutePath());
	}
	
	/**
	 * Convenience method for calling the getString method of an Activity
	 * @param id ID of the String resource to get the value of
	 * @return Text contained in the String resource pointed to
	 * */
	private String getString(int id){
		return parent.getString(id);
	}
	
	/**
	 * Attempts to delete a file or directory
	 * @param file File or directory to delete
	 * @return If all files were deleted successfully, returns true.
	 * If at least one file couldn't be deleted, returns false.
	 * */
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
	 * Displays the properties of a file
	 * @param file File to display the properties of
	 * */
	private void properties(File file){
		
		ArrayList<String> properties = new ArrayList<>();
		
		properties.add(getString(R.string.file_name) + file.getName());
		
		double size = file.length();
		double result;
		DecimalFormat f = new DecimalFormat("##.00");
		Object[][] valuePairs = {
				{3d, getString(R.string.file_size_gigabytes)},
				{2d, getString(R.string.file_size_megabytes)},
				{1d, getString(R.string.file_size_kilobytes)},
				{0d, getString(R.string.file_size_bytes)}
		};
        for (Object[] pair : valuePairs){
			result = Math.pow(1024, (Double)pair[0]);
			if (size >= result){
				size /= result;
				properties.add(getString(R.string.file_size) + f.format(size) + pair[1]);
				break;
			}
		}

		String formattedDate = MainActivity.formatDate(file.lastModified());
		properties.add(getString(R.string.file_date_last_modified) + " " + formattedDate);
		
		parent.runOnUiThread(new MessageThread(properties, parent));
		
	}
	
	/**
	 * Scans a file for supported images. 
	 * Works for Directories and supported archives.
	 * @param file File to scan for supported images
	 * */
	private void scan(File file){
		
		ArrayList<String> contents = new ArrayList<>();
		
		if (file.isDirectory()){
	    	File[] files = file.listFiles();
	    	Arrays.sort(files);
	    	for (File f : files){
				if (ImageParser.isSupportedImage(f) || ArchiveParser.isSupportedArchive(f)){
					contents.add(f.getName());
				}
			}
		}else if (ArchiveParser.isSupportedArchive(file)){
            try {
                SteppableArchive arc = ArchiveParser.parseArchive(file, null);
                if (arc != null) contents = arc.peekAtContents();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
			contents.add(file.getName());
		}

		if (contents.size() > 0){
			parent.runOnUiThread(new MessageThread(contents, parent));
		}else{
			parent.runOnUiThread(new MessageThread(R.string.file_no_supported, parent));
		}
		
	}
	
	/**
	 * Purpose: Processes a click. 
	 * 			If a directory is clicked, user navigates into it.
	 * 			If a directory is held, the directory is returned.
	 * 			If a file is clicked, the file is returned.
	 * @param forceReturn If true, directories will be parsed instead of opened
	 * @param name Text displayed on the control pressed/held down
	 * */
	private void processItem(boolean forceReturn, String name){
		
		String location;
		
		try{
			
			if (name.equals(parent.getString(R.string.recent_general_settings))){
				Log.d("ItemClickListener", "Going to SettingsView");
				Intent i = new Intent(parent, SettingsView.class);
				parent.startActivity(i);
				return;
			}else{
				if (name.startsWith("/")){
					location = name;
				}else{
					location = parent.getHome() + File.separator + name;
				}
			}
		}catch (NullPointerException ex){
			ex.printStackTrace();
			return;
		}

        Log.d("ItemClickListener", "Processing item " + name);
		
		processItemAfter(location, forceReturn);
		
	}
	
	public void processItemAfter(String location, boolean forceReturn){
		
		File file = new File(location);
		
		if (!file.exists()){
            Log.i("ItemClickListener", location + " doesn't exist");
    		parent.runOnUiThread(new ToastThread(R.string.cant_go_up, parent));
		}else if (file.isFile() || forceReturn){
            Log.i("ItemClickListener", "Force return");
			for (Recent recent : MainActivity.getMainActivity().getSettings().getRecent()){
				if (recent.getPath().equals(location)){
					parent.returnValue(file, recent.getPageNumber());
					return;
				}
			}
			int i = 0;
            if (!ArchiveParser.isSupportedArchive(file)) {
                Log.d("ItemClickListener", "Processing item after " + file.getName());
                File[] files = file.getParentFile().listFiles();
                Arrays.sort(files);
                Log.d("ItemClickListener", "List of files: ");
                for (File f : files) {
                    if (ImageParser.isSupportedImage(f)) {
                        Log.d("ItemClickListener", f.getName());
                        if (f.getName().equals(file.getName())) {
                            break;
                        }
                        i++;
                    }
                }
            }
			parent.returnValue(file, i);
		}else {
            Log.i("ItemClickListener", "Reset");
			parent.reset(file);
		}
		
	}
	
}
