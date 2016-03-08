package com.japanzai.koroshiya.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.japanzai.koroshiya.R;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.MainMenuViewHolder> {

    @Override
    public MainMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_menu_item, parent, false);
        return new MainMenuViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainMenuViewHolder holder, int position) {
        holder.setDataOnView(position);
    }

    @Override
    public int getItemCount() {
        return mainMenuItems.values().length;
    }

    public static class MainMenuViewHolder extends RecyclerView.ViewHolder{

        private AppCompatTextView itemText;
        private Context context;

        public MainMenuViewHolder(View v) {
            super(v);
            this.context = v.getContext();
            itemText = (AppCompatTextView) v.findViewById(R.id.viewMenuItemTextView);
        }

        public void setDataOnView(int position) {

            mainMenuItems item = mainMenuItems.values()[position];
            Resources r = context.getResources();
            Drawable d;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                d = r.getDrawable(item.getResId(), null);
            }else{
                d = r.getDrawable(item.getResId());
            }
            if (d != null) d.setBounds(new Rect(0,0,48,48));

            itemText.setCompoundDrawables(d, null, null, null);
            itemText.setText(item.toString());

            Log.i("MainMenuAdapter", "Position: "+position);

        }

    }

    private enum mainMenuItems {
        READ("Select comic to begin reading", R.drawable.ic_book),
        RESUME("Continue reading where you left off", R.drawable.bookmark),
        SETTINGS("Change JComic's settings", R.drawable.ic_settings_24dp),
        HELP("Need help", R.drawable.ic_info_black_24dp),
        CREDITS("Credits", R.drawable.ic_account_circle_24dp),
        ERRORS("Report Error", R.drawable.ic_report_problem_24dp),
        ;

        private final String text;
        private final int resId;

        mainMenuItems(String text, int resId) {
            this.text = text;
            this.resId = resId;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getResId(){
            return resId;
        }
    }

}
