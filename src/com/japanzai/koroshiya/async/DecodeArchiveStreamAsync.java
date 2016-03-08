package com.japanzai.koroshiya.async;

import android.content.Context;

import com.japanzai.koroshiya.ReadCache;
import com.japanzai.koroshiya.archive.steppable.JRarArchive;
import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.controls.JBitmapDrawable;

import java.lang.ref.SoftReference;

public class DecodeArchiveStreamAsync extends DecodeAsync {

    private SteppableArchive archive;

    public DecodeArchiveStreamAsync(Context c, ReadCache cache, int cacheType, SteppableArchive archive){
        super(c, cache, cacheType);
        this.archive = archive;
    }

    @Override
    protected SoftReference<JBitmapDrawable> doInBackground(String... params) {
        readData(params);
        if (cacheType == ReadCache.CACHE_DIRECT || !(archive instanceof JRarArchive) || prefs.isCacheForRar())
            return archive.parseImage(page, p.x, prefs.getDynamicResizing());
        else
            return new SoftReference<>(null);
    }
}
