package com.koroshiya.adapters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import java.util.Collections;

public abstract class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    final ArrayList<String> items = new ArrayList<>();
    File curdir;
    final Handler.Callback permCallback;

    FileAdapter(Context c, Handler.Callback permCallback) {
        this.permCallback = permCallback;
        this.curdir = SettingsManager.getLastDirectory(c);
        setData(c);
    }

    public File getCurdir(){
        return this.curdir;
    }

    public void removeItem(String path){
        int pos = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(path)){
                pos = i;
            }
        }
        if (pos >= 0){
            items.remove(pos);
            notifyItemRemoved(pos);
        }
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

    protected abstract void setData(Context c);

    String getItem(int position) {
        return items.get(position);
    }

    void setFile(final File f, final View v, boolean forceReturn){

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
                i = Recent.getPageNumber(c, f.getAbsolutePath(), 0);
            }else{
                Log.d("ItemClickListener", "Processing item after " + f.getName());
                ArrayList<File> tempList = new ArrayList<>();
                for (File s : f.getParentFile().listFiles()){
                    if ((s.isDirectory() || ImageParser.isSupportedFile(s)) && !s.isHidden()){tempList.add(s);}
                }
                Collections.sort(tempList);
                Log.d("ItemClickListener", "List of files: ");
                for (File file : tempList) {
                    Log.d("ItemClickListener", "Comparing "+file.getAbsolutePath()+" to "+f.getAbsolutePath());
                    if (file.getAbsolutePath().equals(f.getAbsolutePath())) {
                        break;
                    }
                    if (file.isFile()) {
                        //Don't increment for directories.
                        //The cache won't track them, so if we increment here, we'll get an incorrect index.
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

    /**
     * @return If path changed, returns path. Else, null.
     * */
    @Nullable
    public File goToNearestValidPath(){
        if (!curdir.exists() || !curdir.canRead()){
            File parent = curdir.getParentFile();
            if (!curdir.getAbsolutePath().equals(parent.getAbsolutePath())){
                curdir = curdir.getParentFile();
                goToNearestValidPath();
            }
            return curdir;
        }
        return null;
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder{

        ViewHolder(View v) {
            super(v);
        }

        public abstract void setDataOnView(final int position);
    }

}
