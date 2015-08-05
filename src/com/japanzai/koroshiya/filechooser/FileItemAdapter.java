package com.japanzai.koroshiya.filechooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.EllipsizingTextView;

import java.util.ArrayList;

public class FileItemAdapter extends BaseAdapter {

    final ArrayList<FileItem> items;
    final Context c;
    final View.OnClickListener ocl;
    final View.OnLongClickListener olcl;

    public FileItemAdapter(Context c, ArrayList<FileItem> items, View.OnClickListener ocl, View.OnLongClickListener olcl) {
        this.c = c;
        this.items = items;
        this.ocl = ocl;
        this.olcl = olcl;
    }

    public long getItemId(int position) {
        return 0;
    }

    public FileItem getItem(int position) {
        return items.get(position);
    }

    public int getCount() {
        return items.size();
    }

    @Override
<<<<<<< HEAD
    public TextView getView(int position, View v, ViewGroup parent) { //TODO: also look at replacing Menu button panels with listviews
=======
    public EllipsizingTextView getView(int position, View v, ViewGroup parent) { //TODO: also look at replacing Menu button panels with listviews
>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9

        EllipsizingTextView tv;

        if (v == null){
            tv = (EllipsizingTextView) LayoutInflater.from(c).inflate(R.layout.list_item, null);
        }else{
            tv = (EllipsizingTextView) v;
        }

        FileItem p = getItem(position);
        tv.setCompoundDrawablesWithIntrinsicBounds(null, p.getRes(), null, null);

        String t = p.getText();
        tv.setContentDescription(t);

        if (t.startsWith("/")){
            int last = t.lastIndexOf('/');
            int sLast = t.substring(0, last).lastIndexOf('/');
            String lStr = t.substring(last);
            String sStr = t.substring(sLast, last);
            t = sStr + lStr;
        }

        tv.setText(t);


        if (ocl != null) tv.setOnClickListener(ocl);
        if (olcl != null) tv.setOnLongClickListener(olcl);

        return tv;

    }
}