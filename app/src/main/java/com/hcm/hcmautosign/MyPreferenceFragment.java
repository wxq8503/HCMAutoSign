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

public class MyPreferenceFragment extends PreferenceFragment {


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.hcm_settings);

        EditTextPreference authCodeEditTextPre = (EditTextPreference) findPreference("KEY_AUTH_CODE");
        EditTextPreference editTextPreference_LONGITUDE = (EditTextPreference) findPreference("KEY_BIKE_LONGITUDE");
        EditTextPreference editTextPreference_LATITUDE = (EditTextPreference) findPreference("KEY_BIKE_LATITUDE");
        EditTextPreference editTextPreference_RESERVED_BIKE_ID = (EditTextPreference) findPreference("KEY_BIKE_ID");
        EditTextPreference editTextPreference_NEAREST_BIKE_ID = (EditTextPreference) findPreference("KEY_NEAREST_BIKE_ID");
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
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        EditTextPreference editTextPreference_LONGITUDE = (EditTextPreference) findPreference("KEY_BIKE_LONGITUDE");
        EditTextPreference editTextPreference_LATITUDE = (EditTextPreference) findPreference("KEY_BIKE_LATITUDE");
        //如果“保存个人信息”这个按钮被选中，将进行括号里面的操作
        switch(preference.getKey()){
            case "KEY_ENABLE_HCM": {
                break;
            }

            case "KEY_BIKE_USE_GPS": {
                boolean checked = ((SwitchPreference) preference).isChecked();
                if(checked ) {
                    editTextPreference_LONGITUDE.setEnabled(false);
                    editTextPreference_LATITUDE.setEnabled(false);
                }else{
                    editTextPreference_LONGITUDE.setEnabled(true);
                    editTextPreference_LATITUDE.setEnabled(true);
                }
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
            case "KEY_BIKE_SIGN": {
                EditTextPreference signCodeEditTextPre = (EditTextPreference) findPreference("KEY_BIKE_SIGN");
                String newValue = signCodeEditTextPre.getText();
                if("0".equals(String.valueOf(newValue))) {
                    preference.setSummary("");
                } else {
                    preference.setSummary(newValue);
                }
                break;
            }
            case "KEY_BIKE_USER_ID": {
                EditTextPreference user_idEditTextPre = (EditTextPreference) findPreference("KEY_BIKE_USER_ID");
                String newValue = user_idEditTextPre.getText();
                if("0".equals(String.valueOf(newValue))) {
                    preference.setSummary("");
                } else {
                    preference.setSummary(newValue);
                }
                break;
            }
        }
        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}