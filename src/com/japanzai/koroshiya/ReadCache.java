package com.japanzai.koroshiya;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.japanzai.koroshiya.archive.steppable.SteppableArchive;
import com.japanzai.koroshiya.async.DecodeArchiveStreamAsync;
import com.japanzai.koroshiya.async.DecodeAsync;
import com.japanzai.koroshiya.async.DecodeBitmapFileAsync;
import com.japanzai.koroshiya.async.GenerateThumbnailAsync;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.fragments.ReadFragment;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.classes.Recent;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

public class ReadCache {

    private final File f;
    private int currentPage;
    private int totalPages = 0;
    private final ReadFragment fragment;
    private final SteppableArchive archive;
    private SoftReference<JBitmapDrawable> cacheForward = new SoftReference<>(null);
    private SoftReference<JBitmapDrawable> cacheBackward = new SoftReference<>(null);
    private Recent recent;

    public static final int CACHE_DIRECT = 0;
    public static final int CACHE_FORWARD = 1;
    public static final int CACHE_BACKWARD = 2;

    public ReadCache(File f, int page, ReadFragment fragment) throws IOException {

        Context c;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = fragment.getContext();
        }else{
            c = fragment.getActivity();
        }

        if (ArchiveParser.isSupportedArchive(f.getAbsolutePath())){
            archive = ArchiveParser.parseArchive(f, c);
            totalPages = archive.getTotalPages();
        }else{
            archive = null;
            if (f.isFile()) f = f.getParentFile();
            for (File file : f.listFiles()){
                if (ImageParser.isSupportedImage(file)){
                    totalPages++;
                }
            }
        }

        this.f = f;
        this.currentPage = page;
        this.fragment = fragment;

        Log.i("ReadCache", "Initiated with file: "+f.getAbsolutePath());
        Log.i("ReadCache", "Initiated with page: "+page);

    }

    public void parseInitial(Context c){
        parseImage(c, CACHE_DIRECT, currentPage);
        cacheNext(c);
    }

    private void parseImage(Context c, int cacheType, int page){
        Log.i("parseImage", "page: "+page);
        DecodeAsync async;
        if (archive != null){
            async = new DecodeArchiveStreamAsync(c, this, cacheType, archive);
        }else {
            async = new DecodeBitmapFileAsync(c, this, cacheType);
        }
        async.execute(f.getAbsolutePath(), Integer.toString(page));
    }

    public int getTotalPages(){
        return this.totalPages;
    }

    public void setImage(SoftReference<JBitmapDrawable> image, int cacheType) {
        switch (cacheType){
            case CACHE_DIRECT:
                fragment.setImage(image.get());
                setThumb(image);

                break;
            case CACHE_FORWARD:
                cacheForward = image;
                break;
            case CACHE_BACKWARD:
                cacheBackward = image;
                break;
        }
    }

    private void setThumb(SoftReference<JBitmapDrawable> image){

        if (image != null && image.get() != null) {

            Context c;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                c = fragment.getContext();
            } else {
                c = fragment.getActivity();
            }

            if (recent == null) {
                recent = SettingsManager.getRecentAndFavorite(c, f.getAbsolutePath(), true);
            }

            if (recent != null) {
                recent.setPageNumber(currentPage);
            } else {
                JBitmapDrawable jbd = image.get();
                recent = new Recent(f.getAbsolutePath(), currentPage);
                GenerateThumbnailAsync async = new GenerateThumbnailAsync(c);
                async.execute(
                        f.getAbsolutePath(),
                        c.getCacheDir().getAbsolutePath(),
                        Long.toString(recent.getUuid()),
                        Integer.toString(jbd.getWidth()),
                        Integer.toString(jbd.getHeight())
                );
            }

            SettingsManager.addRecentAndFavorite(c, recent); //TODO: move to detach instead?

        }

    }

    public void close(){
        if (archive != null){
            archive.close();
        }
        //TODO: save recent
    }


    public void next(Context c){

        if (currentPage < getTotalPages() - 1) {
            Log.i("ReadCache", "Trying to go to next page");

            currentPage++;

            cacheBackward = new SoftReference<>(this.fragment.getImage());
            if (cacheForward != null && cacheForward.get() != null) {
                setImage(cacheForward, CACHE_DIRECT);
                cacheNext(c);
            } else {
                parseInitial(c); //Parse current page and cache next page
            }

        }else{
            Log.i("ReadCache", "Trying to go to next chapter");
            //TODO: try to go to next chapter
        }

    }

    public void previous(Context c){

        if (currentPage > 0) {
            Log.i("ReadCache", "Trying to go to previous page");

            currentPage--;

            cacheForward = new SoftReference<>(this.fragment.getImage());
            if (cacheBackward != null && cacheBackward.get() != null) {
                Log.i("ReadCache", "bcache is filled");
                setImage(cacheBackward, CACHE_DIRECT);
            } else {
                Log.i("ReadCache", "bcache is null");
                parseImage(c, CACHE_DIRECT, currentPage); //Parse only current page
            }
            cacheBackward = new SoftReference<>(null);

        }else{
            Log.i("ReadCache", "Trying to go to previous chapter");
            //TODO: try to go to previous chapter
        }

    }

    private void cacheNext(Context c){

        cacheForward = new SoftReference<>(null);

        if (getTotalPages() > currentPage + 1 && SettingsManager.isCacheOnStart(c)){
            if (ArchiveParser.isSupportedRarArchive(f.getAbsolutePath())){
                if (!SettingsManager.isCacheForRar(c)){
                    return; //TODO: implement rar check elsewhere? Background cache async?
                }
            }
            parseImage(c, CACHE_FORWARD, currentPage + 1);
        }
    }


}
