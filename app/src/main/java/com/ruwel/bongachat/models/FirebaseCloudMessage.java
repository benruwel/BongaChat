package com.ruwel.bongachat.models;

import androidx.annotation.NonNull;

public class FirebaseCloudMessage {
    private String to;
    private FCMData data;

    public FirebaseCloudMessage(String to, FCMData data) {
        this.to = to;
        this.data = data;
    }

    public FirebaseCloudMessage() {
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public FCMData getData() {
        return data;
    }

    public void setData(FCMData data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
