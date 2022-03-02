package com.mscripts.externalrequesthandler.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mscripts.commonintegrator.service.MessageService;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.MscriptsCommonDao;
import com.mscripts.dao.QueryInvoker;
import com.mscripts.dispensing.invocation.domain.PdxDispensingRequest;
import com.mscripts.dispensing.invocation.domain.PdxDispensingResponse;
import com.mscripts.dispensing.invocation.domain.Prescription;
import com.mscripts.dispensing.invocation.service.IDispensingPatientService;
import com.mscripts.domain.SendCommunicationMode;
import com.mscripts.enums.CommunicationTemplateStrings;
import com.mscripts.enums.DrugFilterType;
import com.mscripts.enums.NotificationTypeKey;
import com.mscripts.enums.PdxContactReason;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.PatientNotificationDao;
import com.mscripts.externalrequesthandler.domain.CreditCard;
import com.mscripts.externalrequesthandler.domain.MedSyncData;
import com.mscripts.externalrequesthandler.domain.PatientData;
import com.mscripts.externalrequesthandler.domain.PatientNotificationRequest;
import com.mscripts.externalrequesthandler.domain.PatientNotificationResponse;
import com.mscripts.externalrequesthandler.domain.PharmacyData;
import com.mscripts.externalrequesthandler.domain.SendBulkReminders;
import com.mscripts.externalrequesthandler.domain.TransactionData;
import com.mscripts.externalrequesthandler.utils.EOPNUtils;
import com.mscripts.service.MscriptsCommonService;
import com.mscripts.txndata.domain.DawCode;
import com.mscripts.txndata.service.DawCodeService;
import com.mscripts.txndata.service.PatientRxTxnMedianService;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.DrugFilterCriteriaUtil;
import com.mscripts.utils.ErrorCodes;
import com.mscripts.utils.FrameMessagesUtil;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.MscriptsStringUtils;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.QueryBuilder;
import com.mscripts.utils.mscriptsExceptionSeverity;

public class PatientNotificationServiceImpl implements PatientNotificationService {

	private PHICredentials pHICredentials;
	private QueryInvoker queryInvoker;
	private MessageService messageService;
	private PatientNotificationDao patientNotificationDao;
	private EOPNUtils eopnUtils;
	private PatientRxTxnMedianService patientRxTxnMedianService;
	private DawCodeService dawCodeService;
	private MscriptsCommonService mscriptsCommonService;
	private IDispensingPatientService dispensingPatientService;
	private MscriptsCommonDao mscriptsCommonDao;
    private DrugFilterCriteriaUtil drugFilterCriteriaUtil;
    
	private static final Logger LOGGER_NON_PHI = LogManager
			.getLogger("non.phi." + PatientNotificationServiceImpl.class.getName());
	private static final Logger LOGGER_PHI = LogManager
			.getLogger("phi." + PatientNotificationServiceImpl.class.getName());
	
			public static final String USER_NOT_ELIGIBLE_TEXT_MSG = "User is NOT Eligible to receive Text message";

	
			
	public MscriptsCommonDao getMscriptsCommonDao() {
		return mscriptsCommonDao;
	}



	public void setMscriptsCommonDao(MscriptsCommonDao mscriptsCommonDao) {
		this.mscriptsCommonDao = mscriptsCommonDao;
	}



	public PHICredentials getpHICredentials() {
		return pHICredentials;
	}



	public void setpHICredentials(PHICredentials pHICredentials) {
		this.pHICredentials = pHICredentials;
	}



	public QueryInvoker getQueryInvoker() {
		return queryInvoker;
	}



	public void setQueryInvoker(QueryInvoker queryInvoker) {
		this.queryInvoker = queryInvoker;
	}



	public MessageService getMessageService() {
		return messageService;
	}



	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}



	public PatientNotificationDao getPatientNotificationDao() {
		return patientNotificationDao;
	}



	public void setPatientNotificationDao(PatientNotificationDao patientNotificationDao) {
		this.patientNotificationDao = patientNotificationDao;
	}



	public EOPNUtils getEopnUtils() {
		return eopnUtils;
	}



	public void setEopnUtils(EOPNUtils eopnUtils) {
		this.eopnUtils = eopnUtils;
	}

	public PatientRxTxnMedianService getPatientRxTxnMedianService() {
		return patientRxTxnMedianService;
	}
