package com.hcm.hcmautosign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by weia on 2018/1/10.
 */

public class HCMPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static String prefName;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private List<String> lstEntries;
    private List<String> lstEntryValues;
    private static final String SEPERATOR = "SePeRaToR";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting_hcm);
        //getPreferenceManager().setSharedPreferencesName(Config.PREFERENCE_NAME);

        EditTextPreference editTextPreference_AUTH_CODE = (EditTextPreference) findPreference(Config.KEY_AUTH_CODE);
        EditTextPreference editTextPreference_LONGITUDE = (EditTextPreference) findPreference(Config.KEY_PUNCHIN_LONGITUDE);
        EditTextPreference editTextPreference_LATITUDE = (EditTextPreference) findPreference(Config.KEY_PUNCHIN_LATITUDE);
        //ListPreference listPreference_Devices = (ListPreference) findPreference(Config.KEY_HCM_USER_AGENT_LIST);
        //final ListPreference dynamicListPreference = (ListPreference) findPreference(Config.KEY_HCM_USER_LIST);

        final ListPreference lp_1 = MutableListPreference((ListPreference) findPreference(Config.KEY_HCM_USER_LIST), getContext());
        final ListPreference lp = setListPreferenceData((ListPreference) findPreference(Config.KEY_HCM_USER_LIST), getActivity());
        lp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setListPreferenceData(lp, getActivity());
                return false;
            }
        });

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

        SwitchPreference auto_hcm_enabled = (SwitchPreference) findPreference(Config.KEY_ENABLE_HCM);
        TimePreference clock_in = (TimePreference) findPreference(Config.KEY_PUNCHIN_TIME);
        TimePreference clock_out = (TimePreference) findPreference(Config.KEY_PUNCHOUT_TIME);
        ListPreference listPreference = (ListPreference) findPreference(Config.KEY_HCM_FUNCTION_LIST);
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

    protected ListPreference setListPreferenceData(ListPreference lp, Activity mActivity) {
        CharSequence[] entries = {"A", "J", "S"};
        EditTextPreference editTextPreference_AUTH_CODES = (EditTextPreference) findPreference(Config.KEY_AUTH_CODE);
        String authorization_codes = editTextPreference_AUTH_CODES.getText();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String all_auth_codes = preferences.getString(Config.KEY_AUTH_CODES, "A-J-S");

        CharSequence[] entryValues =all_auth_codes.split("-");

        if(lp == null)
            lp = new ListPreference(mActivity);
        lp.setEntries(entries);
        lp.setEntryValues(entryValues);
        lp.setSummary(lp.getEntry() + "-" + lp.getValue());

        lp.setDefaultValue("5a069698214826e690fbe082eb0da8fd01af6467");
        lp.setDialogTitle("Select USER");
        //if(Config.debug) Log.i("-----Set Users List",authorization_codes);
        return lp;
    }

    public ListPreference MutableListPreference(ListPreference lp, Context context) {
        prefName = lp.getKey();
        lstEntries = new ArrayList<String>();
        lstEntryValues = new ArrayList<String>();
        mSharedPreferences = context.getSharedPreferences(prefName,0);
        mEditor = mSharedPreferences.edit();
        int count = mSharedPreferences.getInt("count", 0);
        if(count == 0){
            mEntries = lp.getEntries();
            mEntryValues = lp.getEntryValues();
            for(int i=0; i<mEntries.length; i++){
                mEditor.putString("" + i, mEntries[i] + SEPERATOR + mEntryValues[i]);
                if(Config.debug) Log.i("-----User " + i, mEntries[i] + SEPERATOR + mEntryValues[i]);
            }
            mEditor.putInt("count", mEntries.length);
            mEditor.commit();
        }else{
            String[] temp;
            mEntries = new String[count];
            mEntryValues = new String[count];
            for(int i=0; i<count; i++){
                temp = mSharedPreferences.getString(""+i, null).split(SEPERATOR);
                mEntries[i] = temp[0];
                mEntryValues[i] = temp[1];
            }
            lp.setEntries(mEntries);
            lp.setEntryValues(mEntryValues);
        }
        for(CharSequence cs: mEntries)
            lstEntries.add(cs.toString());
        for(CharSequence cs: mEntryValues)
            lstEntryValues.add(cs.toString());
        return lp;
    }

    public ListPreference addEntry(ListPreference lp, String key, String value){
        mEditor.putString(lstEntries.size()+"", key+SEPERATOR+value);
        mEditor.putInt("count", lstEntries.size()+1);
        mEditor.commit();
        lstEntries.add(key);
        lstEntryValues.add(value);
        mEntries = new CharSequence[lstEntries.size()];
        mEntryValues = new CharSequence[lstEntryValues.size()];
        for(int i=0; i< lstEntries.size(); i++){
            mEntries[i] = lstEntries.get(i);
            mEntryValues[i] = lstEntryValues.get(i);
        }
        lp.setEntries(mEntries);
        lp.setEntryValues(mEntryValues);
        return lp;
    }

    public ListPreference removeEntry(ListPreference lp, String key){
        for(int i=0; i< lstEntries.size(); i++){
            if(key.equals(lstEntries.get(i))){
                lstEntries.remove(i);
                lstEntryValues.remove(i);
                --i;
            }
        }
        mEntries = new CharSequence[lstEntries.size()];
        mEntryValues = new CharSequence[lstEntryValues.size()];
        for(int i=0; i< lstEntries.size(); i++){
            mEntries[i] = lstEntries.get(i);
            mEntryValues[i] = lstEntryValues.get(i);
        }
        lp.setEntries(mEntries);
        lp.setEntryValues(mEntryValues);
        for(int i=0; i< mEntries.length; i++){
            mEditor.putString(""+i, mEntries[i] + SEPERATOR + mEntryValues[i]);
        }
        mEditor.putInt("count", lstEntries.size());
        mEditor.commit();
        return lp;
    }

    public ListPreference updateEntry(ListPreference lp, String key, String new_value){
        for(int i=0; i< lstEntries.size(); i++){
            if(key.equals(lstEntries.get(i))){
                lstEntries.remove(i);
                lstEntryValues.remove(i);
                --i;
            }
        }
        mEntries = new CharSequence[lstEntries.size()];
        mEntryValues = new CharSequence[lstEntryValues.size()];
        for(int i=0; i< lstEntries.size(); i++){
            mEntries[i] = lstEntries.get(i);
            if(key.equals(lstEntries.get(i))){
                mEntryValues[i] = new_value;
            }else{
                mEntryValues[i] = lstEntryValues.get(i);
            }
        }
        lp.setEntries(mEntries);
        lp.setEntryValues(mEntryValues);
        for(int i=0; i< mEntries.length; i++){
            mEditor.putString(""+i, mEntries[i] + SEPERATOR + mEntryValues[i]);
        }
        mEditor.putInt("count", lstEntries.size());
        mEditor.commit();
        return lp;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch(key){
            case Config.KEY_ENABLE_HCM: {
                Preference connectionPref = findPreference(key);
                TimePreference clock_in = (TimePreference) findPreference(Config.KEY_PUNCHIN_TIME);
                TimePreference clock_out = (TimePreference) findPreference(Config.KEY_PUNCHOUT_TIME);
                ListPreference listPreference = (ListPreference) findPreference(Config.KEY_HCM_FUNCTION_LIST);

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
            case Config.KEY_PUNCHIN_LONGITUDE:
            case Config.KEY_PUNCHIN_LATITUDE:
            case Config.KEY_AUTH_CODE:
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //editor.putString(Config.KEY_AUTH_CODES, "5a069698214826e690fbe082eb0da8fd01af64670-5a069698214826e690fbe082eb0da8fd01af64671-17a7c3d1ee1bfee70c2eb9a22c8ff783365bc651");
                //editor.commit();

                Preference connectionPref = findPreference(key);
                String new_code = sharedPreferences.getString(key, "");
                connectionPref.setSummary(new_code);

                String all_auth_codes = sharedPreferences.getString(Config.KEY_AUTH_CODES, "A-J-S");
                if(Config.debug) Log.i("-----AllUserCodes", all_auth_codes);
                String auth_code[] = all_auth_codes.split("-");
                if(Config.debug) Log.i("-----auth_code[0]", auth_code[0]);
                if(Config.debug) Log.i("-----auth_code[1]", auth_code[1]);
                if(Config.debug) Log.i("-----auth_code[2]", auth_code[2]);
                String all_auth_codes_new = all_auth_codes;

                ListPreference userListPref = (ListPreference) findPreference(Config.KEY_HCM_USER_LIST);
                String user_name = (String)userListPref.getEntry();
                if(Config.debug) Log.i("-----Cur User Name", user_name);
                if(Config.debug) Log.i("-----Cur User Code", userListPref.getValue());
                if(Config.debug) Log.i("-----New User Code", new_code);
                int index = userListPref.findIndexOfValue(userListPref.getValue());
                if(Config.debug) Log.i("-----User Index", Integer.toString((index)));

                if(index==0){
                    all_auth_codes_new =  new_code + "-" + auth_code[1] + "-" + auth_code[2];
                }else if(index==1){
                    all_auth_codes_new =  auth_code[0] + "-" + new_code + "-" + auth_code[2];
                }else if(index==2){
                    all_auth_codes_new =  auth_code[0] + "-" + auth_code[1] + "-" + new_code;
                }

                if(Config.debug) Log.i("-----AllUserCodes_New", all_auth_codes_new);

                editor.putString(Config.KEY_AUTH_CODES, all_auth_codes_new);
                editor.commit();
                setListPreferenceData(userListPref, getActivity());
                break;
            }
            case Config.KEY_HCM_USER_LIST:
            {
                ListPreference connectionPref = (ListPreference) findPreference(key);
                if(Config.debug) Log.i("-----User Change", connectionPref.getEntry() + "-" + connectionPref.getValue());
                connectionPref.setSummary(connectionPref.getEntry() + "-" + connectionPref.getValue());

                EditTextPreference editTextPreference_AUTH_CODE = (EditTextPreference) findPreference(Config.KEY_AUTH_CODE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Config.KEY_AUTH_CODE, connectionPref.getValue());
                editor.commit();
                editTextPreference_AUTH_CODE.setSummary(connectionPref.getValue());
                if(Config.debug) Log.i("-----Auth Code Change", connectionPref.getValue());
                break;
            }
            case Config.KEY_HCM_LOCATION_LIST:
            case Config.KEY_HCM_USER_AGENT_LIST:
            {
                // Preference connectionPref = findPreference(key);
                ListPreference listPreference = (ListPreference) findPreference(key);
                if(Config.debug) Log.i("-----User Agent Change", listPreference.getValue());
                listPreference.setSummary(listPreference.getEntry());
                break;
            }
            case Config.KEY_PUNCHIN_TIME:
            case Config.KEY_PUNCHOUT_TIME: {
                if(Config.debug) Log.i("-----Preference Change","Hello");
                getActivity().startService(new Intent(getActivity(), HCMAutoSignService.class));
                break;
            }
        }
    }
}