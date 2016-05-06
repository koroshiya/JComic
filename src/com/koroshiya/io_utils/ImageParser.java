package com.koroshiya.io_utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.koroshiya.controls.JBitmapDrawable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

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
    public static final FilenameFilter fullFnf = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            File f = new File(dir, filename);
            return isSupportedDirectory(f) ||
                    (f.isFile() && f.canRead() && f.length() > 0 && (isSupportedImage(f) || ArchiveParser.isSupportedArchive(f)));
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
                (f.length() > 0 && (ArchiveParser.isSupportedArchive(f) || ImageParser.isSupportedImage(f)))
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
	 * @param imageSize Height/Width of image
	 * @return Return a JBitmapDrawable object to be displayed.
	 * */
    public static JBitmapDrawable parseImageFromDisk(InputStream is, Point imageSize, int screenWidth, boolean resize){

        try{

            Log.e("ImageParser", "InWidth: "+imageSize.x);
            Log.e("ImageParser", "InHeight: "+imageSize.y);
            Log.e("ImageParser", "Width: "+screenWidth);
            BitmapFactory.Options opts = getResizeOpts(imageSize, screenWidth, resize);
            JBitmapDrawable b = new JBitmapDrawable(BitmapFactory.decodeStream(new BufferedInputStream(is), null, opts));
            if (imageSize.x == 0 || imageSize.y == 0){
                imageSize.x = b.getWidth();
                imageSize.y = b.getHeight();
            }
            b.setDimensions(imageSize.x, imageSize.y);

            return b;

        }catch(OutOfMemoryError e){
            System.out.println("ImageParser OOM exception");
            System.gc();
            return null;
        }catch (Exception ex){
            ex.printStackTrace();
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
            int scaledNum = (int)Math.floor(1.0 / scale);
            opts.inSampleSize = (int)Math.pow(2, Math.ceil(Math.log(scaledNum)/Math.log(2)));
            Log.i("IP", "Target width: "+targetWidth);
            Log.i("IP", "Target height: "+targetHeight);
            Log.w("IP", "Scale: "+scale);
            Log.i("IP", "Samplesize: "+opts.inSampleSize);
            //opts.outHeight = targetHeight;
            //opts.outWidth = targetWidth;

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

    public static Point getImageSize(File f) throws IOException {

        if (f != null && f.getName().toLowerCase().endsWith(".png")){
            return getPNGSize(f);
        }else {
            throw new IOException("Not a PNG file");
        }

    }

    /**
     * Parses an image from the InputStream passed in, but doesn't extract it.
     * Instead, its height and width are returned.
     * @param is InputStream to parse a file from.
     * 			Could be InputFileStream, ByteArrayInputStream, etc.
     * @return Point containing the width and height of the image parsed
     * */
    public static Point getImageSize(InputStream is) {

        BitmapFactory.Options opts = getSampleOpts();
        BufferedInputStream bis = new BufferedInputStream(is);
        BitmapFactory.decodeStream(bis, null, opts);
        return new Point(opts.outWidth, opts.outHeight);

    }

    private static Point getPNGSize(File f) throws IOException {

        FileInputStream is = new FileInputStream(f);

        int width, height;
        byte[] b137 = new byte[0];
        is.read(b137, 0, 24);

        if ((b137[0] & 0xFF) == 137 && (b137[1] & 0xFF) == 80 && (b137[2] & 0xFF) == 78){
            height = Byte2PosInt(b137[22],b137[23]);
            width = Byte2PosInt(b137[18],b137[19]);
        }else{
            throw new IOException("Invalid PNG file");
        }

        return new Point(width, height);

    }

    private static Integer Byte2PosInt(byte Byte08, byte Byte00) {
        return ((Byte08 & 0xFF) * 256) | ((Byte00 & 0xFF));
    }

    private static BitmapFactory.Options getSampleOpts(){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
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

    private static BitmapFactory.Options getResizeOpts(Point p, int screenWidth, boolean resize){

        BitmapFactory.Options opts = getRealOpts();

        if (resize){
            if (screenWidth == 0) screenWidth = p.x;
            float scale = (float) p.x / (float) screenWidth;
            if (scale > 1) {
                opts.inSampleSize = (int)Math.pow(2, Math.ceil(Math.log(scale)/Math.log(2)));
                Log.e("ImageParser", "Subsample: " + opts.inSampleSize);
            }
        }

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
