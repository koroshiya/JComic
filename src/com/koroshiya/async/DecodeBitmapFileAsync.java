package com.koroshiya.async;

import android.content.Context;
import android.graphics.Point;

import com.koroshiya.ReadCache;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
                Point pt;
                FileInputStream is;
                try {
                    pt = ImageParser.getImageSize(f);
                } catch (IOException e) {
                    is = new FileInputStream(f);
                    pt = ImageParser.getImageSize(is);
                }

                is = new FileInputStream(f);
                temp = ImageParser.parseImageFromDisk(is, pt, p.x, resize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new SoftReference<>(temp);

    }

}
