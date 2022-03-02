/**
 * *****************************************************
 * Title : GeneralServiceImpl.java Author : Sabu Sree Raj Description : Service
 * Implementation of the GeneralService interface Modification History: Not
 * Applicable Created : 18-Jan-10 Modified : Not Applicable Notes : None
 * *****************************************************
 */
package com.mscripts.externalrequesthandler.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.w3c.dom.Document;

import org.json.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.ClientDao;
import com.mscripts.dao.MscriptsCommonDao;
import com.mscripts.dispensing.invocation.domain.Patient;
import com.mscripts.dispensing.invocation.domain.PdxDispensingRequest;
import com.mscripts.dispensing.invocation.domain.PdxDispensingResponse;
import com.mscripts.dispensing.invocation.service.IDispensingPatientService;
import com.mscripts.domain.json.Request;
import com.mscripts.domain.json.Response;
import com.mscripts.externalrequesthandler.dao.GeneralDao;
import com.mscripts.service.MailService;
import com.mscripts.service.MscriptsCommonService;
import com.mscripts.service.SCHService;
import com.mscripts.service.SmsService;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.EncryptionUtil;
import com.mscripts.utils.GenerateRandom;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.MscriptsStringUtils;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.XMLUtils;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;



/**
 *
 * @author ssreeraj
 */
public class GeneralServiceImpl implements GeneralService {

    private GeneralDao generalDao;
    private ClientDao clientDao;
    private MscriptsCommonDao mscriptsCommonDao;
    private DataSourceTransactionManager transactionManager;
    private Properties transactionAttributes;
    private SmsService smsService;
    private MailService mailService;
    private PHICredentials pHICredentials;
    private EncryptionUtil encryptionUtil;
    private MscriptsCommonService mscriptsCommonService;
    private IDispensingPatientService dispensingPatientService;
    /**
     * [BASE-1024] SCH microservice changes
     */
    private SCHService schService;

