package com.japanzai.koroshiya.filechooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.interfaces.ModalReturn;
import com.japanzai.koroshiya.interfaces.archive.ReadableArchive;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.MessageThread;
import com.japanzai.koroshiya.reader.ToastThread;
import com.japanzai.koroshiya.settings.SettingsView;
import com.japanzai.koroshiya.settings.classes.Recent;

import de.innosystec.unrar.exception.RarException;

/**
 * Listener for OnItemClick events pertaining to items listed by the FileChooser tree of classes.
 * ie. It listens for clicks on files displayed for reading, deletion, navigation, etc.
 * */
public class ItemClickListener implements OnItemClickListener, ModalReturn {

	private final FileChooser parent;
	private File tempFile = null;
	private Thread runningThread = null;
	
	public ItemClickListener(FileChooser parent){
		
		this.parent = parent;
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		TextView tv = (TextView) arg1.findViewById(R.id.row_text);
		final String location = tv.getText().toString();
		
		if (location.equals(getString(R.string.recent_function_disabled)) ||
				location.equals(getString(R.string.recent_general_settings)) ||
				location.equals(getString(R.string.recent_no_recent_files)))
		{
			return;
		}
		
		ActionBar bar = parent.getSupportActionBar();
		if (bar.getSelectedTab() == bar.getTabAt(0)){
			File file = new File(location);
			if (!file.exists()){
				processItem(false, location);
			}
			for (Recent recent : MainActivity.mainActivity.getSettings().getRecent()){
				if (recent.getPath().equals(location)){
					parent.returnValue(file, recent.getPageNumber());
					return;
				}
			}
		}
		processItem(false, location);
		
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
			id = writeable ? R.array.array_context_archive_rw : R.array.array_context_file_r;
		}else{
			id = writeable ? R.array.array_context_file_rw : R.array.array_context_file_r;
		}
		
