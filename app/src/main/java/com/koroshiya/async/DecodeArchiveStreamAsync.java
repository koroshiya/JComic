package com.koroshiya.async;

import android.content.Context;
import android.view.View;

import com.koroshiya.ReadCache;
import com.koroshiya.archive.steppable.JRarArchive;
import com.koroshiya.archive.steppable.SteppableArchive;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.settings.SettingsManager;

import java.lang.ref.SoftReference;

public class DecodeArchiveStreamAsync extends DecodeAsync {

    private final SteppableArchive archive;
    private final boolean isCacheRar;

    public DecodeArchiveStreamAsync(Context c, ReadCache cache, int cacheType, SteppableArchive archive, View v){
        super(c, cache, cacheType, v);
        this.archive = archive;
        this.isCacheRar = SettingsManager.isCacheForRar(c);
    }

    @Override
    protected SoftReference<JBitmapDrawable> doInBackground(String... params) {
        readData(params);
        if (cacheType == ReadCache.CACHE_DIRECT || !(archive instanceof JRarArchive) || isCacheRar)
            return archive.parseImage(page, p.x, resize);
        else
            return new SoftReference<>(null);
    }
}
