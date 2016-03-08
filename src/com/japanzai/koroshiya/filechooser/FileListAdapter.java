package com.japanzai.koroshiya.filechooser;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.EllipsizingTextView;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.classes.Recent;

import java.io.File;

public class FileListAdapter extends FileAdapter {

    private File cacheDir;

    public FileListAdapter(Handler.Callback permCallback, SettingsManager prefs, File cacheDir, boolean isRecent) {
        super(permCallback, prefs, isRecent);
        this.cacheDir = cacheDir;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    public void setData(){

        items.clear();
        items.addAll(prefs.getRecentAndFavorites(isRecent));

        super.notifyDataSetChanged();
    }

    public class ViewHolder extends FileAdapter.ViewHolder{

        LinearLayoutCompat llc;
        EllipsizingTextView tv;
        AppCompatImageView iv;

        public ViewHolder(View v) {
            super(v);

            llc = (LinearLayoutCompat) v.findViewById(R.id.row_llc);
            tv = (EllipsizingTextView) llc.findViewById(R.id.row_text);
            iv = (AppCompatImageView) llc.findViewById(R.id.row_img);

        }

        @Override
        public void setDataOnView(FileAdapter.ViewHolder holder, int position) {

            Drawable d;
            final Recent p = getItem(position);
            String t = p.getPath();
            final File f = new File(t);

            File thumb = new File(cacheDir, p.getUuid() + ".webp");

            Log.i("FLA", "Creating image from: "+thumb.getAbsolutePath());
            d = Drawable.createFromPath(thumb.getAbsolutePath());
            if (d == null){
                Log.i("FLA", "Failed to decode...");
                int resId;
                if (f.isDirectory()){
                    resId = R.drawable.file_directory;
                }else if (ImageParser.isSupportedImage(f)){
                    resId = R.drawable.file_media;
                }else{
                    resId = R.drawable.file_zip;
                }
                iv.setImageResource(resId);
            }else{
                iv.setImageDrawable(d);
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