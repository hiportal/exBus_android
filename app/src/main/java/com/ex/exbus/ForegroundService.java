package com.ex.exbus;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {
    final static String TAG = "ForegroundService";
    private Thread mThread;

    public ForegroundService(){
        
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*if(intent == null){
            return Service.START_REDELIVER_INTENT; // 서비스 종료 시 자동 재시작
        }else{
            startForegroundService();
        }
        return super.onStartCommand(intent, flags, startId);*/
        if(intent.getAction().equals("startForeground")){
            startForegroundService();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startForegroundService(){
        int nId = 1;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"com.ex.exbus");
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("exbus");
        builder.setContentText("실행중");
        Intent notificationIntent = new Intent(this,MainActivity.class);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(new NotificationChannel("com.ex.exbus", "exbus", NotificationManager.IMPORTANCE_DEFAULT));
        }
        startForeground(nId,builder.build());
    }

}
