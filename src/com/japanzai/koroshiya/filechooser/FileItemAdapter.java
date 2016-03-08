package com.japanzai.koroshiya.filechooser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.io_utils.ImageParser;
import com.japanzai.koroshiya.settings.SettingsManager;
import com.japanzai.koroshiya.settings.classes.Recent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileItemAdapter extends FileAdapter {

    public FileItemAdapter(Handler.Callback permCallback, SettingsManager prefs) {
        super(permCallback, prefs, false);
    }

    public void setData(){

        items.clear();

        ArrayList<String> tempList = new ArrayList<>();
        for (File s : curdir.listFiles()){
            if ((s.isDirectory() || ImageParser.isSupportedFile(s)) && !s.isHidden()){tempList.add(s.getAbsolutePath());}
        }
        String[] tempArray = tempList.toArray(new String[0]);
        Arrays.sort(tempArray);
        for (String obj : tempArray){
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

        private CardView cardview;
        private AppCompatImageView imageview;
        private AppCompatTextView textview;

        public ViewHolder(View v) {
            super(v);

            cardview = (CardView) v.findViewById(R.id.list_item_rv_card_view);
            imageview = (AppCompatImageView) v.findViewById(R.id.list_item_rv_image_view);
            textview = (AppCompatTextView) v.findViewById(R.id.list_item_rv_text_view);

        }

        @Override
        public void setDataOnView(FileAdapter.ViewHolder holder, int position) {

            final Recent p = getItem(position);
            Drawable d;
            final Context c = textview.getContext();
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d = c.getDrawable(res);
            }else{
                d = c.getResources().getDrawable(res);
            }
            imageview.setImageDrawable(d);

            int last = t.lastIndexOf('/');
            t = t.substring(last + 1);

            textview.setText(t);

            //TODO: long click; change to file selection mode


        }
    }


}