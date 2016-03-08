package com.japanzai.koroshiya.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.japanzai.koroshiya.R;

public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context c;
        SharedPreferences prefs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            c = getContext();
        }else{
            c = getActivity();
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(c);
        try {
            prefs.getString("zoomIndex", "");
            prefs.getString("orientationIndex", "");
            prefs.getString("dynamicResizing", "");
        }catch (ClassCastException cce){ //If we're upgrading from an old version, port across the old list values
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("zoomIndex");
            editor.putString("zoomIndex", Integer.toString(prefs.getInt("zoomIndex", 0)));
            editor.remove("orientationIndex");
            editor.putString("orientationIndex", Integer.toString(prefs.getInt("orientationIndex", 0)));
            editor.remove("dynamicResizing");
            editor.putString("dynamicResizing", Integer.toString(prefs.getInt("dynamicResizing", 0))).apply();
        }
        addPreferencesFromResource(R.xml.pref_general);
        super.onDetach();
    }

}
