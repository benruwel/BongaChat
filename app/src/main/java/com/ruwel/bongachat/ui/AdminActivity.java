package com.ruwel.bongachat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruwel.bongachat.R;
import com.ruwel.bongachat.models.FirebaseCloudMessage;
import com.ruwel.bongachat.utils.FCMApi;
import com.ruwel.bongachat.utils.FCMClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class AdminActivity extends AppCompatActivity {

    private String mServerKey;
    private static final String TAG = "AdminActivity";
    //use Set object because we can't have duplicate message tokens
    Set<String> mTokens;
    Set<String> mUserIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //testing
        getServerKey();
    }

    //Now that is the admin activity, we can query db for some-what risky data
    private void getServerKey() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_server)).orderByValue();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mServerKey = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessageToGroup(String title, String message) {
        Log.d(TAG, "sendMessageToGroup : sending message");
        //attach the headers for the http req using maps
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key="+mServerKey);

        FCMApi client = FCMClient.getClient();
//        Call<ResponseBody> call = client.send(headers, )
    }

    private void getMessageTokens() {
        mTokens = new HashSet<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference

    }
    private void getUserIds() {
        mUserIds = new HashSet<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_users))
                .orderByKey();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    mUserIds.add(snapshot1.getKey().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}