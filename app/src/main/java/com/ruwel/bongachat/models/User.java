package com.ruwel.bongachat.models;

public class User {

    String email;
    String name;
    String messaging_token;
    String phone;
    String security_level;

    public User(String email, String name, String messaging_token, String phone, String security_level) {
        this.email = email;
        this.name = name;
        this.messaging_token = messaging_token;
        this.phone = phone;
        this.security_level = security_level;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSecurity_level() {
        return security_level;
    }

    public void setSecurity_level(String security_level) {
        this.security_level = security_level;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", messaging_token='" + messaging_token + '\'' +
                ", phone='" + phone + '\'' +
                ", security_level='" + security_level + '\'' +
                '}';
    }
}
