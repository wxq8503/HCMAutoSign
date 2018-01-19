package com.hcm.hcmautosign;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by weia on 2018/1/10.
 */

public class HCMPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting_hcm);
        EditTextPreference editTextPreference_AUTH_CODE = (EditTextPreference) findPreference("KEY_AUTH_CODE");
        EditTextPreference editTextPreference_LONGITUDE = (EditTextPreference) findPreference("KEY_PUNCHIN_LONGITUDE");
        EditTextPreference editTextPreference_LATITUDE = (EditTextPreference) findPreference("KEY_PUNCHIN_LATITUDE");

        String scope = editTextPreference_AUTH_CODE.getText();
        if("0".equals(String.valueOf(scope))) {
            editTextPreference_AUTH_CODE.setSummary("N/A");
        } else {
            editTextPreference_AUTH_CODE.setSummary(scope);
        }

        String LONGITUDE = editTextPreference_LONGITUDE.getText();
        if("0".equals(String.valueOf(LONGITUDE))) {
            editTextPreference_LONGITUDE.setSummary("N/A");
        } else {
            editTextPreference_LONGITUDE.setSummary(LONGITUDE);
        }

        String LATITUDE = editTextPreference_LATITUDE.getText();
        if("0".equals(String.valueOf(LONGITUDE))) {
            editTextPreference_LATITUDE.setSummary("N/A");
        } else {
            editTextPreference_LATITUDE.setSummary(LATITUDE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch(key){
            case "KEY_ENABLE_HCM": {
                break;
            }
            case "KEY_PUNCHIN_LONGITUDE":
            case "KEY_PUNCHIN_LATITUDE":
            case "KEY_AUTH_CODE": {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;
            }
            case "KEY_HCM_LOCATION_LIST":
            {
                // Preference connectionPref = findPreference(key);
                ListPreference listPreference = (ListPreference) findPreference(key);
                listPreference.setSummary(listPreference.getEntry());
                break;
            }
        }
    }
}