package com.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
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
import com.koroshiya.settings.classes.Recent;

import java.io.File;

public class RecentFragment extends Fragment {

    private final Handler.Callback callback = msg -> {

        Bundle b = msg.getData();

        if (b.get("permission") != null){
            String permission = b.getString("permission");
            ((Nav) getActivity()).requestPermission(permission);
        }else if (b.get(ReadFragment.ARG_FILE) != null) {
            String filePath = b.getString(ReadFragment.ARG_FILE);
            int page = b.getInt(ReadFragment.ARG_PAGE);
            Log.i("FLA", "Found item: "+filePath);
            ((Nav) getActivity()).fileChooserCallback(filePath, page);
        }else if (b.get(FileListAdapter.ARG_SHEET_FILE) != null){
            String filePath = b.getString(FileListAdapter.ARG_SHEET_FILE);
            showAlertPrompt(filePath);
        }

        return false;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
        FileListAdapter fld = new FileListAdapter(c, callback);
        rgv.setAdapter(fld);

        return rgv;
    }

    private void showAlertPrompt(final String filePath){

        RecyclerView rgv = getActivity().findViewById(R.id.file_chooser_grid_view);

        if (rgv != null && rgv.getAdapter() instanceof FileListAdapter) {

            final FileListAdapter fla = (FileListAdapter) rgv.getAdapter();
            final Context c = rgv.getContext();

            File f = new File(filePath);
            String names[] = {
                    c.getString(R.string.continue_reading),
                    c.getString(R.string.remove_from_recent_list),
                    c.getString(R.string.remove_everything_from_recent_list)
            };

            AlertDialog alert = new AlertDialog.Builder(c)
                    .setTitle(f.getName())
                    .setItems(names, (dialogInterface, i) -> {
                        switch (i){
                            case 0:
                                fla.continueReading(rgv, filePath);
                                break;
                            case 1:
                                Recent.delete(c, filePath);
                                fla.removeItem(filePath);
                                break;
                            case 2:
                                Recent.deleteAll(c);
                                fla.removeAllItems();
                                break;
                        }

                        dialogInterface.dismiss();
                    })
                    .create();

            alert.show();

        }

    }

}
