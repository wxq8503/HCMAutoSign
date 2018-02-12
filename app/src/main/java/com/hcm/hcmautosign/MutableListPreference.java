package com.hcm.hcmautosign;

/**
 * Created by weia on 2018/2/12.
 */

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class MutableListPreference extends ListPreference {

    private static String prefName;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private List<String> lstEntries;
    private List<String> lstEntryValues;
    private static final String SEPERATOR = "SePeRaToR";

    public MutableListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        prefName = getKey();
        lstEntries = new ArrayList<String>();
        lstEntryValues = new ArrayList<String>();
        mSharedPreferences = context.getSharedPreferences(prefName,0);
        mEditor = mSharedPreferences.edit();
        int count = mSharedPreferences.getInt("count", 0);
        if(count == 0){
            mEntries = getEntries();
            mEntryValues = getEntryValues();
            for(int i=0; i<mEntries.length; i++){
                mEditor.putString(""+i, mEntries[i]+SEPERATOR+mEntryValues[i]);
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
            setEntries(mEntries);
            setEntryValues(mEntryValues);
        }
        for(CharSequence cs: mEntries)
            lstEntries.add(cs.toString());
        for(CharSequence cs: mEntryValues)
            lstEntryValues.add(cs.toString());

    }

    public void addEntry(String key, String value){
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
        setEntries(mEntries);
        setEntryValues(mEntryValues);

    }
    public void removeEntry(String key){
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
        setEntries(mEntries);
        setEntryValues(mEntryValues);
        for(int i=0; i< mEntries.length; i++){
            mEditor.putString(""+i, mEntries[i] + SEPERATOR + mEntryValues[i]);
        }
        mEditor.putInt("count", lstEntries.size());
        mEditor.commit();
    }
}