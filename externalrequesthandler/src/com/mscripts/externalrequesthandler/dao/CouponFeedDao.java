/*******************************************************
Title               : CouponFeedDao.java
Author              : Pratyush Pushkar
Description         : Interface for CouponFeedDao.
Modification History: Not Applicable
Created             : 20-Jun-11
Modified            : Not Applicable
Notes               : None
 *******************************************************/
package com.mscripts.externalrequesthandler.dao;

import java.util.Map;

import com.mscripts.utils.mscriptsException;

/**
 *
 * @author ssreeraj
 */
public interface CouponFeedDao {

    //Method that handles adding coupon batches
    public Map addCouponBatches(String batchID, String campaignID,
            String batchName, String batchType, String batchImageThumbnailUrl,
            String batchImageBannerUrl, String batchImageBannerText,
            String batchImageThumbnailText, String validFrom, String validTo,
            String clientID) throws mscriptsException;

    //Method that handles adding coupons
    public Map addCoupons(
            String clientID,
            String campaignID, String batchID,  String batchValidFrom, String batchValidTo,
            int numCoupons,
            String couponIDCSV,
            String categoryCSV, String subCategoryCSV, String brandCSV, String productNameCSV,
            String adCaptionCSV, String adDescCSV, String barcodeImageUrlCSV,
            String adImageUrlCSV, String adThumbnailUrlCSV, String storesCSV,
            String zipCodesCSV,
            String validFromCSV, String validToCSV,
            String priceCSV, String priorityCSV,String separator
            ) throws mscriptsException;
}
