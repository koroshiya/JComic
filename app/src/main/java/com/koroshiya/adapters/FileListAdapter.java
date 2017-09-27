package com.koroshiya.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.koroshiya.R;
import com.koroshiya.controls.EllipsizingTextView;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.classes.Recent;

import java.io.File;

public class FileListAdapter extends FileAdapter {

    private final File cacheDir;
    public static final String ARG_SHEET_FILE = "sheet_file";

    public FileListAdapter(Context c, Handler.Callback permCallback) {
        super(c, permCallback);
        this.cacheDir = c.getCacheDir();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    public void setData(Context c){

        items.clear();
        items.addAll(Recent.getPaths(c));

        super.notifyDataSetChanged();
    }

    private class ViewHolder extends FileAdapter.ViewHolder{

        final LinearLayout llc;
        final ImageView iv;
        final EllipsizingTextView tv;

        ViewHolder(View v) {
            super(v);

            llc = v.findViewById(R.id.row_llc);
            iv = llc.findViewById(R.id.row_img);
            tv = llc.findViewById(R.id.list_item_text);

        }

        @Override
        public void setDataOnView(final int position) {

            String t = getItem(position);
            long uuid = Recent.getUuid(itemView.getContext(), t, -1);
            final File f = new File(t);

            File thumb = new File(cacheDir, Long.toString(uuid) + ".webp");
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

            if (tv != null){
                tv.setText(t);
            }

            llc.setOnClickListener(v -> {
                Message m = new Message();
                Bundle b = new Bundle();
                b.putString(ARG_SHEET_FILE, f.getAbsolutePath());
                m.setData(b);
                permCallback.handleMessage(m);
            });

            llc.setOnLongClickListener(v -> false);

        }
    }

    public void continueReading(View v, String filePath){
        setFile(new File(filePath), v, true);
    }

}