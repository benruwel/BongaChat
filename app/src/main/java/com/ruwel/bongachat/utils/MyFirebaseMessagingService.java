package com.ruwel.bongachat.utils;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";
        try{
            Object object;
            notificationData = remoteMessage.getData().toString();
            notificationBody = remoteMessage.getNotification().getBody();
            notificationTitle = remoteMessage.getNotification().getTitle();
        } catch (NullPointerException e){
            Log.e(TAG, "onMessageReceived: NulllPointerException:" + e.getMessage());
        }

        Log.d(TAG, "onMessageReceived : data" + notificationData);
        Log.d(TAG, "onMessageReceived : body" + notificationBody);
        Log.d(TAG, "onMessageReceived : title" + notificationTitle);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
