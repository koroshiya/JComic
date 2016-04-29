package com.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.activities.Nav;
import com.koroshiya.adapters.FileListAdapter;
import com.koroshiya.io_utils.ImageParser;

public class RecentFragment extends Fragment {

    public static final String ARG_RECENT = "recent";

    final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            Bundle b = msg.getData();

            if (b.get("permission") != null){
                String permission = b.getString("permission");
                ((Nav) getActivity()).requestPermission(permission);
            }else if (b.get("file") != null) {
                String filePath = b.getString(ReadFragment.ARG_FILE);
                int page = b.getInt(ReadFragment.ARG_PAGE);
                Log.i("FLA", "Found item: "+filePath);
                ((Nav) getActivity()).fileChooserCallback(filePath, page);
            }

            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle b = getArguments();
        boolean isRecent = b.getBoolean(ARG_RECENT, true);
        Context c;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = inflater.getContext();
        }

        Point p = ImageParser.getScreenSize(c);
        float scalefactor = getResources().getDisplayMetrics().density * 170;
        int cols = (int) ((float) p.x / scalefactor) / 2;
        if (cols == 0 || cols == 1)
            cols = 2;

        RecyclerView rgv = (RecyclerView) inflater.inflate(R.layout.fragment_file_chooser, container, false);
        rgv.setLayoutManager(new GridLayoutManager(c, cols));
        FileListAdapter fld = new FileListAdapter(c, callback, isRecent);
        rgv.setAdapter(fld);

        return rgv;
    }
}
