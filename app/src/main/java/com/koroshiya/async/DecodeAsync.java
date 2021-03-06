package com.koroshiya.async;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.koroshiya.R;
import com.koroshiya.ReadCache;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;

import java.io.File;
import java.lang.ref.SoftReference;

public abstract class DecodeAsync extends AsyncTask<String, String, SoftReference<JBitmapDrawable>> {

    private final ReadCache readCache;
    private final AlertDialog dialog;
    private final Snackbar snack;
    final int cacheType;
    int page;
    File f;
    final Point p;
    final boolean resize;
    final boolean allowTrim;

    DecodeAsync(Context c, ReadCache readCache, int cacheType, View v){
        this.readCache = readCache;
        this.dialog = new AlertDialog.Builder(c)
                                        .setIcon(R.mipmap.icon)
                                        .setMessage("Loading image...")
                                        .create();
        this.cacheType = cacheType;
        this.snack = v != null ? Snackbar.make(v, "Loading image...", Snackbar.LENGTH_INDEFINITE) : null;

        p = ImageParser.getScreenSize(c);
        resize = SettingsManager.getDynamicResizing(c);
        allowTrim = SettingsManager.isTrimmingAllowed(c);
    }

    void readData(String... params){
        f = new File(params[0]);
        page = Integer.parseInt(params[1]);
    }

    @Override
    protected void onPreExecute() {
        if (cacheType == ReadCache.CACHE_DIRECT) {
            if (this.snack != null){
                this.snack.show();
            }else {
                this.dialog.show();
            }
        }

    }

    @Override
    protected void onPostExecute (SoftReference<JBitmapDrawable> bitmap){

        String msg = null;

        if (cacheType == ReadCache.CACHE_DIRECT) {
            if (this.snack != null) {
                this.snack.dismiss();
                msg = "Opening "+f.getName();
            } else {
                dialog.dismiss();
            }
        }

        readCache.setImage(bitmap, cacheType, msg);
    }

}