		return id;
		
	}
	
	/**
	 * @param name String representing the location of a file to process
	 * @param command Command to perform on the file represented by 'name'
	 * @throws IOException 
	 * @throws RarException 
	 * */
	public void process(String name, String command) throws IOException, Exception{
		
		String location = name.startsWith("/") ? name : parent.getHome() + File.separator + name;
		File file = new File(location);
		String[] options = parent.getResources().getStringArray(R.array.array_context_dir_rw);
		String[] altOptions = parent.getResources().getStringArray(R.array.array_context_archive_rw);
		
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
			
		}else if (command.equals(options[6])){ //Zip supported images
			
			zip(file, false);
			
		}else if (command.equals(options[7])){ //Zip and delete

			zip(file, true);
			
		}else if (command.equals(altOptions[5])){ //Extract archive
			
			extract(file, false);
			
		}else if (command.equals(altOptions[6])){ //Extract archive and delete

			extract(file, true);
			
		}else if (command.equals(getString(R.string.file_remove_favorite))){
			
			MainActivity.mainActivity.getSettings().removeFavorite(name);
			parent.refreshTab();
			
		}else if (command.equals(getString(R.string.file_remove_recent))){
			
			MainActivity.mainActivity.getSettings().removeRecent(name);
			parent.refreshTab();
			
		}else if (command.equals(getString(R.string.file_clear_favorite))){
			
			MainActivity.mainActivity.getSettings().clearFavorites();
			parent.refreshTab();
			
		}else if (command.equals(getString(R.string.file_clear_recent))){
			
			MainActivity.mainActivity.getSettings().clearRecent();
			parent.refreshTab();
			
		}/*else if (command.equals("Enter password")){
			//TODO: prompt for password
		}*/
		
	}
	
	/**
	 * Displays a confirmation dialog for whether or not to delete a file
	 * @param file File to prompt for the deletion of
	 * */
	private void delete(File file){
		
		this.tempFile = file;
		boolean dir = file.isDirectory();
		String type = getString(dir ? R.string.file_folder_and_contents : R.string.file_file);
		parent.confirm(R.string.file_confirm, R.string.file_deny, 
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
		MainActivity.mainActivity.getSettings().addFavorite(f.getAbsolutePath());
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
		
		ArrayList<String> properties = new ArrayList<String>();
		
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
		for (int i = 0; i < valuePairs.length; i++){
			result = Math.pow(1024, (Double)valuePairs[i][0]);
			if (size >= result){
				size /= result;
				properties.add(getString(R.string.file_size) + f.format(size) + (String)valuePairs[i][1]);
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
		
		ArrayList<String> contents = null;
		
		if (file.isDirectory()){
			contents = new ArrayList<String>();
			for (File f : file.listFiles()){
				if (ImageParser.isSupportedImage(f) || ArchiveParser.isSupportedArchive(f)){
					contents.add(f.getName());
				}
			}
		}else if (ArchiveParser.isSupportedArchive(file)){
			try {
				ReadableArchive archive = ArchiveParser.parseArchive(file);
				contents = archive.peekAtContents();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}else {
			contents = new ArrayList<String>();
			contents.add(file.getName());
		}
		
		
		if (contents != null && contents.size() != 0){
			parent.runOnUiThread(new MessageThread(contents, parent));
		}else{
			parent.runOnUiThread(new MessageThread(R.string.file_no_supported, parent));
		}
		
	}
	
	/**
	 * @throws IOException 
	 * */
	private void zip(File dir, boolean delete){
		
		if (runningThread == null || !runningThread.isAlive()){
			runningThread = new ZipThread(dir, delete);
			runningThread.start();
		}
		
	}
	
	private void extract(File f, boolean delete){
		
		if (runningThread == null || !runningThread.isAlive()){
			runningThread = new ExtractThread(f, delete);
			runningThread.start();
		}
		
	}
	
	private class ExtractThread extends Thread{
		
		private final File f;
		private final boolean delete;
		
		public ExtractThread(File f, boolean delete){
			this.f = f;
			this.delete = delete;
		}
		
		@Override
		public void run(){

			parent.showNext();
			boolean success = false;
			String message;
			
			if (ArchiveParser.isSupportedArchive(f)){
				ReadableArchive arch = null;
				try {
					arch = ArchiveParser.parseArchive(f);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				File outputDir = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf('.')));
				if (outputDir.exists() && outputDir.isFile()){
					message = getString(R.string.zip_directory_creation_failed);
				}else{
					if (!outputDir.exists()){
						outputDir.mkdirs();
					}
					if (ArchiveParser.extractArchiveToDisk(arch, outputDir, parent)){
						message = getString(R.string.zip_successfully_extracted);
						success = true;
					}else{
						message = getString(R.string.zip_extract_failed);
					}
				}
			}else{
				message = getString(R.string.zip_no_supported_archive);
			}

			parent.runOnUiThread(new MessageThread(message, parent));
			
			if (success && delete){
				deleteFile(f);
			}
			
			parent.showPrevious();
			parent.runOnUiThread(new RefreshThread());
			
		}
		
	}
	
	private class RefreshThread extends Thread{
		@Override
		public void run(){
			parent.refreshTab();
		}
	}
	
	private class ZipThread extends Thread{
		
		private final File dir;
		private final boolean delete;
		private final FileChooser fc;
		
		public ZipThread(File dir, boolean delete){
			this.dir = dir;
			this.delete = delete;
			this.fc = parent;
		}
		
		@Override
		public void run(){

			parent.showNext();
			ArrayList<File> imagesToZip = new ArrayList<File>();
			
			for (File file : dir.listFiles()){
				if (ImageParser.isSupportedImage(file)){
					imagesToZip.add(file);
				}
			}
			
			String message;
			boolean success = false;
			
			if (imagesToZip.size() == 0){
				message = getString(R.string.zip_no_supported_images);
			}else{
				File output = new File(dir.getAbsolutePath() + ".zip");
				if (output.exists()){
					message = getString(R.string.zip_already_exists);
				}else{
					try{
						ZipArchiveOutputStream zip = new ZipArchiveOutputStream(output);
						ZipArchiveEntry entry;
						byte[] array;
						String name;
						SetTextThread thread = null;
						for (File f : imagesToZip){

							name = f.getName();
							
							if (fc != null && (thread == null || !thread.isAlive())){
								thread = new SetTextThread(fc.getText(R.string.zip_zipping_in_progress) + ": " + name, fc);
								fc.runOnUiThread(thread);
							}
							
							entry = new ZipArchiveEntry(f.getName());
							zip.putArchiveEntry(entry);
							array = IOUtils.toByteArray(new FileInputStream(f));
							zip.write(array, 0, (int)f.length());
							zip.closeArchiveEntry();
						}
						zip.finish();
						zip.close();
						success = true;
						message = getString(R.string.zip_successfully_created);
					} catch (IOException e) {
						e.printStackTrace();
						message = e.getLocalizedMessage();
					}
				}
			}
			
			parent.runOnUiThread(new MessageThread(message, parent));
			
			if (success && delete){
				deleteFile(dir);
			}
			
			parent.showPrevious();
			parent.runOnUiThread(new RefreshThread());
			
		}
		
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
	
	/**
	 * @param resourceID ID of a String resource to get the text for
	 * */
	public void process(int resourceID){
		processItem(false, parent.getString(resourceID));
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
			
			if (name.equals(parent.getString(R.string.up_directory))){
				if (!parent.getHome().equals("/")){
					location = parent.getHomeAsFile().getParent();
				}else {
					location = "/";
				}
			}else if (name.equals(parent.getString(R.string.cancel_selection))){
				parent.finish();
				return;
			}else if (name.equals(parent.getString(R.string.home_directory))){
				location = Environment.getExternalStorageDirectory().getAbsolutePath();
			}else if (name.equals(parent.getString(R.string.recent_general_settings))){
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
		
		File file = new File(location);
		
		if (!file.exists()){
    		parent.runOnUiThread(new ToastThread(R.string.cant_go_up, parent, Toast.LENGTH_SHORT));
			return;
		}else if (file.isFile() || forceReturn){
			for (Recent recent : MainActivity.mainActivity.getSettings().getRecent()){
				if (recent.getPath().equals(location)){
					parent.returnValue(file, recent.getPageNumber());
					return;
				}
			}
			parent.returnValue(file, 0);
		}else {
			parent.reset(file);
		}
		
	}
	
	public boolean isThreadRunning(){
		return 	runningThread != null && runningThread.isAlive();
	}
	
}
