package com.hcm.hcmautosign;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.Switch;

/**
 * Created by weia on 2018/1/10.
 */

public class HCMPreferenceFragment extends PreferenceFragment {


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.hcm_settings);

        EditTextPreference authCodeEditTextPre = (EditTextPreference) findPreference("KEY_AUTH_CODE");
        /*
        authCodeEditTextPre.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if("0".equals(String.valueOf(newValue))) {
                    preference.setSummary("");
                } else {
                    preference.setSummary(newValue + "------Click to change");
                }
                return true;
            }
        });
*/
        String auth_code = authCodeEditTextPre.getText();
        if("0".equals(String.valueOf(auth_code))) {
            authCodeEditTextPre.setSummary("");
        } else {
            authCodeEditTextPre.setSummary(auth_code + "-----Click to change");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {

        switch(preference.getKey()){
            case "KEY_ENABLE_HCM": {
                break;
            }
            case "KEY_AUTH_CODE": {
                EditTextPreference authCodeEditTextPre = (EditTextPreference) findPreference("KEY_AUTH_CODE");
                String newValue = authCodeEditTextPre.getText();
                if("0".equals(String.valueOf(newValue))) {
                    preference.setSummary("");
                } else {
                    preference.setSummary(newValue + "------Click to change");
                }
                break;
            }
        }
        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}