package com.ex.exbus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    String tag = "FirebaseFCM ----- ";

    public static String token;

    // kbr 2022.04.27
    @Override
    public void onNewToken(String token) {
        generateToken();
    }

    public static void generateToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        FirebaseMessagingService.token = task.getResult();
                    }
                });
    }

    //메세지 수신
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Log.d(TAG, tag+"onMessageReceived() - from : "+from);
        Log.d(TAG, tag+"onMessageReceived() - remoteMessage == To : "+remoteMessage.getTo());
        Log.d(TAG, tag+"onMessageReceived() - remoteMessage == Data : "+remoteMessage.getData());
        Log.d(TAG, tag+"onMessageReceived() - remoteMessage == Ttl : "+remoteMessage.getTtl());

        if(remoteMessage.getData().size() > 0){
            Log.d(TAG, tag+"onMessageReceived() - Message Data payload : "+remoteMessage.getData());

            if(true){

            }else{
                handleNow();
            }
        }

        if(remoteMessage.getNotification() != null){
            Log.d(TAG, tag+"onMessageReceived() - Message Notification Body : "+remoteMessage.getNotification().getBody());

            sendNotification(remoteMessage.getNotification().getBody());
        }

    }

    private void handleNow(){
        Log.d(TAG, tag+"handleNow() - short lived task is done");
    }

    private void sendNotification(String messageBody){
        Log.d(TAG, tag+"sendNotification() - messageBody : "+messageBody);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "com.ex.exbus";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(channelId, "exbus", NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setDescription("exbus");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            mNotificationManager.createNotificationChannel(mChannel);

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification.Builder(FirebaseMessagingService.this)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("exBUS")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setChannelId(channelId)
                    .build();
            mNotificationManager.notify(0, notification);
            
        }else{
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("exBUS")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            nManager.notify(0, notificationBuilder.build());
        }

    }
}
