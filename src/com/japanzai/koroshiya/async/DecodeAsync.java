package com.japanzai.koroshiya.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.ReadCache;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;

import java.io.File;
import java.lang.ref.SoftReference;

public abstract class DecodeAsync extends AsyncTask<String, String, SoftReference<JBitmapDrawable>> {

    protected final ReadCache readCache;
    protected final ProgressDialog dialog;
    protected final File cacheDir;
    protected final SettingsManager prefs;
    protected final int cacheType;
    protected int page;
    protected File f;
    protected Point p;

    public DecodeAsync(Context c, ReadCache readCache, int cacheType){
        this.readCache = readCache;
        this.dialog = new ProgressDialog(c);
        this.dialog.setIcon(R.drawable.ic_launcher);
        this.cacheDir = c.getCacheDir();
        this.prefs = new SettingsManager(c, false);
        this.cacheType = cacheType;

        p = ImageParser.getScreenSize(c);
    }

    protected void readData(String... params){
        f = new File(params[0]);
        page = Integer.parseInt(params[1]);
    }

    @Override
    protected void onPreExecute() {
        if (cacheType == ReadCache.CACHE_DIRECT) {
            this.dialog.setMessage("Loading image...");
            this.dialog.show();
        }

    }

    @Override
    protected void onPostExecute (SoftReference<JBitmapDrawable> bitmap){

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        readCache.setImage(bitmap, cacheType);
    }

}
