package com.hcm.hcmautosign;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView mTextAuth;
    private  String action;
    private SharedPreferences preferences;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            String strAuthorization = mTextAuth.getText().toString();

            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String authorization = SP.getString("KEY_AUTH_CODE", strAuthorization);
            mTextAuth.setText(authorization);

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    action = "GeoCheck";
                    new JSONTask().execute(action, authorization);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    action = "Punchin";
                    new JSONTask().execute(action, authorization);
                    //return HCM_Punchin_OKHttp();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    action = "PunCheck";
                    new JSONTask().execute(action, authorization);
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
        mTextAuth   = findViewById(R.id.txtAuth);

        String strAuthorization = mTextAuth.getText().toString();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String authorization = SP.getString("KEY_AUTH_CODE", strAuthorization);
        mTextAuth.setText(authorization);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void getSettingsFragment(View view) {

        Intent launchIntent = new Intent(this, MyPreferencesActivity.class);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    public void getAuthCode(View view) {
        String strAuthorization = mTextAuth.getText().toString();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String authorization = SP.getString("KEY_AUTH_CODE", strAuthorization);
        mTextAuth.setText(authorization);
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

                if(params[0].equals("GeoCheck" )) {
                    Log.i("-----send action", "GeoCheck");
                    return HCM_GeoCheck(authorization);
                }
                if(params[0].equals("Punchin")) {
                    Log.i("-----send action", "Punchin");
                    //return HCM_Punchin();
                    return HCM_Punchin_OKHttp(authorization);
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
                //connection.setRequestProperty("ser-Agent", "Mozilla/5.0 (Linux; Android 5.1; SM-J5008 Build/LMY47O; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043613 Safari/537.36 MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN");
                //connection.setRequestProperty("Connection", "Keep-Alive");
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
                    Log.i("-----send Flag", buffer.toString());
                    return  unicodeToUtf8(buffer.toString());

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

        public String HCM_GeoCheck(String authorization) {
            BufferedReader reader;
            OutputStream os = null;
            InputStream is = null;
            String url_str = "https://open.hcmcloud.cn/api/attend.signin.geocheck";
            String name = "hcm cloud";
            String accuracy = "0";
            String latitude = "31.260886";
            String longitude = "121.62253";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();

            //timestamp ="1513039572365";
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
            HttpURLConnection connection = null;
            try {
                JSONObject ClientKey = new JSONObject();
                ClientKey.put("accuracy", accuracy);
                ClientKey.put("latitude", latitude);
                ClientKey.put("longitude", longitude);
                ClientKey.put("timestamp", timestamp);
                ClientKey.put("hash", hash_text);
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
                connection.setRequestProperty("ser-Agent", user_agent);
                //connection.setRequestProperty("Connection", "Keep-Alive");
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
                    Log.i("-----send Flag", buffer.toString());
                    return  unicodeToUtf8(buffer.toString());

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
                    return  unicodeToUtf8(buffer.toString());
                   */
                }
            } catch (SocketTimeoutException e) {
                Log.i("-----send Error", "连接时间超时");
                e.printStackTrace();

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

        public String HCM_Punchin_OKHttp(String authorization){
            String url_str = "https://open.hcmcloud.cn/api/attend.signin.create";
            String name = "hcm cloud";
            String location_id = "3218";
            String type = "3";
            String latitude = "31.260866";
            String longitude = "121.622440";
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
            RequestBody body = RequestBody.create(mediaType, "{\"location_id\":3218,\"type\":3,\"latitude\":\"31.260866\",\"longitude\":\"121.622440\",\"beacon\":\"\",\"information\":\"{}\",\"timestamp\":"+  timestamp +  ",\"state\":null,\"hash\":\"" + hash_text + "\"}");
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
                //String result = "Test";
                Log.i("-----send Reponse Body", result);
                return unicodeToUtf8(result);
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
            mTextMessage.setText(result);
        }
    }

}
