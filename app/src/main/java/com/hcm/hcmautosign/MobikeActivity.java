package com.hcm.hcmautosign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MobikeActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mTextStatus;
    private Button btnSearch;
    private Button btnReserve;
    private Button btnCancel;

    private String action;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String TAG = MainActivity.class.getSimpleName();
    ArrayList<HashMap<String, String>> contactList;
    private ListView lv;

    @Override
    public void onClick(View v) {
        // handle click
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = preferences.edit();

        String longitude;
        longitude = preferences.getString("KEY_BIKE_LONGITUDE", "121.641999");
        String latitude;
        latitude = preferences.getString("KEY_BIKE_LATITUDE", "31.322225");
        String bike_type = preferences.getString("KEY_BIKE_TYPE", "0");
        String search_scope = preferences.getString("KEY_BIKE_SCOPE", "200");
        String bike_id = preferences.getString("KEY_BIKE_ID", "N/A");
        String nearest_bike_id = preferences.getString("KEY_NEAREST_BIKE_ID", "N/A");
        String nearest_bike_type = preferences.getString("KEY_NEAREST_BIKE_TYPE", "N/A");

        //latitude = "31.260866";
        //longitude = "121.622440";
        switch (v.getId()) {
            case  R.id.btnCheck: {
                action = "NearbyBikeCheck";
                new  MobikeTask().execute(action, longitude, latitude, bike_type, search_scope, bike_id);
                check_status();
                break;
            }

            case R.id.btnReserve: {
                action = "Reserve";
                if("N/A".equals(String.valueOf(nearest_bike_id))) {
                    Log.e(TAG, "No Nearest Bike ID Assigned: " );
                }else{
                    //new  MobikeTask().execute(action, longitude, latitude, nearest_bike_type, search_scope, nearest_bike_id);
                    //check_status();
                    Toast.makeText(getApplicationContext(),
                            "Sorry, I'm not ready yet!",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.btnCancel: {
                // do something for button 2 click
                action = "Cancel";
                if("N/A".equals(String.valueOf(bike_id))) {
                    Log.e(TAG, "No Bike ID Reserved: " );
                }else {
                    new MobikeTask().execute(action, longitude, latitude, bike_type, search_scope, bike_id);
                    check_status();
                }
                break;
            }
        }
    }

    public void getMobikeSettingsFragment(View view) {
        Intent launchIntent = new Intent(this, MobikePreferencesActivity.class);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    public void goGPSActivity(View view) {
        Toast.makeText(getApplicationContext(),
                "Something will be added! " ,
                Toast.LENGTH_LONG).show();
        /*
        Intent launchIntent = new Intent(this, LocationActivity.class);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
        */
    }

    public void check_status(){
        btnReserve= findViewById(R.id.btnReserve);
        btnCancel= findViewById(R.id.btnCancel);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String bike_id = preferences.getString("KEY_BIKE_ID", "N/A");
        String nearest_bike_id = preferences.getString("KEY_NEAREST_BIKE_ID", "N/A");
        Log.i("-----Mobike bike_id", bike_id);
        Log.i("-----Mobike nearbikeid", nearest_bike_id);

        if("N/A".equals(String.valueOf(bike_id))) {
            btnCancel.setEnabled(false);
            if("N/A".equals(String.valueOf(nearest_bike_id))) {
                btnReserve.setEnabled(false);
            }else{
                btnReserve.setEnabled(true);
            }
        }else{
            btnCancel.setEnabled(true);
            btnReserve.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobike);
        mTextStatus = findViewById(R.id.txtStatus);

        btnSearch = findViewById(R.id.btnCheck);
        btnReserve= findViewById(R.id.btnReserve);
        btnCancel= findViewById(R.id.btnCancel);

        btnSearch.setOnClickListener(this);
        btnReserve.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        contactList = new ArrayList<>();
        lv = findViewById(R.id.list);
        check_status();
    }

    public class MobikeTask extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String longitude = params[1];
                String latitude = params[2];
                String bike_type = params[3];
                String search_scope = params[4];
                String bike_id = params[5];

                if(params[0].equals("NearbyBikeCheck" )) {
                    Log.i("-----send action", "NearbyBikeCheck");
                    return MoBike_NearbyCheck(longitude, latitude, bike_type, search_scope, bike_id);
                }
                if(params[0].equals("Reserve")) {
                    Log.i("-----send action", "预约最近的车辆");
                    return MoBike_Reserve(longitude, latitude, bike_type, search_scope, bike_id);
                }
                if(params[0].equals("Cancel")) {
                    Log.i("-----send action", "取消预约最近的车辆");
                    return MoBike_Cancel(longitude, latitude, bike_type, search_scope, bike_id);
                }
            } catch (Exception e){
                Log.i("-----send Error", e.toString());
                Log.i("-----send Error", e.getMessage());
            }
            return null;
        }


        public String MoBike_NearbyCheck(String longitude, String latitude, String biketype, String scope, String bikeid){
            String url_str = "http://app.mobike.com/api/nearby/v3/nearbyBikeInfo";

            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();

            Log.i("-----send timestamp", timestamp);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String post_body = "";
            post_body = post_body + "scope=" + scope;
            post_body = post_body + "&sign=997609fa4ad67c97d1cd6d9f3a3fe9e3&client_id=android";
            post_body = post_body + "&biketype=" + biketype;
            post_body = post_body + "&longitude=" + longitude;
            post_body = post_body + "&latitude=" + latitude;
            post_body = post_body + "&userid=1809516087576576328839&bikenum=10";

            RequestBody body = RequestBody.create(mediaType, post_body);
            Log.i("-----send Request Body", "Send Request Body");
            Log.i("-----send Request Body", post_body);
            //DownloadManager.Request request = new DownloadManager.Request.Builder()
            Request request = new Request.Builder()
                    .url(url_str)
                    .post(body)
                    .addHeader("version", "6.6.0")
                    .addHeader("versioncode", "1632")
                    .addHeader("platform", "1")
                    .addHeader("mainsource", "4002")
                    .addHeader("subsource", "8")
                    .addHeader("os", "24")
                    .addHeader("lang", "zh")
                    .addHeader("time", timestamp)
                    .addHeader("country", "0")
                    .addHeader("eption", "203d7")
                    .addHeader("deviceresolution", "720X1360")
                    .addHeader("utctime", timestamp.substring(10))
                    .addHeader("mobileno", "13585799455")
                    .addHeader("accesstoken", "514e67fc9779ec3482e88e5a12a2c550-0")
                    .addHeader("uuid", "f2bec80be3a281b825a7e115a5b4fae9")
                    .addHeader("longitude", longitude)
                    .addHeader("latitude", latitude)
                    //.addHeader("content-length", "156")
                    .addHeader("host", "app.mobike.com")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("user-agent", "okhttp/3.9.1")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();//4.获得返回结果
                //String result = "Test";
                Log.i("-----send Reponse Body", result);

                try {
                    JSONObject jsonObj = new JSONObject(result);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("bike");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String bike_id = c.getString("bikeIds");
                        String distance = c.getString("distance");
                        String bike_type = c.getString("biketype");
                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("bikeid", bike_id + "--" + bike_type);
                        contact.put("distance", "距离 " + distance + "米");
                        if(i==0){
                            editor.putString("KEY_NEAREST_BIKE_ID", bike_id);
                            editor.putString("KEY_NEAREST_BIKE_TYPE", bike_type);
                            editor.commit();
                        }

                        // adding contact to contact list
                        contactList.add(contact);
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
                return unicodeToUtf8(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String MoBike_Reserve(String longitude, String latitude, String biketype, String scope, String bikeid){
            String url_str = "http://app.mobike.com/api/v2/schedu/confirmation.do";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();


            Log.i("-----send timestamp", timestamp);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String post_body = "";
            post_body = post_body + "sign=8e92c1ce1f779bf1daa69d4ddc007e57&client_id=android";
            post_body = post_body + "&biketype=" + biketype;
            post_body = post_body + "&longitude=" + longitude;
            post_body = post_body + "&isactive=1";
            post_body = post_body + "&latitude=" + latitude;
            try {
                post_body = post_body + "&bikeIds=" + URLEncoder.encode(bikeid,"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            post_body = post_body + "&userid=1809516087576576328839";
            Log.i("-----send Request Body", "Send Request Body");
            Log.i("-----send Request Body", post_body);

            RequestBody body = RequestBody.create(mediaType, post_body);
            Request request = new Request.Builder()
                    .url(url_str)
                    .post(body)
                    .addHeader("version", "6.6.0")
                    .addHeader("versioncode", "1632")
                    .addHeader("platform", "1")
                    .addHeader("mainsource", "4002")
                    .addHeader("subsource", "8")
                    .addHeader("os", "24")
                    .addHeader("lang", "zh")
                    .addHeader("time", timestamp)
                    .addHeader("country", "0")
                    .addHeader("eption", "203d7")
                    .addHeader("deviceresolution", "720X1360")
                    .addHeader("utctime", timestamp.substring(10))
                    .addHeader("mobileno", "13585799455")
                    .addHeader("accesstoken", "514e67fc9779ec3482e88e5a12a2c550-0")
                    .addHeader("uuid", "f2bec80be3a281b825a7e115a5b4fae9")
                    .addHeader("longitude", longitude)
                    .addHeader("latitude", latitude)
                   // .addHeader("content-length", "156")
                    .addHeader("host", "app.mobike.com")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("user-agent", "okhttp/3.9.1")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();//4.获得返回结果
                //String result = "Test";
                Log.i("-----send Reponse Body", result);
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String bike_id = jsonObj.getString("bikeId");
                    String distance = jsonObj.getString("biketype");
                    HashMap<String, String> contact = new HashMap<>();

                    // adding each child node to HashMap key => value
                    contact.put("bikeid", bike_id);
                    contact.put("distance", distance);

                    editor.putString("KEY_BIKE_ID", bike_id);
                    editor.commit();
                    contactList.add(contact);

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
                return unicodeToUtf8(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String MoBike_Cancel(String longitude, String latitude, String biketype, String scope, String bikeid){
            String url_str = "http://app.mobike.com/api/v2/schedu/stop.do";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();

            Log.i("-----send timestamp", timestamp);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String post_body = "";
            post_body = post_body + "sign=8e92c1ce1f779bf1daa69d4ddc007e57&client_id=android";
            post_body = post_body + "&userid=1809516087576576328839";
            post_body = post_body + "&bikeid=" + bikeid;
            //"sign=8e92c1ce1f779bf1daa69d4ddc007e57&client_id=android&bikeid=" + bikeid + "&userid=1809516087576576328839"
            RequestBody body = RequestBody.create(mediaType, post_body);
            Log.i("-----send Request Body", "Send Request Body");
            //DownloadManager.Request request = new DownloadManager.Request.Builder()
            Request request = new Request.Builder()
                    .url(url_str)
                    .post(body)
                    .addHeader("version", "6.6.0")
                    .addHeader("versioncode", "1632")
                    .addHeader("platform", "1")
                    .addHeader("mainsource", "4002")
                    .addHeader("subsource", "8")
                    .addHeader("os", "24")
                    .addHeader("lang", "zh")
                    .addHeader("time", timestamp)
                    .addHeader("country", "0")
                    .addHeader("eption", "203d7")
                    .addHeader("deviceresolution", "720X1360")
                    .addHeader("utctime", timestamp.substring(10))
                    .addHeader("mobileno", "13585799455")
                    .addHeader("accesstoken", "514e67fc9779ec3482e88e5a12a2c550-0")
                    .addHeader("uuid", "f2bec80be3a281b825a7e115a5b4fae9")
                    .addHeader("longitude", longitude)
                    .addHeader("latitude", latitude)
                    //.addHeader("content-length", "156")
                    .addHeader("host", "app.mobike.com")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("accept-encoding", "gzip")
                    .addHeader("user-agent", "okhttp/3.9.1")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
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
            //mTextStatus.setText(result);
            ListAdapter adapter = new SimpleAdapter(MobikeActivity.this, contactList,
                    R.layout.mobike_list_item, new String[]{ "bikeid","distance"},
                    new int[]{R.id.bikeid, R.id.distance});
            lv.setAdapter(adapter);
        }
    }

}
