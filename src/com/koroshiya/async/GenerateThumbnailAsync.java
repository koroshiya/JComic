package com.koroshiya.async;

import android.content.Context;
import android.os.AsyncTask;

import com.koroshiya.archive.steppable.SteppableArchive;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class GenerateThumbnailAsync extends AsyncTask<String, String, Boolean> {

    private final Context c;

    public GenerateThumbnailAsync(Context c){
        this.c = c;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String src = params[0];
        File cacheDir = new File(params[1]);
        long uuid = Long.parseLong(params[2]);
        int imgWidth = Integer.parseInt(params[3]);
        int imgHeight = Integer.parseInt(params[4]);

        File srcFile = new File(src);
        File target = new File(cacheDir, uuid + ".webp");

        if (ImageParser.isSupportedFile(srcFile)){
            try {
                InputStream is = null;
                if (ImageParser.isSupportedDirectory(srcFile)) {
                    File[] files = srcFile.listFiles(ImageParser.fnf);
                    File f = files[0];
                    is = new FileInputStream(f);
                } else if (ArchiveParser.isSupportedArchive(srcFile)) {
                    SteppableArchive sa = ArchiveParser.parseArchive(srcFile, c);
                    if (sa != null) is = sa.getStream(0);
                } else {
                    return false;
                }

                if (is == null){
                    return false;
                }

                ImageParser.createThumbnail(is, target, imgWidth, imgHeight, 128);
                is.close();
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return false;

    }
}
