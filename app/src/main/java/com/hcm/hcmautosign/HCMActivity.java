package com.hcm.hcmautosign;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HCMActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mTextMessage;
    private Button btnPunchin;
    private Button btnHCMSetting;
    private Button btnGeoCheck;
    private Button btnSignRecords;
    private String action;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String TAG = HCMActivity.class.getSimpleName();
    ArrayList<HashMap<String, String>> resultList;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hcm);

        btnPunchin = findViewById(R.id.btnPunchin);
        btnGeoCheck= findViewById(R.id.btnGeoCheck);
        btnSignRecords= findViewById(R.id.btnSignRecords);
        btnHCMSetting = findViewById(R.id.btnHCMSetting);

        btnPunchin.setOnClickListener(this);
        btnGeoCheck.setOnClickListener(this);
        btnSignRecords.setOnClickListener(this);
        btnHCMSetting.setOnClickListener(this);

        mTextMessage = findViewById(R.id.txtStatus);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
        mTextMessage.setText(authorization);
        lv = findViewById(R.id.hcm_list);

        registerReceiver(broadcastReceiver, new IntentFilter("GEO_CHECK"));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // internet lost alert dialog method call from here
            String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
            String longitude;
            longitude = preferences.getString("KEY_PUNCHIN_LONGITUDE", "121.622440");
            String latitude;
            latitude = preferences.getString("KEY_PUNCHIN_LATITUDE", "31.260886");


            action = preferences.getString("KEY_HCM_FUNCTION_LIST", "GeoCheck");
            //action = "GeoCheck";
            Log.i("-----send clock in cmd", action);
            new JSONTask().execute(action, authorization, longitude, latitude);
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        // handle click
        //preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = preferences.edit();

        String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
        String longitude;
        longitude = preferences.getString("KEY_PUNCHIN_LONGITUDE", "121.622440");
        String latitude;
        latitude = preferences.getString("KEY_PUNCHIN_LATITUDE", "31.260886");
        String clock_in;
        clock_in = preferences.getString("timePrefClockIn_Key", "N/A");
        String clock_out;
        clock_out = preferences.getString("timePrefClockOut_Key", "N/A");

        Log.i("-----send time clock in", clock_in);
        Log.i("-----send time clockout", clock_out);

        if("N/A".equals(authorization)){
            Toast.makeText(getApplicationContext(),
                    "Can not find the authrization code,\n Please update the code first!",
                    Toast.LENGTH_LONG).show();
        }

        resultList = new ArrayList<>();
        switch (v.getId()) {
            case R.id.btnGeoCheck:
                mTextMessage.setText("检查打卡点");
                action = "GeoCheck";
                new JSONTask().execute(action, authorization, longitude, latitude);
                break;
            case R.id.btnPunchin:
                mTextMessage.setText(R.string.title_dashboard);
                action = "Punchin";
                new JSONTask().execute(action, authorization, longitude, latitude);
                break;
            case R.id.btnSignRecords:
                mTextMessage.setText(R.string.title_notifications);
                action = "PunCheck";
                new JSONTask().execute(action, authorization, longitude, latitude);
                break;
            case R.id.btnHCMSetting:
                getHCMSettingsFragment(v);
                break;
        }
    }


    public void getHCMSettingsFragment(View view) {
        Intent launchIntent = new Intent(this, HCMPreferencesActivity.class);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    public void goMoBike(View view) {
        Intent launchIntent = new Intent(this, MobikeActivity.class);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    public class JSONTask extends AsyncTask<String,String,String> {

        //Alan
        //final  String authorization = "a6d7677286399a22978a1754cb954c4785eec1f5";
        final  String user_agent = "Mozilla/5.0 (Linux; Android 5.1; SM-J5008 Build/LMY47O; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043613 Safari/537.36 MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN";
        //Jeff
        //final  String authorization = "a01d2cf78baf4d1d0b586f060a804857bede2bbf";
        //final  String user_agent = "Mozilla/5.0 (Linux; Android 6.0; 1505-A01 Build/MRA58K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN";

        @Override
        protected String doInBackground(String... params) {
            try {
                String authorization =  params[1].trim();
                String longitude = params[2].trim();
                String latitude = params[3].trim();

                if(params[0].equals("GeoCheck" )) {
                    Log.i("-----send action", "GeoCheck");
                    return HCM_GeoCheck_OKHttp(authorization, longitude, latitude);
                }
                if(params[0].equals("Punchin")) {
                    Log.i("-----send action", "Punchin");
                    return HCM_Punchin_OKHttp(authorization, longitude, latitude);
                }
                if(params[0].equals("PunCheck")) {
                    Log.i("-----send action", "PunCheck");
                    return HCM_PunCheck_OKHttp(authorization);
                }
            } catch (Exception e){
                Log.i("-----send Error", e.toString());
                Log.i("-----send Error", e.getMessage());
                HashMap<String, String> punch_item = new HashMap<>();

                // adding each child node to HashMap key => value
                punch_item.put("item", "Error Catched");
                punch_item.put("note", e.getMessage());
                resultList.add(punch_item);
                return e.getMessage();
            }
            return null;
        }

        private void addNotification(String showText) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext(), "Channel ID")
                            .setSmallIcon(R.drawable.settings_ic_hcm)
                            .setContentTitle("HCM 打卡")
                            .setContentText(showText)
                            .setContentInfo("Info");

            Intent notificationIntent = new Intent(getApplicationContext(), HCMActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }

        public String HCM_PunCheck_OKHttp(String authorization){
            String url_str = "https://open.hcmcloud.cn/api/attend.view.employee.day";
            String name = "hcm cloud";
            String accuracy = "0";
            //String latitude = "31.260886";
            //String longitude = "121.62253";
            String employee_id = "";
            String date = "";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();
            List<String> hash_text_list = new ArrayList<>();
            //hash_text_list.add(latitude);
            //hash_text_list.add(longitude);
            //hash_text_list.add(accuracy);
            hash_text_list.add(timestamp);
            hash_text_list.add(name);

            String hash_text_joined = TextUtils.join("", hash_text_list);
            String hash_text = md5(hash_text_joined);

            Log.i("-----send timestamp", timestamp);
            Log.i("-----send Hash text", hash_text_joined);
            Log.i("-----send Hashed", hash_text);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, "{\"employee_id\":\"" + employee_id + "\",\"date\":\"" + date + "\"}");
            Log.i("-----send Request Body", "Send Request Body");
            //DownloadManager.Request request = new DownloadManager.Request.Builder()
            Request request = new Request.Builder()
                    .url(url_str)
                    .post(body)
                    .addHeader("charset", "utf-8")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("referer", "https://servicewechat.com/wx3b6d85db7f8fb428/4/page-frame.html")
                    .addHeader("authorization", authorization)
                    .addHeader("content-type", "application/json")
                    .addHeader("user-agent", user_agent)
                    //.addHeader("content-length", "129")
                    .addHeader("host", "open.hcmcloud.cn")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("cache-control", "no-cache")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();//4.获得返回结果
                result = unicodeToUtf8(result);
                //String result = "Test";
                Log.i("-----send Reponse Body", result);
                String return_contactor = "";
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONObject punchinresultObj = jsonObj.getJSONObject("result");
                    String success_flag = punchinresultObj.getString("success");
                    Log.e(TAG, "success_flag: " + success_flag);
                    JSONObject punchindataObj = punchinresultObj.getJSONObject("data");
                    Log.e(TAG, "punchindataObj: " + punchindataObj);
                    // Getting JSON Array node
                    JSONArray punchin = punchindataObj.getJSONArray("signin");
                    Log.e(TAG, "punchin_flag: " + punchin);

                    if(punchin.length()>0){
                        // looping through All punchin
                        for (int i = 0; i < punchin.length(); i++) {
                            JSONObject c = punchin.getJSONObject(i);
                            String source = c.getString("source");
                            String time = c.getString("time");
                            return_contactor = return_contactor + source + "|" + time + "|";
                            // tmp hash map for single contact
                            HashMap<String, String> punch_item = new HashMap<>();
                            // adding each child node to HashMap key => value
                            punch_item.put("item", String.valueOf(i + 1));
                            punch_item.put("note", source + " | " + time);
                            resultList.add(punch_item);
                        }
                    }else{
                        // tmp hash map for single contact
                        HashMap<String, String> punch_item = new HashMap<>();
                        // adding each child node to HashMap key => value
                        punch_item.put("item", "Please clock in first");
                        punch_item.put("note", "No record be found!");
                        resultList.add(punch_item);
                    }
                    addNotification("打卡记录检查完成！");
                } catch (final JSONException e) {
                    try{
                        addNotification("打卡记录检查失败，请检查设置");
                        JSONObject jsonObj = new JSONObject(result);
                        final String errmsg = jsonObj.getString("errmsg");
                        Log.e(TAG, "Error message: " + errmsg);
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Error Message");
                        punch_item.put("note", errmsg);
                        resultList.add(punch_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Error Message: " + errmsg,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }catch ( final JSONException ex) {
                        Log.e(TAG, "Json parsing error 1: " + ex.getMessage());
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Json parsing error_1");
                        punch_item.put("note", ex.getMessage());
                        resultList.add(punch_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error 1: " + ex.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                return  return_contactor;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String HCM_GeoCheck_OKHttp(String authorization, String longitude_str, String latitude_str){
            String url_str = "https://open.hcmcloud.cn/api/attend.signin.geocheck";
            String name = "hcm cloud";
            String accuracy = "0";
            //String latitude = "31.260886";
            //String longitude = "121.62253";
            double random = (Math.random()*10 + 1)/100000;

            double longitude = (Double.parseDouble(longitude_str)+random);
            double latitude = (Double.parseDouble(latitude_str)+random);
            Log.i("-----send longitude", String.valueOf(longitude));
            Log.i("-----send latitude", String.valueOf(latitude));

            NumberFormat format1=NumberFormat.getNumberInstance() ;
            format1.setMaximumFractionDigits(6);
            String longitude_random = format1.format(longitude);
            String latitude_random = format1.format(latitude);

            Log.i("-----send random", String.valueOf(random));
            Log.i("-----send longitude", String.valueOf(longitude_random));
            Log.i("-----send latitude", String.valueOf(latitude_random));

            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();
            List<String> hash_text_list = new ArrayList<>();
            hash_text_list.add(latitude_random);
            hash_text_list.add(longitude_random);
            hash_text_list.add(accuracy);
            hash_text_list.add(timestamp);
            hash_text_list.add(name);

            String hash_text_joined = TextUtils.join("", hash_text_list);
            String hash_text = md5(hash_text_joined);

            Log.i("-----send timestamp", timestamp);
            Log.i("-----send Hash text", hash_text_joined);
            Log.i("-----send Hashed", hash_text);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            String request_body_content = "{\"latitude\":\"" + latitude_random + "\",\"longitude\":\"" + longitude_random + "\",\"accuracy\":0,\"timestamp\":" + timestamp + ",\"hash\":\"" + hash_text + "\"}";
            RequestBody body = RequestBody.create(mediaType, request_body_content);
            Log.i("-----send Request Body", "Send Request Body:"+ request_body_content);
            //DownloadManager.Request request = new DownloadManager.Request.Builder()
            Request request = new Request.Builder()
                    .url(url_str)
                    .post(body)
                    .addHeader("charset", "utf-8")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("referer", "https://servicewechat.com/wx3b6d85db7f8fb428/4/page-frame.html")
                    .addHeader("authorization", authorization)
                    .addHeader("content-type", "application/json")
                    .addHeader("user-agent", user_agent)
                    .addHeader("content-length", "129")
                    .addHeader("host", "open.hcmcloud.cn")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("cache-control", "no-cache")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();//4.获得返回结果
                result = unicodeToUtf8(result);
                //String result = "Test";
                Log.i("-----send Reponse Body", result);
                String return_contact = "";
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONObject jsonCheckresultObj = jsonObj.getJSONObject("result");
                    Log.e(TAG, "Json result: " + jsonCheckresultObj);

                    String check_flag = jsonCheckresultObj.getString("success");
                    Log.e(TAG, "Json check flag: " + check_flag);

                    if("false".equals(check_flag)){
                        HashMap<String, String> punch_item = new HashMap<>();

                        // adding each child node to HashMap key => value
                        punch_item.put("item", "状态--False");
                        punch_item.put("note", "当前坐标不在打卡点附近！");
                        resultList.add(punch_item);
                    }else{
                        JSONObject jsonLocationObj = jsonCheckresultObj.getJSONObject("location");

                        String address = jsonLocationObj.getString("address");
                        String distance = jsonLocationObj.getString("distance");
                        HashMap<String, String> punch_item = new HashMap<>();

                        // adding each child node to HashMap key => value
                        punch_item.put("item", "状态OK");
                        punch_item.put("note", "目标打卡点:" + address + " | 当前距离打卡点:"+ distance + "米");
                        resultList.add(punch_item);
                        return_contact = check_flag + "|打卡地点:" + address + "| 距离目标:"+ distance + "米";
                    }
                    addNotification("位置检查完成");
                } catch (final JSONException e) {
                    try{
                        addNotification("检查到错误");
                        JSONObject jsonObj = new JSONObject(result);
                        final String errmsg = jsonObj.getString("errmsg");
                        Log.e(TAG, "Error message: " + errmsg);
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Error Message");
                        punch_item.put("note", errmsg);
                        resultList.add(punch_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Error Message: " + errmsg,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }catch ( final JSONException ex) {
                        Log.e(TAG, "Json parsing error 1: " + ex.getMessage());
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Json parsing error_1");
                        punch_item.put("note", ex.getMessage());
                        resultList.add(punch_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error 1: " + ex.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                return return_contact;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String HCM_Punchin_OKHttp(String authorization, String longitude, String latitude){
            String url_str = "https://open.hcmcloud.cn/api/attend.signin.create";
            String name = "hcm cloud";
            String location_id = "3218";
            String type = "3";
            //String latitude = "31.260866";
            //String longitude = "121.622440";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();
            //timestamp ="1513039572365";
            List<String> hash_text_list = new ArrayList<>();
            hash_text_list.add(location_id);
            hash_text_list.add(type);
            hash_text_list.add(latitude);
            hash_text_list.add(longitude);
            hash_text_list.add(timestamp);
            hash_text_list.add(name);

            String hash_text_joined = TextUtils.join("", hash_text_list);
            String hash_text = md5(hash_text_joined);

            Log.i("-----send timestamp", timestamp);
            Log.i("-----send Hash text", hash_text_joined);
            Log.i("-----send Hashed", hash_text);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, "{\"location_id\":3218,\"type\":3,\"latitude\":\"" + latitude + "\",\"longitude\":\"" + longitude + "\",\"beacon\":\"\",\"information\":\"{}\",\"timestamp\":"+  timestamp +  ",\"state\":null,\"hash\":\"" + hash_text + "\"}");
            Log.i("-----send Request Body", "Send Request Body");
            //DownloadManager.Request request = new DownloadManager.Request.Builder()
            Request request = new Request.Builder()
                    .url(url_str)
                    .post(body)
                    .addHeader("charset", "utf-8")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("referer", "https://servicewechat.com/wx3b6d85db7f8fb428/4/page-frame.html")
                    .addHeader("authorization", authorization)
                    .addHeader("content-type", "application/json")
                    .addHeader("user-agent", user_agent)
                    .addHeader("content-length", "189")
                    .addHeader("host", "open.hcmcloud.cn")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("cache-control", "no-cache")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();//4.获得返回结果
                result = unicodeToUtf8(result);
                //String result = "Test";
                Log.i("-----send Reponse Body", result);
                String return_contact = "";
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONObject jsonCheckresultObj = jsonObj.getJSONObject("result");
                    Log.e(TAG, "Json result: " + jsonCheckresultObj);

                    String check_flag = jsonCheckresultObj.getString("success");
                    Log.e(TAG, "Json check flag: " + check_flag);

                    JSONObject jsonPunchinObj = jsonCheckresultObj.getJSONObject("punchin");

                    String count = jsonPunchinObj.getString("count");
                    String index = jsonPunchinObj.getString("index");
                    String firsttime = jsonPunchinObj.getString("firsttime");
                    String address = jsonPunchinObj.getString("address");
                    String time = jsonPunchinObj.getString("time");

                    HashMap<String, String> punch_item = new HashMap<>();
                    punch_item.put("item", "打卡地点");
                    punch_item.put("note", address);
                    resultList.add(punch_item);

                    HashMap<String, String> punch_item1 = new HashMap<>();
                    punch_item1.put("item", "排名");
                    punch_item1.put("note", index);
                    resultList.add(punch_item1);

                    HashMap<String, String> punch_item2 = new HashMap<>();
                    punch_item2.put("item", "今日打卡次数");
                    punch_item2.put("note", count);
                    resultList.add(punch_item2);

                    HashMap<String, String> punch_item3 = new HashMap<>();
                    punch_item3.put("item", "第一次打卡时间");
                    punch_item3.put("note", firsttime);
                    resultList.add(punch_item3);

                    HashMap<String, String> punch_item4 = new HashMap<>();
                    punch_item4.put("item", "本次打卡时间");
                    punch_item4.put("note", time);
                    resultList.add(punch_item4);

                    return_contact = check_flag + "|打卡地点:" + address + "|排名:"+ index + "|今日打卡次数:" + count + "|签到时间:" + firsttime + "|本次打卡时间:" + time;
                    addNotification("成功打卡");
                } catch (final JSONException e) {
                    try{
                        addNotification("打卡失败，请检查设置");
                        JSONObject jsonObj = new JSONObject(result);
                        final String errmsg = jsonObj.getString("errmsg");
                        Log.e(TAG, "Error message: " + errmsg);
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Error Message");
                        punch_item.put("note", errmsg);
                        resultList.add(punch_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Error Message: " + errmsg,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }catch ( final JSONException ex) {
                        Log.e(TAG, "Json parsing error 1: " + ex.getMessage());
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Json parsing error_1");
                        punch_item.put("note", ex.getMessage());
                        resultList.add(punch_item);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error 1: " + ex.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                return return_contact;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * unicode 转换成 utf-8
         * @author fanhui
         * 2007-3-15
         * @param theString
         * @return
         */
        public String unicodeToUtf8(String theString) {
            char aChar;
            int len = theString.length();
            StringBuffer outBuffer = new StringBuffer(len);
            for (int x = 0; x < len;) {
                aChar = theString.charAt(x++);
                if (aChar == '\\') {
                    aChar = theString.charAt(x++);
                    if (aChar == 'u') {
                        // Read the xxxx
                        int value = 0;
                        for (int i = 0; i < 4; i++) {
                            aChar = theString.charAt(x++);
                            switch (aChar) {
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    value = (value << 4) + aChar - '0';
                                    break;
                                case 'a':
                                case 'b':
                                case 'c':
                                case 'd':
                                case 'e':
                                case 'f':
                                    value = (value << 4) + 10 + aChar - 'a';
                                    break;
                                case 'A':
                                case 'B':
                                case 'C':
                                case 'D':
                                case 'E':
                                case 'F':
                                    value = (value << 4) + 10 + aChar - 'A';
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Malformed   \\uxxxx   encoding.");
                            }
                        }
                        outBuffer.append((char) value);
                    } else {
                        if (aChar == 't')
                            aChar = '\t';
                        else if (aChar == 'r')
                            aChar = '\r';
                        else if (aChar == 'n')
                            aChar = '\n';
                        else if (aChar == 'f')
                            aChar = '\f';
                        outBuffer.append(aChar);
                    }
                } else
                    outBuffer.append(aChar);
            }
            return outBuffer.toString();
        }

        /**
         * md5加密
         * @param str
         * @return
         */
        public String md5(String str) {
            StringBuffer buffer = new StringBuffer();

            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(str.getBytes());

                for (byte b : digest) {
                    int x = b & 0xff;  // 将byte转换2位的16进制int类型数
                    String s = Integer.toHexString(x); // 将一个int类型的数转为2位的十六进制数
                    if (s.length() == 1) {
                        s = "0" + s;
                    }
                    buffer.append(s);
                    Log.d("vivi", "encode: " + s);

                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //mTextMessage.setText(result);
            ListAdapter adapter = new SimpleAdapter(HCMActivity.this, resultList, R.layout.hcm_list_item, new String[]{ "item","note"}, new int[]{R.id.item, R.id.note});
            lv.setAdapter(adapter);
        }
    }

}