public void setPatientRxTxnMedianService(PatientRxTxnMedianService patientRxTxnMedianService) {
		this.patientRxTxnMedianService = patientRxTxnMedianService;
	}



	public DawCodeService getDawCodeService() {
		return dawCodeService;
	}



	public void setDawCodeService(DawCodeService dawCodeService) {
		this.dawCodeService = dawCodeService;
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



	public DrugFilterCriteriaUtil getDrugFilterCriteriaUtil() {
		return drugFilterCriteriaUtil;
	}



	public void setDrugFilterCriteriaUtil(DrugFilterCriteriaUtil drugFilterCriteriaUtil) {
		this.drugFilterCriteriaUtil = drugFilterCriteriaUtil;
	}



	@Override
	public PatientNotificationResponse processPatientNotification(PatientNotificationRequest patientNotification,
			String requestString) throws MscriptsException {
		PatientNotificationResponse patientNotificationResponse = new PatientNotificationResponse();
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		String langCode = Constants.LANG_CODE;

		String secondaryKey = pHICredentials.getSecondaryKey();

		List<PatientData> patientData = null;
		List<PharmacyData> pharmacyData;
		List<TransactionData> transactionData;

		Map<String, String> encryptionKeyMap = null;
		Map<String, String> clientIdMap = null;
		Map<String, String> patientDetailsMap = null;
		Map<String, String> countMessagesMap = null;
		Map<String, String> customerTextPreference = null;
		Map<String, String> notificationTimeMap = null;
		Map<String, String> customerPreference = null;

		String finalEncryptionKey = null;
		String clientId = null;
		String customerId = null;
		String contactReason = null;

		boolean bReprocessPatientNotification = false;

		SimpleDateFormat formatter = null;
		SimpleDateFormat formatterInput = null;

		if (pHICredentials == null || queryInvoker == null || messageService == null
				|| patientNotificationDao == null) {
			LOGGER_NON_PHI.error(
					"pHICredentials / queryInvoker / messageService / patientNotificationDao is not initialized --> Check if the beans are defined properly in Application Context");
			throw new MscriptsException(clientId, "Bean not initialized.", ErrorCodes.GENERIC_ERROR,
					new Exception("Bean not initialized."), errorSeverity);
		}

		try {
			LOGGER_NON_PHI.info("Inside processPatientNotification method");
			// Getting values from the config files
			String cvContactReasonOutOfStock = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONOUTOFSTOCK);
			String cvContactReasonPartialFill = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONPARTIALFILL);
			String cvContactReasonReady = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONREADY);
			String cvContactReasonRxRejected = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONRXREJECTED);
			String cvContactReasonRxAdjudicationComplete = ConfigReader.readConfig(Constants.cvDefaultClientId,
					langCode, ConfigKeys.CVCONTACTREASONRXADJUDICATIONCOMPLETE);
			String cvContactReasonSold = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONSOLD);
			String cvContactOnDemand = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONONDEMAND);

			String cvContactReasonPartialFillOnHold = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONPARTIALFILLONHOLD);
			String cvContactReasonCentralFillDelayed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONCENTRALFILLDELAYED);
			String cvContactReasonThirdPartyException = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONTHIRDPARTYEXCEPTION);
			String cvContactReasonCallPrescriber = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONCALLPRESCRIBER);
			String cvContactReasonEPrescriptionReceived = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCONTACTREASONEPRESCRIPTIONRECEIVED);
			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);
			String cvCopayEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVCOPAYENABLED);
			String cvDateTimeFormatInputParsed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATINPUTPARSED);
			String cvTimeFormatUser = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEFORMATUSER);
			// Partial Fill Statuses
			String cvPartialFillStatusPartial = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVPARTIALFILLSTATUSPARTIAL);
			String cvPartialFillStatusCompleted = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVPARTIALFILLSTATUSCOMPLETED);
			String cvPartialFillStatusNew = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVPARTIALFILLSTATUSNEW);
			String cvtRefillStatusFilled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTREFILLSTATUSFILLED);
			String cvUpdatePromiseTimeEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVUPDATEPROMISETIMEENABLED);
			String cvContactReasonCancelledOutOfWillCall = ConfigReader.readConfig(Constants.cvDefaultClientId,
					langCode, ConfigKeys.CVCONTACTREASONCANCELLEDOUTOFWILLCALL);
			// Config: save patient communication consent received
			String cvSavePatientCommunicationConsent = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVSAVEPATIENTCOMMUNICATIONCONSENT);
			/*
			 * Notification Logging related configs
			 */
			String cvBlockTypeDrugExcluded = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPEDRUGEXCLUDED);
			String cvBlockReasonScheduleDrugExcluded = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONSCHEDULEDRUGEXCLUDED);
			String cvBlockReasonGPIDrugExcluded = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONGPIDRUGEXCLUDED);
			String cvBlockReasonNotificationDisabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONNOTIFICATIONDISABLED);
			String cvBlockTypeBackDatedMessage = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPEBACKDATEDMESSAGE);
			String cvBlockReasonBackDatedMessage = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONBACKDATEDMESSAGE);
			String cvBlockTypeMobileNotVerified = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPEMOBILENOTVERIFIED);
			String cvBlockReasonMobileNotVerified = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONMOBILENOTVERIFIED);
			String cvBlockTypeTextNotActive = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPETEXTNOTACTIVE);
			String cvBlockReasonTextNotActive = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONTEXTNOTACTIVE);
			String cvsendReminderModeText = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVSENDREMINDERMODETEXT);
			String cvsendReminderModeAndroidPush = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVSENDREMINDERMODEANDROIDPUSH);
			String cvBlockTypePush = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPEPUSH);
			String cvBlockReasonPush = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONPUSH);
			String cvSendReminderModeIosPush = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVSENDREMINDERMODEIOSPUSH);
			String cvSendReminderModeEmail = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVSENDREMINDERMODEEMAIL);
			String cvBlockTypeEmail = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPEEMAIL);
			String cvBlockReasonEmail = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONEMAIL);
			String cvBlockTypeStoreNotAllowed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPESTORENOTALLOWED);
			String cvBlockReasonStoreNotAllowed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONSTORENOTALLOWED);
			String cvBlockTypeSyncScript = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPESYNCSCRIPT);
			String cvBlockReasonSyncScript = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONSYNCSCRIPT);
			String cvBlockTypeConsentNotGiven = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPECONSENTNOTGIVEN);
			String cvBlockReasonConsentNotGiven = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONCONSENTNOTGIVEN);
			String cvBlockTypeReceivedReadyBeforePartial = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPERECEIVEDREADYBEFOREPARTIAL);
			String cvBlockReasonReceivedReadyBeforePartial = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, 
					ConfigKeys.CVBLOCKREASONRECEIVEDREADYBEFOREPARTIAL);
			String cvDateFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,ConfigKeys.CVDATEFORMATDOB);
			/*
			 * End of Notification Logging related configs
			 */
			/*
			 * To read configuration value for Refill Reminder Delivery Mode
			 */
			String cvOtherReminderMappingID = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVOTHERREMINDERMAPPINGID);

			String cvtCommNameOutOfStock = null;
			String cvIsOutOfStockMessageEnabled = null;
			String cvtCommNamePartialFill = null;
			String cvIsPartialFillMessageEnabled = null;
			String cvtCommNameRxRejected = null;
			String cvIsReadyMessageEnabled = null;
			String userTimezone = null;
			String cvDelayInNotification = null;
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);
			String lastFilledDate = null;
			Calendar calendar = null;
			Map<String, String> communicationIdMap = null;
			Map<String, String> transactionStateMap = null;
			String contactReasonStatus = null;
			boolean isRxTransactionMessageAllowedForStore = true;
			String cvIsRejectedMessageEnabled = null;
			String cvIsAdjudicationMessageEnabled = null;
			String cvtCommNameRxAdjudicationComplete = null;
			String promiseTime = null;
			String promiseDate = null;
			String nextSyncFillDate = null;
			boolean isSyncScriptEnrolled = false;
			boolean patientSyncScriptEnrolled = false;
			String cvRoundTimeToNearestQuarter = null;
			String formatToUse = null;
			String cvIsSoldMessageEnabled = null;

			String cvIsCancelledOutOfWillCallMessageEnabled = null;
			String cvIsStoreExceptionEnabled = null;
			patientData = patientNotification.getPatientData();
			transactionData = patientNotification.getTransactionData();
			pharmacyData = patientNotification.getPharmacyData();
			String cvIsAdjudicationCommunicationEnabled = null;
			String cvCheckMscriptsAutoFillCriteria = null;
			String cvIsRejectedCommunicationEnabled = null;
			String cvIsReadyCommunicationEnabled = null;
			String cvtCommNameRxReadyInStore = null;
			String cvPickupRestockingLimit = null;
			String cvtCommNameRxReadyInStoreWithCopay = null;
			String cvIsPartialFillCommunicationEnabled = null;
			String cvIsOutOfStockCommunicationEnabled = null;
			String cvIsOnDemandCommunicationEnabled = null;
			String cvIsPartialFillOnHoldMessageEnabled = null;
			String cvIsPartialFillOnHoldCommunicationEnabled = null;
			String cvIsCentralFillDelayedMessageEnabled = null;
			String cvIsCentralFillDelayedCommunicationEnabled = null;
			String cvIsThirdPartyExceptionMessageEnabled = null;
			String cvIsThirdPartyExceptionCommunicationEnabled = null;
			String cvIsCallPrescriberMessageEnabled = null;
			String cvIsCallPrescriberCommunicationEnabled = null;
			String cvIsEPrescriptionReceivedMessageEnabled = null;
			String cvIsEPrescriptionReceivedCommunicationEnabled = null;
			String cvtCommNamePartialFillOnHold = null;
			String cvtCommNameCentralFillDelayed = null;
			String cvtCommNameThirdPartyException = null;
			String cvtCommNameCallPrescriber = null;
			String cvtCommNameEPrescriptionReceived = null;
			String cvtCommNameRxReadyInStoreWithCopayAndDeliveryLink = null;
			// Finding client id for future database calls -- > Only for few
			// notification this logic will work. Not for new update
			// notification of a drug
			clientIdMap = eopnUtils.getClientId(transactionData, pharmacyData);
			if (!MscriptsStringUtils.isStringEmptyOrNull(patientData.get(0).getSyncScriptEnrolled())) {
				patientSyncScriptEnrolled = patientData.get(0).getSyncScriptEnrolled().equalsIgnoreCase("E");
			}
			if (MscriptsStringUtils.isMapEmptyOrNull(clientIdMap) 
					&& transactionData != null 
					&& transactionData.get(0) != null) {
				contactReason = transactionData.get(0).getContactReason();
			}
			
			cvIsPartialFillMessageEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVISPARTIALFILLMESSAGEENABLED);
			/**
			 * If client id is not found then process only ondemand and
			 * in-process EOPN notificaiton
			 * CR1 without RX Info
			 */
			if (MscriptsStringUtils.isMapEmptyOrNull(clientIdMap)) {
				
				/**
				 * Getting encryption key from the database by using client id
				 * Constants.NUMERIC_TRUE_STRING
				 */
				encryptionKeyMap = eopnUtils.getFinalKey(Constants.cvDefaultClientId, secondaryKey);
				finalEncryptionKey = encryptionKeyMap.get("encryptionKey");
				LOGGER_PHI.info("Encryption Key with client ID 1 {} ", finalEncryptionKey);
				LOGGER_NON_PHI.info(
						"Either its not our customer or new Rx came for the existing customer. Checking the scenario...");
				String dateOfBirth = MiscUtils.dateFormat(patientData.get(0).getPatientBirthDate(), cvDateFormatDOB, cvDateFormatDOB);
				if (!MscriptsStringUtils.isStringEmptyOrNull(patientData.get(0).getRxcomPatientID()))
					patientDetailsMap = eopnUtils.getCustomersDetailsWithoutClientId(dateOfBirth, finalEncryptionKey,
							patientData);
				
				 
				if (MscriptsStringUtils.isMapEmptyOrNull(patientDetailsMap)) {
					LOGGER_NON_PHI.info("Inside defualt client id; Customer Not found; Logging the request");
					bReprocessPatientNotification = false;
					clientId = Constants.cvDefaultClientId;
					// mce-1769
					// replace old data with first name,last name and dob
					if (MscriptsStringUtils.compareStringEquals(Constants.NUMERIC_TRUE_STRING, cvSavePatientCommunicationConsent)) {
						eopnUtils.insertOrUpdatePatientCommunicationConsentForNonRegistered(
								patientData.get(0).getRxcomPatientID(), finalEncryptionKey, dateOfBirth, patientData);
					}
					throw new MscriptsException(clientId, Constants.PATIENT_NOT_FOUND_ERROR_MSG,
							ErrorCodes.PATIENT_NOT_FOUND, new Exception(Constants.PATIENT_NOT_FOUND_ERROR_MSG),
							errorSeverity);
				} else {
					LOGGER_NON_PHI.info(" Customer In our database!! ");
					customerId = patientDetailsMap.get(Constants.CUSTOMER_ID_DB_STRING);
					clientId = patientDetailsMap.get(Constants.CLIENT_ID);
					
					cvIsRejectedMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISREJECTEDMESSAGEENABLED);
					cvIsRejectedCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISREJECTEDCOMMUNICATIONENABLED);
					cvIsOutOfStockMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISOUTOFSTOCKMESSAGEENABLED);
					cvIsOutOfStockCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISOUTOFSTOCKCOMMUNICATIONENABLED);
					cvIsCallPrescriberMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISCALLPRESCRIBERMESSAGEENABLED);
					cvIsCallPrescriberCommunicationEnabled = ConfigReader.readConfig(clientId,
							langCode, ConfigKeys.CVISCALLPRESCRIBERCOMMUNICATIONENABLED);
					cvIsThirdPartyExceptionMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISTHIRDPARTYEXCEPTIONMESSAGEENABLED);
					cvIsThirdPartyExceptionCommunicationEnabled = ConfigReader.readConfig(clientId,
							langCode, ConfigKeys.CVISTHIRDPARTYEXCEPTIONCOMMUNICATIONENABLED);
					
					
					patientDetailsMap.put(Constants.ENCRYPTION_KEY_STRING, finalEncryptionKey);
					/**
					 * Send generic message for CR 1,2,12,13 if Rx number is missing
					 */
					if (MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getPrescriptionNumber())
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonRxRejected)
							&& MscriptsStringUtils.compareStringEquals(cvIsRejectedMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						patientDetailsMap.put(Constants.IS_COMMUNICATION_ENABLED, cvIsRejectedCommunicationEnabled);
						sendGenericMessage(ConfigKeys.CV_COMM_EOPN_GENERIC_MSG, patientDetailsMap, pharmacyData, patientData, transactionData, NotificationTypeKey.rxrejected,langCode);
	
					} 
					else if (MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getPrescriptionNumber())
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonOutOfStock)
							&& MscriptsStringUtils.compareStringEquals(cvIsOutOfStockMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						patientDetailsMap.put(Constants.IS_COMMUNICATION_ENABLED, cvIsOutOfStockCommunicationEnabled);
						sendGenericMessage(ConfigKeys.CV_COMM_EOPN_GENERIC_MSG_OUT_OF_STOCK, patientDetailsMap, pharmacyData, patientData, transactionData, NotificationTypeKey.rxoutofstock, langCode);
	
					}
					else if (MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getPrescriptionNumber())
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonCallPrescriber)
							&& MscriptsStringUtils.compareStringEquals(cvIsCallPrescriberMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						patientDetailsMap.put(Constants.IS_COMMUNICATION_ENABLED, cvIsCallPrescriberCommunicationEnabled);
						sendGenericMessage(ConfigKeys.CV_COMM_EOPN_GENERIC_MSG_CALL_PRESCRIBER, patientDetailsMap, pharmacyData, patientData, transactionData, NotificationTypeKey.rxcallprescriber,langCode);
					}
					else if (MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getPrescriptionNumber())
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonThirdPartyException)
							&& MscriptsStringUtils.compareStringEquals(cvIsThirdPartyExceptionMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						patientDetailsMap.put(Constants.IS_COMMUNICATION_ENABLED, cvIsThirdPartyExceptionCommunicationEnabled);
						sendGenericMessage(ConfigKeys.CV_COMM_EOPN_GENERIC_MSG_THIRD_PARTY_EXCEPTION, patientDetailsMap, pharmacyData, patientData, transactionData,NotificationTypeKey.rxthirdpartyexception ,langCode);
					}
					
					
					else {
					
						cvIsAdjudicationMessageEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISADJUDICATIONMESSAGEENABLED);
						cvIsAdjudicationCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISADJUDICATIONCOMMUNICATIONENABLED);
						cvCheckMscriptsAutoFillCriteria = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVCHECKMSCRIPTSAUTOFILLCRITERIA);
						contactReason = transactionData.get(0).getContactReason();
						LOGGER_NON_PHI.info("Contact reasson = {}", contactReason);
						cvtCommNameRxAdjudicationComplete = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVTCOMMNAMERXADJUDICATIONCOMPLETE);
						cvRoundTimeToNearestQuarter = ConfigReader.readConfig(clientId, langCode,
								Constants.cvRoundTimeToNearestQuarter);
						cvIsStoreExceptionEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISSTOREEXCEPTIONENABLED);
						cvIsOnDemandCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISONDEMANDCOMMUNICATIONENABLED);
						cvIsEPrescriptionReceivedMessageEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISEPRESCRIPTIONRECEIVEDMESSAGEENABLED);
						cvIsEPrescriptionReceivedCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISEPRESCRIPTIONRECEIVEDCOMMUNICATIONENABLED);
						cvtCommNameEPrescriptionReceived = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED);
	
						if (MscriptsStringUtils.compareStringEquals(Constants.NUMERIC_TRUE_STRING, cvIsStoreExceptionEnabled)) {
							LOGGER_NON_PHI.info("Exception of reminder based on store is enabled, Getting list of stores");
							isRxTransactionMessageAllowedForStore = eopnUtils.checkStoreException(clientId,
									pharmacyData.get(0).getPharmacyNCPDP());
						}
	
						// MCE-1557 & MCE-1558
						// save patient communication consent
						if (MscriptsStringUtils.compareStringEquals(Constants.NUMERIC_TRUE_STRING, cvSavePatientCommunicationConsent)) {
							eopnUtils.insertOrUpdatePatientCommunicationConsent(customerId, patientData);
						}
	
						if (!MscriptsStringUtils.isStringEmptyOrNull(cvCheckMscriptsAutoFillCriteria)
								&& MscriptsStringUtils.compareStringEquals(cvCheckMscriptsAutoFillCriteria, Constants.NUMERIC_TRUE_STRING)) {
							eopnUtils.updateMscriptsAutoFillCriteria(clientId, customerId,
									transactionData.get(0).getPrescriptionNumber(), pharmacyData.get(0).getPharmacyNCPDP(),
									null, patientNotification);
						}
						/**
						 * processing ondemand EOPN messages
						 */
						if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactOnDemand)
								&& MscriptsStringUtils.compareStringEquals(cvIsOnDemandCommunicationEnabled, Constants.NUMERIC_TRUE_STRING) 
								&& (!MscriptsStringUtils.isStringEmptyOrNull(patientData.get(0).getRxcomPatientID()))) {
								String communicationName = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
										ConfigKeys.CVTCOMMNAMEONDEMANDMESSAGE);
								customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
										clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());
								Map<String, String> communicationContent = eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);
								communicationContent.put(
										CommunicationTemplateStrings.ONDEMAND_MESSAGE_DETAILS.getTemplateString(),
										transactionData.get(0).getFreeFormText());
								eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
										patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
										communicationContent, communicationName, true);
						}
						/**
						 * processing contact reason 8 (inprocess)
						 */
						else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason,
										cvContactReasonRxAdjudicationComplete)
								&& MscriptsStringUtils.compareStringEquals(cvIsAdjudicationMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
	
							/*
							 * Patient credit card info is shared with mscripts for
							 * contact reasons 8(Adjudication Complete) and 4(Ready)
							 */
							List<CreditCard> patientCreditCardList = patientData.get(0).getCreditCard();
							eopnUtils.processPatientCreditCard(Integer.parseInt(clientId), Long.parseLong(customerId),
									patientCreditCardList);
	
							/* Processing Patient Loyalty card */
							eopnUtils.processPatientLoyaltyCard(clientId, customerId, patientData);
	
							promiseTime = eopnUtils.getLocalDatabaseTime(customerId,
									transactionData.get(0).getPromiseTime());
	
							Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
							drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY,
									NotificationTypeKey.rxrefillinprocess);
							boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId,
									DrugFilterType.SCHEDULE, transactionData.get(0).getDrugSchedule(),
									drugFilterCriteriasMap);
							/*
							 * Logging in ext_req_handler_blocked_details table if
							 * drug excluded because of drug filter type SCHEDULE
							 */
							if (bisDrugExcluded) {
								eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
										finalEncryptionKey, transactionData, cvBlockTypeDrugExcluded,
										cvBlockReasonScheduleDrugExcluded);
							}
							if (!bisDrugExcluded) {
								Map<String, Object> drugFilterCriteriaMap = new HashMap<>();
								drugFilterCriteriaMap.put(Constants.NOTIFICATION_KEY,
										NotificationTypeKey.rxrefillinprocess);
								bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
										transactionData.get(0).getDrugGPI(), drugFilterCriteriaMap);
	
								/*
								 * Logging in ext_req_handler_blocked_details table
								 * if drug excluded because of drug filter type GPI
								 */
								if (bisDrugExcluded) {
									eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
											patientData, finalEncryptionKey, transactionData, cvBlockTypeDrugExcluded,
											cvBlockReasonGPIDrugExcluded);
								}
	
								if (!bisDrugExcluded) {
									bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
											customerId, String.valueOf(NotificationTypeKey.rxrefillinprocess), patientData,
											finalEncryptionKey, transactionData, patientDetailsMap);
								}
							}
							/*
							 * For New Rx if WSo2 comes first calculate the quantity
							 */
							String sTotalQuantity = null;
							if (MscriptsStringUtils.compareStringEquals(cvPartialFillStatusNew,
									transactionData.get(0).getPartialFillStatus())) {
								sTotalQuantity = transactionData.get(0).getQuantityDispensed();
							}
							
							if (!MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getSyncScriptEnrolled())) {
								isSyncScriptEnrolled = transactionData.get(0).getSyncScriptEnrolled().equalsIgnoreCase("Y");
							}
	
							// MCE-1325 fixes related to med sync
							if (!patientSyncScriptEnrolled) {
								isSyncScriptEnrolled = false;
							}
							if (!isSyncScriptEnrolled) {
								nextSyncFillDate = null;
							} else {
								nextSyncFillDate = eopnUtils.getLocalDatabaseTime(customerId,
									transactionData.get(0).getNextSyncFillDate());
							}
	
							LOGGER_NON_PHI.info("Inserting the promise time in patient_rx_txn_median");
	
							// mscripts daw code value is same as PDX value. So
							// store it, as it is.
							formatterInput = new SimpleDateFormat(
									ConfigReader.readConfig(clientId, langCode, Constants.cvDateTimeFormatDOB));
							patientRxTxnMedianService.createOrUpdatePatientRxTxnMedian(
									transactionData.get(0).getPrescriptionNumber(), pharmacyData.get(0).getPharmacyNCPDP(),
									formatterInput.parse(promiseTime), transactionData.get(0).getDaysSupply(),
									Float.parseFloat(transactionData.get(0).getQuantityDispensed()),
									transactionData.get(0).getDrugUnit(), Float.parseFloat(sTotalQuantity),
									Long.parseLong(transactionData.get(0).getDawCode()), (isSyncScriptEnrolled) ? 1 : 0,
									nextSyncFillDate, 0, transactionData.get(0).getPartialFillStatus());
	
							boolean bIsBackDatedMessage = eopnUtils.isBackDatedMessage(
									transactionData.get(0).getPromiseTime(), patientNotification.getMessageDate(),
									PdxContactReason.ADJUDICATION_COMPLETE);
							/*
							 * Logging in ext_req_handler_blocked_details table if
							 * request is back Dated
							 * 
							 */
							if (bIsBackDatedMessage) {
								eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
										finalEncryptionKey, transactionData, cvBlockTypeBackDatedMessage,
										cvBlockReasonBackDatedMessage);
							}
	
							// get customer preferences
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId, clientId,
									cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());
	
							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !isSyncScriptEnrolled
									&& MscriptsStringUtils.compareStringEquals(cvIsAdjudicationCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)
									&& !bisDrugExcluded && !bIsBackDatedMessage) {
	
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								if (transactionData.get(0).getLastFilledDate() != null) {
									lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
											transactionData.get(0).getLastFilledDate());
								}
	
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx Adjudication Complete notification received during outbound communication period, Sending the reminder.... ");
									LOGGER_NON_PHI.info("Parsing the promise time from the request");
									if (MscriptsStringUtils.compareStringEquals(cvRoundTimeToNearestQuarter, Constants.NUMERIC_TRUE_STRING)) {
										promiseTime = MiscUtils.roundTimeToNearestQuarter(promiseTime,
												ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVDATETIMEFORMATDOB),
												userTimezone);
									}
									formatToUse = MiscUtils.checkDayorDate(clientId, userTimezone, langCode, promiseTime,
											Constants.cvDateTimeFormatDOB);
									formatter = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, langCode, formatToUse));
									formatterInput = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, langCode, Constants.cvDateTimeFormatDOB));
	
									promiseDate = formatter.format(formatterInput.parse(promiseTime));
									formatter = new SimpleDateFormat(cvTimeFormatUser);
	
									promiseTime = formatter.format(formatterInput.parse(promiseTime));
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);
									/*
									 * add parameter value in communication map for
									 * sending message
									 */
	
									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);
	
									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);
									communicationContent.put(CommunicationTemplateStrings.DAY.getTemplateString(),
											promiseDate);
									communicationContent.put(CommunicationTemplateStrings.TIME.getTemplateString(),
											promiseTime);
									if (MscriptsStringUtils.compareStringEquals(cvUpdatePromiseTimeEnabled, Constants.NUMERIC_TRUE_STRING)) {
										String cvAppResolverPageUrl = ConfigReader.readConfig(clientId, langCode,
												ConfigKeys.CVAPPRESOLVERPAGEURL);
										communicationContent.put(
												CommunicationTemplateStrings.APP_RESOLVER_WEBPAGE_URL.getTemplateString(),
									communicationContent.put(
											CommunicationTemplateStrings.CUSTOMER_FIRST_NAME.getTemplateString(),
											patientData.get(0).getPatientFirstName()));
									
	
									}
									/*
									 * check if customer has received
									 * cvtCommNameRxAdjudicationComplete in the last
									 * cvHowLongInHoursBeforeSendingSameNotificationForPresc
									 */
	
									countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
											patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
											cvtCommNameRxAdjudicationComplete);
	
									if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
											&& MscriptsStringUtils.compareStringEquals(
													countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
											&& MscriptsStringUtils.compareStringEquals(cvIsAdjudicationCommunicationEnabled,
													Constants.NUMERIC_TRUE_STRING)) {
	
										/*
										 * send AdjudicationComplete communication
										 * to the user
										 */
										eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
												patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
												communicationContent, cvtCommNameRxAdjudicationComplete, false);
	
									}
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)) {
									/*
									 * Receiving EOPN notification for new Rx before
									 * patient update notification during off
									 * business hour We can't send message via send
									 * bulk pickup reminder since we don't have
									 * customer prescription details in customer
									 * prescription This is edge case where we are
									 * not sending In process notification to the
									 * end user
									 */
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)) {
									/*
									 * Receiving EOPN notification for new Rx before
									 * patient update notification during off
									 * business hour We can't send message via send
									 * bulk pickup reminder since we don't have
									 * customer prescription details in customer
									 * prescription This is edge case where we are
									 * not sending In process notification to the
									 * end user
									 */
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}
	
						}
						/**
						 * processing contact reason 14 (E prescription received)
						 */
						else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason,
										cvContactReasonEPrescriptionReceived)
								&& MscriptsStringUtils.compareStringEquals(cvIsEPrescriptionReceivedMessageEnabled,
										Constants.NUMERIC_TRUE_STRING)) {
	
							promiseTime = eopnUtils.getLocalDatabaseTime(customerId,
									transactionData.get(0).getPromiseTime());
							LOGGER_NON_PHI.info("Inserting the promise time in patient_rx_txn_median");
							formatterInput = new SimpleDateFormat(
									ConfigReader.readConfig(clientId, langCode, Constants.cvDateTimeFormatDOB));
							patientRxTxnMedianService.createOrUpdatePatientRxTxnMedian(
									transactionData.get(0).getPrescriptionNumber(), pharmacyData.get(0).getPharmacyNCPDP(),
									formatterInput.parse(promiseTime), transactionData.get(0).getDaysSupply(),
									Float.parseFloat(transactionData.get(0).getQuantityDispensed()),
									transactionData.get(0).getDrugUnit(), Float.parseFloat(Constants.NUMERIC_FALSE_STRING),
									Long.parseLong(transactionData.get(0).getDawCode()), (isSyncScriptEnrolled) ? 1 : 0,
									nextSyncFillDate, 0, transactionData.get(0).getPartialFillStatus());
							
							boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
									customerId, String.valueOf(NotificationTypeKey.rxescriptreceived), patientData,
									finalEncryptionKey, transactionData, patientDetailsMap);
							// get customer preferences
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId, clientId,
									cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());
	
							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore
									&& !bisDrugExcluded
									&& MscriptsStringUtils.compareStringEquals(
											cvIsEPrescriptionReceivedCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)) {
	
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								if (transactionData.get(0).getLastFilledDate() != null) {
									lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
											transactionData.get(0).getLastFilledDate());
								}
	
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"E prescription received notification received during outbound communication period, Sending the reminder.... ");
									LOGGER_NON_PHI.info(Constants.PARSING_PROMISE_TIME_STRING);
									/*
									 * add parameter value in communication map for
									 * sending message
									 */
	
									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);
	
									countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
											patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
											cvtCommNameEPrescriptionReceived);
	
									if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
											&& MscriptsStringUtils.compareStringEquals(
													countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
											&& MscriptsStringUtils.compareStringEquals(
													cvIsEPrescriptionReceivedCommunicationEnabled,
													Constants.NUMERIC_TRUE_STRING)) {
										eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
												patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
												communicationContent, cvtCommNameEPrescriptionReceived, true);
	
									}
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)) {
									/*
									 * Receiving EOPN notification for new Rx before
									 * patient update notification during off
									 * business hour We can't send message via send
									 * bulk pickup reminder since we don't have
									 * customer prescription details in customer
									 * prescription This is edge case where we are
									 * not sending In process notification to the
									 * end user
									 */
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)) {
									/*
									 * Receiving EOPN notification for new Rx before
									 * patient update notification during off
									 * business hour We can't send message via send
									 * bulk pickup reminder since we don't have
									 * customer prescription details in customer
									 * prescription This is edge case where we are
									 * not sending In process notification to the
									 * end user
									 */
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}
	
						}
					}
				}			
			}
			/**
			 * Processing all contact reasons when client id is present
			 */
			else {
				clientId = clientIdMap.get("client_id");
				// Finding the encryption key based on the client ID
				encryptionKeyMap = eopnUtils.getFinalKey(clientId, secondaryKey);
				finalEncryptionKey = encryptionKeyMap.get("encryptionKey");
				LOGGER_PHI.info("Encryption Key with Client ID:{} is = {}", clientId, finalEncryptionKey);
				// Get the customer information --> customer_id
				// DOB format is W3C date-time format,Converting it to
				// yyyy-MM-dd DB format
				
				patientDetailsMap = eopnUtils.getCustomerDetails(finalEncryptionKey, patientData,
						transactionData, pharmacyData, clientId);

				if (MscriptsStringUtils.isMapEmptyOrNull(patientDetailsMap)) {
					throw new MscriptsException(clientId, Constants.PATIENT_NOT_FOUND_ERROR_MSG,
							ErrorCodes.PATIENT_NOT_FOUND, new Exception(Constants.PATIENT_NOT_FOUND_ERROR_MSG),
							errorSeverity);
				} else {
					customerId = patientDetailsMap.get("customer_id");

					LOGGER_PHI.info("Customer ID = {} ", customerId);

					// MCE-1557 & MCE-1558
					// save patient communication consent
					if (MscriptsStringUtils.compareStringEquals(Constants.NUMERIC_TRUE_STRING, cvSavePatientCommunicationConsent)) {
						eopnUtils.insertOrUpdatePatientCommunicationConsent(customerId, patientData);
					}

					// we are inserting/updating mscripts_auto_fill_criteria
					cvCheckMscriptsAutoFillCriteria = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVCHECKMSCRIPTSAUTOFILLCRITERIA);

					if (!MscriptsStringUtils.isStringEmptyOrNull(cvCheckMscriptsAutoFillCriteria)
							&& MscriptsStringUtils.compareStringEquals(cvCheckMscriptsAutoFillCriteria, Constants.NUMERIC_TRUE_STRING)) {
						eopnUtils.updateMscriptsAutoFillCriteria(clientId, customerId,
								transactionData.get(0).getPrescriptionNumber(), pharmacyData.get(0).getPharmacyNCPDP(),
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), patientNotification);

					}

					if (!MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getSyncScriptEnrolled())) {
						isSyncScriptEnrolled = transactionData.get(0).getSyncScriptEnrolled().equalsIgnoreCase("Y");
					}

					// Differentiate between types of contact reason

					// Transaction data will be an array of more than one
					// node ?
					// Assuming only one transaction node for now. If
					// multiple then parsing needs to be done to pick the
					// latest transaction
					
					cvIsOutOfStockCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISOUTOFSTOCKCOMMUNICATIONENABLED);
					cvIsOutOfStockMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISOUTOFSTOCKMESSAGEENABLED);
					cvIsPartialFillMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISPARTIALFILLMESSAGEENABLED);
					cvIsPartialFillCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISPARTIALFILLCOMMUNICATIONENABLED);
					cvIsReadyMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISREADYMESSAGEENABLED);
					cvIsReadyCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISREADYCOMMUNICATIONENABLED);
					
					cvIsRejectedMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISREJECTEDMESSAGEENABLED);
					cvIsRejectedCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISREJECTEDCOMMUNICATIONENABLED);
					cvIsAdjudicationMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISADJUDICATIONMESSAGEENABLED);
					cvIsAdjudicationCommunicationEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISADJUDICATIONCOMMUNICATIONENABLED);
					cvIsSoldMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISSOLDMESSAGEENABLED);
					cvIsPartialFillOnHoldMessageEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
							ConfigKeys.CVISPARTIALFILLONHOLDMESSAGEENABLED);
					cvIsPartialFillOnHoldCommunicationEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISPARTIALFILLONHOLDCOMMUNICATIONENABLED);
					cvIsCentralFillDelayedMessageEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISCENTRALFILLDELAYEDMESSAGEENABLED);
					cvIsCentralFillDelayedCommunicationEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISCENTRALFILLDELAYEDCOMMUNICATIONENABLED);
					cvIsThirdPartyExceptionMessageEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISTHIRDPARTYEXCEPTIONMESSAGEENABLED);
					cvIsThirdPartyExceptionCommunicationEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISTHIRDPARTYEXCEPTIONCOMMUNICATIONENABLED);
					cvIsCallPrescriberMessageEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
							ConfigKeys.CVISCALLPRESCRIBERMESSAGEENABLED);
					cvIsCallPrescriberCommunicationEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISCALLPRESCRIBERCOMMUNICATIONENABLED);
					cvIsEPrescriptionReceivedMessageEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISEPRESCRIPTIONRECEIVEDMESSAGEENABLED);
					cvIsEPrescriptionReceivedCommunicationEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
							langCode, ConfigKeys.CVISEPRESCRIPTIONRECEIVEDCOMMUNICATIONENABLED);

					cvIsCancelledOutOfWillCallMessageEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISCANLLEDOUTOFWILLCALLMESSAGEENABLED);
					cvtCommNameRxReadyInStoreWithCopay = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CV_COMM_NAME_RX_READY_INSTORE_WITH_COPAY);
					cvtCommNameRxReadyInStoreWithCopayAndDeliveryLink = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CV_COMM_NAME_RX_READY_WITH_COPAY_DELIVERY_LINK);
					cvtCommNameRxReadyInStore = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMERXREADYINSTORE);
					cvtCommNamePartialFillOnHold = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMEPARTIALFILLONHOLD);
					cvtCommNameCentralFillDelayed = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMECENTRALFILLDELAYED);
					cvtCommNameThirdPartyException = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMETHIRDPARTYEXCEPTION);
					cvtCommNameCallPrescriber = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMECALLPRESCRIBER);
					cvtCommNameEPrescriptionReceived = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED);
					cvPickupRestockingLimit = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVPICKUPRESTOCKINGLIMIT);
					cvIsStoreExceptionEnabled = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVISSTOREEXCEPTIONENABLED);

					contactReason = transactionData.get(0).getContactReason();
					LOGGER_NON_PHI.info("Contact reasson = {}", contactReason);
					cvtCommNamePartialFill = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMEPARTIALFILL);
					cvtCommNameOutOfStock = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMEOUTOFSTOCK);
					cvtCommNameRxRejected = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMERXREJECTED);
					cvtCommNameRxAdjudicationComplete = ConfigReader.readConfig(clientId, langCode,
							ConfigKeys.CVTCOMMNAMERXADJUDICATIONCOMPLETE);
					cvRoundTimeToNearestQuarter = ConfigReader.readConfig(clientId, langCode,
							Constants.cvRoundTimeToNearestQuarter);
					if (MscriptsStringUtils.compareStringEquals(Constants.NUMERIC_TRUE_STRING, cvIsStoreExceptionEnabled)) {
						LOGGER_NON_PHI.info("Exception of reminder based on store is enabled, Getting list of stores");
						isRxTransactionMessageAllowedForStore = eopnUtils.checkStoreException(clientId,
								pharmacyData.get(0).getPharmacyNCPDP());
					}

					// MCE-1325 fixes related to med sync
					if (!patientSyncScriptEnrolled) {
						isSyncScriptEnrolled = false;
					}
					if (!isSyncScriptEnrolled) {
						nextSyncFillDate = null;
					} else {
						nextSyncFillDate = eopnUtils.getLocalDatabaseTime(customerId,
							transactionData.get(0).getNextSyncFillDate());
					}

					boolean isNotificationInSequence = eopnUtils
							.checkIfPrescNotificationInSequenceWithCurrentPrescriptionStatus(contactReasonStatus,
									transactionData.get(0).getPrescriptionNumber(),
									pharmacyData.get(0).getPharmacyNCPDP());

					LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
					transactionStateMap = eopnUtils.getPatientRxTxn(patientDetailsMap);

					String sAutoFillEnabled = transactionData.get(0).getAutoFillEnabled();
					int iAutoFillenabled = eopnUtils.getRxAutoFillEnabledValue(sAutoFillEnabled);

					int iPdxDawCode = Integer.parseInt(transactionData.get(0).getDawCode());
					DawCode dawCode = dawCodeService.getDawCodeByIdentifier(iPdxDawCode, null);
					String sTotalQuantity = null;
					promiseTime = eopnUtils.getLocalDatabaseTime(customerId, transactionData.get(0).getPromiseTime());

					if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonOutOfStock)
							&& MscriptsStringUtils.compareStringEquals(cvIsOutOfStockMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVOUTOFSTOCKSTATUS);

						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);

							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
									promiseTime, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
									nextSyncFillDate, iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);

							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
									promiseTime, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
									nextSyncFillDate, iAutoFillenabled);

							if (!patientSyncScriptEnrolled) {
								// fix for MCE-1325 Med Sync
								eopnUtils.updatePatientAllRxTxn(isSyncScriptEnrolled, customerId, nextSyncFillDate);
							}

						}

						if (isSyncScriptEnrolled || patientSyncScriptEnrolled) {
							eopnUtils.updateSyncDateForAllRxUnderStoreForCustomer(patientDetailsMap);
						}

						// Check whether we have sent out of stock message
						// for the user for the same rx within 24 hrs -->
						// Done to avoid multiple messages with in a day

						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId, cvtCommNameOutOfStock);

						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
								&& MscriptsStringUtils.compareStringEquals(
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								&& MscriptsStringUtils.compareStringEquals(cvIsOutOfStockCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)) {

							Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
							drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxoutofstock);
							boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId,
									DrugFilterType.GPI, transactionData.get(0).getDrugGPI(), drugFilterCriteriasMap);

							/*
							 * Logging in ext_req_handler_blocked_details table
							 * if drug excluded because of drug filter type GPI
							 */
							if (bisDrugExcluded) {
								eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
										patientData, finalEncryptionKey, transactionData, cvBlockTypeDrugExcluded,
										cvBlockReasonGPIDrugExcluded);
							}
							if (!bisDrugExcluded) {
								bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
										customerId, String.valueOf(NotificationTypeKey.rxoutofstock), patientData,
										finalEncryptionKey, transactionData, patientDetailsMap);
							}
							LOGGER_NON_PHI.info("Out of stock - Checking for back dated message");
							boolean bIsBackDatedMessage = eopnUtils.isBackDatedMessage(
									transactionData.get(0).getPromiseTime(), patientNotification.getMessageDate(),
									PdxContactReason.OUT_OF_STOCK);
							/*
							 * Logging in ext_req_handler_blocked_details table
							 * if request is back Dated
							 * 
							 */
							if (bIsBackDatedMessage) {
								eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
										patientData, finalEncryptionKey, transactionData, cvBlockTypeBackDatedMessage,
										cvBlockReasonBackDatedMessage);
							}

							communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId, cvtCommNameOutOfStock,
									customerId);

							// get customer preferences
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
									clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !isSyncScriptEnrolled
									&& !bisDrugExcluded && !bIsBackDatedMessage && mscriptsCommonService
											.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
								/*
								 * MCE-714 If Rx is enrolled in sync script, we
								 * will not be sending the Out of Stock message.
								 */

								// TD - check the time before sending
								// notification

								// get the user timezone
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());

								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info("userTimezone = {},lastFilledDate = {} ", userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Out of Stock notification received during outbound communication period, Sending the reminder ");

									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received out of stock state for the prescription");
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									LOGGER_NON_PHI.info(
											"Parsing the promise time from the request, value of cvRoundTimeToNearestQuarter :: {}",
											cvRoundTimeToNearestQuarter);
									if (MscriptsStringUtils.compareStringEquals(cvRoundTimeToNearestQuarter, Constants.NUMERIC_TRUE_STRING)) {
										promiseTime = MiscUtils.roundTimeToNearestQuarter(promiseTime, ConfigReader
												.readConfig(clientId, langCode, ConfigKeys.CVDATETIMEFORMATDOB),
												userTimezone);
									}
									formatToUse = MiscUtils.checkDayorDate(clientId, userTimezone, langCode,
											promiseTime, Constants.cvDateTimeFormatDOB);
									formatter = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, langCode, formatToUse));
									formatterInput = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, langCode, Constants.cvDateTimeFormatDOB));

									promiseDate = formatter.format(formatterInput.parse(promiseTime));
									formatter = new SimpleDateFormat(cvTimeFormatUser);

									promiseTime = formatter.format(formatterInput.parse(promiseTime));
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);
									/*
									 * add parameter value in communication map
									 * for sending message
									 */
									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);
									communicationContent.put(CommunicationTemplateStrings.DAY.getTemplateString(),
											promiseDate);
									communicationContent.put(CommunicationTemplateStrings.TIME.getTemplateString(),
											promiseTime);
									/*
									 * send Out of Stock communication to the
									 * user
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameOutOfStock, true);

								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Out of stock state for the prescription");
									LOGGER_NON_PHI.info(
											"Out of Stock notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received out of stock state for the prescription");
									LOGGER_NON_PHI.info(
											"Out of stock notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								}

							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with
								 * reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {

							LOGGER_NON_PHI.info("Already sent out of stock message to the user for the day, Count = "
									+ countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap, cvIsOutOfStockCommunicationEnabled,
									patientData, finalEncryptionKey, transactionData, clientId, customerId);
						}

					} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonPartialFill)
							&& MscriptsStringUtils.compareStringEquals(cvIsPartialFillMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						LOGGER_NON_PHI.info("Request for partial fill");

						if (!MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getPartialFillStatus())
								&& MscriptsStringUtils.compareStringEquals(
										transactionData.get(0).getPartialFillStatus(), cvPartialFillStatusPartial)) {
							contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
									ConfigKeys.CVPARTIALFILLSTATUS);

							LOGGER_PHI.info("PartialFillStatus = {} ", transactionData.get(0).getPartialFillStatus());
							if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
								LOGGER_NON_PHI
										.info("Record not available in  PATIENT_RX_TXN table, Inside Partial Fill");

								eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										promiseTime, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
							}
							// update the record if already present
							else {
								LOGGER_NON_PHI.info(
										"Record available PATIENT_RX_TXN table, updating the record, Inside Partial Fill");

								eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										promiseTime, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);

								if (!patientSyncScriptEnrolled) {
									// fix for MCE-1325 Med Sync
									eopnUtils.updatePatientAllRxTxn(isSyncScriptEnrolled, customerId, nextSyncFillDate);
								}

							}
							if (isSyncScriptEnrolled || patientSyncScriptEnrolled) {
								eopnUtils.updateSyncDateForAllRxUnderStoreForCustomer(patientDetailsMap);
							}

						} else {
							LOGGER_NON_PHI.info("Partial Fill Status is not valid");
						}
					}
					// Contact Reason --> 1
					else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonRxRejected)
							&& MscriptsStringUtils.compareStringEquals(cvIsRejectedMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVRXREJECTEDSTATUS);
						LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
						eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);

						// Add in the status in patient_rx_txn
						// table if not already present
						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);
							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);
							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);

							if (!patientSyncScriptEnrolled) {
								// fix for MCE-1325 Med Sync
								eopnUtils.updatePatientAllRxTxn(isSyncScriptEnrolled, customerId, nextSyncFillDate);

							}

						}
						if (isSyncScriptEnrolled || patientSyncScriptEnrolled) {
							eopnUtils.updateSyncDateForAllRxUnderStoreForCustomer(patientDetailsMap);
						}
						// Check whether we have sent out of stock message
						// for the user for the same rx within 24 hrs -->
						// Done to avoid multiple messages with in a day
						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId, cvtCommNameRxRejected);

						Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
						drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY,
								NotificationTypeKey.rxrejected);
						boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
								transactionData.get(0).getDrugGPI(), drugFilterCriteriasMap);
					
						/*
						 * Logging in ext_req_handler_blocked_details table if
						 * drug excluded because of drug filter type GPI
						 */
						if (bisDrugExcluded) {
							eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
									finalEncryptionKey, transactionData, cvBlockTypeDrugExcluded,
									cvBlockReasonGPIDrugExcluded);
						}

						if (!bisDrugExcluded) {
							bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
									customerId, String.valueOf(NotificationTypeKey.rxrejected), patientData,
									finalEncryptionKey, transactionData, patientDetailsMap);
						}

						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
								&& MscriptsStringUtils.compareStringEquals(
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								&& MscriptsStringUtils.compareStringEquals(cvIsRejectedCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)) {
							// Check whether user has text msg active and
							// verified = 1
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
									clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

							boolean bIsBackDatedMessage = eopnUtils.isBackDatedMessage(
									transactionData.get(0).getPromiseTime(), patientNotification.getMessageDate(),
									PdxContactReason.ISSUE);

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !bisDrugExcluded && !bIsBackDatedMessage
									&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
											customerId)) {
								/*
								 * MCE-714 If Rx is enrolled in sync script, we
								 * need to send the Rx Rejected message. So
								 * please do not block it here.
								 */

								// TD - check the time before sending
								// notification

								communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
										cvtCommNameRxRejected, customerId);
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx Rejected notification received during outbound communication period, Sending the reminder ");
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Rejected state for the prescription");
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);

									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);
									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);
									communicationContent.put(
											CommunicationTemplateStrings.CUSTOMER_FIRST_NAME
													.getTemplateString(),
											patientData.get(0).getPatientFirstName().toUpperCase());

									/*
									 * sending Rx Rejected communication to user
									 * 
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameRxRejected, true);

								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Rejected state for the prescription when time is less than lower cut off");
									LOGGER_NON_PHI.info(
											"Rx Rejected notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Rejected state for the prescription when time is greater than upper cut off");
									LOGGER_NON_PHI.info(
											"Rx Rejected notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with
								 * reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {
							LOGGER_NON_PHI.info("Already sent Rx rejected message to the user for the day, Count = "
									+ countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap, cvIsRejectedCommunicationEnabled,
									patientData, finalEncryptionKey, transactionData, clientId, customerId);
						}

					}
					// Contact Reason --> 8 For existing Rx
					else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason,
									cvContactReasonRxAdjudicationComplete)
							&& MscriptsStringUtils.compareStringEquals(cvIsAdjudicationMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVRXADJUDICATEDSTATUS);

						/*
						 * Patient credit card info is shared with mscripts for
						 * contact reasons 8(Adjudication Complete) and 4(Ready)
						 */
						List<CreditCard> patientCreditCardList = patientData.get(0).getCreditCard();
						eopnUtils.processPatientCreditCard(Integer.parseInt(clientId), Long.parseLong(customerId),
								patientCreditCardList);
						/* Processing Patient Loyalty card */
						eopnUtils.processPatientLoyaltyCard(clientId, customerId, patientData);
						
						Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
						drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxrefillinprocess);
						boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId,
								DrugFilterType.SCHEDULE, transactionData.get(0).getDrugSchedule(),
								drugFilterCriteriasMap);
						/*
						 * Logging in ext_req_handler_blocked_details table if
						 * drug excluded because of drug filter type SCHEDULE
						 */
						if (bisDrugExcluded) {
							eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
									finalEncryptionKey, transactionData, cvBlockTypeDrugExcluded,
									cvBlockReasonScheduleDrugExcluded);
						}
						if (!bisDrugExcluded) {
							bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
									transactionData.get(0).getDrugGPI(), drugFilterCriteriasMap);
						
							/*
							 * Logging in ext_req_handler_blocked_details table
							 * if drug excluded because of drug filter type GPI
							 */
							if (bisDrugExcluded) {
								eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
										patientData, finalEncryptionKey, transactionData, cvBlockTypeDrugExcluded,
										cvBlockReasonGPIDrugExcluded);
							}
							if (!bisDrugExcluded) {
								bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
										customerId, String.valueOf(NotificationTypeKey.rxrefillinprocess), patientData,
										finalEncryptionKey, transactionData, patientDetailsMap);
							}
						}
						/* needed earlier for customer timezone */

						boolean newPromiseTimeAfterOldPromiseTimeFlag = false;
						boolean roundedUpNewPromiseTimeAfterOldPromiseTimeFlag = false;

						boolean roundUpPromiseTimeConfiguredFlag = MscriptsStringUtils
								.compareStringEquals(cvRoundTimeToNearestQuarter, Constants.NUMERIC_TRUE_STRING);

						String oldPromiseTime = null;
						// get customer preferences
						customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId, clientId,
								cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

						communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
								cvtCommNameRxAdjudicationComplete, customerId);
						// Add in the status in patient_rx_txn
						// table if not already present
						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);

							/**
							 * For partialFillStatus=C, since we do not have no
							 * previous record here for this Rx, we cannot
							 * calculate total_quantity. So setting
							 * total_quantity as null.
							 */
							if (MscriptsStringUtils.compareStringEquals(cvPartialFillStatusNew,
									transactionData.get(0).getPartialFillStatus())
									|| MscriptsStringUtils.compareStringEquals(cvPartialFillStatusPartial,
											transactionData.get(0).getPartialFillStatus())) {
								sTotalQuantity = transactionData.get(0).getQuantityDispensed();
							}

							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
									promiseTime, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
									nextSyncFillDate, iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);

							if (MscriptsStringUtils.compareStringEquals(cvPartialFillStatusNew,
									transactionData.get(0).getPartialFillStatus())
									|| MscriptsStringUtils.compareStringEquals(cvPartialFillStatusPartial,
											transactionData.get(0).getPartialFillStatus())) {
								sTotalQuantity = transactionData.get(0).getQuantityDispensed();
							} else if (MscriptsStringUtils.compareStringEquals(cvPartialFillStatusCompleted,
									transactionData.get(0).getPartialFillStatus())) {
								String sCompletionFillQuantity = transactionData.get(0).getQuantityDispensed();
								float fCompletionFillQuantity = Float.parseFloat(sCompletionFillQuantity);

								String sPartialFillQuantity = transactionStateMap.get("quantity");
								float fPartialFillQuantity = Float.parseFloat(sPartialFillQuantity);

								float fTotalQuantity = fPartialFillQuantity + fCompletionFillQuantity;
								sTotalQuantity = Float.toString(fTotalQuantity);
							}

							oldPromiseTime = transactionStateMap.get("promise_time");

							String promiseTimeUsedToUpdateTxnRecord = promiseTime;
							if (!MscriptsStringUtils.isStringEmptyOrNull(oldPromiseTime)) {

								// MCE-2125 : handled EOPN for users who do not
								// have any reminder mapping entry
								newPromiseTimeAfterOldPromiseTimeFlag = eopnUtils
										.isNewPromiseTimeAfterOldPromiseTime(oldPromiseTime, promiseTime, clientId);

								if (newPromiseTimeAfterOldPromiseTimeFlag) {
									/*
									 * application is configured to round up
									 * promise times before sending SMS so check
									 * if rounded up promise times have changed
									 * between both old and new promise times
									 */

									// MCE-2125 : handled EOPN for users who do
									// not have any reminder mapping entry
									if (roundUpPromiseTimeConfiguredFlag) {

										String customerTimezone = null;
										if (MscriptsStringUtils.isMapEmptyOrNull(customerPreference)) {
											Map customerTimezoneMap = eopnUtils.getUserTimeZone(customerId);
											customerTimezone = (String) customerTimezoneMap.get(Constants.TIME_ZONE);
										} else {
											customerTimezone = customerPreference.get(Constants.TIME_ZONE);
										}

										roundedUpNewPromiseTimeAfterOldPromiseTimeFlag = eopnUtils
												.isRoundedUpNewPromiseTimeAfterOldPromiseTime(oldPromiseTime,
														promiseTime, customerTimezone, clientId);
									}
								} else {

									/*
									 * new promise time is before old promise
									 * time, so keep old promise time in
									 * database
									 */
									promiseTimeUsedToUpdateTxnRecord = oldPromiseTime;
								}

							}
							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
									promiseTimeUsedToUpdateTxnRecord, transactionData, dawCode, sTotalQuantity,
									isSyncScriptEnrolled, nextSyncFillDate, iAutoFillenabled);

							if (!patientSyncScriptEnrolled) {
								// fix for MCE-1325 Med Sync
								eopnUtils.updatePatientAllRxTxn(isSyncScriptEnrolled, customerId, nextSyncFillDate);
							}

						}
						if (isSyncScriptEnrolled || patientSyncScriptEnrolled) {
							eopnUtils.updateSyncDateForAllRxUnderStoreForCustomer(patientDetailsMap);
						}
						// Check whether we have sent out of stock message
						// for the user for the same rx within 24 hrs -->
						// Done to avoid multiple messages with in a day
						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
								cvtCommNameRxAdjudicationComplete);

						String cvBlockInProcessPostOutOfStock = ConfigReader.readConfig(Constants.cvDefaultClientId,
								langCode, ConfigKeys.CVBLOCKINPROCESSPOSTOUTOFSTOCK);
						String rxTxnStatusId = null;
						if (null != transactionStateMap) {
							rxTxnStatusId = transactionStateMap.get("rx_txn_status_id");
						}

						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap) && (MscriptsStringUtils
								.compareStringEquals(countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								|| (roundUpPromiseTimeConfiguredFlag && roundedUpNewPromiseTimeAfterOldPromiseTimeFlag)
								|| (!roundUpPromiseTimeConfiguredFlag && newPromiseTimeAfterOldPromiseTimeFlag))
								&& MscriptsStringUtils.compareStringEquals(cvIsAdjudicationCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)
								&& isNotificationInSequence
								&& (MscriptsStringUtils.compareStringEquals(cvBlockInProcessPostOutOfStock, Constants.NUMERIC_FALSE_STRING)
										|| MscriptsStringUtils.isStringEmptyOrNull(rxTxnStatusId)
										|| (MscriptsStringUtils.compareStringEquals(cvBlockInProcessPostOutOfStock, Constants.NUMERIC_TRUE_STRING)
												&& (!Constants.NUMERIC_TRUE_STRING.equals(rxTxnStatusId))))) {

							boolean bIsBackDatedMessage = eopnUtils.isBackDatedMessage(
									transactionData.get(0).getPromiseTime(), patientNotification.getMessageDate(),
									PdxContactReason.ADJUDICATION_COMPLETE);
							/*
							 * Logging in ext_req_handler_blocked_details table
							 * if request is back Dated
							 * 
							 */
							if (bIsBackDatedMessage) {
								eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
										patientData, finalEncryptionKey, transactionData, cvBlockTypeBackDatedMessage,
										cvBlockReasonBackDatedMessage);
							}

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !isSyncScriptEnrolled
									&& !bisDrugExcluded && !bIsBackDatedMessage && mscriptsCommonService
											.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
								/*
								 * MCE-714 If Rx is enrolled in sync script, we
								 * will not be sending the Adjudication Complete
								 * message.
								 */

								/*
								 * MCE - 917 Dont't send message if its a
								 * Schedule Drug - 2
								 */

								// get the user timezone
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info("userTimezone = {}, lastFilledDate = {} ", userTimezone,
										lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx Adjudication Complete notification received during outbound communication period, Sending the reminder.... ");
									LOGGER_NON_PHI.info("Parsing the promise time from the request");
									if (MscriptsStringUtils.compareStringEquals(cvRoundTimeToNearestQuarter, Constants.NUMERIC_TRUE_STRING)) {
										promiseTime = MiscUtils.roundTimeToNearestQuarter(promiseTime, ConfigReader
												.readConfig(clientId, langCode, ConfigKeys.CVDATETIMEFORMATDOB),
												userTimezone);
									}
									formatToUse = MiscUtils.checkDayorDate(clientId, userTimezone, langCode,
											promiseTime, Constants.cvDateTimeFormatDOB);
									formatter = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, langCode, formatToUse));
									formatterInput = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, langCode, Constants.cvDateTimeFormatDOB));

									promiseDate = formatter.format(formatterInput.parse(promiseTime));
									formatter = new SimpleDateFormat(cvTimeFormatUser);

									promiseTime = formatter.format(formatterInput.parse(promiseTime));
									LOGGER_NON_PHI.info(Constants.DELETING_BULK_RX_PICKUP_INSTANCES_MSG);
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);
									/*
									 * add parameter value in communication map
									 * for sending message
									 */
									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);
									communicationContent.put(CommunicationTemplateStrings.DAY.getTemplateString(),
											promiseDate);
									communicationContent.put(CommunicationTemplateStrings.TIME.getTemplateString(),
											promiseTime);

									if (MscriptsStringUtils.compareStringEquals(cvUpdatePromiseTimeEnabled, Constants.NUMERIC_TRUE_STRING)) {
										String cvAppResolverPageUrl = ConfigReader.readConfig(clientId, langCode,
												ConfigKeys.CVAPPRESOLVERPAGEURL);
										communicationContent.put(CommunicationTemplateStrings.APP_RESOLVER_WEBPAGE_URL
												.getTemplateString(), cvAppResolverPageUrl);
									communicationContent.put(
											CommunicationTemplateStrings.CUSTOMER_FIRST_NAME.getTemplateString(),
											patientData.get(0).getPatientFirstName());

									}
									/*
									 * sending Adjudication Complete
									 * communication to user
									 * 
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameRxAdjudicationComplete, false);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(Constants.DELETING_BULK_RX_PICKUP_INSTANCES_MSG);
									LOGGER_NON_PHI.info(
											"Insert - Rx Adjudication Complete notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {

									LOGGER_NON_PHI.info(Constants.DELETING_BULK_RX_PICKUP_INSTANCES_MSG);
									LOGGER_NON_PHI.info(
											"Insert - Rx Adjudication Complete notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else {
									LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
									/*
									 * Logging blocked notification in
									 * ext_req_handler_blocked_details table
									 * with reason
									 */
									eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
											isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
											transactionData);
								}

							} else {
								LOGGER_NON_PHI.info(
										"Already sent Rx Adjudication Complete message to the user for the day, Count = {} , OR notification not in sequence (isNotificationInSequence= {})",
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY),
										isNotificationInSequence);
								eopnUtils.logDuplicateMessage(countMessagesMap, cvIsAdjudicationCommunicationEnabled,
										patientData, finalEncryptionKey, transactionData, clientId, customerId);
							}

						} 
					} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonReady)
								&& MscriptsStringUtils.compareStringEquals(cvIsReadyMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {

							contactReasonStatus = MscriptsStringUtils.compareStringEquals(
									transactionData.get(0).getPartialFillStatus(), cvPartialFillStatusPartial)
											? ConfigReader.readConfig(clientId, langCode,
													ConfigKeys.CVPARTIALFILLREADYSTATUS)
											: ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVRXREADYSTATUS);

							// delete the records from
							// patient_rx_txn table && update the
							// bulk rx table so that we dont send the
							// partial fill notification
							String cvFetchRxCoPayOnReceivingEopnReadyUpdate = ConfigReader.readConfig(clientId,
									langCode, ConfigKeys.CVFETCHRXCOPAYONRECEIVINGEOPNREADYUPDATE);
							String cvSendRxReadyMsgOnReceivingEopnReadyUpdate = ConfigReader.readConfig(clientId,
									langCode, ConfigKeys.CVSENDRXREADYMSGONRECEIVINGEOPNREADYUPDATE);

							/*
							 * Patient credit card info is shared with mscripts
							 * for contact reasons 8(Adjudication Complete) and
							 * 4(Ready)
							 * 
							 * 
							 */
							List<CreditCard> patientCreditCardList = patientData.get(0).getCreditCard();
							eopnUtils.processPatientCreditCard(Integer.parseInt(clientId), Long.parseLong(customerId),
									patientCreditCardList);

							eopnUtils.processPatientLoyaltyCard(clientId, customerId, patientData);

							if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
								LOGGER_NON_PHI.info("Record not available in  PATIENT_RX_TXN table, Inside Ready Fill");

								eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										null, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
							}
							// update the record if already present
							else {
								LOGGER_NON_PHI.info(
										"Record available PATIENT_RX_TXN table, updating the record, Inside Ready Fill");

								sTotalQuantity = transactionStateMap.get("total_quantity");

								String oldPromiseTime = transactionStateMap.get("promise_time");

								eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										oldPromiseTime, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
								if (!patientSyncScriptEnrolled) {
									// fix for MCE-1325 Med Sync
									eopnUtils.updatePatientAllRxTxn(isSyncScriptEnrolled, customerId, nextSyncFillDate);
								}

							}
							if (isSyncScriptEnrolled || patientSyncScriptEnrolled) {
								eopnUtils.updateSyncDateForAllRxUnderStoreForCustomer(patientDetailsMap);
							}
							communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId, cvtCommNamePartialFill,
									customerId);
							if (!MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getPartialFillStatus())
									&& MscriptsStringUtils.compareStringEquals(
											transactionData.get(0).getPartialFillStatus(),
											cvPartialFillStatusPartial)) {

								LOGGER_PHI.info("PartialFillStatus = {}",
										transactionData.get(0).getPartialFillStatus());
								// Check whether we have sent Partial Fill
								// message
								// for the user for the same rx within 24 hrs
								//
								// Done to avoid multiple messages with in a day
								// [GEUE-38] If rx is in ready state for same transaction,do not send partial
								// message and do not delete aging reminders
							boolean isRxReady = false;
							Map<String, String> rxStatusMap = patientNotificationDao.jdbcDataGet(
										QueryBuilder.GET_LATEST_REFILL_STATUS_AND_TRANSACTION_NUMBER,
										new Object[] { patientDetailsMap.get("prescription_id"), customerId });

							if ((!MscriptsStringUtils.isMapEmptyOrNull(rxStatusMap))
									&& MscriptsStringUtils.compareStringEquals(rxStatusMap.get("refillStatus"),
											cvtRefillStatusFilled)
									&& MscriptsStringUtils.compareStringEquals(rxStatusMap.get("txNumber"),
											transactionData.get(0).getTxNumber().toString())) {
									LOGGER_NON_PHI.info("Received Partial after Ready");
								isRxReady = true;
								}
							if (!isRxReady) {
								LOGGER_NON_PHI.info(
										"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Ready state for the prescription");
								eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
							}
								countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
										patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
										cvtCommNamePartialFill);

								if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
										&& MscriptsStringUtils.compareStringEquals(
												countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
										&& MscriptsStringUtils.compareStringEquals(cvIsPartialFillCommunicationEnabled,
												Constants.NUMERIC_TRUE_STRING) && !isRxReady) {
									// Check whether user has text msg active
									// and
									// verified = 1
									customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
											clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());
								
								Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
								drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY,
										NotificationTypeKey.rxpartialfill);
								boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId,
										DrugFilterType.GPI, transactionData.get(0).getDrugGPI(),
										drugFilterCriteriasMap);

									/*
									 * Logging in
									 * ext_req_handler_blocked_details table if
									 * drug excluded because of drug filter type
									 * GPI
									 */
									if (bisDrugExcluded) {
										eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
												patientData, finalEncryptionKey, transactionData,
												cvBlockTypeDrugExcluded, cvBlockReasonGPIDrugExcluded);
									}

									if (!bisDrugExcluded) {
										bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(
												clientId, customerId, String.valueOf(NotificationTypeKey.rxpartialfill),
												patientData, finalEncryptionKey, transactionData, patientDetailsMap);
									}

									boolean bIsBackDatedMessage = eopnUtils.isBackDatedMessage(
											transactionData.get(0).getPromiseTime(),
											patientNotification.getMessageDate(), PdxContactReason.PARTIAL_FILL);
									/*
									 * Logging in
									 * ext_req_handler_blocked_details table if
									 * request is back Dated
									 * 
									 */
									if (bIsBackDatedMessage) {
										eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
												patientData, finalEncryptionKey, transactionData,
												cvBlockTypeBackDatedMessage, cvBlockReasonBackDatedMessage);
									}

									if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
											&& isRxTransactionMessageAllowedForStore && !isSyncScriptEnrolled
											&& !bisDrugExcluded && !bIsBackDatedMessage) {
										/*
										 * MCE-714 If Rx is enrolled in sync
										 * script, we will not be sending the
										 * Partial Fill message.
										 */

										// TD - check the time before
										// sending
										// notification

										// get the user timezone
										userTimezone = customerPreference.get(Constants.TIME_ZONE);
										lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
												transactionData.get(0).getLastFilledDate());
										notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
										LOGGER_PHI.info(" Inside Partial Fill,userTimezone = {}, lastFilledDate = {} ",
												userTimezone, lastFilledDate);
										if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
												&& MscriptsStringUtils.compareStringEquals(
														notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
														Constants.TIME_BTW_CUTOFFS)) {

											LOGGER_NON_PHI
													.info("Partial Fill notification received during outbound communication period, Add a 10 min delay in partial fill Message, will be sent via Job schelduler"
															+ "Add in Bulk Rx pick up table ");
											cvDelayInNotification = ConfigReader.readConfig(clientId, langCode,
													ConfigKeys.CVDELAYINNOTIFICATION);
											calendar = Calendar.getInstance(TimeZone.getTimeZone(cvTimeZoneUTC));
											calendar.add(Calendar.MINUTE, Integer.parseInt(cvDelayInNotification));
											formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
											formatter.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
											notificationTimeMap.put(Constants.SENDPICKUPTIME,
													formatter.format(calendar.getTime()));
											Map<String, Map<String, String>> consolidatedPatientDataMap = new HashMap <String, Map<String, String>>();
											consolidatedPatientDataMap.put("patientDetailsMap", patientDetailsMap);
											consolidatedPatientDataMap.put("notificationTimeMap", notificationTimeMap);
											consolidatedPatientDataMap.put("customerPreference", customerPreference);
											consolidatedPatientDataMap.put("communicationIdMap", communicationIdMap);
											eopnUtils.insertBulkRxPickup(clientId, transactionData, pharmacyData, lastFilledDate, cvtRefillStatusFilled, patientData, finalEncryptionKey, consolidatedPatientDataMap);
											// Send message to the user if all
											// the
											// above criteria is fulfilled
											// Mask the RX number

										} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
												&& MscriptsStringUtils.compareStringEquals(
														notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
														Constants.TIME_LESS_THATN_LOWER_CUT_OFF)) {

											// Send the reminder next day
											LOGGER_NON_PHI.info(
													"Insert - Partial Fill notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

											Map<String, Map<String, String>> consolidatedPatientDataMap = new HashMap <String, Map<String, String>>();
											consolidatedPatientDataMap.put("patientDetailsMap", patientDetailsMap);
											consolidatedPatientDataMap.put("notificationTimeMap", notificationTimeMap);
											consolidatedPatientDataMap.put("customerPreference", customerPreference);
											consolidatedPatientDataMap.put("communicationIdMap", communicationIdMap);
											// Insert in bulk rx pickup
											// table to
											// be send by JOB scheduler
											eopnUtils.insertBulkRxPickup(clientId, transactionData, pharmacyData, lastFilledDate, cvtRefillStatusFilled, patientData, finalEncryptionKey, consolidatedPatientDataMap);
										} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
												&& MscriptsStringUtils.compareStringEquals(
														notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
														Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)) {

											// Send the reminder next day
											LOGGER_NON_PHI.info(
													"Insert - Partial Fill notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
											// Insert in bulk rs pickup table to
											// be send by JOB scheduler
											Map<String, Map<String, String>> consolidatedPatientDataMap = new HashMap <String, Map<String, String>>();
											consolidatedPatientDataMap.put("patientDetailsMap", patientDetailsMap);
											consolidatedPatientDataMap.put("notificationTimeMap", notificationTimeMap);
											consolidatedPatientDataMap.put("customerPreference", customerPreference);
											consolidatedPatientDataMap.put("communicationIdMap", communicationIdMap);
											eopnUtils.insertBulkRxPickup(clientId, transactionData, pharmacyData, lastFilledDate, cvtRefillStatusFilled, patientData, finalEncryptionKey, consolidatedPatientDataMap);
										}
									} else {
										LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
										/*
										 * Logging blocked notification in
										 * ext_req_handler_blocked_details table
										 * with reason
										 */
										eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
												isSyncScriptEnrolled, clientId, customerId, patientData,
												finalEncryptionKey, transactionData);
									}

								} else {
									LOGGER_NON_PHI
											.info("Already sent partial fill message to the user for the day, Count = "
													+ countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
													if (isRxReady) {
														eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData, finalEncryptionKey, transactionData, cvBlockTypeReceivedReadyBeforePartial, cvBlockReasonReceivedReadyBeforePartial);
													} else {
														eopnUtils.logDuplicateMessage(countMessagesMap, cvIsPartialFillCommunicationEnabled,
														patientData, finalEncryptionKey, transactionData, clientId, customerId);
													}
									
								}

							}

							else if (!MscriptsStringUtils
									.isStringEmptyOrNull(transactionData.get(0).getPartialFillStatus())
									&& MscriptsStringUtils.compareStringEquals(
											transactionData.get(0).getPartialFillStatus(),
											cvPartialFillStatusCompleted)) {

								LOGGER_NON_PHI
										.info("Deleting from Refill Promise time table as we received Ready from WSO2");
								eopnUtils.deletePatientRxTxnMedian(transactionData, pharmacyData);
							} else if ((MscriptsStringUtils
									.isStringEmptyOrNull(transactionData.get(0).getPartialFillStatus())
									|| MscriptsStringUtils.compareStringEquals(
											transactionData.get(0).getPartialFillStatus(), cvPartialFillStatusNew))
									&& MscriptsStringUtils.compareStringEquals(
											cvSendRxReadyMsgOnReceivingEopnReadyUpdate,
											Constants.NUMERIC_TRUE_STRING)) {
								// mce-1516
								LOGGER_PHI.info("PartialFillStatus = {} ",
										transactionData.get(0).getPartialFillStatus());
								// mce-1560
								// if cvFetchRxCoPayOnReceivingEopnReadyUpdate
								// is turned on then we have to fetch copay
								// amount
								String copay = null;
								String cvPickupReminderMappingId = ConfigReader.readConfig(clientId, langCode,
										ConfigKeys.CVPICKUPREMINDERMAPPINGID);
								if (MscriptsStringUtils.compareStringEquals(cvFetchRxCoPayOnReceivingEopnReadyUpdate,
										Constants.NUMERIC_TRUE_STRING)
										&& (!MscriptsStringUtils
												.isStringEmptyOrNull(patientData.get(0).getRxcomPatientID()))) {
									PdxDispensingRequest pdxDispensingRequest = new PdxDispensingRequest();
									pdxDispensingRequest.setClientId(clientId);
									pdxDispensingRequest.setLanguage(langCode);
									pdxDispensingRequest.setPrimaryIdentifier(patientData.get(0).getRxcomPatientID());

									PdxDispensingResponse dispensingResponse = (PdxDispensingResponse) dispensingPatientService
											.selectPatient(pdxDispensingRequest, null);
									// from patient profile trying to find the
									// rx number and store ncpdpid from list of
									// prescriptions
									for (Prescription prescription : dispensingResponse.getPatientList().get(0)
											.getPrescriptionsList()) {
										if (null != prescription && null != transactionData.get(0)
												&& null != transactionData.get(0).getPrescriptionNumber()
												&& transactionData.get(0).getPrescriptionNumber()
														.equals(prescription.getRxNum())) {

											copay = prescription.getCopay();
											break;
										}
									}
								} // updating the copay amount and ready status
									// in customer_prescriptions
								if (MscriptsStringUtils.compareStringEquals(cvSendRxReadyMsgOnReceivingEopnReadyUpdate,
										Constants.NUMERIC_TRUE_STRING) && null != copay) {
									patientNotificationDao.insertOrUpdateJdbcData(
											QueryBuilder.UPDATE_CUSTOMER_PRESCRIPTION_COPAY,
											new Object[] { copay, cvtRefillStatusFilled,
													patientDetailsMap.get(Constants.PRESCRIPTION_ID) });

								}
								LOGGER_NON_PHI.info(
										"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Ready state for the prescription");

								/*
								 * this call is to check if we have sent any
								 * ready message (with
								 * cvtCommNameRxReadyInStoreWithCopay or
								 * cvtCommNameRxReadyInStore) in last 24 hours
								 * for the same rx
								 */
								countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
										patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
										cvtCommNameRxReadyInStoreWithCopay);
								Map<String, String> countMessageReasyOnStoreCommunicationMap = eopnUtils
										.getPrescNotificationTypeCountWithinSpecifiedHours(
												patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
												cvtCommNameRxReadyInStore);
								Map<String, String> countReadyMessageWithDeliveryLickCommunicationMap = eopnUtils
										.getPrescNotificationTypeCountWithinSpecifiedHours(
												patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
												cvtCommNameRxReadyInStoreWithCopayAndDeliveryLink);
								Map<String, String> bulkRxPickupCount = patientNotificationDao
										.jdbcDataGet(QueryBuilder.GET_BULK_RX_PICKUP_INSTANCE_COUNT,
												new Object[] { patientDetailsMap.get(Constants.PRESCRIPTION_ID),
														cvtCommNameRxReadyInStoreWithCopay,
														cvtCommNameRxReadyInStore,cvtCommNameRxReadyInStoreWithCopayAndDeliveryLink });
								/*
								 * if there are no messages sent in last 24
								 * hours for this rx AND if
								 * cvIsReadyCommunicationEnabled is turned on
								 * then proceed
								 */
								if ((((!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap) && MscriptsStringUtils
										.compareStringEquals(countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY),
												Constants.NUMERIC_FALSE_STRING))
										&& (!MscriptsStringUtils
												.isMapEmptyOrNull(countMessageReasyOnStoreCommunicationMap)
												&& MscriptsStringUtils.compareStringEquals(
														countMessageReasyOnStoreCommunicationMap
																.get(Constants.VARPICKUPCOUNTFORTODAY),
														Constants.NUMERIC_FALSE_STRING))
										&& (!MscriptsStringUtils
												.isMapEmptyOrNull(countReadyMessageWithDeliveryLickCommunicationMap)
												&& MscriptsStringUtils.compareStringEquals(
														countReadyMessageWithDeliveryLickCommunicationMap
																.get(Constants.VARPICKUPCOUNTFORTODAY),
														Constants.NUMERIC_FALSE_STRING)))
										|| (MscriptsStringUtils.compareStringEquals(
												bulkRxPickupCount.get("bulk_rx_pickup_count"), Constants.NUMERIC_FALSE_STRING)))
										&& (MscriptsStringUtils.compareStringEquals(cvIsReadyCommunicationEnabled,
												Constants.NUMERIC_TRUE_STRING))) {
									/*
									 * check if user's communication prefrence
									 */
									boolean isMessageSent = true;
									if ((!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap) && MscriptsStringUtils
											.compareStringEquals(countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY),
													Constants.NUMERIC_FALSE_STRING))
											&& (!MscriptsStringUtils
													.isMapEmptyOrNull(countMessageReasyOnStoreCommunicationMap)
													&& MscriptsStringUtils.compareStringEquals(
															countMessageReasyOnStoreCommunicationMap
																	.get(Constants.VARPICKUPCOUNTFORTODAY),
															Constants.NUMERIC_FALSE_STRING))
											&& (!MscriptsStringUtils
													.isMapEmptyOrNull(countReadyMessageWithDeliveryLickCommunicationMap)
													&& MscriptsStringUtils.compareStringEquals(
															countReadyMessageWithDeliveryLickCommunicationMap
																	.get(Constants.VARPICKUPCOUNTFORTODAY),
															Constants.NUMERIC_FALSE_STRING))) {
										isMessageSent = false;
									}
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									customerTextPreference = patientNotificationDao
											.jdbcDataGet(QueryBuilder.GET_CUSTOMER_TEXT_INFO,
													new Object[] { clientId, customerId, clientId,
															cvPickupReminderMappingId,
															pharmacyData.get(0).getPharmacyNCPDP() });
								Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
								drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY,
										NotificationTypeKey.rxrefillreadyforpickup);
								boolean bisDrugExcluded = drugFilterCriteriaUtil.isDrugExcluded(clientId,
										DrugFilterType.GPI, transactionData.get(0).getDrugGPI(),
										drugFilterCriteriasMap);

									String reminderMode = "";
									if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)) {
										reminderMode = customerTextPreference.get(Constants.SEND_REMINDER_MODE);
									}
									SendCommunicationMode sendCommunicationMode = eopnUtils
											.setSendCommunicationMode(reminderMode, customerTextPreference, customerId);
									/*
									 * Logging in
									 * ext_req_handler_blocked_details table if
									 * drug excluded because of drug filter type
									 * GPI
									 */
									if (bisDrugExcluded) {
										eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
												patientData, finalEncryptionKey, transactionData,
												cvBlockTypeDrugExcluded, cvBlockReasonGPIDrugExcluded);
									}

									/*
									 * Logging in
									 * ext_req_handler_blocked_details table if
									 * drug excluded because of customer
									 * Notification disabled
									 * 
									 */
									if (bisDrugExcluded) {
										eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
												patientData, finalEncryptionKey, transactionData,
												cvBlockTypeDrugExcluded, cvBlockReasonNotificationDisabled);
									}

									boolean bIsBackDatedMessage = eopnUtils.isBackDatedMessage(
											transactionData.get(0).getPromiseTime(),
											patientNotification.getMessageDate(), PdxContactReason.READY);
									/*
									 * Logging in
									 * ext_req_handler_blocked_details table if
									 * request is back Dated
									 * 
									 */
									if (bIsBackDatedMessage) {
										eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId,
												patientData, finalEncryptionKey, transactionData,
												cvBlockTypeBackDatedMessage, cvBlockReasonBackDatedMessage);
									}

									if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)

											&& ((MscriptsStringUtils.compareStringEquals(reminderMode,
													cvsendReminderModeText)
													&& (sendCommunicationMode.getSendSms()
															.equals(Constants.NUMERIC_TRUE_STRING)))
													|| (MscriptsStringUtils.compareStringEquals(reminderMode,
															Constants.EMAIL)
															&& (sendCommunicationMode.getSendEmail()
																	.equals(Constants.NUMERIC_TRUE_STRING)))
													|| (MscriptsStringUtils.compareStringEquals(reminderMode,
															Constants.IOS_PUSH)
															&& (sendCommunicationMode.getSendApns()
																	.equals(Constants.NUMERIC_TRUE_STRING)))
													|| (MscriptsStringUtils.compareStringEquals(reminderMode,
															Constants.IVR)
															&& (sendCommunicationMode.getSendIvr()
																	.equals(Constants.NUMERIC_TRUE_STRING)))
													|| (MscriptsStringUtils.compareStringEquals(reminderMode,
															Constants.ANDROID_PUSH)
															&& (sendCommunicationMode.getSendGcms()
																	.equals(Constants.NUMERIC_TRUE_STRING))))
											&& isRxTransactionMessageAllowedForStore && mscriptsCommonService
													.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
										/*
										 * MCE-714 If Rx is enrolled in sync
										 * script, we will not be sending the
										 * Partial Fill message.
										 */
										LOGGER_NON_PHI.info("mobile_number_verified = "
												+ customerTextPreference.get("mobile_number_verified")
												+ " is_text_message_active = "
												+ customerTextPreference.get("is_text_message_active")
												+ " send_reminder_mode = "
												+ customerTextPreference.get(Constants.SEND_REMINDER_MODE));
										LOGGER_NON_PHI.info(
												"User is Eligible to receive Text message,Checking the time of the notification received");
										// TD - check the time before
										// sending
										// notification

										// get the user timezone
										userTimezone = customerTextPreference.get(Constants.TIME_ZONE);
										lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
												transactionData.get(0).getLastFilledDate());
										notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
										LOGGER_PHI.info(" Inside Partial Fill,userTimezone = {}, lastFilledDate = ",
												userTimezone, lastFilledDate);
										String communicationName = cvtCommNameRxReadyInStore;
										if (isSyncScriptEnrolled
												|| !MscriptsStringUtils.compareStringEquals(cvCopayEnabled, Constants.NUMERIC_TRUE_STRING)) {
											communicationName = cvtCommNameRxReadyInStore;
											copay = "";
										} else if (MscriptsStringUtils.compareStringEquals(cvCopayEnabled, Constants.NUMERIC_TRUE_STRING)
												&& null != copay) {
											communicationName = cvtCommNameRxReadyInStoreWithCopay;
										}
										// To check whether text delivery
										// enabled or not
										boolean isTextDeliveryAllowed = checkIfTextDeliveryEnabled(
												clientId, patientDetailsMap.get(Constants.PRESCRIPTION_ID),transactionData.get(0));
										String orderUrl = null;
										if (isTextDeliveryAllowed) {
											communicationName = cvtCommNameRxReadyInStoreWithCopayAndDeliveryLink;
											
											// If text based delivery is allowed then get the order url
											customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey,
													customerId, clientId, cvOtherReminderMappingID,
													pharmacyData.get(0).getPharmacyNCPDP());
											if(!MscriptsStringUtils.isMapEmptyOrNull(customerPreference) && !MscriptsStringUtils.isStringEmptyOrNull(customerPreference.get(Constants.MOBILE_NUMBER)))
												orderUrl = eopnUtils.getOrderUrl(clientId, customerId, customerPreference.get(Constants.MOBILE_NUMBER));
										}
										
										communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
												communicationName, customerId);
										String nextMedsyncReadyDate = null;
										if (isSyncScriptEnrolled) {
											Map<String, String> nextMedsyncDateMap = eopnUtils.getNextMedSyncDate(
													customerTextPreference, nextSyncFillDate, clientId);
											if (null != nextMedsyncDateMap) {
												nextMedsyncReadyDate = nextMedsyncDateMap
														.get("next_medsync_ready_date");
											}
										}
										// if it is a backdated then we'll not
										// send any instantaneous message but
										// schedule the aging pickup reminders
										eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
										formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
										formatterInput = new SimpleDateFormat(cvDateTimeFormatInputParsed);
										String startDate = formatter.format(formatterInput
												.parse(patientNotification.getMessageDate().substring(0, 19)));
										if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
												&& MscriptsStringUtils.compareStringEquals(
														notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
														Constants.TIME_BTW_CUTOFFS)
												&& mscriptsCommonService
														.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

											LOGGER_NON_PHI
													.info("Ready notification received during outbound communication period, Add a 10 min delay in partial fill Message, will be sent via Job schelduler"
															+ "Add in Bulk Rx pick up table ");
											cvDelayInNotification = ConfigReader.readConfig(clientId, langCode,
													ConfigKeys.CVDELAYINNOTIFICATION);
											calendar = Calendar.getInstance(TimeZone.getTimeZone(cvTimeZoneUTC));
											calendar.add(Calendar.MINUTE, Integer.parseInt(cvDelayInNotification));
											formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
											formatter.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
											String nextStartDate = isSyncScriptEnrolled && null != nextMedsyncReadyDate
													? nextMedsyncReadyDate : formatter.format(calendar.getTime());
											startDate = bIsBackDatedMessage ? startDate : nextStartDate;

											eopnUtils.insertIntoBulkRx(clientId, transactionData, pharmacyData,
													lastFilledDate, patientDetailsMap, bIsBackDatedMessage,
													nextStartDate, startDate, customerId, sendCommunicationMode,
													communicationIdMap);
											if (!isSyncScriptEnrolled && !bIsBackDatedMessage && !isMessageSent) {
												// get customer preferences
												if(MscriptsStringUtils.isMapEmptyOrNull(customerPreference)) {
													customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey,
															customerId, clientId, cvOtherReminderMappingID,
															pharmacyData.get(0).getPharmacyNCPDP());
												}
												
												String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
														customerPreference.get(Constants.SHOW_RX_NAME), clientId);
												/*
												 * add parameter value in
												 * communication map for sending
												 * message
												 */
												String cvRefillRxPrefix = ConfigReader.readConfig(clientId, langCode,
														ConfigKeys.CVREFILLRXPREFIX);
												String cvDateFormatUser = ConfigReader.readConfig(clientId, langCode,
														ConfigKeys.CVDATEFORMATUSER);

												Calendar now = Calendar.getInstance();
												now.add(Calendar.DATE, Integer.parseInt(cvPickupRestockingLimit));
												Date date = now.getTime();
												SimpleDateFormat format1 = new SimpleDateFormat(cvDateFormatUser);
												String restockDay = format1.format(date);

												Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

												communicationContent.put(
														CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
														rxNameOrNumber);
												communicationContent.put(
														CommunicationTemplateStrings.COPAY_AMOUNT.getTemplateString(),
														copay);
												communicationContent.put(CommunicationTemplateStrings.REFILL_RX_PREFIX
														.getTemplateString(), cvRefillRxPrefix);
												communicationContent.put(CommunicationTemplateStrings.REFILL_RX_SUFFIX
														.getTemplateString(), "");
												communicationContent.put(
														CommunicationTemplateStrings.RESTOCK_DAY.getTemplateString(),
														restockDay);
												communicationContent.put(
														CommunicationTemplateStrings.COPAY_AMT.getTemplateString(),
														copay);
												// this tag added in *RX Ready
												// in Store* template but for
												// eopn instantaneous it'll
												// always be empty
												communicationContent
														.put(CommunicationTemplateStrings.TOTAL_COPAY_AMT_PREFIX
																.getTemplateString(), "");
												communicationContent.put(
														CommunicationTemplateStrings.QUANTITY.getTemplateString(),
														transactionData.get(0).getQuantityDispensed());
												communicationContent.put(
														CommunicationTemplateStrings.ORDER_URL.getTemplateString(),
														orderUrl);
												
												eopnUtils.sendEOPNCommunicationToUser(clientId, customerId,
														transactionData, patientDetailsMap, patientData,
														finalEncryptionKey, customerPreference, communicationContent,
														communicationName, true);
												// update
												// bulk_rx_pickup_instances to
												// error note and all 0
												patientNotificationDao
														.insertOrUpdateJdbcData(
																QueryBuilder.UPDATE_BULK_RX_PICKUP_INSTANCES_ALREADY_SENT,
																new Object[] { Constants.NUMERIC_FALSE_STRING,
																		Constants.NUMERIC_FALSE_STRING,
																		Constants.NUMERIC_FALSE_STRING,
																		Constants.NUMERIC_FALSE_STRING,
																		Constants.NUMERIC_FALSE_STRING,
																		patientDetailsMap
																				.get(Constants.PRESCRIPTION_CODE),
																		"Instantaneous Ready Message sent out",
																		patientDetailsMap
																				.get(Constants.PRESCRIPTION_ID),
																		transactionData.get(0).getTxNumber(),
																		Constants.NUMERIC_TRUE_STRING });
											}
											// Send message to the user if all
											// the
											// above criteria is fulfilled
											// Mask the RX number

										} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
												&& MscriptsStringUtils.compareStringEquals(
														notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
														Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
												&& mscriptsCommonService
														.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

											// Send the reminder next day
											LOGGER_NON_PHI.info(
													"Insert - Partial Fill notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

											// Insert in bulk rx pickup
											// table to
											// be send by JOB scheduler
											String nextStartDate = isSyncScriptEnrolled && null != nextMedsyncReadyDate
													? nextMedsyncReadyDate
													: notificationTimeMap.get(Constants.SENDPICKUPTIME);
											startDate = bIsBackDatedMessage ? startDate : nextStartDate;
											eopnUtils.insertIntoBulkRx(clientId, transactionData, pharmacyData,
													lastFilledDate, patientDetailsMap, bIsBackDatedMessage,
													nextStartDate, startDate, customerId, sendCommunicationMode,
													communicationIdMap);
										} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
												&& MscriptsStringUtils.compareStringEquals(
														notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
														Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
												&& mscriptsCommonService
														.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

											// Send the reminder next day
											LOGGER_NON_PHI.info(
													"Insert - Ready notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
											// Insert in bulk rs pickup table to
											// be send by JOB scheduler
											String nextStartDate = isSyncScriptEnrolled && null != nextMedsyncReadyDate
													? nextMedsyncReadyDate
													: notificationTimeMap.get(Constants.SENDPICKUPTIME);
											startDate = bIsBackDatedMessage ? startDate : nextStartDate;
											eopnUtils.insertIntoBulkRx(clientId, transactionData, pharmacyData,
													lastFilledDate, patientDetailsMap, bIsBackDatedMessage,
													nextStartDate, startDate, customerId, sendCommunicationMode,
													communicationIdMap);
										}
										// fetch prescription code
										if (mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
											Map<String, String> prescriptionCode = patientNotificationDao.jdbcDataGet(
													QueryBuilder.GET_PRESCRIPTION_CODE,
													new Object[] { clientId, customerId, "RxPickup" });
										
													Map<String, Object> customerprescriptionMap = new HashMap<>();
													customerprescriptionMap.put(CommunicationTemplateStrings.CUSTOMER_ID.getTemplateString(), customerId);
													customerprescriptionMap.put(CommunicationTemplateStrings.PRESCRIPTION_ID.getTemplateString(),
															patientDetailsMap.get("prescription_id"));
													customerprescriptionMap.put(CommunicationTemplateStrings.PRESCRIPTION_CODE.getTemplateString(),
															prescriptionCode.get("prescription_code"));
													customerprescriptionMap.put(CommunicationTemplateStrings.RX_NUMBER.getTemplateString(),
															transactionData.get(0).getPrescriptionNumber());
													customerprescriptionMap.put(CommunicationTemplateStrings.STORE_NCPDP_ID.getTemplateString(),
															pharmacyData.get(0).getPharmacyNCPDP());
													customerprescriptionMap.put(CommunicationTemplateStrings.FILLED_DATE.getTemplateString(), lastFilledDate);
													customerprescriptionMap.put(CommunicationTemplateStrings.REFILL_STATUS.getTemplateString(), cvtRefillStatusFilled);
													customerprescriptionMap.put(CommunicationTemplateStrings.LATEST_TX_NUMBER.getTemplateString(),
															transactionData.get(0).getTxNumber());
													customerprescriptionMap.put(CommunicationTemplateStrings.REMINDER_START_DATE.getTemplateString(), startDate);
													customerprescriptionMap.put(CommunicationTemplateStrings.CUSTOMER_TIME_ZONE.getTemplateString(),
															customerTextPreference.get("timezone"));
													customerprescriptionMap.put(CommunicationTemplateStrings.SEND_HOUR.getTemplateString(),
															customerTextPreference.get("send_reminder_hour"));
													customerprescriptionMap.put(CommunicationTemplateStrings.CREATED_BY.getTemplateString(), "EOPN_processPatientNotification");
													customerprescriptionMap.put(CommunicationTemplateStrings.LAST_UPDATE_BY.getTemplateString(), "EOPN_processPatientNotification");
													// call insert aging reminder method to insert aging pickup reminders 
													mscriptsCommonService.insertAgeingPickupReminders(clientId, customerprescriptionMap, isTextDeliveryAllowed);

											/*
											 * if it is back dated message
											 * update sendText = 0,sendEmail =
											 * 0,sendApns = 0,sendGcms =
											 * 0,prescription_code for first
											 * message and for aging reminders
											 * update reminder mode
											 */
											if (bIsBackDatedMessage || isMessageSent) {
												patientNotificationDao.insertOrUpdateJdbcData(
														QueryBuilder.UPDATE_BULK_RX_PICKUP_INSTANCES_BACKDATED,
														new Object[] { Constants.NUMERIC_FALSE_STRING,
																Constants.NUMERIC_FALSE_STRING,
																Constants.NUMERIC_FALSE_STRING,
																Constants.NUMERIC_FALSE_STRING,
																Constants.NUMERIC_FALSE_STRING,
																prescriptionCode.get(Constants.PRESCRIPTION_CODE),
																patientDetailsMap.get(Constants.PRESCRIPTION_ID),
																transactionData.get(0).getTxNumber(),
																Constants.NUMERIC_TRUE_STRING });
												patientNotificationDao.insertOrUpdateJdbcData(
														QueryBuilder.UPDATE_BULK_RX_PICKUP_INSTANCES_BACKDATED,
														new Object[] { sendCommunicationMode.getSendSms(),
																sendCommunicationMode.getSendEmail(),
																sendCommunicationMode.getSendApns(),
																sendCommunicationMode.getSendGcms(),
																sendCommunicationMode.getSendIvr(),
																prescriptionCode.get(Constants.PRESCRIPTION_CODE),
																patientDetailsMap.get(Constants.PRESCRIPTION_ID),
																transactionData.get(0).getTxNumber(),
																Constants.NUMERIC_FALSE_STRING });
											} else {
												// update aging reminders
												patientNotificationDao.insertOrUpdateJdbcData(
														QueryBuilder.UPDATE_BULK_RX_PICKUP_INSTANCES,
														new Object[] { sendCommunicationMode.getSendSms(),
																sendCommunicationMode.getSendEmail(),
																sendCommunicationMode.getSendApns(),
																sendCommunicationMode.getSendGcms(),
																sendCommunicationMode.getSendIvr(),
																prescriptionCode.get(Constants.PRESCRIPTION_CODE),
																patientDetailsMap.get(Constants.PRESCRIPTION_ID),
																transactionData.get(0).getTxNumber() });
											}					 
										 

										}
									} else {
										LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
										/*
										 * Logging blocked notification in
										 * ext_req_handler_blocked_details table
										 * with reason
										 */
										if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)
												&& MscriptsStringUtils.compareStringEquals(
														customerTextPreference.get("mobile_number_verified"), Constants.NUMERIC_FALSE_STRING)) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypeMobileNotVerified, cvBlockReasonMobileNotVerified);

										} else if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)
												&& MscriptsStringUtils.compareStringEquals(
														customerTextPreference.get("is_text_message_active"), Constants.NUMERIC_FALSE_STRING)) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypeTextNotActive, cvBlockReasonTextNotActive);

										} else if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)
												&& MscriptsStringUtils.compareStringEquals(
														customerTextPreference.get(Constants.SEND_REMINDER_MODE),
														cvsendReminderModeAndroidPush)) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypePush, cvBlockReasonPush);

										} else if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)
												&& MscriptsStringUtils.compareStringEquals(
														customerTextPreference.get(Constants.SEND_REMINDER_MODE),
														cvSendReminderModeIosPush)) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypePush, cvBlockReasonPush);

										} else if (!MscriptsStringUtils.isMapEmptyOrNull(customerTextPreference)
												&& MscriptsStringUtils.compareStringEquals(
														customerTextPreference.get(Constants.SEND_REMINDER_MODE),
														cvSendReminderModeEmail)) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypeEmail, cvBlockReasonEmail);

										} else if (!isRxTransactionMessageAllowedForStore) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypeStoreNotAllowed, cvBlockReasonStoreNotAllowed);

										} else if (isSyncScriptEnrolled) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypeSyncScript, cvBlockReasonSyncScript);

										} else if (!mscriptsCommonService
												.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

											eopnUtils.insertIntoExternalRequestHandlerBlockedDetails(clientId,
													customerId, patientData, finalEncryptionKey, transactionData,
													cvBlockTypeConsentNotGiven, cvBlockReasonConsentNotGiven);

										} else {
											LOGGER_NON_PHI.info(
													"Failed to log blocked notification dateails in ext_req_handler_blocked_details table");
										}
									}

								} else {
									LOGGER_NON_PHI
											.info("Already sent partial fill message to the user for the day, Count = "
													+ countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
									eopnUtils.logDuplicateMessage(countMessagesMap, cvIsPartialFillCommunicationEnabled,
											patientData, finalEncryptionKey, transactionData, clientId, customerId);
								}
							}

						} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonSold)
								&& MscriptsStringUtils.compareStringEquals(cvIsSoldMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {

							contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
									ConfigKeys.CVRXSOLDSTATUS);

							if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
								LOGGER_NON_PHI.info("Record not available in  PATIENT_RX_TXN table, Inside Sold");

								eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										null, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
							}
							// update the record if already present
							else {
								LOGGER_NON_PHI.info(
										"Record available PATIENT_RX_TXN table, updating the record, Inside Sold");

								patientNotificationDao.insertOrUpdateJdbcData(
										QueryBuilder.UPDATE_PATIENT_RX_TXN_CHECKOUT_COMPLETE,
										new Object[] { contactReasonStatus, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, customerId,
												patientDetailsMap.get(Constants.PRESCRIPTION_ID) });
							}

							// update quantity & quantity_unit in Rx Refill
							// History
							eopnUtils.updateRxQuantityInRefillHistory(patientDetailsMap.get(Constants.PRESCRIPTION_ID),
									transactionData.get(0).getQuantityDispensed(),
									transactionData.get(0).getDrugUnit());
							// here we have to add code for 1210
							// we should add a flag so that other clients don't
							// get affected
							String cvIsMedSyncNotificationEnabled = ConfigReader.readConfig(Constants.cvDefaultClientId,
									Constants.DEFAULT_LANGUAGE, ConfigKeys.CVISMEDSYNCNOTIFICATIONENABLED);
							String support_email_address = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
									Constants.cvSupportEmailAddress);
							String support_phone_number = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
									Constants.cvSupportPhoneNumber);
							String client_name = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
									Constants.cvClientName);
							String cvMedSyncNotificationBuffer = ConfigReader.readConfig(clientId,
									Constants.DEFAULT_LANGUAGE, Constants.cvMedSyncNotificationBuffer);
							eopnUtils.updateExpressCheckoutAndLogRxSoldStatus(patientDetailsMap);

							if (isSyncScriptEnrolled && null != cvIsMedSyncNotificationEnabled
									&& Constants.NUMERIC_TRUE_STRING.equals(cvIsMedSyncNotificationEnabled)) {
								// here we have to calculate the next sync
								// fill
								// date as per the logic mentioned in
								// mce-1210
								// willCallPickedUpDate is considering as
								// sold
								// date
								nextSyncFillDate = eopnUtils.calculateNextSyncFillDate(transactionData.get(0),
										customerId, pharmacyData.get(0).getPharmacyNCPDP());
								// fetch communication map
								if (null != nextSyncFillDate) {
									String cvtNextMedSyncDateNotification = ConfigReader.readConfig(clientId, langCode,
											ConfigKeys.CVTNEXTMEDSYNCDATENOTIFICATION);
									Map<String, String> medSyncCommunication = eopnUtils
											.populateCommunicationMap(clientId, cvtNextMedSyncDateNotification);
									Map<String, String> customerDetails = eopnUtils.fetchCustomerDetails(clientId,
											customerId, patientDetailsMap.get(Constants.PRESCRIPTION_ID));

									// change nextmedsync date format in
									// text,email or push message
									formatToUse = MiscUtils.checkDayorDate(clientId,
											customerDetails.get(Constants.TIME_ZONE), Constants.DEFAULT_LANGUAGE,
											new SimpleDateFormat(cvDateTimeFormatDOB)
													.format(new SimpleDateFormat(Constants.DATE_TIME_FORMAT)
															.parse(nextSyncFillDate.substring(0, 19))),
											Constants.cvDateTimeFormatDOB);
									// here we'll check possible date
									// formats for next sync fill date that
									// we'll be passing as notification
									String otherMedSyncDateFormat = Constants.cvDateFormatUser;
									if (otherMedSyncDateFormat.equalsIgnoreCase(formatToUse)) {
										otherMedSyncDateFormat = Constants.cvDayFormatUser;
									}
									formatterInput = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
									SimpleDateFormat sdf = new SimpleDateFormat(ConfigReader.readConfig(clientId,
											Constants.DEFAULT_LANGUAGE, otherMedSyncDateFormat));
									String otherMedSyncFormatDate = sdf
											.format(formatterInput.parse(nextSyncFillDate.substring(0, 19)));
									sdf = new SimpleDateFormat(
											ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE, formatToUse));

									// this object is set to frame
									// text,email and push messages
									MedSyncData medSyncData = new MedSyncData();
									nextSyncFillDate = sdf
											.format(formatterInput.parse(nextSyncFillDate.substring(0, 19)));
									// every variable is set for framing the
									// message,email or push
									medSyncData.setNext_med_sync_date(nextSyncFillDate);
									medSyncData.setStore_address_line1(customerDetails.get(Constants.ADDRESS_LINE_1));
									medSyncData.setStore_name(customerDetails.get(Constants.STORE_NAME));
									medSyncData.setSupport_email_address(support_email_address);
									medSyncData.setSupport_phone_number(support_phone_number);
									medSyncData.setClient_name(client_name);
									medSyncData.setCustomer_firstname(customerDetails.get("first_name"));
									// object is set for creating entry in
									// send_bulk_reminders
									SendBulkReminders sendBulkReminders = new SendBulkReminders();
									sendBulkReminders.setClient_id(clientId);
									sendBulkReminders.setCustomer_id(customerId);
									// reminder hour from
									// customer_preference
									sendBulkReminders.setCustomer_reminder_send_hour(
										Constants.SINGLE_WHITESPACE + customerDetails.get(Constants.SEND_REMINDER_HOUR));
									sendBulkReminders.setCustomer_timezone(customerDetails.get(Constants.TIME_ZONE));
									sendBulkReminders.setCommunication_id(medSyncCommunication.get("id"));
									// [MCE - 2036] To disable text
									// messaging from all communications
									String isTextMessagingEnabled = ConfigReader.readConfig(clientId,
											Constants.DEFAULT_LANGUAGE, Constants.IS_TEXTMESSAGING_ENABLED);
									if (Constants.NUMERIC_TRUE_STRING.equals(isTextMessagingEnabled)) {
										sendBulkReminders.setMessage_text(
												FrameMessagesUtil.frameSMSMessages(medSyncCommunication, medSyncData));
									}
									sendBulkReminders.setEmail_text(
											FrameMessagesUtil.frameEmailMessages(medSyncCommunication, medSyncData));
									sendBulkReminders.setPush_text(
											FrameMessagesUtil.framePushMessages(medSyncCommunication, medSyncData));

									sendBulkReminders.setMessage_sent(Constants.NUMERIC_FALSE_STRING);

									Calendar soldDate = Calendar.getInstance();

									soldDate.setTime(formatterInput
											.parse(transactionData.get(0).getWillCallPickedUpDate().substring(0, 19)));
									soldDate.add(Calendar.DATE, Integer.parseInt(cvMedSyncNotificationBuffer));
									SimpleDateFormat dobFotmat = new SimpleDateFormat(cvDateTimeFormatDOB);
									String soldDateString = dobFotmat.format(soldDate.getTime());
									// create an entry into
									// send_bulk_reminders
									eopnUtils.updateOrInsertSendBulkReminders(sendBulkReminders, clientId,
											soldDateString, medSyncData.getStore_name(), otherMedSyncFormatDate,
											nextSyncFillDate);
								}
							}
						} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason,
										cvContactReasonCancelledOutOfWillCall)
								&& MscriptsStringUtils.compareStringEquals(cvIsCancelledOutOfWillCallMessageEnabled,
										Constants.NUMERIC_TRUE_STRING)) {

							contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
									ConfigKeys.CVRXCANCELLOUTOFWILLCALLSTATUS);

							eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);

							transactionStateMap = eopnUtils.getPatientRxTxn(patientDetailsMap);

							if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
								LOGGER_NON_PHI.info(
										"Record not available in  PATIENT_RX_TXN table, Inside Cancelled will call ready");

								eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										null, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
							}
							// update the record if already present
							else {
								LOGGER_NON_PHI.info(
										"Record available PATIENT_RX_TXN table, updating the record, Inside Cancelled will call ready");

								patientNotificationDao.insertOrUpdateJdbcData(
										QueryBuilder.UPDATE_PATIENT_RX_TXN_CHECKOUT_COMPLETE,
										new Object[] { contactReasonStatus, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_FALSE_STRING, customerId,
												patientDetailsMap.get(Constants.PRESCRIPTION_ID) });
							}

						} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
								&& MscriptsStringUtils.compareStringEquals(contactReason,
										cvContactReasonPartialFillOnHold)
								&& MscriptsStringUtils.compareStringEquals(cvIsPartialFillOnHoldMessageEnabled, Constants.NUMERIC_TRUE_STRING)) {
							contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
									ConfigKeys.CVTCOMMNAMEPARTIALFILLONHOLD);
							LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
							// Add in the status in patient_rx_txn
							// table if not already present
							if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
								LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);
								eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId,
										null, transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
							}
							// update the record if already present
							else {
								LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);
								eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
										transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled,
										nextSyncFillDate, iAutoFillenabled);
							}

							// Logic to avoid multiple messages with in a day
							countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
									patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
									cvtCommNamePartialFillOnHold);
							if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
									&& MscriptsStringUtils.compareStringEquals(
											countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
									&& MscriptsStringUtils
											.compareStringEquals(cvIsPartialFillOnHoldCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)) {

								boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(
										clientId, customerId, String.valueOf(NotificationTypeKey.rxpartialfillonhold),
										patientData, finalEncryptionKey, transactionData, patientDetailsMap);
								// Check whether user has text msg active and
								// verified = 1
								customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
										clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

								if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
										&& isRxTransactionMessageAllowedForStore && !bisDrugExcluded
										&& mscriptsCommonService
												.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
									/*
									 * MCE-714 If Rx is enrolled in sync script,
									 * we need to send the Rx Rejected message.
									 * So please do not block it here.
									 */

									// TD - check the time before sending
									// notification

									communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
											cvtCommNamePartialFillOnHold, customerId);
									userTimezone = customerPreference.get(Constants.TIME_ZONE);
									lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
											transactionData.get(0).getLastFilledDate());
									notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
									LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone,
											lastFilledDate);
									if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
											&& MscriptsStringUtils.compareStringEquals(
													notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
													Constants.TIME_BTW_CUTOFFS)) {
										LOGGER_NON_PHI.info(
												"Rx Partially filled on hold notification received during outbound communication period, Sending the reminder ");
										LOGGER_NON_PHI.info(
												"Deleting from BULK_RX_PICKUP_INSTANCES table as we received partial filled on hold state for the prescription");
										eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
										String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
												customerPreference.get(Constants.SHOW_RX_NAME), clientId);

										Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

										communicationContent.put(
												CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
												rxNameOrNumber);

										/*
										 * sending Rx partial filled on hold
										 * communication to user
										 * 
										 */
										eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
												patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
												communicationContent, cvtCommNamePartialFillOnHold, true);

									} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
											&& MscriptsStringUtils.compareStringEquals(
													notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
													Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
											&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
													customerId)) {
										LOGGER_NON_PHI.info(
												"Deleting from BULK_RX_PICKUP_INSTANCES table as we received partial filled on hold state for the prescription when time is less than lower cut off time");
										LOGGER_NON_PHI.info(
												"Partial filled on hold notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

										eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
												transactionData, pharmacyData, lastFilledDate, patientData,
												finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
												communicationIdMap);
									} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
											&& MscriptsStringUtils.compareStringEquals(
													notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
													Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
											&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
													customerId)) {
										LOGGER_NON_PHI.info(
												"Deleting from BULK_RX_PICKUP_INSTANCES table as we received partial filled on hold state for the prescription whem time is greater than upper cut off time");
										LOGGER_NON_PHI.info(
												"Rx Rejected notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
										eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
												transactionData, pharmacyData, lastFilledDate, patientData,
												finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
												communicationIdMap);
									}
								} else {
									LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table
								 * with reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {
							LOGGER_NON_PHI.info(
									"Already sent partial fill on hold message to the user for the day, Count = {}",
									countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap, cvIsPartialFillOnHoldCommunicationEnabled,
									patientData, finalEncryptionKey, transactionData, clientId, customerId);
						}
					} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonCentralFillDelayed)
							&& MscriptsStringUtils.compareStringEquals(cvIsCentralFillDelayedMessageEnabled,
									Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVTCOMMNAMECENTRALFILLDELAYED);
						LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
						// Add in the status in patient_rx_txn
						// table if not already present
						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);
							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);
							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}

						// Logic to avoid multiple messages with in a day
						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
								cvtCommNameCentralFillDelayed);
						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
								&& MscriptsStringUtils.compareStringEquals(
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								&& MscriptsStringUtils.compareStringEquals(cvIsCentralFillDelayedCommunicationEnabled,
										Constants.NUMERIC_TRUE_STRING)) {
							boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
									customerId, String.valueOf(NotificationTypeKey.rxcentralfilldelayed), patientData,
									finalEncryptionKey, transactionData, patientDetailsMap);
							// Check whether user has text msg active and
							// verified = 1
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
									clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !bisDrugExcluded
									&& mscriptsCommonService
											.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

								communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
										cvtCommNameCentralFillDelayed, customerId);
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx central fill delayed notification received during outbound communication period, Sending the reminder ");
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received central fill delayed state for the prescription");
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);

									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);

									/*
									 * sending Rx partial filled central fill delayed
									 * communication to user
									 * 
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameCentralFillDelayed, true);

								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received central fill delayed state for the prescription when time is less than lower cut off time");
									LOGGER_NON_PHI.info(
											"central fill delayed notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received central fill delayed state for the prescription whem time is greater than upper cut off time");
									LOGGER_NON_PHI.info(
											"central fill delayed notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with
								 * reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {
							LOGGER_NON_PHI.info(
									"Already sent central fill delayed message to the user for the day, Count = {}",
									countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap, cvIsCentralFillDelayedCommunicationEnabled,
									patientData, finalEncryptionKey, transactionData, clientId, customerId);
						}
					} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason,
									cvContactReasonThirdPartyException)
							&& MscriptsStringUtils.compareStringEquals(cvIsThirdPartyExceptionMessageEnabled,
									Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVTCOMMNAMETHIRDPARTYEXCEPTION);
						LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
						// Add in the status in patient_rx_txn
						// table if not already present
						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);
							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);
							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}

						// Logic to avoid multiple messages with in a day
						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
								cvtCommNameThirdPartyException);
						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
								&& MscriptsStringUtils.compareStringEquals(
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								&& MscriptsStringUtils.compareStringEquals(cvIsThirdPartyExceptionCommunicationEnabled,
										Constants.NUMERIC_TRUE_STRING)) {
							boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
									customerId, String.valueOf(NotificationTypeKey.rxthirdpartyexception), patientData,
									finalEncryptionKey, transactionData, patientDetailsMap);
							// Check whether user has text msg active and
							// verified = 1
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
									clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !bisDrugExcluded
									&& mscriptsCommonService
											.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

								communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
										cvtCommNameThirdPartyException, customerId);
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx third party exception notification received during outbound communication period, Sending the reminder ");
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received third party exception state for the prescription");
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);

									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);

									/*
									 * sending Rx partial filled on hold
									 * communication to user
									 * 
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameThirdPartyException, true);

								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received third party exception state for the prescription when time is less than lower cut off time");
									LOGGER_NON_PHI.info(
											"third party exception notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received third party exception state for the prescription whem time is greater than upper cut off time");
									LOGGER_NON_PHI.info(
											"third party exception notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with
								 * reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {
							LOGGER_NON_PHI.info(
									"Already sent third party exception message to the user for the day, Count = {}",
									countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap, cvIsThirdPartyExceptionCommunicationEnabled,
									patientData, finalEncryptionKey, transactionData, clientId, customerId);
						}
					} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonCallPrescriber)
							&& MscriptsStringUtils.compareStringEquals(cvIsCallPrescriberMessageEnabled,
									Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVTCOMMNAMECALLPRESCRIBER);
						LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
						// Add in the status in patient_rx_txn
						// table if not already present
						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);
							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);
							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}

						// Logic to avoid multiple messages with in a day
						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId, cvtCommNameCallPrescriber);
						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
								&& MscriptsStringUtils.compareStringEquals(
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								&& MscriptsStringUtils.compareStringEquals(cvIsCallPrescriberCommunicationEnabled,
										Constants.NUMERIC_TRUE_STRING)) {
							boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
									customerId, String.valueOf(NotificationTypeKey.rxcallprescriber), patientData,
									finalEncryptionKey, transactionData, patientDetailsMap);
							// Check whether user has text msg active and
							// verified = 1
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
									clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !bisDrugExcluded
									&& mscriptsCommonService
											.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

								communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
										cvtCommNameCallPrescriber, customerId);
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx Call prescriber exception notification received during outbound communication period, Sending the reminder ");
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Call prescriber state for the prescription");
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									String rxNameOrNumber = eopnUtils.checkRxnumberOrName(transactionData,
											customerPreference.get(Constants.SHOW_RX_NAME), clientId);

									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);

									communicationContent.put(CommunicationTemplateStrings.REFILL_RX.getTemplateString(),
											rxNameOrNumber);

									/*
									 * sending Rx partial filled on hold
									 * communication to user
									 * 
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameCallPrescriber, true);

								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Call prescriber state for the prescription when time is less than lower cut off time");
									LOGGER_NON_PHI.info(
											"Call prescriber notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received Call prescriber state for the prescription whem time is greater than upper cut off time");
									LOGGER_NON_PHI.info(
											"Call prescriber notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with
								 * reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {
							LOGGER_NON_PHI.info(
									"Already sent call prescriber message to the user for the day, Count = {}",
									countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap, cvIsCallPrescriberCommunicationEnabled,
									patientData, finalEncryptionKey, transactionData, clientId, customerId);
						}
					} else if (!MscriptsStringUtils.isStringEmptyOrNull(contactReason)
							&& MscriptsStringUtils.compareStringEquals(contactReason,
									cvContactReasonEPrescriptionReceived)
							&& MscriptsStringUtils.compareStringEquals(cvIsEPrescriptionReceivedMessageEnabled,
									Constants.NUMERIC_TRUE_STRING)) {
						contactReasonStatus = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED);
						LOGGER_NON_PHI.info(Constants.CHECKING_RECORD_IN_PATIENT_RX_TXN_TABLE_MSG);
						// Add in the status in patient_rx_txn
						// table if not already present
						if (MscriptsStringUtils.isMapEmptyOrNull(transactionStateMap)) {
							LOGGER_NON_PHI.info(Constants.RECORD_NOT_PRSNT_IN_PATIENT_RX_TXN_MSG);
							eopnUtils.insertIntoPatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}
						// update the record if already present
						else {
							LOGGER_NON_PHI.info(Constants.UPDATE_PATIENT_RX_TXN_TABLE_MSG);
							eopnUtils.updatePatientRxTxn(patientDetailsMap, contactReasonStatus, customerId, null,
									transactionData, dawCode, sTotalQuantity, isSyncScriptEnrolled, nextSyncFillDate,
									iAutoFillenabled);
						}

						// Logic to avoid multiple messages with in a day
						countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
								patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId,
								cvtCommNameEPrescriptionReceived);
						if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
								&& MscriptsStringUtils.compareStringEquals(
										countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)
								&& MscriptsStringUtils.compareStringEquals(
										cvIsEPrescriptionReceivedCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)) {
							boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
									customerId, String.valueOf(NotificationTypeKey.rxescriptreceived), patientData,
									finalEncryptionKey, transactionData, patientDetailsMap);
							// Check whether user has text msg active and
							// verified = 1
							customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
									clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& isRxTransactionMessageAllowedForStore && !bisDrugExcluded
									&& mscriptsCommonService
											.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

								communicationIdMap = eopnUtils.getCustomerCommunicationId(clientId,
										cvtCommNameEPrescriptionReceived, customerId);
								userTimezone = customerPreference.get(Constants.TIME_ZONE);
								lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
										transactionData.get(0).getLastFilledDate());
								notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
								LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
								if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
										.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_BTW_CUTOFFS)) {
									LOGGER_NON_PHI.info(
											"Rx E prescription received exception notification received during outbound communication period, Sending the reminder ");
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received E prescription received state for the prescription");
									eopnUtils.deleteBulkRxPickupInstances(patientDetailsMap, clientId);
									Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);


									/*
									 * sending Rx partial filled on hold
									 * communication to user
									 * 
									 */
									eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
											patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
											communicationContent, cvtCommNameEPrescriptionReceived, true);

								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_LESS_THATN_LOWER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received E prescription received state for the prescription when time is less than lower cut off time");
									LOGGER_NON_PHI.info(
											"E prescription received notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time ");

									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								} else if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap)
										&& MscriptsStringUtils.compareStringEquals(
												notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
												Constants.TIME_GREATER_THAN_UPPER_CUT_OFF)
										&& mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId,
												customerId)) {
									LOGGER_NON_PHI.info(
											"Deleting from BULK_RX_PICKUP_INSTANCES table as we received E prescription received state for the prescription whem time is greater than upper cut off time");
									LOGGER_NON_PHI.info(
											"E prescription received notification received during no outbound communication period, reminder will be sent via Job Scheduler. Time = lowerCutOff Time + 1 day ");
									eopnUtils.deleteAndInsertIntoBulkRxPickupInstances(patientDetailsMap, clientId,
											transactionData, pharmacyData, lastFilledDate, patientData,
											finalEncryptionKey, notificationTimeMap, customerId, customerPreference,
											communicationIdMap);
								}
							} else {
								LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
								/*
								 * Logging blocked notification in
								 * ext_req_handler_blocked_details table with
								 * reason
								 */
								eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
										isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
										transactionData);
							}

						} else {
							LOGGER_NON_PHI.info(
									"Already sent e prescription received message to the user for the day, Count = {}",
									countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
							eopnUtils.logDuplicateMessage(countMessagesMap,
									cvIsEPrescriptionReceivedCommunicationEnabled, patientData, finalEncryptionKey,
									transactionData, clientId, customerId);
						}
					}
				}
			}
			patientNotificationResponse.setStatus(
					!bReprocessPatientNotification ? Constants.RESPONSE_SUCCESS : Constants.RESPONSE_FAILURE);
			patientNotificationResponse.setHttpStatusCode(!bReprocessPatientNotification
					? Constants.HTTP_STATUS_CODE_SUCCESS : Constants.HTTP_STATUS_CODE_FAILURE);
			eopnUtils.insertIntoExternalRequestHandlerAuditsWSO2(clientId, patientData, finalEncryptionKey,
					requestString, Constants.HTTP_STATUS_CODE_SUCCESS, patientNotificationResponse, customerId);
		} catch (JAXBException eJax) {
			LOGGER_NON_PHI.error("Error while mapping XML to java object {}", eJax.getMessage());
			patientNotificationResponse.setStatus(
					!bReprocessPatientNotification ? Constants.RESPONSE_SUCCESS : Constants.RESPONSE_FAILURE);
			patientNotificationResponse.setHttpStatusCode(!bReprocessPatientNotification
					? Constants.HTTP_STATUS_CODE_SUCCESS : Constants.HTTP_STATUS_CODE_FAILURE);
			patientNotificationResponse.setError(eJax.getMessage());
			patientNotificationResponse.setErrorCode(eJax.getErrorCode());
			throw new MscriptsException(clientId, eJax.getMessage(), ErrorCodes.GENERIC_ERROR, eJax, errorSeverity);

		} catch (MscriptsException mEx) {
			LOGGER_NON_PHI.error(mEx.getErrorMessage());

			patientNotificationResponse.setStatus(
					!bReprocessPatientNotification ? Constants.RESPONSE_SUCCESS : Constants.RESPONSE_FAILURE);
			patientNotificationResponse.setHttpStatusCode(!bReprocessPatientNotification
					? Constants.HTTP_STATUS_CODE_SUCCESS : Constants.HTTP_STATUS_CODE_FAILURE);
			patientNotificationResponse.setError(mEx.getMessage());
			patientNotificationResponse.setErrorCode(mEx.getErrorCode());
			patientNotificationResponse.setErrorMessage(mEx.getErrorMessage());

			try {
				String responseStatus = !bReprocessPatientNotification ? Constants.HTTP_STATUS_CODE_SUCCESS
						: Constants.HTTP_STATUS_CODE_FAILURE;
				eopnUtils.insertIntoExternalRequestHandlerAuditsWSO2(clientId, patientData, finalEncryptionKey,
						requestString, responseStatus, patientNotificationResponse, customerId);
			} catch (Exception e) {
				throw new MscriptsException(clientId, e.getMessage(), ErrorCodes.GENERIC_ERROR, e, errorSeverity);
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Generic Error Message {}", e.getMessage());
			try {
				eopnUtils.insertIntoExternalRequestHandlerAuditsWSO2(clientId, patientData, finalEncryptionKey,
						requestString, Constants.HTTP_STATUS_CODE_FAILURE, patientNotificationResponse, customerId);
			} catch (Exception e1) {
				throw new MscriptsException(clientId, e.getMessage(), ErrorCodes.GENERIC_ERROR, e, errorSeverity);
			}
		}
		return patientNotificationResponse;
	}

	/**
	 * To check test delivery feature is enabled for the specific client and
	 * prescription
	 * 
	 * @param clientId
	 * @param customerPrescriptionId
	 * @param transactionData 
	 * @return boolean
	 */
	private boolean checkIfTextDeliveryEnabled(String clientId, String customerPrescriptionId, TransactionData transactionData) {
		boolean isTextDeliveryAllowed = false;
		try {
			String cvIsDeliveryAllowed = ConfigReader.readConfig(clientId, Constants.LANG_CODE,
					ConfigKeys.CVISDELIVERYALLOWED);
			if (Constants.NUMERIC_TRUE_STRING.equalsIgnoreCase(cvIsDeliveryAllowed)) {

				Map<String, String> customerPrescriptionDetailsMap = patientNotificationDao.jdbcDataGet(
						QueryBuilder.GET_CUSTOMER_PRESCRIPTION_DETAILS,
						new Object[] { clientId, customerPrescriptionId });

				String latestRefillStatus = customerPrescriptionDetailsMap.get("latest_refill_status");
				if (Constants.READY_STRING.equalsIgnoreCase(latestRefillStatus)) {
					isTextDeliveryAllowed = checkForDrugexclusionValue(clientId, customerPrescriptionDetailsMap,transactionData);
				}

			}
			return isTextDeliveryAllowed;

		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured while processing checkIfTextDeliveryEnabled request:{}",
					ex);
			// Default isTextDeliveryAllowed -false will be returned If any
			// MscriptsException thrown from try block to retain normal flow
			return isTextDeliveryAllowed;
		}
	}


	/**
	 * To map drug filter criteria value with the prescription parameters to check
	 * whether drug is excluded or not
	 * 
	 * @param clientId
	 * @param customerPrescriptionDetailsMap
	 * @param transactionData 
	 * @return boolean value isTextDeliveryAllowed
	 * @throws MscriptsException
	 */
	private boolean checkForDrugexclusionValue(String clientId, Map<String, String> customerPrescriptionDetailsMap, TransactionData transactionData)
			throws MscriptsException {
		boolean isTextDeliveryAllowed = false;
		String origNCPDPId = customerPrescriptionDetailsMap.get("store_ncpdp_id");
		String drugNDC = customerPrescriptionDetailsMap.get("presc_drug_ndc");
		String drugGPI = customerPrescriptionDetailsMap.get("presc_drug_gpi");
		String schedule = customerPrescriptionDetailsMap.get("sched");
		// Get text delivery enabled delivery detail id's
		List<Map<String, String>> textDeliveryEnabledVendorsList = mscriptsCommonDao
				.getTextDeliveryEnabledVendors(clientId);
		for (Map<String, String> textDeliveryEnabledVendor : textDeliveryEnabledVendorsList) {
			Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
			drugFilterCriteriasMap.put(Constants.DELIVERY_DETAIL_ID,
					textDeliveryEnabledVendor.get(Constants.DELIVERY_DETAIL_ID));
			
			String fillNumber = null;
			if (transactionData != null && !MscriptsStringUtils.isStringEmptyOrNull(transactionData.getRefillNum())) {
				fillNumber = transactionData.getRefillNum();
			} else if (customerPrescriptionDetailsMap.get("ref_aut") != null
					&& customerPrescriptionDetailsMap.get("ref_rem") != null && customerPrescriptionDetailsMap
							.get("ref_aut").equalsIgnoreCase(customerPrescriptionDetailsMap.get("ref_rem"))) {
				fillNumber = Constants.FILL_NUMBER_ZERO;
			}
			
			boolean fillNumberExcluded = false;
			if (fillNumber != null && drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.FILLNUMBER,
					fillNumber, drugFilterCriteriasMap)) {
				fillNumberExcluded = true;
			}

			boolean isDrugExcluded = (!fillNumberExcluded && (drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.NCPDPID,
					origNCPDPId, drugFilterCriteriasMap))
					&& !(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.NDC, drugNDC,
							drugFilterCriteriasMap))
					&& !(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.SCHEDULE, schedule,
							drugFilterCriteriasMap))
					&& !(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI, drugGPI,
							drugFilterCriteriasMap)));
			if (isDrugExcluded) {
				isTextDeliveryAllowed = true;
				break;
			}
		}
		return isTextDeliveryAllowed;
	}
	
	/**
	 * Method to send a generic message when the Rx details are absent in the EOPN request
	 * @param communicationTemplateName
	 * @param patientDetailsMap
	 * @param pharmacyData
	 * @param patientData
	 * @param transactionData
	 * @param notificationTypekey
	 * @param langCode
	 * @throws MscriptsException
	 */
	private void sendGenericMessage(String communicationTemplateName, Map<String,String> patientDetailsMap, List<PharmacyData> pharmacyData,
			List<PatientData> patientData,List<TransactionData> transactionData, NotificationTypeKey notificationTypekey, String langCode) throws MscriptsException {
			
			Map<String,String> customerPreference = null;
			String userTimezone = null;
			String lastFilledDate = null;
			Map<String, String> notificationTimeMap = null;
			String clientId = null;
			String customerId = null;
			String finalEncryptionKey = null;
			Map<String, String> countMessagesMap = null;
			boolean isRxTransactionMessageAllowedForStore = true;
			String cvIsStoreExceptionEnabled = null;
			boolean isSyncScriptEnrolled = false;
			boolean patientSyncScriptEnrolled = false;
			mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
			
			try {

				LOGGER_NON_PHI.info("Inside PatientNotificationServiceImpl.sendGenericMessage() method");

				clientId = patientDetailsMap.get(Constants.CLIENT_ID);
				customerId = patientDetailsMap.get(Constants.CUSTOMER_ID_DB_STRING);
				finalEncryptionKey = patientDetailsMap.get(Constants.ENCRYPTION_KEY_STRING);

				String cvOtherReminderMappingID = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVOTHERREMINDERMAPPINGID);

				String communicationName = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						communicationTemplateName);
				if (!MscriptsStringUtils.isStringEmptyOrNull(communicationName)) {
					
					// Check if the message with same template was sent within the specified time. If yes then the message is blocked.
					countMessagesMap = eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHoursByCustomerId(
							customerId, clientId, communicationName);
					if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
							&& MscriptsStringUtils.compareStringEquals(
									countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY), Constants.NUMERIC_FALSE_STRING)) {

						// Check whether user has text msg active and
						// verified = 1
						customerPreference = eopnUtils.getCustomerPreference(finalEncryptionKey, customerId,
								clientId, cvOtherReminderMappingID, pharmacyData.get(0).getPharmacyNCPDP());

						// Check if the customer had disabled the notifications
						boolean bisDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(clientId,
								customerId, String.valueOf(notificationTypekey), patientData,
								finalEncryptionKey, transactionData, patientDetailsMap);
						
						cvIsStoreExceptionEnabled = ConfigReader.readConfig(clientId, langCode,
								ConfigKeys.CVISSTOREEXCEPTIONENABLED);
						
						if (MscriptsStringUtils.compareStringEquals(Constants.NUMERIC_TRUE_STRING, cvIsStoreExceptionEnabled)) {
							LOGGER_NON_PHI.info("Exception of reminder based on store is enabled, Getting list of stores");
							isRxTransactionMessageAllowedForStore = eopnUtils.checkStoreException(clientId,
									pharmacyData.get(0).getPharmacyNCPDP());
						}
						
						if (!MscriptsStringUtils.isStringEmptyOrNull(transactionData.get(0).getSyncScriptEnrolled())) {
							isSyncScriptEnrolled = transactionData.get(0).getSyncScriptEnrolled().equalsIgnoreCase("Y");
						}
						
						if (!MscriptsStringUtils.isStringEmptyOrNull(patientData.get(0).getSyncScriptEnrolled())) {
							patientSyncScriptEnrolled = patientData.get(0).getSyncScriptEnrolled().equalsIgnoreCase("E");
						}

						// MCE-1325 fixes related to med sync
						if (!patientSyncScriptEnrolled) {
							isSyncScriptEnrolled = false;
						}
						
						if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
								&& !bisDrugExcluded
								&& isRxTransactionMessageAllowedForStore
								&& mscriptsCommonService
								.isPatientCommunicationConsentSatisfied(clientId, customerId)) {

							userTimezone = customerPreference.get(Constants.TIME_ZONE);
							lastFilledDate = eopnUtils.getLastFilledDate(userTimezone,
									transactionData.get(0).getLastFilledDate());

							// Check if the send time is within the allowed window
							notificationTimeMap = eopnUtils.checkNotificationTime(clientId, userTimezone);
							LOGGER_PHI.info(Constants.LOG_FOR_USERTIMEZONE_LASTFILLEDDATE, userTimezone, lastFilledDate);
							if (!MscriptsStringUtils.isMapEmptyOrNull(notificationTimeMap) && MscriptsStringUtils
									.compareStringEquals(notificationTimeMap.get(Constants.SEND_MESSAGE_STATUS),
											Constants.TIME_BTW_CUTOFFS)) {
								LOGGER_NON_PHI.info(
										"PatientNotificationServiceImpl.sendGenericMessage(): Notification received during outbound communication period, Sending the reminder");

								Map<String, String> communicationContent =  eopnUtils.prepareCommonCommunicationContent(customerPreference, patientData);


								eopnUtils.sendEOPNCommunicationToUser(clientId, customerId, transactionData,
										patientDetailsMap, patientData, finalEncryptionKey, customerPreference,
										communicationContent, communicationName, true);
								LOGGER_NON_PHI.info("PatientNotificationServiceImpl.sendGenericMessage(): Message is sent to the user");
							}


						}
						else {
							LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
							/*
							 * Logging blocked notification in
							 * ext_req_handler_blocked_details table with
							 * reason
							 */
							eopnUtils.logBlockedNotification(isRxTransactionMessageAllowedForStore,
									isSyncScriptEnrolled, clientId, customerId, patientData, finalEncryptionKey,
									transactionData);
						}

					} else {
						LOGGER_NON_PHI.info(
								"Already sent third party exception message to the user for the day, Count = {}",
								countMessagesMap.get(Constants.VARPICKUPCOUNTFORTODAY));
						eopnUtils.logDuplicateMessage(countMessagesMap, patientDetailsMap.get(Constants.IS_COMMUNICATION_ENABLED),
								patientData, finalEncryptionKey, transactionData, clientId, customerId);
					}


				}
			} catch (MscriptsException mEx) {
				LOGGER_NON_PHI.error("Error occured within PatientNotificationServiceImpl.sendGenericMessage(): {}",mEx);
				throw new MscriptsException(clientId, mEx.getMessage(), ErrorCodes.GENERIC_ERROR, mEx, errorSeverity);
			}
			catch (Exception ex) {
				LOGGER_NON_PHI.error("Error occured within PatientNotificationServiceImpl.sendGenericMessage(): {}",ex);
				throw new MscriptsException(clientId, ex.getMessage(), ErrorCodes.GENERIC_ERROR, ex, errorSeverity);
			}
	}
}