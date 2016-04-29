package com.koroshiya.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileItemMultiAdapter extends FileAdapter {

    private final ArrayList<String> selected;
    private final int primary;
    private final int secondary;

    public FileItemMultiAdapter(Context c, String fileName) {
        super(c, null, false);

        selected = new ArrayList<>();
        primary = Color.WHITE;
        secondary = Color.LTGRAY;

        selected.add(fileName);
    }

    public void setData(Context c){

        if (!curdir.exists() || !curdir.isDirectory() || !curdir.canRead()){
            this.curdir = SettingsManager.getLastDirectory(c);
        }

        SettingsManager.setLastDirectory(c, this.curdir);

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_multi_rv, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends FileAdapter.ViewHolder{

        private final CardView cardview;
        private final AppCompatImageView imageview;
        private final AppCompatTextView textview;

        public ViewHolder(View v) {
            super(v);

            cardview = (CardView) v.findViewById(R.id.list_item_multi_rv_card_view);
            imageview = (AppCompatImageView) v.findViewById(R.id.list_item_multi_rv_image_view);
            textview = (AppCompatTextView) v.findViewById(R.id.list_item_multi_rv_text_view);

        }

        @Override
        public void setDataOnView(int position) {

            final Recent p = getItem(position);
            final String t = p.getPath();

            if (selected.contains(t)) {
                cardview.setCardBackgroundColor(secondary);
            }else {
                cardview.setCardBackgroundColor(primary);
            }

            cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cur = getAdapterPosition();
                    if (selected.contains(t)) {
                        selected.remove(t);
                        cardview.setCardBackgroundColor(primary);
                    }
                    else {
                        selected.add(t);
                        cardview.setCardBackgroundColor(secondary);
                    }
                    notifyItemChanged(cur);
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
            String val = t.substring(last + 1);

            textview.setText(val);

        }
    }


}