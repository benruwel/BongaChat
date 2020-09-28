package com.ruwel.bongachat.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruwel.bongachat.R;
import com.ruwel.bongachat.models.ChatMessage;
import com.ruwel.bongachat.models.ChatRoom;
import com.ruwel.bongachat.ui.ChatRoomActivity;
import com.ruwel.bongachat.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewChatDialog extends DialogFragment implements View.OnClickListener {

    @BindView(R.id.new_chat_button)
    Button mNewChat;
    @BindView(R.id.chatroom_name)
    TextInputLayout mNewChatName;
    @BindView(R.id.security_seek_bar)
    SeekBar mSeekBar;
    @BindView(R.id.security_level)
    TextView mSecurityLevel;
    private int mUserSecurityLevel;
    private int mSeekProgress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_new_chat, container, false);
        ButterKnife.bind(this, view);
        getUserSecurityLevel();
        getDialog().setTitle("New Chat");
        mSeekProgress = 0;
        mSecurityLevel.setText(String.valueOf(mSeekBar.getProgress()));
        mNewChat.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view == mNewChat) {
            boolean validName = isValidName(mNewChatName.getEditText().getText().toString());
            if(validName) {
                if(mUserSecurityLevel >= mSeekBar.getProgress()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    String chatRoomId = reference.child(getString(R.string.dbnode_chatrooms)).push().getKey();
                    //create new chatroom
                    ChatRoom chatRoom = new ChatRoom();
                    chatRoom.setSecurity_level(String.valueOf(mSeekBar.getProgress()));
                    chatRoom.setChatroom_name(mNewChatName.getEditText().getText().toString());
                    chatRoom.setCreator_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    //insert new chatroom into db
                    reference.child(getString(R.string.dbnode_chatrooms)).child(chatRoomId).setValue(chatRoom);
                    //generate unique id for the message
                    String messageId = reference.child(getString(R.string.dbnode_chatrooms)).push().getKey();
                    //insert the first message
                    ChatMessage message = new ChatMessage();
                    message.setMessage("Welcome to BongaChat");
                    message.setTimestamp(getTimestamp());
                    reference.child(getString(R.string.dbnode_chatrooms))
                            .child(chatRoomId)
                            .child(getString(R.string.field_chatroom_messages))
                            .child(messageId)
                            .setValue(message);
                    ((MainActivity)getActivity()).init();
                    getDialog().dismiss();
                } else {
                    Snackbar.make(mNewChat, "Insufficient security level", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getResources().getColor(R.color.gray_dark))
                            .setActionTextColor(getResources().getColor(R.color.gray))
                            .show();
                }
            }
        }
    }

    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 75% of the screen width
        window.setLayout((int) (size.x * 0.75), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        // Call super onResume after sizing
        super.onResume();
    }

    private void getUserSecurityLevel() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_security_level)).orderByValue();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUserSecurityLevel = Integer.parseInt(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNewChatName.setError("Please enter the topic");
            return false;
        }
        return true;
    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Africa/Nairobi"));
        return sdf.format(new Date());
    }

}
