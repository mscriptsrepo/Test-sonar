/**
 * *****************************************************
 * Title : GeneralDaoJdbc.java Author : Sabu Sree Raj Description : Implements
 * the GeneralDao interface. Modification History: Not Applicable Created :
 * 18-Jan-10 Modified : Not Applicable Notes : None
 ******************************************************
 */
package com.mscripts.externalrequesthandler.dao.jdbc;

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.QueryInvoker;
import com.mscripts.dao.SPInvoker;
import com.mscripts.externalrequesthandler.dao.GeneralDao;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.QueryBuilder;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author ssreeraj
 */
public class GeneralDaoJdbc implements GeneralDao {

    private SPInvoker spInvoker;
    private SPInvoker readInvoker;
    private DataSourceTransactionManager transactionManager;
    private Properties transactionAttributes;
    private QueryInvoker queryInvoker;
    private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + GeneralDaoJdbc.class.getName());
   	private static final Logger LOGGER_PHI = LogManager.getLogger("phi." + GeneralDaoJdbc.class.getName());
       

    //Method to audit the handler request
    public boolean auditLog(String clientID, String apiName, String apiFeatureCode, String handlerRequestBody, String handlerResponseCode, String errorDetailID,
            String handlerResponse, String addedToQueue, String accessKey) throws mscriptsException {
        try {

            
                LOGGER_NON_PHI.info("Entered into audit log method for clientID " + clientID);
           

            spInvoker.invokeSp("sp_ERH_addAuditLog", new Object[]{clientID, apiName, apiFeatureCode,
                handlerRequestBody, handlerResponseCode, errorDetailID,
                handlerResponse, addedToQueue, accessKey});
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception occured while auditing logs :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-auditLog", mscriptsExceptionSeverity.High, ex);
        }
        return true;
    }

    //Method that handles PDX initial link token message
    public Map initialLinkTokenMessage(String mobile, String token, String verificationCode, String action,
            String fName, String lName, String storeID, String timeZone,
            String cvPdxSignupModeOnphone, String cvPdxSignupModeStore,
            String cvPdxILTMModeTeardown, String cvPdxILTMModeSetup, String cvAdmin,
            String clientID, String oldToken, String oldMobile, String survivingToken,
            String shortCode, String shortCodeUserName, String shortCodeServiceID,
            String scHandlerErrorCode, String prefix, String accessKey, String langCode) throws mscriptsException {
        try {

            if (LOGGER_NON_PHI.isInfoEnabled()) {
                LOGGER_NON_PHI.info("Entered into initialLinkTokenMessage  method for mobile " + mobile);
            }
            String cvtCommNameStorekeeperSignupFailure = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVTCOMMNAMESTOREKEEPERSIGNUPFAILURE);
            return spInvoker.invokeSp_Map("sp_ERH_pdxILTM", new Object[]{clientID, mobile, verificationCode, token,
                fName, lName, storeID, timeZone, action, cvPdxSignupModeOnphone,
                cvPdxSignupModeStore, cvPdxILTMModeTeardown, cvPdxILTMModeSetup, cvAdmin,
                oldToken, oldMobile, survivingToken, shortCode, shortCodeUserName,
                shortCodeServiceID, scHandlerErrorCode, prefix, accessKey, cvtCommNameStorekeeperSignupFailure});
        } catch (Exception ex) {

            LOGGER_NON_PHI.error("Exception occured while initializing link token message :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-initialLinkTokenMessage", mscriptsExceptionSeverity.High, ex);
        }
    }

    public Map getCommunicationDetails(String message, String clientID, String customerID, String mobile, String prefix, String accessKey) throws mscriptsException {
        Map map = null;
        try {
            
                LOGGER_PHI.info("Entered into get communication details method for mobile={} " + mobile);
           
            String cvtAdminConsoleUserTimeZone =ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvtAdminConsoleUserTimeZone");
        	String  cvtDefaultVC =ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvtDefaultVC");
            map = readInvoker.invokeSp_Map("sp_CI_getCommunicationsTemplate", new Object[]{clientID, message, customerID, mobile, prefix, null, accessKey,cvtAdminConsoleUserTimeZone,cvtDefaultVC});
            map.put("cvSupportEmailAddress",ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvSupportEmailAddress").toString());
            map.put("cvSupportPhoneNumber",ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvSupportPhoneNumber").toString());
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception occured while getting communication details :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-getCommunicationDetails", mscriptsExceptionSeverity.High, ex);
        }
        return map;
    }

    public Map getClientDetails(String token, String mobile, String verificationCode, String firstName, String storeID, String secondaryKey) throws mscriptsException {
        try {
           
            LOGGER_PHI.info("Entered into get client details method for mobile={} " + mobile);
            
            return readInvoker.invokeSp_Map("sp_ERH_getClientDetails", new Object[]{token, mobile, verificationCode, firstName, storeID, secondaryKey});
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception occured while getting client details :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-getClientDetails", mscriptsExceptionSeverity.High, ex);
        }
    }
    
    @Override
    public Map fetchTOSUrl(String clientID) throws mscriptsException {
       
        Map<String, String> returnMap = null;
        try {
        	LOGGER_NON_PHI.debug("Invoking Query to fetch TOS URL with clientID ::"+ clientID);
            returnMap = queryInvoker.invokeQueryMap(QueryBuilder.GET_TOS_URL, new Object[]{clientID});
        } catch (BadSqlGrammarException bsge) {
        	LOGGER_NON_PHI.error("Invalid SQL Error. Please check logs:", bsge);
        	throw new mscriptsException(bsge.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-fetchTOSUrl", mscriptsExceptionSeverity.High, bsge);
        } catch (IncorrectResultSizeDataAccessException ex) {
        	LOGGER_NON_PHI.error("Zero return from DB for Input params:", ex);
            return returnMap;
        } catch (Exception ex) {
        	LOGGER_NON_PHI.error("Error occured while processing fetchTOSUrl: ", ex);
        	throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-fetchTOSUrl", mscriptsExceptionSeverity.High, ex);
        }
        return returnMap;
    }
    
	@Override
	public void updateSmsTokenFlag(String flagStatus, String customerId, String clientId) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateSmsTokenFlag";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		Map<String, String> resultMap = null;
		String sqlString = QueryBuilder.UPDATE_TOKEN_FLAG;
		try {
			queryInvoker.updateUsingSqlString(sqlString, new Object[]{flagStatus, customerId, clientId});
		} catch(Exception e) {
			LOGGER_NON_PHI.error("Exception occured while processing updateSmsTokenFlag DAO");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}

		
	}
	
	@Override
	public Map checkIsMigratedUser(String clientID, String mobileNumber, String firstName, String lastName,String verificationCode, String secondaryKey) throws mscriptsException{
        try {
           if (LOGGER_NON_PHI.isInfoEnabled()) {
        	   LOGGER_NON_PHI.info("Entered into checkIsMigratedUser method for mobile ");
           }
           return readInvoker.invokeSp_Map("sp_MU_checkIfMigratedUser", new Object[]{clientID,mobileNumber, firstName, lastName, verificationCode,null, secondaryKey});
       }catch (IncorrectResultSizeDataAccessException ex) {
    	   LOGGER_NON_PHI.error("Exception occured while getting client details :", ex);
           return null;
       } catch (Exception ex) {
    	   LOGGER_NON_PHI.error("Exception occured while getting client details :", ex);
           throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-checkIsMigratedUser", mscriptsExceptionSeverity.High, ex);
       }
   }
	
	@Override
	public void updateDeviceStagings(String shortcode, String shortcode_username, String shortcode_serviceid, String enable_prefix, String sms_token) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateDeviceStagings";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;		
		String sqlString = QueryBuilder.UPDATE_DEVICE_STAGINGS;
		try {
			queryInvoker.updateUsingSqlString(sqlString, new Object[]{shortcode, shortcode_username, shortcode_serviceid, enable_prefix, sms_token});
		} catch(Exception e) {
			LOGGER_NON_PHI.error("Exception occured while processing updateDeviceStagings DAO");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}

		
	}
	
	@Override
	public void updateCustomerSignupStore(String deviceStagingsId) throws mscriptsException{
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateDeviceStagings";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;		
		String sqlString = QueryBuilder.UPDATE_CUSTOMER_SIGNUP_STORE;
		try {
			queryInvoker.updateUsingSqlString(sqlString, new Object[]{deviceStagingsId});
		} catch(Exception e) {
			LOGGER_NON_PHI.error("Exception occured while processing updateDeviceStagings DAO");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}

		
	}


    public QueryInvoker getQueryInvoker() {
		return queryInvoker;
	}

	public void setQueryInvoker(QueryInvoker queryInvoker) {
		this.queryInvoker = queryInvoker;
	}

	public SPInvoker getReadInvoker() {
        return readInvoker;
    }

    public void setReadInvoker(SPInvoker readInvoker) {
        this.readInvoker = readInvoker;
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
    

    @Override
	public boolean isCustomerExist( String lName, String mobile, String verificationCode,
			String encryptKey)throws mscriptsException{
    	String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - isCustomerExists";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;		
    	String sqlString = QueryBuilder.IS_CUSTOMER_EXIST;
	try {
		
		Map count=queryInvoker.invokeQueryMap(sqlString,new Object[]{ lName, encryptKey,mobile,encryptKey, verificationCode});
		String c = (String)(count.get("count"));
		if(Integer.parseInt(c)>0){
			return true;
		}else{
			return false;
		}
		} catch (Exception e) {
		LOGGER_NON_PHI.error("Exception occured while processing isCustomerExist");
		throw new mscriptsException(null, errorSource, errorSeverity, e);
	}

    	
    }
    
    @Override
    public String getdeviceStagingId(String lastName, String mobile, String encryptKey,String verificationCode)throws mscriptsException{
    	String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - getdeviceStagingId";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;		
    	String sqlString = QueryBuilder.GET_DEVICE_STAGING_ID;
    	try{
    		Map map = queryInvoker.invokeQueryMap(sqlString,
    				new Object[] {lastName,encryptKey, mobile, encryptKey,verificationCode});
    			String id =(String) map.get("device_staging_id");
    			return  id;
		}catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while getting deviceStaging id");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}
    }
    

	@Override
	public void updateDeviceStagings(String deviceStaging_id,String token) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateDeviceStagings";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;	
		String sqlString = QueryBuilder.UPDATE_DEVICE_STAGINGS_TOKEN;
		try{
			queryInvoker.updateUsingSqlString(sqlString, new Object[]{token,deviceStaging_id});
		}catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while updating token in deviceStaging");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}
	}

	@Override
	public void updateMigrationList(String deviceStagingId,String fName, String lName, String mobile, String encryptKey)
			throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateMigrationList";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;	
		String sqlString = QueryBuilder.UPDATE_MIGRATIION_LIST;
		try{
			queryInvoker.updateUsingSqlString(sqlString, new Object[]{fName,encryptKey,"ILTM first_name differs ",fName,encryptKey,"ECMJ07 ",lName,encryptKey,mobile,encryptKey,deviceStagingId});
		}catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while updating token in deviceStaging");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}
		
	}

	@Override
	public void updateIltmParam(String fName, String lName, String mobile, String encryptKey,
			String verificationCode,String token) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateMigrationList";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;	
		String sqlString = QueryBuilder.UPDATE_ILTM_PARAMS;
		try{
			queryInvoker.updateUsingSqlString(sqlString, new Object[]{fName,encryptKey,token,lName,encryptKey,mobile ,encryptKey,verificationCode});
		}catch (Exception e) {
			LOGGER_NON_PHI.error("The received ILTM  parameters not matching or device_staging entry not there ");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}
		
	}

	@Override
	public String getKey(String secondaryKey, String clientID) throws mscriptsException {
		Map<String, String> encryptionKeyMap = queryInvoker.invokeQueryMap(QueryBuilder.FN_GET_FINAL_KEY,
				new Object[] { clientID, secondaryKey });
		String finalKey = encryptionKeyMap.get("encryptionKey");
		return finalKey;
	}

 
    public Map getOldAuthCode(String token, String mobile) throws mscriptsException {
        try {
           
            LOGGER_PHI.info("Entered into get getOldAuthCode method for mobile={} " + mobile);
            return queryInvoker.invokeQueryMap(QueryBuilder.GET_OLD_AUTH_CODE, new Object[]{token,mobile});
             
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception occured while getting getOldAuthCode :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-getOldAuthCode", mscriptsExceptionSeverity.High, ex);
        }
    }

 
	@Override
	public boolean isMigratedUser(String lName, String mobile, String verificationCode, String encryptKey)
			throws mscriptsException {
	 	String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - isCustomerExists";
			mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;		
	    	String sqlString = QueryBuilder.IS_MIGRATED_USER;
		try {
			
			Map count=queryInvoker.invokeQueryMap(sqlString,new Object[]{ lName, encryptKey,mobile,encryptKey, verificationCode});
			String c = (String)(count.get("count"));
			if(Integer.parseInt(c)>0){
				return true;
			}else{
				return false;
			}
			} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while processing isCustomerExist");
			throw new mscriptsException(null, errorSource, errorSeverity, e);
		}
	}
	
  
	@Override
	public void updateErrorInMigrationList(String deviceStagingId, String error,String errorCode,String mobile, String encryptKey) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc - updateErrorInMigrationList";
  		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;	
  		String sqlString = QueryBuilder.UPDATE_ERROR_IN_MIGRATION_USERS_LIST;
  		try{
  			queryInvoker.updateUsingSqlString(sqlString, new Object[]{error,errorCode,mobile,encryptKey,deviceStagingId});
  		}catch (Exception e) {
  			LOGGER_NON_PHI.error("Exception occured while updating token in deviceStaging");
  			throw new mscriptsException(null, errorSource, errorSeverity, e);
  		}
		
	}
}
