package com.ruwel.bongachat.utils;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Parcel;
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
import com.ruwel.bongachat.models.ChatRoom;
import com.ruwel.bongachat.ui.AdminActivity;
import com.ruwel.bongachat.ui.LoginActivity;
import com.ruwel.bongachat.ui.MainActivity;
import com.ruwel.bongachat.ui.SignupActivity;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    private int mNumPendingMessages;

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
                            ChatRoom chatRoom = new ChatRoom();
                            Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                            chatRoom.setChatroom_id(objectMap.get(getString(R.string.field_chatroom_id)).toString());
                            chatRoom.setChatroom_name(objectMap.get(getString(R.string.field_chatroom_name)).toString());
                            chatRoom.setCreator_id(objectMap.get(getString(R.string.field_creator_id)).toString());
                            chatRoom.setSecurity_level(objectMap.get(getString(R.string.field_security_level)).toString());

                            Log.d(TAG, "onDataChange: chatroom - " + chatRoom );
                            //find the number of messages seen by the user
                            int numMessageSeen = Integer.parseInt(dataSnapshot
                                    .child(getString(R.string.field_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.field_last_message_seen))
                                    .getValue().toString());
                            int numMessages = (int) snapshot.child(getString(R.string.field_chatroom_messages)).getChildrenCount();
                            mNumPendingMessages = (numMessages - numMessageSeen);
                            Log.d(TAG, "onDataChange: num of pending messages = " + mNumPendingMessages);

                            sendChatMessageNotification(title, message, chatRoom);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
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

    private void sendChatMessageNotification(String title, String message, ChatRoom chatRoom) {
        //get notification id
        int notificationId = buildNotificationId(chatRoom.getChatroom_id());
        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_name));
        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(this, MainActivity.class);
        // Sets the Activity to start in a new, empty task
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent.putExtra(getString(R.string.intent_chatroom), Parcels.wrap(chatRoom));
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.bonga_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.bonga_logo))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText("New messages in " + chatRoom.getChatroom_name())
                .setColor(getResources().getColor(R.color.gray))
                .setAutoCancel(true)
                .setSubText(message) //on squished state
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("New messages in " + chatRoom.getChatroom_name()).setSummaryText(message))
                .setNumber(mNumPendingMessages)
                .setOnlyAlertOnce(true);

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationId, builder.build());

    }

    private int buildNotificationId(String id){
        Log.d(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(TAG, "buildNotificationId: id: " + id);
        Log.d(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }


}
