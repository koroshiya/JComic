package com.koroshiya.async;

import android.content.Context;

import com.koroshiya.ReadCache;
import com.koroshiya.archive.steppable.JRarArchive;
import com.koroshiya.archive.steppable.SteppableArchive;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.settings.SettingsManager;

import java.lang.ref.SoftReference;

public class DecodeArchiveStreamAsync extends DecodeAsync {

    private final SteppableArchive archive;

    public DecodeArchiveStreamAsync(Context c, ReadCache cache, int cacheType, SteppableArchive archive){
        super(c, cache, cacheType);
        this.archive = archive;
    }

    @Override
    protected SoftReference<JBitmapDrawable> doInBackground(String... params) {
        readData(params);
        if (cacheType == ReadCache.CACHE_DIRECT || !(archive instanceof JRarArchive) || SettingsManager.isCacheForRar(c))
            return archive.parseImage(page, p.x, SettingsManager.getDynamicResizing(c));
        else
            return new SoftReference<>(null);
    }
}
