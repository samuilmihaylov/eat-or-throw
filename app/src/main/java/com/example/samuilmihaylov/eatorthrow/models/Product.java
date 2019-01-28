package com.example.samuilmihaylov.eatorthrow.models;

public class Product {

    private String productName;
    private String productCategory;
    private String purchaseDate;
    private String expiryDate;
    private String additionalNote;

    public Product(String productName, String productCategory, String purchaseDate, String expiryDate, String additionalNote) {
        this.productName = productName;
        this.productCategory = productCategory;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.additionalNote = additionalNote;
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
}
