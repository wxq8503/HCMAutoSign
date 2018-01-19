package com.hcm.hcmautosign;

/**
 * Created by weia on 2018/1/10.
 */

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class HCMPreferencesActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_setting_hcm, false);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new HCMPreferenceFragment()).commit();
    }


}
