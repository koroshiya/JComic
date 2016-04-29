package com.koroshiya.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.controls.EllipsizingTextView;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;

public class FileListAdapter extends FileAdapter {

    private final File cacheDir;

    public FileListAdapter(Context c, Handler.Callback permCallback, boolean isRecent) {
        super(c, permCallback, isRecent);
        this.cacheDir = c.getCacheDir();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    public void setData(Context c){

        items.clear();
        items.addAll(SettingsManager.getRecentAndFavorites(c, isRecent));

        super.notifyDataSetChanged();
    }

    public class ViewHolder extends FileAdapter.ViewHolder{

        final LinearLayoutCompat llc;
        final EllipsizingTextView tv;
        final AppCompatImageView iv;

        public ViewHolder(View v) {
            super(v);

            llc = (LinearLayoutCompat) v.findViewById(R.id.row_llc);
            tv = (EllipsizingTextView) llc.findViewById(R.id.row_text);
            iv = (AppCompatImageView) llc.findViewById(R.id.row_img);

        }

        @Override
        public void setDataOnView(int position) {

            final Recent p = getItem(position);
            String t = p.getPath();
            final File f = new File(t);

            File thumb = new File(cacheDir, p.getUuid() + ".webp");
            Log.i("FLA", "Creating image from: "+thumb.getAbsolutePath());

            if (thumb.exists() && thumb.isFile() && thumb.canRead()){
                Log.i("FLA", "Image size: "+thumb.length());
                Uri uri = Uri.fromFile(thumb);
                iv.setImageURI(uri);
            }else{
                int resId;
                if (f.isDirectory()){
                    resId = R.drawable.file_directory;
                }else if (ImageParser.isSupportedImage(f)){
                    resId = R.drawable.file_media;
                }else{
                    resId = R.drawable.file_zip;
                }
                iv.setImageResource(resId);
            }

            if (f.isFile()){
                t = f.getName();
            }

            tv.setText(t);

            llc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFile(f, v, true);
                }
            });

            llc.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

        }
    }

}