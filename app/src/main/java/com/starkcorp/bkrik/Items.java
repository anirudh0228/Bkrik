package com.starkcorp.bkrik;

/**
 * Created by anirudh on 6/16/2017.
 */
public class Items {
    private String product;
    private String shop;
    private String ratings;
    private String imageurl;
    private String price;
    private String unit;
    private String Quantity;
    private String UserLocation;
    private String Description;
    private String DeliveryStatus;//3 status //request send //on the way //delivered
    private String StatusInStore;

    public String getStatusInStore() {
        return StatusInStore;
    }

    public void setStatusInStore(String statusInStore) {
        StatusInStore = statusInStore;
    }

    public Items(String product, String shop, String ratings, String price, String unit,String imageurl) {
        this.product = product;
        this.shop = shop;
        this.imageurl=imageurl;
        this.ratings = ratings;
        //this.imageuri = imageuri;
        this.price = price;
        this.unit = unit;
    }
    public Items(String product, String shop, String ratings, String price, String unit) {
        this.product = product;
        this.shop = shop;
        this.ratings = ratings;
        //this.imageuri = imageuri;
        this.price = price;
        this.unit = unit;
    }



    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getUserLocation() {
        return UserLocation;
    }

    public void setUserLocation(String userLocation) {
        UserLocation = userLocation;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDeliveryStatus() {
        return DeliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        DeliveryStatus = deliveryStatus;
    }
}
