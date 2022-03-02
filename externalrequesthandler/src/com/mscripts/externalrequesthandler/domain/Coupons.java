/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.domain;

/**
 *
 * @author ppushkar
 */
public class Coupons {

    private String couponID;
    private String batchID;
    private String category;
    private String subCategory;
    private String brand;
    private String productName;
    private String adCaption;
    private String adDesc;
    private String barcodeImageUrl;
    private String adImageUrl;
    private String adThumbnailUrl;
    private String stores;
    private String zipCodes;
    private String validFrom;
    private String validTo;
    private String priority;
    private String price;

    public String getAdCaption() {
        return adCaption;
    }

    public void setAdCaption(String adCaption) {
        this.adCaption = adCaption;
    }

    public String getAdDesc() {
        return adDesc;
    }

    public void setAdDesc(String adDesc) {
        this.adDesc = adDesc;
    }

    public String getAdImageUrl() {
        return adImageUrl;
    }

    public void setAdImageUrl(String adImageUrl) {
        this.adImageUrl = adImageUrl;
    }

    public String getAdThumbnailUrl() {
        return adThumbnailUrl;
    }

    public void setAdThumbnailUrl(String adThumbnailUrl) {
        this.adThumbnailUrl = adThumbnailUrl;
    }

    public String getBarcodeImageUrl() {
        return barcodeImageUrl;
    }

    public void setBarcodeImageUrl(String barcodeImageUrl) {
        this.barcodeImageUrl = barcodeImageUrl;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
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

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getCouponID() {
        return couponID;
    }

    public void setCouponID(String couponID) {
        this.couponID = couponID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getStores() {
        return stores;
    }

    public void setStores(String stores) {
        this.stores = stores;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public String getZipCodes() {
        return zipCodes;
    }

    public void setZipCodes(String zipCodes) {
        this.zipCodes = zipCodes;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
