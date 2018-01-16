package com.hcm.hcmautosign;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

/**
 * Created by weia on 2018/1/10.
 */

public class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.hcm_settings);

        //自动打卡开关
        Preference hcmPref = findPreference("KEY_ENABLE_HCM");
        hcmPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((Boolean) newValue) {
                    //Intent startIntent = new Intent(this, HCMAutoSignService.class);
                    //startService(startIntent);
                    return true;
                    //((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                } else{
                    //Intent startIntent = new Intent(this, HCMAutoSignService.class);
                    //stopService(startIntent);
                    return false;
                }
            }
        });

        final EditTextPreference authCodeEditTextPre = (EditTextPreference) findPreference("KEY_AUTH_CODE");
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
/*
        String delay = authCodeEditTextPre.getText();
        if("0".equals(String.valueOf(delay))) {
            authCodeEditTextPre.setSummary("");
        } else {
            authCodeEditTextPre.setSummary(delay + "-----Click to change");
        }
*/
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        //如果“保存个人信息”这个按钮被选中，将进行括号里面的操作
        if ("yesno_save_individual_info".equals(preference.getKey())) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("yesno_save_individual_info");
            EditTextPreference editTextPreference = (EditTextPreference) findPreference("individual_name");
            //让editTextPreference和checkBoxPreference的状态保持一致
            editTextPreference.setEnabled(checkBoxPreference.isChecked());
        }
        // TODO Auto-generated method stub
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}