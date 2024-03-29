package com.example.olx.model;

public class ModelCart {
    private int id;
    private String productAdsId;
    private String ten;
    private int price;
    private int soluongdadat;
    private int tongtien;
    private String uidNguoiBan;
    private String uidNguoiMua;


    public ModelCart() {
    }

    public ModelCart(int id, String productAdsId, String ten, int price, int soluongdadat, int tongtien, String uidNguoiBan, String uidNguoiMua) {
        this.id = id;
        this.productAdsId = productAdsId;
        this.ten = ten;
        this.price = price;
        this.soluongdadat = soluongdadat;
        this.tongtien = tongtien;
        this.uidNguoiBan = uidNguoiBan;
        this.uidNguoiMua = uidNguoiMua;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductAdsId() {
        return productAdsId;
    }

    public void setProductAdsId(String productAdsId) {
        this.productAdsId = productAdsId;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSoluongdadat() {
        return soluongdadat;
    }

    public void setSoluongdadat(int soluongdadat) {
        this.soluongdadat = soluongdadat;
    }

    public int getTongtien() {
        return tongtien;
    }

    public void setTongtien(int tongtien) {
        this.tongtien = tongtien;
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
