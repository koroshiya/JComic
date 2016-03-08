package com.japanzai.koroshiya.filechooser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.io_utils.StorageHelper;
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    protected final boolean isRecent;

    public FileAdapter(Context c, Handler.Callback permCallback, boolean isRecent) {
        this.permCallback = permCallback;
        this.curdir = Environment.getExternalStorageDirectory(); // TODO: Query preferences
        this.isRecent = isRecent;
        setData(c);
    }

    final ArrayList<Recent> items = new ArrayList<>();
    protected File curdir;
    protected final Handler.Callback permCallback;

    //public static final String ARG_SCROLL = "scroll";

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDataOnView(holder, position);
    }

    public abstract void setData(Context c);

    public Recent getItem(int position) {
        return items.get(position);
    }

    protected void setFile(final File f, final View v, boolean forceReturn){

        Context c = v.getContext();

        if (!f.exists()){
            Snackbar.make(v, "File does not exist", Snackbar.LENGTH_SHORT).show();
            Log.e("FileItemAdapter", "Doesn't exist: "+f.getAbsolutePath());
        }else if (!f.canRead()){
            if (StorageHelper.isExternalStorageReadable() &&
                    ContextCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){

                Message m = new Message();
                Bundle b = new Bundle();
                b.putString("permission", Manifest.permission.READ_EXTERNAL_STORAGE);
                m.setData(b);
                permCallback.handleMessage(m);

            }else{
                Snackbar.make(v, "Cannot read file/folder - Permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }else if (f.isDirectory() && !forceReturn){
            this.curdir = f;
            this.setData(c);
        }else{
            int i = 0;
            Message m = new Message();
            Bundle b = new Bundle();
            if (f.isDirectory() || ArchiveParser.isSupportedArchive(f.getAbsolutePath())) {
                Recent recent = SettingsManager.getRecentAndFavorite(c, f.getAbsolutePath(), true);
                if (recent != null) i = recent.getPageNumber();
            }else{
                Log.d("ItemClickListener", "Processing item after " + f.getName());
                File[] files = f.getParentFile().listFiles();
                Arrays.sort(files);
                Log.d("ItemClickListener", "List of files: ");
                for (File file : files) {
                    if (ImageParser.isSupportedImage(file)) {
                        Log.d("ItemClickListener", file.getName());
                        if (file.getName().equals(f.getName())) {
                            break;
                        }
                        i++;
                    }
                }
            }
            b.putString("file", f.getAbsolutePath());
            b.putInt("page", i);
            m.setData(b);
            permCallback.handleMessage(m);
        }
    }

    public void up(View v){
        if (this.curdir.getParentFile() != null){
            this.curdir = this.curdir.getParentFile();
            this.setData(v.getContext());
        }else{
            Snackbar.make(v, "Cannot go up any further", Snackbar.LENGTH_SHORT).show();
        }
    }

    /*protected void scrollToTop(){
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SCROLL, "top");
        message.setData(bundle);
        permCallback.handleMessage(message);
    }*/


    public abstract class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View v) {
            super(v);
        }

        public abstract void setDataOnView(ViewHolder holder, final int position);
    }

}
