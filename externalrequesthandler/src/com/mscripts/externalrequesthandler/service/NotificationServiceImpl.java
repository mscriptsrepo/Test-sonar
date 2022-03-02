/*******************************************************
Title               : NotificationServiceImpl.java
Author              : Abhinandan U S
Description         : Service Implementation of the NotificationService interface
Modification History: Not Applicable
Created             : 18-Jan-10
Modified            : Not Applicable
Notes               : None
 *******************************************************/
package com.mscripts.externalrequesthandler.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.MscriptsCommonDao;
import com.mscripts.domain.SendCommunicationMode;
import com.mscripts.enums.DrugFilterType;
import com.mscripts.enums.NotificationTypeKey;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.GeneralDao;
import com.mscripts.externalrequesthandler.dao.NotificationDao;
import com.mscripts.externalrequesthandler.domain.AdhCommn;
import com.mscripts.externalrequesthandler.domain.AdhError;
import com.mscripts.externalrequesthandler.domain.AdhInterceptMsgRequest;
import com.mscripts.externalrequesthandler.domain.AdhPatientCommn;
import com.mscripts.externalrequesthandler.domain.CustomerPrescription;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionFile;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionTxtpfile;
import com.mscripts.externalrequesthandler.domain.InsuranceCard;
import com.mscripts.externalrequesthandler.domain.InsuranceCardLink;
import com.mscripts.service.APNSService;
import com.mscripts.service.FCMService;
import com.mscripts.service.GCMSService;
import com.mscripts.service.IvrService;
import com.mscripts.service.MailService;
import com.mscripts.service.MscriptsCommonService;
import com.mscripts.service.SmsService;
import com.mscripts.service.URLShortner;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.DrugFilterCriteriaUtil;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.GenerateRandom;
import com.mscripts.utils.MscriptsStringUtils;
import com.mscripts.utils.NotificationFilterUtil;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.QueryBuilder;
import com.mscripts.utils.TextMaskingService;
import com.mscripts.utils.XMLUtils;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author abhinandanus
 */
public class NotificationServiceImpl implements NotificationService {

	private NotificationDao notificationDao;
	private DataSourceTransactionManager transactionManager;
	private Properties transactionAttributes;
	private MscriptsCommonDao mscriptsCommonDao;
	private SmsService smsService;
	private APNSService apnsService;
	private GCMSService gcmsService;
	private MailService mailService;
	private GeneralDao generalDao;
	private PHICredentials pHICredentials;
	private FCMService fcmService;
	private NotificationFilterUtil notificationFilterUtil;
	private TextMaskingService textMaskingService;
	private IvrService ivrService;
	private MscriptsCommonService mscriptsCommonService; 
	private URLShortner urlShortnerService;
	private DrugFilterCriteriaUtil drugFilterCriteriaUtil;

	private static final Logger LOGGER_NON_PHI = LogManager
			.getLogger("non.phi." + NotificationServiceImpl.class.getName());
	private static final Logger LOGGER_PHI = LogManager.getLogger("phi." + NotificationServiceImpl.class.getName());

