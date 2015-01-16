package com.japanzai.koroshiya.archive.non_steppable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import android.graphics.Point;
import android.widget.TextView;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.filechooser.FileChooser;
import com.japanzai.koroshiya.interfaces.archive.ReadableArchive;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;

/**
 * Provides methods pertaining to .tar and .ar archives.
 * */
public class ArWrapper implements ReadableArchive{
	
	private final File archive;
	protected SetTextThread thread = null;
	private Reader parent;
	
	public ArWrapper(File archive, Reader parent){
		this.archive = archive;
		this.parent = parent;
	}
	
	@Override
	public boolean extractContentsToDisk(File pathToExtractTo, FileChooser fc) {
		
		ArchiveInputStream zip = null;
		ArchiveEntry entry;
		
		try {			
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archive));
			if (archive.getName().endsWith(".ar")){
				zip = new ArchiveStreamFactory().createArchiveInputStream("ar", bis);
			}else{
				zip = new ArchiveStreamFactory().createArchiveInputStream("tar", bis);
			}
			
			String name;
			while ((entry = zip.getNextEntry()) != null){
				name = entry.getName();
				if (name.contains("/")){
					name = name.substring(name.lastIndexOf('/') + 1);
				}else if (name.contains("\\")){
					name = name.substring(name.lastIndexOf('\\') + 1);
				}
				System.out.println("Tar extracting: " + name);
				if (fc != null && (thread == null || !thread.isAlive())){
					thread = new SetTextThread(fc.getText(R.string.zip_extract_in_progress) + ": " + name, fc);
					fc.runOnUiThread(thread);
				}
				
				if (ImageParser.isSupportedImage(entry.getName())){
					byte[] content = new byte[(int) entry.getSize()];
					
					do {zip.read(content, 0, content.length);}
					while (zip.getBytesRead() == entry.getSize());
						
					ArchiveParser.writeStreamToDisk(pathToExtractTo, new ByteArrayInputStream(content), name);
				}
			}
				
		}catch (IOException ex){
			ex.printStackTrace();
			return false;
		} catch (ArchiveException e) {
			e.printStackTrace();
			System.out.println("ArArchiver exception");
			return false;
		}catch (Exception ex){
			System.out.println(ex.toString());
			return false;
		}finally{
			try {
				if (zip != null) zip.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		return true;
		
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
	
	@Override
	public ArrayList<JBitmapDrawable> extractContentsToArrayList() {
		
		ArrayList<JBitmapDrawable> bitmaps = new ArrayList<>();
		ArchiveInputStream zip;
		ArchiveEntry entry;
		
		try {			
			
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archive));
			zip = new ArchiveStreamFactory().createArchiveInputStream(bis);
				
			try {
				ByteArrayInputStream s;
				Point size;
				JBitmapDrawable bp;
				while ((entry = zip.getNextEntry()) != null){
					if (ImageParser.isSupportedImage(entry.getName())){
						s = new ByteArrayInputStream(ArchiveParser.parseEntry(zip, entry));
						size = ImageParser.getImageSize(s);
						s = new ByteArrayInputStream(ArchiveParser.parseEntry(zip, entry));
						bp = ImageParser.parseImageFromDisk(s, size.x, size.y, parent);
						bitmaps.add(bp);
					}
				}
			}catch (Exception ex){
				System.out.println(ex.toString());
				System.out.println("ArWrapper exception");
			}finally {
			    zip.close();
			}
				
		}catch(Exception e) {
			e.printStackTrace();
		}
			
		return bitmaps;
		
	}

	@Override
	public ArrayList<String> peekAtContents() {
		
		ArchiveInputStream zip;
		ArchiveEntry entry;
		ArrayList<String> names = new ArrayList<>();
		
		try{
		
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archive));
            zip = new ArchiveStreamFactory().createArchiveInputStream(bis);
			
			while ((entry = zip.getNextEntry()) != null){
				names.add(entry.getName());
			}
		
		} catch (IOException | ArchiveException e) {
			e.printStackTrace();
		}
		
		return names;
		
	}

}
