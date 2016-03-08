package com.japanzai.koroshiya.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.japanzai.koroshiya.R;

public class CreditsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context c;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = inflater.getContext();
        }

        View rootView = inflater.inflate(R.layout.credits, container, false);

        String versionName;
        try{
            versionName = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A"; // This should never happen anyway. Handling added just in case.
        }
        ((AppCompatTextView)rootView.findViewById(R.id.txtVersion)).setText(String.format(getString(R.string.version), versionName));

        return rootView;
    }
}
