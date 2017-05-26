package com.koroshiya.archive.steppable;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;

import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JZipArchive extends SteppableArchive{
	
	private final ZipFile zip;

	public JZipArchive(String path, Context c) throws IOException {
		
		super(c);
		
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
			throw new IOException("No supported images in "+new File(path).getName());
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
    @Nullable
	public SoftReference<JBitmapDrawable> parseImage(int i, int width, boolean resize, boolean allowTrim){

        JImage j = this.cache.get(i);
        ZipEntry zipEntry = (ZipEntry)j.getImage();
        String name = j.getName();
		File f = new File(tempDir, name);
		
		if (!this.tempDir.exists()){
            createTempDir();
		}

        JBitmapDrawable temp = null;

        try {

            InputStream is = null;
            try {
                if (this.progressive) {
                    if (f.exists() || ArchiveParser.writeStreamToDisk(this.tempDir, zip.getInputStream(zipEntry), name)) {
                        Point p;
                        try {
                            p = ImageParser.getImageSize(f);
                        }catch(IOException ioe){
                            is = new FileInputStream(f);
                            p = ImageParser.getImageSize(is);
                        }
                        is = new FileInputStream(f);
                        temp = ImageParser.parseImageFromDisk(is, p, width, resize, allowTrim);
                    }
                } else {
                        is = zip.getInputStream(zipEntry);
                        Point p = ImageParser.getImageSize(is);
                        is = zip.getInputStream(zipEntry);
                        temp = ImageParser.parseImageFromDisk(is, p, width, resize, allowTrim);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    is.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	return new SoftReference<>(temp);
    	
    }

    @Override
    public void close(){}
	
}
