package com.example.olx.model;

public class ModelOrderUser {
    private String orderId;
    private long orderMaHD,timestamp;
    private int orderTongTien;
    private String orderBy,orderStatus,orderTo,address;

    public ModelOrderUser() {
    }

    public ModelOrderUser(String orderId, long orderMaHD, long timestamp, int orderTongTien, String orderBy, String orderStatus, String orderTo, String address) {
        this.orderId = orderId;
        this.orderMaHD = orderMaHD;
        this.timestamp = timestamp;
        this.orderTongTien = orderTongTien;
        this.orderBy = orderBy;
        this.orderStatus = orderStatus;
        this.orderTo = orderTo;
        this.address = address;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getOrderMaHD() {
        return orderMaHD;
    }

    public void setOrderMaHD(long orderMaHD) {
        this.orderMaHD = orderMaHD;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getOrderTongTien() {
        return orderTongTien;
    }

    public void setOrderTongTien(int orderTongTien) {
        this.orderTongTien = orderTongTien;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
