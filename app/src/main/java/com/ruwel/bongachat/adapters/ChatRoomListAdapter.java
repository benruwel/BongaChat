package com.ruwel.bongachat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ruwel.bongachat.R;

import com.ruwel.bongachat.models.ChatRoom;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ChatRoomViewHolder> {
    private List<ChatRoom> chatRoomList;
    private Context context;

    public ChatRoomListAdapter(List<ChatRoom> chatRoomList, Context context) {
        this.chatRoomList = chatRoomList;
        this.context = context;
    }

    @Override
    public ChatRoomListAdapter.ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_list_item, parent, false);
        ChatRoomViewHolder viewHolder = new ChatRoomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ChatRoomListAdapter.ChatRoomViewHolder holder, int position) {
        holder.bindChatRooms(chatRoomList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    public class ChatRoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;
        @BindView(R.id.time)
        TextView mTime;
        @BindView(R.id.topic)
        TextView mTopic;
        @BindView(R.id.message_summary)
        TextView mMessageSummary;
        @BindView(R.id.unread_messages)
        TextView mUnreadMessages;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
        }

        @Override
        public void onClick(View view) {

        }
        public void bindChatRooms(ChatRoom chatRoom) {
            mTopic.setText(chatRoom.chatroom_name);
            mUnreadMessages.setText(chatRoom.chatroom_messages.size());
        }
    }
}
