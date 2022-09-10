package com.chowdhuryelab.addressbook;

public class ModelAddresses {
    private String name, email, phn1, phn2, address, profileImage, timestamp, uid;

    public ModelAddresses() {
    }

    public ModelAddresses(String name, String email, String phn1, String phn2, String address, String profileImage, String timestamp, String uid) {
        this.name = name;
        this.email = email;
        this.phn1 = phn1;
        this.phn2 = phn2;
        this.address = address;
        this.profileImage = profileImage;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhn1() {
        return phn1;
    }

    public void setPhn1(String phn1) {
        this.phn1 = phn1;
    }

    public String getPhn2() {
        return phn2;
    }

    public void setPhn2(String phn2) {
        this.phn2 = phn2;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}