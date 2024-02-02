package com.example.olx.model;

public class ModelUsers {
    private String email;
    private String name;
    private String dob;
    private String password;
    private String accountType;
    private String address;
    private double latitude;
    private double longitude;
    private String uid;
    private String phoneCode;
    private String phone;
    private String phoneNumber;
    private String profileImageUrl;
    private String shopName;
    private String timestamp;

    private String online;
    private String shopOpen;

    public ModelUsers() {
    }

    public ModelUsers(String email, String name, String dob, String password, String accountType, String address, double latitude, double longitude, String uid, String phoneCode, String phone, String phoneNumber, String profileImageUrl, String shopName, String timestamp, String online, String shopOpen) {
        this.email = email;
        this.name = name;
        this.dob = dob;
        this.password = password;
        this.accountType = accountType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uid = uid;
        this.phoneCode = phoneCode;
        this.phone = phone;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.shopName = shopName;
        this.timestamp = timestamp;
        this.online = online;
        this.shopOpen = shopOpen;
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getShopOpen() {
        return shopOpen;
    }

    public void setShopOpen(String shopOpen) {
        this.shopOpen = shopOpen;
    }
}
