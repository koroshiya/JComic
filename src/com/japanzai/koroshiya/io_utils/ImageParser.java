package com.japanzai.koroshiya.io_utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.japanzai.koroshiya.controls.JBitmapDrawable;

/**
 * Purpose: Used to check if a file is a supported image and/or parse it
 * */
public class ImageParser {
	
	private ImageParser(){
		//We don't want this file to be instantiated
	}
	
	private static final String[] supportedImages = {".png", ".jpg", ".jpeg", ".gif", ".webp"};
    public static final FilenameFilter fnf = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            File f = new File(dir, filename);
            return f.isFile() && f.canRead() && f.length() > 0 && isSupportedImage(f);
        }
    };
	   
    /**
     * Purpose: Checks to see if image is of a supported format
     * @param f File to parse
     * @return If the file is supported, returns true. Otherwise, false.
     * */
	public static boolean isSupportedImage(File f){
    	
    	return isSupportedImage(f.getAbsolutePath());
    	
    }

    /**
     * @param f File to check if supported
     * @return Returns true if the file is supported, otherwise false
     * */
    public static boolean isSupportedFile(File f){

        return f != null && f.exists() && (
                (f.isDirectory() && f.list(fnf).length > 0) ||
                (f.length() > 0 && (ArchiveParser.isSupportedArchive(f.getAbsolutePath()) || ImageParser.isSupportedImage(f)))
        );

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
		}
		
    	for (String ext : supportedImages) if (comp.endsWith(ext)) return true;
    	
    	return false;
    	
    }

    /**
     * Checks if the file is a directory, and if it contains at least one supported image.
     *
     * @param f Directory to process
     *
     * @return True if the directory is suitable for parsing
     **/
    public static boolean isSupportedDirectory(File f) {
        return f.isDirectory() && f.listFiles(fnf).length > 0;
    }
	
	/**
	 * Purpose: Parse a displayable object from a stream.
	 * @param is InputStream to parse a file from.
	 * 			Could be InputFileStream, ByteArrayInputStream, etc.
	 * @param inWidth Width at which to extract the image
	 * @param inHeight Height at which to extract the image
	 * @return Return a JBitmapDrawable object to be displayed.
	 * */
    public static JBitmapDrawable parseImageFromDisk(InputStream is, int inWidth, int inHeight, int width, int resize){

        BitmapFactory.Options opts = getRealOpts();

        try{

            Log.e("ImageParser", "InWidth: "+inWidth);
            Log.e("ImageParser", "InHeight: "+inHeight);
            Log.e("ImageParser", "Width: "+width);

            if (resize != 0){
                if (width == 0) width = inWidth;
                width = inWidth / width;
                if (!(resize == 2 && width < 1)){
                    opts.inSampleSize = width;
                }
            }
            JBitmapDrawable b = new JBitmapDrawable(BitmapFactory.decodeStream(new BufferedInputStream(is), null, opts));
            if (inWidth == 0 || inHeight == 0){
                inWidth = b.getWidth();
                inHeight = b.getHeight();
            }
            b.setDimensions(inWidth, inHeight);

            return b;

        }catch(OutOfMemoryError e){
            System.out.println("ImageParser OOM exception");
            System.gc();
            return null;
        }catch (Exception ex){
            return null;
        }

    }

    public static void createThumbnail(InputStream is, File targetFile, int imgWidth, int imgHeight, int targetDimen){

        BitmapFactory.Options opts = getRealOpts();

        try{

            float scale;
            Log.i("IP", "Target targetDimen: "+targetDimen);
            Log.i("IP", "Target imgWidth: "+imgWidth);
            Log.i("IP", "Target imgHeight: "+imgHeight);
            if (imgWidth > imgHeight)
                scale = (float)targetDimen / (float)imgWidth;
            else
                scale = (float)targetDimen / (float)imgHeight;

            int targetWidth = (int)Math.floor(imgWidth * scale);
            int targetHeight = (int)Math.floor(imgHeight * scale);
            Log.i("IP", "Target width: "+targetWidth);
            Log.i("IP", "Target height: "+targetHeight);
            Log.i("IP", "Scale: "+scale);
            //opts.inSampleSize = (int)Math.floor(1.0 / scale);
            opts.outHeight = targetHeight;
            opts.outWidth = targetWidth;

            Bitmap b = BitmapFactory.decodeStream(new BufferedInputStream(is), null, opts);
            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            b.compress(Bitmap.CompressFormat.WEBP, 75, bos);
            bos.close();
            fos.close();

        }catch(OutOfMemoryError | Exception ex){
            ex.printStackTrace();
        }

    }

	public static JBitmapDrawable parseImageFromDisk(File image, int width, int resize){
		
		BitmapFactory.Options opts = getRealOpts();

		try{
			if (resize != 0){
				Point p = getImageSize(new FileInputStream(image));
                if (width == 0) width = p.x;
				width = p.x / width;
				if (!(resize == 2 && width < 1)){
					opts.inSampleSize = width;
				}
			}
            return new JBitmapDrawable(BitmapFactory.decodeFile(image.getAbsolutePath(), opts));
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

        BitmapFactory.Options opts = getSampleOpts();
        BufferedInputStream bis = new BufferedInputStream(is);
        BitmapFactory.decodeStream(bis, null, opts);
        return new Point(opts.outWidth * 4, opts.outHeight * 4);

    }

    /**
     * Parses an image from the InputStream passed in, but doesn't extract it.
     * Instead, its height and width are returned.
     * @return Point containing the width and height of the image parsed
     * */
    public static Point getImageSize(File f){

        BitmapFactory.Options opts = getSampleOpts();
        BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        return new Point(opts.outWidth * 4, opts.outHeight * 4);

    }

    private static BitmapFactory.Options getSampleOpts(){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferQualityOverSpeed = false;
        opts.inDither = false;
        opts.inSampleSize = 4;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            opts.inPurgeable = true;
            opts.inInputShareable = true;
        }
        opts.inTempStorage = new byte[32768]; //32kb
        return opts;
    }

    private static BitmapFactory.Options getRealOpts(){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDither = false;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            opts.inPurgeable = true;
            opts.inInputShareable = true;
        }
        opts.inTempStorage = new byte[32768]; //32kb
        return opts;
    }

    public static Point getScreenSize(Context c){
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p;
    }

}
