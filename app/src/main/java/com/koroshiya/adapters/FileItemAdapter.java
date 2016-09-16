package com.koroshiya.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koroshiya.R;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class FileItemAdapter extends FileAdapter {

    private final DisplayMetrics metrics;

    public FileItemAdapter(Context c, Handler.Callback permCallback) {
        super(c, permCallback, false);
        metrics = c.getResources().getDisplayMetrics();
    }

    public void setData(Context c){

        if (!curdir.exists() || !curdir.isDirectory() || !curdir.canRead()){
            this.curdir = SettingsManager.getLastDirectory(c);
        }

        ArrayList<Recent> recents = SettingsManager.getRecentAndFavorites(c, true);

        SettingsManager.setLastDirectory(c, this.curdir);

        items.clear();

        ArrayList<String> tempList = new ArrayList<>();
        for (File s : curdir.listFiles()){
            if ((s.isDirectory() || ImageParser.isSupportedFile(s)) && !s.isHidden()){tempList.add(s.getAbsolutePath());}
        }
        Collections.sort(tempList);
        for (String obj : tempList){
            Recent r = null;
            for (Recent recent : recents) {
                if (r == null && recent.getPath().equals(obj)){
                    r = recent;
                }
            }
            if (r == null) {
                r = new Recent(obj, 0, 0);
            }
            items.add(r);
        }

        this.notifyDataSetChanged();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rv, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends FileAdapter.ViewHolder{

        private final CardView cardview;
        private final TextView textview, pageCount;

        public ViewHolder(View v) {
            super(v);

            cardview = (CardView) v.findViewById(R.id.list_item_rv_card_view);
            textview = (TextView) v.findViewById(R.id.list_item_rv_text_view);
            pageCount = (TextView) v.findViewById(R.id.list_item_rv_page_count);

        }

        @Override
        public void setDataOnView(int position) {

            final Recent p = getItem(position);
            final String t = p.getPath();

            cardview.setOnClickListener(v -> {
                String fileName = p.getPath();
                File f = new File(fileName);
                setFile(f, v, false);
            });

            int res;
            if (ImageParser.isSupportedImage(t)){
                res = R.drawable.file_media;
            }else if (new File(t).isDirectory()){
                res = R.drawable.file_directory;
            }else{
                res = R.drawable.file_zip;
            }

            int last = t.lastIndexOf('/');
            String val = t.substring(last + 1);

            if (textview != null) {

                int fifteenDp = fromMetrics(15);

                textview.setCompoundDrawablesWithIntrinsicBounds(res, 0, 0, 0);
                textview.setCompoundDrawablePadding(fifteenDp);
                textview.setText(val);

                if (pageCount != null){
                    long uuid = p.getUuid();
                    if (uuid == 0){
                        pageCount.setVisibility(View.GONE);
                        textview.setPadding(fifteenDp, fifteenDp, fifteenDp, fifteenDp);
                    }else{
                        int pnum = p.getPageNumber();
                        String msg = String.format(Locale.getDefault(), "Continue from page %d", pnum);
                        pageCount.setText(msg);
                        pageCount.setVisibility(View.VISIBLE);
                        textview.setPadding(fifteenDp, fifteenDp, fifteenDp, 0);
                    }
                }
            }

        }
    }

    private int fromMetrics(int px){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, metrics);
    }


}