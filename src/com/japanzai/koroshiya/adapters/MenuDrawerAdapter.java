package com.japanzai.koroshiya.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.japanzai.koroshiya.R;

public class MenuDrawerAdapter extends ArrayAdapter<MainMenuItem> {

    public MenuDrawerAdapter(Context context, int resource, MainMenuItem[] objects) {
        super(context, resource, objects);

    }

    private class ViewHolder {
        AppCompatTextView textView;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        MainMenuItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_menu_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (AppCompatTextView) convertView.findViewById(R.id.viewMenuItemTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Drawable d;
        Resources r = getContext().getResources();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            d = r.getDrawable(rowItem.getResId(), null);
        }else{
            d = r.getDrawable(rowItem.getResId());
        }
        if (d != null) d.setBounds(new Rect(0,0,48,48));

        holder.textView.setCompoundDrawables(d, null, null, null);
        holder.textView.setText(rowItem.getText());

        return convertView;
    }



}
