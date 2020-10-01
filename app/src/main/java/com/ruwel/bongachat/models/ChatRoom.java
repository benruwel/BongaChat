package com.ruwel.bongachat.models;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class ChatRoom {

    public String chatroom_name;
    public String creator_id;
    public String security_level;
    public String chatroom_id;
    public List<ChatMessage> chatroom_messages;
    public List<String> users;

    public ChatRoom(String chatroom_name, String creator_id, String security_level, String chatroom_id, List<ChatMessage> chatroom_messages, List<String> users) {
        this.chatroom_name = chatroom_name;
        this.creator_id = creator_id;
        this.security_level = security_level;
        this.chatroom_id = chatroom_id;
        this.chatroom_messages = chatroom_messages;
        this.users = users;
    }

    public ChatRoom() {
    }

    public String getChatroom_name() {
        return chatroom_name;
    }

    public void setChatroom_name(String chatroom_name) {
        this.chatroom_name = chatroom_name;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getSecurity_level() {
        return security_level;
    }

    public void setSecurity_level(String security_level) {
        this.security_level = security_level;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public List<ChatMessage> getChatroom_messages() {
        return chatroom_messages;
    }

    public void setChatroom_messages(List<ChatMessage> chatroom_messages) {
        this.chatroom_messages = chatroom_messages;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
