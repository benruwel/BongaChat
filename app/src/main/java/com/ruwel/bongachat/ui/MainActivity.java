package com.ruwel.bongachat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruwel.bongachat.R;
import com.ruwel.bongachat.fragments.NewChatDialog;
import com.ruwel.bongachat.models.ChatMessage;
import com.ruwel.bongachat.models.ChatRoom;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.noChat)
    TextView mNoChat;
    @BindView(R.id.chat_messages_recycler_view)
    RecyclerView mChatsRV;
    @BindView(R.id.new_chat_fab)
    FloatingActionButton mNewChatFAB;

    private int mSecurity_level;
    private boolean adminTruthy;
    private static final String TAG = "MainActivity";
    public static boolean isActivityRunning;
    private ArrayList<ChatRoom> mChatRooms;
    private DatabaseReference mChatRoomReference;
    private HashMap<String, String> mNumChatRoomMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //handle intents of chat room notifications
        getPendingIntent();

        mNewChatFAB.setOnClickListener(this);

        init();
    }

    @Override
    public void onClick(View view) {
        if(view == mNewChatFAB) {
            NewChatDialog dialog = new NewChatDialog();
            dialog.show(getSupportFragmentManager(), "dialog_new_chatroom");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.log_out) {
            Snackbar.make(mNoChat, "Logging out", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                    .setActionTextColor(getResources().getColor(R.color.gray))
                    .show();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            startActivity(intent);
        }
        if(id == R.id.admin) {
            if(mSecurity_level == 10) {
                Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                startActivity(intent);
            } else {
                Snackbar.make(mNoChat, "You don't have admin privilege", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                        .setActionTextColor(getResources().getColor(R.color.gray))
                        .show();
            }
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    public void getChatrooms(){
        Log.d(TAG, "getChatrooms: retrieving chatrooms from firebase database.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        mNumChatRoomMessages = new HashMap<>();
        if(mAdapter != null){
            mAdapter.clear();
            mChatRooms.clear();
        }
        Query query = reference.child(getString(R.string.dbnode_chatrooms)).orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){
//                    Log.d(TAG, "onDataChange: found chatroom: "
//                            + singleSnapshot.getValue());
                    try{
                        if(singleSnapshot.exists()){
                            ChatRoom chatroom = new ChatRoom();
                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                            Log.d(TAG, "onDataChange: found a chatroom: "
                                    + objectMap.get(getString(R.string.field_chatroom_name)).toString());
                            chatroom.setChatroom_id(objectMap.get(getString(R.string.field_chatroom_id)).toString());
                            chatroom.setChatroom_name(objectMap.get(getString(R.string.field_chatroom_name)).toString());
                            chatroom.setCreator_id(objectMap.get(getString(R.string.field_creator_id)).toString());
                            chatroom.setSecurity_level(objectMap.get(getString(R.string.field_security_level)).toString());


                            //                    chatroom.setChatroom_id(singleSnapshot.getValue(Chatroom.class).getChatroom_id());
                            //                    chatroom.setSecurity_level(singleSnapshot.getValue(Chatroom.class).getSecurity_level());
                            //                    chatroom.setCreator_id(singleSnapshot.getValue(Chatroom.class).getCreator_id());
                            //                    chatroom.setChatroom_name(singleSnapshot.getValue(Chatroom.class).getChatroom_name());

                            //get the chatrooms messages
                            ArrayList<ChatMessage> messagesList = new ArrayList<ChatMessage>();
                            int numMessages = 0;
                            for(DataSnapshot snapshot: singleSnapshot
                                    .child(getString(R.string.field_chatroom_messages)).getChildren()){
                                ChatMessage message = new ChatMessage();
                                message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                                message.setUser_id(snapshot.getValue(ChatMessage.class).getUser_id());
                                message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                                messagesList.add(message);
                                numMessages++;
                            }
                            if(messagesList.size() > 0){
                                chatroom.setChatroom_messages(messagesList);

                                //add the number of chatrooms messages to a hashmap for reference
                                mNumChatRoomMessages.put(chatroom.getChatroom_id(), String.valueOf(numMessages));
                            }

                            //get the list of users who have joined the chatroom
                            List<String> users = new ArrayList<String>();
                            for(DataSnapshot snapshot: singleSnapshot
                                    .child(getString(R.string.field_users)).getChildren()){
                                String user_id = snapshot.getKey();
                                Log.d(TAG, "onDataChange: user currently in chatroom: " + user_id);
                                users.add(user_id);
                            }
                            if(users.size() > 0){
                                chatroom.setUsers(users);
                            }

                            mChatRooms.add(chatroom);
                        }

                        setupChatroomList();
                    }catch (NullPointerException e){
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage() );
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void isAdmin() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_security_level)).orderByValue();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mSecurity_level = Integer.parseInt(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPendingIntent() {
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_chatroom))) {
            ChatRoom chatRoom = Parcels.unwrap(intent.getParcelableExtra(getString(R.string.intent_chatroom)));
            Intent chatroomIntent = new Intent(MainActivity.this, ChatRoomActivity.class);
            chatroomIntent.putExtra(getString(R.string.intent_chatroom), Parcels.wrap(chatRoom));
            startActivity(chatroomIntent);
        }
    }

    public void init() {
        mChatRooms = new ArrayList<>();
        //check the user's admin privilege as soon as the activity starts
        isAdmin();
    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Africa/Nairobi"));
        return sdf.format(new Date());
    }
    private void showChatList() {
        mChatsRV.setVisibility(View.VISIBLE);
    }
    private void hideNoChatsText() {
        mNoChat.setVisibility(View.GONE);
    }
}