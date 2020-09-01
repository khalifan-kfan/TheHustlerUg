package com.example.thehustler.NotifyHandler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.icu.text.CaseMap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.thehustler.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessageService extends FirebaseMessagingService {
    public static final String id =" com.example.thehustler.NotifyHandler.HUSTLER";
    String  channel_name="HUSTLER";
    String title,message;
    @Override
    public void onCreate() {
        super.onCreate();
        createchannel();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        title = remoteMessage.getData().get("Tittle");
        message = remoteMessage.getData().get("Message");

       NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),id)
                .setSmallIcon(R.drawable.ic_msg)
                .setContentText(message)
                .setContentTitle(title).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0,builder.build());

    }
    private  void  createchannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(id,channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications from the hustler");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
    }
}
