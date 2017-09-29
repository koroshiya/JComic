package com.koroshiya.async;

import android.content.Context;
import android.graphics.Point;
import android.view.View;

import com.koroshiya.ReadCache;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.io_utils.ImageParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;

public class DecodeBitmapFileAsync extends DecodeAsync {

    public DecodeBitmapFileAsync(Context c, ReadCache cache, int cacheType, View v){
        super(c, cache, cacheType, v);
    }

    @Override
    protected SoftReference<JBitmapDrawable> doInBackground(String... params) {

        JBitmapDrawable temp = null;
        readData(params);

        ArrayList<File> tempList = new ArrayList<>();
        for (File s : f.listFiles()){
            if (ImageParser.isSupportedFile(s) && !s.isHidden()){tempList.add(s);}
        }
        Collections.sort(tempList);

        if (tempList.size() > page || page < 0) {
            f = tempList.get(page);
            try {
                Point pt;
                FileInputStream is;
                try {
                    pt = ImageParser.getImageSize(f);
                } catch (IOException e) {
                    is = new FileInputStream(f);
                    pt = ImageParser.getImageSize(is);
                }

                is = new FileInputStream(f);
                temp = ImageParser.parseImageFromDisk(is, pt, p.x, resize, allowTrim);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new SoftReference<>(temp);

    }

}
