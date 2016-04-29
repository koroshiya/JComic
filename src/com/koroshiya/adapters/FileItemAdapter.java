package com.koroshiya.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileItemAdapter extends FileAdapter {

    public FileItemAdapter(Context c, Handler.Callback permCallback) {
        super(c, permCallback, false);
    }

    public void setData(Context c){

        items.clear();

        ArrayList<String> tempList = new ArrayList<>();
        for (File s : curdir.listFiles()){
            if ((s.isDirectory() || ImageParser.isSupportedFile(s)) && !s.isHidden()){tempList.add(s.getAbsolutePath());}
        }
        Collections.sort(tempList);
        for (String obj : tempList){
            Recent r = new Recent(obj, 0, 0);
            items.add(r);
        }

        //scrollToTop();

        this.notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rv, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends FileAdapter.ViewHolder{

        private final CardView cardview;
        private final AppCompatImageView imageview;
        private final AppCompatTextView textview;

        public ViewHolder(View v) {
            super(v);

            cardview = (CardView) v.findViewById(R.id.list_item_rv_card_view);
            imageview = (AppCompatImageView) v.findViewById(R.id.list_item_rv_image_view);
            textview = (AppCompatTextView) v.findViewById(R.id.list_item_rv_text_view);

        }

        @Override
        public void setDataOnView(FileAdapter.ViewHolder holder, int position) {

            final Recent p = getItem(position);
            String t = p.getPath();

            cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName = p.getPath();
                    File f = new File(fileName);
                    setFile(f, v, false);
                }
            });

            int res;
            if (ImageParser.isSupportedImage(t)){
                res = R.drawable.file_media;
            }else if (new File(t).isDirectory()){
                res = R.drawable.file_directory;
            }else{
                res = R.drawable.file_zip;
            }

            imageview.setImageResource(res);

            int last = t.lastIndexOf('/');
            t = t.substring(last + 1);

            textview.setText(t);

            //TODO: long click; change to file selection mode


        }
    }


}