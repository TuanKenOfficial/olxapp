package com.example.olx.model;

public class ModelOrderSeller {
    private String sanpham;
    private String orderId, productId;
    private long orderMaHD,timestamp;
    private int orderTongTien, soluong;
    private String orderBy,orderTo,address,orderStatus;
    private double latitude,longitude;

    public ModelOrderSeller() {
    }

    public ModelOrderSeller(String sanpham, String orderId, String productId, long orderMaHD, long timestamp, int orderTongTien, int soluong, String orderBy, String orderTo, String address, String orderStatus, double latitude, double longitude) {
        this.sanpham = sanpham;
        this.orderId = orderId;
        this.productId = productId;
        this.orderMaHD = orderMaHD;
        this.timestamp = timestamp;
        this.orderTongTien = orderTongTien;
        this.soluong = soluong;
        this.orderBy = orderBy;
        this.orderTo = orderTo;
        this.address = address;
        this.orderStatus = orderStatus;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSanpham() {
        return sanpham;
    }

    public void setSanpham(String sanpham) {
        this.sanpham = sanpham;
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

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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
}
