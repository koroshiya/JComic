package com.japanzai.koroshiya.async;

import android.content.Context;
import android.graphics.Point;

import com.japanzai.koroshiya.ReadCache;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

public class DecodeBitmapFileAsync extends DecodeAsync {

    public DecodeBitmapFileAsync(Context c, ReadCache cache, int cacheType){
        super(c, cache, cacheType);
    }

    @Override
    protected SoftReference<JBitmapDrawable> doInBackground(String... params) {

        JBitmapDrawable temp = null;
        readData(params);

        File[] files = f.listFiles(ImageParser.fnf);

        if (files.length > page || page < 0) {
            f = files[page];
            try {
                boolean resize = SettingsManager.getDynamicResizing(c);
                FileInputStream is = new FileInputStream(f);
                Point pt = ImageParser.getImageSize(is);

                is = new FileInputStream(f);
                temp = ImageParser.parseImageFromDisk(is, pt, p.x, resize);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return new SoftReference<>(temp);

    }

}
