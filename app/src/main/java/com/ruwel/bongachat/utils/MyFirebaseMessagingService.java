package com.ruwel.bongachat.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ruwel.bongachat.R;
import com.ruwel.bongachat.ui.AdminActivity;
import com.ruwel.bongachat.ui.LoginActivity;
import com.ruwel.bongachat.ui.MainActivity;
import com.ruwel.bongachat.ui.SignupActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final int BROADCAST_NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";
        try{
            notificationData = remoteMessage.getData().toString();
            notificationBody = remoteMessage.getNotification().getBody();
            notificationTitle = remoteMessage.getNotification().getTitle();
        } catch (NullPointerException e){
            Log.e(TAG, "onMessageReceived: NulllPointerException:" + e.getMessage());
        }

        Log.d(TAG, "onMessageReceived : data" + notificationData);
        Log.d(TAG, "onMessageReceived : body" + notificationBody);
        Log.d(TAG, "onMessageReceived : title" + notificationTitle);

        Object object;
        String identifyDataType = remoteMessage.getData().get(getString(R.string.data_type));
        //SITUATION : app is in the foreground, Amdin broadcast in more important
        if(isApplicationInForeground()) {
            if(identifyDataType.equals(getString(R.string.data_type_admin_broadcast))) {
                //build admin broadcast notification
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                sendBroadcastNotification(title, message);
            }
        }
        //SITUATION : app is in the background
        else if(!isApplicationInForeground()) {
            if(identifyDataType.equals(getString(R.string.data_type_admin_broadcast))) {
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                sendBroadcastNotification(title, message);
            }
            else if(identifyDataType.equals(getString(R.string.data_type_chat_message))) {
            }
                final String title = remoteMessage.getData().get(getString(R.string.data_title));
                final String message = remoteMessage.getData().get(getString(R.string.data_message));
                final String chatRoomId = remoteMessage.getData().get(getString(R.string.data_chatroom_id));

            Log.d(TAG, "onMessageReceived: chatroom id = " + chatRoomId);
            Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_chatrooms))
                    .orderByKey()
                    .equalTo(chatRoomId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getChildren().iterator().hasNext()) {
                        DataSnapshot dataSnapshot = snapshot.getChildren().iterator().next();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    //FCM registration token, for individual notifications
    @Override
    public void onNewToken(@NonNull String s) {
        //the user's token is refreshed periodically, this method captures the new token
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        //send token to server
                        sendRegistrationToServer(token);
                    }
                });
        // [END retrieve_current_token]
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server : " + refreshedToken);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(refreshedToken);
    }

    private boolean isApplicationInForeground() {
        //check all activities to see if they are running
        boolean isActivityRunning = AdminActivity.isActivityRunning || LoginActivity.isActivityRunning
                || MainActivity.isActivityRunning || SignupActivity.isActivityRunning;

        return isActivityRunning;
    }


    private void sendBroadcastNotification(String title, String message){
        Log.d(TAG, "sendBroadcastNotification: building a admin broadcast notification");
        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_name));
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent(this, LoginActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.bonga_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.bonga_logo))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(message)
                .setColor(getResources().getColor(R.color.gray))
                .setAutoCancel(true);

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(BROADCAST_NOTIFICATION_ID, builder.build());

    }


}
