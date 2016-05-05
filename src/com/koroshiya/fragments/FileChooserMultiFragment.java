package com.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.adapters.FileItemMultiAdapter;

public class FileChooserMultiFragment extends Fragment {

    public static final String ARG_SELECTED = "selected";
    private String fileName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle b = getArguments();
        fileName = b.getString(ARG_SELECTED, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context c;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = inflater.getContext();
        }

        View rootView = inflater.inflate(R.layout.fragment_file_list_multi, container, false);

        RecyclerView rgv = (RecyclerView) rootView.findViewById(R.id.file_chooser_multi_recycler_view);
        rgv.setLayoutManager(new LinearLayoutManager(c));
        FileItemMultiAdapter fia = new FileItemMultiAdapter(c, fileName);
        rgv.setAdapter(fia);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static FileChooserMultiFragment newInstance(String file) {
        FileChooserMultiFragment fragment = new FileChooserMultiFragment();
        Bundle b = new Bundle();
        b.putString(ARG_SELECTED, file);
        fragment.setArguments(b);
        return fragment;
    }

}
