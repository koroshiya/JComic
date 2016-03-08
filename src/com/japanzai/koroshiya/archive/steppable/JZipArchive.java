package com.japanzai.koroshiya.archive.steppable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.graphics.Point;
import android.util.Log;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

public class JZipArchive extends SteppableArchive{
	
	private final ZipFile zip;

	public JZipArchive(String path, File cacheDir, SettingsManager prefs) throws IOException {
		
		super(cacheDir, prefs);
		
		zip = new ZipFile(path);

        Enumeration<? extends ZipEntry> e = zip.entries();

        while (e.hasMoreElements()) {

            ZipEntry zipEntry = e.nextElement();
            String name = zipEntry.getName();

            if (ImageParser.isSupportedImage(name) && zipEntry.getCompressedSize() > 0 && isValidEntry(zip, zipEntry)) {
                addImageToCache(zipEntry, name);
            }

        }
		
		if (getTotalPages() > 0){
            super.sort();
		}else {
			throw new IOException();
		}
		
	}

    private boolean isValidEntry(ZipFile zip, ZipEntry entry){
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

            return crc32.getValue() == entry.getCrc();
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
    public InputStream getStream(int i) throws IOException {
        JImage j = this.cache.get(i);
        ZipEntry zipEntry = (ZipEntry)j.getImage();
        return zip.getInputStream(zipEntry);
    }

	@Override
	public SoftReference<JBitmapDrawable> parseImage(int i, int width, int resizeMode){

        JImage j = this.cache.get(i);
        ZipEntry zipEntry = (ZipEntry)j.getImage();
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
        Log.d("JZipArchive", "Scanning zip archive");
		
		for (int i = 0; i < getTotalPages(); i++){
			String name = this.cache.get(i).getName();
			names.add(name);
            Log.d("JZipArchive", "Zip entry " + i + ": " + name);
        }
		
		return names;
		
	}

    @Override
    public void close(){}
	
}
