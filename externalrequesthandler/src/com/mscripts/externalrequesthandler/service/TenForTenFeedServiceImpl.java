/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mscripts.externalrequesthandler.dao.TenForTenFeedDao;
import com.mscripts.utils.AppConfiguration;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author rhiresheddi
 */
public class TenForTenFeedServiceImpl implements TenForTenFeedService {

    private TenForTenFeedDao tenForTenFeedDao;
    private DataSourceTransactionManager transactionManager;
    private Properties transactionAttributes;
    private AppConfiguration appConfiguration;
    private static final Logger LOGGER = Logger.getLogger(TenForTenFeedServiceImpl.class);

    public Map feedTenForTen(NodeList nodes, String feedFileName, String feedDate, String clientID) throws mscriptsException {

        String errorSource = "com.mscripts.externalrequesthandler.service.TenForTenFeedServiceImpl-feedTenForTen";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

        String cvErrorCodeInvalidXML = null;
        String feedRunID = null;
        Map feedRunMap = null;

        try {

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Entered feedTenForTen for the client with ID:" + clientID);
            }
            cvErrorCodeInvalidXML = appConfiguration.get("cvErrorCodeInvalidXML");
            //Get no of records
            int numberRecords = nodes.getLength();
            List ardSubList = new ArrayList();
            //add cardfile records to list
            for (int i = 0; i < numberRecords; i++) {
                ardSubList.add(nodes.item(i));
            }

            Iterator iter = ardSubList.listIterator();
            String strUpdateValues = "";
            String updateSep = "";

            Node cardfileNode = null;
            String cardNum = null;
            String refillCount = null;
            String statusFlag = null;

            while (iter.hasNext()) {
                //Gets cardfile info from iterator content
                cardfileNode = (Node) iter.next();
                try {
                    cardNum = cardfileNode.getAttributes().getNamedItem("cardnum").getNodeValue();
                    refillCount = cardfileNode.getAttributes().getNamedItem("refillcount").getNodeValue();
                    statusFlag = cardfileNode.getAttributes().getNamedItem("statusflag").getNodeValue();
                } catch (Exception e) {
                    //tobe determined: Check whether to skip this exception throw to process already read records
                    LOGGER.error("Exception occured while iterating :",e);
                    throw new mscriptsException(cvErrorCodeInvalidXML, errorSource, errorSeverity, e);
                }
                strUpdateValues = strUpdateValues + updateSep + "('" + cardNum + "','" + refillCount + "','" + statusFlag + "')";
                updateSep = ",";
            }
            feedRunMap = tenForTenFeedDao.feedTenForTen(clientID, feedFileName, feedDate, strUpdateValues);
        } catch (mscriptsException e) {
            LOGGER.error("Mscripts Exception occured while Ten For Ten feed services  :",e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception occured while Ten For Ten feed services  :",e);
            throw new mscriptsException(e.getMessage(), errorSource, errorSeverity, e);
        }
        return feedRunMap;
    }

    public AppConfiguration getAppConfiguration() {
        return appConfiguration;
    }

    public void setAppConfiguration(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    public TenForTenFeedDao getTenForTenFeedDao() {
        return tenForTenFeedDao;
    }

    public void setTenForTenFeedDao(TenForTenFeedDao tenForTenFeedDao) {
        this.tenForTenFeedDao = tenForTenFeedDao;
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
