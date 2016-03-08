package com.japanzai.koroshiya.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.ReadCache;
import com.japanzai.koroshiya.controls.JBitmapDrawable;
import com.japanzai.koroshiya.io_utils.ImageParser;

import java.io.File;
import java.lang.ref.SoftReference;

public abstract class DecodeAsync extends AsyncTask<String, String, SoftReference<JBitmapDrawable>> {

    protected final ReadCache readCache;
    protected final ProgressDialog dialog;
    protected final int cacheType;
    protected int page;
    protected File f;
    protected final Point p;
    protected final Context c;

    public DecodeAsync(Context c, ReadCache readCache, int cacheType){
        this.readCache = readCache;
        this.dialog = new ProgressDialog(c);
        this.dialog.setIcon(R.drawable.ic_launcher);
        this.cacheType = cacheType;
        this.c = c;

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
