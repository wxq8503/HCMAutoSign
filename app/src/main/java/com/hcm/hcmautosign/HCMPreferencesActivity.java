package com.hcm.hcmautosign;

/**
 * Created by weia on 2018/1/10.
 */

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class HCMPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new HCMPreferenceFragment()).commit();
    }
}
