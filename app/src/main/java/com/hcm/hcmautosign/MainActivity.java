package com.hcm.hcmautosign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    //private TextView mTextAuth;
    private  String action;
    private SharedPreferences preferences;
    private String TAG = MainActivity.class.getSimpleName();
    ArrayList<HashMap<String, String>> resultList;
    private ListView lv;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
            String longitude;
            longitude = preferences.getString("KEY_PUNCHIN_LONGITUDE", "121.622440");
            String latitude;
            latitude = preferences.getString("KEY_PUNCHIN_LATITUDE", "31.260886");

            if("N/A".equals(authorization)){
                Toast.makeText(getApplicationContext(),
                        "Can not find the authrization code,\n Please update the code first!",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            resultList = new ArrayList<>();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText("检查打卡点");
                    action = "GeoCheck";
                    new JSONTask().execute(action, authorization, longitude, latitude);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    action = "Punchin";
                    new JSONTask().execute(action, authorization, longitude, latitude);
                    //return HCM_Punchin_OKHttp();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    action = "PunCheck";
                    new JSONTask().execute(action, authorization, longitude, latitude);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
        mTextMessage.setText(authorization);

        lv = findViewById(R.id.hcm_list);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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
                String authorization =  params[1];
                String longitude = params[2];
                String latitude = params[3];

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
                    return HCM_PunCheck(authorization);
                }
            } catch (Exception e){
                Log.i("-----send Error", e.toString());
                Log.i("-----send Error", e.getMessage());
            }
            return null;
        }

        public String HCM_PunCheck(String authorization) {
            BufferedReader reader;
            OutputStream os = null;
            InputStream is = null;
            String url_str = "https://open.hcmcloud.cn/api/attend.view.employee.day";
            String name = "hcm cloud";
            String employee_id = "";
            String date = "";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();

            //timestamp ="1513039572365";
            List<String> hash_text_list = new ArrayList<>();
            hash_text_list.add(timestamp);
            hash_text_list.add(name);
            String hash_text_joined = TextUtils.join("", hash_text_list);
            String hash_text = md5(hash_text_joined);
            Log.i("-----send timestamp", timestamp);
            Log.i("-----send Hash text", hash_text_joined);
            Log.i("-----send Hashed", hash_text);
            HttpURLConnection connection = null;
            try {
                JSONObject ClientKey = new JSONObject();
                ClientKey.put("employee_id", employee_id);
                ClientKey.put("date", date);
                String content = String.valueOf(ClientKey);
                Log.i("-----send content", content);
                URL url = new URL(url_str);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Charset", "UTF-8");
                //connection.setRequestProperty("accept-encoding", "gzip");
                connection.setRequestProperty("referer", "https://servicewechat.com/wx3b6d85db7f8fb428/4/page-frame.html");
                connection.setRequestProperty("authorization", authorization);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("user-Agent", user_agent);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("host", "open.hcmcloud.cn");
                //connection.setRequestProperty("content-length", "129");
                connection.setRequestProperty("cache-control", "no-cache");

                os = connection.getOutputStream();
                os.write(content.getBytes());
                os.flush();
                os.close();
                // 定义BufferedReader输入流来读取URL的响应
                Log.i("-----send", "end");
                int code = connection.getResponseCode();
                if (code == 200) {
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

                    StringBuffer buffer = new StringBuffer();
                    String line ;
                    while ((line = reader.readLine())!= null){
                        buffer.append(line);
                    }
                    String result_utf8 = buffer.toString();
                    Log.i("-----send Flag", result_utf8);
                    String return_contactor = "";
                    try {
                        JSONObject jsonObj = new JSONObject(result_utf8);
                        JSONObject punchinresultObj = jsonObj.getJSONObject("result");
                        String success_flag = punchinresultObj.getString("success");
                        Log.e(TAG, "success_flag: " + success_flag);
                        JSONObject punchindataObj = punchinresultObj.getJSONObject("data");
                        Log.e(TAG, "punchindataObj: " + punchindataObj);
                        // Getting JSON Array node
                        JSONArray punchin = punchindataObj.getJSONArray("signin");
                        Log.e(TAG, "punchin_flag: " + punchin);

                        // looping through All Contacts
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
                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    return  return_contactor;

                } else {
                    Log.i("-----send Code", Integer.toString(code));
                    Log.i("-----send Flag", "数据提交失败");
                    return "数据提交失败:" + Integer.toString(code);
                    /*
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

                    StringBuffer buffer = new StringBuffer();
                    String line ;
                    while ((line = reader.readLine())!= null){
                        buffer.append(line);
                    }
                    Log.i("-----send Flag", buffer.toString());
                    return  buffer.toString();
                    */
                }
            } catch (SocketTimeoutException e) {
                Log.i("-----send Error", "连接时间超时");
                e.printStackTrace();
                return "连接时间超时";

            } catch (MalformedURLException e) {
                Log.i("-----send Error", "Malformed");
                e.printStackTrace();

            } catch (ProtocolException e) {
                Log.i("-----send Error", "Protocol");
                e.printStackTrace();

            } catch (IOException e) {
                Log.i("-----send Error", "IO");
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("-----send Error", "JSON");
            } catch (Exception e) {
                Log.i("-----send Error", "Other" + e.toString());
                e.printStackTrace();
            } finally {// 使用finally块来关闭输出流、输入流
                try {
                    if (connection!= null) {
                        connection.disconnect();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }

        public String HCM_GeoCheck_OKHttp(String authorization, String longitude, String latitude){
            String url_str = "https://open.hcmcloud.cn/api/attend.signin.geocheck";
            String name = "hcm cloud";
            String accuracy = "0";
            //String latitude = "31.260886";
            //String longitude = "121.62253";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();
            List<String> hash_text_list = new ArrayList<>();
            hash_text_list.add(latitude);
            hash_text_list.add(longitude);
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
            RequestBody body = RequestBody.create(mediaType, "{\"latitude\":\"" + latitude + "\",\"longitude\":\"" + longitude + "\",\"accuracy\":0,\"timestamp\":" + timestamp + ",\"hash\":\"" + hash_text + "\"}");
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

                    JSONObject jsonLocationObj = jsonCheckresultObj.getJSONObject("location");

                    String address = jsonLocationObj.getString("address");
                    String distance = jsonLocationObj.getString("distance");
                    HashMap<String, String> punch_item = new HashMap<>();

                    // adding each child node to HashMap key => value
                    punch_item.put("item", "状态OK");
                    punch_item.put("note", "目标打卡点:" + address + " | 当前距离打卡点:"+ distance + "米");
                    resultList.add(punch_item);

                    return_contact = check_flag + "|打卡地点:" + address + "| 距离目标:"+ distance + "米";
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
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
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
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
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, resultList, R.layout.hcm_list_item, new String[]{ "item","note"}, new int[]{R.id.item, R.id.note});
            lv.setAdapter(adapter);
        }
    }

}
