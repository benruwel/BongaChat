package com.ruwel.bongachat.models;

import org.parceler.Parcel;

@Parcel
public class ChatMessage {

    public String message;
    public String user_id;
    public String timestamp;
    public String profile_image;
    public String name;

    public ChatMessage() {
    }

    public ChatMessage(String message, String user_id, String timestamp, String profile_image, String name) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.profile_image = profile_image;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