	//Method to update customer email
	@Override
	public boolean updateCustomerEmail(String customerid, String emailid, String deleteEmail, String clientID)
			throws mscriptsException {
		try {

			LOGGER_NON_PHI.info("Updating customer email with customer id ={}", customerid);

			String secondaryKey = pHICredentials.getSecondaryKey();
			this.getNotificationDao().updateCustomerEmail(customerid, emailid, deleteEmail, clientID, secondaryKey);
		} catch (mscriptsException ex) {
			LOGGER_NON_PHI.error(" mscripts Exception occured while updating customer email:", ex);
			throw ex;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while updating customer email={}", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.service.NotificationServiceImpl-updateCustomerEmail",
					mscriptsExceptionSeverity.Medium, ex);
		}
		return true;
	}

	@Override
	public void updatePatient(String clientID, String customerID, String deceased) throws mscriptsException {
		try {

			LOGGER_NON_PHI.info("Updating patient info with client id ={}", clientID);

			this.notificationDao.updatePatient(clientID, customerID, deceased);
		} catch (mscriptsException ex) {
			LOGGER_NON_PHI.error(" mscripts Exception occured while updating patient info:", ex);
			throw ex;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while updating patient info:", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.service.NotificationServiceImpl-updatePatient",
					mscriptsExceptionSeverity.Medium, ex);
		}
	}

	@Override
	public void updateInsurance(String clientID, String customerID, NodeList insuranceCardNode,
			NodeList insuranceLinkNode) throws mscriptsException {

		try {

			LOGGER_NON_PHI.info("Updating patient insurance info with client id ={}", clientID);

			//1. get insurnace card Array
			//2. get insurance card link array
			//3. call card dao method by passing array object
			//4. call cardlink dao method by passing array object

			InsuranceCard[] insCardArr = InsuranceCard.parseCustomerCard(insuranceCardNode);
			for (int i = 0; i < insCardArr.length; i++) {
				notificationDao.updateCustomerCard(clientID, customerID, insCardArr[i],
						pHICredentials.getSecondaryKey());
			}

			InsuranceCardLink[] insCardLinkArr = InsuranceCardLink.parseCustomerCardLink(insuranceLinkNode);
			for (int j = 0; j < insCardLinkArr.length; j++) {
				notificationDao.updateCustomerCardLink(clientID, customerID, insCardLinkArr[j],
						pHICredentials.getSecondaryKey());
			}

		} catch (mscriptsException ex) {
			LOGGER_NON_PHI.error(" mscripts Exception occured while updating patient insurance:", ex);
			throw ex;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while updating patient insurnace:", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.service.NotificationServiceImpl-updateInsurance",
					mscriptsExceptionSeverity.Medium, ex);
		}
	}

	//Method to update customer prescription.
	@Override
	public void updateCustomerPrescription(String customerID, String prescriptionXml, String clientID,
			String patientUpdateNode, String messageDate) throws mscriptsException {
		String cvCheckMscriptsAutoFillCriteria = null;
		String cvIsMscriptsAutofillEnabled = null;
		try {
			// Create the prescription list from the xml.
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Updating customer prescription info with customer id ={}", customerID);
			}

			cvCheckMscriptsAutoFillCriteria = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.cvCheckMscriptsAutoFillCriteria);
			cvIsMscriptsAutofillEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.cvIsMscriptsAutofillEnabled);
	
			String cvLogTransactionLevelInsuranceCard = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					ConfigKeys.CV_LOG_TRANSACTION_LEVEL_INSURANCE_CARD);
			
			Map<String, Object> rxStatus = null;

			Map PresList = parseCustomerPrescriptions(prescriptionXml, patientUpdateNode, clientID);
			List custPresList = (List) PresList.get("customerPresList");

			Iterator iter = custPresList.listIterator();
			int isPickupEligible = 0;
			while (iter.hasNext()) {

				/*
				   	isPickupEligible flag to determine whether notifications will be sent to user or not
				   	0 - Normal User(is_migrated = 0 or 1) They will be sent normal messages
					1 - Migrated User with is_migrated as 2. They need to be sent instantaneous migration pick up message only when the pickup interception 
						for migration is enabled. Also the bulk rx entry for aeging messages should be deleted. 
					2 - Migrated User with is_migrated > 2. They should not be sent any notifications as well as the bulk rx entry should be deleted
				 */

				String cvInterceptPickupForMigration = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
						"cvInterceptPickupForMigration");
				// cvAccelaretRxPickupType - none , optin , optout
				String cvAccelaretRxPickupType = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
						"cvAccelaretRxPickupType");
				String cvMsgRxReadyForMU = null;
				Map<String, String> migrationResponse = new HashMap<>();
				try {
					if (cvAccelaretRxPickupType.equals("optin")) {
						cvMsgRxReadyForMU = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
								"cvMsgRxReadyForOptInMU");
					} else if (cvAccelaretRxPickupType.equals("optout")) {
						cvMsgRxReadyForMU = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
								"cvMsgRxReadyForOptOutMU");
					}
					migrationResponse = mscriptsCommonDao.checkMigratedUserForPickupReminder(clientID, customerID,
							cvInterceptPickupForMigration, cvMsgRxReadyForMU, cvAccelaretRxPickupType,
							pHICredentials.getSecondaryKey());
					if (migrationResponse != null) {
						isPickupEligible = (int) (migrationResponse.containsKey("isPickupEligible")
								? migrationResponse.get("isPickupEligible") : isPickupEligible);
					}
				} catch (Exception ex) {
					LOGGER_NON_PHI
							.info("Error occured during migration check.Pickup Inerception for Migration is disabled.");
				}
				CustomerPrescription customerPres = (CustomerPrescription) iter.next();
				// Check whether text delivery enabled or not
				boolean isTextDeliveryAllowed = checkTextDeliveryEnabledForClient(clientID, customerPres);
				// Map created to pass additional parameters as a single Map
				Map<String, Object> inputParamsMap = new HashMap<>();
				inputParamsMap.put("isTextDeliveryAllowed", isTextDeliveryAllowed);
				rxStatus = notificationDao.updateCustomerPrescriptions(customerID, customerPres, clientID, messageDate,
						pHICredentials.getSecondaryKey(),inputParamsMap);
				
				/*rxStatus returned will be empty every time we attempt to insert a duplicate prescription -- BASE-272*/
				if(rxStatus != null && !rxStatus.isEmpty()){
					
					// Insert Aging pickup Reminder
					insertAgingPickupReminders(clientID, rxStatus, isTextDeliveryAllowed);

					//to update customer_prescription_id in mscripts_auto_fill_criteria incase if it doesn't exists
					try{
						if(!MscriptsStringUtils.isStringEmptyOrNull(cvCheckMscriptsAutoFillCriteria) 
								&& MscriptsStringUtils.compareStringEquals(cvCheckMscriptsAutoFillCriteria, "1")){
							LOGGER_NON_PHI.info("Inside check Autofill Criteria if block");
							mscriptsCommonDao.updateCustPrescIdInMscriptsAutoFillCriteria(customerPres.getRxNum(),
									customerPres.getStoreNCPDP());
						}
					}catch(Exception ex){
						LOGGER_NON_PHI.error(" Exception occured while updating Mscripts_auto_fill_criteria in updateMscriptsAutoFillCriteria() : {} ", ex);
					}
	
					/*
					 * calculate next mscripts autofill dates on receiving sold
					 * update.
					 */
					if(Constants.NUMERIC_TRUE_STRING.equalsIgnoreCase(cvIsMscriptsAutofillEnabled)){
						mscriptsCommonDao.calcAndUpdateNextMscriptsAutofillDate(clientID, customerID,
							rxStatus.get("custPrescID").toString(), customerPres.getRxNum(), customerPres.getStoreNCPDP());
					}
	
					// log all transactions
					notificationDao.logPrescriptionTransactions(clientID, customerID, customerPres.getRxNum(),
							customerPres.getStoreNCPDP(), customerPres.getCustomerTxFile());
					
					// log insurance card details related to any transaction
					if (MscriptsStringUtils.compareStringEquals(cvLogTransactionLevelInsuranceCard,
							Constants.NUMERIC_TRUE_STRING)) {
						notificationDao.logInsuranceCardTransactions(clientID, customerID, customerPres.getRxNum(),
								customerPres.getStoreNCPDP(), customerPres.getCustomerTxFile());
					}

					
					if (MscriptsStringUtils.compareStringEquals(rxStatus.get("vIsDrugEligibleForInProcessText").toString(),
							"1")) {
						Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
						drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxrefillinprocess);
						boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientID,
								DrugFilterType.SCHEDULE, customerPres.getSched(), drugFilterCriteriasMap);
						if (!bisDrugExcluded) {
							bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientID, DrugFilterType.GPI,
									customerPres.getDispDrugGPI(), drugFilterCriteriasMap);
							if (!bisDrugExcluded && MscriptsStringUtils.compareStringEquals(
									rxStatus.get("isRxStatusNotificationsEnabled").toString(), Constants.NUMERIC_TRUE_STRING)) {
								bisDrugExcluded = !notificationFilterUtil.isCustomerNotifcationEnabled(clientID,
										customerID, NotificationTypeKey.rxrefillinprocess.toString());
							}
						}
						if (!bisDrugExcluded) {
							// Insert into Bulk Rx table for scheduling In process
							// (PNS) text which is received during the blocked hours
							notificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_BULK_RX,
									new Object[] { clientID, customerPres.getRxNum(), customerPres.getStoreNCPDP(),
											customerPres.getStoreNCPDP(), rxStatus.get("filled").toString(),
											rxStatus.get("refillStatus").toString(), customerPres.getLatestTxNumber(), null,
											rxStatus.get("custPrescID").toString(),
											rxStatus.get("wso2notificationTime").toString(),
											rxStatus.get("wso2notificationTime").toString(), customerID, customerID,
											customerID, "1", "0", "0", "0", "0", rxStatus.get("prescriptionCode").toString(),
											"0", "1", rxStatus.get("rxAdjudicationComID").toString() });
						}
					}
					if (isPickupEligible == 0 || isPickupEligible == 1) {
						String sSendRxPickup = rxStatus.get("sendRxPickup").toString();
						boolean bIsRxReadyForPickup = "1".compareToIgnoreCase(sSendRxPickup) == 0
								|| "true".compareToIgnoreCase(sSendRxPickup) == 0 ? true : false;
	
						String sIsRxSold = rxStatus.get("varIsRxSold").toString();
						boolean bIsRxSold = "1".compareToIgnoreCase(sIsRxSold) == 0
								|| "true".compareToIgnoreCase(sIsRxSold) == 0 ? true : false;
	
						if (bIsRxReadyForPickup) {
							// Code for sending BulkRxpickup reminder
							String brpiID = rxStatus.get("brpiID").toString();
							Map<String, Object> pickupReminderDetails = notificationDao
									.selectRxPickupReminderInstancesRecords(clientID, brpiID,
											pHICredentials.getSecondaryKey());
							
							String custPrescID = null;
							
							if (rxStatus != null && rxStatus.get("custPrescID") != null && !Constants.EMPTY_STRING.equals(rxStatus.get("custPrescID").toString())) {
								custPrescID = rxStatus.get("custPrescID").toString();
							}
							Map<String, String> rxMiscDetailsMap = null;
							
							if (custPrescID != null && !Constants.EMPTY_STRING.equals(custPrescID.trim())) {
								rxMiscDetailsMap = notificationDao.getRxDetailsForDaysQtyClients(clientID, custPrescID);
							}
							if (pickupReminderDetails != null) {
								pickupReminderDetails.put("days", rxMiscDetailsMap.get("days"));
								pickupReminderDetails.put("quantity", rxMiscDetailsMap.get("quantity")); 
								try {
									sendRxPickupReminder(clientID, pickupReminderDetails, migrationResponse);							
							} catch (Exception ex) {
								if (isPickupEligible > 0) {
									LOGGER_PHI.error(
											"Exception occured during sendRxPickupReminder. Deleting entries in bulk rx entries for migrated users.",
											ex);
									notificationDao.processPickupReminderForMigratedUser(clientID, customerID,
											isPickupEligible, cvAccelaretRxPickupType, 0);
								}
							}
							} else {
								if (isPickupEligible > 0) {
									try {
										LOGGER_PHI.info("Deleting entries in bulk rx entries for migrated users. ");
										notificationDao.processPickupReminderForMigratedUser(clientID, customerID, 2,
												cvAccelaretRxPickupType, 0);
									} catch (Exception ex) {
										LOGGER_NON_PHI.error(
												" Exception caught while deleting bulk rx entries for migrated users: ",
												ex);
									}
								}
							}
						}else {
							if (isPickupEligible > 0) {
								try {
									LOGGER_PHI.info("Deleting entries in bulk rx entries for migrated users. ");
									notificationDao.processPickupReminderForMigratedUser(clientID, customerID, 2,
											cvAccelaretRxPickupType, 0);
								} catch (Exception ex) {
									LOGGER_NON_PHI.error(
											" Exception caught while deleting bulk rx entries for migrated users: ",
											ex);
								}
							}
						}
	
						/*
						 * Below code is temporary. Once we add the ability to send text
						 * messages directly from Adherence code, we will be removing
						 * the below code.
						 */
						if (isPickupEligible == 0) {
							if (bIsRxSold) {
								String sClientIdentifier = rxStatus.get("client_identifier").toString();
								String sCustPrescId = rxStatus.get("custPrescID").toString();
	
								if (rxStatus.get("varMobile") != null) {
									String sMobile = rxStatus.get("varMobile").toString();
									String sShortcode = rxStatus.get("varShortcode").toString();
									String sShortcodeUsername = rxStatus.get("varShortcodeUsername").toString();
									String sShortcodeServiceId = rxStatus.get("varShortcodeServiceId").toString();
									String sCommunicationId = rxStatus.get("varCommunicationID").toString();
									sendAdherenceSoldMessage(sClientIdentifier, clientID, customerID, sCustPrescId, sMobile,
											sShortcode, sShortcodeUsername, sShortcodeServiceId, sCommunicationId);
								}
							}
						}
					} else {
						try {
							LOGGER_PHI.info("Deleting entries in bulk rx entries for migrated users. ");
							notificationDao.processPickupReminderForMigratedUser(clientID, customerID, 2,
									cvAccelaretRxPickupType, 0);
						} catch (Exception ex) {
							LOGGER_NON_PHI.error(" Exception caught while deleting bulk rx entries for migrated users: ",
									ex);
						}
					}
					
					/**
					 * [BASE-2344] [snelluru] Added customer prescription ID as a parameter to this function.
					 */
					if (isPickupEligible == 0) {
						//Fill Refill Reminder instances
						mscriptsCommonDao.fillRefillReminderInstances(clientID, customerID,
								rxStatus.get("custPrescID").toString(), Constants.DEFAULT_LANGUAGE);
					}
				}	
			}
			
			if (isPickupEligible == 0) {
				//Fill Dosage Reminder instances
				mscriptsCommonDao.fillDosageReminderInstances(clientID, customerID, Constants.DEFAULT_LANGUAGE);
			}
			
		} catch (mscriptsException mEx) {
			LOGGER_NON_PHI.error(" mscripts Exception occured while updating customer prescription info:", mEx);
			throw mEx;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while updating customer prescription info:", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.service.NotificationServiceImpl-updateCustomerPrescription",
					mscriptsExceptionSeverity.Medium, ex);
		}
	}

	/**
	 * This method will check whether text delivery is enabled for the specified
	 * client or not based on the drug filter criteria rules
	 * 
	 * @param clientId     -To specify the client Id
	 * @param customerPres - Prescription object contains the prescription details
	 * @return isTextDeliveryAllowed boolean flag
	 * @throws MscriptsException
	 */
	private boolean checkTextDeliveryEnabledForClient(String clientId, CustomerPrescription customerPres)
			throws MscriptsException {
		boolean isTextDeliveryAllowed = false;
		try {
			// Fetch required config values
			String cvIsDeliveryAllowed = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					ConfigKeys.CVISDELIVERYALLOWED);
			String cvTxFileWillCallReadyFlag = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					ConfigKeys.CV_TX_FILE_WILL_CALL_READY_FLAG);
			LOGGER_NON_PHI.info("config cvIsDeliveryAllowed :{} and cvTxFileWillCallReadyFlag:{}", cvIsDeliveryAllowed,
					cvTxFileWillCallReadyFlag);
			if (Constants.NUMERIC_TRUE_STRING.equalsIgnoreCase(cvIsDeliveryAllowed)
					&& customerPres.getWillCallReady() != null
					&& cvTxFileWillCallReadyFlag.equalsIgnoreCase(customerPres.getWillCallReady())) {
				// Get text delivery enabled delivery detail id's
				List<Map<String, String>> textDeliveryEnabledVendorsList = mscriptsCommonDao
						.getTextDeliveryEnabledVendors(clientId);
				if (!MscriptsStringUtils.isCollectionEmptyOrNull(textDeliveryEnabledVendorsList)) {
					// Iterate over each text enabled delivery vendor
					for (Map<String, String> textDeliveryEnabledVendor : textDeliveryEnabledVendorsList) {
						boolean isDrugExcluded = checkForDrugExcludedFlag(clientId, customerPres,
								textDeliveryEnabledVendor);
						LOGGER_NON_PHI.info("Drug excluded value :{} for delivery detail id :{}", isDrugExcluded,
								textDeliveryEnabledVendor.get(Constants.DELIVERY_DETAIL_ID));
						if (isDrugExcluded) {
							isTextDeliveryAllowed = true;
							break;
						}

					}
				}
			}
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while processing checkTextDeliveryEnabledForClient method:{}", ex);
			throw new MscriptsException(clientId,
					"Exception occured while processing checkTextDeliveryEnabledForClient method", ex,
					mscriptsExceptionSeverity.Low);

		}
		return isTextDeliveryAllowed;
	}

	/**
	 * Method will compare the drug filter criteria values and will return the
	 * boolean flag false if the drug is excluded from text delivery
	 * 
	 * @param clientId                  --To specify the client Id
	 * @param customerPres              - Customer prescription object
	 * @param textDeliveryEnabledVendor
	 * @return boolean flag
	 * @throws MscriptsException
	 */
	private boolean checkForDrugExcludedFlag(String clientId, CustomerPrescription customerPres,
			Map<String, String> textDeliveryEnabledVendor) throws MscriptsException {
		Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
		drugFilterCriteriasMap.put(Constants.DELIVERY_DETAIL_ID,
				textDeliveryEnabledVendor.get(Constants.DELIVERY_DETAIL_ID));
		boolean firstFillCheck = true;

		//For first refill
		if (customerPres.getRefAut() != null && customerPres.getRefRem() != null
				&& customerPres.getRefAut().equalsIgnoreCase(customerPres.getRefRem())
				&& drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.SCHEDULEFORFIRSTREFILL,
						customerPres.getSched(), drugFilterCriteriasMap)) {
			firstFillCheck = false;
		}
		// Check drug filter criteria rules for NCPDPID,NDC,Schedule,GPI
		return ((drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.NCPDPID, customerPres.getStoreNCPDP(),
				drugFilterCriteriasMap))
				&& !(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.NDC, customerPres.getPrescDrugNDC(),
						drugFilterCriteriasMap))
				&& !(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.SCHEDULE, customerPres.getSched(),
						drugFilterCriteriasMap))
				&& !(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI, customerPres.getPrescDrugGPI(),
						drugFilterCriteriasMap))
				&& firstFillCheck);
	}

	private void insertAgingPickupReminders(String clientId, Map<String, Object> rxStatus,
			boolean isTextDeliveryAllowed) {
		if (rxStatus.get("enableAgingReminder") != null && (boolean) rxStatus.get("enableAgingReminder")) {
			try {
				rxStatus.put("prescriptionId", rxStatus.get("custPrescID"));
				rxStatus.put("filledDate", rxStatus.get("filled"));
				rxStatus.put("created_by", "ERH_updateCustomerPrescription");
				rxStatus.put("last_updated_by", "ERH_updateCustomerPrescription");
                mscriptsCommonService.insertAgeingPickupReminders(clientId, rxStatus, isTextDeliveryAllowed);
			} catch (Exception e) {
				LOGGER_NON_PHI.error("Error occured while inserting aging reminder for customerId:{} and rx_number:{}",
						rxStatus.get("customerId"), rxStatus.get("rxNumber"));
			}
		}
	}

	//Method to create customer prescription domain object list from prescription xml.
	public Map parseCustomerPrescriptions(String patientServiceXml, String patientUpdateNode, String clientID)
			throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-parseCustomerPrescriptions";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into parsing customer prescriptions method");
		}
		try {
			List customerPresList = new ArrayList();
			Map returnMap = new HashMap();
			Document patientServiceXmlDoc = XMLUtils.createXMLDocument(patientServiceXml);
			NodeList rxfileNodeList = XMLUtils.getNodeList(patientServiceXmlDoc,
					"//XmlMessage/" + patientUpdateNode + "/rxfile");
			int rxfileNodeSize = rxfileNodeList.getLength();
			String sgCode = null;
			String quantity = null;
			String prescDrugName = null;
			NodeList txNumNodeList = null;
			NodeList txNodeList = null;
			int txNumNodeSize = 0;
			int latestTxNumber = 0;
			String strTxNumber = null;
			int intTxNumber = 0;
			int intTxNumberWithSoldORWillCall = 0;
			CustomerPrescription customerPres = null;
			Document rxfiledoc = null;
			String prescTxList = "";
			int index = 0;
			String cvCopayEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvCopayEnabled");
			String cvPriceAsCopayEnabled=ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvPriceAsCopayEnabled");
			for (int i = 0; i < rxfileNodeSize; i++) {
				latestTxNumber = 0; //TQ-138 - mshri - re-initiate latestTxNumber for every rxfile node
				rxfiledoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(rxfileNodeList.item(i)));
				customerPres = new CustomerPrescription();
				customerPres.setRxNum(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/rxnum"));
				customerPres.setSgCode(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/sgcode"));
				customerPres.setRxStatus(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/status"));
				customerPres.setWhyDeact(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/whydeact"));
				customerPres.setSched(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/sched"));
				customerPres.setRefAut(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/refaut"));
				customerPres.setRefRem(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/refrem"));
				customerPres.setPrefill(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/prefill"));
				customerPres.setQuantity(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/quantity"));
				customerPres.setFirst(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/first"));
				customerPres.setExpire(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/expire"));
				customerPres.setStoreNCPDP(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/storeNCPDP"));
				customerPres.setDeleteRx(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/DeleteRx"));
				customerPres.setSigText(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/sigText"));
				customerPres.setPrescribingDocName(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docName"));
				customerPres
						.setPrescribingDocLname(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docLastName"));
				customerPres
						.setPrescribingDocFname(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docFirstName"));
				customerPres
						.setPrescribingDocMname(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docMiddleName"));
				customerPres.setPrescribingDocCity(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docCity"));
				customerPres.setPrescribingDocState(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docState"));
				customerPres.setPrescribingDocZip(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docZip"));
				customerPres.setPrescribingDocDEA(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docDea"));
				customerPres
						.setPrescribingDocAreaCode(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docAreaCode"));
				customerPres.setPrescribingDocPhone(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docPhone"));
				customerPres.setPrescribingDocFaxAreaCode(
						XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docFaxAreaCode"));
				customerPres
						.setPrescribingDocFaxPhone(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docFaxPhone"));
				customerPres.setPrescDrugName(MscriptsStringUtils
						.removeSpaceBtwnWords(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/prescDrugName")));
				customerPres.setPrescDrugNDC(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/prescDrugNDC"));
				customerPres.setPrescDrugGPI(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/prescDrugGPI"));
				customerPres
						.setPrescribingDocAddress1(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docAddress"));
				customerPres
						.setPrescribingDocAddress2(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/RxAddendum/docAddress2"));

				txNumNodeList = XMLUtils.getNodeList(rxfiledoc, "//rxfile/txfile/txnum");
				txNodeList = XMLUtils.getNodeList(rxfiledoc, "//rxfile/txfile");
				txNumNodeSize = txNumNodeList.getLength();

				CustomerTransactionFile[] custTxFile = parseCustomerTransactions(txNodeList, clientID);
				customerPres.setCustomerTxFile(custTxFile);

				for (int j = 0; j < txNumNodeSize; j++) {
					strTxNumber = txNumNodeList.item(j).getTextContent().trim();
					if (!strTxNumber.equals("")) {
						intTxNumber = Integer.parseInt(strTxNumber);
						if ((!XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + intTxNumber + "']/sold")
								.trim().equals("")
								|| !XMLUtils
										.getNodeValue(rxfiledoc,
												"//rxfile/txfile[txnum='" + intTxNumber + "']/willCallReady")
										.trim().equals(""))
								&& intTxNumber > intTxNumberWithSoldORWillCall) {
							intTxNumberWithSoldORWillCall = intTxNumber;
						}
						prescTxList += strTxNumber + "," + XMLUtils
								.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + strTxNumber + "']/filled").trim()
								+ " "
								+ XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + strTxNumber + "']/time")
										.trim()
								+ ","
								+ XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + strTxNumber + "']/sold")
										.trim()
								+ " "
								+ XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + strTxNumber + "']/stime")
										.trim()
								+ ","
								+ XMLUtils
										.getNodeValue(rxfiledoc,
												"//rxfile/txfile[txnum='" + strTxNumber + "']/TxAddendum/dispDrugNDC")
										.trim()
								+ ","
								+ XMLUtils
										.getNodeValue(rxfiledoc,
												"//rxfile/txfile[txnum='" + strTxNumber + "']/TxAddendum/dispDrugGPI")
										.trim()
								+ "~~";
						if (intTxNumber > latestTxNumber) {
							strTxNumber = txNumNodeList.item(j).getTextContent().trim();
							latestTxNumber = intTxNumber;
						}
					}
				}
				// Finding the index of the latest transaction
				for (index = 0; index < custTxFile.length; index++) {
					//txNum = Integer.parseInt(custTxFile[index].getTxnum());
					if (custTxFile[index].getTxnum().equals(String.valueOf(latestTxNumber))) {
						//System.out.println("inside if");
						break;
					}
				}
				//Give sold / willcallready transaction precedence based on filled date
				Date latestTxnFilledAt = null;
				Date preferredTxnFilledAt = null;
				String latestTxnFilled = XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/filled") + " "
						+ XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/time");
				String preferredTxnFilled = XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + intTxNumberWithSoldORWillCall + "']/filled") + " "
						+ XMLUtils.getNodeValue(rxfiledoc,
								"//rxfile/txfile[txnum='" + intTxNumberWithSoldORWillCall + "']/time");

				if (!latestTxnFilled.trim().equals("") && !preferredTxnFilled.trim().equals("")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					latestTxnFilledAt = sdf.parse(latestTxnFilled);
					preferredTxnFilledAt = sdf.parse(preferredTxnFilled);

					if (intTxNumberWithSoldORWillCall != 0 && latestTxnFilledAt.before(preferredTxnFilledAt)) {
						latestTxNumber = intTxNumberWithSoldORWillCall;
					}
				}

				customerPres.setTxList(prescTxList);
				customerPres.setLatestTxNumber(latestTxNumber);
				customerPres.setFilled(XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/filled") + " "
						+ XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/time"));
				sgCode = XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/sgcode");
				if (!sgCode.equals("")) {
					customerPres.setSgCode(sgCode);
				}

				quantity = XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/quantity");
				if (!quantity.equals("")) {
					customerPres.setQuantity(quantity);
				}

				customerPres.setDays(
						XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/days"));
				customerPres.setTxStatus(
						XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/txstatus"));
				customerPres.setLatestSoldDate(XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/sold") + " "
						+ XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/stime"));
				customerPres.setWillCallReady(XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/willCallReady"));
				customerPres.setDeleteTx(
						XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/DeleteTx"));

				customerPres.setDispDrugName(MscriptsStringUtils.removeSpaceBtwnWords(XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/TxAddendum/dispDrugName")));
				customerPres.setDispDrugNDC(XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/TxAddendum/dispDrugNDC"));
				customerPres.setDispDrugGPI(XMLUtils.getNodeValue(rxfiledoc,
						"//rxfile/txfile[txnum='" + latestTxNumber + "']/TxAddendum/dispDrugGPI"));

				customerPres.setTransfer(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/transfer"));
				customerPres.setNewrxnum(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/newrxnum"));
				customerPres.setOldrxnum(XMLUtils.getNodeValue(rxfiledoc, "//rxfile/oldrxnum"));
				String copay=null;
				if (cvCopayEnabled.equals("1")) {
					copay = custTxFile[index].getCopay();
					// Jira COST-1723
					if("1".equalsIgnoreCase(cvPriceAsCopayEnabled)) {
						if (!(copay == null || copay.isEmpty())) {
							copay = String.format("%.2f", (Double.parseDouble(copay)));
						}else{
							copay=custTxFile[index].getPrice();
							if (!(copay == null || copay.isEmpty())) {
							copay= String.format("%.2f",(Double.parseDouble(copay)));
							}
						}
					}else{
					
					//String copay = XMLUtils.getNodeValue(rxfiledoc, "//rxfile/txfile[txnum='" + latestTxNumber + "']/txtpfile/copay");
					copay = custTxFile[index].getCopay();
					if (!(copay == null || copay.isEmpty())) {
						copay = String.format("%.2f", (Double.parseDouble(copay)));
					}
					}
					customerPres.setCopay(copay);
				}
				customerPresList.add(customerPres);
			}
			returnMap.put("customerPresList", customerPresList);
			return returnMap;

		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while parsing customer prescription:", ex);
			throw new mscriptsException(null, errorSource, errorSeverity, ex);
		}
	}

	public CustomerTransactionFile[] parseCustomerTransactions(NodeList txNodeList, String clientID)
			throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-parseCustomerTransactions";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		CustomerTransactionFile customerTransactionFile[] = new CustomerTransactionFile[txNodeList.getLength()];
		Document txfiledoc = null;
		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into parsing customer transactions file method");
		}
		try {

			String cvTxFileFilled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvTxFileFilled");
			String cvTxFileWCR = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvTxFileWCR");
			String cvTxFileSold = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvTxFileSold");
			String cvTxFileWillCallReadyFlag = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					ConfigKeys.CV_TX_FILE_WILL_CALL_READY_FLAG);
			NodeList customerTransactionTxtpfile = null;
			String cvCopayEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvCopayEnabled");
			for (int i = 0; i < txNodeList.getLength(); i++) {

				txfiledoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(txNodeList.item(i)));

				customerTransactionFile[i] = new CustomerTransactionFile();
				customerTransactionFile[i].setTxnum(XMLUtils.getNodeValue(txfiledoc, "//txfile/txnum"));
				customerTransactionFile[i].setFilleddate(XMLUtils.getNodeValue(txfiledoc, "//txfile/filled") + " "
						+ XMLUtils.getNodeValue(txfiledoc, "//txfile/time"));
				customerTransactionFile[i].setSolddate(XMLUtils.getNodeValue(txfiledoc, "//txfile/sold") + " "
						+ XMLUtils.getNodeValue(txfiledoc, "//txfile/stime"));
				customerTransactionFile[i].setPhcode(XMLUtils.getNodeValue(txfiledoc, "//txfile/phcode"));
				customerTransactionFile[i].setSgcode(XMLUtils.getNodeValue(txfiledoc, "//txfile/sgcode"));
				customerTransactionFile[i].setDrcode(XMLUtils.getNodeValue(txfiledoc, "//txfile/drcode"));
				customerTransactionFile[i].setNdc(XMLUtils.getNodeValue(txfiledoc, "//txfile/ndc"));
				customerTransactionFile[i].setMfg(XMLUtils.getNodeValue(txfiledoc, "//txfile/mfg"));
				customerTransactionFile[i].setStatus(XMLUtils.getNodeValue(txfiledoc, "//txfile/status"));
				customerTransactionFile[i].setTpbill(XMLUtils.getNodeValue(txfiledoc, "//txfile/tpbill"));
				customerTransactionFile[i].setHold(XMLUtils.getNodeValue(txfiledoc, "//txfile/hold"));
				customerTransactionFile[i].setPostype(XMLUtils.getNodeValue(txfiledoc, "//txfile/postype"));
				customerTransactionFile[i].setPrcode(XMLUtils.getNodeValue(txfiledoc, "//txfile/prcode"));
				customerTransactionFile[i].setTaxcode(XMLUtils.getNodeValue(txfiledoc, "//txfile/taxcode"));
				customerTransactionFile[i].setInitials(XMLUtils.getNodeValue(txfiledoc, "//txfile/initials"));
				customerTransactionFile[i].setOrder(XMLUtils.getNodeValue(txfiledoc, "//txfile/order"));
				customerTransactionFile[i].setRphcoun(XMLUtils.getNodeValue(txfiledoc, "//txfile/rphcoun"));
				customerTransactionFile[i].setTechinit(XMLUtils.getNodeValue(txfiledoc, "//txfile/techinit"));
				customerTransactionFile[i].setDaw(XMLUtils.getNodeValue(txfiledoc, "//txfile/daw"));
				customerTransactionFile[i].setIntover(XMLUtils.getNodeValue(txfiledoc, "//txfile/intover"));
				customerTransactionFile[i].setAllover(XMLUtils.getNodeValue(txfiledoc, "//txfile/allover"));
				customerTransactionFile[i].setPdover(XMLUtils.getNodeValue(txfiledoc, "//txfile/pdover"));
				customerTransactionFile[i].setDcover(XMLUtils.getNodeValue(txfiledoc, "//txfile/dcover"));
				customerTransactionFile[i].setDtover(XMLUtils.getNodeValue(txfiledoc, "//txfile/dtover"));
				customerTransactionFile[i].setDurover(XMLUtils.getNodeValue(txfiledoc, "//txfile/durover"));
				customerTransactionFile[i].setMesg(XMLUtils.getNodeValue(txfiledoc, "//txfile/mesg"));
				customerTransactionFile[i].setQuantity(XMLUtils.getNodeValue(txfiledoc, "//txfile/quantity"));
				customerTransactionFile[i].setRefnum(XMLUtils.getNodeValue(txfiledoc, "//txfile/refnum"));
				customerTransactionFile[i].setDays(XMLUtils.getNodeValue(txfiledoc, "//txfile/days"));
				customerTransactionFile[i].setCost(XMLUtils.getNodeValue(txfiledoc, "//txfile/cost"));
				customerTransactionFile[i].setAccost(XMLUtils.getNodeValue(txfiledoc, "//txfile/accost"));
				customerTransactionFile[i].setDiscount(XMLUtils.getNodeValue(txfiledoc, "//txfile/discount"));
				customerTransactionFile[i].setTax(XMLUtils.getNodeValue(txfiledoc, "//txfile/tax"));
				customerTransactionFile[i].setPrice(XMLUtils.getNodeValue(txfiledoc, "//txfile/price"));
				customerTransactionFile[i].setUcprice(XMLUtils.getNodeValue(txfiledoc, "//txfile/ucprice"));
				customerTransactionFile[i].setCompfee(XMLUtils.getNodeValue(txfiledoc, "//txfile/compfee"));
				customerTransactionFile[i].setUpcharge(XMLUtils.getNodeValue(txfiledoc, "//txfile/upcharge"));
				customerTransactionFile[i].setDrexp(XMLUtils.getNodeValue(txfiledoc, "//txfile/drexp"));
				customerTransactionFile[i].setHost(XMLUtils.getNodeValue(txfiledoc, "//txfile/host"));
				customerTransactionFile[i].setUsual(XMLUtils.getNodeValue(txfiledoc, "//txfile/usual"));
				customerTransactionFile[i].setProgadd(XMLUtils.getNodeValue(txfiledoc, "//txfile/progadd"));
				customerTransactionFile[i].setSchdrug(XMLUtils.getNodeValue(txfiledoc, "//txfile/schdrug"));
				customerTransactionFile[i].setGenmesg(XMLUtils.getNodeValue(txfiledoc, "//txfile/genmesg"));
				customerTransactionFile[i].setNscchoice(XMLUtils.getNodeValue(txfiledoc, "//txfile/nscchoice"));
				customerTransactionFile[i].setCounchoice(XMLUtils.getNodeValue(txfiledoc, "//txfile/counchoice"));
				customerTransactionFile[i].setPacmed(XMLUtils.getNodeValue(txfiledoc, "//txfile/pacmed"));
				customerTransactionFile[i].setViaprefill(XMLUtils.getNodeValue(txfiledoc, "//txfile/viaprefill"));
				customerTransactionFile[i].setOthprice(XMLUtils.getNodeValue(txfiledoc, "//txfile/othprice"));
				customerTransactionFile[i].setAcsPriority(XMLUtils.getNodeValue(txfiledoc, "//txfile/acspriority"));
				customerTransactionFile[i].setDecqty(XMLUtils.getNodeValue(txfiledoc, "//txfile/decqty"));
				customerTransactionFile[i].setDeleteTx(XMLUtils.getNodeValue(txfiledoc, "//txfile/deletetx"));
				customerTransactionFile[i].setDispDrugName(MscriptsStringUtils
						.removeSpaceBtwnWords(XMLUtils.getNodeValue(txfiledoc, "//txfile/TxAddendum/dispDrugName")));
				customerTransactionFile[i]
						.setDispDrugNDC(XMLUtils.getNodeValue(txfiledoc, "//txfile/TxAddendum/dispDrugNDC"));
				customerTransactionFile[i]
						.setDispDrugGPI(XMLUtils.getNodeValue(txfiledoc, "//txfile/TxAddendum/dispDrugGPI"));
				customerTransactionFile[i].setWillCallReady(XMLUtils.getNodeValue(txfiledoc, "//txfile/willCallReady"));

				// Fix for multiple insurance card 
				// To Do - Add configuration here after the code merge
				if (cvCopayEnabled.equals("1")) {
					customerTransactionTxtpfile = XMLUtils.getNodeList(txfiledoc, "//txfile/txtpfile");
					// setting the copay amount received in the notification
					if (customerTransactionTxtpfile != null && customerTransactionTxtpfile.getLength() > 0) {
						// setting the copay amount received in the notification
						
						List<CustomerTransactionTxtpfile> customerTransactionTxtpFileList=parseCustomerTransactionTxtpfile(clientID,customerTransactionTxtpfile);
						customerTransactionFile[i].setCustomerTransactionTxtpfile(customerTransactionTxtpFileList);
						customerTransactionFile[i]
								.setCopay(customerTransactionTxtpFileList.get(0).getCopay());
					}
				}
				//If Sold date present, status ="sold"
				if (customerTransactionFile[i].getSolddate() != null
						&& !customerTransactionFile[i].getSolddate().equals(" ")) {
					customerTransactionFile[i].setTxstatus(cvTxFileSold);
				} //If WCR flag is "Y", status ="WCR"
				else if (customerTransactionFile[i].getWillCallReady() != null
						&& customerTransactionFile[i].getWillCallReady().equals(cvTxFileWillCallReadyFlag)) {
					customerTransactionFile[i].setTxstatus(cvTxFileWCR);
				} //If Filled date is present and its not Sold or WCR, status ="Fill"
				else if (customerTransactionFile[i].getFilleddate() != null
						&& !customerTransactionFile[i].getFilleddate().equals(" ")) {
					customerTransactionFile[i].setTxstatus(cvTxFileFilled);
				}
			}
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while parsing customer prescription:", ex);
			throw new mscriptsException(null, errorSource, errorSeverity, ex);
		}

		return customerTransactionFile;
	}
	
	/**
	 * Method to parse the txtpfile node received in the update notification
	 * 
	 * @param clientID - String containing the ID from the clients table
	 * @param customerTransactionTxtpfile - Contains the insurance transaction details
	 * @return List of CustomerTransactionTxtpfile
	 * @throws MscriptsException
	 */
	public List<CustomerTransactionTxtpfile> parseCustomerTransactionTxtpfile(String clientID,NodeList customerTransactionTxtpfile)
			throws MscriptsException {

		String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-parseCustomerTransactionTxtpfile";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		
		List<CustomerTransactionTxtpfile> customerTransactionTxtpfileList = new ArrayList<CustomerTransactionTxtpfile>();
		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into parsing customer transactions txtpfile method");
		}
		try {
			// looping through all the <txtpfile> node
			for (int i = 0; i < customerTransactionTxtpfile.getLength(); i++) {
				Document txtpfiledoc = XMLUtils
						.createXMLDocument(XMLUtils.nodeToString(customerTransactionTxtpfile.item(i)));
				CustomerTransactionTxtpfile customerTxtpFile = new CustomerTransactionTxtpfile();
				customerTxtpFile.setCounter(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/counter"));
				customerTxtpFile.setCopay(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/copay"));
				customerTxtpFile.setId(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/id"));
				customerTxtpFile.setCard(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/card"));
				customerTxtpFile.setBalance(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/balance"));
				customerTxtpFile.setTxtpPrice(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/price"));
				customerTxtpFile.setTxtpCost(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/cost"));
				customerTxtpFile.setPaid(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/paid"));
				customerTxtpFile.setIncent(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/incent"));
				customerTxtpFile.setOrigtype(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/origtype"));
				customerTxtpFile.setPlan(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/plan"));
				customerTxtpFile.setTxtpTax(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/tax"));
				customerTxtpFile.setTxtpCompfee(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/compfee"));
				customerTxtpFile.setPlanPCN(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/TxTPAddendum/planPCN"));
				customerTxtpFile.setPlanName(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/TxTPAddendum/planName"));
				customerTxtpFile.setPlanBin(XMLUtils.getNodeValue(txtpfiledoc, "//txtpfile/TxTPAddendum/planBin"));
				customerTransactionTxtpfileList.add(customerTxtpFile);
			}
			// Sorting to find the latest counter node
			if (customerTransactionTxtpfileList != null && customerTransactionTxtpfileList.size() > 1) {
				Collections.sort(customerTransactionTxtpfileList, new Comparator<CustomerTransactionTxtpfile>() {
					@Override
					public int compare(CustomerTransactionTxtpfile a, CustomerTransactionTxtpfile b) {
						try {
							return b.compareTo(a);
						} catch (Exception e) {
							LOGGER_NON_PHI.error("Error encountered while comparing INTEGER equivalent of "
									+ a.getCounter() + " and " + b.getCounter());
							return 0;
						}
					}
				});
			}

		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while parsing customer prescription txtpfile:", ex);
			throw new MscriptsException(clientID, ex.getMessage(), ex, errorSeverity);
		}
		return customerTransactionTxtpfileList;
	}

	public void sendRxPickupReminder(String clientID, Map<String, Object> rriSendReminderMap,
			Map<String, String> migrationResponse) throws mscriptsException {

		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into sending reminder method");
		}

		String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-sendReminder";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		Map refillDetails = new HashMap();
		String rxNumber = null;
		String showRxNameVal = null;
		String apiMsgID = null;
		String customerID = null;
		String communicationID = null;
		String mscriptsEntityID = null;
		String mobile = null;
		String messageContent = null;
		String apnsMessageContent = null;
		String gcmsMessageContent = null;
		String recordID = null;
		String errorNotes = null;
		String txNumber = null;
		String smsTemplate = null;
		String onphoneTemplate = null;
		String onphonePlaceHolderLengths = null;
		String deviceID = null;
		String gcmDeviceID = null;
		int actualServiceCount = 0;
		int serviceCount = 0;
		String shortCodeServiceID = "";
		String shortCodeUserName = "";
		String shortCode = "";
		String emailTemplate = "";
		String days = "";
		String quantity = "";
		String refNum = "";
		// copay changes MCE-4
		String copay = null;
		int cvIsError = 0;
		String ivrTemplateId = null;
		SendCommunicationMode sendCommunication = new SendCommunicationMode();
		try {
			String cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgStatus");
			String cvAdmin = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvAdmin");
			String cvShowRxNameY = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvShowRxNameY");
			String cvMsgTypeSMS = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgTypeSMS");
			String cvMsgTypeApns = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgTypeApns");
			String cvMsgTypeEmail = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgTypeEmail");
			String cvMsgTypeGcms = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMsgTypeGcms");
			String secondaryKey = pHICredentials.getSecondaryKey();
			String cvClientname = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvClientName");
			String cvSupportPhoneNumber = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvSupportPhoneNumber");
			String cvSupportEmailAddress = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvSupportEmailAddress");
			String cvTosUrl = null;
			String cvAccelaretRxPickupType = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvAccelaretRxPickupType");
			String cvMaskRegexExpression = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvMaskRegexExpression");
			String cvMaskPattern = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvMaskPattern");

			LOGGER_NON_PHI.info(rriSendReminderMap);
			if (rriSendReminderMap != null) {
				days = rriSendReminderMap.get("days") == null ? null
						: rriSendReminderMap.get("days").toString();
				quantity = rriSendReminderMap.get("quantity") == null ? null
						: rriSendReminderMap.get("quantity").toString();
				refNum = rriSendReminderMap.get("refill_number") == null ? null
						: rriSendReminderMap.get("refill_number").toString();
				emailTemplate = rriSendReminderMap.get("email_text") == null ? null
						: rriSendReminderMap.get("email_text").toString();
				String cvtWSSLinkforEmails = rriSendReminderMap.get("cvtWSSLinkforEmails") == null ? null
						: rriSendReminderMap.get("cvtWSSLinkforEmails").toString();
				String mscriptsToken = rriSendReminderMap.get("mscripts_token") == null ? null
						: rriSendReminderMap.get("mscripts_token").toString();
				shortCode = rriSendReminderMap.get("shortcode") == null ? null
						: rriSendReminderMap.get("shortcode").toString();
				shortCodeUserName = rriSendReminderMap.get("shortcode_username") == null ? null
						: rriSendReminderMap.get("shortcode_username").toString();
				shortCodeServiceID = rriSendReminderMap.get("shortcode_serviceid") == null ? null
						: rriSendReminderMap.get("shortcode_serviceid").toString();
				//Gets recordID from rriRowSet
				recordID = rriSendReminderMap.get("id") == null ? null : rriSendReminderMap.get("id").toString();
				String customer_firstname = rriSendReminderMap.get("customer_first_name") == null ? null
						: rriSendReminderMap.get("customer_first_name").toString();
				//Gets customerID from rriRowSet
				customerID = rriSendReminderMap.get("customer_id") == null ? null
						: rriSendReminderMap.get("customer_id").toString();
				//Gets communicationID from rriRowSet
				communicationID = rriSendReminderMap.get("communication_id") == null ? null
						: rriSendReminderMap.get("communication_id").toString();
				//Gets mscriptsEntityID from rriRowSet
				mscriptsEntityID = rriSendReminderMap.get("customer_prescription_id") == null ? null
						: rriSendReminderMap.get("customer_prescription_id").toString();
				rxNumber = rriSendReminderMap.get("rxNumber") == null ? null
						: rriSendReminderMap.get("rxNumber").toString();
				txNumber = rriSendReminderMap.get("tx_num") == null ? null
						: rriSendReminderMap.get("tx_num").toString();
				showRxNameVal = rriSendReminderMap.get("show_rx_name") == null ? "0"
						: rriSendReminderMap.get("show_rx_name").toString();
				mobile = rriSendReminderMap.get("mobile") == null ? null : rriSendReminderMap.get("mobile").toString();
				deviceID = rriSendReminderMap.get("deviceid") == null ? null
						: rriSendReminderMap.get("deviceid").toString();
				gcmDeviceID = rriSendReminderMap.get("gcm_deviceid") == null ? null
						: rriSendReminderMap.get("gcm_deviceid").toString();
				
				sendCommunication.setSendSms(String.valueOf(rriSendReminderMap.get("send_sms") == null ? "0"
						:String.valueOf(rriSendReminderMap.get("send_sms"))));
				sendCommunication.setSendGcms(String.valueOf(rriSendReminderMap.get("send_gcms") == null ? "0"
						:String.valueOf(rriSendReminderMap.get("send_gcms"))));
				sendCommunication.setSendApns(String.valueOf(rriSendReminderMap.get("send_apns") == null ? "0"
						:String.valueOf(rriSendReminderMap.get("send_apns"))));
				sendCommunication.setSendEmail(String.valueOf(rriSendReminderMap.get("send_email") == null ? "0"
						:String.valueOf(rriSendReminderMap.get("send_email"))));
				sendCommunication.setSendIvr(String.valueOf(rriSendReminderMap.get("send_ivr") == null ? "0"
						:String.valueOf(rriSendReminderMap.get("send_ivr"))));
				
				ivrTemplateId = rriSendReminderMap.get("ivrTemplateId") == null ? null
						: rriSendReminderMap.get("ivrTemplateId").toString();
				String restockingMessage = rriSendReminderMap.get("restocking_message") == null ? ""
						: rriSendReminderMap.get("restocking_message").toString();
				String prescriptionCode = rriSendReminderMap.get("prescription_code") == null ? ""
						: rriSendReminderMap.get("prescription_code").toString();
				String deliveryCode = rriSendReminderMap.get("delivery_code") == null ? null
						: rriSendReminderMap.get("delivery_code").toString();
				//Gets the smsTemplate from rriRowSet if it exists else initialize smsTemplate to nulll
				smsTemplate = rriSendReminderMap.get("sms_text") == null ? null
						: rriSendReminderMap.get("sms_text").toString();
				//Gets the smsTemplate from rriRowSet if it exists else initialize smsTemplate to nulll
				onphoneTemplate = rriSendReminderMap.get("onphone_text") == null ? null
						: rriSendReminderMap.get("onphone_text").toString();
				String storeName = rriSendReminderMap.get("store_name") == null ? null
						: rriSendReminderMap.get("store_name").toString();
				String storeAddressLine1 = rriSendReminderMap.get("address_line_1") == null ? null
						: rriSendReminderMap.get("address_line_1").toString();
				String storeCity = rriSendReminderMap.get("storeCity") == null ? null
						: rriSendReminderMap.get("storeCity").toString();
				String storePhoneNumber = rriSendReminderMap.get("phone_number") == null ? null
						: rriSendReminderMap.get("phone_number").toString();
				String drugName = rriSendReminderMap.get("presc_drug_name") == null ? null
						: rriSendReminderMap.get("presc_drug_name").toString();
				String storeNcpdpId = rriSendReminderMap.get("storeNcpdpId") == null ? null
						: rriSendReminderMap.get("storeNcpdpId").toString();
				//Gets the errorNotes from rriRowSet if it exists else initialize errorNotes to nulll
				errorNotes = rriSendReminderMap.get("error_notes") == null ? null
						: rriSendReminderMap.get("error_notes").toString();
				//Get copay value MCE-4
				copay = rriSendReminderMap.get("copay") == null ? null : rriSendReminderMap.get("copay").toString();
				if (errorNotes == null) {
					errorNotes = "";
				}

				String msgClientId = rriSendReminderMap.get("msg_client_id").toString();
				if (!msgClientId.equals("")) {
					cvClientname = ConfigReader.readConfig(msgClientId, Constants.DEFAULT_LANGUAGE,
							Constants.cvClientName);
				}
				int isPickupEligible = 0;
				//Gets the pharmacy object
				refillDetails.put("customer_firstname", customer_firstname);
				refillDetails.put("store_name", storeName);
				refillDetails.put("store_address_line1", storeAddressLine1);
				refillDetails.put("store_phone_number", storePhoneNumber);
				refillDetails.put("wss_link", cvtWSSLinkforEmails);
				refillDetails.put("clientid", clientID);
				refillDetails.put("mscripts_token", mscriptsToken);
				refillDetails.put("restocking_message", restockingMessage);
				refillDetails.put("prescription_code", prescriptionCode);
				refillDetails.put("delivery_code", deliveryCode);
				refillDetails.put("refill_RX", rxNumber);
				refillDetails.put("client_name", cvClientname);
				// copay changes MCE-4
				refillDetails.put("copay_amount", copay);
				refillDetails.put("support_phone_number", cvSupportPhoneNumber);
				refillDetails.put("support_email_address", cvSupportEmailAddress);
				refillDetails.put("mscriptsEntityId",mscriptsEntityID);
				// misc details added
				refillDetails.put("days", days);
				refillDetails.put("quantity", quantity);
				refillDetails.put("refill_number", refNum);
				//Check if the sms text o email text contain primary store number or primary store alias
				if (smsTemplate.contains("primarystorealias")) {
					try {
						refillDetails.put("store_alias", rriSendReminderMap.get("alias"));
					} catch (Exception ex) {
						refillDetails.put("store_alias", storeName);
					}
				}

				/* Masking Type value
				0 - User Preference for show Rx name 0 then send Rx Number
				0 - User Preference for show Rx name 1 then send Full Rx name
				1 - User Preference for show Rx name 0 then send Rx Number
				1 - User Preference for show Rx name 1 then send Masked Rx Name
				2 - User Preference for show Rx name 0 then send Masked Rx Name
				2 - User Preference for show Rx name 1 then send Full Rx Number
				*/

				String rxNameOrNumber = textMaskingService.maskingTextBasedOnMaskingType(clientID, drugName,
						cvMaskRegexExpression, cvMaskPattern, showRxNameVal, rxNumber);
				// if the customer requires to show the rx name,then invoke the
				// web service to ge the rx name for the rx
				// if (showRxNameVal.compareToIgnoreCase(cvShowRxNameY) == 0) {
				refillDetails.put("refill_RX", rxNameOrNumber);
				String commName = notificationDao.getCommunicationName(communicationID, clientID);
				String cvtCommNameRxReadyWithCopayDeliveryLink= ConfigReader.readConfig(clientID,Constants.DEFAULT_LANGUAGE, "cvtCommNameRxReadyWithCopayDeliveryLink");
				boolean isTextDeliveryEnabled = mscriptsCommonDao.checkTextDeliveyEnabledForClient(clientID);
				
	           if(isTextDeliveryEnabled  && (MscriptsStringUtils.compareStringEquals(cvtCommNameRxReadyWithCopayDeliveryLink,commName))){
				String shortenedURL = new String();
				Map orderUrl= notificationDao.getOrderUrl(clientID, customerID);
				
				boolean isShared = false;
					if (null == orderUrl.get(Constants.ORDER_URL)
							|| orderUrl.get(Constants.ORDER_URL).toString().isEmpty()
							|| orderUrl.get(Constants.ORDER_URL).toString().equalsIgnoreCase("INVALID")) {
						Map map = mscriptsCommonService.isSharedMobile(clientID, "1", mobile.substring(1), null, null);
						if ("TRUE".equalsIgnoreCase((String) map.get("exists"))
								&& "TRUE".equalsIgnoreCase((String) map.get("is_shared"))) {
							isShared = true;
						}
						String cvOrderUrlExpirationPeriod = ConfigReader.readConfig(clientID,
								Constants.DEFAULT_LANGUAGE, "cvOrderUrlExpirationPeriod");
						// Generate SHA Code which should be sent with the SMS
						String shaCode = new GenerateRandom().generateShaHashCode(customerID + mobile);
						String URL = notificationDao.updateMscriptsProxyAccessToken(clientID, customerID, "0", shaCode,
								"orderpage", Constants.DEFAULT_LANGUAGE);
						String clientName = ConfigReader
								.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvClientName").toString();
						String shrStr = "&shr=0";
						if (isShared) {
							shrStr = "&shr=1";
						}
						String redirectParams = "?token=" + shaCode + shrStr;
						String cvURLShortnerServiceUrl = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
								"cvURLShortnerServiceUrl");
						String cvPatientPortalUrlShortnerAppName = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
								"cvPatientPortalUrlShortnerAppName");
						String cvCheckoutLinkExpiryURL = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
								"cvCheckoutLinkExpiryUrl");
						// String shortenedURL = null;
						Integer orderURLExpiryPeriod = ((cvOrderUrlExpirationPeriod != null
								&& cvOrderUrlExpirationPeriod.trim().length() != 0)
										? Integer.parseInt(cvOrderUrlExpirationPeriod)
										: 0);
						shortenedURL = urlShortnerService.URLShortnerServiceWithRedirectParamsAndExpUrl(
								cvURLShortnerServiceUrl, URL, clientName, cvPatientPortalUrlShortnerAppName, redirectParams,
								orderURLExpiryPeriod, cvCheckoutLinkExpiryURL);
						if (null != (orderUrl.get(Constants.ORDER_URL))
								&& orderUrl.get(Constants.ORDER_URL).toString().equalsIgnoreCase("INVALID")) {
							notificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_ORDER_URL,
									new Object[] { shortenedURL, cvOrderUrlExpirationPeriod, customerID, clientID, });
						} else {
							notificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_CUSTOMER_ORDER_URL,
									new Object[] { customerID, clientID, shortenedURL, cvOrderUrlExpirationPeriod });
						}
					} else {
						shortenedURL = orderUrl.get(Constants.ORDER_URL).toString();
					}

					refillDetails.put(Constants.ORDER_URL_KEY, shortenedURL);

				}
			
			

				try {
					if ((mobile != null) && (sendCommunication.getSendSms().equalsIgnoreCase(Constants.NUMERIC_TRUE_STRING))) {
						serviceCount++;
						// Code for Accelerate Rx with PickUp reminders interception
						int cvtCommNamePickupForMUId = 0;
						String cvtCommNamePickupForMUText = null;
						try {
							if (migrationResponse != null) {
								isPickupEligible = (int) (migrationResponse.containsKey("isPickupEligible")
										? migrationResponse.get("isPickupEligible") : isPickupEligible);
							}
							if (isPickupEligible == 1) {
								//flag to disable the rxName
								String cvRxNameDisable = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
										"cvRxNameDisable");
								LOGGER_NON_PHI.info("Pickup Inerception for Migration is enabled.");
								cvtCommNamePickupForMUId = (int) (migrationResponse.containsKey(
										"cvtCommNamePickupForMUId") ? migrationResponse.get("cvtCommNamePickupForMUId")
												: cvtCommNamePickupForMUId);
								cvtCommNamePickupForMUText = migrationResponse.containsKey("cvtCommNamePickupForMUText")
										? migrationResponse.get("cvtCommNamePickupForMUText")
										: cvtCommNamePickupForMUText;
								communicationID = String.valueOf(cvtCommNamePickupForMUId);
								smsTemplate = cvtCommNamePickupForMUText;
								cvTosUrl = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvTosUrl");
								refillDetails.put("tos_url", cvTosUrl);
								// hiding rxName based on flag
								if(("1").equals(cvRxNameDisable)){
									refillDetails.put("refill_RX", rxNumber);
								}
							}
						} catch (Exception ex) {
							LOGGER_NON_PHI.info(
									"Error occured during migration check.Pickup Inerception for Migration is disabled.");
							isPickupEligible = 0;
							cvIsError = 1;
						}
						//Gets the placeHolderLengths from rriRowSet if it exists else initialize placeHolderLengths to nulll
						String placeHolderLengths = rriSendReminderMap.get("sms_placeholder_lengths") == null ? null
								: rriSendReminderMap.get("sms_placeholder_lengths").toString();
						//Assign values to the smsContentsArray[]
						String[] smsContentsArray = smsService.getSmsContent(smsTemplate);
						for (int i = 0; i < smsContentsArray.length; i++) {
							smsContentsArray[i] = //String.valueOf(refillDetails.getClass().getField(smsContentsArray[i]).get(refillDetails));/
									String.valueOf(refillDetails.get(smsContentsArray[i].toString()));
						}
						/* if (placeHolderLengths.equals("") || placeHolderLengths.equals("0")) {
						
						    throw new mscriptsException("Empty placeholder length;", errorSource, errorSeverity, null);
						}*/
						/*    String newSmsContentsArray[];
						if (placeHolderLengths.contains(",")) {
						    String[] placeHoldersAry = placeHolderLengths.split(",");
						    newSmsContentsArray = smsService.sliceSmsContents(placeHoldersAry, smsContentsArray);
						} else {
						    String[] placeHolderAry = {placeHolderLengths};
						    newSmsContentsArray = smsService.sliceSmsContents(placeHolderAry, smsContentsArray);
						}
						if (newSmsContentsArray == null) {
						    throw new mscriptsException("SMS content failed;", errorSource, errorSeverity, null);
						}*/
						//Split smsTemplate and store it in smsTemplateArray[]
						String[] smsTemplateAry = smsTemplate.split("<");
						//Frame the sms contents with the suitable place holders.
						messageContent = smsService.frameSmsContent(smsTemplateAry, smsContentsArray);
						if (messageContent == null) {
							throw new mscriptsException("SMS content failed;", errorSource, errorSeverity, null);
						}

						String sClientIdentifier = rriSendReminderMap.get("client_identifier") == null ? null
								: rriSendReminderMap.get("client_identifier").toString();
						AdhPatientCommn adhPatientCommn = new AdhPatientCommn();
						try {
							String sIsAdherenceEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
									Constants.isAdherenceEnabled);
							LOGGER_PHI.debug("isAdherenceEnabled = {} ", sIsAdherenceEnabled);

							if ("0".equalsIgnoreCase(sIsAdherenceEnabled) || isPickupEligible > 0) {
								adhPatientCommn = null;
							} else {
								adhPatientCommn = isMessageInterceptedByAdherence(clientID, sClientIdentifier,
										customerID, mscriptsEntityID, Constants.cvAdhFirstPickupNotification);
							}
						} catch (Exception e) {
							adhPatientCommn = null;
							LOGGER_NON_PHI.error("Exception occured in Adherence API call: ", e);
						}

						boolean isMsgIntercepted = false;
						
						//[MCE - 2036] To disable text messaging from all communications
						String isTextMessagingEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
								Constants.IS_TEXTMESSAGING_ENABLED);
						try {
							if (adhPatientCommn != null) {
								/*
								 * There could be eligible communications from
								 * multiple programs. This is a rare scenario which
								 * may never happen, but keeping the code ready.
								 */
								List<AdhCommn> adhCommnList = adhPatientCommn.getEligibleCommnList();
								for (AdhCommn adhCommn : adhCommnList) {
									boolean isEligible = adhCommn.isEligible();
									boolean isTextMsg = adhCommn.isTextMsg();

									if (isEligible && isTextMsg) {
										isMsgIntercepted = true;
										String[] sArrayTextMsg = adhCommn.getTextMsg();
										
										//[MCE - 2036] To disable text messaging from all communications
										if(Constants.NUMERIC_TRUE_STRING.equals(isTextMessagingEnabled)) {
											/*
											 * For every 1 message in base product
											 * adherence can send 2 messages. So below
											 * code should be in loop.
											 */
											for (String sTextMsg : sArrayTextMsg) {
												apiMsgID = "ADHMSGID";
												errorNotes = "Message intecepted by mscripts Adherence.";
												
												//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
												String cvUseMessagingEngine = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvUseMessagingEngine");
												
												if(!cvUseMessagingEngine.equals("1")){
													Map smsMap = smsService.sendSms(mobile, sTextMsg, false, shortCode,
															customerID, true, true, clientID, true, shortCodeUserName,
															shortCodeServiceID,null,null);
		
													if (smsMap.containsKey("errorCode")) {
														String errorMessage = smsMap.get("errorCode").toString();
														throw new mscriptsException(errorMessage + ";", errorSource,
																errorSeverity, null);
													} else {
														apiMsgID = smsMap.get("apiMsgID").toString();
														shortCode = smsMap.get("shortCode").toString();
													}
		
													mscriptsCommonDao.insertCommunicationHistories(clientID, customerID,
															communicationID, mscriptsEntityID, cvMsgTypeSMS, sTextMsg,
															shortCode, mobile, null, cvMsgStatus, apiMsgID, null, errorNotes,
															null, cvAdmin, txNumber, secondaryKey);
												}
												else
												{
													//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
													cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
															"cvMsgStatusSentToMsgEngine");
													
													String commHistoriesId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientID, customerID,
															communicationID, mscriptsEntityID, cvMsgTypeSMS, sTextMsg,
															shortCode, mobile, null, cvMsgStatus, apiMsgID, null, errorNotes,
															null, cvAdmin, txNumber, 1,secondaryKey);
													
													Map smsMap = smsService.sendSms(mobile, sTextMsg, false, shortCode,
															customerID, true, true, clientID, true, shortCodeUserName,
															shortCodeServiceID, null,commHistoriesId);
													
													if (smsMap.containsKey("shortCode")) {
														shortCode = smsMap.get("shortCode").toString();
													}
												}
											}
										}//[MCE - 2036] To disable text messaging from all communications
									}
								}
							}
						} catch (Exception e) {
							adhPatientCommn = null;
							LOGGER_NON_PHI.error("Exception occured while processing Adherence API response: ", e);
						}

						/*
						 * actualServiceCount should be incremented based on
						 * actual msg count; not based on adherence msg count
						 */
						if (isMsgIntercepted) {
							sendCommunication.setSendSms(Constants.NUMERIC_FALSE_STRING);
							actualServiceCount++;
						}
						//[MCE - 2036] To disable text messaging from all communications
						// If migration flag is 2 don't send the message
						if ( (adhPatientCommn == null || !isMsgIntercepted ) && (Constants.NUMERIC_TRUE_STRING.equals(isTextMessagingEnabled)) ) {
							
							//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
							String cvUseMessagingEngine = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvUseMessagingEngine");
							
							if(!cvUseMessagingEngine.equals("1")){
							
								Map smsMap = smsService.sendSms(mobile, messageContent, false, shortCode, customerID, true,
										true, clientID, true, shortCodeUserName, shortCodeServiceID, null, null);
	
								if (smsMap.containsKey("errorCode")) {
									String errorMessage = smsMap.get("errorCode").toString();
									cvIsError = 1;
									throw new mscriptsException(errorMessage + ";", errorSource, errorSeverity, null);
								} else {
									apiMsgID = smsMap.get("apiMsgID").toString();
									shortCode = smsMap.get("shortCode").toString();
								}
	
								// Invokes
								// reminderDao.insertCommunicationHistories()
								mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, communicationID,
										mscriptsEntityID, cvMsgTypeSMS, messageContent, shortCode, mobile, null,
										cvMsgStatus, apiMsgID, null, errorNotes, null, cvAdmin, txNumber, secondaryKey);
								
							}
							else
							{
								//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
								cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
										"cvMsgStatusSentToMsgEngine");
								
								String commHistoriesId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientID, customerID, communicationID,
										mscriptsEntityID, cvMsgTypeSMS, messageContent, shortCode, mobile, null,
										cvMsgStatus, apiMsgID, null, errorNotes, null, cvAdmin, txNumber,1, secondaryKey);
								
								Map smsMap = smsService.sendSms(mobile, messageContent, false, shortCode, customerID, true,
										true, clientID, true, shortCodeUserName, shortCodeServiceID, null,commHistoriesId);
								
								if (smsMap.containsKey("shortCode")) {
									shortCode = smsMap.get("shortCode").toString();
								}
								
							}
							try {
								//For accelarte rx pickup optout flow 2 extra messages need to be sent
								if (isPickupEligible == 1
										&& ("optout").equals(ConfigReader.readConfig(clientID,
												Constants.DEFAULT_LANGUAGE, "cvAccelaretRxPickupType"))
										&& ("1").equals(ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
												"cvPrefMessageInPickupForMigration"))) {
									String cvPreferenceMessageCommunicationName = ConfigReader.readConfig(clientID,
											Constants.DEFAULT_LANGUAGE, "cvPreferenceMessageCommunicationName");
									String cvSecondPreferenceMessageCommunicationName = ConfigReader.readConfig(
											clientID, Constants.DEFAULT_LANGUAGE,
											"cvSecondPreferenceMessageCommunicationName");
									// Flag to check second pref message is required or not
									String cvSecondPreferenceCommunicationMsgEnabled = ConfigReader.readConfig(clientID,
											Constants.DEFAULT_LANGUAGE, "cvSecondPreferenceCommunicationMsgEnabled");
									String[] prefMessages;
									if ("1".equalsIgnoreCase(cvSecondPreferenceCommunicationMsgEnabled)) {
										prefMessages = new String[] { cvPreferenceMessageCommunicationName,
												cvSecondPreferenceMessageCommunicationName };
									} else {
										prefMessages = new String[] { cvPreferenceMessageCommunicationName };
									}

									for (String message : prefMessages) {
										Map communicationDetails = generalDao.getCommunicationDetails(message, clientID,
												customerID, mobile, null, secondaryKey);
										String commID = communicationDetails.get("id").toString();
										String smstext = communicationDetails.get("sms_text").toString();
										String[] smsContentArray = smsService.getSmsContent(smstext);
										for (int i = 0; i < smsContentArray.length; i++) {
											smsContentArray[i] = //String.valueOf(refillDetails.getClass().getField(smsContentsArray[i]).get(refillDetails));/
													String.valueOf(refillDetails.get(smsContentArray[i].toString()));
										}
										String[] smsTemplateArray = smstext.split("<");
										//Frame the sms contents with the suitable place holders.
										String finalMessage = smsService.frameSmsContent(smsTemplateArray,
												smsContentArray);
										
										//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
										if(!cvUseMessagingEngine.equals("1")){
											
											Map sendSmsMap = smsService.sendSms(mobile, finalMessage, false, shortCode,
													customerID, true, true, clientID, true, shortCodeUserName,
													shortCodeServiceID,null,null);
	
											if (sendSmsMap.containsKey("errorCode")) {
												String errorMessage = sendSmsMap.get("errorCode").toString();
												cvIsError = 1;
												throw new mscriptsException(errorMessage + ";", errorSource, errorSeverity,
														null);
											} else {
												apiMsgID = sendSmsMap.get("apiMsgID").toString();
												shortCode = sendSmsMap.get("shortCode").toString();
											}
	
											// Invokes
											// reminderDao.insertCommunicationHistories()
											mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, commID,
													mscriptsEntityID, cvMsgTypeSMS, finalMessage, shortCode, mobile, null,
													cvMsgStatus, apiMsgID, null, errorNotes, null, cvAdmin, txNumber,
													secondaryKey);
										}
										else
										{
											//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
											cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
													"cvMsgStatusSentToMsgEngine");
											
											String commHistoriesId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientID, customerID, commID,
													mscriptsEntityID, cvMsgTypeSMS, finalMessage, shortCode, mobile, null,
													cvMsgStatus, apiMsgID, null, errorNotes, null, cvAdmin, txNumber,1,
													secondaryKey);
											
											Map sendSmsMap = smsService.sendSms(mobile, finalMessage, false, shortCode,
													customerID, true, true, clientID, true, shortCodeUserName,
													shortCodeServiceID,null,commHistoriesId);
											
											if (sendSmsMap.containsKey("shortCode")) {
												shortCode = sendSmsMap.get("shortCode").toString();
											}
											
										}

									}

								}
							} catch (Exception e) {
								LOGGER_NON_PHI.info(
										"Error occured during migration check.Pickup Inerception for Migration is disabled.");
							}
							sendCommunication.setSendSms(Constants.NUMERIC_FALSE_STRING);
							actualServiceCount++;
						}
					}
				} catch (mscriptsException mEx) {
					LOGGER_NON_PHI.error(" mscripts Exception occured while sending reminders:", mEx);
					errorNotes = (errorNotes.contains(mEx.getErrorMessage() + ";"
							+ mEx.getErrorObject().getLocalizedMessage() + ";SMS block failed;") ? errorNotes
									: (errorNotes + mEx.getErrorMessage() + ";"
											+ mEx.getErrorObject().getLocalizedMessage() + ";SMS block failed;"));
				} catch (Exception e) {
					LOGGER_NON_PHI.error(" Exception occured while sending reminders:", e);
					errorNotes = (errorNotes.contains(errorNotes + e.toString() + ";SMS block failed;") ? errorNotes
							: (errorNotes + e.toString() + ";SMS block failed;"));
				}

				try {
					//Check for sending APNS
					if (deviceID != null && sendCommunication.getSendApns().equalsIgnoreCase(Constants.NUMERIC_TRUE_STRING)) {
						serviceCount++;
						//Gets the placeHolderLengths from rriRowSet if it exists else initialize placeHolderLengths to nulll
						onphonePlaceHolderLengths = rriSendReminderMap.get("onphone_placeholder_lengths") == null ? null
								: rriSendReminderMap.get("onphone_placeholder_lengths").toString();
						//Assign values to the smsContentsArray[]
						String[] apnsContentsArray = apnsService.getApnsContent(onphoneTemplate);
						for (int i = 0; i < apnsContentsArray.length; i++) {
							apnsContentsArray[i] = //String.valueOf(refillDetails.getClass().getField(apnsContentsArray[i]).get(refillDetails));
									String.valueOf(refillDetails.get(apnsContentsArray[i].toString()));
						}
						//  if (onphonePlaceHolderLengths != null) {
						String onphoneTemplateArray[];
						//String newOnphoneContentsArray[];
						//    if (onphonePlaceHolderLengths.contains(",")) {
						//   String[] onphonePlaceHolderArray = onphonePlaceHolderLengths.split(",");
						//Invokes smsService.sliceSmsContents() [slice the sms contents]
						//   newOnphoneContentsArray = apnsService.sliceApnsContents(onphonePlaceHolderArray, apnsContentsArray);
						//     } else {
						//         String[] onphonePlaceHolderArray = {onphonePlaceHolderLengths};
						//Invokes smsService.sliceSmsContents() [slice the sms contents]
						//         newOnphoneContentsArray = apnsService.sliceApnsContents(onphonePlaceHolderArray, apnsContentsArray);
						//    }
						//   if (newOnphoneContentsArray == null) {
						//    LOGGER_NON_PHI.error(" Throwing mscripts exception as newOnphoneContentsArray is null ");
						//    throw new mscriptsException("APNS content failed;", errorSource, errorSeverity, null);
						//  } else {
						//Split smsTemplate and store it in smsTemplateArray[]
						onphoneTemplateArray = onphoneTemplate.split("<");

						apnsMessageContent = apnsService.frameApnsContent(onphoneTemplateArray, apnsContentsArray);
						if (apnsMessageContent == null) {
							LOGGER_NON_PHI.error(" Throwing mscripts exception as apnsMessageContent is null ");
							throw new mscriptsException("APNS content failed;", errorSource, errorSeverity, null);
						} else {
							HashMap<String, String> configurationObject = new HashMap<String, String>();
							configurationObject.put("cvApnsHost",
									ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvApnsHost"));
							configurationObject.put("cvApnsKeyStorePath",
									(rriSendReminderMap.get("cvApnsKeyStorePath") == null
											? ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
													"cvApnsKeyStorePath")
											: rriSendReminderMap.get("cvApnsKeyStorePath").toString()));
							configurationObject.put("cvApnsKeyPass",
									ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvApnsKeyPass"));
							configurationObject.put("cvApnsPort",
									ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvApnsPort"));

							this.apnsService.sendApns(deviceID, apnsMessageContent, 0, clientID);
							//Invokes reminderDao.insertCommunicationHistories()
							mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, communicationID,
									mscriptsEntityID, cvMsgTypeApns, apnsMessageContent, cvAdmin, deviceID, null,
									cvMsgStatus, "", null, errorNotes, null, cvAdmin, txNumber, secondaryKey);
							sendCommunication.setSendApns(Constants.NUMERIC_FALSE_STRING);
							actualServiceCount++;
							errorNotes = "";
						}
					}
					//}
					// }
				} catch (mscriptsException mEx) {
					LOGGER_NON_PHI.error(" mscripts Exception occured while sending reminders:", mEx);
					errorNotes = (errorNotes.contains(mEx.getErrorMessage() + ";"
							+ mEx.getErrorObject().getLocalizedMessage() + ";APNS block failed;") ? errorNotes
									: (errorNotes + mEx.getErrorMessage() + ";"
											+ mEx.getErrorObject().getLocalizedMessage() + ";APNS block failed;"));
				} catch (Exception e) {
					LOGGER_NON_PHI.error("Exception occured while sending reminders:", e);
					errorNotes = (errorNotes.contains(e.toString() + ";APNS block failed;") ? errorNotes
							: (errorNotes + e.toString() + ";APNS block failed;"));
				}
				try {
					//Check for sending GCM
					if (gcmDeviceID != null && sendCommunication.getSendGcms().equalsIgnoreCase(Constants.NUMERIC_TRUE_STRING)) {
						serviceCount++;
						// Gets the placeHolderLengths from rriRowSet if it exists else initialize
						// placeHolderLengths to nulll
						onphonePlaceHolderLengths = rriSendReminderMap.get("onphone_placeholder_lengths") == null ? null
								: rriSendReminderMap.get("onphone_placeholder_lengths").toString();
						// Assign values to the smsContentsArray[]
						String[] gcmContentsArray = gcmsService.getGcmsContent(onphoneTemplate);
						for (int i = 0; i < gcmContentsArray.length; i++) {
							gcmContentsArray[i] = String.valueOf(refillDetails.get(gcmContentsArray[i].toString()));
						}
						// if (onphonePlaceHolderLengths != null) {
						String onphoneTemplateArray[];
						String newOnphoneContentsArray[];
						/*
						 * if (onphonePlaceHolderLengths.contains(",")) { String[]
						 * onphonePlaceHolderArray = onphonePlaceHolderLengths.split(","); //Invokes
						 * smsService.sliceSmsContents() [slice the sms contents]
						 * newOnphoneContentsArray =
						 * gcmsService.sliceGcmsContents(onphonePlaceHolderArray, gcmContentsArray); }
						 * else { String[] onphonePlaceHolderArray = {onphonePlaceHolderLengths};
						 * //Invokes smsService.sliceSmsContents() [slice the sms contents]
						 * newOnphoneContentsArray =
						 * gcmsService.sliceGcmsContents(onphonePlaceHolderArray, gcmContentsArray); }
						 */
						/*
						 * if (newOnphoneContentsArray == null) {
						 * 
						 * throw new mscriptsException("GCM content failed;", errorSource,
						 * errorSeverity, null); } else {
						 */
						onphoneTemplateArray = onphoneTemplate.split("<");

						gcmsMessageContent = gcmsService.frameGcmsContent(onphoneTemplateArray, gcmContentsArray);
						if (gcmsMessageContent == null) {
							LOGGER_NON_PHI.error(" Throwing mscripts exception as gcmsMessageContent is null ");
							throw new mscriptsException("GCM content failed;", errorSource, errorSeverity, null);
						} else {
							String cvIsFCMPushEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
									"cvIsFCMPushEnabled");
							if ("1".equals(cvIsFCMPushEnabled)) {
								this.fcmService.sendFCM(gcmDeviceID, gcmsMessageContent, clientID);
							} else {
								gcmsService.sendGcms(gcmDeviceID, gcmsMessageContent, 0);
							}
							// Invokes reminderDao.insertCommunicationHistories()
							mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, communicationID,
									mscriptsEntityID, cvMsgTypeGcms, gcmsMessageContent, cvAdmin, gcmDeviceID, null,
									cvMsgStatus, "", null, errorNotes, null, cvAdmin, txNumber, secondaryKey);
							sendCommunication.setSendGcms(Constants.NUMERIC_FALSE_STRING);
							actualServiceCount++;
							errorNotes = "";
						}
					}
					//}
					//}
				} catch (mscriptsException mEx) {
					LOGGER_NON_PHI.error(" Mscripts Exception caught while sending reminders: ", mEx);
					errorNotes = (errorNotes.contains(mEx.getErrorMessage() + ";"
							+ mEx.getErrorObject().getLocalizedMessage() + ";GCM block failed;") ? errorNotes
									: (errorNotes + mEx.getErrorMessage() + ";"
											+ mEx.getErrorObject().getLocalizedMessage() + ";GCM block failed;"));
				} catch (Exception e) {
					LOGGER_NON_PHI.error(" Exception caught while sending reminders: ", e);
					errorNotes = (errorNotes.contains(e.toString() + ";GCM block failed;") ? errorNotes
							: (errorNotes + e.toString() + ";GCM block failed;"));
				}
				//Check for sending EMAIL
				String emailContent = "";
				String emailSender = "";
				String emailAddress = "";
				try {
					if (sendCommunication.getSendEmail().equalsIgnoreCase(Constants.NUMERIC_TRUE_STRING)) {
						serviceCount++;
						emailAddress = rriSendReminderMap.get("email_address") == null ? null
								: rriSendReminderMap.get("email_address").toString();
						//Gets the emailTemplate from rriRowSet if it exists else initialize emailTemplate to nulll
						String emailSubject = rriSendReminderMap.get("email_subject") == null ? null
								: rriSendReminderMap.get("email_subject").toString();
						//Gets the emailTemplate from rriRowSet if it exists else initialize emailTemplate to nulll
						emailSender = rriSendReminderMap.get("email_from") == null ? null
								: rriSendReminderMap.get("email_from").toString();
						//Frame the emailContent						
						String[] newEmailContentsArray = this.smsService.getSmsContent(emailTemplate);
						for (int i = 0; i < newEmailContentsArray.length; i++) {
							newEmailContentsArray[i] = //String.valueOf(refillDetails.getClass().getField(newEmailContentsArray[i]).get(refillDetails));
									String.valueOf(refillDetails.get(newEmailContentsArray[i].toString()));
						}
						String[] receiver = emailAddress.split(",");
						String[] emailTemplateArray = emailTemplate.split("<");

						emailContent = mailService.frameEmailContent(emailTemplateArray, newEmailContentsArray);

						if (emailContent == null) {
							LOGGER_NON_PHI.error("Throwing Mscripts exception as emailcontent is null");
							throw new mscriptsException("Email content failed;", errorSource, errorSeverity, null);
						} else {
							String[] cc = null;
							String[] bcc = null;
							mailService.createMail(receiver, cc, bcc, emailSubject, emailSender, emailContent, true);
							//Invokes insertCommunicationHistories()
							mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, communicationID,
									mscriptsEntityID, cvMsgTypeEmail, emailContent, emailSender, emailAddress, null,
									cvMsgStatus, "", null, errorNotes, null, cvAdmin, null, secondaryKey);
							sendCommunication.setSendEmail(Constants.NUMERIC_FALSE_STRING);
							actualServiceCount++;
						}
					}
				} catch (mscriptsException mEx) {
					LOGGER_NON_PHI.error(" Mscripts Exception caught while sending reminders: ", mEx);
					errorNotes = (errorNotes.contains(mEx.getErrorMessage() + ";"
							+ mEx.getErrorObject().getLocalizedMessage() + ";Email block failed;") ? errorNotes
									: (errorNotes + mEx.getErrorMessage() + ";"
											+ mEx.getErrorObject().getLocalizedMessage() + ";Email block failed;"));
				} catch (Exception e) {
					LOGGER_NON_PHI.error("Exception caught while sending reminders ", e);
					errorNotes = (errorNotes.contains(e.toString() + ";Email block failed;") ? errorNotes
							: (errorNotes + e.toString() + ";Email block failed;"));
				}
				if (sendCommunication.getSendIvr().equalsIgnoreCase(Constants.NUMERIC_TRUE_STRING)) {
					 Map customerMap = new HashMap();
					 customerMap.put("customerId", customerID);
					 customerMap.put("rxNumber", rxNumber);
					 customerMap.put("storeName", storeName);
					 customerMap.put("storeAddressLine1", storeAddressLine1);
					 customerMap.put("storePhoneNumber", storePhoneNumber);
					 customerMap.put("customerFirstName",customer_firstname);
					 customerMap.put("mscriptsEntityId",mscriptsEntityID);
					 customerMap.put("mobileNumber",mobile);
					 customerMap.put("rxName", drugName);
					 customerMap.put("showRxNameVal", showRxNameVal);
					 customerMap.put("copayAmount", copay);
					 customerMap.put("communicationId",communicationID);
					 customerMap.put("ivrTemplateId", ivrTemplateId);
					 customerMap.put("storeCity",storeCity);
					 customerMap.put("storeNcpdpId", storeNcpdpId);
					 String result = sendIvrCommunication(clientID,customerMap);
					 LOGGER_NON_PHI.info(result);
				}
				//On succesfull SMS/Email/APNS reset the flags - send_sms, send_email and send_apns
				//For migrated users delete all bulk rx entry
				if (isPickupEligible > 0) {
					try {
						LOGGER_PHI.info("Deleting entries in bulk rx entries for migrated users. ");
						notificationDao.processPickupReminderForMigratedUser(clientID, customerID, isPickupEligible,
								cvAccelaretRxPickupType, cvIsError);
					} catch (Exception ex) {
						LOGGER_NON_PHI.error(" Exception caught while deleting bulk rx entries for migrated users: ",
								ex);
					}
				} else {
					if (serviceCount == actualServiceCount) {
						notificationDao.deleteBulkRxPickup(clientID, recordID);
					} else {
						notificationDao.updateBulkRxPickupErrorNotes(clientID, recordID, errorNotes, sendCommunication);
					}
				}
			} //end of while
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception caught reading rriSendReminderMap :", ex);
			try {
				//Update bulk_rx_pickup_instance record in db in case of error in sending messages
				notificationDao.updateBulkRxPickupErrorNotes(clientID, recordID, errorNotes, sendCommunication);
				//For migrated users delete all bulk rx entry
				//notificationDao.processPickupReminderForMigratedUser(clientID, customerID, 2, null, 1);
			} catch (Exception e) {
				LOGGER_NON_PHI.error("Exception caught while updating bulk rx pick up error notes :", e);
			}

			throw new mscriptsException(null, errorSource, errorSeverity, ex);
		}
	}

	public void sendAdherenceSoldMessage(String cvPdxClientName, String clientID, String customerID,
			String sCustPrescId, String sMobile, String sShortcode, String sShortcodeUsername,
			String sShortcodeServiceId, String sCommunicationId) throws mscriptsException {
		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into sendAdherenceSoldMessage method");
		}

		String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-sendAdherenceSoldMessage";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		try {
			String sIsAdherenceEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.isAdherenceEnabled);
			if ("0".equalsIgnoreCase(sIsAdherenceEnabled)) {
				return;
			}
			//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
			String cvUseMessagingEngine = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvUseMessagingEngine");
			String cvMsgTypeSMS = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvMsgTypeSMS");
			String cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvMsgStatus");
			String cvAdmin = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvAdmin");
			
			AdhPatientCommn adhPatientCommn = new AdhPatientCommn();

			adhPatientCommn = isMessageInterceptedByAdherence(clientID, cvPdxClientName, customerID, sCustPrescId,
					Constants.cvAdhRefillSold);

			if (adhPatientCommn != null) {
				/*
				 * There could be eligible communications from multiple
				 * programs. This is a rare scenario which may never happen, but
				 * keeping the code ready.
				 */
				List<AdhCommn> adhCommnList = adhPatientCommn.getEligibleCommnList();
				for (AdhCommn adhCommn : adhCommnList) {
					boolean isEligible = adhCommn.isEligible();
					boolean isTextMsg = adhCommn.isTextMsg();
					
					//[MCE - 2036] To disable all text messaging for all communications
					String isTextMessagingEnabled =  ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, Constants.IS_TEXTMESSAGING_ENABLED);
					if (isEligible && isTextMsg && Constants.NUMERIC_TRUE_STRING.equals(isTextMessagingEnabled)) {
						String[] sArrayTextMsg = adhCommn.getTextMsg();

						/*
						 * For every 1 message in base product adherence can
						 * send 2 messages. So below code should be in loop.
						 */
						for (String sTextMsg : sArrayTextMsg) {
							String apiMsgID = "ADHMSGID";
							String errorNotes = "Message intecepted by mscripts Adherence.";
							
							//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
							if(!cvUseMessagingEngine.equals("1")){
								
								Map smsMap = smsService.sendSms(sMobile, sTextMsg, false, sShortcode, customerID, true,
										true, clientID, true, sShortcodeUsername, sShortcodeServiceId, null, null);
	
								if (smsMap.containsKey("errorCode")) {
									String errorMessage = smsMap.get("errorCode").toString();
									throw new mscriptsException(errorMessage + ";", errorSource, errorSeverity, null);
								} else {
									apiMsgID = smsMap.get("apiMsgID").toString();
									sShortcode = smsMap.get("shortCode").toString();
								}
								
								mscriptsCommonDao.insertCommunicationHistories(clientID, customerID, sCommunicationId,
										sCustPrescId, cvMsgTypeSMS, sTextMsg, sShortcode, sMobile, null, cvMsgStatus,
										apiMsgID, null, errorNotes, null, cvAdmin, null, pHICredentials.getSecondaryKey());
							}
							else
							{
								//[PHA - 2692] [kgupta] Changing the SMS call for CLX adaptor change
								cvMsgStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
										"cvMsgStatusSentToMsgEngine");
								
								String commHistoriesId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientID, customerID, sCommunicationId,
										sCustPrescId, cvMsgTypeSMS, sTextMsg, sShortcode, sMobile, null, cvMsgStatus,
										apiMsgID, null, errorNotes, null, cvAdmin, null,1, pHICredentials.getSecondaryKey());
								
								Map smsMap = smsService.sendSms(sMobile, sTextMsg, false, sShortcode, customerID, true,
										true, clientID, true, sShortcodeUsername, sShortcodeServiceId,null,commHistoriesId);
								
								if (smsMap.containsKey("shortCode")) {
									sShortcode = smsMap.get("shortCode").toString();
								}
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception caught in processing in sendAdherenceSoldMessage method ", ex);
			// suppress exception here.
		}
	}

	private AdhPatientCommn isMessageInterceptedByAdherence(String clientID, String clientName, String customerID,
			String mscriptsEntityID, String messageType) throws Exception {
		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into isMessageInterceptedByAdherence method.");
		}

		String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-isMessageInterceptedByAdherence";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		try {
			Gson gson = new Gson();
			String sJsonRequest = null;

			AdhInterceptMsgRequest interceptMessage = new AdhInterceptMsgRequest();
			interceptMessage.setPharmacyIdentifier(clientName);
			interceptMessage.setPatientId(Long.parseLong(customerID.trim()));
			interceptMessage.setPatientRxId(Long.parseLong(mscriptsEntityID.trim()));
			interceptMessage.setMediation(messageType);
			sJsonRequest = gson.toJson(interceptMessage);

			String cvAdherencePostURL = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.cvAdhPostURL);
			String cvInterceptMethod = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.cvAdhInterceptMethod);

			String sJsonResponse = HTTPRequestPoster.doPost("xyz", cvAdherencePostURL + cvInterceptMethod,
					sJsonRequest);
			LOGGER_PHI.debug("Adh API: request = {}, response = {}", sJsonRequest, sJsonResponse);

			if (sJsonResponse.contains("error")) {
				// Error reason is not utilized in code for now.
				AdhError adhError = gson.fromJson(sJsonResponse, AdhError.class);
				return null;
			} else {
				AdhPatientCommn adhPatientCommn = gson.fromJson(sJsonResponse, AdhPatientCommn.class);
				return adhPatientCommn;
			}
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured in Adherence API call:", ex);
			throw new mscriptsException(null, errorSource, errorSeverity, ex);
		}
	}

	@Override
	public void updateRxcomId(String sClientId, String sCustomerId, String sRxcomId) throws mscriptsException {
		try {
			LOGGER_NON_PHI.info("Updating RxcomId for customer id ={}", sCustomerId);

			this.getNotificationDao().updateRxcomId(sClientId, sCustomerId, sRxcomId);
		} catch (mscriptsException ex) {
			LOGGER_NON_PHI.error(" mscripts Exception occured while updating customer Rxcom Id:", ex);
			throw ex;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(" Exception occured while updating customer Rxcom Id={}", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.service.NotificationServiceImpl-updateRxcomId",
					mscriptsExceptionSeverity.Medium, ex);
		}
	}
	
	public String sendIvrCommunication(String clientId, Map customerMap) {
		int messageCount = 0;
		String errorNotes = null;
		String customerId = null;
		String rxNumber = null;
		String storeName = null;
		String storeAddressLine1 = null;
		String storePhoneNumber = null;
		String customerFirstName = null;
		String mscriptsEntityId = null;
		String mobileNumber = null;
		String rxName = null;
		String showRxNameVal = null;
		String message = null;
		String copayAmount = null;
		String communicationId = null;
		String ivrTemplateId = null;
		String storeCity = null;
		try {

			String reminderLowerCutoffTime = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					Constants.cvtReminderLowerCutoffTime);
			String reminderUpperCutoffTime = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					Constants.cvtReminderUpperCutoffTime);
			String msgEngineCallBackURL = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					Constants.CVMSGENGINECALLBACKURL);
			String cvMsgTypeIvr = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE, Constants.CVMSGTYPEIVR);
			String cvAdmin = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE, Constants.cvAdmin);
			String ivrCommunicationType = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					Constants.CVSINGLEPICKUPIVRPROCESS);
			String cvMessageStatus = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE, Constants.CVIVRENGINESTATUS);
			String cvCopayEnabled = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE, Constants.cvCopayEnabled);
			String cvMaskRegexExpression = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					ConfigKeys.CV_MASK_REG_EXPRESSION);
			String cvMaskPattern = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE, ConfigKeys.CV_MASK_PATTERN);
			
			if (!MscriptsStringUtils.isMapEmptyOrNull(customerMap)) {
				if (null != customerMap.get("customerId")) {
					customerId = String.valueOf(customerMap.get("customerId"));
				}
				if (null != customerMap.get("rxNumber")) {
					rxNumber = String.valueOf(customerMap.get("rxNumber"));
				}
				if (null != customerMap.get("storeName")) {
					storeName = String.valueOf(customerMap.get("storeName"));
				}
				if (null != customerMap.get("storeAddressLine1")) {
					storeAddressLine1 = String.valueOf(customerMap.get("storeAddressLine1"));
				}
				if (null != customerMap.get("storePhoneNumber")) {
					storePhoneNumber = String.valueOf(customerMap.get("storePhoneNumber"));
				}
				if (null != customerMap.get("customerFirstName")) {
					customerFirstName = String.valueOf(customerMap.get("customerFirstName"));
				}
				if (null != customerMap.get("mscriptsEntityId")) {
					mscriptsEntityId = String.valueOf(customerMap.get("mscriptsEntityId"));
				}
				if (null != customerMap.get("mobileNumber")) {
					mobileNumber = String.valueOf(customerMap.get("mobileNumber"));
				}
				if (null != customerMap.get("rxName")) {
					rxName = String.valueOf(customerMap.get("rxName"));
				}
				if (null != customerMap.get("showRxNameVal")) {
					showRxNameVal = String.valueOf(customerMap.get("showRxNameVal"));
				}
				if (null != customerMap.get("communicationId")) {
					communicationId = String.valueOf(customerMap.get("communicationId"));
				}
				if (null != customerMap.get("storeCity")) {
					storeCity = String.valueOf(customerMap.get("storeCity"));
				}
			}

			LOGGER_PHI.debug("Sending instantaneous pickup reminder to customer = {} via IVR (Phone call) for Rx = {}",
					customerId, rxNumber);
			if(cvCopayEnabled.contentEquals(Constants.NUMERIC_TRUE_STRING)){
				if (null != customerMap.get("copayAmount")) {
					copayAmount = String.valueOf(customerMap.get("copayAmount"));
					ivrTemplateId = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
							Constants.CVSINGLEPICKUPWITHCOPAYTEMPLATEID);
				}		
			}
			else {
				ivrTemplateId = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
					Constants.CVSINGLEPICKUPWITHOUTCOPAYLTEMPLATEID);
			}
			String customerPreference = ivrService.fetchCustomerPreference(clientId, showRxNameVal);
			List<Map<String, String>> prescriptionList = new ArrayList<>();
			HashMap<String, String> prescriptionMap = new HashMap<>();
			//prescriptionMap.put("storeDetails", storeName + ", " + storeAddressLine1 + ", " + storeCity);
			//BASE-3329 - Split storeDetails field
			prescriptionMap.put("storeName", storeName);
			prescriptionMap.put("storeAddressLine", storeAddressLine1);
			prescriptionMap.put("storeCity", storeCity);
			
			String rxNameOrNumber = textMaskingService.maskingTextBasedOnMaskingType(clientId, rxName,
					cvMaskRegexExpression, cvMaskPattern, showRxNameVal, rxNumber);
			prescriptionMap.put(Constants.RXNUMBER, rxNumber);
			prescriptionMap.put(Constants.RXNAME, rxName);
			prescriptionMap.put(customerPreference, rxNameOrNumber);
			
			prescriptionMap.put("copay", copayAmount);
			prescriptionMap.put("rxNumId", mscriptsEntityId);
			prescriptionList.add(prescriptionMap);
			storePhoneNumber = MiscUtils.formatPhoneNumber(storePhoneNumber);
			Map optionalMap = new HashMap();
			optionalMap.put("callbackRefillUrl", msgEngineCallBackURL);
			optionalMap.put("pharmacyPhoneNumber", storePhoneNumber);
			optionalMap.put("cutOffStartTime", reminderLowerCutoffTime);
			optionalMap.put("cutOffEndTime", reminderUpperCutoffTime);
			optionalMap.put("prescriptions", prescriptionList);
			optionalMap.put("templateId", ivrTemplateId);
			optionalMap.put("customerName", customerFirstName);
			optionalMap.put("ivrCommunicationType", ivrCommunicationType);
			optionalMap.put("customerPreference", customerPreference);
			messageCount++;
			String commHistoriesId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientId, customerId,
					communicationId, mscriptsEntityId, cvMsgTypeIvr, String.valueOf(optionalMap), storePhoneNumber,
					mobileNumber, null, cvMessageStatus, "", null, errorNotes, null, cvAdmin, null, messageCount,
					pHICredentials.getSecondaryKey());
			LOGGER_PHI.debug("communication Details added to communicationHistories for customer {}", customerId);
			Map responseMap = ivrService.sendIvr(clientId, commHistoriesId, message, null, mobileNumber, customerId,
					Constants.DEFAULT_LANGUAGE, optionalMap);
			mscriptsCommonDao.updateCommunicationHistories(clientId, commHistoriesId,
					String.valueOf(responseMap.get("postRequest")), pHICredentials.secondaryKey);
			LOGGER_NON_PHI.debug("Ivr communication sent successfully");
			return Constants.IVRSUCCESS;
		} catch (MscriptsException mEx) {
			LOGGER_PHI.error(
					"Exception occured while processing  instantaneous pickup Reminder for customerID = {} rxNumber = {} via IVR {}",
					customerId, rxNumber, mEx);
			StringBuilder sb = new StringBuilder().append(mEx.getErrorMessage()).append(";")
					.append(mEx.getErrorObject().getLocalizedMessage()).append(";").append(Constants.IVRBLOCKFAILED)
					.append(":");
			errorNotes = errorNotes.contains(sb) ? errorNotes : errorNotes.concat(sb.toString());
			return errorNotes;

		} catch (Exception e) {

			LOGGER_PHI.error(
					"Exception occured while processing instantaneous pickup Reminder for customerID = {} rxNumber = {} via IVR {}",
					customerId, rxNumber, e);
			StringBuilder sb = new StringBuilder().append(errorNotes).append(e.toString()).append(";IVR block failed;");
			errorNotes = errorNotes.contains(sb) ? errorNotes : errorNotes.concat(sb.toString());
			return errorNotes;
		}
	}


	public MscriptsCommonDao getMscriptsCommonDao() {
		return mscriptsCommonDao;
	}

	public void setMscriptsCommonDao(MscriptsCommonDao mscriptsCommonDao) {
		this.mscriptsCommonDao = mscriptsCommonDao;
	}

	public NotificationDao getNotificationDao() {
		return notificationDao;
	}

	public void setNotificationDao(NotificationDao notificationDao) {
		this.notificationDao = notificationDao;
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

	public void setTransactionManager(DataSourceTransactionManager transactionManagefr) {
		this.transactionManager = transactionManagefr;
	}

	public GeneralDao getGeneralDao() {
		return generalDao;
	}

	public void setGeneralDao(GeneralDao generalDao) {
		this.generalDao = generalDao;
	}

	public SmsService getSmsService() {
		return smsService;
	}

	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}

	public APNSService getApnsService() {
		return apnsService;
	}

	public void setApnsService(APNSService apnsService) {
		this.apnsService = apnsService;
	}

	public GCMSService getGcmsService() {
		return gcmsService;
	}

	public void setGcmsService(GCMSService gcmsService) {
		this.gcmsService = gcmsService;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public PHICredentials getpHICredentials() {
		return pHICredentials;
	}

	public void setpHICredentials(PHICredentials pHICredentials) {
		this.pHICredentials = pHICredentials;
	}

	public FCMService getFcmService() {
		return fcmService;
	}

	public void setFcmService(FCMService fcmService) {
		this.fcmService = fcmService;
	}

	public NotificationFilterUtil getNotificationFilterUtil() {
		return notificationFilterUtil;
	}

	public void setNotificationFilterUtil(NotificationFilterUtil notificationFilterUtil) {
		this.notificationFilterUtil = notificationFilterUtil;
	}

	public TextMaskingService getTextMaskingService() {
		return textMaskingService;
	}

	public void setTextMaskingService(TextMaskingService textMaskingService) {
		this.textMaskingService = textMaskingService;
	}
	public MscriptsCommonService getMscriptsCommonService() {
		return mscriptsCommonService;
	}

	public IvrService getIvrService() {
		return ivrService;
	}

	public void setIvrService(IvrService ivrService) {
		this.ivrService = ivrService;
	}

	public void setMscriptsCommonService(MscriptsCommonService mscriptsCommonService) {
		this.mscriptsCommonService = mscriptsCommonService;
	}
	public URLShortner getUrlShortnerService() {
		return urlShortnerService;
	}

	public void setUrlShortnerService(URLShortner urlShortnerService) {
		this.urlShortnerService = urlShortnerService;
	}
	
	public DrugFilterCriteriaUtil getDrugFilterCriteriaUtil() {
		return drugFilterCriteriaUtil;
	}

	public void setDrugFilterCriteriaUtil(DrugFilterCriteriaUtil drugFilterCriteriaUtil) {
		this.drugFilterCriteriaUtil = drugFilterCriteriaUtil;
	}

	@Override
	public void updateRecordType(String clientId, String customerId, String recordType) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {
			LOGGER_NON_PHI.info("Updating recordType for customer id = {}", customerId);

			notificationDao.updateRecordType(clientId, customerId, recordType);
		} catch (MscriptsException ex) {
			LOGGER_NON_PHI.error("Error occured while updating customer record type:", ex);
			throw ex;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured while updating customer record type = {}", ex);
			throw new MscriptsException(clientId, "Exception occurred while executing updateRecordType", ex, errorSeverity);
		}
	}
}
