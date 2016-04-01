package com.japanzai.koroshiya.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.activities.Nav;
import com.japanzai.koroshiya.activities.NavigationDrawerFragment;

public class MenuDrawerAdapter extends RecyclerView.Adapter<MenuDrawerAdapter.ViewHolder> {

    private MainMenuItem[] items;
    private NavigationDrawerFragment.NavigationDrawerCallbacks callbacks;

    public MenuDrawerAdapter(NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks, MainMenuItem[] items) {
        this.items = items;
        this.callbacks = mCallbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_menu_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDataOnView(position);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (AppCompatTextView) itemView.findViewById(R.id.viewMenuItemTextView);
        }

        public void setDataOnView(final int position){
            MainMenuItem rowItem = items[position];

            Drawable d;
            Resources r = textView.getContext().getResources();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                d = r.getDrawable(rowItem.getResId(), null);
            }else{
                d = r.getDrawable(rowItem.getResId());
            }
            if (d != null) d.setBounds(new Rect(0,0,48,48));

            textView.setCompoundDrawables(d, null, null, null);
            textView.setText(rowItem.getText());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbacks.selectNavDrawerItem(position);
                }
            });
        }
    }

}
