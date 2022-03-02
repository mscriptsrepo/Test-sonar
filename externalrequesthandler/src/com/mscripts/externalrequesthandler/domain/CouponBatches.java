/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.domain;

/**
 *
 * @author ppushkar
 */
public class CouponBatches {

    private String batchID;
    private String campaignID;
    private String batchType;
    private String batchName;
    private String batchImageThumbnailUrl;
    private String batchImageThumbnailText;
    private String batchImageBannerUrl;
    private String batchImageBannerText;
    private String validFrom;
    private String validTo;

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getBatchImageBannerText() {
        return batchImageBannerText;
    }

    public void setBatchImageBannerText(String batchImageBannerText) {
        this.batchImageBannerText = batchImageBannerText;
    }

    public String getBatchImageBannerUrl() {
        return batchImageBannerUrl;
    }

    public void setBatchImageBannerUrl(String batchImageBannerUrl) {
        this.batchImageBannerUrl = batchImageBannerUrl;
    }

    public String getBatchImageThumbnailText() {
        return batchImageThumbnailText;
    }

    public void setBatchImageThumbnailText(String batchImageThumbnailText) {
        this.batchImageThumbnailText = batchImageThumbnailText;
    }

    public String getBatchImageThumbnailUrl() {
        return batchImageThumbnailUrl;
    }

    public void setBatchImageThumbnailUrl(String batchImageThumbnailUrl) {
        this.batchImageThumbnailUrl = batchImageThumbnailUrl;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
    }

    public String getCampaignID() {
        return campaignID;
    }

    public void setCampaignID(String campaignID) {
        this.campaignID = campaignID;
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
}
