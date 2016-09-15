package com.koroshiya.adapters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.koroshiya.fragments.ReadFragment;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.io_utils.StorageHelper;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    protected final boolean isRecent;
    final ArrayList<Recent> items = new ArrayList<>();
    protected File curdir;
    protected final Handler.Callback permCallback;

    public FileAdapter(Context c, Handler.Callback permCallback, boolean isRecent) {
        this.permCallback = permCallback;
        this.curdir = SettingsManager.getLastDirectory(c);
        this.isRecent = isRecent;
        setData(c);
    }

    public File getCurdir(){
        return this.curdir;
    }

    public void removeItem(int pos){
        if (pos < items.size()){
            items.remove(pos);
        }
        notifyItemRemoved(pos);
    }

    public void removeAllItems(){
        items.clear();
        notifyDataSetChanged();
    }

    //public static final String ARG_SCROLL = "scroll";

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDataOnView(position);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                    StorageHelper.isExternalStorageReadable() &&
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

            Message m = new Message();
            Bundle b = new Bundle();
            b.putString(FilePathAdapter.ARG_FILE_PATH_CHUNK, f.getAbsolutePath());
            m.setData(b);
            permCallback.handleMessage(m);

        }else{
            int i = 0;
            Message m = new Message();
            Bundle b = new Bundle();
            if (f.isDirectory() || ArchiveParser.isSupportedArchive(f)) {
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
            b.putString(ReadFragment.ARG_FILE, f.getAbsolutePath());
            b.putInt(ReadFragment.ARG_PAGE, i);
            m.setData(b);
            permCallback.handleMessage(m);
        }
    }

    public boolean setPath(View v, String path){
        if (path != null){
            File dir = new File(path);
            if (dir.canRead()) {
                this.curdir = dir;
                this.setData(v.getContext());
                return true;
            }else{
                Snackbar.make(v, "Cannot read directory - Permission denied", Snackbar.LENGTH_SHORT).show();
            }
        }else{
            Snackbar.make(v, "Invalid path", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View v) {
            super(v);
        }

        public abstract void setDataOnView(final int position);
    }

}
