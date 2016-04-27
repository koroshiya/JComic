package com.japanzai.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.activities.Nav;
import com.japanzai.koroshiya.adapters.FileItemAdapter;

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
            }

            return false;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context c;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = inflater.getContext();
        }

        View rootView = inflater.inflate(R.layout.fragment_file_list, container, false);

        RecyclerView rgv = (RecyclerView) rootView.findViewById(R.id.file_chooser_recycler_view);
        rgv.setLayoutManager(new LinearLayoutManager(c));
        FileItemAdapter fia = new FileItemAdapter(c, callback);
        rgv.setAdapter(fia);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.file_chooser_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_up:
                goUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goUp(){
        View view = getActivity().findViewById(R.id.file_chooser_recycler_view);
        RecyclerView lvc = (RecyclerView) view;
        FileItemAdapter fia = (FileItemAdapter) lvc.getAdapter();
        fia.up(view);
    }

}
