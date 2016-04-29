package com.koroshiya.archive.steppable;

import android.content.Context;
import android.graphics.Point;

import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

public class JRarArchive extends SteppableArchive{

	private final Archive rar;
	
	public JRarArchive(String path, Context c) throws IOException {
		
		super(c);

		FileHeader header;
		rar = new Archive(new File(path));
		List<FileHeader> heads = rar.getFileHeaders();
		
		for (int i = 0; i < heads.size(); i++){
			
			header = heads.get(i);
			if (ImageParser.isSupportedImage(header.getFileNameString())){ //TODO: implement CRC check, like with zip file
				if (header.getFullUnpackSize() > 0) addImageToCache(i, header.getFileNameString());
			}
			
		}

		if (getTotalPages() > 0){
            super.sort();
		}else {
			throw new IOException();
		}
		
	}

    @Override
    public InputStream getStream(int i) throws IOException {
        FileHeader entry = (FileHeader)getEntry(i);
        return rar.getInputStream(entry);
    }

    @Override
	public SoftReference<JBitmapDrawable> parseImage(int i, int width, boolean resize) {

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

            try {

                if (this.progressive) {
                    if (!f.exists()) {
                        extractFileToDisk(i, tempDir);
                    }

                    is = new FileInputStream(f);
                    p = ImageParser.getImageSize(is);

                    is = new FileInputStream(f);
                    temp = ImageParser.parseImageFromDisk(is, p, width, resize);
                } else {

                    is = rar.getInputStream(entry);
                    p = ImageParser.getImageSize(is);

                    is = rar.getInputStream(entry);
                    temp = ImageParser.parseImageFromDisk(is, p, width, resize);
                }

            } catch (IOException e) {
                e.printStackTrace();
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
		
		for (int i = 0; i < getTotalPages(); i++){
			names.add(((FileHeader)getEntry(i)).getFileNameString());
		}
		
		return names;
		
	}

	public Archive getArchive() {
		return this.rar;
	}

	public Object getEntry(int i){
		if (i < getTotalPages()){
			return rar.getFileHeaders().get((Integer)this.cache.get(i).getImage());
		}
		return null;
	}

    @Override
	public void close(){
		try {
			rar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
