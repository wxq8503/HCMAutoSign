package com.hcm.hcmautosign;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

/**
 * Created by weia on 2018/1/10.
 */

public class MobikePreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref__setting_mobike);
        PreferenceManager.setDefaultValues(getContext(), R.xml.pref__setting_mobike, false);

        EditTextPreference editTextPreference_SCOPE = (EditTextPreference) findPreference("KEY_BIKE_SCOPE");
        EditTextPreference editTextPreference_LONGITUDE = (EditTextPreference) findPreference("KEY_BIKE_LONGITUDE");
        EditTextPreference editTextPreference_LATITUDE = (EditTextPreference) findPreference("KEY_BIKE_LATITUDE");
        EditTextPreference editTextPreference_RESERVED_BIKE_ID = (EditTextPreference) findPreference("KEY_BIKE_ID");
        EditTextPreference editTextPreference_NEAREST_BIKE_ID = (EditTextPreference) findPreference("KEY_NEAREST_BIKE_ID");
        EditTextPreference editTextPreference_SIGN_ID = (EditTextPreference) findPreference("KEY_BIKE_SIGN");
        EditTextPreference editTextPreference_USER_ID = (EditTextPreference) findPreference("KEY_BIKE_USER_ID");

        String scope = editTextPreference_SCOPE.getText();
        if("0".equals(String.valueOf(scope))) {
            editTextPreference_SCOPE.setSummary("N/A");
        } else {
            editTextPreference_SCOPE.setSummary(scope);
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

        String RESERVED_BIKE_ID = editTextPreference_RESERVED_BIKE_ID.getText();
        if("0".equals(String.valueOf(LONGITUDE))) {
            editTextPreference_RESERVED_BIKE_ID.setSummary("N/A");
        } else {
            editTextPreference_RESERVED_BIKE_ID.setSummary(RESERVED_BIKE_ID);
        }

        String NEAREST_BIKE_ID = editTextPreference_NEAREST_BIKE_ID.getText();
        if("0".equals(String.valueOf(LONGITUDE))) {
            editTextPreference_NEAREST_BIKE_ID.setSummary("N/A");
        } else {
            editTextPreference_NEAREST_BIKE_ID.setSummary(NEAREST_BIKE_ID);
        }

        String SIGN_ID = editTextPreference_SIGN_ID.getText();
        if("0".equals(String.valueOf(SIGN_ID))) {
            editTextPreference_SIGN_ID.setSummary("N/A");
        } else {
            editTextPreference_SIGN_ID.setSummary(SIGN_ID);
        }

        String USER_ID = editTextPreference_USER_ID.getText();
        if("0".equals(String.valueOf(USER_ID))) {
            editTextPreference_USER_ID.setSummary("N/A");
        } else {
            editTextPreference_USER_ID.setSummary(USER_ID);
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
            case "KEY_BIKE_SCOPE":
            case "KEY_BIKE_LONGITUDE":
            case "KEY_BIKE_LATITUDE":
            case "KEY_BIKE_SIGN":
            case "KEY_BIKE_USER_ID":
            {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;
            }

            case "KEY_BIKE_TYPE":
                {
               // Preference connectionPref = findPreference(key);
                ListPreference listPreference = (ListPreference) findPreference(key);
                listPreference.setSummary(listPreference.getEntry());
                break;
            }



            case "KEY_BIKE_USE_GPS": {
                Preference connectionPref = findPreference(key);
                EditTextPreference editTextPreference_LONGITUDE = (EditTextPreference) findPreference("KEY_BIKE_LONGITUDE");
                EditTextPreference editTextPreference_LATITUDE = (EditTextPreference) findPreference("KEY_BIKE_LATITUDE");

                boolean checked = ((SwitchPreference) connectionPref).isChecked();
                if(checked ) {
                    editTextPreference_LONGITUDE.setEnabled(false);
                    editTextPreference_LATITUDE.setEnabled(false);
                }else{
                    editTextPreference_LONGITUDE.setEnabled(true);
                    editTextPreference_LATITUDE.setEnabled(true);
                }
                break;
            }
        }
    }
}