/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mscripts.externalrequesthandler.service;


import java.util.Map;

import org.w3c.dom.NodeList;

import com.mscripts.utils.mscriptsException;

/**
 *
 * @author rhiresheddi
 */
public interface TenForTenFeedService {

    //Method to feed Ten For Ten Details 
    public Map feedTenForTen(NodeList nodes, String feedFileName, String feedDate, String ClientID) throws mscriptsException;
}