	private static final Logger LOGGER = LogManager.getLogger(ExternalRequestHandler.class);
    private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + ExternalRequestHandler.class.getName());
    private static final Logger LOGGER_PHI = LogManager.getLogger("phi." +ExternalRequestHandler.class.getName());
    //Method that gets client details based on client name.
    @Override
	public Map getClientDetails(String cvPdxClientName) throws mscriptsException {
        try {
           
                LOGGER_NON_PHI.debug("Entered into getting client details for= {}" ,cvPdxClientName);
          
            return clientDao.getClientDetails(cvPdxClientName);
        } catch (mscriptsException mex) {
            LOGGER_NON_PHI.error(" mscripts Exception occured while getting client details:", mex);
            throw mex;
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("  Exception occured while getting client details:", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-getClientDetails", mscriptsExceptionSeverity.Medium, ex);
        }
    }

    @Override
	public Map getClientDetails(String token, String mobile, String verificationCode, String firstName, String storeID) throws mscriptsException {
        try {
           
            LOGGER_PHI.debug("Entered into getting client details for mobile ={} verificationCode = {} firstName = {} storeID = {}" , mobile,verificationCode,firstName,storeID);
            return generalDao.getClientDetails(token, mobile, verificationCode, firstName, storeID, pHICredentials.getSecondaryKey());
        } catch (mscriptsException mex) {
            LOGGER_NON_PHI.error(" mscripts Exception occured while getting client details:", mex);
            throw mex;
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("  Exception occured while getting client details:", ex);
            throw new mscriptsException(ex.getLocalizedMessage(), "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-getClientDetails", mscriptsExceptionSeverity.Medium, ex);
        }
    }

    //Method that handles PDX initial link token message
    @Override
	public void initialLinkTokenMessage(String mobile, String token, String verificationCode, String action, String fName,
            String lName, String storeID, String timeZone, String clientID, String clientName, String oldToken, String oldMobile,
            String survivingToken, String langCode) throws mscriptsException {
        String errorSource = "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-initialLinkTokenMessage";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        try {

            
        	LOGGER_PHI.debug("Entered into initialLinkToTokenMessage method for mobile ={} verificationCode={}" , mobile,verificationCode);
           
            String cvPdxSignupModeOnphone = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVPDXSIGNUPMODEONPHONE);
            String cvPdxSignupModeStore = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVPDXSIGNUPMODESTORE);
            String cvErrorString = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVERRORSTRING);
            String cvAdmin = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVADMIN);
            String cvErrorCodeMobileExists = ConfigReader.readConfig(clientID, langCode, ConfigKeys.cvErrorCodeMobileExists);
            String cvErrorCodeMobileUnavailable = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVERRORCODEMOBILEUNAVAILABLE);
            String cvErrorCodeShortCode = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVERRORCODESHORTCODE);
           // String cvComNameWelcomeAccount = ConfigReader.readConfig(clientID, langCode, ConfigKeys.cvComNameWelcomeAccount);
            String cvComNameDeviceVerification = ConfigReader.readConfig(clientID, langCode,"cvComNameDeviceVerification");
            String cvPdxILTMModeTeardown = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVPDXILTMMODETEARDOWN);
            String cvPdxILTMModeSetup = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVPDXILTMMODESETUP);
            
            String cvSCHandlerGetSCMethod = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERGETSCMETHOD);
            String cvSCHandlerDelSCMethod = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERDELSCMETHOD);

            String cvSCHandlerGetSCRequest1 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERGETSCREQUEST1);
            String cvSCHandlerGetSCRequest2 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERGETSCREQUEST2);
            String cvSCHandlerGetSCRequest3 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERGETSCREQUEST3);
            String cvSCHandlerGetSCRequest4 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERGETSCREQUEST4);

            String cvSCHandlerDelSCRequest1 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERDELSCREQUEST1);
            String cvSCHandlerDelSCRequest2 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERDELSCREQUEST2);
            String cvSCHandlerDelSCRequest3 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERDELSCREQUEST3);
            String cvSCHandlerDelSCRequest4 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERDELSCREQUEST4);
            
            String cvSCHandlerUrl = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERURL);
            String cvSCHandlerProtocol = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVSCHANDLERPROTOCOL);
            
            String cvOPHandlerUrl = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERURL);
            String cvOPHandlerProtocol = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERPROTOCOL);
            
            String cvOPHandlerUserSignupMethod = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERSIGNUPMETHOD);
            String cvOPHandlerUserSignupRequest1 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERSIGNUPREQUEST1);
            String cvOPHandlerUserSignupRequest2 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERSIGNUPREQUEST2);
            String cvOPHandlerUserSignupRequest3 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERSIGNUPREQUEST3);
            String cvOPHandlerUserSignupRequest4 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERSIGNUPREQUEST4);
            String cvMobileInternationalCode = ConfigReader.readConfig(clientID, langCode, "cvMobileInternationalCode"); 
            String cvPdxErrorDescriptionCommunicationConsentIsNo = ConfigReader.readConfig(clientID, langCode, "cvPdxErrorDescriptionCommunicationConsentIsNo");
            String cvPdxErrorDescriptionPatientQueryResponseReturnedNone = ConfigReader.readConfig(clientID, langCode, "cvPdxErrorDescriptionPatientQueryResponseReturnedNone");
            String cvPdxErrorDescriptionPatientQueryResponseReturnedMany = ConfigReader.readConfig(clientID, langCode, "cvPdxErrorDescriptionPatientQueryResponseReturnedMany");            
            String cvPdxErrorString = ConfigReader.readConfig(clientID, langCode, "cvPdxErrorString");
            String cvIsTearDownMessageEnabled= ConfigReader.readConfig(clientID, langCode, "cvIsTearDownMessageEnabled"); 
            String cvComNameTearDownMessage = ConfigReader.readConfig(clientID, langCode,"cvComNameTearDownMessage");
            String cvUseSCHMicroService = ConfigReader.readConfig(clientID, langCode, Constants.CV_USE_SCH_MICROSERVICE);
            //Disabling text message feature - MCE-2036
			String isTextMessagingEnabled = ConfigReader.readConfig(clientID, langCode,Constants.IS_TEXTMESSAGING_ENABLED);
            
            String secondaryKey = pHICredentials.getSecondaryKey();
            Document scHandlerResponseDoc = null;
            String shortCode = null;
            String shortCodeUserName = null;
            String shortCodeServiceID = null;
            String prefix = null;
            String scHandlerErrorMessage = null;
            String scHandlerErrorID = null;
            String scHandlerErrorCode = null;
            String scHandlerRequest = null;
			boolean isFromMigration = false;
            boolean sendSmS = true;
            boolean isTearDown=false;
            String deviceStagingId = null;
            String encryptKey = null;
            String msiLogId = new GenerateRandom().generateSessionID(String.valueOf(clientID + lName + mobile + verificationCode));
            
            if (action.compareToIgnoreCase(cvPdxILTMModeSetup) == 0) {

				// if store id null not store user 
				// it can be app user
				if (storeID == null || storeID.isEmpty()|| (storeID.compareToIgnoreCase("99992233")==0)) {
					LOGGER_NON_PHI.debug("Customer from migration_users_list received and his storeId null");
					sendSmS = false;
					
					// getting encryption key
					try {
						encryptKey = generalDao.getKey(secondaryKey, clientID);
					} catch (mscriptsException e) {
						LOGGER_NON_PHI.error("error while getting encryptKey ");
						throw e;
					}

					// check entry present in customers table
					//if yes it can be app user
					try {
						// true if entry in customer and device table present
						boolean isCustomerExist = generalDao.isCustomerExist( lName, mobile, verificationCode,
								encryptKey);

						
						if (!isCustomerExist) {
							//check entry present in migration list
							try{
								boolean isMigratedUser = generalDao.isMigratedUser(lName, mobile, verificationCode,
										encryptKey);
								// if migratedUser true initiate migration flow
								if (isMigratedUser) {
									isFromMigration = true;

									// when firstname in our system is different
									// than
									// that of iltm but all other parameter
									// matches
									// update deviceStaging entry
									try {
										generalDao.updateIltmParam(fName, lName, mobile, encryptKey, verificationCode,
												token);
									} catch (mscriptsException e) {
										LOGGER_NON_PHI.error(
												"The received ILTM  parameters not matching or device_staging entry not there ");
										throw e;
									}

									try {
										LOGGER_NON_PHI.info("initialLinkTokenMessage - getting device staging id");
										deviceStagingId = generalDao.getdeviceStagingId(lName, mobile, encryptKey,
												verificationCode);
									} catch (mscriptsException ex) {
										LOGGER_NON_PHI.error("error occured while  getting device staging id in iltm ");
										throw ex;
									}

									// update the error notes in migration user
									// list set
									// null if token received for that user
									try {
										generalDao.updateMigrationList(deviceStagingId, fName, lName, mobile,
												encryptKey);
									} catch (mscriptsException e) {
										throw e;
									}
								}
							} catch (mscriptsException mex) {
								LOGGER_NON_PHI.error("error occured while checking migrated_user or not");
								throw mex;
							}
							


						} else {
							LOGGER_NON_PHI.debug("CUSTOMER ALREADY EXISTS CANNOT ENTER MIGRATION FLOW");
						}
					} catch (mscriptsException e) {
						throw e;
					}

				}
				//Disabling text message feature - MCE-2036
				else if(Constants.NUMERIC_FALSE_STRING.equals(isTextMessagingEnabled)) {
					LOGGER_NON_PHI.info("ILTM not processed as text messaging is disabled for the client");
					return;
				}
            	
//                try {	
//
//            		LOGGER_NON_PHI.info("initialLinkTokenMessage - checking if the ILTM is for a migrated user");
//                    isMigratedUserDetails = generalDao.checkIsMigratedUser(clientID, mobile, fName, lName, verificationCode, secondaryKey);
//
//                } catch (mscriptsException e) {
//                    //throw e;
//                }
				
				if ((ConfigReader.readConfig(clientID, langCode, "cvYEnrollmentViaMicroServices").equals("0"))&&!isFromMigration){
					
					/**
					 * [BASE-1024] SCH microservice integration changes
					 */
					if(Constants.NUMERIC_TRUE_STRING.equals(cvUseSCHMicroService)) {
						
						/**
						 * Invoking SCH microservice
						 */
						LOGGER_NON_PHI.debug("Invoking SCH microservice");
						
						JSONObject responseObject = schService.invokeShortCodeService(clientID, langCode, msiLogId,
								mobile, verificationCode, cvSCHandlerGetSCMethod);
						
						if(Constants.HTTP_STATUS_CODE_SUCCESS.equalsIgnoreCase(responseObject.getString("code"))) {
							org.json.JSONObject data = responseObject.getJSONObject("data");
							shortCode = data.getString("shortcode");
							shortCodeUserName = data.getString("username");
							shortCodeServiceID = data.getString("serviceId");
							prefix = data.getString("enablePrefix");
						} else {
							scHandlerErrorID = responseObject.getString("error_id");
							scHandlerErrorCode = responseObject.getString("errorCode");
						}
					} else {
					
	            		scHandlerRequest = cvSCHandlerGetSCRequest1 + clientName + cvSCHandlerGetSCRequest2 + mobile + cvSCHandlerGetSCRequest3 + verificationCode + cvSCHandlerGetSCRequest4;
	                    try {
	                        scHandlerResponseDoc = invokeHandler(cvSCHandlerGetSCMethod, cvSCHandlerProtocol,
	                                cvSCHandlerUrl, scHandlerRequest, clientID);
	                    } catch (mscriptsException mEx) {
	                        // Retry logic if any
	                        throw mEx;
	                    }
	                    scHandlerErrorMessage = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/error/errormessage");
	                    if (scHandlerErrorMessage.equals("")) {
	                        shortCode = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/shortcode");
	                        shortCodeUserName = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/username");
	                        shortCodeServiceID = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/serviceid");
	                        prefix = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/prefix");
	                    } else {
	                        scHandlerErrorID = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/error/errorid");
	                        scHandlerErrorCode = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/error/errorcode");
	                    }
					}
            	}
            } else if (action.compareToIgnoreCase(cvPdxILTMModeTeardown) == 0) {
				// work around for pdx teardown is coming with new authcode,
				// we may have to remove this code and retain only else part
				Map getOldAuthCodeMap = null;
				String oldAuthCodeMap = null;
				String verificationCodeForTearDown = null;
				try {
					getOldAuthCodeMap = generalDao.getOldAuthCode(token, mobile);
					oldAuthCodeMap = (String) getOldAuthCodeMap.get("verification_code");
				} catch (mscriptsException mEx) {
					// throw mEx;
				}
				
				verificationCodeForTearDown = (getOldAuthCodeMap != null && (!oldAuthCodeMap.equals(verificationCode))) ? oldAuthCodeMap : verificationCode;
				
				
				LOGGER_PHI.debug("Sending the request to SCH for teardown."
						+ "Clientname : {}, mobile : {}, verificationCodeForTearDown : {}", clientName, mobile, verificationCodeForTearDown);
				
				/**
				 * [BASE-1024] SCH microservice integration changes
				 */
				if(Constants.NUMERIC_TRUE_STRING.equals(cvUseSCHMicroService)) {
					
					/**
					 * Invoking SCH microservice
					 */
					LOGGER_NON_PHI.debug("Invoking SCH microservice");
					
					JSONObject responseObject = schService.invokeShortCodeService(clientID, langCode, msiLogId, mobile,
							verificationCodeForTearDown, cvSCHandlerDelSCMethod);
					
					if(!Constants.HTTP_STATUS_CODE_SUCCESS.equalsIgnoreCase(responseObject.getString("code"))) {
						scHandlerErrorID = responseObject.getString("error_id");
						scHandlerErrorCode = responseObject.getString("errorCode");
					}
				} else {
					
					scHandlerRequest = cvSCHandlerDelSCRequest1 + clientName + cvSCHandlerDelSCRequest2 + mobile
							+ cvSCHandlerDelSCRequest3 + MiscUtils.sanatizeString(verificationCodeForTearDown, false) + cvSCHandlerDelSCRequest4;
	                try {
	                    scHandlerResponseDoc = invokeHandler(cvSCHandlerDelSCMethod, cvSCHandlerProtocol, cvSCHandlerUrl, scHandlerRequest,clientID);
	                } catch (mscriptsException mEx) {
	                    // Retry logic if any
	                    throw mEx;
	                }
	                scHandlerErrorMessage = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/teardowncustomershortcode/error/errormessage");
	                if (!scHandlerErrorMessage.equals("")) {
	                    scHandlerErrorID = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/teardowncustomershortcode/error/errorid");
	                    scHandlerErrorCode = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/teardowncustomershortcode/error/errorcode");
	                }
				}
            } 
			// if isFromMigration =1 and no entry in customers(i.e no mobile
			// number update) start migration flow
			if (isFromMigration) {

				// if no device stagingId then throw exception.
				if (deviceStagingId == null) {
					throw new mscriptsException(clientID, " parameter Missing - no deviceStagingId  ",
							mscriptsExceptionSeverity.High, new Exception("Invalid Request - parmeter Missing"));
				}
				// getting microservice dependencies
				String cvUserSignupProtocol = ConfigReader.readConfig(clientID, langCode, "cvUserSignupProtocol");
				String cvUserSignupURL = ConfigReader.readConfig(clientID, langCode, "cvUserSignupURL");
				String microServiceSignupMethod = ConfigReader.readConfig(clientID, langCode,
						"microServiceSignupMethod");
				String microServiceVersion = ConfigReader.readConfig(clientID, langCode, "microServiceVersion");
				String microServiceAuthToken = ConfigReader.readConfig(clientID, langCode, "microServiceAuthToken");
				// forming request
				Request request = new Request();
				request.setLang(langCode);
				request.setMsi_log_id(new GenerateRandom().generateSessionID(String.valueOf(deviceStagingId)));
				request.setVersion(microServiceVersion);

				Map requestData = new HashMap();
				requestData.put("deviceStagingsID", deviceStagingId);
				requestData.put("fromMigration", isFromMigration);

				ObjectMapper objectMapper = new ObjectMapper();
				JsonParser parser = new JsonParser();
				String dataNodeString = objectMapper.writeValueAsString(requestData);
				JsonObject jsonData = (JsonObject) parser.parse(dataNodeString);
				@SuppressWarnings("rawtypes")
				List jsonNodeList = new ArrayList();
				jsonNodeList.add(jsonData);
				request.setData(jsonNodeList);

				Response response = invokeUserSignupMicroservice(
						cvUserSignupProtocol + cvUserSignupURL + microServiceSignupMethod, request,
						microServiceAuthToken);

				if (!response.getCode().equals(Constants.HTTP_STATUS_CODE_SUCCESS)) {
					// throw exception
					LOGGER_NON_PHI.error("Exception occured in microservice Error code={}", response.getErrorCode());
					//update error in migration users list
					generalDao.updateErrorInMigrationList(deviceStagingId,
							"Exception occured in microservice Error code= " + response.getErrorCode(),response.getErrorCode(),mobile,encryptKey);
					throw new mscriptsException(response.getErrorCode(), errorSource, errorSeverity,
							new Exception("Error occured in microservice:" + response.getErrorCode()));
				}
			}else if(!isFromMigration){

            	Map map = generalDao.initialLinkTokenMessage(mobile, token, verificationCode, action,
                        fName, lName, storeID, timeZone, cvPdxSignupModeOnphone, cvPdxSignupModeStore,
                        cvPdxILTMModeTeardown, cvPdxILTMModeSetup, cvAdmin, clientID, oldToken,
                        oldMobile, survivingToken, shortCode, shortCodeUserName, shortCodeServiceID, scHandlerErrorCode, prefix, secondaryKey, langCode);
                
                LOGGER_PHI.debug("Map obtained from sp_ERH_pdxILTM = {}",map);
                int result = Integer.parseInt(map.get("result").toString());
                if (map.containsKey("mobileNumber")) {
                    mobile = map.get("mobileNumber").toString();
                   
                }

                String tenDigitMobile=(mobile.length()) > 10 ? mobile.substring(1) : mobile;
				

				if (action.compareToIgnoreCase(cvPdxILTMModeSetup) == 0 && ConfigReader.readConfig(clientID, langCode, "cvYEnrollmentViaMicroServices").equals("1") && result !=2) {
					
					/**
					 * [BASE-1024] SCH microservice integration changes
					 */
					if(Constants.NUMERIC_TRUE_STRING.equals(cvUseSCHMicroService)) {
						
						/**
						 * Invoking SCH microservice
						 */
						LOGGER_NON_PHI.debug("Invoking SCH microservice");
						
						JSONObject responseObject = schService.invokeShortCodeService(clientID, langCode, msiLogId,
								tenDigitMobile, verificationCode, cvSCHandlerGetSCMethod);
						
						if(Constants.HTTP_STATUS_CODE_SUCCESS.equalsIgnoreCase(responseObject.getString("code"))) {
							JSONObject data = responseObject.getJSONObject("data");
							shortCode = data.getString("shortcode");
							shortCodeUserName = data.getString("username");
							shortCodeServiceID = data.getString("serviceId");
							prefix = data.getString("enablePrefix");
						} else {
							scHandlerErrorID = responseObject.getString("error_id");
							scHandlerErrorCode = responseObject.getString("errorCode");
						}
					} else {
						scHandlerRequest = cvSCHandlerGetSCRequest1 + clientName + cvSCHandlerGetSCRequest2 + tenDigitMobile
											+ cvSCHandlerGetSCRequest3 + verificationCode + cvSCHandlerGetSCRequest4;
						try {
							scHandlerResponseDoc = invokeHandler(cvSCHandlerGetSCMethod, cvSCHandlerProtocol,
									cvSCHandlerUrl, scHandlerRequest, clientID);
						} catch (mscriptsException mEx) {
							// Retry logic if any
							throw mEx;
						}
						scHandlerErrorMessage = XMLUtils.getNodeValue(scHandlerResponseDoc,
								"//response/getsmsshortcode/error/errormessage");
						if (scHandlerErrorMessage.equals("")) {
							shortCode = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/shortcode");
							shortCodeUserName = XMLUtils.getNodeValue(scHandlerResponseDoc,
									"//response/getsmsshortcode/username");
							shortCodeServiceID = XMLUtils.getNodeValue(scHandlerResponseDoc,
									"//response/getsmsshortcode/serviceid");
							prefix = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/getsmsshortcode/prefix");
							
						} else {
							scHandlerErrorID = XMLUtils.getNodeValue(scHandlerResponseDoc,
									"//response/getsmsshortcode/error/errorid");
							scHandlerErrorCode = XMLUtils.getNodeValue(scHandlerResponseDoc,
									"//response/getsmsshortcode/error/errorcode");
						}										
					}
					
					String enable_prefix = "0";
					if(!prefix.isEmpty()){
						enable_prefix ="1";
					}
					generalDao.updateDeviceStagings(shortCode, shortCodeUserName, shortCodeServiceID, enable_prefix, token);
				}
                switch (result) {
                    case 0:// On Phone ILTM Success
                        String varIsMobileAccessNumber = map.get("varIsMobileAccessNumber").toString();
                        if (varIsMobileAccessNumber.equals("1")) {
                            String opHandlerRequest = cvOPHandlerUserSignupRequest1 + clientName
                                    + cvOPHandlerUserSignupRequest2 + verificationCode + cvOPHandlerUserSignupRequest3
                                    + tenDigitMobile + cvOPHandlerUserSignupRequest4;
                            try {
                                scHandlerResponseDoc = invokeHandler(cvOPHandlerUserSignupMethod, cvOPHandlerProtocol,
                                        cvOPHandlerUrl, opHandlerRequest,clientID);
                            } catch (mscriptsException mEx) {
                                LOGGER_NON_PHI.error(" mscripts Exception occured while processing initialLinkToTokenMessage method :", mEx);
                                throw mEx;
                            }
                            String opHandlerErrorMessage = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/usersignup/error/errormessage");
                            if (!opHandlerErrorMessage.equals("")) {
                                String opHandlerErrorID = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/usersignup/error/errorid");
                                // Retry logic if any
                                LOGGER_NON_PHI.error("throwing mscripts exception as opHandlerErrorMessage is not null in initialLinkToTokenMessage method");
                                throw new mscriptsException(null, errorSource, errorSeverity, new Exception("OPHandlerErrorID-" + opHandlerErrorID + "-" + opHandlerErrorMessage));
                            }
                            sendSmS = false;
                        }
                        break;
                    case 1://[RNH] -- Temporary Fix for Multiple ILTM Fix. Once PDX fixes this issue this code need to be removed.
                        String opHandlerRequest = cvOPHandlerUserSignupRequest1 + clientName
                                + cvOPHandlerUserSignupRequest2 + verificationCode + cvOPHandlerUserSignupRequest3
                                + cvMobileInternationalCode + tenDigitMobile + cvOPHandlerUserSignupRequest4;
                        try {
                            scHandlerResponseDoc = invokeHandler(cvOPHandlerUserSignupMethod, cvOPHandlerProtocol,
                                    cvOPHandlerUrl, opHandlerRequest,clientID);
                        } catch (mscriptsException mEx) {
                            LOGGER_NON_PHI.error(" mscripts Exception occured while processing initialLinkToTokenMessage method :", mEx);
                            throw mEx;
                        }
                        String opHandlerErrorMessage = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/usersignup/error/errormessage");
                        if (!opHandlerErrorMessage.equals("")) {
                            String opHandlerErrorID = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/usersignup/error/errorid");
                            // Retry logic if any
                            LOGGER_NON_PHI.error("throwing mscripts exception as opHandlerErrorMessage is not null in initialLinkToTokenMessage method");
                            throw new mscriptsException(null, errorSource, errorSeverity, new Exception("OPHandlerErrorID-" + opHandlerErrorID + "-" + opHandlerErrorMessage));
                        }
                        // Do not send SMS
                        sendSmS = false;
                        break;
                    case 3:// Mobile Number already registered. Tear down new token
                        sendSmS = false;
                        try {
                            sendStoreSignupFailureEmail(map, mobile, fName, lName, storeID, clientID);
                        } catch (Exception ex) {
                            // Email failed..do nothing.
                            LOGGER_NON_PHI.error(" Exception occured while sending email in initialLinkToTokenMessage method :", ex);
                        }
                        throw new mscriptsException(cvErrorCodeMobileExists, errorSource, errorSeverity, new Exception("scHandlerErrorID-" + scHandlerErrorID + "-" + scHandlerErrorMessage));
                    case 4:// Phone number update by unverified user
                    	if(cvIsTearDownMessageEnabled.compareTo("1") == 0){
                    		sendSmS=true;
                    		isTearDown=true;
                    		
                    	}else{
                    		sendSmS = false;
                    	}
                        break;
                    case 5:// Token Teardown
                        sendSmS = false;
                        break;
                    case 6:// Mobile number registered with one of the client.
                        sendSmS = false;
                        try {
                            sendStoreSignupFailureEmail(map, mobile, fName, lName, storeID, clientID);
                        } catch (Exception ex) {
                            // Email failed..do nothing.
                            LOGGER_NON_PHI.error(" Exception occured while sending email in initialLinkToTokenMessage method :", ex);
                        }
                        throw new mscriptsException(cvErrorCodeMobileUnavailable, errorSource, errorSeverity, new Exception("scHandlerErrorID-" + scHandlerErrorID + "-" + scHandlerErrorMessage));
                    case 7: // Update the token donot send sms as verification code has not changed.
                        sendSmS = false;
                        break;
                    case 8:// Mobile number registered with one of the client.
                        sendSmS = false;
                        try {
                            sendStoreSignupFailureEmail(map, mobile, fName, lName, storeID, clientID);
                        } catch (Exception ex) {
                            // Email failed..do nothing.
                            LOGGER_NON_PHI.error(" Exception occured while sending email in initialLinkToTokenMessage method :", ex);
                        }
                        throw new mscriptsException(cvErrorCodeShortCode, errorSource, errorSeverity, new Exception("scHandlerErrorID-" + scHandlerErrorID + "-" + scHandlerErrorMessage));
                    case 9:// Update the token for new user in the devices table.
                        sendSmS = false;
                        String cvOPHandlerUserTokenLinkConfirmRequest1 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERTOKENLINKCONFIRMREQUEST1);
                        String cvOPHandlerUserTokenLinkConfirmRequest2 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERTOKENLINKCONFIRMREQUEST2);
                        String cvOPHandlerUserTokenLinkConfirmRequest3 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERTOKENLINKCONFIRMREQUEST3);
                        String cvOPHandlerUserTokenLinkConfirmRequest4 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERTOKENLINKCONFIRMREQUEST4);
                        String cvOPHandlerUserTokenLinkConfirmRequest5 = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERTOKENLINKCONFIRMREQUEST5);
                        String opHandlerUserTokenLinkConfirmRequest = cvOPHandlerUserTokenLinkConfirmRequest1 + clientName
                                + cvOPHandlerUserTokenLinkConfirmRequest2 + verificationCode + cvOPHandlerUserTokenLinkConfirmRequest3
                                + tenDigitMobile + cvOPHandlerUserTokenLinkConfirmRequest4 + token + cvOPHandlerUserTokenLinkConfirmRequest5;
                        try {
                            String cvOPHandlerUserTokenLinkConfirmMethod = ConfigReader.readConfig(clientID, langCode, ConfigKeys.CVOPHANDLERUSERTOKENLINKCONFIRMMETHOD);
                            scHandlerResponseDoc = invokeHandler(cvOPHandlerUserTokenLinkConfirmMethod, cvOPHandlerProtocol,
                                    cvOPHandlerUrl, opHandlerUserTokenLinkConfirmRequest,clientID);
                        } catch (mscriptsException mEx) {
                            // Retry logic if any
                            LOGGER_NON_PHI.error(" mscripts excetion caught while retry logic :", mEx);
                            throw mEx;
                        }
                        String opHandlerUserTokenLinkConfirmErrorMessage = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/usersignup/error/errormessage");
                        if (!opHandlerUserTokenLinkConfirmErrorMessage.equals("")) {
                            String opHandlerErrorID = XMLUtils.getNodeValue(scHandlerResponseDoc, "//response/usersignup/error/errorid");
                            LOGGER_NON_PHI.error(" mscripts excetion thrown asopHandlerUserTokenLinkConfirmErrorMessage is not empty");
                            throw new mscriptsException(null, errorSource, errorSeverity, new Exception("OPHandlerErrorID-" + opHandlerErrorID + "-" + opHandlerUserTokenLinkConfirmErrorMessage));
                        }
                        String customerID = map.get("customerID").toString();
                        generalDao.updateSmsTokenFlag("1", customerID, clientID);
					case 2:// Instore signup / instore phone number edit flow

					if (ConfigReader.readConfig(clientID, langCode, "cvYEnrollmentViaMicroServices").equals("1") && map.containsKey("deviceStagingsID") && map.get("deviceStagingsID") != null) {
						sendSmS = false;
						String deviceStagingsID = map.get("deviceStagingsID").toString();
						generalDao.updateCustomerSignupStore(deviceStagingsID);
						String cvUserSignupProtocol = ConfigReader.readConfig(clientID, langCode, "cvUserSignupProtocol");
						String cvUserSignupURL = ConfigReader.readConfig(clientID, langCode, "cvUserSignupURL");
						String microServiceSignupMethod = ConfigReader.readConfig(clientID, langCode,
								"microServiceSignupMethod");
						String microServiceVersion = ConfigReader.readConfig(clientID, langCode, "microServiceVersion");
						String microServiceAuthToken = ConfigReader.readConfig(clientID, langCode,
								"microServiceAuthToken");

						Request request = new Request();
						request.setLang(langCode);
						request.setMsi_log_id(new GenerateRandom().generateSessionID(deviceStagingsID));
						request.setVersion(microServiceVersion);
						
						Map requestData = new HashMap();
						requestData.put("deviceStagingsID", deviceStagingsID);
						
						ObjectMapper objectMapper = new ObjectMapper();
						JsonParser parser = new JsonParser();		
						String dataNodeString = objectMapper.writeValueAsString(requestData);				
						JsonObject jsonData = (JsonObject) parser.parse(dataNodeString);
						@SuppressWarnings("rawtypes")
						List jsonNodeList = new ArrayList();
				    	jsonNodeList.add(jsonData);
				    	request.setData(jsonNodeList);

						Response response = invokeUserSignupMicroservice(cvUserSignupProtocol+cvUserSignupURL + microServiceSignupMethod,
								request, microServiceAuthToken);

						if (!response.getCode().equals(Constants.HTTP_STATUS_CODE_SUCCESS)) {
							// throw exception
							LOGGER_NON_PHI.error("Exception occured in microservice Error code={}",response.getErrorCode());
							throw new mscriptsException(response.getErrorCode(), errorSource, errorSeverity, new Exception("Error occured in microservice:"+response.getErrorCode()));
						}
					}

				}
                
            if (sendSmS) {
                try {
                    // Send SMS
                	Map urlMap = generalDao.fetchTOSUrl(clientID);
                	String tosUrl = urlMap.get("url").toString();
                    String customerID = map.get("customerID").toString();
                	String[] smscontents;
                	String communicationName=null;
                    if(cvIsTearDownMessageEnabled.compareTo("1") == 0 && isTearDown){
                    	String[] tempSmsContents = {ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvClientName"),fName,ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvClientHelpUrl")};
                    	smscontents=tempSmsContents;
                    	communicationName=cvComNameTearDownMessage;      
                    	mobile=(mobile.length()) ==10 ? "1"+mobile : mobile;
                    }
                    else{
                    	String[] tempSmsContents = {ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvClientName"),fName,tosUrl};
                    	smscontents=tempSmsContents;
                    	communicationName=cvComNameDeviceVerification;
                    }
                    //mce-1835 code changes 
                    //check if user has given his consent for text messaging
                    boolean isConsentGiven = false;
                    String cvIsPatientCommunicationConsentRequiredForText = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvIsPatientCommunicationConsentRequiredForText");
                    if(MscriptsStringUtils.compareStringEquals(cvIsPatientCommunicationConsentRequiredForText, Constants.NUMERIC_TRUE_STRING)){
                    	//fetch rxComId from search patient
                    	String cvPdxPatientQueryService = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvPdxPatientQueryService");
                    	String cvVendorNhinId = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvVendorNhinId");
                    	PdxDispensingRequest patientQueryRequest = new PdxDispensingRequest();
                    	Map clientServiceUrls = clientDao.getServiceUrls(clientID);

                    	patientQueryRequest.setServiceUrl((String) clientServiceUrls.get(cvPdxPatientQueryService));
                    	patientQueryRequest.setChainId((String) clientServiceUrls.get("chain_id"));
                    	patientQueryRequest.setFlowPos((String) clientServiceUrls.get("flow_pos"));
                    	patientQueryRequest.setVendorNhinId(cvVendorNhinId);

                    	patientQueryRequest.setFirstName(fName.toUpperCase());
                    	patientQueryRequest.setLastName(lName.toUpperCase());
                    	patientQueryRequest.setClientId(clientID);
                    	patientQueryRequest.setLanguage(Constants.DEFAULT_LANGUAGE);

                    	// Invoke PatientQueryService API to get RxComId by using
                    	// firstName, lastName and phoneNumber search.
                    	patientQueryRequest.setMobileNumber(tenDigitMobile);                    	
                    	PdxDispensingResponse patientQueryResponse = (PdxDispensingResponse) dispensingPatientService
                    			.getPatientQueryService(patientQueryRequest, null);

                    	List<Patient> patientQueryList =  patientQueryResponse.getPatientList();
                    	
                    	if(1!=patientQueryList.size()) {
                    		//handle error condition - Log error and throw exception
                    		if(0==patientQueryList.size()) {
                    			throw new mscriptsException(cvPdxErrorString, errorSource, errorSeverity,
    									new Exception(cvPdxErrorDescriptionPatientQueryResponseReturnedNone));
                    		}else {
                    			throw new mscriptsException(cvPdxErrorString, errorSource, errorSeverity,
    									new Exception(cvPdxErrorDescriptionPatientQueryResponseReturnedMany));
                    		}
                    	}
                    	
                    	isConsentGiven = mscriptsCommonService.isNonRegisteredPatientCommunicationConsentSatisfied(clientID, customerID, 
    							fName, lName, patientQueryList.get(0).getDob(), patientQueryList.get(0).getPrimaryIdentifier(), secondaryKey);
                    	
                    }else {
                    	isConsentGiven = true; //Set to true because 'cvIsPatientCommunicationConsentRequiredForText' = '0' (false)
                    }
                    
						if (isConsentGiven && Constants.NUMERIC_TRUE_STRING.equals(isTextMessagingEnabled)) {
							sendMessage(mobile, clientID, communicationName, smscontents, null, false, false,
									customerID, shortCode, shortCodeUserName, shortCodeServiceID, prefix);
						}else {
							throw new mscriptsException(cvPdxErrorString, errorSource, errorSeverity,
									new Exception("-".concat("communicationConsent").concat("-").concat(cvPdxErrorDescriptionCommunicationConsentIsNo)));
						}
						
                } catch (mscriptsException mEx) {
                    LOGGER_NON_PHI.error(" Exception occured while sending sms in initialLinkToTokenMessage method :", mEx);
                    try {
                        String errorMessage = mEx.getErrorMessage();
                        if (errorMessage != null && errorMessage.contains(cvErrorString) && Constants.NUMERIC_TRUE_STRING.equals(isTextMessagingEnabled)) {
                            sendStoreSignupFailureEmail(map, mobile, fName, lName, storeID, clientID);
                        }
                    } catch (Exception ex) {
                        // Email failed..do nothing.
                        LOGGER_NON_PHI.error(" Exception occured while sending sms in initialLinkToTokenMessage method :", ex);
                    }
                    throw mEx;
					}
                }
            }
        } catch (mscriptsException mex) {
            LOGGER_NON_PHI.error(" mscripts Exception thrown  :", mex);
            throw mex;
        } catch (Exception ex) {
            LOGGER_NON_PHI.error(" Exception occured while initialising link to token messages  :", ex);

            throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
        }
    }
		private Response invokeUserSignupMicroservice(String microServiceUrl, Request request, String authToken)
			throws mscriptsException, IOException {
		String errorSource = "com.mscripts.externalrequesthandler.service-invokeUserSignupMicroservice";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		String resp = null;
		String returnResponse = "";
		BufferedReader in = null;
		try {

			URL url = new URL(microServiceUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("authToken", authToken);
			conn.setRequestProperty("content-type", "application/json");
			conn.setConnectTimeout(60000);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			
			String requestString = request.toString();
			JSONObject requestJsonObject = new JSONObject(requestString);
			String postRequest = requestJsonObject.toString();
			
			out.write(postRequest);
			out.flush();
			out.close();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			while ((resp = in.readLine()) != null) {
				returnResponse += resp;
			}
			LOGGER_NON_PHI.trace("Response from microservice ={} ", returnResponse);

			Response response = parseMicroserviceResponse(returnResponse);

			return response;
		} catch (mscriptsException mex) {
			LOGGER_NON_PHI.error("Exception occured while invoking micrcoservice ={}", mex.getMessage());
			throw mex;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while invoking micrcoservice ={}", ex.getMessage());

			throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
		} finally {
			if (in != null) {
				in.close();
			}
		}

	}

	private Response parseMicroserviceResponse(String responseString) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-parseMicroserviceResponse";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		ObjectMapper responseMapper = new ObjectMapper();
		try {
			responseMapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
			responseMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
			Response response = responseMapper.readValue(responseString, Response.class);
			return response;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while parsing micrcoservice response ={}", ex.getMessage());
			throw new mscriptsException(null, errorSource, errorSeverity, ex);
		}
	}
	private Document invokeNewMigrationUtility(JSONObject requestToMigrationUtility) throws mscriptsException {
        String errorSource = "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-invokeNewMigrationUtility";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
        Socket client = null;
        DataOutputStream out=null;
        OutputStream outToServer=null;
        InputStream inFromServer=null;
        try {
            String host =requestToMigrationUtility.get("utilityHost").toString();
            int port = Integer.parseInt(requestToMigrationUtility.get("utilityPort").toString());
            InetAddress address = InetAddress.getByName(host);
            client = new Socket(address, port);

             outToServer = client.getOutputStream();
             out = new DataOutputStream(outToServer);

            out.writeUTF(requestToMigrationUtility.toString());
             inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            
            String message=in.readUTF();
           
            client.close();
            return XMLUtils.createXMLDocument(message);
            //System.out.println("Message received from the server : " +message);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER_NON_PHI.error("Exception  thrown while establishing connection at invoking handler", e);
            throw new mscriptsException(e.getMessage(), errorSource, errorSeverity, e);
        } finally {
            //Closing the socket
            try {
                if(client!=null)
                    client.close();
                if(out!=null)
                	out.close();
                if(outToServer!=null)
                	outToServer.close();
                if(inFromServer!=null)
                	inFromServer.close();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER_NON_PHI.error("Exception  thrown while establishing connection at invoking handler", e);
                throw new mscriptsException(e.getMessage(), errorSource, errorSeverity, e);
            }
        }
    }
    public void sendStoreSignupFailureEmail(Map map, String mobile, String fName, String lName, String storeID, String clientID) throws mscriptsException {
        String errorSource = "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-sendStoreSignupFailureEmail";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

     
            LOGGER_PHI.info("Entered into sending store sign up failure email method for mobile ={}" , mobile);
        
        try {
            String cvMsgTypeEmail = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgTypeEmail");
            String cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvMsgStatus");
            String cvAdmin = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvAdmin");
            String secondaryKey = pHICredentials.getSecondaryKey();

            Map templateFieldValues = new HashMap();
            String[] newEmailContentsArray, emailTemplateArray;
            String emailReceiverPrimary = (String) map.get("email_primary");
            if (!emailReceiverPrimary.equals("")) {
                String emailTemplate = (String) map.get("email_body");
                String emailReceiversAdditionalCSV = (String) map.get("email_additional");
                String emailSubject = (String) map.get("email_subject");
                String emailSender = (String) map.get("email_from");
                String commID = (String) map.get("varCommID");
                String storeName = (String) map.get("varStoreName");
                String emailContent;
                String[] emailCC = null;
                newEmailContentsArray = this.smsService.getSmsContent(emailTemplate);

                templateFieldValues.put("mobile_number", mobile);
                templateFieldValues.put("first_name", fName);
                templateFieldValues.put("last_name", lName);
                templateFieldValues.put("store_ncpdp", storeID);
                templateFieldValues.put("store_name", storeName);
                templateFieldValues.put("error_details", map.get("signup_error_details"));
                for (int j = 0; j < newEmailContentsArray.length; j++) {
                    newEmailContentsArray[j] = templateFieldValues.get(newEmailContentsArray[j]).toString();
                }
                emailTemplateArray = emailTemplate.split("<");
                emailContent = this.mailService.frameEmailContent(emailTemplateArray, newEmailContentsArray);
                if (emailReceiversAdditionalCSV.length() == 0) {
                    emailReceiversAdditionalCSV = null;
                } else {
                    emailCC = emailReceiversAdditionalCSV.split(",");
                }
                String[] emailReceivers = emailReceiverPrimary.split(",");

                String[] emailBCC = null;
                mailService.createMail(emailReceivers, emailCC, emailBCC, emailSubject, emailSender, emailContent, true);

                String defaultSessionID[] = mscriptsCommonDao.getDefaultSessionID(clientID, secondaryKey);
                String customerID = defaultSessionID[2];
                mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, commID, null, cvMsgTypeEmail, emailContent, emailSender, emailReceiverPrimary + "," + emailReceiversAdditionalCSV,
                        null, cvMsgStatus, "", null, null, null, cvAdmin, null, secondaryKey);
            }
        } catch (mscriptsException mex) {
            LOGGER_NON_PHI.error(" mscripts Exception thrown  :", mex);
            throw mex;
        } catch (Exception ex) {

            LOGGER_NON_PHI.error("exception occured while sending store sign up failure email :", ex);
            throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
        }
    }

    @Override
	public void sendMessage(String mobile, String clientID, String commName, String[] smscontents,
            String rxNumber, boolean isVerified, boolean isTxtMsgActive, String customerID,
            String shortCode, String shortCodeUserName, String shortCodeServiceID, String prefix) throws Exception {


        String errorSource = "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-sendMessage";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

        
            LOGGER_PHI.info("Entered into sending message method for mobile ={}" , mobile);
        
        try {
            String cvMsgTypeSMS = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvMsgTypeSMS");
            String cvAdmin = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvAdmin");
            String cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvMsgStatus");
            String secondaryKey = pHICredentials.getSecondaryKey();
            //Get the communication id for the user
            Map communicationDetails = generalDao.getCommunicationDetails(commName, clientID,
                    customerID, mobile, prefix, secondaryKey);

            String commID = communicationDetails.get("id").toString();
            String placeholder = communicationDetails.get("sms_placeholder_lengths").toString();
            String smstext = communicationDetails.get("sms_text").toString();

          //  String[] newSMSContents = null;

        /*    if (placeholder.contains(",")) {
                String[] placeholders = placeholder.split(",");
                newSMSContents = smsService.sliceSmsContents(placeholders, smscontents);
            } else {
                String[] placeholders = {placeholder};
                newSMSContents = smsService.sliceSmsContents(placeholders, smscontents);
            }*/
            String[] smstemplate = smstext.split("<");
            //Slice the sms placeholder contents.
          /*  if (newSMSContents == null) {
                LOGGER_NON_PHI.error(" mscripts Exception thrown as sms content is null ");
                throw new mscriptsException(null, errorSource, errorSeverity, new Exception("SMS content failed"));
            }*/
            //Frame the sms contents with the suitable place holders.
            smstext = smsService.frameSmsContent(smstemplate, smscontents);
            if (smstext == null) {
                LOGGER_NON_PHI.error(" mscripts Exception thrown as sms text is null ");
                throw new mscriptsException(null, errorSource, errorSeverity, new Exception("SMS content failed"));
            }
            String apimsgid = null;
            
            String cvUseMessagingEngine = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvUseMessagingEngine");
            
            if(!cvUseMessagingEngine.equals("1")){
            	
            	Map smsMap = smsService.sendSms(mobile, smstext, true, shortCode, customerID,
                        isTxtMsgActive, isVerified, clientID, false, commID, null, null, true, shortCodeUserName, shortCodeServiceID,null, null);
                
//                apimsgid = smsMap.get("apiMsgID").toString();
//                shortCode = smsMap.get("shortCode").toString();
//                mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, commID, rxNumber, cvMsgTypeSMS, smstext, shortCode, mobile, null, cvMsgStatus, apimsgid, null, null, null, cvAdmin, null, secondaryKey);

                if (smsMap != null && smsMap.get("apiMsgID") != null && smsMap.get("shortCode") != null) {
                    apimsgid = smsMap.get("apiMsgID").toString();
                    shortCode = smsMap.get("shortCode").toString();
                    mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientID, customerID, commID, rxNumber, cvMsgTypeSMS, smstext, shortCode, mobile, null, cvMsgStatus, apimsgid, null, null, null, cvAdmin, null, 1,secondaryKey);
                } else if (smsMap != null && smsMap.get("errorCode") != null) {
                    LOGGER_NON_PHI.error(" mscripts Exception thrown as sms map is null ");
                    String cvErrorCodemBloxStatusFailure = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvErrorCodemBloxStatusFailure");
                    throw new mscriptsException(cvErrorCodemBloxStatusFailure, errorSource, errorSeverity, new Exception(" mBlox Error : " + smsMap.get("errorCode")));
                } else {
                    LOGGER_NON_PHI.error(" mscripts Exception  thrown ");
                    throw new mscriptsException(null, errorSource, errorSeverity, new Exception(" Error occured when sending the user message"));
                }
            	
            }else{
            	
            	String cvShortcode =  ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvShortcode");
            	cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgStatusSentToMsgEngine");
            	
            	String comId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientID, customerID, commID, rxNumber, cvMsgTypeSMS, smstext, cvShortcode, mobile, null, cvMsgStatus, null, null, null, null, cvAdmin, null, 1,secondaryKey);
            	
            	Map smsMap = smsService.sendSms(mobile, smstext, true, shortCode, customerID,
                        isTxtMsgActive, isVerified, clientID, false, commID, null, null, true, shortCodeUserName, shortCodeServiceID,null, comId);

            }
        } catch (mscriptsException mex) {
            LOGGER_NON_PHI.error(" mscripts Exception  thrown while sending message", mex);
            throw mex;
        } catch (Exception ex) {
            LOGGER_NON_PHI.error("Exception  thrown while sending message", ex);
            throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
        }
    }

    @Override
	public Map isValidSmsToken(String token, String clientid) throws mscriptsException {

       
            LOGGER_NON_PHI.info("Entered into is valid sms token method for client with id ={}", clientid);
       

        Map map = mscriptsCommonDao.isValidSmsToken(clientid, token, pHICredentials.getSecondaryKey());
        return map;
    }

    public Document invokeHandler(String handlerMethod, String handlerProtocol, String handlerURL, String handlerRequest, String clientID) throws mscriptsException {
        String errorSource = "com.mscripts.externalrequesthandler.service.GeneralServiceImpl-invokeHandler";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        int cvConnectionTimeout = 0;
        if (handlerProtocol.equalsIgnoreCase("http")) {
            java.net.HttpURLConnection conn = null;
            try {
            	
            	cvConnectionTimeout = Integer.parseInt(ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvConnectionTimeout"));

            	String cvDefaultSessionID = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvDefaultSessionID");
                final java.net.URL url = new java.net.URL(handlerProtocol + handlerURL + handlerMethod);
                final java.net.URLConnection uconn = url.openConnection();

                conn = (java.net.HttpURLConnection) uconn;
                // Set up a request.
                conn.setConnectTimeout(cvConnectionTimeout);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("sessionid", cvDefaultSessionID);
                // Send the request.
                java.io.OutputStream req = conn.getOutputStream();
                req.write(MiscUtils.sanatizeString(handlerRequest, true).getBytes(), 0, handlerRequest.length());
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = null;
                String ret_response = "";
                while ((response = in.readLine()) != null) {
                    ret_response += response;
                }
                return XMLUtils.createXMLDocument(ret_response.toString());
            } catch (Exception e) {
                LOGGER_NON_PHI.error("Exception  thrown while invoking handler:", e);
                throw new mscriptsException(e.getMessage(), errorSource, errorSeverity, e);
            } finally {
                conn.disconnect();
            }
        } else {
            //HTTPS code here
            HttpsURLConnection conn = null;
            try {
            	cvConnectionTimeout = Integer.parseInt(ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvConnectionTimeout"));
            	String cvDefaultSessionID = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvDefaultSessionID");

                final java.net.URL url = new java.net.URL(handlerProtocol + handlerURL + handlerMethod);
                final java.net.URLConnection uconn = url.openConnection();

                conn = (HttpsURLConnection) uconn;
                // Set up a request.
                conn.setConnectTimeout(cvConnectionTimeout);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("sessionid", cvDefaultSessionID);
                // Send the request.
                java.io.OutputStream req = conn.getOutputStream();
                req.write(MiscUtils.sanatizeString(handlerRequest, true).getBytes(), 0, handlerRequest.length());
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = null;
                String ret_response = "";
                while ((response = in.readLine()) != null) {
                    ret_response += response;
                }
                return XMLUtils.createXMLDocument(ret_response.toString());
            } catch (Exception e) {
                LOGGER_NON_PHI.error("Exception  thrown while establishing connection at invoking handler:", e);
                throw new mscriptsException(e.getMessage(), errorSource, errorSeverity, e);
            } finally {
                conn.disconnect();
            }
        }
    }

    /**
     * @return the generalDao
     */
    public GeneralDao getGeneralDao() {
        return generalDao;
    }

    /**
     * @param generalDao the generalDao to set
     */
    public void setGeneralDao(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    /**
     * @return the transactionManager
     */
    public DataSourceTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * @param transactionManager the transactionManager to set
     */
    public void setTransactionManager(DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * @return the transactionAttributes
     */
    public Properties getTransactionAttributes() {
        return transactionAttributes;
    }

    /**
     * @param transactionAttributes the transactionAttributes to set
     */
    public void setTransactionAttributes(Properties transactionAttributes) {
        this.transactionAttributes = transactionAttributes;
    }
    
    /**
     * @return the smsService
     */
    public SmsService getSmsService() {
        return smsService;
    }

    /**
     * @param smsService the smsService to set
     */
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * @return the clientDao
     */
    public ClientDao getClientDao() {
        return clientDao;
    }

    /**
     * @param clientDao the clientDao to set
     */
    public void setClientDao(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    /**
     * @return the mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * @param clientDao the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public PHICredentials getpHICredentials() {
        return pHICredentials;
    }

    public void setpHICredentials(PHICredentials pHICredentials) {
        this.pHICredentials = pHICredentials;
    }

    /**
     * @return the mscriptsCommonDao
     */
    public MscriptsCommonDao getMscriptsCommonDao() {
        return mscriptsCommonDao;
    }

    /**
     * @param mscriptsCommonDao the mscriptsCommonDao to set
     */
    public void setMscriptsCommonDao(MscriptsCommonDao mscriptsCommonDao) {
        this.mscriptsCommonDao = mscriptsCommonDao;
    }

    public EncryptionUtil getEncryptionUtil() {
        return encryptionUtil;
    }

    public void setEncryptionUtil(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

	public MscriptsCommonService getMscriptsCommonService() {
		return mscriptsCommonService;
	}

	public void setMscriptsCommonService(MscriptsCommonService mscriptsCommonService) {
		this.mscriptsCommonService = mscriptsCommonService;
	}

	public IDispensingPatientService getDispensingPatientService() {
		return dispensingPatientService;
	}

	public void setDispensingPatientService(IDispensingPatientService dispensingPatientService) {
		this.dispensingPatientService = dispensingPatientService;
	}
	
	public SCHService getSchService() {
			return schService;
	}

	public void setSchService(SCHService schService) {
		this.schService = schService;
	}
    
}
