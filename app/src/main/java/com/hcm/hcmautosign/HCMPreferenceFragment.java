package com.hcm.hcmautosign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

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

        SwitchPreference auto_hcm_enabled = (SwitchPreference) findPreference("KEY_ENABLE_HCM");
        TimePreference clock_in = (TimePreference) findPreference("timePrefClockIn_Key");
        TimePreference clock_out = (TimePreference) findPreference("timePrefClockOut_Key");
        ListPreference listPreference = (ListPreference) findPreference("KEY_HCM_FUNCTION_LIST");
        boolean checked = auto_hcm_enabled.isChecked();
        if(checked) {
            clock_in.setEnabled(true);
            clock_out.setEnabled(true);
            listPreference.setEnabled(true);
        }else{
            clock_in.setEnabled(false);
            clock_out.setEnabled(false);
            listPreference.setEnabled(false);
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
                Preference connectionPref = findPreference(key);
                TimePreference clock_in = (TimePreference) findPreference("timePrefClockIn_Key");
                TimePreference clock_out = (TimePreference) findPreference("timePrefClockOut_Key");
                ListPreference listPreference = (ListPreference) findPreference("KEY_HCM_FUNCTION_LIST");

                boolean checked = ((SwitchPreference) connectionPref).isChecked();
                if(checked) {
                    clock_in.setEnabled(true);
                    clock_out.setEnabled(true);
                    listPreference.setEnabled(true);
                    getActivity().startService(new Intent(getActivity(), HCMAutoSignService.class));
                }else{
                    getActivity().stopService(new Intent(getActivity(), HCMAutoSignService.class));
                    clock_in.setEnabled(false);
                    clock_out.setEnabled(false);
                    listPreference.setEnabled(false);
                }
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
            case "timePrefClockIn_Key":
            case "timePrefClockOut_Key": {
                Log.i("-----Preference Change","Hello");
                getActivity().startService(new Intent(getActivity(), HCMAutoSignService.class));
                break;
            }
        }
    }
}