/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.service;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mscripts.externalrequesthandler.dao.CouponFeedDao;
import com.mscripts.externalrequesthandler.domain.CouponBatches;
import com.mscripts.utils.AppConfiguration;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.XMLUtils;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author ppushkar
 */
public class CouponFeedServiceImpl implements CouponFeedService {

    private CouponFeedDao couponFeedDao;
    private DataSourceTransactionManager transactionManager;
    private Properties transactionAttributes;
    private AppConfiguration appConfiguration;
    
    private static final Logger LOGGER=Logger.getLogger(CouponFeedServiceImpl.class);

    public String addCouponBatches(String campaignID, NodeList batchNodes, String clientID) throws mscriptsException {
        String errorSource = "com.mscripts.externalrequesthandler.service.CouponFeedServiceImpl-addCouponBatches";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        try {

            Document batchDoc = null;
            int batchNodeSize = batchNodes.getLength();
            Map batchMap = null;
            String batchID = null;
            String batchValidFrom = null;
            String batchValidTo = null;
            CouponBatches batch = null;
            String succeededBatchIDCSV = "";
            String failedBatchIDCSV = "";

            for (int i = 0; i < batchNodeSize; i++) {

                batchDoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(batchNodes.item(i)));
                batch = new CouponBatches();


                batch.setBatchID(XMLUtils.getNodeValue(batchDoc, "//batch/@id"));
                batch.setBatchName(XMLUtils.getNodeValue(batchDoc, "//batch/@name"));
                batch.setBatchType(XMLUtils.getNodeValue(batchDoc, "//batch/@type"));
                // TODO: Should we do URL encoding as well?
                batch.setBatchImageThumbnailUrl(XMLUtils.getNodeValue(batchDoc, "//batch/batchimagethumbnailurl"));
                batch.setBatchImageBannerUrl(XMLUtils.getNodeValue(batchDoc, "//batch/batchimagebannerurl"));
                batch.setBatchImageBannerText(XMLUtils.getNodeValue(batchDoc, "//batch/batchimagebannertext"));
                batch.setBatchImageThumbnailText(XMLUtils.getNodeValue(batchDoc, "//batch/batchimagethumbnailtext"));
                batch.setValidFrom(XMLUtils.getNodeValue(batchDoc, "//batch/validfrom"));
                batch.setValidTo(XMLUtils.getNodeValue(batchDoc, "//batch/validto"));

                batchValidFrom = MiscUtils.dateFormat(batch.getValidFrom(), "MM/dd/yyyy", "yyyy-MM-dd");
                batchValidTo = MiscUtils.dateFormat(batch.getValidTo(), "MM/dd/yyyy", "yyyy-MM-dd");

                batchMap = couponFeedDao.addCouponBatches(batch.getBatchID(), campaignID, batch.getBatchName(), batch.getBatchType(), batch.getBatchImageThumbnailUrl(),
                        batch.getBatchImageBannerUrl(), batch.getBatchImageBannerText(), batch.getBatchImageThumbnailText(),
                        batchValidFrom, batchValidTo, clientID);
                batchID = batchMap.get("id_val").toString();
                succeededBatchIDCSV = succeededBatchIDCSV + batchID + ",";
                // Add coupons for the batch
                addCouponsForBatch(campaignID, batchID, batchValidFrom, batchValidTo, batchDoc, clientID);
            }
        } catch (mscriptsException ex) {
            LOGGER.error("mscripts Exception occured while adding coupon batches :",ex);
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Exception occured while adding coupon batches :",ex);
            throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
        }

