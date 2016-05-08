package com.koroshiya;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.koroshiya.archive.steppable.SteppableArchive;
import com.koroshiya.async.DecodeArchiveStreamAsync;
import com.koroshiya.async.DecodeAsync;
import com.koroshiya.async.DecodeBitmapFileAsync;
import com.koroshiya.async.GenerateThumbnailAsync;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.fragments.ReadFragment;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReadCache {

    private final File f;
    private int currentPage;
    private int totalPages = 0;
    private final ReadFragment fragment;
    private final SteppableArchive archive;
    private SoftReference<JBitmapDrawable> cacheForward = new SoftReference<>(null);
    private SoftReference<JBitmapDrawable> cacheBackward = new SoftReference<>(null);
    private Recent recent;
    private boolean parsingInitial = true;

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

        //TODO: check if in recent list? Or just leave that in ReadFragment

        if (ArchiveParser.isSupportedArchive(f)){
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
                fragment.hideProgress();

                if (parsingInitial){
                    cacheNext(fragment.getActivity());
                    parsingInitial = false;
                }

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


    public void next(View v, Context c){

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
            File parent = f.getParentFile();
            if (parent.canRead()){
                String[] files = parent.list(ImageParser.fullFnf);
                List<String> fileNames = Arrays.asList(files);
                Collections.sort(fileNames);

                int foundAtIndex = -1, totalIndex = 0;
                for (String filename : fileNames) {
                    Log.i("ReadCache", "Comparing "+filename+" to "+f.getName());
                    if (filename.equals(f.getName())){
                        foundAtIndex = totalIndex;
                    }
                    totalIndex++;
                }
                if (foundAtIndex == -1 || foundAtIndex == totalIndex - 1){
                    Snackbar.make(v, "End of chapter - No more chapters found", Snackbar.LENGTH_SHORT).show();
                }else{
                    String s = fileNames.get(foundAtIndex + 1);
                    Snackbar.make(v, "Opening next chapter - "+s, Snackbar.LENGTH_SHORT).show();
                    File file = new File(parent, s);
                    fragment.reset(file.getAbsolutePath(), 0);
                }
            }
        }

    }

    public void previous(View v, Context c){

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
            File parent = f.getParentFile();
            if (parent.canRead()){
                String[] files = parent.list(ImageParser.fullFnf);
                List<String> fileNames = Arrays.asList(files);
                Collections.sort(fileNames);

                int foundAtIndex = 0, totalIndex = 0;
                for (String filename : fileNames) {
                    Log.i("ReadCache", "Comparing "+filename+" to "+f.getName());
                    if (filename.equals(f.getName())){
                        foundAtIndex = totalIndex;
                    }
                    totalIndex++;
                }
                if (foundAtIndex == 0){
                    Snackbar.make(v, "Start of chapter - No previous chapters found", Snackbar.LENGTH_SHORT).show();
                }else{
                    String s = fileNames.get(foundAtIndex - 1);
                    Snackbar.make(v, "Opening previous chapter - "+s, Snackbar.LENGTH_SHORT).show();
                    File file = new File(parent, s);
                    fragment.reset(file.getAbsolutePath(), 0);
                }
            }
        }

    }

    private void cacheNext(Context c){

        cacheForward = new SoftReference<>(null);

        if (getTotalPages() > currentPage + 1 && SettingsManager.isCacheOnStart(c)){
            if (ArchiveParser.isSupportedRarArchive(f)){
                if (!SettingsManager.isCacheForRar(c)){
                    return; //TODO: implement rar check elsewhere? Background cache async?
                }
            }
            parseImage(c, CACHE_FORWARD, currentPage + 1);
        }
    }


}
