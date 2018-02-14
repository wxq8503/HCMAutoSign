package com.hcm.hcmautosign;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HCMUserEdit extends AppCompatActivity implements View.OnClickListener{

    private TextView txtUser;
    private EditText edtUser;
    private EditText edtCode;
    private Button btnUpdateUser;
    private Button btnDeleteUser;

    private static String prefName;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private List<String> lstEntries;
    private List<String> lstEntryValues;
    private static final String SEPERATOR = "SePeRaToR";

    private String TAG = HCMUserEdit.class.getSimpleName();

    //首先还是先声明这个Spinner控件
    private Spinner users;

    //定义一个String类型的List数组作为数据源
    private List<String> dataList;
    //定义一个ArrayAdapter适配器作为spinner的数据适配器
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Context mContext;
        mContext = getBaseContext();
        //SharedPreferences sharedPref = mContext.getSharedPreferences(Config.KEY_HCM_USER_LIST, Context.MODE_PRIVATE);
        if(Config.debug) Log.i("-----Current User " , mSharedPreferences.getString(Config.KEY_HCM_USER_LIST, "N/A"));

        //final ListPreference lp = (ListPreference) mContext.getSharedPreferences(Config.KEY_HCM_USER_LIST, Context.MODE_PRIVATE);
        //final ListPreference lp = (ListPreference)  mSharedPreferences.getAll();
        int count = mSharedPreferences.getInt("count", 0);
        if(Config.debug) Log.i("-----Current User " , Integer.toString(count));

        if(count == 0){
            //mEntries = lp.getEntries();
            //mEntryValues = lp.getEntryValues();
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
            //lp.setEntries(mEntries);
            //lp.setEntryValues(mEntryValues);
        }
        for(CharSequence cs: mEntries)
            lstEntries.add(cs.toString());
        for(CharSequence cs: mEntryValues)
            lstEntryValues.add(cs.toString());

        mEditor = mSharedPreferences.edit();

        txtUser = findViewById(R.id.txtUser);
        edtUser = findViewById(R.id.edtName);
        edtCode = findViewById(R.id.edtCode);
        btnUpdateUser = findViewById(R.id.btnUpdateUser);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);
        users = findViewById(R.id.spnUsers);

        btnUpdateUser.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);

        //为dataList赋值，将下面这些数据添加到数据源中
        dataList = new ArrayList<String>();
        dataList.add("北京");
        dataList.add("上海");
        dataList.add("广州");
        dataList.add("深圳");
        dataList.add("咸宁");

        /*为spinner定义适配器，也就是将数据源存入adapter，这里需要三个参数
        1. 第一个是Context（当前上下文），这里就是this
        2. 第二个是spinner的布局样式，这里用android系统提供的一个样式
        3. 第三个就是spinner的数据源，这里就是dataList*/
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,lstEntries);

        //为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //为spinner绑定我们定义好的数据适配器
        users.setAdapter(adapter);

        //为spinner绑定监听器，这里我们使用匿名内部类的方式实现监听器
        users.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtUser.setText("您当前选择的是：");
                edtUser.setText(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txtUser.setText("请选择User");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

    @Override
    public void onClick(View v) {
        // handle click

        String action;
        String authorization = mSharedPreferences.getString("KEY_AUTH_CODE", "N/A");

        if("N/A".equals(authorization)){
            Toast.makeText(getApplicationContext(),
                    "Can not find the authrization code,\n Please update the code first!",
                    Toast.LENGTH_LONG).show();
        }else{
            switch (v.getId()) {
                case R.id.btnUpdateUser:
                    action = "Update";
                    if(Config.debug) Log.i("-----Action", action);
                    break;
                case R.id.btnDeleteUser:
                    action = "Delete";
                    if(Config.debug) Log.i("-----Action", action);
                    break;
            }
        }
    }
}