        // TODO: // Should we set the newly created or updated batch ID and return it back? We can use that batch ID to pass as a
        // parameter to the addCouponsForBatch?
        return null;
        // TODO: //throw new UnsupportedOperationException("Not supported yet.");
    }

    private void addCouponsForBatch(String campaignID, String batchID, String batchValidFrom,
            String batchValidTo, Document batchDoc, String clientID) throws mscriptsException {
        //throw new UnsupportedOperationException("Not yet implemented");
        String errorSource = "com.mscripts.externalrequesthandler.service.CouponFeedServiceImpl-addCouponsForBatch";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        try {
            NodeList adNodes;

            adNodes = XMLUtils.getNodeList(batchDoc, "//batch/ad");
            int adNodeSize = adNodes.getLength();

            Document adDoc = null;

            int cvCouponsCsvMaxCount = Integer.parseInt(appConfiguration.get("cvCouponCSVMaxCount"));
            double f = Double.parseDouble(Integer.toString(adNodeSize)) / Double.parseDouble(Integer.toString(cvCouponsCsvMaxCount));
            int threadCount = (int) java.lang.Math.ceil(f);

            int threadNumber = 0;
            String separatorCSV = appConfiguration.get("cvCouponDelimiter");
            String validTo = null;
            String validFrom = null;
            Thread threadArray[] = new Thread[threadCount];

            int i = 0;
            while (i < adNodeSize) //Iterate through all coupons inside the particular batch
            {
                String couponIdCSV = "";
                String categoryCSV = "";
                String subCategoryCSV = "";
                String brandCSV = "";
                String productNameCSV = "";
                String adCaptionCSV = "";
                String adDescCSV = "";
                String barcodeImageUrlCSV = "";
                String adImageUrlCSV = "";
                String adThumbnailUrlCSV = "";
                String storeListCSV = "";
                String zipcodesListCSV = "";
                String validFromCSV = "";
                String validToCSV = "";
                String itemPriceCSV = "";
                String couponPriorityCSV = "";


                int j = 0;
                while (j < cvCouponsCsvMaxCount && i < adNodeSize) // form csvs for - smaller of (adNodeSize and cvCouponsCsvMaxCount) times
                {

                    adDoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(adNodes.item(i)));
                    i++;//increment coupon iterator after reading the coupon detail

                    //Concatenate in a comma separated list
                    couponIdCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/@id"));
                    categoryCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/category"));
                    subCategoryCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/subcategory"));
                    brandCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/brand"));
                    productNameCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/productname"));
                    adCaptionCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/adcaption"));
                    adDescCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/addesc"));
                    barcodeImageUrlCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/barcodeimageurl"));
                    adImageUrlCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/adimageurl"));
                    adThumbnailUrlCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/adthumbnailurl"));
                    storeListCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/stores"));
                    zipcodesListCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/zipcodes"));

                    if ((validFrom = MiscUtils.dateFormat(XMLUtils.getNodeValue(adDoc, "//ad/validfrom"), "MM/dd/yyyy", "yyyy-MM-dd")) == null) {
                        validFrom = "";
                    }
                    if ((validTo = MiscUtils.dateFormat(XMLUtils.getNodeValue(adDoc, "//ad/validto"), "MM/dd/yyyy", "yyyy-MM-dd")) == null) {
                        validTo = "";
                    }
                    validToCSV += (separatorCSV + validTo);
                    validFromCSV += (separatorCSV + validFrom);
                    itemPriceCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/price"));
                    couponPriorityCSV += (separatorCSV + XMLUtils.getNodeValue(adDoc, "//ad/priority"));

                    j++;//increment couponCount iterator

                }// end of for - <cvCouponsCsvMaxCount>


                couponIdCSV = couponIdCSV.replaceFirst(separatorCSV, "");

                categoryCSV = categoryCSV.replaceFirst(separatorCSV, "");

                subCategoryCSV = subCategoryCSV.replaceFirst(separatorCSV, "");

                brandCSV = brandCSV.replaceFirst(separatorCSV, "");

                productNameCSV = productNameCSV.replaceFirst(separatorCSV, "");

                adCaptionCSV = adCaptionCSV.replaceFirst(separatorCSV, "");

                adDescCSV = adDescCSV.replaceFirst(separatorCSV, "");

                barcodeImageUrlCSV = barcodeImageUrlCSV.replaceFirst(separatorCSV, "");

                adImageUrlCSV = adImageUrlCSV.replaceFirst(separatorCSV, "");

                adThumbnailUrlCSV = adThumbnailUrlCSV.replaceFirst(separatorCSV, "");

                storeListCSV = storeListCSV.replaceFirst(separatorCSV, "");

                zipcodesListCSV = zipcodesListCSV.replaceFirst(separatorCSV, "");

                validFromCSV = validFromCSV.replaceFirst(separatorCSV, "");

                validToCSV = validToCSV.replaceFirst(separatorCSV, "");

                itemPriceCSV = itemPriceCSV.replaceFirst(separatorCSV, "");

                couponPriorityCSV = couponPriorityCSV.replaceFirst(separatorCSV, "");


                AddCoupons adCpnThread = new AddCoupons(threadNumber, couponFeedDao, clientID, campaignID, batchID, batchValidFrom, batchValidTo, (j),
                        couponIdCSV, categoryCSV,
                        subCategoryCSV, brandCSV, productNameCSV, adCaptionCSV,
                        adDescCSV, barcodeImageUrlCSV, adImageUrlCSV,
                        adThumbnailUrlCSV, storeListCSV, zipcodesListCSV,
                        validFromCSV, validToCSV, itemPriceCSV, couponPriorityCSV, separatorCSV);

                threadArray[threadNumber++] = adCpnThread;
                adCpnThread.start();

            }// end of while - all coupons

            //Wait for all threads to complete
            for (int k = 0; k < threadCount; k++) {
                threadArray[k].join();
            }
            return;
        } catch (mscriptsException ex) {
            LOGGER.error("mscripts Exception occured while adding coupon for batches :",ex);
            throw ex;
        } catch (Exception ex) {
             LOGGER.error("Exception occured while adding coupon for batches :",ex);
            throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
        }

        // TODO: // Do we need to return something here?

    }

    public AppConfiguration getAppConfiguration() {
        return appConfiguration;
    }

    public void setAppConfiguration(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    public CouponFeedDao getCouponFeedDao() {
        return couponFeedDao;
    }

    public void setCouponFeedDao(CouponFeedDao couponFeedDao) {
        this.couponFeedDao = couponFeedDao;
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
