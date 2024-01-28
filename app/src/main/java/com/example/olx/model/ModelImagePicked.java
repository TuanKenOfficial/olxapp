package com.example.olx.model;

import android.net.Uri;

public class ModelImagePicked {
    private String id;

    private Uri imageUri = null;

    private String imageUrl = null;

    private Boolean fromInternet = false;

    public ModelImagePicked() {
    }

    public ModelImagePicked(String id, Uri imageUri, String imageUrl, Boolean fromInternet) {
        this.id = id;
        this.imageUri = imageUri;
        this.imageUrl = imageUrl;
        this.fromInternet = fromInternet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getImageUri() {
        return imageUri;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getFromInternet() {
        return fromInternet;
    }

    public void setFromInternet(Boolean fromInternet) {
        this.fromInternet = fromInternet;
    }

}
