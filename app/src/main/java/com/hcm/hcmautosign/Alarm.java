package com.hcm.hcmautosign;

/**
 * Created by weia on 2018/1/22.
 * *https://stackoverflow.com/questions/4459058/alarm-manager-example
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

public class Alarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        //HCMActivity outer = new HCMActivity();
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //String action = preferences.getString("KEY_HCM_FUNCTION_LIST", "GeoCheck");
        //Log.i("-----Receive Broadcast", action);
        //outer.new JSONTask().execute(action);
        context.sendBroadcast(new Intent("HCM_CLOUD"));
        wl.release();
    }

    public void setAlarm(Context context)
    {

        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String clock_in;
        clock_in = preferences.getString("timePrefClockIn_Key", "8:35");
        String clock_out;
        clock_out = preferences.getString("timePrefClockOut_Key", "17:25");

        Log.i("-----send time clock in", clock_in);
        Log.i("-----send time clockout", clock_out);

        // Set the alarm to start at 8:30 a.m.
        Integer clock_in_hour = Integer.parseInt(clock_in.split(":")[0]);
        Integer clock_in_minute = Integer.parseInt(clock_in.split(":")[1]);

        Log.i("-----send time in hour", String.valueOf(clock_in_hour));
        Log.i("-----send time in mint", String.valueOf(clock_in_minute));

        Calendar calendar_in = Calendar.getInstance();
        calendar_in.setTimeInMillis(System.currentTimeMillis());
        calendar_in.set(Calendar.HOUR_OF_DAY, clock_in_hour);
        calendar_in.set(Calendar.MINUTE, clock_in_minute);

        // Set the alarm to start at 8:30 a.m.
        Integer clock_out_hour = Integer.parseInt(clock_out.split(":")[0]);
        Integer clock_out_minute = Integer.parseInt(clock_out.split(":")[1]);
        Log.i("-----send time out hour", String.valueOf(clock_out_hour));
        Log.i("-----send time out mint", String.valueOf(clock_out_minute));

        Calendar calendar_out = Calendar.getInstance();
        calendar_out.setTimeInMillis(System.currentTimeMillis());
        calendar_out.set(Calendar.HOUR_OF_DAY, clock_out_hour);
        calendar_out.set(Calendar.MINUTE, clock_out_minute);


        Intent intent_in = new Intent(context, Alarm.class);
        intent_in.setAction("CLOCK_IN");//自定义的执行定义任务的Action
        cancelAlarm(context, intent_in);
        PendingIntent pi_in = PendingIntent.getBroadcast(context, 0, intent_in, 0);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi_in); // Millisec * Second * Minute
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pi_in); // Millisec * Second * Minute
        Log.i("-----Set Alarm", intent_in.getAction());
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar_in.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi_in);

        Intent intent_out = new Intent(context, Alarm.class);
        intent_out.setAction("CLOCK_OUT");//自定义的执行定义任务的Action
        cancelAlarm(context, intent_out);
        PendingIntent pi_out = PendingIntent.getBroadcast(context, 0, intent_out, 0);
        Log.i("-----Set Alarm", intent_out.getAction());
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar_out.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi_out);

        boolean clock_out_b = (PendingIntent.getBroadcast(context, 0,
                new Intent("CLOCK_OUT"),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (clock_out_b)
        {
            Log.d("myTag", "CLOCK_OUT Alarm is already active");
        }
        boolean clock_in_b = (PendingIntent.getBroadcast(context, 0,
                new Intent("CLOCK_IN"),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (clock_in_b)
        {
            Log.d("myTag", "CLOCK_IN Alarm is already active");
        }

    }

    public void cancelAlarm(Context context, Intent intent)
    {
        try {
            //Intent intent = new Intent(context, Alarm.class);
            Log.i("-----Cancel Alarm", intent.getAction());
            PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(sender);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelAlarmIfExists(Context mContext,int requestCode,Intent intent){
        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent,0);
            AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.cancel(pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}