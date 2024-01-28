package com.example.olx.model;

public class ModelAddProduct {
    private String id;
    private String uid;
    private String address;
    private String brand;
    private String category;
    private String condition;
    private String description;
    private double latitude;
    private double longitude;
    private int price;
    private int quantity;
    private int reducedprice;
    private int raito;
    private String status;
    private long timestamp;
    private String title;

    private boolean discount;
    private boolean favorite;

    public ModelAddProduct() {
    }

    public ModelAddProduct(String id, String uid, String address, String brand, String category, String condition, String description, double latitude, double longitude, int price, int quantity, int reducedprice, int raito, String status, long timestamp, String title, boolean discount, boolean favorite) {
        this.id = id;
        this.uid = uid;
        this.address = address;
        this.brand = brand;
        this.category = category;
        this.condition = condition;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
        this.quantity = quantity;
        this.reducedprice = reducedprice;
        this.raito = raito;
        this.status = status;
        this.timestamp = timestamp;
        this.title = title;
        this.discount = discount;
        this.favorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReducedprice() {
        return reducedprice;
    }

    public void setReducedprice(int reducedprice) {
        this.reducedprice = reducedprice;
    }

    public int getRaito() {
        return raito;
    }

    public void setRaito(int raito) {
        this.raito = raito;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }


    public boolean isDiscount() {
        return discount;
    }

    public void setDiscount(boolean discount) {
        this.discount = discount;
    }
}

