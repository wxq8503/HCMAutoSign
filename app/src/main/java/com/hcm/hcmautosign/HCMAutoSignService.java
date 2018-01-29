package com.hcm.hcmautosign;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class HCMAutoSignService extends Service {

    Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setAlarm(this);
        Toast.makeText(this, "Auto HCM Service Started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy() {
        Intent intent_in = new Intent(this, alarm.getClass());
        intent_in.setAction("com.hcm.hcmautosign.CLOCK_IN");//自定义的执行定义任务的Action
        alarm.cancelAlarm(this, intent_in);

        Intent intent_out = new Intent(this, alarm.getClass());
        intent_out.setAction("com.hcm.hcmautosign.CLOCK_OUT");//自定义的执行定义任务的Action
        alarm.cancelAlarm(this, intent_out);
        Toast.makeText(this, "Auto HCM Service Canceled", Toast.LENGTH_SHORT).show();
    }
}