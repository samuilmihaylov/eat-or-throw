package com.example.samuilmihaylov.eatorthrow.models;

import java.io.Serializable;

public class Product implements Serializable {

    private String id;
    private String productName;
    private String productCategory;
    private String purchaseDate;
    private String expiryDate;
    private String additionalNote;
    private String productImageUrl;

    public Product() {

    }

    public Product(String id, String productName, String productCategory, String purchaseDate, String expiryDate, String additionalNote, String productImageUrl) {
        this.id = id;
        this.productName = productName;
        this.productCategory = productCategory;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.additionalNote = additionalNote;
        this.productImageUrl = productImageUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getAdditionalNote() {
        return additionalNote;
    }

    public void setAdditionalNote(String additionalNote) {
        this.additionalNote = additionalNote;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
