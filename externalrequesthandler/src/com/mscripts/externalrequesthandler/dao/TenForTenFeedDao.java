/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mscripts.externalrequesthandler.dao;

import java.util.Map;

import com.mscripts.utils.mscriptsException;

/**
 *
 * @author rhiresheddi
 */
public interface TenForTenFeedDao {

    public Map feedTenForTen(String clientId, String feedFileName, String feedDate, String strUpdateAryValue) throws mscriptsException;

}
