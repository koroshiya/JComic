package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.zip.CRC32;

import android.graphics.Point;
import android.util.Log;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.cache.StepThread;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.reader.MainActivity;
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

	public JZipArchive(Reader reader, String path) throws IOException {
		
		super(reader, path);
		
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
            setPrimary(new StepThread(this, true, true, width, extractMode));
            setSecondary(new StepThread(this, true, false, width, extractMode));

			setIndex(0, reader);
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

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return crc32.getValue() == entry.getCrc32();
        } catch (Exception e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }
	
	@Override
	public SoftReference<JBitmapDrawable> parseImage(int i, int width, int resizeMode){

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
                    temp = ImageParser.parseImageFromDisk(f, width, resizeMode);
                }
            } else {
                InputStream is = null;
                try {
                    is = zip.getInputStream(zipEntry);
                    Point p = ImageParser.getImageSize(is);

                    is = zip.getInputStream(zipEntry);
                    temp = ImageParser.parseImageFromDisk(is, p.x, p.y, width, resizeMode);

                    is.close();
                    is = null;

                } catch (IOException e) {
                    e.printStackTrace();
                    new ToastThread(R.string.archive_read_error, MainActivity.getMainActivity()).start();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return new SoftReference<>(temp);
    	
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
