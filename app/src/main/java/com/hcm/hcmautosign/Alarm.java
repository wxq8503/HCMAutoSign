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
import android.widget.Toast;

import java.util.Calendar;

public class Alarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        Log.e("----Received", "Alarm");
        String action =  intent.getAction();

        switch (action) {
            case "Clock in":
                doClockInAction(context);
                break;
            case "Clock out":
                doClockOutAction(context);
            break;
        }
        // Put here YOUR code.
        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

        wl.release();
    }

    private void doClockInAction(Context context) {
        Toast.makeText(context, "Clock In !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
    }

    private void doClockOutAction(Context context) {
        Toast.makeText(context, "Clock !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
    }

    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute

        // Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        SharedPreferences preferences = context.getSharedPreferences("pref_setting_hcm",Context.MODE_PRIVATE);
        String authorization = preferences.getString("timePrefClockIn_Key", "N/A");
        Log.e("----Received", "Alarm" + authorization);

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 00);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pi); // Millisec * Second * Minute
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}