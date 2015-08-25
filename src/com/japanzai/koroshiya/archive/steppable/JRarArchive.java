package com.japanzai.koroshiya.archive.steppable;

import android.graphics.Point;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.cache.StepThread;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * Purpose: Represents a rar archive.
 * 			Provides methods for manipulating such an archive.
 * */
public class JRarArchive extends SteppableArchive{

	private final Archive rar;
	
	public JRarArchive(String path, Reader parent) throws IOException {
		
		super(parent, path);
		
		setPrimary(new StepThread(this, true, true, width, extractMode));
		//setSecondary(new ArchiveThread(this, true, false));

		FileHeader header;
		rar = new Archive(new File(path));
		List<FileHeader> heads = rar.getFileHeaders();
		
		for (int i = 0; i < heads.size(); i++){
			
			header = heads.get(i);
			if (ImageParser.isSupportedImage(header.getFileNameString())){ //TODO: implement CRC check, like with zip file
				if (header.getFullUnpackSize() > 0) addImageToCache(i, header.getFileNameString());
			}
			
		}

        super.sort();
		
		if (getMax() > 0){
			setIndex(0, parent);
		}else {
			throw new IOException();
		}
		
	}
	
	@Override
	public SoftReference<JBitmapDrawable> parseImage(int i, int width, int resize) {

		FileHeader entry = (FileHeader)getEntry(i);
		String name = entry.getFileNameString();
		name = name.substring(name.lastIndexOf('\\') + 1);
		File f = new File(tempDir + "/" + name);
		InputStream is = null;
		Point p;
		JBitmapDrawable temp = null;
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}

        try {

            if (this.progressive) {
                if (!f.exists()) {
                    extractFileToDisk(i, tempDir);
                }

                p = ImageParser.getImageSize(f);
                is = new FileInputStream(f);
                temp = ImageParser.parseImageFromDisk(is, p.x, p.y, width, resize);
            } else {

                try {

                    is = rar.getInputStream(entry);
                    p = ImageParser.getImageSize(is);

                    is = rar.getInputStream(entry);
                    temp = ImageParser.parseImageFromDisk(is, p.x, p.y, width, resize);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (is != null) {
                is.close();
            }

        }catch (IOException ioe){
            return null;
        }

		return new SoftReference<>(temp);
    	
    }

	private void extractFileToDisk(int i, File pathToExtractTo){

		FileHeader header = (FileHeader) getEntry(i);
		String name = header.getFileNameString();
		name = name.substring(name.lastIndexOf('\\') + 1);

		if (ImageParser.isSupportedImage(name)){
			try {
				ArchiveParser.writeStreamToDisk(pathToExtractTo, rar.getInputStream(header), name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println("unsupported");
		}

	}

	@Override
	public ArrayList<String> peekAtContents() {
		
		ArrayList<String> names = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			names.add(((FileHeader)getEntry(i)).getFileNameString());
		}
		
		return names;
		
	}

	public Archive getArchive() {
		return this.rar;
	}

	public Object getEntry(int i){
		if (i < getImages().size()){
			return rar.getFileHeaders().get((Integer)getImages().get(i).getImage());
		}
		return null;
	}
	
	public void close(){
		try {
			rar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
