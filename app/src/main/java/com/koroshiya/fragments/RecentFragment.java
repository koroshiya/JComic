package com.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koroshiya.R;
import com.koroshiya.activities.Nav;
import com.koroshiya.adapters.FileListAdapter;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;

public class RecentFragment extends Fragment {

    public static final String ARG_RECENT = "recent";

    final Handler.Callback callback = msg -> {

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
            int pageNo = b.getInt(FileListAdapter.ARG_SHEET_PAGE_NO);
            showBottomSheet(filePath, pageNo);
        }

        return false;
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

    @Override
    public void onResume(){
        super.onResume();
        instantiateBottomSheet();
    }

    private void instantiateBottomSheet(){

        //TODO: immersive mode seems to kill the bottom sheet's visibility

        ViewGroup bottomSheet = (ViewGroup) getActivity().findViewById(R.id.bottom_sheet);
        bottomSheet.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        inflater.inflate(R.layout.stub_file_chooser_bottom_sheet, bottomSheet, true);

    }

    private void showBottomSheet(final String filePath, final int pageNo){

        View bottomSheet = getActivity().findViewById(R.id.bottom_sheet);

        if (bottomSheet != null) {
            final BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

            if (filePath != null) {

                RecyclerView rgv = (RecyclerView) getActivity().findViewById(R.id.file_chooser_grid_view);

                if (rgv != null && rgv.getAdapter() instanceof FileListAdapter) {

                    final FileListAdapter fla = (FileListAdapter) rgv.getAdapter();
                    final Context c = rgv.getContext();
                    File f = new File(filePath);

                    ((TextView)bottomSheet.findViewById(R.id.file_chooser_bottom_sheet_txt_title)).setText(f.getName());

                    bottomSheet.findViewById(R.id.file_chooser_bottom_sheet_btn_continue_reading).setOnClickListener(v -> {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        fla.continueReading(v, filePath);
                    });

                    bottomSheet.findViewById(R.id.file_chooser_bottom_sheet_btn_remove_item).setOnClickListener(v -> {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        Recent recent = SettingsManager.getRecentAndFavorite(c, filePath, true);
                        if (recent != null) {
                            SettingsManager.removeRecentAndFavorite(c, recent);
                        }
                        fla.removeItem(pageNo);
                    });

                    bottomSheet.findViewById(R.id.file_chooser_bottom_sheet_btn_remove_all).setOnClickListener(v -> {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        SettingsManager.removeRecentAndFavorites(c);
                        fla.removeAllItems();
                    });

                }

                Log.i("test", "Showing sheet");
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }else{
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        }

    }

}
