/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.service;

import org.w3c.dom.NodeList;

import com.mscripts.utils.mscriptsException;

/**
 *
 * @author ppushkar
 */
public interface CouponFeedService {
//Method that gets client details based on client name.

    public String addCouponBatches(String campaignID, NodeList batchNodes, String clientID) throws mscriptsException;
    //private String addCouponsForBatch(String batchId, Document batchDoc) throws mscriptsException;
}
