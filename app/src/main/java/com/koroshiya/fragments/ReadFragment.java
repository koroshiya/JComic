package com.koroshiya.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.R;
import com.koroshiya.ReadCache;
import com.koroshiya.activities.Nav;
import com.koroshiya.controls.JBitmapDrawable;
import com.koroshiya.controls.JScrollView;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.listeners.GestureListener;
import com.koroshiya.settings.SettingsManager;
import com.koroshiya.settings.classes.Recent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ReadFragment extends Fragment {

    public static final String ARG_FILE = "file";
    public static final String ARG_PAGE = "page";

    private JScrollView jsv;
    private ReadCache cache;

    public static ReadFragment newInstance(String file, int page, Context c) {
        ReadFragment fragment = new ReadFragment();
        Bundle b = new Bundle();
        Log.i("FLA", "Instance: "+file);
        if (file == null) {

            ArrayList<Recent> recents = Recent.get(c, true);

            if (recents.size() == 0) {
                return null;
            }else{
                Recent r = recents.get(recents.size() - 1);
                page = r.getPageNumber();
                file = r.getPath();
            }

        }
        File f = new File(file);

        if (!ImageParser.isSupportedFile(f) || page == -1){
            return null;
        }else{
            b.putString(ARG_FILE, file);
            b.putInt(ARG_PAGE, page);
        }
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readBundleArgs(getArguments());
    }

    public void reset(String fileName, int page, View v){
        try {

            if (fileName.length() == 0 || page < 0)
                throw new IOException("Invalid file name or page number");

            File file = new File(fileName);
            cache = new ReadCache(file, page, this);

            if (v != null) cache.parseInitial(v.getContext(), v);

        } catch (IOException e) {
            v = getActivity().findViewById(R.id.drawer_layout);
            if (v != null) Snackbar.make(v, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
            ((Nav)getActivity()).selectNavItem(R.id.nav_select_comic);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_read, container, false);
        jsv = v.findViewById(R.id.read_fragment_jscrollview);

        GestureListener gestureListener = new GestureListener(jsv, this);

        jsv.setOnTouchListener(gestureListener);

        readBundleArgs(savedInstanceState);

        return v;
    }

    private void readBundleArgs(Bundle b){
        if (b != null) {
            String fileName = b.getString(ARG_FILE, "");
            Log.i("FLA", "Create: " + fileName);
            int page = b.getInt(ARG_PAGE, -1);
            reset(fileName, page, null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        showProgress(view.getContext());
    }

    private void showProgress(Context c){
        if (cache != null) {
            cache.parseInitial(c, null);
        }else{
            View v = getActivity().findViewById(R.id.drawer_layout);
            if (v != null) Snackbar.make(v, "Can't read current comic - Cache empty", Snackbar.LENGTH_LONG).show();
            ((Nav)getActivity()).selectNavItem(R.id.nav_select_comic);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        if (cache != null) {
            b.putString(ARG_FILE, cache.getFileName());
            b.putInt(ARG_PAGE, cache.getCurrentPage());
        }
        super.onSaveInstanceState(b);
    }

    public void hideProgress(){
        applySettings(true);
    }

    private void applySettings(boolean enabled){
        Activity act = getActivity();
        SettingsManager.setBacklightAlwaysOn(act, enabled);
        SettingsManager.setImmersiveMode(act, enabled);
    }

    @Override
    public void onResume(){
        super.onResume();
        applySettings(true);
    }

    @Override
    public void onPause(){
        applySettings(false);
        super.onPause();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        applySettings(true);
    }

    @Override
    public void onDetach() {
        applySettings(false);

        if (cache != null) {
            cache.close();
        }
        super.onDetach();
    }

    public void setImage(JBitmapDrawable drawable){
        jsv.setImageDrawable(drawable);
        jsv.invalidate();
    }

    public JBitmapDrawable getImage(){
        return jsv.getImageDrawable();
    }

    public void next(View v){

        Context c;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = getActivity();
        }

        cache.next(v, c);
    }

    public void previous(View v){

        Context c;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = getActivity();
        }

        cache.previous(v, c);
    }

}
