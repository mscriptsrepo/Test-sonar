/*******************************************************
Title               : GeneralService.java
Author              : Sabu Sree Raj
Description         : GeneralService Interface
Modification History: Not Applicable
Created             : 18-Jan-10
Modified            : Not Applicable
Notes               : None
 *******************************************************/
package com.mscripts.externalrequesthandler.service;

import java.util.Map;

import com.mscripts.utils.mscriptsException;

/**
 *
 * @author ssreeraj
 */
public interface GeneralService {

    //Method that gets client details based on client name.
    public Map getClientDetails(String cvPdxClientName) throws mscriptsException;

    public Map getClientDetails(String token, String mobile, String verificationCode, String firstName, String storeID) throws mscriptsException;

    //Method that handles PDX initial link token message
    public void initialLinkTokenMessage(String mobile, String token, String verificationCode, String action, String fName,
            String lName, String storeID, String timeZone, String clientID, String clientName, String oldToken, String oldMobile, String survivingToken, String langCode) throws mscriptsException;

    public Map isValidSmsToken(String token, String clientid) throws mscriptsException;

    public void sendMessage(String mobile, String clientID, String commName, String[] smscontents, String rxNumber,
            boolean isVerified, boolean isTxtMsgActive, String customerID,
            String shortCode, String shortCodeUserName, String shortCodeServiceID, String prefix) throws Exception;

}
