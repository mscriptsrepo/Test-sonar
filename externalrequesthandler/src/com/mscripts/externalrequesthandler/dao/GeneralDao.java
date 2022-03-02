/*******************************************************
Title               : GeneralDao.java
Author              : Sabu Sree Raj
Description         : Interface for GeneralDao.
Modification History: Not Applicable
Created             : 18-Jan-10
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
public interface GeneralDao {

    //Method to audit the handler request
    public boolean auditLog(String clientID, String apiName, String apiFeatureCode, String handlerRequestBody, String handlerResponseCode, String errorDetailID,
            String handlerResponse, String addedToQueue, String accessKey) throws mscriptsException;

     //Method that handles PDX initial link token message
    public Map initialLinkTokenMessage(String mobile, String token, String verificationCode, String action,
            String fName, String lName, String storeID, String timeZone,
            String cvPdxSignupModeOnphone, String cvPdxSignupModeStore,
            String cvPdxILTMModeTeardown, String cvPdxILTMModeSetup, String cvAdmin,
            String clientID, String oldToken, String oldMobile,
            String survivingToken, String shortCode, String shortCodeUserName,
            String shortCodeServiceID, String scHandlerErrorCode, String prefix, String accessKey, String langCode) throws mscriptsException;

    public Map getCommunicationDetails(String message, String clientID, String customerID, String mobile, String prefix, String secondaryKey) throws mscriptsException;

    public Map getClientDetails(String token, String mobile, String verificationCode, String firstName, String storeID, String secondaryKey) throws mscriptsException;
    
    public Map fetchTOSUrl(String clientID) throws mscriptsException;
    
    /**
	 * Method to update is_sms_token_valid flag
	 * @param clientId
	 * @param customerId
	 * @param webLogicSessionId
	 * @throws mscriptsException
	 */
	public void updateSmsTokenFlag(String flagStatus, String customerId, String clientId) throws mscriptsException;
	
	public Map checkIsMigratedUser(String clientID, String mobileNumber, String firstName, String lastName, String verificationCode, String secondaryKey) throws mscriptsException;
	
	
	public void updateDeviceStagings(String shortcode, String shortcode_username, String shortcode_serviceid, String enable_prefix, String sms_token) throws mscriptsException;
	
	public boolean isCustomerExist( String lName, String mobile, String verificationCode,
			String encryptKey)throws mscriptsException;
	
	public String getdeviceStagingId( String lastName, String mobile, String encryptKey,String verificationCode)
			throws mscriptsException;

	public void updateDeviceStagings(String deviceStaging_id,String token)throws mscriptsException;

	public void updateMigrationList(String deviceStagingId,String fName, String lName, String mobile, String encryptKey)throws mscriptsException;

	public void updateIltmParam(String fName, String lName, String mobile, String encryptKey,
			String verificationCode, String token)throws mscriptsException;

	public String getKey(String secondaryKey, String clientID)throws mscriptsException;


	public void updateCustomerSignupStore(String deviceStagingsId) throws mscriptsException;
 
	public Map getOldAuthCode(String token, String mobile) throws mscriptsException;
 
 	public boolean isMigratedUser(String lName, String mobile, String verificationCode,String encryptKey)throws mscriptsException;
 
	public void updateErrorInMigrationList(String deviceStagingId,String error,String errorCode,String mobile, String encryptKey)throws mscriptsException;

}
