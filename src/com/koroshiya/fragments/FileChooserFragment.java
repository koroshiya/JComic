package com.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.activities.Nav;
import com.koroshiya.adapters.FileItemAdapter;
import com.koroshiya.adapters.FilePathAdapter;

public class FileChooserFragment extends Fragment {

    final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            Bundle b = msg.getData();

            if (b.get("scroll") != null){
                getActivity().findViewById(R.id.file_chooser_recycler_view).scrollTo(0, 0);
            }else if (b.get("permission") != null){
                String permission = b.getString("permission");
                ((Nav) getActivity()).requestPermission(permission);
            }else if (b.get(ReadFragment.ARG_FILE) != null) {
                String filePath = b.getString(ReadFragment.ARG_FILE);
                int page = b.getInt(ReadFragment.ARG_PAGE);
                ((Nav) getActivity()).fileChooserCallback(filePath, page);
            }else if (b.get("selected") != null){
                String filePath = b.getString("selected");
                ((Nav) getActivity()).fileChooserMultiCallback(filePath);
            }else if (b.get(FilePathAdapter.ARG_FILE_PATH_CHUNK) != null){
                String filePath = b.getString(FilePathAdapter.ARG_FILE_PATH_CHUNK);
                boolean goingBack = b.getBoolean(FilePathAdapter.ARG_GOING_BACK, false);
                goToPath(filePath, goingBack);
            }

            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Context c;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = inflater.getContext();
        }

        View rootView = inflater.inflate(R.layout.fragment_file_list, container, false);

        RecyclerView rgv = (RecyclerView) rootView.findViewById(R.id.file_chooser_recycler_view);
        final FileItemAdapter fia = new FileItemAdapter(c, callback);
        rgv.setAdapter(fia);

        RecyclerView bread = (RecyclerView) rootView.findViewById(R.id.file_chooser_breadcrumbs);
        bread.setLayoutManager(new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false));
        FilePathAdapter fpa = new FilePathAdapter(fia.getCurdir(), callback);
        bread.setAdapter(fpa);

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.file_chooser_swiperefreshlayout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fia.setData(c);
                srl.setRefreshing(false);
            }
        });

        return rootView;
    }

    public boolean goToPath(String path, boolean goingBack){
        View view = getActivity().findViewById(R.id.file_chooser_recycler_view);
        RecyclerView lvc = (RecyclerView) view;
        FileItemAdapter fia = (FileItemAdapter) lvc.getAdapter();
        if (goingBack || fia.setPath(view, path)){
            RecyclerView bread = (RecyclerView) getActivity().findViewById(R.id.file_chooser_breadcrumbs);
            FilePathAdapter fpa = (FilePathAdapter) bread.getAdapter();
            boolean success = fpa.setNewPath(path, goingBack);
            if (success) {
                fpa.notifyDataSetChanged();
                bread.invalidateItemDecorations();

                int chunk = fpa.getCurrentChunk();
                if (chunk > 0) {
                    bread.scrollToPosition(chunk);
                }else{
                    bread.getLayoutManager().scrollToPosition(0); //Purposely different than lvc's implementation
                }
            }
            if (goingBack){
                fia.setPath(view, fpa.getCurrentDir());
            }
            lvc.scrollToPosition(0);
            return success;
        }
        return true;
    }

}
