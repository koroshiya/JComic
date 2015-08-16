package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.CRC32;

import android.graphics.Point;
import android.util.Log;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.cache.StepThread;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.reader.ToastThread;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

/**
 * Purpose: Represents a standard Zip archive.
 * 			Also used for cbz archives.
 * 			Could be used for Jar archives, if implemented later.
 * */
public class JZipArchive extends SteppableArchive{
	
	private final ZipFile zip;

	public JZipArchive(String path, Reader parent) throws IOException {
		
		super(parent, path);
		
		zip = new ZipFile(path);

        if (!zip.isValidZipFile()) throw new IOException();

        FileHeader zipEntry;
        String name;

        for (Object o : zip.getFileHeaders()) {

            zipEntry = (FileHeader) o;
            name = zipEntry.getFileName();

            if (ImageParser.isSupportedImage(name) && zipEntry.getCompressedSize() > 0 && isValidEntry(zip, zipEntry)) {
                addImageToCache(zipEntry, zipEntry.getFileName());
            }

        }

        super.sort();
		
		if (getMax() > 0){
            setPrimary(new StepThread(this, true, true));
            setSecondary(new StepThread(this, true, false));

			setIndex(0);
			setMin();
		}else {
			throw new IOException();
		}
		
	}

    private boolean isValidEntry(ZipFile zip, FileHeader entry){
        InputStream inputStream = null;
        try {
            inputStream = zip.getInputStream(entry);

            byte[] buff = new byte[4096];
            int readLen;
            CRC32 crc32 = new CRC32();

            while ((readLen = inputStream.read(buff)) != -1) {
                crc32.update(buff, 0, readLen);
            }

            return crc32.getValue() == entry.getCrc32();
        } catch (Exception e) {
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    return false;
                }
            }
        }
    }
	
	@Override
	public JBitmapDrawable parseImage(int i){

        JImage j = getImages().get(i);
        FileHeader zipEntry = (FileHeader)j.getImage();
        String name = j.getName();
		File f = new File(tempDir + "/" + name);
		
		if (!this.tempDir.exists()){
			this.tempDir.mkdirs();
		}

        JBitmapDrawable temp = null;

        try {

            if (this.progressive) {
                if (f.exists() || ArchiveParser.writeStreamToDisk(this.tempDir, zip.getInputStream(zipEntry), name)) {
                    temp = ImageParser.parseImageFromDisk(f, parent);
                    if (temp == null) {
                        super.clear();
                        temp = ImageParser.parseImageFromDisk(f, parent);
                    }
                }
            } else {
                InputStream is = null;
                try {
                    is = zip.getInputStream(zipEntry);
                    Point p = ImageParser.getImageSize(is);
                    is = zip.getInputStream(zipEntry);

                    temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
                    if (temp == null) {
                        super.clear();
                        is = zip.getInputStream(zipEntry);
                        temp = ImageParser.parseImageFromDisk(is, p.x, p.y, parent);
                    }
                    is.close();
                    is = null;

                } catch (IOException e) {
                    e.printStackTrace();
                    new ToastThread(R.string.archive_read_error, parent).start();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }

        } catch (IOException e) {
            return null;
        }
    	
    	return temp;
    	
    }

	@Override
	public ArrayList<String> peekAtContents() {

		ArrayList<String> names = new ArrayList<>();
		
		for (int i = 0; i < getMax(); i++){
			String name = getImages().get(i).getName();
			names.add(name);
            Log.d("JZipArchive", "Zip entry " + i + ": " + name);
        }
		
		return names;
		
	}
	
	public void close(){}
	
}
