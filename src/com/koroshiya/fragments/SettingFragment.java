package com.koroshiya.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.koroshiya.R;

public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        super.onDetach();
    }

}
