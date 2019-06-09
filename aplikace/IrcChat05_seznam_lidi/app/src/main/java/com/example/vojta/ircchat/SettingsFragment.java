package com.example.vojta.ircchat;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String root) {
        setPreferencesFromResource(R.xml.settings, root);
    }
}
