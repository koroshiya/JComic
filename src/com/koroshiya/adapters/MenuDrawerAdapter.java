package com.koroshiya.adapters;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.activities.NavigationDrawerFragment;

public class MenuDrawerAdapter extends RecyclerView.Adapter<MenuDrawerAdapter.ViewHolder> {

    private final MainMenuItem[] items;
    private final NavigationDrawerFragment.NavigationDrawerCallbacks callbacks;

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
        private final AppCompatImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (AppCompatTextView) itemView.findViewById(R.id.viewMenuItemTextView);
            imageView = (AppCompatImageView) itemView.findViewById(R.id.viewMenuItemImageView);
        }

        public void setDataOnView(final int position){
            MainMenuItem rowItem = items[position];

            imageView.setImageResource(rowItem.getImageResId());
            textView.setText(rowItem.getStrResId());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbacks.selectNavDrawerItem(position);
                }
            });
        }
    }

}
