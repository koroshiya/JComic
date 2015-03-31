package com.japanzai.koroshiya.io_utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.reader.Reader;

/**
 * Purpose: Used to check if a file is a supported image and/or parse it
 * */
public class ImageParser {
	
	private ImageParser(){
		//We don't want this file to be instantiated
	}
	
	private static final String[] supportedImages = {".png", ".jpg", ".jpeg", ".gif"};
	   
    /**
     * Purpose: Checks to see if image is of a supported format
     * @param f File to parse
     * @return If the file is supported, returns true. Otherwise, false.
     * */
	public static boolean isSupportedImage(File f){
    	
    	return isSupportedImage(f.getName());
    	
    }
	
	/**
     * Purpose: Checks to see if image is of a supported format
     * @param s Name of file to parse
     * @return If the file is supported, returns true. Otherwise, false.
     * */
	public static boolean isSupportedImage(String s){
    	
		String comp = s.toLowerCase(Locale.getDefault());
		
		if ((new File(comp)).getName().charAt(0) == '.'){ //Prevents hidden files and, more importantly MACOSX folder contents, from being read
			return false;
		}if (android.os.Build.VERSION.SDK_INT >= 14){ //WebP is only natively available for android 4.0+
			if (comp.endsWith(".webp")){return true;}
		}
		
    	for (String ext : supportedImages){
    		if (comp.endsWith(ext)){return true;}
    	}
    	
    	return false;
    	
    }

    /**
     * Checks if the file is a directory, and if it contains at least one supported image.
     *
     * @param f Directory to process
     *
     * @return True if the directory is suitable for parsing
     **/
    public static boolean isSupportedDirectory(File f){
        if (f.isDirectory()){
            for (File img : f.listFiles()){
                if (img.length() > 0 && isSupportedImage(img)){
                    return true;
                }
            }
        }
        return false;
    }
	
	/**
	 * Purpose: Parse a displayable object from a stream.
	 * @param is InputStream to parse a file from.
	 * 			Could be InputFileStream, ByteArrayInputStream, etc.
	 * @param inWidth Width at which to extract the image
	 * @param inHeight Height at which to extract the image
	 * @return Return a JBitmapDrawable object to be displayed.
	 * */
    public static JBitmapDrawable parseImageFromDisk(InputStream is, int inWidth, int inHeight, Reader r){

        BufferedInputStream bis = new BufferedInputStream(is);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDither = false;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            opts.inPurgeable = true;
            opts.inInputShareable = true;
        }
        opts.inTempStorage = new byte[32 * 1024];
        JBitmapDrawable b;

        try{

            int resize = r.getSettings().getDynamicResizing();
            if (resize != 0){
                int width = inWidth / r.getWidth();
                if (!(resize == 2 && width < 1)){
                    opts.inSampleSize = width;
                }
            }
            b = new JBitmapDrawable(BitmapFactory.decodeStream(bis, null, opts));
            if (inWidth == 0 || inHeight == 0){
                inWidth = b.getWidth();
                inHeight = b.getHeight();
            }
            b.setDimensions(inWidth, inHeight);

            return (b);

        }catch(OutOfMemoryError e){
            System.out.println("ImageParser OOM exception");
            System.gc();
            return null;
        }catch (Exception ex){
            return null;
        }

    }

	public static JBitmapDrawable parseImageFromDisk(File image, Reader r){
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inDither = false;
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
			opts.inPurgeable = true;
			opts.inInputShareable = true;
		}
		opts.inTempStorage = new byte[32 * 1024];
		JBitmapDrawable b;
		
		try{
			int resize = r.getSettings().getDynamicResizing();
			if (resize != 0){
				Point p = getImageSize(new FileInputStream(image));
				int width = p.x / r.getWidth();
				if (!(resize == 2 && width < 1)){
					opts.inSampleSize = width;
				}
			}
			b = new JBitmapDrawable(BitmapFactory.decodeFile(image.getAbsolutePath(), opts));
			return (b);
		}catch(OutOfMemoryError e){
			System.out.println("ImageParser OOM exception");
			System.gc();
			return null;
		}catch (Exception ex){
			return null;
		}
		
	}

    /**
     * Parses an image from the InputStream passed in, but doesn't extract it.
     * Instead, its height and width are returned.
     * @param is InputStream to parse a file from.
     * 			Could be InputFileStream, ByteArrayInputStream, etc.
     * @return Point containing the width and height of the image parsed
     * */
    public static Point getImageSize(InputStream is){
        BufferedInputStream bis = new BufferedInputStream(is);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            opts.inPurgeable = true;
            opts.inInputShareable = true;
        }
        opts.inTempStorage = new byte[32 * 1024];

        BitmapFactory.decodeStream(bis, null, opts);
        return new Point(opts.outWidth, opts.outHeight);

    }

    /**
     * Parses an image from the InputStream passed in, but doesn't extract it.
     * Instead, its height and width are returned.
     * @return Point containing the width and height of the image parsed
     * */
    public static Point getImageSize(File f){

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            opts.inPurgeable = true;
            opts.inInputShareable = true;
        }
        opts.inTempStorage = new byte[32 * 1024];

        BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        return new Point(opts.outWidth, opts.outHeight);

    }
	
	public static byte[] compress(byte[] content){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(content);
            gzipOutputStream.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        System.out.printf("Compression ratio %f\n", (1.0f * content.length/byteArrayOutputStream.size()));
        return byteArrayOutputStream.toByteArray();
    }
	
}
