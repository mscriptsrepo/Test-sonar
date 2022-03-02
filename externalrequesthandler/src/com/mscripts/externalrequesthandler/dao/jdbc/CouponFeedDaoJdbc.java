/*******************************************************
Title               : CouponFeedDao.java
Author              : Pratyush Pushkar
Description         : Interface for CouponFeedDao.
Modification History: Not Applicable
Created             : 20-Jun-11
Modified            : Not Applicable
Notes               : None
 *******************************************************/
package com.mscripts.externalrequesthandler.dao.jdbc;

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.mscripts.dao.SPInvoker;
import com.mscripts.externalrequesthandler.dao.CouponFeedDao;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author ssreeraj
 */
public class CouponFeedDaoJdbc implements CouponFeedDao {

    private SPInvoker spInvoker;
    private DataSourceTransactionManager transactionManager;
    private Properties transactionAttributes;
    private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + CouponFeedDaoJdbc.class.getName());
	private static final Logger LOGGER_PHI = LogManager.getLogger("phi." + CouponFeedDaoJdbc.class.getName());
    public Map addCouponBatches(String batchID, String campaignID, String batchName, String batchType,
            String batchImageThumbnailUrl, String batchImageBannerUrl, String batchImageBannerText,
            String batchImageThumbnailText, String validFrom, String validTo, String clientID) throws mscriptsException {
        try {
      
                LOGGER_NON_PHI.debug("Entered into addCoupons Batches method for clientID "+clientID);
           
            return spInvoker.invokeSp_Map("sp_ERH_addCouponBatch", new Object[]{clientID, batchID, campaignID, batchName, batchType,
                        batchImageThumbnailUrl, batchImageBannerUrl, batchImageBannerText,
                        batchImageThumbnailText, validFrom, validTo});
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception occured while adding coupon batches :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.CouponFeedDaoJdbc-addCouponBatches", mscriptsExceptionSeverity.High, ex);
        }
    }

    public Map addCoupons(String clientID,
            String campaignID, String batchID, String batchValidFrom, String batchValidTo,
            int numCoupons,
            String couponIDCSV,
            String categoryCSV, String subCategoryCSV, String brandCSV, String productNameCSV,
            String adCaptionCSV, String adDescCSV, String barcodeImageUrlCSV,
            String adImageUrlCSV, String adThumbnailUrlCSV, String storesCSV,
            String zipCodesCSV,
            String validFromCSV, String validToCSV,
            String priceCSV, String priorityCSV, String separator) throws mscriptsException {


        Map map = null;


        try {
            
             
                LOGGER_NON_PHI.debug("Entered into addCoupons method for clientID "+clientID);
           
             
            map = spInvoker.invokeSp_Map("sp_ERH_addCoupons", new Object[]{
                        clientID, campaignID, batchID, batchValidFrom, batchValidTo, numCoupons,
                        couponIDCSV, categoryCSV, subCategoryCSV, brandCSV, productNameCSV,
                        adCaptionCSV, adDescCSV, barcodeImageUrlCSV, adImageUrlCSV, adThumbnailUrlCSV, storesCSV,
                        zipCodesCSV, validFromCSV, validToCSV, priceCSV, priorityCSV, separator}
                   );
        } catch (Exception ex) {
             LOGGER_NON_PHI.error("Exception occured while adding coupons:", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.CouponFeedDaoJdbc-addCoupons", mscriptsExceptionSeverity.High, ex);
        }
        return map;
    }

    public SPInvoker getSpInvoker() {
        return spInvoker;
    }

    public void setSpInvoker(SPInvoker spInvoker) {
        this.spInvoker = spInvoker;
    }

    public Properties getTransactionAttributes() {
        return transactionAttributes;
    }

    public void setTransactionAttributes(Properties transactionAttributes) {
        this.transactionAttributes = transactionAttributes;
    }

    public DataSourceTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
