/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.service;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mscripts.externalrequesthandler.dao.CouponFeedDao;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author sbhat
 */
public class AddCoupons extends Thread {

    private int threadId;
    private String clientID;
    private String campaignID;
    private String batchID;
    private String batchValidFrom;
    private String batchValidTo;
    private int numCoupons;
    private String couponIDCSV;
    private String categoryCSV;
    private String subCategoryCSV;
    private String brandCSV;
    private String productNameCSV;
    private String adCaptionCSV;
    private String adDescCSV;
    private String barcodeImageUrlCSV;
    private String adImageUrlCSV;
    private String adThumbnailUrlCSV;
    private String storesCSV;
    private String zipCodesCSV;
    private String validFromCSV;
    private String validToCSV;
    private String priceCSV;
    private String priorityCSV;
    private String separator;
    private CouponFeedDao couponFeedDao;
    private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + AddCoupons.class.getName());
  	private static final Logger LOGGER_PHI = LogManager.getLogger("phi." + AddCoupons.class.getName());


    public AddCoupons(
            int threadID,
            CouponFeedDao couponFeedDao,
            String clientID,
            String campaignID,
            String batchID,
            String batchValidFrom,
            String batchValidTo,
            int numCoupons,
            String couponIDCSV,
            String categoryCSV,
            String subCategoryCSV,
            String brandCSV,
            String productNameCSV,
            String adCaptionCSV,
            String adDescCSV,
            String barcodeImageUrlCSV,
            String adImageUrlCSV,
            String adThumbnailUrlCSV,
            String storesCSV,
            String zipCodesCSV,
            String validFromCSV,
            String validToCSV,
            String priceCSV,
            String priorityCSV,
            String separator) {

        this.threadId = threadID;
        this.couponFeedDao = couponFeedDao;
        this.clientID = clientID;
        this.campaignID = campaignID;
        this.batchID = batchID;
        this.batchValidFrom = batchValidFrom;
        this.batchValidTo = batchValidTo;
        this.numCoupons = numCoupons;
        this.couponIDCSV = couponIDCSV;
        this.categoryCSV = categoryCSV;
        this.subCategoryCSV = subCategoryCSV;
        this.brandCSV = brandCSV;
        this.productNameCSV = productNameCSV;
        this.adCaptionCSV = adCaptionCSV;
        this.adDescCSV = adDescCSV;
        this.barcodeImageUrlCSV = barcodeImageUrlCSV;
        this.adImageUrlCSV = adImageUrlCSV;
        this.adThumbnailUrlCSV = adThumbnailUrlCSV;
        this.storesCSV = storesCSV;
        this.zipCodesCSV = zipCodesCSV;
        this.validFromCSV = validFromCSV;
        this.validToCSV = validToCSV;
        this.priceCSV = priceCSV;
        this.priorityCSV = priorityCSV;
        this.separator = separator;
    }

    public AddCoupons() {
    }

    private void addCouponsCSV() throws mscriptsException {
        try {

          
                LOGGER_NON_PHI.debug("Entered into addCouponsCSV method");
           
            Map couponMap = couponFeedDao.addCoupons(clientID, campaignID, batchID, batchValidFrom, batchValidTo, numCoupons,
                    couponIDCSV, categoryCSV,
                    subCategoryCSV, brandCSV, productNameCSV, adCaptionCSV,
                    adDescCSV, barcodeImageUrlCSV, adImageUrlCSV,
                    adThumbnailUrlCSV, storesCSV, zipCodesCSV,
                    validFromCSV, validToCSV, priceCSV, priorityCSV, separator);
            if (couponMap != null) {
                
                    LOGGER_NON_PHI.info("Coupon map recieved from the database is ={}" + couponMap + " which has ={}" + couponMap.size() + " number of coupons");
              
            }

            return;
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception occured while adding coupons to CSV :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.service.AddCoupons - addCouponsCSV", mscriptsExceptionSeverity.High, ex);
        }
    }

    public void run() {
        try {
            //System.out.print("Thread started ID: "+this.getId()+ " "+ java.util.Calendar.getInstance().getTime());

            if (LOGGER_NON_PHI.isInfoEnabled()) {
                LOGGER_NON_PHI.info("Thread started ID={} " + this.getId() + " ={} " + java.util.Calendar.getInstance().getTime());
            }

            addCouponsCSV();
            //System.out.print("Thread terminating ID: "+this.getId()+ " "+ java.util.Calendar.getInstance().getTime()+ "Thread Count " +Thread.activeCount());
            
                LOGGER_NON_PHI.info("Thread terminating ID={} " + this.getId() + " ={} " + java.util.Calendar.getInstance().getTime() + "Thread Count ={}" + Thread.activeCount());
           

            return;
        } catch (mscriptsException ex) {
            LOGGER_NON_PHI.error("Exception occured in run method of the thread :", ex);
        } catch (Exception e) {
            LOGGER_NON_PHI.error("Exception occured in run method of the thread :", e);
        }
    }
}
