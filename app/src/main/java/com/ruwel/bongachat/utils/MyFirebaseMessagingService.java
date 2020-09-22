package com.ruwel.bongachat.utils;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ruwel.bongachat.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

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
}
