package com.example.olx.model;

public class ModelCart {
    private int id;
    private String pId;
    private String tenSP;
    private int price;
    private int quantity;
    private int tongtienSP;
    private String uidNguoiBan;
    private String uidNguoiMua;


    public ModelCart() {
    }

    public ModelCart(int id, String pId, String tenSP, int price, int quantity, int tongtienSP, String uidNguoiBan, String uidNguoiMua) {
        this.id = id;
        this.pId = pId;
        this.tenSP = tenSP;
        this.price = price;
        this.quantity = quantity;
        this.tongtienSP = tongtienSP;
        this.uidNguoiBan = uidNguoiBan;
        this.uidNguoiMua = uidNguoiMua;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
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

    public int getTongtienSP() {
        return tongtienSP;
    }

    public void setTongtienSP(int tongtienSP) {
        this.tongtienSP = tongtienSP;
    }

    public String getUidNguoiBan() {
        return uidNguoiBan;
    }

    public void setUidNguoiBan(String uidNguoiBan) {
        this.uidNguoiBan = uidNguoiBan;
    }

    public String getUidNguoiMua() {
        return uidNguoiMua;
    }

    public void setUidNguoiMua(String uidNguoiMua) {
        this.uidNguoiMua = uidNguoiMua;
    }
}
