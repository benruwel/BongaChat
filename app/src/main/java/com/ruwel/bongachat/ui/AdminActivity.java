package com.ruwel.bongachat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruwel.bongachat.R;
import com.ruwel.bongachat.models.FCMData;
import com.ruwel.bongachat.models.FirebaseCloudMessage;
import com.ruwel.bongachat.utils.FCMApi;
import com.ruwel.bongachat.utils.FCMClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.broadcast_title)
    TextInputLayout mBroadcastTitle;
    @BindView(R.id.broadcast_message)
    TextInputLayout mBroadcastMessage;
    @BindView(R.id.broadcast_button)
    Button mBroadcastButton;

    private String mServerKey;
    private static final String TAG = "AdminActivity";
    //use Set object because we can't have duplicate message tokens
    Set<String> mTokens;
    Set<String> mUserIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);

        mBroadcastButton.setOnClickListener(this);

        //testing
        getServerKey();
        getUserIds();
    }

    @Override
    public void onClick(View view) {
        if (view == mBroadcastButton) {
            String title = mBroadcastTitle.getEditText().getText().toString();
            String message = mBroadcastMessage.getEditText().getText().toString();
            sendMessageToGroup(title, message);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    //Now that is the admin activity, we can query db for some-what risky data
    private void getServerKey() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_server)).child(getString(R.string.field_server_key)).orderByValue();
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
        Log.d(TAG, "sendMessageToGroup: here is the server key - " + mServerKey);

        //send messages to all tokes
        for(String token : mTokens){
            Log.d(TAG, "sendMessageToGroup: sending to token " + token);
            FCMData data = new FCMData();
            data.setMessage(message);
            data.setTitle(title);
            data.setData_type(getString(R.string.data_type_admin_broadcast));
            FirebaseCloudMessage fcMessage = new FirebaseCloudMessage();
            fcMessage.setData(data);
            fcMessage.setTo(token);

            //set up retrofit to send POST reqs
            FCMApi client = FCMClient.getClient();
            Call<ResponseBody> call = client.send(headers, fcMessage);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "onResponse: Server response : " + response.toString());
                    Snackbar.make(mBroadcastButton, "Broadcast sent", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                            .setActionTextColor(getResources().getColor(R.color.gray))
                            .show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, "onFailure: Unable to send message: " + t.getMessage());
                    Snackbar.make(mBroadcastButton, "Something went wrong", Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                            .setActionTextColor(getResources().getColor(R.color.gray))
                            .show();
                }
            });
        }

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
                    String uid = snapshot1.getKey().toString();
                    mUserIds.add(uid);
                    Log.d(TAG, "onDataChange thread value : " + Thread.currentThread().getId());
                }
                if(!mUserIds.isEmpty()) {
                    getMessageTokens();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMessageTokens() {
        mTokens = new HashSet<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "getMessageTokens: testing if we get here");
        for(String uid : mUserIds) {
            Log.d(TAG, "getMessageTokens: here is the uid we want to query " + uid);
            Query query = reference.child(getString(R.string.dbnode_users))
                    .child(uid).child(getString(R.string.field_messaging_token));
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String token = snapshot.getValue().toString();
                    mTokens.add(token);
                    Log.d(TAG, "onDataChange: message tokens " + token );
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onDataChange: getting message tokens produced this error - " + error.getMessage() );
                }
            });
        }
    }
}