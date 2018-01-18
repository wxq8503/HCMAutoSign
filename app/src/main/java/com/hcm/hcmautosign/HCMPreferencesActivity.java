package com.hcm.hcmautosign;

/**
 * Created by weia on 2018/1/10.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class HCMPreferencesActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.hcm_settings, false);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new HCMPreferenceFragment()).commit();
    }


}
