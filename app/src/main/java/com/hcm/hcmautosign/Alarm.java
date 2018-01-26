package com.hcm.hcmautosign;

/**
 * Created by weia on 2018/1/22.
 * *https://stackoverflow.com/questions/4459058/alarm-manager-example
 */

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getBroadcast;


public class Alarm extends BroadcastReceiver
{
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String TAG = HCMActivity.class.getSimpleName();
    ArrayList<HashMap<String, String>> resultList;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        //HCMActivity outer = new HCMActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        String action = preferences.getString("KEY_HCM_FUNCTION_LIST", "GeoCheck");
        Log.i("-----Receive Broadcast 0", action);
        new JSONTask().execute(action);
        //context.sendBroadcast(new Intent("com.hcm.hcmautosign.HCM_CLOUD"));
        wl.release();
    }

    public void setAlarm(Context context)
    {

        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        String clock_in;
        clock_in = preferences.getString("timePrefClockIn_Key", "8:35");
        String clock_out;
        clock_out = preferences.getString("timePrefClockOut_Key", "17:25");

        Log.i("-----send time clock in", clock_in);
        Log.i("-----send time clockout", clock_out);

        // Set the alarm to start at 8:30 a.m.
        Integer clock_in_hour = Integer.parseInt(clock_in.split(":")[0]);
        Integer clock_in_minute = Integer.parseInt(clock_in.split(":")[1]) + ThreadLocalRandom.current().nextInt(1, 4 + 1);

        Log.i("-----send time in hour", String.valueOf(clock_in_hour));
        Log.i("-----send time in mint", String.valueOf(clock_in_minute));

        long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        Calendar calendar_in = Calendar.getInstance();
        calendar_in.setTimeInMillis(System.currentTimeMillis());
        calendar_in.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar_in.set(Calendar.HOUR_OF_DAY, clock_in_hour);
        calendar_in.set(Calendar.MINUTE, clock_in_minute);
        calendar_in.set(Calendar.SECOND, ThreadLocalRandom.current().nextInt(1, 30 + 1));
        calendar_in.set(Calendar.MILLISECOND, 0);

        // 选择的定时时间
        long selectTime = calendar_in.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            //Toast.makeText(getBaseContext(),"设置的时间小于当前时间", Toast.LENGTH_SHORT).show();
            Log.i("-----Set Alarm 1", "设置的时间小于当前时间");
            calendar_in.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar_in.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;

        Intent intent_in = new Intent(context, Alarm.class);
        intent_in.setAction("com.hcm.hcmautosign.CLOCK_IN");//自定义的执行定义任务的Action
        cancelAlarm(context, intent_in);
        PendingIntent pi_in = PendingIntent.getBroadcast(context, 0, intent_in, FLAG_UPDATE_CURRENT);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi_in); // Millisec * Second * Minute
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pi_in); // Millisec * Second * Minute
        Log.i("-----Set Alarm", intent_in.getAction());
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, AlarmManager.INTERVAL_DAY, pi_in);


        // Set the alarm to start at 8:30 a.m.
        Integer clock_out_hour = Integer.parseInt(clock_out.split(":")[0]);
        Integer clock_out_minute = Integer.parseInt(clock_out.split(":")[1]) + ThreadLocalRandom.current().nextInt(1, 10 + 1);
        Log.i("-----send time out hour", String.valueOf(clock_out_hour));
        Log.i("-----send time out mint", String.valueOf(clock_out_minute));

        Calendar calendar_out = Calendar.getInstance();
        calendar_out.setTimeInMillis(System.currentTimeMillis());
        calendar_out.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar_out.set(Calendar.HOUR_OF_DAY, clock_out_hour);
        calendar_out.set(Calendar.MINUTE, clock_out_minute);
        calendar_out.set(Calendar.SECOND, ThreadLocalRandom.current().nextInt(1, 30 + 1));
        calendar_out.set(Calendar.MILLISECOND, 0);

        long firstTime_out = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        // 选择的定时时间
        systemTime = System.currentTimeMillis();
        long selectTime_out = calendar_out.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime_out) {
            //Toast.makeText(HCMActivity.getContext(),"设置的时间小于当前时间", Toast.LENGTH_SHORT).show();
            Log.i("-----Set Alarm 2", "设置的时间小于当前时间");
            calendar_out.add(Calendar.DAY_OF_MONTH, 1);
            selectTime_out = calendar_out.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time_out = selectTime_out - systemTime;
        firstTime_out += time_out;

        Intent intent_out = new Intent(context, Alarm.class);
        intent_out.setAction("com.hcm.hcmautosign.CLOCK_OUT");//自定义的执行定义任务的Action
        cancelAlarm(context, intent_out);
        PendingIntent pi_out = PendingIntent.getBroadcast(context, 1, intent_out, FLAG_UPDATE_CURRENT);
        Log.i("-----Set Alarm", intent_out.getAction());
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime_out, AlarmManager.INTERVAL_DAY, pi_out);

        /*
        boolean clock_in_b = (PendingIntent.getBroadcast(context, 0,
                new Intent("CLOCK_IN"),
                PendingIntent.FLAG_NO_CREATE) != null);
        if (clock_in_b)
        {
            Log.i("myTag", "CLOCK_IN Alarm is already active");
        }else{
            Log.i("myTag", "CLOCK_IN Alarm not active");
        }

        boolean clock_out_b = (PendingIntent.getBroadcast(context, 1,
                new Intent("CLOCK_OUT"),
                PendingIntent.FLAG_NO_CREATE) != null);
        if (clock_out_b)
        {
            Log.i("myTag", "CLOCK_OUT Alarm is already active");
        }else{
            Log.i("myTag", "CLOCK_OUT Alarm not active");
        }
        */

    }

    public void cancelAlarm(Context context, Intent intent)
    {
        try {
            //Intent intent = new Intent(context, Alarm.class);
            Log.i("-----Cancel Alarm", intent.getAction());
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(sender);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class JSONTask extends AsyncTask<String,String,ArrayList<HashMap<String, String>>> {

        //Alan
        //final  String authorization = "a6d7677286399a22978a1754cb954c4785eec1f5";
        final  String user_agent = "Mozilla/5.0 (Linux; Android 5.1; SM-J5008 Build/LMY47O; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043613 Safari/537.36 MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN";
        //Jeff
        //final  String authorization = "a01d2cf78baf4d1d0b586f060a804857bede2bbf";
        //final  String user_agent = "Mozilla/5.0 (Linux; Android 6.0; 1505-A01 Build/MRA58K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN MicroMessenger/6.5.23.1180 NetType/WIFI Language/zh_CN";

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
            try {
                //String authorization =  params[1].trim();
                //String longitude = params[2].trim();
                //String latitude = params[3].trim();
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                editor.putString("KEY_LAST_ACTION_AND_TIME", params[0] + "|" + currentDateTimeString);
                editor.commit();

                if(params[0].equals("GeoCheck" )) {
                    Log.i("-----send action", "GeoCheck");
                    return HCM_GeoCheck_OKHttp();
                }
                if(params[0].equals("Punchin")) {
                    Log.i("-----send action", "Punchin");
                    //return HCM_Punchin_OKHttp(authorization, longitude, latitude);
                    return HCM_Punchin_OKHttp();
                }
                if(params[0].equals("PunCheck")) {
                    Log.i("-----send action", "PunCheck");
                    return HCM_PunCheck_OKHttp();
                }
            } catch (Exception e){
                Log.i("-----send Error", e.toString());
                Log.i("-----send Error", e.getMessage());
                HashMap<String, String> punch_item = new HashMap<>();

                // adding each child node to HashMap key => value
                punch_item.put("item", "Error Catched");
                punch_item.put("note", "Error");
                resultList.add(punch_item);
                return resultList;
            }
            return null;
        }
/*
        private void addNotification(String showText) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(Alarm.this.getClass(), "Channel ID")
                            .setSmallIcon(R.drawable.settings_ic_hcm)
                            .setContentTitle("HCM 打卡")
                            .setContentText(showText)
                            .setContentInfo("Info");

            Intent notificationIntent = new Intent(Alarm.), Alarm.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }
*/
        public ArrayList<HashMap<String, String>> HCM_PunCheck_OKHttp(){
            String url_str = "https://open.hcmcloud.cn/api/attend.view.employee.day";
            String name = "hcm cloud";
            String accuracy = "0";
            //String latitude = "31.260886";
            //String longitude = "121.62253";

            String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");

            String employee_id = "";
            String date = "";
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();
            List<String> hash_text_list = new ArrayList<>();
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
                    //addNotification("打卡记录检查完成！");
                } catch (final JSONException e) {
                    try{
                        //addNotification("打卡记录检查失败，请检查设置");
                        JSONObject jsonObj = new JSONObject(result);
                        final String errmsg = jsonObj.getString("errmsg");
                        Log.e(TAG, "Error message: " + errmsg);
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Error Message");
                        punch_item.put("note", errmsg);
                        resultList.add(punch_item);

                    }catch ( final JSONException ex) {
                        Log.e(TAG, "Json parsing error 1: " + ex.getMessage());
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Json parsing error_1");
                        punch_item.put("note", ex.getMessage());
                        resultList.add(punch_item);
                    }
                }
                return  resultList;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //public String HCM_GeoCheck_OKHttp(String authorization, String longitude_str, String latitude_str){
        public ArrayList<HashMap<String, String>> HCM_GeoCheck_OKHttp(){
            String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
            String longitude_str;
            longitude_str = preferences.getString("KEY_PUNCHIN_LONGITUDE", "121.622440");
            String latitude_str;
            latitude_str = preferences.getString("KEY_PUNCHIN_LATITUDE", "31.260886");

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
                    //addNotification("位置检查完成");
                } catch (final JSONException e) {
                    try{
                        //addNotification("检查到错误");
                        JSONObject jsonObj = new JSONObject(result);
                        final String errmsg = jsonObj.getString("errmsg");
                        Log.e(TAG, "Error message: " + errmsg);
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Error Message");
                        punch_item.put("note", errmsg);
                        resultList.add(punch_item);

                    }catch ( final JSONException ex) {
                        Log.e(TAG, "Json parsing error 1: " + ex.getMessage());
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Json parsing error_1");
                        punch_item.put("note", ex.getMessage());
                        resultList.add(punch_item);
                    }
                }
                return resultList;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //public String HCM_Punchin_OKHttp(String authorization, String longitude, String latitude){
        public ArrayList<HashMap<String, String>> HCM_Punchin_OKHttp(){
            String authorization = preferences.getString("KEY_AUTH_CODE", "N/A");
            String longitude;
            longitude = preferences.getString("KEY_PUNCHIN_LONGITUDE", "121.622440");
            String latitude;
            latitude = preferences.getString("KEY_PUNCHIN_LATITUDE", "31.260886");

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
                    //addNotification("成功打卡");
                } catch (final JSONException e) {
                    try{
                        //addNotification("打卡失败，请检查设置");
                        JSONObject jsonObj = new JSONObject(result);
                        final String errmsg = jsonObj.getString("errmsg");
                        Log.e(TAG, "Error message: " + errmsg);
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Error Message");
                        punch_item.put("note", errmsg);
                        resultList.add(punch_item);
                    }catch ( final JSONException ex) {
                        Log.e(TAG, "Json parsing error 1: " + ex.getMessage());
                        HashMap<String, String> punch_item = new HashMap<>();
                        punch_item.put("item", "Json parsing error_1");
                        punch_item.put("note", ex.getMessage());
                        resultList.add(punch_item);
                    }
                }
                return resultList;
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
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            super.onPostExecute(result);
            //mTextMessage.setText(result);
            //ListAdapter adapter = new SimpleAdapter(HCMActivity.this, resultList, R.layout.hcm_list_item, new String[]{ "item","note"}, new int[]{R.id.item, R.id.note});
            //lv.setAdapter(adapter);
        }

        @Override
        protected void onPreExecute(){
            // Activity 1 GUI stuff
            super.onPreExecute();
        }


    }


}