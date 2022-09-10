package com.example.phoenix4;

import android.annotation.SuppressLint;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    public static final String channelId = "notification_channel";
    public static final String channelName = "com.example.phoenix4";

    // generate notification
    // attach the notification created with custom layout
    // show the notification

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification ( ) != null) {
            MyApplication.generateNotification ( getApplicationContext ( ), remoteMessage.getNotification ( ).getTitle ( ), remoteMessage.getNotification ( ).getBody ( ) );
        }
    }

    public static RemoteViews getRemoteView(String title, String description) {
        RemoteViews remoteViews = new RemoteViews ( "com.example.phoenix4", R.layout.notification_collapsed );
        remoteViews.setTextViewText ( R.id.textViewTitle, title );
        remoteViews.setTextViewText ( R.id.textViewDescription, description );
        remoteViews.setImageViewResource ( R.id.imageViewNotification, R.drawable.app_icon );
        return remoteViews;
    }
}
