package com.mscripts.externalrequesthandler.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.MscriptsCommonDao;
import com.mscripts.dao.QueryInvoker;
import com.mscripts.domain.SendCommunicationMode;
import com.mscripts.enums.CommunicationTemplateStrings;
import com.mscripts.enums.PdxContactReason;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.NotificationDao;
import com.mscripts.externalrequesthandler.dao.PatientNotificationDao;
import com.mscripts.externalrequesthandler.domain.CreditCard;
import com.mscripts.externalrequesthandler.domain.PatientData;
import com.mscripts.externalrequesthandler.domain.PatientNotificationRequest;
import com.mscripts.externalrequesthandler.domain.PatientNotificationResponse;
import com.mscripts.externalrequesthandler.domain.PharmacyData;
import com.mscripts.externalrequesthandler.domain.SendBulkReminders;
import com.mscripts.externalrequesthandler.domain.TransactionData;
import com.mscripts.externalrequesthandler.service.PatientNotificationServiceImpl;
import com.mscripts.service.APNSService;
import com.mscripts.service.FCMService;
import com.mscripts.service.GCMSService;
import com.mscripts.service.IvrService;
import com.mscripts.service.MailService;
import com.mscripts.service.MscriptsCommonService;
import com.mscripts.service.SmsService;
import com.mscripts.service.URLShortner;
import com.mscripts.txndata.domain.DawCode;
import com.mscripts.txndata.service.PatientLoyaltyCardService;
import com.mscripts.txndata.service.PatientPaymentMethodService;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.GenerateRandom;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.MscriptsStringUtils;
import com.mscripts.utils.NotificationFilterUtil;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.QueryBuilder;
import com.mscripts.utils.TextMaskingService;
import com.mscripts.utils.mscriptsExceptionSeverity;

public class EOPNUtils {

	private PatientNotificationDao patientNotificationDao;
	private MscriptsCommonService mscriptsCommonService;
	protected PHICredentials pHICredentials;
	private SmsService smsService;
	private APNSService apnsService;
	private GCMSService gcmsService;
	private MailService mailService;
	private MscriptsCommonDao mscriptsCommonDao;
	private IvrService ivrService;
	private FCMService fcmService;
	private NotificationFilterUtil notificationFilterUtil;
	private QueryInvoker queryInvoker;
	private PatientPaymentMethodService patientPaymentMethodService;
	private TextMaskingService textMaskingService;
	private PatientLoyaltyCardService patientLoyaltyCardService;
	private NotificationDao notificationDao;
	private URLShortner urlShortnerService;
	

	public static final String USER_NOT_ELIGIBLE_TEXT_MSG = "User is NOT Eligible to receive Text message";
	public static final String USER_NOT_ELIGIBLE_IVR_MSG = "User is Not Eligible to receive ivr message";
	
	
	
	public NotificationDao getNotificationDao() {
		return notificationDao;
	}

	public void setNotificationDao(NotificationDao notificationDao) {
		this.notificationDao = notificationDao;
	}

	public URLShortner getUrlShortnerService() {
		return urlShortnerService;
	}

	public void setUrlShortnerService(URLShortner urlShortnerService) {
		this.urlShortnerService = urlShortnerService;
	}

	public QueryInvoker getQueryInvoker() {
		return queryInvoker;
	}

	public void setQueryInvoker(QueryInvoker queryInvoker) {
		this.queryInvoker = queryInvoker;
	}

	public PatientPaymentMethodService getPatientPaymentMethodService() {
		return patientPaymentMethodService;
	}

	public void setPatientPaymentMethodService(PatientPaymentMethodService patientPaymentMethodService) {
		this.patientPaymentMethodService = patientPaymentMethodService;
	}

	public TextMaskingService getTextMaskingService() {
		return textMaskingService;
	}

	public void setTextMaskingService(TextMaskingService textMaskingService) {
		this.textMaskingService = textMaskingService;
	}

	public PatientLoyaltyCardService getPatientLoyaltyCardService() {
		return patientLoyaltyCardService;
	}

	public void setPatientLoyaltyCardService(PatientLoyaltyCardService patientLoyaltyCardService) {
		this.patientLoyaltyCardService = patientLoyaltyCardService;
	}

	public NotificationFilterUtil getNotificationFilterUtil() {
		return notificationFilterUtil;
	}

	public void setNotificationFilterUtil(NotificationFilterUtil notificationFilterUtil) {
		this.notificationFilterUtil = notificationFilterUtil;
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

	public MscriptsCommonDao getMscriptsCommonDao() {
		return mscriptsCommonDao;
	}

	public void setMscriptsCommonDao(MscriptsCommonDao mscriptsCommonDao) {
		this.mscriptsCommonDao = mscriptsCommonDao;
	}

	public IvrService getIvrService() {
		return ivrService;
	}

	public void setIvrService(IvrService ivrService) {
		this.ivrService = ivrService;
	}

	public FCMService getFcmService() {
		return fcmService;
	}

	public void setFcmService(FCMService fcmService) {
		this.fcmService = fcmService;
	}

	public PHICredentials getpHICredentials() {
		return pHICredentials;
	}

	public void setpHICredentials(PHICredentials pHICredentials) {
		this.pHICredentials = pHICredentials;
	}

	public MscriptsCommonService getMscriptsCommonService() {
		return mscriptsCommonService;
	}

	public void setMscriptsCommonService(MscriptsCommonService mscriptsCommonService) {
		this.mscriptsCommonService = mscriptsCommonService;
	}

	public PatientNotificationDao getPatientNotificationDao() {
		return patientNotificationDao;
	}

	public void setPatientNotificationDao(PatientNotificationDao patientNotificationDao) {
		this.patientNotificationDao = patientNotificationDao;
	}

	private static final Logger LOGGER_NON_PHI = LogManager
			.getLogger("non.phi." + PatientNotificationServiceImpl.class.getName());
	private static final Logger LOGGER_PHI = LogManager
			.getLogger("phi." + PatientNotificationServiceImpl.class.getName());

	/**
	 * This function is used to get encryption key map
	 * 
	 * @param clientId
	 * @param secondaryKey
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getFinalKey(String clientId, String secondaryKey) throws MscriptsException {
		Map<String, String> encryptionMap = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			encryptionMap = patientNotificationDao.jdbcDataGet(QueryBuilder.FN_GET_FINAL_KEY,
					new Object[] { clientId, secondaryKey });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting final key {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return encryptionMap;
	}

	/**
	 * This function is used to get customer preference
	 * 
	 * @param finalEncryptionKey
	 * @param customerId
	 * @param clientId
	 * @param reminderDeliveryMode
	 * @param storeNcpdpId
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getCustomerPreference(String finalEncryptionKey, String customerId, String clientId,
			String reminderDeliveryMode, String storeNcpdpId) throws MscriptsException {
		Map<String, String> customerPreference = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			customerPreference = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_CUSTOMER_PREFERENCE, new Object[] {
					finalEncryptionKey, finalEncryptionKey, customerId, clientId, reminderDeliveryMode, storeNcpdpId });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting customer preference {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return customerPreference;
	}

	/**
	 * This function is to set send communication mode
	 * 
	 * @param reminderMode
	 * @param customerTextPreference
	 * @param customerId
	 * @return
	 */
	public SendCommunicationMode setSendCommunicationMode(String reminderMode,
			Map<String, String> customerTextPreference, String customerId) {
		SendCommunicationMode sendCommunicationMode = new SendCommunicationMode();
		sendCommunicationMode.setSendSms(Constants.NUMERIC_FALSE_STRING);
		sendCommunicationMode.setSendEmail(Constants.NUMERIC_FALSE_STRING);
		sendCommunicationMode.setSendGcms(Constants.NUMERIC_FALSE_STRING);
		sendCommunicationMode.setSendApns(Constants.NUMERIC_FALSE_STRING);
		sendCommunicationMode.setSendIvr(Constants.NUMERIC_FALSE_STRING);
		switch (reminderMode) {
		case Constants.TEXT:
			if (MscriptsStringUtils.compareStringEquals(customerTextPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
					Constants.NUMERIC_TRUE_STRING)
					&& MscriptsStringUtils.compareStringEquals(
							customerTextPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE),
							Constants.NUMERIC_TRUE_STRING)) {
				sendCommunicationMode.setSendSms(Constants.NUMERIC_TRUE_STRING);
			}
			break;
		case Constants.EMAIL:
			if (MscriptsStringUtils.compareStringEquals(customerTextPreference.get(Constants.EMAIL_VERIFIED),
					Constants.NUMERIC_TRUE_STRING)) {
				sendCommunicationMode.setSendEmail(Constants.NUMERIC_TRUE_STRING);
			}
			break;
		case Constants.IOS_PUSH:
			sendCommunicationMode.setSendApns(Constants.NUMERIC_TRUE_STRING);
			break;
		case Constants.ANDROID_PUSH:
			sendCommunicationMode.setSendGcms(Constants.NUMERIC_TRUE_STRING);
			break;
		case Constants.IVR:
			if (MscriptsStringUtils.compareStringEquals(customerTextPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
					Constants.NUMERIC_TRUE_STRING)) {
				sendCommunicationMode.setSendIvr(Constants.NUMERIC_TRUE_STRING);
			}
			break;
		default:
			LOGGER_NON_PHI.info("Reminder mode not set for customer {}", customerId);
		}

		return sendCommunicationMode;
	}

	/**
	 * This function is used to get customer communication id
	 * 
	 * @param clientId
	 * @param communicationName
	 * @param customerId
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getCustomerCommunicationId(String clientId, String communicationName, String customerId)
			throws MscriptsException {
		Map<String, String> customerCommunicationId = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {
			customerCommunicationId = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_CUST_COMMUNICATION_ID,
					new Object[] { clientId, communicationName, customerId });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting cusotmer communication ID {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return customerCommunicationId;
	}

	/**
	 * This function is used to log blocked notification
	 * 
	 * @param isRxTransactionMessageAllowedForStore
	 * @param isSyncScriptEnrolled
	 * @param clientId
	 * @param customerId
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param transactionData
	 * @throws MscriptsException
	 */
	public void logBlockedNotification(boolean isRxTransactionMessageAllowedForStore, boolean isSyncScriptEnrolled,
			String clientId, String customerId, List<PatientData> patientData, String finalEncryptionKey,
			List<TransactionData> transactionData) throws MscriptsException {
		String langCode = Constants.LANG_CODE;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			if (!isRxTransactionMessageAllowedForStore) {
				String cvBlockTypeStoreNotAllowed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKTYPESTORENOTALLOWED);
				String cvBlockReasonStoreNotAllowed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKREASONSTORENOTALLOWED);
				insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData, finalEncryptionKey,
						transactionData, cvBlockTypeStoreNotAllowed, cvBlockReasonStoreNotAllowed);
			}
			if (isSyncScriptEnrolled) {
				String cvBlockTypeSyncScript = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKTYPESYNCSCRIPT);
				String cvBlockReasonSyncScript = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKREASONSYNCSCRIPT);
				insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData, finalEncryptionKey,
						transactionData, cvBlockTypeSyncScript, cvBlockReasonSyncScript);
			}
			if (!mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
				String cvBlockTypeConsentNotGiven = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKTYPECONSENTNOTGIVEN);
				String cvBlockReasonConsentNotGiven = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKREASONCONSENTNOTGIVEN);
				insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData, finalEncryptionKey,
						transactionData, cvBlockTypeConsentNotGiven, cvBlockReasonConsentNotGiven);
			}
		} catch (Exception e) {

			LOGGER_NON_PHI.info(
					"Exception while logging blocked notification dateails in ext_req_handler_blocked_details table");
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to log duplicate EOPN notification
	 * 
	 * @param countMessagesMap
	 * @param isCommunicationEnabled
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param transactionData
	 * @param clientId
	 * @param customerId
	 * @throws MscriptsException
	 */
	public void logDuplicateMessage(Map<String, String> countMessagesMap, String isCommunicationEnabled,
			List<PatientData> patientData, String finalEncryptionKey, List<TransactionData> transactionData,
			String clientId, String customerId) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		if (!MscriptsStringUtils.isMapEmptyOrNull(countMessagesMap)
				&& !MscriptsStringUtils.compareStringEquals(countMessagesMap.get("varPickupCountForToday"),
						Constants.NUMERIC_FALSE_STRING)
				&& MscriptsStringUtils.compareStringEquals(isCommunicationEnabled, Constants.NUMERIC_TRUE_STRING)) {
			try {
				String langCode = Constants.LANG_CODE;
				String cvBlockTypeMaxMessageCount = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKTYPEMAXMESSAGECOUNT);
				String cvBlockReasonMaxMessageCount = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVBLOCKREASONMAXMESSAGECOUNT);
				insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData, finalEncryptionKey,
						transactionData, cvBlockTypeMaxMessageCount, cvBlockReasonMaxMessageCount);
			} catch (Exception e) {
				LOGGER_NON_PHI.info(
						"Exception while logging duplicated notification dateails in ext_req_handler_blocked_details table");
				throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
			}
		}
	}

	/**
	 * This function is used to delete anc insert data into bulk rx pick up
	 * instance table when notification is recieved outside business hours
	 * 
	 * @param patientDetailsMap
	 * @param clientId
	 * @param transactionData
	 * @param pharmacyData
	 * @param lastFilledDate
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param notificationTimeMap
	 * @param customerId
	 * @param customerPreference
	 * @param communicationIdMap
	 * @throws MscriptsException
	 */
	public void deleteAndInsertIntoBulkRxPickupInstances(Map<String, String> patientDetailsMap, String clientId,
			List<TransactionData> transactionData, List<PharmacyData> pharmacyData, String lastFilledDate,
			List<PatientData> patientData, String finalEncryptionKey, Map<String, String> notificationTimeMap,
			String customerId, Map<String, String> customerPreference, Map<String, String> communicationIdMap)
			throws MscriptsException {
		LOGGER_NON_PHI.info("Deleting from BULK_RX_PICKUP_INSTANCES table");
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String langCode = Constants.LANG_CODE;
			String cvtRefillStatusRequested = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTREFILLSTATUSREQUESTED);
			deleteBulkRxPickupInstances(patientDetailsMap, clientId);
			// Send the reminder next day
			LOGGER_NON_PHI.info(
					"notification received during no outbound communication period, reminder will be sent via Job Scheduler.");

			Map<String, Map<String, String>> consolidatedPatientDataMap = new HashMap<String, Map<String, String>>();
			consolidatedPatientDataMap.put("patientDetailsMap", patientDetailsMap);
			consolidatedPatientDataMap.put("notificationTimeMap", notificationTimeMap);
			consolidatedPatientDataMap.put("customerPreference", customerPreference);
			consolidatedPatientDataMap.put("communicationIdMap", communicationIdMap);
			/*
			 * call method to insert into bulk rx pickup table to be send by JOB
			 * scheduler
			 */

			insertBulkRxPickup(clientId, transactionData, pharmacyData, lastFilledDate, cvtRefillStatusRequested,
					patientData, finalEncryptionKey, consolidatedPatientDataMap);
		} catch (Exception e) {
			LOGGER_NON_PHI.info("Exception while deleting/inserting bulk rx pickup instances");
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}

	}

	/**
	 * This method is to insert entry into bulk_rx-pickup_instances to send EOPN
	 * communications to customers based on their preferences during their
	 * blocking hours
	 * 
	 * @param clientId
	 * @param transactionData
	 * @param pharmacyData
	 * @param lastFilledDate
	 * @param refillStatus
	 * @param patientDetailsMap
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param notificationTimeMap
	 * @param customerId
	 * @param customerPreference
	 * @param communicationIdMap
	 * @throws MscriptsException
	 */
	public void insertBulkRxPickup(String clientId, List<TransactionData> transactionData,
			List<PharmacyData> pharmacyData, String lastFilledDate, String refillStatus, List<PatientData> patientData,
			String finalEncryptionKey, Map<String, Map<String, String>> consolidatedPatientDataMap)
			throws MscriptsException {
		SendCommunicationMode sendCommunicationMode = new SendCommunicationMode();
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		String langCode = Constants.LANG_CODE;
		try {
			String cvBlockTypeMobileNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKTYPEMOBILENOTVERIFIED);
			String cvBlockReasonMobileNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKREASONMOBILENOTVERIFIED);
			String cvBlockTypeTextNotActive = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKTYPETEXTNOTACTIVE);
			String cvBlockReasonTextNotActive = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKREASONTEXTNOTACTIVE);
			String cvBlockTypeEmailNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKTYPEEMAILNOTVERIFIED);
			String cvBlockReasonEmailNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKREASONEMAILNOTVERIFIED);
			sendCommunicationMode.setSendSms(Constants.NUMERIC_FALSE_STRING);
			sendCommunicationMode.setSendGcms(Constants.NUMERIC_FALSE_STRING);
			sendCommunicationMode.setSendApns(Constants.NUMERIC_FALSE_STRING);
			sendCommunicationMode.setSendEmail(Constants.NUMERIC_FALSE_STRING);
			sendCommunicationMode.setSendIvr(Constants.NUMERIC_FALSE_STRING);

			// [MCE - 2036] To disable text messaging for all communications
			String cvBlockReasonClientTextNotActive = ConfigReader.readConfig(clientId, langCode,
					Constants.CV_BLOCK_REASON_CLIENT_TEXT_NOT_ACTIVE);
			String isTextMessagingEnabled = ConfigReader.readConfig(clientId, langCode,
					Constants.IS_TEXTMESSAGING_ENABLED);
			Map<String, String> patientDetailsMap = consolidatedPatientDataMap.get("patientDetailsMap");
			Map<String, String> notificationTimeMap = consolidatedPatientDataMap.get("notificationTimeMap");
			Map<String, String> customerPreference = consolidatedPatientDataMap.get("customerPreference");
			Map<String, String> communicationIdMap = consolidatedPatientDataMap.get("communicationIdMap");
			String customerId = patientDetailsMap.get("customer_id");
			if (mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
				if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
						&& customerPreference.get(Constants.SEND_REMINDER_MODE) != null) {
					switch (customerPreference.get(Constants.SEND_REMINDER_MODE)) {
					case Constants.TEXT:
						// [MCE - 2036] To disable text messaging for all
						// communications
						if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED)
								&& null != customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE)
								&& MscriptsStringUtils.compareStringEquals(
										customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
										Constants.NUMERIC_TRUE_STRING)
								&& MscriptsStringUtils.compareStringEquals(
										customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE),
										Constants.NUMERIC_TRUE_STRING)
								&& MscriptsStringUtils.compareStringEquals(isTextMessagingEnabled,
										Constants.NUMERIC_TRUE_STRING)) {

							sendCommunicationMode.setSendSms(Constants.NUMERIC_TRUE_STRING);
						} else {
							LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
							// [MCE - 2036] To disable text messaging for all
							// communications
							String blockType = Constants.EMPTY_STRING;
							String blockReason = Constants.EMPTY_STRING;
							if (MscriptsStringUtils.compareStringEquals(isTextMessagingEnabled,
									Constants.NUMERIC_FALSE_STRING)) {
								blockType = cvBlockTypeTextNotActive;
								blockReason = cvBlockReasonClientTextNotActive;

							} else if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED)
									&& !MscriptsStringUtils.compareStringEquals(
											customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
											Constants.NUMERIC_TRUE_STRING)) {
								blockType = cvBlockTypeMobileNotVerified;
								blockReason = cvBlockReasonMobileNotVerified;
							} else if (null != customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE)
									&& !MscriptsStringUtils.compareStringEquals(
											customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE),
											Constants.NUMERIC_TRUE_STRING)) {
								blockType = cvBlockTypeTextNotActive;
								blockReason = cvBlockReasonTextNotActive;
							}

							insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
									finalEncryptionKey, transactionData, blockType, blockReason);
						}

						break;
					case Constants.ANDROID_PUSH:
						sendCommunicationMode.setSendGcms(Constants.NUMERIC_TRUE_STRING);
						break;
					case Constants.IOS_PUSH:
						sendCommunicationMode.setSendApns(Constants.NUMERIC_TRUE_STRING);
						break;
					case Constants.EMAIL:
						if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
								&& customerPreference.get(Constants.EMAIL_VERIFIED) != null && customerPreference
										.get(Constants.EMAIL_VERIFIED).equals(Constants.NUMERIC_TRUE_STRING)) {
							sendCommunicationMode.setSendEmail(Constants.NUMERIC_TRUE_STRING);

						} else {
							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& customerPreference.get(Constants.EMAIL_VERIFIED) != null) {
								insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
										finalEncryptionKey, transactionData, cvBlockTypeEmailNotVerified,
										cvBlockReasonEmailNotVerified);
							}

						}

						break;

					case Constants.IVR:
						if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED) && MscriptsStringUtils
								.compareStringEquals(customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
										Constants.NUMERIC_TRUE_STRING)) {

							sendCommunicationMode.setSendIvr(Constants.NUMERIC_TRUE_STRING);
						} else {
							LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
							if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED) && !MscriptsStringUtils
									.compareStringEquals(customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
											Constants.NUMERIC_TRUE_STRING)) {
								insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
										finalEncryptionKey, transactionData, cvBlockTypeMobileNotVerified,
										cvBlockReasonMobileNotVerified);
							}

						}

						break;
					default:
						break;
					}

				}
				if (sendCommunicationMode.getSendSms().equals(Constants.NUMERIC_TRUE_STRING)
						|| sendCommunicationMode.getSendApns().equals(Constants.NUMERIC_TRUE_STRING)
						|| sendCommunicationMode.getSendEmail().equals(Constants.NUMERIC_TRUE_STRING)
						|| sendCommunicationMode.getSendGcms().equals(Constants.NUMERIC_TRUE_STRING)) {
					patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_BULK_RX,
							new Object[] { clientId, transactionData.get(0).getPrescriptionNumber(),
									pharmacyData.get(0).getPharmacyNCPDP(), pharmacyData.get(0).getPharmacyNCPDP(),
									lastFilledDate, refillStatus, transactionData.get(0).getTxNumber(), null,
									patientDetailsMap.get(Constants.PRESCRIPTION_ID),
									notificationTimeMap.get(Constants.SENDPICKUPTIME),
									notificationTimeMap.get(Constants.SENDPICKUPTIME), customerId, customerId,
									customerId, sendCommunicationMode.getSendSms(),
									sendCommunicationMode.getSendEmail(), sendCommunicationMode.getSendApns(),
									sendCommunicationMode.getSendGcms(), sendCommunicationMode.getSendIvr(),
									patientDetailsMap.get("prescription_code"), Constants.NUMERIC_FALSE_STRING,
									Constants.NUMERIC_TRUE_STRING, communicationIdMap.get("id") });

				}
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Generic Error Message " + e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);

		}

	}

	/**
	 * This method is to send EOPN communication to customers based on their
	 * preferences
	 * 
	 * @param clientId
	 * @param customerId
	 * @param transactionData
	 * @param patientDetailsMap
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param customerPreference
	 * @param communicationContent
	 * @param communicationName
	 * @param cvInternationalCode
	 * @throws MscriptsException
	 */
	public void sendEOPNCommunicationToUser(String clientId, String customerId, List<TransactionData> transactionData,
			Map<String, String> patientDetailsMap, List<PatientData> patientData, String finalEncryptionKey,
			Map<String, String> customerPreference, Map<String, String> communicationContent, String communicationName,
			boolean isHtml) throws MscriptsException {
		String reminderMode = null;
		String communicationTemplate = null;
		String communicationId = null;
		String apiMsgID = "";
		String rxNumber = null;
		String txNumber = null;
		String emailVerified = null;
		String emailSubject = "";
		String emailSender = "";
		String emailAddress = "";
		String[] cc = null;
		String[] bcc = null;
		String emailContent = null;
		String mscriptsEntityId = null;
		String ivrTemplateId = null;
		String copayAmount = null;
		String storePhoneNumber = null;
		String storeAddressLine1 = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

		Map<String, String> customerDeviceIdMap = null;
		Map<String, String> communicationTemplateMap = null;
		String langCode = Constants.LANG_CODE;
		try {

			String cvMsgTypeApns = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVMSGTYPEAPNS);
			String cvMsgTypeEmail = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVMSGTYPEEMAIL);
			String cvMsgTypeGcms = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVMSGTYPEGCMS);
			String cvMsgStatus = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVMSGSTATUS);
			String cvBlockTypeMobileNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKTYPEMOBILENOTVERIFIED);
			String cvBlockReasonMobileNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKREASONMOBILENOTVERIFIED);
			String cvBlockTypeTextNotActive = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKTYPETEXTNOTACTIVE);
			String cvBlockReasonTextNotActive = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKREASONTEXTNOTACTIVE);
			// [MCE - 2036] To disable text messaging for all communications
			String cvBlockReasonClientTextNotActive = ConfigReader.readConfig(clientId, langCode,
					Constants.CV_BLOCK_REASON_CLIENT_TEXT_NOT_ACTIVE);
			String cvBlockTypeEmailNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKTYPEEMAILNOTVERIFIED);
			String cvBlockReasonEmailNotVerified = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVBLOCKREASONEMAILNOTVERIFIED);
			String reminderLowerCutoffTime = ConfigReader.readConfig(clientId, langCode,
					Constants.cvtReminderLowerCutoffTime);
			String reminderUpperCutoffTime = ConfigReader.readConfig(clientId, langCode,
					Constants.cvtReminderUpperCutoffTime);
			String msgEngineCallBackURL = ConfigReader.readConfig(clientId, langCode, Constants.CVMSGENGINECALLBACKURL);
			String cvMsgTypeIvr = ConfigReader.readConfig(clientId, langCode, Constants.CVMSGTYPEIVR);
			String cvInternationalCode = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVINTERNATINALCODE);
			String secondaryKey = pHICredentials.getSecondaryKey();
			if (mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientId, customerId)) {
				if (null != customerPreference.get(Constants.SEND_REMINDER_MODE)) {
					reminderMode = customerPreference.get(Constants.SEND_REMINDER_MODE);
				}
				if (null != transactionData.get(0).getPrescriptionNumber()) {
					rxNumber = transactionData.get(0).getPrescriptionNumber();
				}
				if (null != transactionData.get(0).getTxNumber()) {
					txNumber = transactionData.get(0).getTxNumber();
				}
				if (null != patientDetailsMap.get(Constants.PRESCRIPTION_ID)) {
					mscriptsEntityId = patientDetailsMap.get("prescription_id");
				}

				String mobileNumber = cvInternationalCode.concat(customerPreference.get("mobile_number"));

				if (null != reminderMode) {
					switch (reminderMode) {
					case Constants.TEXT:
						// [MCE - 2036] To disable text messaging for all
						// communications
						String isTextMessagingEnbaled = ConfigReader.readConfig(clientId, langCode,
								Constants.IS_TEXTMESSAGING_ENABLED);
						if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED)
								&& null != customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE)
								&& MscriptsStringUtils.compareStringEquals(
										customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
										Constants.NUMERIC_TRUE_STRING)
								&& MscriptsStringUtils.compareStringEquals(
										customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE),
										Constants.NUMERIC_TRUE_STRING)
								&& MscriptsStringUtils.compareStringEquals(isTextMessagingEnbaled,
										Constants.NUMERIC_TRUE_STRING)) {

							LOGGER_NON_PHI.info(
									"mobile_number_verified = {}  is_text_message_active = {} send_reminder_mode = {}",
									customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
									customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE),
									customerPreference.get(Constants.SEND_REMINDER_MODE));

							LOGGER_NON_PHI.info("User is Eligible to receive Text message");
							String sendText = smsService.sendMessageFromMap(clientId, mobileNumber, communicationName,
									communicationContent, mscriptsEntityId, true, true, customerId, true,
									customerPreference.get(Constants.SHORTCODE),
									customerPreference.get("shortcode_username"),
									customerPreference.get("shortcode_serviceid"), null,
									customerPreference.get("prefix"), null);
							LOGGER_PHI.info("Sent message = {}", sendText);

						} else {
							LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_TEXT_MSG);
							// [MCE - 2036] To disable text messaging for all
							// communications
							String blockType = Constants.EMPTY_STRING;
							String blockReason = Constants.EMPTY_STRING;
							if (MscriptsStringUtils.compareStringEquals(isTextMessagingEnbaled,
									Constants.NUMERIC_FALSE_STRING)) {
								blockType = cvBlockTypeTextNotActive;
								blockReason = cvBlockReasonClientTextNotActive;
							} else if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED)
									&& !MscriptsStringUtils.compareStringEquals(
											customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
											Constants.NUMERIC_TRUE_STRING)) {
								blockType = cvBlockTypeMobileNotVerified;
								blockReason = cvBlockReasonMobileNotVerified;
							} else if (null != customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE)
									&& !MscriptsStringUtils.compareStringEquals(
											customerPreference.get(Constants.IS_TEXT_MESSAGE_ACTIVE),
											Constants.NUMERIC_TRUE_STRING)) {
								blockType = cvBlockTypeTextNotActive;
								blockReason = cvBlockReasonTextNotActive;
							}

							insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
									finalEncryptionKey, transactionData, blockType, blockReason);
						}

						break;
					case Constants.IOS_PUSH:

						LOGGER_NON_PHI.info("User is Eligible to receive ios push notification");
						customerDeviceIdMap = patientNotificationDao.jdbcDataGet(
								QueryBuilder.GET_CUSTOMER_APNS_DEVICE_ID, new Object[] { customerId, clientId });
						communicationTemplateMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_PUSH_TEMPLATE,
								new Object[] { communicationName, clientId, customerId });
						if (!MscriptsStringUtils.isMapEmptyOrNull(communicationTemplateMap)
								&& communicationTemplateMap.get(Constants.ON_PHONE_TEXT) != null
								&& communicationTemplateMap.get(Constants.COMMUNICATION_ID) != null) {

							communicationTemplate = communicationTemplateMap.get(Constants.ON_PHONE_TEXT);
							communicationId = communicationTemplateMap.get(Constants.COMMUNICATION_ID);
						}

						if (!MscriptsStringUtils.isMapEmptyOrNull(customerDeviceIdMap)
								&& customerDeviceIdMap.get(Constants.DEVICE_ID) != null) {
							
							String apnsFinalMessage = apnsService.frameApnsContent(communicationTemplate,
									communicationContent);
							if (null != apnsFinalMessage) {
								apnsService.sendApns(customerDeviceIdMap.get(Constants.DEVICE_ID), apnsFinalMessage, 0);
								mscriptsCommonDao.insertCommunicationHistories(clientId, customerId, communicationId,
										mscriptsEntityId, cvMsgTypeApns, apnsFinalMessage,
										customerPreference.get(Constants.SHORTCODE),
										customerDeviceIdMap.get(Constants.DEVICE_ID), null, cvMsgStatus, apiMsgID, null,
										null, null, customerId, txNumber, secondaryKey);
							}
						}
						break;
					case Constants.ANDROID_PUSH:
						LOGGER_NON_PHI.info("User is Eligible to receive android push notification");
						customerDeviceIdMap = patientNotificationDao.jdbcDataGet(
								QueryBuilder.GET_CUSTOMER_GCMS_DEVICE_ID, new Object[] { customerId, clientId });
						communicationTemplateMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_PUSH_TEMPLATE,
								new Object[] { communicationName, clientId, customerId });
						if (!MscriptsStringUtils.isMapEmptyOrNull(communicationTemplateMap)
								&& communicationTemplateMap.get(Constants.ON_PHONE_TEXT) != null) {

							communicationTemplate = communicationTemplateMap.get(Constants.ON_PHONE_TEXT);
							communicationId = communicationTemplateMap.get(Constants.COMMUNICATION_ID);
						}
						if (!MscriptsStringUtils.isMapEmptyOrNull(customerDeviceIdMap)
								&& customerDeviceIdMap.get(Constants.DEVICE_ID) != null) {
							String gcmsFinalMessage = gcmsService.frameGcmsContent(communicationTemplate,
									communicationContent);
							if (null != gcmsFinalMessage) {

								String cvIsFCMPushEnabled = ConfigReader.readConfig(clientId, langCode,
										Constants.CV_IS_FCM_PUSH_ENABLED);
								if (Constants.NUMERIC_TRUE_STRING.equals(cvIsFCMPushEnabled)) {
									fcmService.sendFCM(customerDeviceIdMap.get(Constants.DEVICE_ID), gcmsFinalMessage,
											clientId);
								} else {
									gcmsService.sendGcms(customerDeviceIdMap.get(Constants.DEVICE_ID), gcmsFinalMessage,
											0);
								}
								mscriptsCommonDao.insertCommunicationHistories(clientId, customerId, communicationId,
										mscriptsEntityId, cvMsgTypeGcms, gcmsFinalMessage,
										customerPreference.get(Constants.SHORTCODE),
										customerDeviceIdMap.get(Constants.DEVICE_ID), null, cvMsgStatus, apiMsgID, null,
										null, null, customerId, txNumber, secondaryKey);
							}
						}
						break;
					case Constants.EMAIL:
						if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
								&& customerPreference.get(Constants.EMAIL_VERIFIED) != null) {
							emailVerified = customerPreference.get(Constants.EMAIL_VERIFIED);
						}
						if (null != emailVerified && emailVerified.equals(Constants.NUMERIC_TRUE_STRING)) {
							communicationTemplateMap = patientNotificationDao.jdbcDataGet(
									QueryBuilder.GET_EMAIL_TEMPLATE,
									new Object[] { communicationName, clientId, customerId });

							if (!MscriptsStringUtils.isMapEmptyOrNull(communicationTemplateMap)
									&& communicationTemplateMap.get("email_body") != null
									&& communicationTemplateMap.get("email_from") != null
									&& communicationTemplateMap.get("email_subject") != null
									&& communicationTemplateMap.get(Constants.COMMUNICATION_ID) != null) {
								communicationTemplate = communicationTemplateMap.get("email_body");
								emailSender = communicationTemplateMap.get("email_from");
								emailSubject = communicationTemplateMap.get("email_subject");
								communicationId = communicationTemplateMap.get(Constants.COMMUNICATION_ID);
							}

							if (!MscriptsStringUtils.isMapEmptyOrNull(customerPreference)
									&& customerPreference.get("email_address") != null) {
								emailAddress = customerPreference.get("email_address");

							}
							String[] reciever = { emailAddress };
							emailContent = mailService.frameEmailContent(communicationTemplate, communicationContent);
							emailSubject = mailService.frameEmailContent(emailSubject, communicationContent);
							if (emailContent != null) {
								mailService.createMail(reciever, cc, bcc, emailSubject, emailSender, emailContent,
										isHtml);

								// Invokes insertCommunicationHistories()
								mscriptsCommonDao.insertCommunicationHistories(clientId, customerId, communicationId,
										mscriptsEntityId, cvMsgTypeEmail, emailContent, emailSender, emailAddress, null,
										cvMsgStatus, Constants.NUMERIC_TRUE_STRING, null, null, null, customerId, null,
										secondaryKey);
							}
						} else {
							if (null != emailVerified && !emailVerified.equals(Constants.NUMERIC_TRUE_STRING)) {
								insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
										finalEncryptionKey, transactionData, cvBlockTypeEmailNotVerified,
										cvBlockReasonEmailNotVerified);
							}

						}
						break;
					case Constants.IVR:
						if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED) && MscriptsStringUtils
								.compareStringEquals(customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
										Constants.NUMERIC_TRUE_STRING)) {

							LOGGER_NON_PHI.debug(new StringBuilder("mobile_number_verified = ")
									.append(customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED))
									.append("send_reminder_mode = ")
									.append(customerPreference.get(Constants.SEND_REMINDER_MODE)));

							LOGGER_NON_PHI.info("User is Eligible to receive ivr message");
							String ivrCommunicationType = ConfigReader.readConfig(clientId, langCode,
									Constants.CVSINGLEPICKUPIVRPROCESS);
							String cvCopayEnabled = ConfigReader.readConfig(clientId, langCode,
									Constants.cvCopayEnabled);
							Map communicationMap = mscriptsCommonDao.getCommunicationDetails(communicationName,
									clientId);
							if (cvCopayEnabled.contentEquals(Constants.NUMERIC_TRUE_STRING)
									&& communicationContent.get("copay_amt") != null
									&& (!communicationContent.get("copay_amt").isEmpty())) {
								copayAmount = communicationContent.get("copay_amt");
								ivrTemplateId = ConfigReader.readConfig(clientId, langCode,
										Constants.CVSINGLEPICKUPWITHCOPAYTEMPLATEID);
							} else {
								ivrTemplateId = ConfigReader.readConfig(clientId, langCode,
										Constants.CVSINGLEPICKUPWITHOUTCOPAYLTEMPLATEID);
							}
							if (!MscriptsStringUtils.isMapEmptyOrNull(communicationContent)) {
								if (null != communicationContent.get("store_phone_number")) {
									storePhoneNumber = communicationContent.get("store_phone_number");
								}
								if (null != communicationContent.get("store_address_line1")) {
									storeAddressLine1 = communicationContent.get("store_address_line1");
								}
							}

							String showRxName = customerPreference.get("show_rx_name");
							String storeName = customerPreference.get("store_name");
							String storeCity = customerPreference.get("storeCity");
							
							String customerPreferenceBasedOnMaskingType = ivrService.fetchCustomerPreference(clientId, showRxName);

							List<Map<String, String>> prescriptionList = new ArrayList<>();
							HashMap<String, String> prescriptionMap = new HashMap<>();
							/*prescriptionMap.put("storeDetails",
									storeName + ", " + storeAddressLine1 + ", " + storeCity);*/
							//BASE-3329 - Split storeDetails field
							prescriptionMap.put("storeName", storeName);
							prescriptionMap.put("storeAddressLine", storeAddressLine1);
							prescriptionMap.put("storeCity", storeCity);
							
							String rxNameOrNumber = checkRxnumberOrName(transactionData, showRxName, clientId);
							
							prescriptionMap.put(Constants.RXNUMBER, rxNumber);
							prescriptionMap.put(Constants.RXNAME, transactionData.get(0).getDrugName());
							prescriptionMap.put(customerPreferenceBasedOnMaskingType, rxNameOrNumber);
							
							prescriptionMap.put("rxNumId", mscriptsEntityId);
							prescriptionMap.put("copay", copayAmount);

							prescriptionList.add(prescriptionMap);
							
							storePhoneNumber = MiscUtils.formatPhoneNumber(storePhoneNumber);

							Map optionalMap = new HashMap();
							optionalMap.put("callbackRefillUrl", msgEngineCallBackURL);
							optionalMap.put("cutOffStartTime", reminderLowerCutoffTime);
							optionalMap.put("cutOffEndTime", reminderUpperCutoffTime);
							optionalMap.put("pharmacyPhoneNumber", storePhoneNumber);
							optionalMap.put("prescriptions", prescriptionList);
							optionalMap.put("templateId", ivrTemplateId);
							optionalMap.put("ivrCommunicationType", ivrCommunicationType);
							optionalMap.put("customerName", patientData.get(0).getPatientFirstName());
							optionalMap.put("customerPreference", customerPreferenceBasedOnMaskingType);

							String commHistoriesId = mscriptsCommonDao.insertCommunicationHistoriesForSMS(clientId,
									customerId, String.valueOf(communicationMap.get("id")), rxNumber, cvMsgTypeIvr,
									null, storePhoneNumber, mobileNumber, null, cvMsgStatus, apiMsgID, null, null, null,
									Constants.UPDATED_BY_EOPN_STRING, null, 1, secondaryKey);

							LOGGER_PHI.debug("communication Details added to communicationHistories for customer {}",
									customerId);
							Map responseMap = ivrService.sendIvr(clientId, commHistoriesId, "", null, mobileNumber,
									customerId, langCode, optionalMap);
							mscriptsCommonDao.updateCommunicationHistories(clientId, commHistoriesId,
									String.valueOf(responseMap.get("postRequest")), secondaryKey);
							LOGGER_NON_PHI.debug("Ivr communication sent successfully");
							LOGGER_PHI.info("Sent Ivr = {} ", String.valueOf(responseMap.get("postRequest")));

						} else {
							LOGGER_NON_PHI.info(USER_NOT_ELIGIBLE_IVR_MSG);
							if (null != customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED) && !MscriptsStringUtils
									.compareStringEquals(customerPreference.get(Constants.MOBILE_NUMBER_VERIFIED),
											Constants.NUMERIC_TRUE_STRING)) {
								insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData,
										finalEncryptionKey, transactionData, cvBlockTypeMobileNotVerified,
										cvBlockReasonMobileNotVerified);
							}

						}

						break;
					default:
						break;

					}
				}
			}

		} catch (MscriptsException mEx) {
			LOGGER_NON_PHI.error(mEx.getErrorMessage());
			throw new MscriptsException(clientId, mEx.getMessage(), mEx, errorSeverity);
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Generic Error Message " + e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);

		}

	}

	/**
	 * This method is used to insert/update patient communication consent in
	 * mscripts DB for those users who are not registered to mscripts portal.
	 * 
	 * @param rxComId
	 * @param finalEncryptionKey
	 * @param dateOfBirth
	 * @param patientData
	 * @throws MscriptsException
	 */
	public void insertOrUpdatePatientCommunicationConsentForNonRegistered(String rxComId, String finalEncryptionKey,
			String dateOfBirth, List<PatientData> patientData) {
		LOGGER_NON_PHI.info("Inside insertOrUpdatePatientCommunicationConsentForNonRegistered() method");

		Map<String, String> patientCommunicationConsentMap = null;
		String sPatientCommunicationConsent = null;
		String sRecentPatientCommunicationConsent = null;

		try {
			/*
			 * get recent patient communication consent from EOPN update
			 */
			sRecentPatientCommunicationConsent = patientData.get(0).getCommunicationConsent();
			if (null == sRecentPatientCommunicationConsent) {
				/*
				 * patient communication consent value is null. so, ignore
				 * further processing and return
				 */
				LOGGER_NON_PHI.info("Patient communication consent received is NULL; skipping DB insert.");
			} else {
				/*
				 * get patient communication Consent from
				 * non_registered_patient_communication_consent table
				 */
				patientCommunicationConsentMap = patientNotificationDao.jdbcDataGet(
						QueryBuilder.GET_NON_REGISTERED_PATIENT_COMMUNICATION_CONSENT, new Object[] { rxComId });

				if (MscriptsStringUtils.isMapEmptyOrNull(patientCommunicationConsentMap)) {
					/*
					 * patient communication consent not captured previously.
					 * save patient communication consent.
					 */
					LOGGER_NON_PHI
							.info("Patient communication consent received for the first time; inserting into DB.");
					patientNotificationDao.insertOrUpdateJdbcData(
							QueryBuilder.INSERT_INTO_NON_REGISTERED_PATIENT_COMMUNICATION_CONSENT,
							new Object[] { sRecentPatientCommunicationConsent,
									patientData.get(0).getPatientFirstName().toUpperCase(), finalEncryptionKey,
									patientData.get(0).getPatientLastName().toUpperCase(), finalEncryptionKey,
									dateOfBirth, finalEncryptionKey, rxComId, rxComId, rxComId });
				} else {
					/*
					 * get patient communication consent value from
					 * patientCommunicationConsentMap
					 */
					sPatientCommunicationConsent = patientCommunicationConsentMap.get("patientCommunicationConsent");

					/*
					 * if previously stored value is null (set at the time of
					 * feature roll out to distinguish between users before and
					 * after feature roll out) (or) recent value is different
					 * than the previously stored value, save the recent patient
					 * communication consent.
					 */
					if ((null == sPatientCommunicationConsent)
							|| (!sRecentPatientCommunicationConsent.equalsIgnoreCase(sPatientCommunicationConsent))) {
						LOGGER_NON_PHI.info(
								"Patient communication consent received is different from what is stored in DB; updating DB.");
						patientNotificationDao.insertOrUpdateJdbcData(
								QueryBuilder.UPDATE_NON_REGISTERED_PATIENT_COMMUNICATION_CONSENT,
								new Object[] { sRecentPatientCommunicationConsent, rxComId, rxComId });
					} else {
						/*
						 * patient communication consent value is same as
						 * previously captured value. So ignore further
						 * processing and return
						 */
						LOGGER_NON_PHI.info(
								"Patient communication consent received is same as what is stored in DB; skipping DB update.");
					}
				}
			}
		} catch (Exception ex) {
			/*
			 * Note: Exception while capturing patient communication consent
			 * should not stop further processing of the EOPN message.
			 */
			LOGGER_NON_PHI.error("Error while insertOrUpdatePatientCommunicationConsentForNonRegistered {}",
					ex.getMessage());
		}
	}

	/**
	 * This method is used to insert/update patient communication consent in
	 * mscripts DB.
	 * 
	 * @param clientId
	 * @param customerId
	 * @param patientData
	 * @throws MscriptsException
	 */
	public void insertOrUpdatePatientCommunicationConsent(String customerId, List<PatientData> patientData)
			throws MscriptsException {
		LOGGER_NON_PHI.info("Inside insertOrUpdatePatientCommunicationConsent() method");

		Map<String, String> patientCommunicationConsentMap = null;
		String sPatientCommunicationConsent = null;
		String sRecentPatientCommunicationConsent = null;

		try {
			/*
			 * get recent patient communication consent from EOPN update
			 */
			sRecentPatientCommunicationConsent = patientData.get(0).getCommunicationConsent();
			if (null == sRecentPatientCommunicationConsent) {
				/*
				 * patient communication consent value is null. so, ignore
				 * further processing and return
				 */
				LOGGER_NON_PHI.info("Patient communication consent received is NULL; skipping DB insert.");
			} else {
				/*
				 * get patient communication Consent from
				 * patient_communication_consent table
				 */
				patientCommunicationConsentMap = patientNotificationDao
						.jdbcDataGet(QueryBuilder.GET_PATIENT_COMMUNICATION_CONSENT, new Object[] { customerId });

				if (MscriptsStringUtils.isMapEmptyOrNull(patientCommunicationConsentMap)) {
					/*
					 * patient communication consent not captured previously.
					 * save patient communication consent.
					 */
					LOGGER_NON_PHI
							.info("Patient communication consent received for the first time; inserting into DB.");
					patientNotificationDao.insertOrUpdateJdbcData(
							QueryBuilder.INSERT_INTO_PATIENT_COMMUNICATION_CONSENT,
							new Object[] { customerId, sRecentPatientCommunicationConsent, customerId, customerId });
				} else {
					/*
					 * get patient communication consent value from
					 * patientCommunicationConsentMap
					 */
					sPatientCommunicationConsent = patientCommunicationConsentMap.get("patientCommunicationConsent");

					/*
					 * if previously stored value is null (set at the time of
					 * feature roll out to distinguish between users before and
					 * after feature roll out) (or) recent value is different
					 * than the previously stored value, save the recent patient
					 * communication consent.
					 */
					if ((null == sPatientCommunicationConsent)
							|| (!sRecentPatientCommunicationConsent.equalsIgnoreCase(sPatientCommunicationConsent))) {
						LOGGER_NON_PHI.info(
								"Patient communication consent received is different from what is stored in DB; updating DB.");
						patientNotificationDao.insertOrUpdateJdbcData(
								QueryBuilder.INSERT_INTO_PATIENT_COMMUNICATION_CONSENT_HISTORY,
								new Object[] { customerId });
						patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_PATIENT_COMMUNICATION_CONSENT,
								new Object[] { sRecentPatientCommunicationConsent, customerId, customerId });
					} else {
						/*
						 * patient communication consent value is same as
						 * previously captured value. So ignore further
						 * processing and return
						 */
						LOGGER_NON_PHI.info(
								"Patient communication consent received is same as what is stored in DB; skipping DB update.");
					}
				}
			}
		} catch (Exception ex) {
			/*
			 * Note: Exception while capturing patient communication consent
			 * should not stop further processing of the EOPN message.
			 */
			LOGGER_NON_PHI.error("Error while insertOrUpdatePatientCommunicationConsent {}", ex.getMessage());
		}
	}

	/**
	 * This method is used to check if notification came in blocking hours or
	 * not
	 * 
	 * @param clientId
	 * @param userTimezone
	 * @return
	 */
	public Map<String, String> checkNotificationTime(String clientId, String userTimezone) {
		Calendar notificationTimeCalendar;
		Calendar lowerCutOffCalendar;
		Calendar upperCutOffCalendar;
		SimpleDateFormat formatDate;
		SimpleDateFormat formatDateTime;
		String currentDate = null;
		String lowerCutOffDateTime = null;
		String upperCutOffDateTime = null;
		Map<String, String> checkNotificationTimeMap = new HashMap<String, String>();
		String pickUpTime = null;
		String langCode = Constants.LANG_CODE;
		try {
			/*
			 * notificationFlag = 1 - Current date & time is between lowercutoff
			 * and upper cutoff notificationFlag = 2 - current date & time is
			 * smaller than lower cutoff notificationFlag = 3 - current date &
			 * time is bigger than upper cutoff notificationFlag = -1 - Error
			 * while processing
			 */
			String cvtReminderUpperCutoffTime = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTREMINDERUPPERCUTOFFTIME);
			String cvtReminderLowerCutoffTime = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTREMINDERLOWERCUTOFFTIME);
			String cvDateFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATEFORMATDOB);
			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);
			LOGGER_NON_PHI.info("Inside checkNotificationTime method");
			notificationTimeCalendar = Calendar.getInstance(TimeZone.getTimeZone(userTimezone));
			lowerCutOffCalendar = Calendar.getInstance(TimeZone.getTimeZone(userTimezone));
			upperCutOffCalendar = Calendar.getInstance(TimeZone.getTimeZone(userTimezone));

			formatDate = new SimpleDateFormat(cvDateFormatDOB);
			formatDateTime = new SimpleDateFormat(cvDateTimeFormatDOB);
			formatDate.setTimeZone(TimeZone.getTimeZone(userTimezone));
			formatDateTime.setTimeZone(TimeZone.getTimeZone(userTimezone));

			currentDate = formatDate.format(notificationTimeCalendar.getTime());
			lowerCutOffDateTime = currentDate + Constants.SINGLE_WHITESPACE + cvtReminderLowerCutoffTime;
			upperCutOffDateTime = currentDate + Constants.SINGLE_WHITESPACE + cvtReminderUpperCutoffTime;

			lowerCutOffCalendar.setTime(new Timestamp(formatDateTime.parse(lowerCutOffDateTime).getTime()));
			upperCutOffCalendar.setTime(new Timestamp(formatDateTime.parse(upperCutOffDateTime).getTime()));

			if (notificationTimeCalendar.after(lowerCutOffCalendar)
					&& notificationTimeCalendar.before(upperCutOffCalendar)) {

				checkNotificationTimeMap.put(Constants.SEND_MESSAGE_STATUS, Constants.TIME_BTW_CUTOFFS);
			} else if (notificationTimeCalendar.before(lowerCutOffCalendar)) {

				formatDateTime.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
				pickUpTime = formatDateTime.format(lowerCutOffCalendar.getTime());
				// putting the values in the map
				checkNotificationTimeMap.put(Constants.SEND_MESSAGE_STATUS, Constants.TIME_LESS_THATN_LOWER_CUT_OFF);
				checkNotificationTimeMap.put(Constants.SENDPICKUPTIME, pickUpTime);

			} else {
				lowerCutOffCalendar.add(Calendar.DATE, 1);
				formatDateTime.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
				pickUpTime = formatDateTime.format(lowerCutOffCalendar.getTime());
				checkNotificationTimeMap.put(Constants.SEND_MESSAGE_STATUS, Constants.TIME_GREATER_THAN_UPPER_CUT_OFF);
				checkNotificationTimeMap.put(Constants.SENDPICKUPTIME, pickUpTime);
			}

		} catch (Exception pe) {
			LOGGER_NON_PHI.error("Error while calculation of dates inside checkNotificationTime method {}",
					pe.getMessage());
			checkNotificationTimeMap.put(Constants.SEND_MESSAGE_STATUS, "-1");
			return checkNotificationTimeMap;
		}
		return checkNotificationTimeMap;
	}

	/**
	 * this method calculates next sync fill date based on the given logic in
	 * MCE-1210
	 * 
	 * @param transactionData
	 * @param customerId
	 * @param storeNcpdpId
	 * @return
	 * @throws MscriptsException
	 */
	public String calculateNextSyncFillDate(TransactionData transactionData, String customerId, String storeNcpdpId)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			if (null != transactionData.getNextSyncFillDate() && null != transactionData.getWillCallPickedUpDate()) {
				Calendar nextSyncFillDate = Calendar.getInstance();
				Calendar soldDate = Calendar.getInstance();
				SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);// yyyy-MM-dd'T'HH:mm:ss

				nextSyncFillDate.setTime(format.parse(transactionData.getNextSyncFillDate().substring(0, 19)));
				soldDate.setTime(format.parse(transactionData.getWillCallPickedUpDate().substring(0, 19)));
				Integer daysSupply = Integer.parseInt(transactionData.getDaysSupply());
				if (nextSyncFillDate.equals(soldDate)) {
					nextSyncFillDate.add(Calendar.DATE, daysSupply);

				} else if (nextSyncFillDate.after(soldDate)) {
					nextSyncFillDate.add(Calendar.DATE, daysSupply);

				} else if (nextSyncFillDate.before(soldDate)) {
					nextSyncFillDate = Calendar.getInstance();
					nextSyncFillDate.add(Calendar.DATE, daysSupply);
				}

				String nextSyncFillDateString = getLocalDatabaseTime(customerId,
						format.format(nextSyncFillDate.getTime()));
				patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_MEDSYNC_DATE,
						new Object[] { nextSyncFillDateString, customerId, storeNcpdpId });
				return format.format(nextSyncFillDate.getTime());
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while updating medsync {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return null;
	}

	
	/**
	 * This function is used to populate communication map
	 * @param clientId
	 * @param communicationName
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> populateCommunicationMap(String clientId, String communicationName)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			LOGGER_NON_PHI.info("Inside EOPNUtils-populateCommunicationMap");
			String query = QueryBuilder.GET_COMMUNICATION_DETAILS_FROM_NAME;
			return queryInvoker.invokeQueryMap(query, new Object[] { clientId, communicationName });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured Inside populateCommunicationMap {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * The function is used to get customer details
	 * 
	 * @param clientId
	 * @param customerId
	 * @param prescriptionId
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> fetchCustomerDetails(String clientId, String customerId, String prescriptionId)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			LOGGER_NON_PHI.info("Inside fetchCustomerDetails()");
			Map<String, String> encryptionKeyMap = getFinalKey(clientId, pHICredentials.getSecondaryKey());
			final String finalKey = encryptionKeyMap.get("encryptionKey");
			return queryInvoker.invokeQueryMap(QueryBuilder.GET_RX_STORE_DETAILS,
					new Object[] { finalKey, customerId, prescriptionId });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured Inside fetchCustomerDetails() {}", e);
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}

	}

	/**
	 * This function is used to update or insert bulk data in sendbulk_reminders
	 * 
	 * @param sendBulkReminders
	 * @param clientId
	 * @param soldDateString
	 * @param storeName
	 * @param otherMedSyncFormatDate
	 * @param nextSyncFillDate
	 * @throws MscriptsException
	 */
	public void updateOrInsertSendBulkReminders(final SendBulkReminders sendBulkReminders, String clientId,
			String soldDateString, String storeName, String otherMedSyncFormatDate, String nextSyncFillDate)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		String langCode = Constants.LANG_CODE;
		try {
			LOGGER_NON_PHI.debug("Inside updateOrInsertSendBulkReminders");
			final String cvTimeZoneUTC = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVTIMEZONEUTC);
			Map<String, String> encryptionKeyMap = getFinalKey(clientId, pHICredentials.getSecondaryKey());
			final String finalKey = encryptionKeyMap.get("encryptionKey");
			String query = QueryBuilder.GET_SEND_BULK_REMINDERS;
			List<Map<String, String>> sendBulkReminderDetailsList = queryInvoker.invokeQueryMapList(query,
					new Object[] { finalKey, sendBulkReminders.getCustomer_id(), sendBulkReminders.getClient_id(),
							sendBulkReminders.getCommunication_id() });
			if (null != sendBulkReminderDetailsList && !sendBulkReminderDetailsList.isEmpty()) {
				boolean isReminderExist = false;
				for (Map<String, String> sendBulkReminderDetails : sendBulkReminderDetailsList) {
					if (null != sendBulkReminderDetails
							&& null != sendBulkReminderDetails.get(Constants.MESSAGE_TEXT)) {
						if (sendBulkReminderDetails.get(Constants.MESSAGE_TEXT).contains(storeName)
								&& Constants.NUMERIC_FALSE_STRING
										.equalsIgnoreCase(sendBulkReminderDetails.get("message_sent"))) {
							// if reminder exists for same store update the send
							// message date
							query = QueryBuilder.UPDATE_SEND_BULK_REMINDERS;
							queryInvoker.updateUsingSqlString(query,
									new Object[] { sendBulkReminders.getMessage_text(), finalKey,
											sendBulkReminders.getEmail_text(), finalKey,
											sendBulkReminders.getPush_text(), finalKey, soldDateString,
											sendBulkReminders.getCustomer_reminder_send_hour(),
											sendBulkReminders.getCustomer_timezone(), cvTimeZoneUTC, soldDateString,
											sendBulkReminders.getCustomer_reminder_send_hour(),
											sendBulkReminders.getCustomer_timezone(), cvTimeZoneUTC, soldDateString,
											sendBulkReminders.getCustomer_reminder_send_hour(),
											sendBulkReminders.getCustomer_timezone(), cvTimeZoneUTC,
											sendBulkReminderDetails.get("id") });
							isReminderExist = true;
							break;
						} else if (sendBulkReminderDetails.get(Constants.MESSAGE_TEXT).contains(storeName)
								&& Constants.NUMERIC_TRUE_STRING
										.equalsIgnoreCase(sendBulkReminderDetails.get("message_sent"))
								&& (sendBulkReminderDetails.get(Constants.MESSAGE_TEXT).contains(otherMedSyncFormatDate)
										|| sendBulkReminderDetails.get(Constants.MESSAGE_TEXT)
												.contains(nextSyncFillDate))) {
							return;

						}
					}
				}
				if (!isReminderExist) {
					query = QueryBuilder.INSERT_SEND_BULK_REMINDERS;
				}
			} else {
				query = QueryBuilder.INSERT_SEND_BULK_REMINDERS;
			}
			LOGGER_NON_PHI.debug("insert for nextsync filll date reminders messages");
			patientNotificationDao.insertOrUpdateJdbcData(query, new Object[] { sendBulkReminders.getMobile_number(),
					sendBulkReminders.getMessage_text(), finalKey, sendBulkReminders.getEmail_text(), finalKey,
					sendBulkReminders.getPush_text(), finalKey, sendBulkReminders.getCustomer_id(),
					sendBulkReminders.getShortcode(), sendBulkReminders.getClient_id(),
					sendBulkReminders.getRx_number(), sendBulkReminders.getCommunication_id(),
					sendBulkReminders.getMessage_sent(), sendBulkReminders.getErrorSmsReturnType(), soldDateString,
					sendBulkReminders.getCustomer_reminder_send_hour(), sendBulkReminders.getCustomer_timezone(),
					cvTimeZoneUTC, soldDateString, sendBulkReminders.getCustomer_reminder_send_hour(),
					sendBulkReminders.getCustomer_timezone(), cvTimeZoneUTC, soldDateString,
					sendBulkReminders.getCustomer_reminder_send_hour(), sendBulkReminders.getCustomer_timezone(),
					cvTimeZoneUTC, sendBulkReminders.getCustomer_id(), sendBulkReminders.getCustomer_id() });
		} catch (MscriptsException mEx) {
			LOGGER_NON_PHI.error("Error occured in updateOrInsertSendBulkReminders {}", mEx);
			throw new MscriptsException(clientId, mEx.getMessage(), mEx, errorSeverity);
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured in updateOrInsertSendBulkReminders {}", ex);
			throw new MscriptsException(clientId, ex.getMessage(), ex, errorSeverity);
		}

	}

	/**
	 * This function is used to change the promise time date format into user time zone
	 * @param customerId
	 * @param time
	 * @return
	 * @throws MscriptsException
	 */
	public String getLocalDatabaseTime(String customerId, String time) throws MscriptsException {
		Map<String, String> customerTextPreference = null;
		SimpleDateFormat formatter = null;
		SimpleDateFormat formatterInput = null;
		String formattedPromiseTime = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		String langCode = Constants.LANG_CODE;
		try {
			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);
			String cvDateTimeFormatInputParsed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATINPUTPARSED);
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);
			if (time != null) {
				LOGGER_PHI.info("Getting the user timeZone");
				customerTextPreference = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_USER_TIMEZONE,
						new Object[] { customerId });
				String userTimezone = customerTextPreference.get(Constants.TIME_ZONE);
				time = time.substring(0, 19);
				formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
				formatterInput = new SimpleDateFormat(cvDateTimeFormatInputParsed);
				// Formatting Promise time to store in
				// DB
				formatter.setTimeZone(TimeZone.getTimeZone(userTimezone));
				formatterInput.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
				formattedPromiseTime = formatter.format(formatterInput.parse(time));
				LOGGER_PHI.info("Level 1 Formatted promise Time =  {}", formattedPromiseTime);

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(cvDateTimeFormatDOB);
				Date date = simpleDateFormat.parse(formattedPromiseTime);

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeZone(TimeZone.getTimeZone(userTimezone));
				calendar.setTime(date);

				formattedPromiseTime = simpleDateFormat.format(calendar.getTime());
				LOGGER_PHI.info("Final Formatted promise Time to store in DB = {}", formattedPromiseTime);
			}
			return formattedPromiseTime;
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting user time/partialFillDate {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to get the last filled date
	 * 
	 * @param userTimezone
	 * @param lastFilledDate
	 * @return
	 * @throws Exception
	 */
	public String getLastFilledDate(String userTimezone, String lastFilledDate) throws Exception {

		SimpleDateFormat formatter = null;
		SimpleDateFormat formatterInput = null;
		String langCode = Constants.LANG_CODE;
		try {
			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);
			String cvDateTimeFormatInputParsed = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATINPUTPARSED);
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);
			lastFilledDate = lastFilledDate.substring(0, 19);
			formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
			formatterInput = new SimpleDateFormat(cvDateTimeFormatInputParsed);
			// Formatting Last filled date to store in DB
			formatter.setTimeZone(TimeZone.getTimeZone(userTimezone));
			formatterInput.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
			String formattedLastFilledDate = formatter.format(formatterInput.parse(lastFilledDate));
			LOGGER_PHI.info("Formatted last filled date = {}", formattedLastFilledDate);
			return formattedLastFilledDate;
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting user lastFilledDate {}", e.getMessage());
			return null;
		}
	}

	/**
	 * This method is used to determine whether to send the PNS message or not
	 * based on promise date / message date. </br>
	 * </br>
	 * *
	 * 
	 * @param customerId
	 * @param promiseDate
	 * @param messageDate
	 * @param pdxContactReason
	 * @return boolean
	 * @throws MscriptsException
	 */
	public boolean isBackDatedMessage(String promiseDate, String messageDate, PdxContactReason pdxContactReason)
			throws MscriptsException {
		boolean isBackDatedMessage = false;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			LOGGER_NON_PHI.info("Inside isBackDatedMessage method promiseDate {} messageDate {}", promiseDate,
					messageDate);
			String langCode = Constants.LANG_CODE;

			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);
			String cvDateTimeFormatInput = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATINPUT);
			String cvDateTimeFormatXml = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATXML);
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);

			String currentDate = MiscUtils.getCurrentDateByFormat(cvDateTimeFormatDOB, cvTimeZoneUTC);

			LOGGER_NON_PHI.info("currentDate {}", currentDate);

			/* current time will be in UTC. */
			DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(cvDateTimeFormatDOB);
			dateTimeFormatter.withZoneUTC();
			DateTime currentDateTime = dateTimeFormatter.parseDateTime(currentDate);
			LOGGER_NON_PHI.info("currentDateTime {}", currentDateTime);
			/* message time will be in UTC. */
			dateTimeFormatter = DateTimeFormat.forPattern(cvDateTimeFormatXml);
			dateTimeFormatter.withZoneUTC();
			DateTime messageDateTime = dateTimeFormatter.parseDateTime(messageDate);
			LOGGER_NON_PHI.info("messageDateTime {}", messageDateTime);
			String cvNumberOfHoursToConsiderForBackDatedInProcessMsgs = ConfigReader.readConfig(
					Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVNUMBEROFHOURSTOCONSIDERFORBACKDATEDINPROCESSMSGS);
			String cvNumberOfHoursToConsiderForBackDatedOutOfStockMsgs = ConfigReader.readConfig(
					Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVNUMBEROFHOURSTOCONSIDERFORBACKDATEDOUTOFSTOCKMSGS);
			String cvNumberOfHoursToConsiderForBackDatedPartialFillMsgs = ConfigReader.readConfig(
					Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVNUMBEROFHOURSTOCONSIDERFORBACKDATEDPARTIALFILLMSGS);
			String cvNumberOfHoursToConsiderForBackDatedIssueMsgs = ConfigReader.readConfig(Constants.cvDefaultClientId,
					langCode, ConfigKeys.CVNUMBEROFHOURSTOCONSIDERFORBACKDATEDISSUEMSGS);
			String cvNumberOfHoursToConsiderForBackDatedReadyMsgs = ConfigReader.readConfig(Constants.cvDefaultClientId,
					langCode, ConfigKeys.CVNUMBEROFHOURSTOCONSIDERFORBACKDATEDREADYMSGS);

			int hoursBetween = 0;
			int minutesBetween = 0;
			switch (pdxContactReason) {
			case ISSUE:
				/*
				 * There is a very slight chance of messageDateTime being
				 * greater than currentDate due to time difference in servers.
				 */
				if (messageDateTime.isAfter(currentDateTime)) {
					isBackDatedMessage = false;
				} else {
					hoursBetween = Hours.hoursBetween(messageDateTime, currentDateTime).getHours();
					minutesBetween = Minutes.minutesBetween(messageDateTime, currentDateTime).getMinutes() % 60;
					if ((hoursBetween < Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedIssueMsgs))
							|| (hoursBetween == Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedIssueMsgs)
									&& minutesBetween < 1)) {
						isBackDatedMessage = false;
					} else {
						isBackDatedMessage = true;
					}
				}
				break;
			case OUT_OF_STOCK:
				/*
				 * There is a very slight chance of messageDateTime being
				 * greater than currentDate due to time difference in servers.
				 */
				if (messageDateTime.isAfter(currentDateTime)) {
					LOGGER_NON_PHI.info("Send OOS message");
					isBackDatedMessage = false;
				} else {
					hoursBetween = Hours.hoursBetween(messageDateTime, currentDateTime).getHours();
					LOGGER_NON_PHI.info("OOS hoursBetween {}", hoursBetween);
					minutesBetween = Minutes.minutesBetween(messageDateTime, currentDateTime).getMinutes() % 60;
					LOGGER_NON_PHI.info("OOS minutesBetween {}", minutesBetween);
					if ((hoursBetween < Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedOutOfStockMsgs))
							|| (hoursBetween == Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedOutOfStockMsgs)
									&& minutesBetween < 1)) {
						LOGGER_NON_PHI.info("OOS Send OOS message");
						isBackDatedMessage = false;
					} else {
						LOGGER_NON_PHI.info("OOS !Send OOS message");
						isBackDatedMessage = true;
					}
				}
				break;
			case PARTIAL_FILL:
				/*
				 * There is a very slight chance of messageDateTime being
				 * greater than currentDate due to time difference in servers.
				 */
				if (messageDateTime.isAfter(currentDateTime)) {
					isBackDatedMessage = false;
				} else {
					hoursBetween = Hours.hoursBetween(messageDateTime, currentDateTime).getHours();
					minutesBetween = Minutes.minutesBetween(messageDateTime, currentDateTime).getMinutes() % 60;
					if ((hoursBetween < Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedPartialFillMsgs))
							|| (hoursBetween == Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedPartialFillMsgs)
									&& minutesBetween < 1)) {
						isBackDatedMessage = false;
					} else {
						isBackDatedMessage = true;
					}
				}
				break;
			case READY:
				if (messageDateTime.isAfter(currentDateTime)) {
					isBackDatedMessage = false;
				} else {
					hoursBetween = Hours.hoursBetween(messageDateTime, currentDateTime).getHours();
					minutesBetween = Minutes.minutesBetween(messageDateTime, currentDateTime).getMinutes() % 60;
					if ((hoursBetween < Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedReadyMsgs))
							|| (hoursBetween == Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedReadyMsgs)
									&& minutesBetween < 1)) {
						isBackDatedMessage = false;
					} else {
						isBackDatedMessage = true;
					}
				}
				break;
			case SOLD:
				break;
			case CANCELLED:
				break;
			case HOLD_FOR_PF:
				break;
			case ADJUDICATION_COMPLETE:
				/*
				 * promise time will be in UTC.
				 * 
				 * Promise time format in adjudication complete is:
				 * cvDateTimeFormatInput Promise time format in out of stock is:
				 * cvDateTimeFormatXml CAUTION: So do not move this part of code
				 * outside.
				 */
				dateTimeFormatter = DateTimeFormat.forPattern(cvDateTimeFormatInput);
				dateTimeFormatter.withZoneUTC();
				DateTime promiseDateTime = dateTimeFormatter.parseDateTime(promiseDate);

				// Promise date is expected to be future date.
				if (promiseDateTime.isAfter(currentDateTime)) {
					isBackDatedMessage = false;
				} else {
					hoursBetween = Hours.hoursBetween(promiseDateTime, currentDateTime).getHours();
					minutesBetween = Minutes.minutesBetween(promiseDateTime, currentDateTime).getMinutes() % 60;
					if ((hoursBetween < Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedInProcessMsgs))
							|| (hoursBetween == Integer.parseInt(cvNumberOfHoursToConsiderForBackDatedInProcessMsgs)
									&& minutesBetween < 1)) {
						isBackDatedMessage = false;
					} else {
						isBackDatedMessage = true;
					}
				}
				break;
			default:
			}

			return isBackDatedMessage;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error while checking whether the message is back dated. {}", ex.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, ex.getMessage(), ex, errorSeverity);
		}
	}

	/**
	 * To process Patient Payment method
	 * 
	 * @param iClientId
	 * @param lCustomerId
	 * @param patientCreditCardList
	 * @throws MscriptsException
	 *             if Unable to insert/update/delete the Patient Payment method.
	 */
	public void processPatientCreditCard(int iClientId, long lCustomerId, List<CreditCard> patientCreditCardList)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			if (patientCreditCardList != null && !patientCreditCardList.isEmpty()) {
				CreditCard patientCreditCard = patientCreditCardList.get(0);

				LOGGER_NON_PHI.info("Inserting/Updating patient credit card info");
				String expiryDate = patientCreditCard.getExpiryDate();
				if (!MscriptsStringUtils.isStringEmptyOrNull(expiryDate)) {
					int length = expiryDate.length();
					expiryDate = expiryDate.substring(0, length - 2) + "/" + expiryDate.substring(length - 2);
				}
				patientPaymentMethodService.deletePatientPaymentMethod(lCustomerId, Constants.NUMERIC_TRUE_STRING);

				patientPaymentMethodService.createPatientPaymentMethod(Long.valueOf(iClientId), lCustomerId,
						patientCreditCard.getType(), patientCreditCard.getLastFourDigits(), expiryDate,
						Constants.NUMERIC_TRUE_STRING, null, null, false);

			} else {
				LOGGER_NON_PHI
						.info("No credit card info received. So deleting existing patient credit card info, if any");
				patientPaymentMethodService.deleteAllPatientPaymentMethod(lCustomerId);
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while updating the credit card info {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to get if we have to show rx number of name based
	 * on customer preference in the notification
	 * 
	 * @param transactionDataList
	 * @param iCustomershowRxNameFlag
	 * @param clientId
	 * @return
	 * @throws MscriptsException
	 */
	public String checkRxnumberOrName(List<TransactionData> transactionDataList, String iCustomershowRxNameFlag,
			String clientId) throws MscriptsException {
		String rxName = "";
		String rxNameOrNumber = "";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		String langCode = Constants.LANG_CODE;
		try {
			String cvMaskRegexExpression = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVMASKREGEXEXPRESSION);
			String cvMaskPattern = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVMASTPATTERN);
			if (!MscriptsStringUtils.isCollectionEmptyOrNull(transactionDataList)) {
				TransactionData transactionData = transactionDataList.get(0);
				if (!MscriptsStringUtils.isStringEmptyOrNull(iCustomershowRxNameFlag)) {
					rxName = transactionData.getDrugName().toUpperCase();

					/*
					 * Masking Type value = 0 - User Preference for show Rx name
					 * 0 then send Rx Number = 0 - User Preference for show Rx
					 * name 1 then send Full Rx name = 1 - User Preference for
					 * show Rx name 0 then send Rx Number = 1 - User Preference
					 * for show Rx name 1 then send Masked Rx Name = 2 - User
					 * Preference for show Rx name 0 then send Masked Rx Name =
					 * 2 - User Preference for show Rx name 1 then send Full Rx
					 * Number
					 */

					rxNameOrNumber = textMaskingService.maskingTextBasedOnMaskingType(clientId, rxName,
							cvMaskRegexExpression, cvMaskPattern, iCustomershowRxNameFlag,
							transactionData.getPrescriptionNumber());

				}

			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error(
					"Error while calculating whether Rx name or Number is the preference of the customer {}",
					e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return rxNameOrNumber;
	}

	/**
	 * This function is used to check if prescription notification is in
	 * sequence with current prescription status
	 * 
	 * @param contactReason
	 * @param rxNumber
	 * @param storeNcpdpId
	 * @return
	 * @throws MscriptsException
	 */
	public boolean checkIfPrescNotificationInSequenceWithCurrentPrescriptionStatus(String contactReason,
			String rxNumber, String storeNcpdpId) throws MscriptsException {

		String methodName = "checkIfPrescNotificationInSequenceWithCurrentPrescriptionStatus";
		boolean notificationInSequenceFlag = true;
		String langCode = Constants.LANG_CODE;
		String sep = Constants.PIPE;

		SimpleDateFormat formatter = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvEnforceNotificationContactReasonSequence = ConfigReader.readConfig(Constants.cvDefaultClientId,
					langCode, ConfigKeys.CVENFORCENOTIFICATIONCONTACTREASONSEQUENCE);

			if (!MscriptsStringUtils.isStringEmptyOrNull(cvEnforceNotificationContactReasonSequence)
					&& MscriptsStringUtils.compareStringEquals(cvEnforceNotificationContactReasonSequence,
							Constants.NUMERIC_TRUE_STRING)) {

				String configStatusReady = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVTREFILLSTATUSFILLED);
				String configStatusSold = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVTREFILLSTATUSCOMPLETED);

				/*
				 * these are status strings stored in rx_txn_status table,
				 * referenced in patient_rx_txn
				 */
				String configRxTxnStatusReady = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVRXREADYSTATUS);
				String configRxTxnStatusSold = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVRXSOLDSTATUS);

				String cvContactReasonReady = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVCONTACTREASONREADY);

				String cvContactReasonRxAdjudicationComplete = ConfigReader.readConfig(Constants.cvDefaultClientId,
						langCode, ConfigKeys.CVCONTACTREASONRXADJUDICATIONCOMPLETE);

				String cvHowLongInHoursBeforeContactReasonIsOutOfSequence = ConfigReader.readConfig(
						Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVHOWLONGINHOURSBEFORECONTACTREASONISOUTOFSEQUENCE);

				int howLongInHoursBeforeContactReasonIsOutOfSequence = Integer
						.parseInt(cvHowLongInHoursBeforeContactReasonIsOutOfSequence);

				String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVTIMEZONEUTC);
				String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
						ConfigKeys.CVDATETIMEFORMATDOB);

				Calendar currTimeAdjustedOutOfSequenceConfig = Calendar
						.getInstance(TimeZone.getTimeZone(cvTimeZoneUTC));

				currTimeAdjustedOutOfSequenceConfig.add(Calendar.HOUR,
						-howLongInHoursBeforeContactReasonIsOutOfSequence);

				formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
				formatter.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
				String currTimeAdjustedOutOfSequenceConfigStr = formatter
						.format(currTimeAdjustedOutOfSequenceConfig.getTime());
				Map<String, Object> prescriptionAndTxnMap = patientNotificationDao
						.getTxnStatusAndCustomerPrxRefillStatusByRxNumber(new Object[] { rxNumber, storeNcpdpId });

				if (prescriptionAndTxnMap != null) {

					if (prescriptionAndTxnMap.get("prtxStatusName") != null
							&& prescriptionAndTxnMap.get("prtxLastUpdated") != null) {
						String rxTxnStatusName = (String) prescriptionAndTxnMap.get("prtxStatusName");

						Date prtxLastUpdated = (Date) prescriptionAndTxnMap.get("prtxLastUpdated");

						boolean isLastTxnUpdateAfterConfigThreshold = prtxLastUpdated
								.after(currTimeAdjustedOutOfSequenceConfig.getTime());

						// If last txn update time is newer than config
						// threshold then it is not in sequence
						if (isLastTxnUpdateAfterConfigThreshold) {
							if (MscriptsStringUtils.compareStringEquals(rxTxnStatusName, configRxTxnStatusSold)) {
								if (MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonReady)
										|| MscriptsStringUtils.compareStringEquals(contactReason,
												cvContactReasonRxAdjudicationComplete)) {
									notificationInSequenceFlag = false;
								}
							} else if (MscriptsStringUtils.compareStringEquals(rxTxnStatusName, configRxTxnStatusReady)
									&& MscriptsStringUtils.compareStringEquals(contactReason,
											cvContactReasonRxAdjudicationComplete)) {
								notificationInSequenceFlag = false;
							}
						}

						StringBuilder sBuilder = new StringBuilder(methodName).append(": ")
								.append("Sequence check. Using patient_rx_txn record: ").append(sep)
								.append("contactRsn: ").append(contactReason).append(sep).append("prtxId: ")
								.append(prescriptionAndTxnMap.get("prtxId")).append(sep).append("prtxStatusName: ")
								.append(rxTxnStatusName).append(sep).append("prtxLastUpdated: ").append(prtxLastUpdated)
								.append(sep).append("currTime minus threshold: ")
								.append(currTimeAdjustedOutOfSequenceConfigStr).append(sep).append("result: ")
								.append(notificationInSequenceFlag);

						LOGGER_NON_PHI.info(sBuilder);

					} else if (prescriptionAndTxnMap.get("cpLatestRefillStatus") != null
							&& prescriptionAndTxnMap.get("cpLastUpdated") != null) {

						String cpLatestRefillStatus = (String) prescriptionAndTxnMap.get("cpLatestRefillStatus");

						Date cpLastUpdated = (Date) prescriptionAndTxnMap.get("cpLastUpdated");

						boolean isLastPrescUpdateAfterConfigThreshold = cpLastUpdated
								.after(currTimeAdjustedOutOfSequenceConfig.getTime());

						if (isLastPrescUpdateAfterConfigThreshold) {
							// if customer prescription status is sold and
							if (MscriptsStringUtils.compareStringEquals(cpLatestRefillStatus, configStatusSold)) {
								if (MscriptsStringUtils.compareStringEquals(contactReason, cvContactReasonReady)
										|| MscriptsStringUtils.compareStringEquals(contactReason,
												cvContactReasonRxAdjudicationComplete)) {
									notificationInSequenceFlag = false;
								}
							} else if (MscriptsStringUtils.compareStringEquals(cpLatestRefillStatus, configStatusReady)
									&& MscriptsStringUtils.compareStringEquals(contactReason,
											cvContactReasonRxAdjudicationComplete)) {
								// if current txn status is ready and contact
								// reason is ready or in process
								notificationInSequenceFlag = false;
							}
						}

						StringBuilder sBuilder = new StringBuilder(methodName).append(": ")
								.append("Sequence check. Using customer_prescriptions record: ").append(sep)
								.append("contactRsn: ").append(contactReason).append(sep).append("cpId: ")
								.append(prescriptionAndTxnMap.get("cpId")).append(sep).append("cpLatestRefillStatus: ")
								.append(cpLatestRefillStatus).append(sep).append("cpLastUpdated: ")
								.append(cpLastUpdated).append(sep).append("currTime minus threshold: ")
								.append(currTimeAdjustedOutOfSequenceConfigStr).append(sep).append("result: ")
								.append(notificationInSequenceFlag);

						LOGGER_NON_PHI.info(sBuilder);
					}
				}
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in {} :{}", methodName, e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}

		return notificationInSequenceFlag;
	}

	/**
	 * This function is used to get prescription notification type with in
	 * specified hours
	 * 
	 * @param prescriptionId
	 * @param clientId
	 * @param communicationName
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getPrescNotificationTypeCountWithinSpecifiedHours(String prescriptionId, String clientId,
			String communicationName) throws MscriptsException {

		String methodName = "getPrescNotificationTypeCountWithinSpecifiedHours";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Map<String, String> countMessagesMap = null;
		Calendar calendar = null;
		SimpleDateFormat formatter = null;
		String langCode = Constants.LANG_CODE;

		try {
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);
			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);

			/*
			 * Check whether we have sent a particular type of communication
			 * within a period specified in conifugration:
			 * cvHowLongInHoursBeforeSendingSameNotificationForPresc. Done to
			 * avoid multiple messages for a prescription within a period of
			 * time
			 */

			String cvHowLongInHoursBeforeSendingSameNotificationForPresc = ConfigReader.readConfig(
					Constants.cvDefaultClientId, langCode,
					ConfigKeys.CV_HOW_LONG_IN_HOURS_BEFORE_SENDING_SAME_NOTIFICATION_FOR_PRESC);

			int howLongInHoursBeforeSendingSameNotificationForPresc = Integer
					.parseInt(cvHowLongInHoursBeforeSendingSameNotificationForPresc);

			calendar = Calendar.getInstance(TimeZone.getTimeZone(cvTimeZoneUTC));
			calendar.add(Calendar.HOUR, -howLongInHoursBeforeSendingSameNotificationForPresc);
			formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
			formatter.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
			String date = formatter.format(calendar.getTime());
			LOGGER_NON_PHI.info("Date to be compared = {}", date);
			countMessagesMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_DAILY_COUNT_MESSAGE,
					new Object[] { date, prescriptionId, communicationName, clientId });

		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in method {} : {}", methodName, e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return countMessagesMap;
	}

	/**
	 * This function is used to get promise time for notification
	 * 
	 * @param clientId
	 * @param promiseTimeFromPayload
	 * @param customerTextPreferenceTimeZone
	 * @return
	 * @throws MscriptsException
	 */
	public Date getPromiseTimeForNotification(String clientId, String promiseTimeFromPayload,
			String customerTextPreferenceTimeZone) throws MscriptsException {

		String methodName = "getPromiseTimeForMessage";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Date result = null;
		String langCode = Constants.LANG_CODE;

		try {
			String cvDateTimeFormatDOB = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVDATETIMEFORMATDOB);

			String cvRoundTimeToNearestQuarter = ConfigReader.readConfig(clientId, langCode,
					Constants.cvRoundTimeToNearestQuarter);

			String promiseTimeStr = null;

			if (MscriptsStringUtils.compareStringEquals(cvRoundTimeToNearestQuarter, Constants.NUMERIC_TRUE_STRING)) {
				promiseTimeStr = MiscUtils.roundTimeToNearestQuarter(promiseTimeFromPayload, cvDateTimeFormatDOB,
						customerTextPreferenceTimeZone);
			}

			SimpleDateFormat formatterInput = new SimpleDateFormat(cvDateTimeFormatDOB);

			result = formatterInput.parse(promiseTimeStr);

			LOGGER_NON_PHI.debug("Promise time for notification: {}", result);
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in {} : {}", methodName, e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return result;
	}

	/**
	 * This function returns true if new promise time is after old promise time
	 * 
	 * @param oldPromiseTime
	 * @param newPromiseTime
	 * @param customerTextPreferenceTimeZone
	 * @param clientId
	 * @return
	 * @throws MscriptsException
	 */
	public boolean isRoundedUpNewPromiseTimeAfterOldPromiseTime(String oldPromiseTime, String newPromiseTime,
			String customerTextPreferenceTimeZone, String clientId) throws MscriptsException {

		String methodName = "isRoundedUpNewPromiseTimeAfterOldPromiseTime";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		boolean result = false;
		String langCode = Constants.LANG_CODE;
		try {

			String cvDateTimeFormatDOB = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVDATETIMEFORMATDOB);

			String oldPromiseTimeRoundedUpStr = MiscUtils.roundTimeToNearestQuarter(oldPromiseTime, cvDateTimeFormatDOB,
					customerTextPreferenceTimeZone);
			String newPromiseTimeRoundedUpStr = MiscUtils.roundTimeToNearestQuarter(newPromiseTime, cvDateTimeFormatDOB,
					customerTextPreferenceTimeZone);

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(cvDateTimeFormatDOB);
			Date oldPromiseTimeObj = simpleDateFormat.parse(oldPromiseTimeRoundedUpStr);
			Date newPromiseTimeObj = simpleDateFormat.parse(newPromiseTimeRoundedUpStr);

			LOGGER_NON_PHI.debug("roundedup-old:{} - roundedup-new: ", oldPromiseTimeObj, newPromiseTimeObj);

			if (newPromiseTimeObj.after(oldPromiseTimeObj)) {
				result = true;
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in {} : {}", methodName, e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return result;
	}

	/**
	 * MCE-2125 : handled EOPN for users who do not have any reminder mapping
	 * entry, removed timezone parameter
	 * 
	 * @param oldPromiseTime
	 * @param newPromiseTime
	 * @param clientId
	 * @return
	 * @throws MscriptsException
	 */
	public boolean isNewPromiseTimeAfterOldPromiseTime(String oldPromiseTime, String newPromiseTime, String clientId)
			throws MscriptsException {

		String methodName = "isNewPromiseTimeAfterOldPromiseTime";
		boolean result = false;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		String langCode = Constants.LANG_CODE;
		try {
			String cvDateTimeFormatDOB = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVDATETIMEFORMATDOB);

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(cvDateTimeFormatDOB);
			Date oldPromiseTimeObj = simpleDateFormat.parse(oldPromiseTime);
			Date newPromiseTimeObj = simpleDateFormat.parse(newPromiseTime);

			LOGGER_NON_PHI.debug("old: {}  - new: ", oldPromiseTimeObj, newPromiseTimeObj);

			if (newPromiseTimeObj.after(oldPromiseTimeObj)) {
				result = true;
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in {} : ", methodName, e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return result;
	}

	/**
	 * This function is used to process patient loyalty card
	 * 
	 * @param sClientId
	 * @param sCustomerId
	 * @param patientDataList
	 * @throws MscriptsException
	 */
	public void processPatientLoyaltyCard(String sClientId, String sCustomerId, List<PatientData> patientDataList)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			PatientData patientData = patientDataList.get(0);
			String patientLoyalityNumber = patientData.getLoyaltyCardNumber();
			if (!MscriptsStringUtils.isStringEmptyOrNull(patientLoyalityNumber))
				patientLoyaltyCardService.updatePatientLoyaltyCard(sClientId, sCustomerId, patientLoyalityNumber,
						patientData.getLoyaltyCardOptOut());
			else {
				patientLoyaltyCardService.deletePatientLoyaltyCard(sCustomerId);
			}
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while updating the Loyalty Card info {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to check store exception
	 * 
	 * @param sClientId
	 * @param inputStoreNcpdpId
	 * @return
	 * @throws MscriptsException
	 */
	public boolean checkStoreException(String sClientId, String inputStoreNcpdpId) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			List<String> storeExceptionNcpdpList = patientNotificationDao
					.jdbcDataGetList(QueryBuilder.GET_STORE_EXCEPTION_NCPDP, new Object[] { sClientId });
			if (storeExceptionNcpdpList.contains(inputStoreNcpdpId))
				return false;
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while checking store exception {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return true;
	}

	/**
	 * This function is used to get Rx Auto fill enabled value
	 * 
	 * @param sAutoFillEnabled
	 * @return
	 * @throws MscriptsException
	 */
	public int getRxAutoFillEnabledValue(String sAutoFillEnabled) throws MscriptsException {
		int iAutoFillEnabled = 0;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			switch (sAutoFillEnabled) {
			// Auto Fill Enabled
			case "Y":
				iAutoFillEnabled = 1;
				break;
			// Patient Refused Auto Fill
			case "R":
				iAutoFillEnabled = 2;
				break;
			// Auto Fill NOT Enabled
			case "N":
			default:
				iAutoFillEnabled = 0;
			}

			return iAutoFillEnabled;
		} catch (NullPointerException npEx) {
			return iAutoFillEnabled;
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while processing Rx auto fill enabled flag. {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This method is used to determine whether to send the PNS message or not
	 * based on promise date. </br>
	 * </br>
	 * Requirement: If promiseDate is equal to or greater than current date,
	 * please send the PNS message. Else, please do not send PNS message.
	 * 
	 * @param sCustomerId
	 * @param sPromiseTime
	 * @param sPromiseTimeFormat
	 * @param sDateFormat
	 * @return
	 * @throws MscriptsException
	 */
	public boolean isSendPnsMessage(String sCustomerId, String sPromiseTime, String sPromiseTimeFormat,
			String sDateFormat) throws MscriptsException {
		boolean bIsSendPnsMessage = false;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			if (sPromiseTime != null && (!sPromiseTime.isEmpty()) && sPromiseTimeFormat != null
					&& (!sPromiseTimeFormat.isEmpty())) {
				SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat(sPromiseTimeFormat);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(sDateFormat);

				LOGGER_PHI.info("Getting the user timeZone");
				Map<String, String> customerTextPreference = patientNotificationDao
						.jdbcDataGet(QueryBuilder.GET_USER_TIMEZONE, new Object[] { sCustomerId });
				String sUserTimezone = customerTextPreference.get("timezone");
				simpleDateTimeFormat.setTimeZone(TimeZone.getTimeZone(sUserTimezone));
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone(sUserTimezone));

				Date dPromiseDate = simpleDateTimeFormat.parse(sPromiseTime);

				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(sUserTimezone));
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				Date dCurrentDate = calendar.getTime();

				LOGGER_NON_PHI.info("Current Date: {} ; Promise Date: {}", dCurrentDate, dPromiseDate);

				if (dPromiseDate.equals(dCurrentDate) || dPromiseDate.after(dCurrentDate)) {
					bIsSendPnsMessage = true;
				}
			}

			return bIsSendPnsMessage;
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while comparing promise time with current date. {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to update Rx quantity in refill history
	 * 
	 * @param sCustomerPrescriptionId
	 * @param sRxQuantity
	 * @param sRxQuantityUnit
	 * @throws MscriptsException
	 */
	public void updateRxQuantityInRefillHistory(String sCustomerPrescriptionId, String sRxQuantity,
			String sRxQuantityUnit) throws MscriptsException {
		LOGGER_NON_PHI.info("Updating Rx quantity and quantity unit in Refill History.");
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			LOGGER_NON_PHI.info("Getting the recent filled date for the Rx.");
			Map<String, String> rxMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_RECENT_RX_FILLED_DATE,
					new Object[] { sCustomerPrescriptionId });

			String sFilledDate = rxMap.get("filled");

			LOGGER_NON_PHI.info("Getting the Rx Refill Id for the recent transaction.");
			Map<String, String> rxRefillMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_RECENT_RX_REFILL_ID,
					new Object[] { sCustomerPrescriptionId, sFilledDate });

			String sRxRefillId = rxRefillMap.get("id");

			LOGGER_NON_PHI
					.info("Updating the Rx quantity and quantity unit in prescription_transaction_history table.");
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_RX_QUANTITY_IN_REFILL_HISTORY,
					new Object[] { sRxQuantity, sRxQuantityUnit, sRxRefillId });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while updating the Rx quantity in Refill History. Exception suppressed. {}",
					e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This method is used to update the mscriptsAutoFillCriteria for a Rx
	 * 
	 * @param customerId
	 * @param rxNumber
	 * @param storeNcpdpId
	 * @param prescriptionId
	 * @param patientNotification
	 * @throws MscriptsException
	 */
	public void updateMscriptsAutoFillCriteria(String clientId, String customerId, String rxNumber, String storeNcpdpId,
			String prescriptionId, PatientNotificationRequest patientNotification) throws MscriptsException {

		LOGGER_NON_PHI.info("Inside updateMscriptsAutoFillCriteria() method");

		/*
		 * <transactionData> - <specialtyDrug>Y</specialtyDrug>
		 * <transactionData> - <drugDisallowAutofill>Y</drugDisallowAutofill>
		 * <transactionData> - <syncScriptEnrolled>Y</syncScriptEnrolled>
		 * <patientData> - <patientDisallowAutofill>Y</patientDisallowAutofill>
		 * <prescriberData> -
		 * <prescriberDisallowAutofill>Y</prescriberDisallowAutofill>
		 * <insurancePlanData> -
		 * <insuranceDisallowAutofill>Y</insuranceDisallowAutofill>
		 */

		String isSpecialtyDrug = null;
		String isDrugDisAllowAutoFill = null;
		String isSyncScriptEnrolled = null;
		String isPatientDisallowAutofill = null;
		String isPrescriberDisallowAutofill = null;
		String isInsuranceDisallowAutofill = null;
		String cvIsAutoFillDisAllowed = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		String langCode = Constants.LANG_CODE;
		try {
			cvIsAutoFillDisAllowed = ConfigReader.readConfig(clientId, langCode, ConfigKeys.CVISAUTOFILLDISALLOWED);
			isSpecialtyDrug = !MscriptsStringUtils.isCollectionEmptyOrNull(patientNotification.getTransactionData())
					&& null != patientNotification.getTransactionData().get(0)
					&& !MscriptsStringUtils
							.isStringEmptyOrNull(patientNotification.getTransactionData().get(0).getSpecialtyDrug())
									? patientNotification.getTransactionData().get(0).getSpecialtyDrug()
									: cvIsAutoFillDisAllowed;
			isDrugDisAllowAutoFill = !MscriptsStringUtils
					.isCollectionEmptyOrNull(patientNotification.getTransactionData())
					&& null != patientNotification.getTransactionData().get(0)
					&& !MscriptsStringUtils.isStringEmptyOrNull(
							patientNotification.getTransactionData().get(0).getDrugDisallowAutofill())
									? patientNotification.getTransactionData().get(0).getDrugDisallowAutofill()
									: cvIsAutoFillDisAllowed;
			isSyncScriptEnrolled = !MscriptsStringUtils
					.isCollectionEmptyOrNull(patientNotification.getTransactionData())
					&& null != patientNotification.getTransactionData().get(0)
					&& !MscriptsStringUtils.isStringEmptyOrNull(
							patientNotification.getTransactionData().get(0).getSyncScriptEnrolled())
									? patientNotification.getTransactionData().get(0).getSyncScriptEnrolled()
									: cvIsAutoFillDisAllowed;
			isPatientDisallowAutofill = !MscriptsStringUtils
					.isCollectionEmptyOrNull(patientNotification.getPatientData())
					&& null != patientNotification.getPatientData().get(0)
					&& !MscriptsStringUtils.isStringEmptyOrNull(
							patientNotification.getPatientData().get(0).getPatientDisallowAutofill())
									? patientNotification.getPatientData().get(0).getPatientDisallowAutofill()
									: cvIsAutoFillDisAllowed;
			isPrescriberDisallowAutofill = !MscriptsStringUtils
					.isCollectionEmptyOrNull(patientNotification.getPrescriberData())
					&& null != patientNotification.getPrescriberData().get(0)
					&& !MscriptsStringUtils.isStringEmptyOrNull(
							patientNotification.getPrescriberData().get(0).getPrescriberDisallowAutofill())
									? patientNotification.getPrescriberData().get(0).getPrescriberDisallowAutofill()
									: cvIsAutoFillDisAllowed;
			isInsuranceDisallowAutofill = null != patientNotification.getInsurancePlanData() && !MscriptsStringUtils
					.isStringEmptyOrNull(patientNotification.getInsurancePlanData().getInsuranceDisallowAutofill())
							? patientNotification.getInsurancePlanData().getInsuranceDisallowAutofill()
							: cvIsAutoFillDisAllowed;

			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_MSCRIPTS_AUTO_FILL_CRITERIA,
					new Object[] { rxNumber, storeNcpdpId, prescriptionId, isSpecialtyDrug, isDrugDisAllowAutoFill,
							isSyncScriptEnrolled, isPatientDisallowAutofill, isPrescriberDisallowAutofill,
							isInsuranceDisallowAutofill, customerId, customerId });

		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while updating Mscripts Auto Fill Criteria {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to insert data into ext_req_handler_blocked_details
	 * table if notification is blocked
	 * 
	 * @param clientId
	 * @param customerId
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param transactionData
	 * @param blockType
	 * @param blockReason
	 */
	public void insertIntoExternalRequestHandlerBlockedDetails(String clientId, String customerId,
			List<PatientData> patientData, String finalEncryptionKey, List<TransactionData> transactionData,
			String blockType, String blockReason) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_EXT_REQ_HANDLER_BLOCKED_DETAILS,
					new Object[] { clientId, customerId, patientData.get(0).getPatientFirstName(), finalEncryptionKey,
							patientData.get(0).getPatientLastName(), finalEncryptionKey,
							transactionData.get(0).getTxNumber(), blockType, blockReason,
							Constants.UPDATED_BY_EOPN_STRING, Constants.UPDATED_BY_EOPN_STRING });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error while inserting into ext_req_handler_blocked_details {}", ex.getMessage());
			throw new MscriptsException(clientId, ex.getMessage(), ex, errorSeverity);
		}
	}

	/**
	 * This method is used to delete entries from bulk rx pickup instances
	 * 
	 * @param patientDetailsMap
	 * @param clientId
	 */
	public void deleteBulkRxPickupInstances(Map<String, String> patientDetailsMap, String clientId)
			throws MscriptsException {
		String langCode = Constants.LANG_CODE;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvtCommNameOutOfStock = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMEOUTOFSTOCK);
			String cvtCommNameRxRejected = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMERXREJECTED);
			String cvtCommNameRxAdjudicationComplete = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMERXADJUDICATIONCOMPLETE);
			String cvtCommNameRxReadyInStoreWithCopay = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CV_COMM_NAME_RX_READY_INSTORE_WITH_COPAY);
			String cvtCommNameRxReadyWithCopayDeliveryLink = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CV_COMM_NAME_RX_READY_WITH_COPAY_DELIVERY_LINK);
			String cvtCommNameRxReadyInStore = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMERXREADYINSTORE);
			String cvtCommNameRxReadyInStoreFinal = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMERXREADYINSTOREFINAL);
			String cvtCommNameRxReadyDeliveryLinkFinal = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CV_COMM_NAME_RX_READY_DELIVERY_LINK_FINAL);
			String cvtCommNamePartialFill = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMEPARTIALFILL);
			String cvtCommNamePartialFillOnHold = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMEPARTIALFILLONHOLD);
			String cvtCommNameCentralFillDelayed = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMECENTRALFILLDELAYED);
			String cvtCommNameThirdPartyException = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMETHIRDPARTYEXCEPTION);
			String cvtCommNameCallPrescriber = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMECALLPRESCRIBER);
			String cvtCommNameEPrescriptionReceived = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED);
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.DELETE_BULK_RX_PICKUP_INSTANCES,
					new Object[] { patientDetailsMap.get(Constants.PRESCRIPTION_ID), clientId, cvtCommNameOutOfStock,
							cvtCommNamePartialFill, cvtCommNameRxRejected, cvtCommNameRxAdjudicationComplete,
							cvtCommNameRxReadyInStoreWithCopay, cvtCommNameRxReadyInStore,
							cvtCommNameRxReadyWithCopayDeliveryLink, cvtCommNameRxReadyInStoreFinal,
							cvtCommNameRxReadyDeliveryLinkFinal, cvtCommNamePartialFillOnHold,
							cvtCommNameCentralFillDelayed, cvtCommNameThirdPartyException, cvtCommNameCallPrescriber,
							cvtCommNameEPrescriptionReceived });
		} catch (MscriptsException e) {
			LOGGER_NON_PHI.error("Error while deleting data from bulk rx pickup instances {} MscriptsException",
					e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error while deleting data from bulk rx pickup instances {} Exception",
					ex.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, ex.getMessage(), ex, errorSeverity);
		}
	}

	/**
	 * This function is used to insert data into Patient_rx_txn table
	 * 
	 * @param patientDetailsMap
	 * @param contactReasonStatus
	 * @param customerId
	 * @param promiseTime
	 * @param transactionData
	 * @param dawCode
	 * @param sTotalQuantity
	 * @param isSyncScriptEnrolled
	 * @param nextSyncFillDate
	 * @param iAutoFillenabled
	 */
	public void insertIntoPatientRxTxn(Map<String, String> patientDetailsMap, String contactReasonStatus,
			String customerId, String promiseTime, List<TransactionData> transactionData, DawCode dawCode,
			String sTotalQuantity, boolean isSyncScriptEnrolled, String nextSyncFillDate, int iAutoFillenabled)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_REFILL_TRANSACTION_STATE,
					new Object[] { patientDetailsMap.get(Constants.PRESCRIPTION_ID), contactReasonStatus, customerId,
							customerId, promiseTime, transactionData.get(0).getRefillNum(),
							transactionData.get(0).getQuantityDispensed(), transactionData.get(0).getDrugUnit(),
							sTotalQuantity, transactionData.get(0).getDaysSupply(), dawCode.getDawCodeId(),
							isSyncScriptEnrolled ? 1 : 0, nextSyncFillDate, iAutoFillenabled,
							transactionData.get(0).getPartialFillStatus() });
		} catch (MscriptsException e) {
			LOGGER_NON_PHI.error("Error while inserting data into patient rx txn {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to update data in Patient_rx_txn table
	 * 
	 * @param patientDetailsMap
	 * @param contactReasonStatus
	 * @param customerId
	 * @param promiseTime
	 * @param transactionData
	 * @param dawCode
	 * @param sTotalQuantity
	 * @param isSyncScriptEnrolled
	 * @param nextSyncFillDate
	 * @param iAutoFillenabled
	 */
	public void updatePatientRxTxn(Map<String, String> patientDetailsMap, String contactReasonStatus, String customerId,
			String promiseTime, List<TransactionData> transactionData, DawCode dawCode, String sTotalQuantity,
			boolean isSyncScriptEnrolled, String nextSyncFillDate, int iAutoFillenabled) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_PATIENT_RX_TXN,
					new Object[] { contactReasonStatus, customerId, promiseTime, transactionData.get(0).getRefillNum(),
							transactionData.get(0).getQuantityDispensed(), transactionData.get(0).getDrugUnit(),
							sTotalQuantity, transactionData.get(0).getDaysSupply(), dawCode.getDawCodeId(),
							isSyncScriptEnrolled ? 1 : 0, nextSyncFillDate, iAutoFillenabled, null,
							transactionData.get(0).getPartialFillStatus(),
							patientDetailsMap.get(Constants.PRESCRIPTION_ID) });
		} catch (MscriptsException e) {
			LOGGER_NON_PHI.error("Error while inserting data into patient rx txn {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is use to get client id
	 * 
	 * @param transactionData
	 * @param pharmacyData
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getClientId(List<TransactionData> transactionData, List<PharmacyData> pharmacyData)
			throws MscriptsException {
		Map<String, String> clientIdMap = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String langCode = Constants.LANG_CODE;
			String cvPrescriptionStatusActive = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVPRESCRIPTIONSTATUSACTIVE);
			clientIdMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_CLIENT_ID,
					new Object[] { transactionData.get(0).getPrescriptionNumber(),
							pharmacyData.get(0).getPharmacyNCPDP(), cvPrescriptionStatusActive });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting client id {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return clientIdMap;

	}

	/**
	 * This function is used to get customer details when client id is not found
	 * 
	 * @param dateOfBirth
	 * @param finalEncryptionKey
	 * @param patientData
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getCustomersDetailsWithoutClientId(String dateOfBirth, String finalEncryptionKey,
			List<PatientData> patientData) throws MscriptsException {
		Map<String, String> customerDetailsWithoutClientId = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			customerDetailsWithoutClientId = patientNotificationDao.jdbcDataGet(
					QueryBuilder.GET_CUSTOMER_DETAILS_WITHOUT_CLIENTID, new Object[] { dateOfBirth, finalEncryptionKey,
							Constants.NUMERIC_TRUE_STRING, patientData.get(0).getRxcomPatientID() });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting customer details without client id {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return customerDetailsWithoutClientId;
	}

	/**
	 * This function is used to find customer details when client id is found
	 * 
	 * @param dateOfBirth
	 * @param finalEncryptionKey
	 * @param patientData
	 * @param transactionData
	 * @param pharmacyData
	 * @param clientId
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getCustomerDetails(String finalEncryptionKey, List<PatientData> patientData,
			List<TransactionData> transactionData, List<PharmacyData> pharmacyData, String clientId)
			throws MscriptsException {
		Map<String, String> customerDetails = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvDateFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,ConfigKeys.CVDATEFORMATDOB);
			String dateOfBirth = MiscUtils.dateFormat(patientData.get(0).getPatientBirthDate(), cvDateFormatDOB, cvDateFormatDOB);
			customerDetails = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_CUSTOMER_DETAILS,
					new Object[] { dateOfBirth, finalEncryptionKey,
							patientData.get(0).getPatientFirstName().toUpperCase(), finalEncryptionKey,
							patientData.get(0).getPatientLastName().toUpperCase(), finalEncryptionKey,
							Constants.NUMERIC_TRUE_STRING, transactionData.get(0).getPrescriptionNumber(),
							pharmacyData.get(0).getPharmacyNCPDP(), clientId });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting customer details  {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return customerDetails;
	}

	/**
	 * This function is used to get data from Patient_rx_txn table
	 * 
	 * @param patientDetailsMap
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getPatientRxTxn(Map<String, String> patientDetailsMap) throws MscriptsException {
		Map<String, String> patientRxTxn = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientRxTxn = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_PATIENT_RX_TXN,
					new Object[] { patientDetailsMap.get(Constants.PRESCRIPTION_ID) });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting patient rx txn  {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return patientRxTxn;
	}

	/**
	 * This function is used to update Patient_rx_txn table
	 * 
	 * @param isSyncScriptEnrolled
	 * @param customerId
	 * @param nextSyncFillDate
	 * @throws MscriptsException
	 */
	public void updatePatientAllRxTxn(boolean isSyncScriptEnrolled, String customerId, String nextSyncFillDate)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_PATIENT_ALL_RX_TXN,
					new Object[] { isSyncScriptEnrolled ? 1 : 0, customerId, nextSyncFillDate, customerId });
		} catch (MscriptsException e) {
			LOGGER_NON_PHI.error("Error while updating patient all rx txn {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to update sync date for all rx under particular
	 * store for customer
	 * 
	 * @param patientDetailsMap
	 * @throws MscriptsException
	 */
	public void updateSyncDateForAllRxUnderStoreForCustomer(Map<String, String> patientDetailsMap)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_SYNC_DATE_ALL_RX_UNDER_STORE_FOR_CUSTOMER,
					new Object[] { patientDetailsMap.get(Constants.PRESCRIPTION_ID) });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while updating patient all rx txn {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to get user time zone
	 * 
	 * @param customerId
	 * @return
	 * @throws MscriptsException
	 */
	public Map getUserTimeZone(String customerId) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Map userTimeZone = null;
		try {
			userTimeZone = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_USER_TIMEZONE,
					new Object[] { customerId });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while getting user time zone {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return userTimeZone;
	}

	/**
	 * This function is used to delete patient rx txn median table
	 * 
	 * @param transactionData
	 * @param pharmacyData
	 * @throws MscriptsException
	 */
	public void deletePatientRxTxnMedian(List<TransactionData> transactionData, List<PharmacyData> pharmacyData)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.DELETE_PATIENT_RX_TXN_MEDIAN, new Object[] {
					transactionData.get(0).getPrescriptionNumber(), pharmacyData.get(0).getPharmacyNCPDP() });
		} catch (MscriptsException e) {
			LOGGER_NON_PHI.error("Error while deleting data from patient rx txn median {}", e.getMessage());
			throw new MscriptsException(Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to insert data into EXT_REQ_HANDLER_AUDITS table
	 * 
	 * @param clientId
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param requestString
	 * @param responseStatus
	 * @param patientNotificationResponse
	 * @param customerId
	 * @throws MscriptsException
	 */
	public void insertIntoExternalRequestHandlerAuditsWSO2(String clientId, List<PatientData> patientData,
			String finalEncryptionKey, String requestString, String responseStatus,
			PatientNotificationResponse patientNotificationResponse, String customerId) throws MscriptsException {
		String langCode = Constants.LANG_CODE;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvPatientnotify = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTPATIENTNOTIFY);
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_INTO_EXT_REQ_HANDLER_AUDITS_WSO2,
					new Object[] { clientId, cvPatientnotify, null, patientData.get(0).getContactPhoneNumber(),
							finalEncryptionKey, patientData.get(0).getPatientFirstName().toUpperCase(),
							finalEncryptionKey, patientData.get(0).getPatientLastName().toUpperCase(),
							finalEncryptionKey, requestString, finalEncryptionKey, responseStatus, null,
							patientNotificationResponse.toString(), null, Constants.UPDATED_BY_EOPN_STRING,
							Constants.UPDATED_BY_EOPN_STRING, null, Constants.NUMERIC_FALSE_STRING, customerId, null });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while inserting data into External Request Handler Autids WSO2 table {}",
					e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function is used to insert data into bulk rx table
	 * 
	 * @param clientId
	 * @param transactionData
	 * @param pharmacyData
	 * @param lastFilledDate
	 * @param patientDetailsMap
	 * @param bIsBackDatedMessage
	 * @param nextStartDate
	 * @param startDate
	 * @param customerId
	 * @param sendCommunicationMode
	 * @param communicationIdMap
	 * @throws MscriptsException
	 */
	public void insertIntoBulkRx(String clientId, List<TransactionData> transactionData,
			List<PharmacyData> pharmacyData, String lastFilledDate, Map<String, String> patientDetailsMap,
			boolean bIsBackDatedMessage, String nextStartDate, String startDate, String customerId,
			SendCommunicationMode sendCommunicationMode, Map<String, String> communicationIdMap)
			throws MscriptsException {
		String langCode = Constants.LANG_CODE;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvtRefillStatusFilled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTREFILLSTATUSFILLED);
			String cvReadyNotificationUpdatedByEopn = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVREADYNOTIFICATIONUPDATEDBYEOPN);
			patientNotificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_BULK_RX,
					new Object[] { clientId, transactionData.get(0).getPrescriptionNumber(),
							pharmacyData.get(0).getPharmacyNCPDP(), pharmacyData.get(0).getPharmacyNCPDP(),
							lastFilledDate, cvtRefillStatusFilled, transactionData.get(0).getTxNumber(), null,
							patientDetailsMap.get(Constants.PRESCRIPTION_ID),

							bIsBackDatedMessage ? startDate : nextStartDate,
							bIsBackDatedMessage ? startDate : nextStartDate, customerId,
							cvReadyNotificationUpdatedByEopn, cvReadyNotificationUpdatedByEopn,
							sendCommunicationMode.getSendSms(), sendCommunicationMode.getSendEmail(),
							sendCommunicationMode.getSendApns(), sendCommunicationMode.getSendGcms(),
							sendCommunicationMode.getSendIvr(), patientDetailsMap.get(Constants.PRESCRIPTION_CODE),
							Constants.NUMERIC_FALSE_STRING, Constants.NUMERIC_TRUE_STRING,
							communicationIdMap.get("id") });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error while inserting data into bulk rx table {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}

	}

	/**
	 * This function is used to update express checkout
	 * 
	 * @param patientDetailsMap
	 * @throws MscriptsException
	 */
	public void updateExpressCheckoutAndLogRxSoldStatus(Map<String, String> patientDetailsMap)
			throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			queryInvoker.executeQuery(QueryBuilder.UPDATE_EXPRESS_CHECKOUT_LOG_RX_SOLD_STATUS,
					new Object[] { patientDetailsMap.get(Constants.PRESCRIPTION_ID) });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while processing updateExpressCheckoutLogRxSoldStatus Exception {}",
					ex);
			throw new MscriptsException(Constants.cvDefaultClientId, ex.getMessage(), ex, errorSeverity);
		}
	}

	/**
	 * This function is used to get next med sync date
	 * 
	 * @param customerTextPreference
	 * @param nextSyncFillDate
	 * @param clientId
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getNextMedSyncDate(Map<String, String> customerTextPreference, String nextSyncFillDate,
			String clientId) throws MscriptsException {
		String langCode = Constants.LANG_CODE;
		Map<String, String> nextMedsyncDateMap = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvVarHowManyDaysEarlyToSendNextSyncFill = ConfigReader.readConfig(clientId, langCode,
					ConfigKeys.CVVARHOWMANYDAYSEARLYTOSENDNEXTSYNCFILL);
			nextMedsyncDateMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_NEXT_MEDSYNC_DATE, new Object[] {
					customerTextPreference.get(Constants.TIME_ZONE), nextSyncFillDate,
					cvVarHowManyDaysEarlyToSendNextSyncFill, nextSyncFillDate, cvVarHowManyDaysEarlyToSendNextSyncFill,
					customerTextPreference.get(Constants.SEND_REMINDER_HOUR),
					customerTextPreference.get(Constants.TIME_ZONE), customerTextPreference.get(Constants.TIME_ZONE),
					customerTextPreference.get(Constants.SEND_REMINDER_HOUR),
					customerTextPreference.get(Constants.TIME_ZONE), customerTextPreference.get(Constants.TIME_ZONE),
					customerTextPreference.get(Constants.TIME_ZONE), customerTextPreference.get(Constants.TIME_ZONE),
					customerTextPreference.get(Constants.TIME_ZONE), customerTextPreference.get(Constants.TIME_ZONE),
					customerTextPreference.get(Constants.SEND_REMINDER_HOUR),
					customerTextPreference.get(Constants.TIME_ZONE) });
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while getting next medsync date {}", e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return nextMedsyncDateMap;

	}

	/**
	 * This function return true or false base on customer has enabled or
	 * disabled the notification for the given contact reason and if
	 * notification is disabled it create the entry in external request handler
	 * blocked details table
	 * 
	 * @param clientId
	 * @param customerId
	 * @param notificationTypeKey
	 * @param patientData
	 * @param finalEncryptionKey
	 * @param transactionData
	 * @return
	 * @throws MscriptsException
	 */
	public boolean blockAndDeleteIfCustomerNotificationDisabled(String clientId, String customerId, String notificationTypeKey,
			List<PatientData> patientData, String finalEncryptionKey, List<TransactionData> transactionData,
			Map<String, String> patientDetailsMap) throws MscriptsException {
		String langCode = Constants.LANG_CODE;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			String cvBlockReasonNotificationDisabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKREASONNOTIFICATIONDISABLED);
			String cvBlockTypeDrugExcluded = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVBLOCKTYPEDRUGEXCLUDED);
			boolean bisDrugExcluded = !notificationFilterUtil.isCustomerNotifcationEnabled(clientId, customerId,
					notificationTypeKey);
			if (bisDrugExcluded) {
				insertIntoExternalRequestHandlerBlockedDetails(clientId, customerId, patientData, finalEncryptionKey,
						transactionData, cvBlockTypeDrugExcluded, cvBlockReasonNotificationDisabled);
				/*
				 * Deleting the older scheduled Notification if any
				 */
				deleteBulkRxPickupInstances(patientDetailsMap, clientId);
			}
			return bisDrugExcluded;
		} catch (Exception e) {
			LOGGER_NON_PHI
					.info("Exception while logging blocked notification dateails in ext_req_handler_blocked_details table when customer"
							+ "has blocked notification for {}", notificationTypeKey);
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
	}

	/**
	 * This function maps the common placeholder's for each message template
	 *
	 * 
	 * @param customerPreference
	 * @param patientData
	 * @return map of communicationContent (client_name, customer_firstName,
	 *         AddressLine_1,StorePhoneNumber,Store_name)
	 * @throws MscriptsException
	 */
	public Map<String, String> prepareCommonCommunicationContent(Map<String, String> customerPreference,
			List<PatientData> patientData) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Map<String, String> communicationContent = new HashMap<>();
		try {
		communicationContent.put(CommunicationTemplateStrings.CLIENT_NAME.getTemplateString(),
				customerPreference.get(Constants.CLIENT_NAME));
		communicationContent.put(CommunicationTemplateStrings.CUSTOMER_FIRST_NAME.getTemplateString(),
				patientData.get(0).getPatientFirstName().toUpperCase());
		communicationContent.put(CommunicationTemplateStrings.STORE_ADDRESS_LINE1.getTemplateString(),
				customerPreference.get(Constants.ADDRESS_LINE_1));
		communicationContent.put(CommunicationTemplateStrings.STORE_PHONE_NUMBER.getTemplateString(),
				customerPreference.get(Constants.STORE_PHONE_NUMBER));
		communicationContent.put(CommunicationTemplateStrings.STORE_NAME.getTemplateString(),
				customerPreference.get(Constants.STORE_NAME));
		communicationContent.put(CommunicationTemplateStrings.STORE_CITY.getTemplateString(),
				customerPreference.get(Constants.STORE_CITY_PREFERENCE));
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while mapping placeholders for message template content", e.getMessage());
			throw new MscriptsException( Constants.cvDefaultClientId, e.getMessage(), e, errorSeverity);
		}
		return communicationContent;
	}

	/**
	 * Method to check if the message was sent to the customer with the given communication id within the sepecified time
	 * @param customerId
	 * @param clientId
	 * @param communicationName
	 * @return
	 * @throws MscriptsException
	 */
	public Map<String, String> getPrescNotificationTypeCountWithinSpecifiedHoursByCustomerId(String customerId,
			String clientId, String communicationName) throws MscriptsException {
		String methodName = "getPrescNotificationTypeCountWithinSpecifiedHoursByCustomerId";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Map<String, String> countMessagesMap = null;
		Calendar calendar = null;
		SimpleDateFormat formatter = null;
		String langCode = Constants.LANG_CODE;

		try {
			String cvTimeZoneUTC = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVTIMEZONEUTC);
			String cvDateTimeFormatDOB = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode,
					ConfigKeys.CVDATETIMEFORMATDOB);

			/*
			 * Check whether we have sent a particular type of communication
			 * within a period specified in conifugration:
			 * cvHowLongInHoursBeforeSendingSameNotificationForPresc. Done to
			 * avoid multiple messages for a prescription within a period of
			 * time
			 */

			String cvHowLongInHoursBeforeSendingSameNotificationForPresc = ConfigReader.readConfig(
					Constants.cvDefaultClientId, langCode,
					ConfigKeys.CV_HOW_LONG_IN_HOURS_BEFORE_SENDING_SAME_NOTIFICATION_FOR_PRESC);

			int howLongInHoursBeforeSendingSameNotificationForPresc = Integer
					.parseInt(cvHowLongInHoursBeforeSendingSameNotificationForPresc);

			calendar = Calendar.getInstance(TimeZone.getTimeZone(cvTimeZoneUTC));
			calendar.add(Calendar.HOUR, -howLongInHoursBeforeSendingSameNotificationForPresc);
			formatter = new SimpleDateFormat(cvDateTimeFormatDOB);
			formatter.setTimeZone(TimeZone.getTimeZone(cvTimeZoneUTC));
			String date = formatter.format(calendar.getTime());
			LOGGER_NON_PHI.info("Date to be compared = {}", date);
		
			countMessagesMap = patientNotificationDao.jdbcDataGet(QueryBuilder.GET_DAILY_COUNT_MESSAGE_BY_CUSTOMER_ID,
					new Object[] { date, customerId, communicationName, clientId });

		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in method {} : {}", methodName, e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		return countMessagesMap;
		
	}
	
	
	/**
	 * Method to generate the order url if text based delivery is enabled
	 * @param clientId
	 * @param customerId
	 * @param mobile
	 * @return
	 * @throws MscriptsException 
	 */
	public String getOrderUrl(String clientId, String customerId, String mobile) throws MscriptsException {
		String methodName = "EOPNUtils.getOrderUrl()";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		String shortenedURL = new String();
		
		try {
			Map orderUrl= notificationDao.getOrderUrl(clientId, customerId);

			boolean isShared = false;
			if (null == orderUrl.get(Constants.ORDER_URL)
					|| orderUrl.get(Constants.ORDER_URL).toString().isEmpty()
					|| orderUrl.get(Constants.ORDER_URL).toString().equalsIgnoreCase(Constants.INVALID)) {
				Map map = mscriptsCommonService.isSharedMobile(clientId, "1", mobile.substring(1), null, null);
				if ("TRUE".equalsIgnoreCase((String) map.get("exists"))
						&& "TRUE".equalsIgnoreCase((String) map.get("is_shared"))) {
					isShared = true;
				}
				String cvOrderUrlExpirationPeriod = ConfigReader.readConfig(clientId,
						Constants.DEFAULT_LANGUAGE, ConfigKeys.CV_ORDER_URL_EXPIRATION_PERIOD);
				// Generate SHA Code which should be sent with the SMS
				String shaCode = new GenerateRandom().generateShaHashCode(customerId + mobile);
				String URL = notificationDao.updateMscriptsProxyAccessToken(clientId, customerId, "0", shaCode,
						"orderpage", Constants.DEFAULT_LANGUAGE);
				String clientName = ConfigReader
						.readConfig(clientId, Constants.DEFAULT_LANGUAGE, ConfigKeys.CV_CLIENT_NAME).toString();
				String shrStr = "&shr=0";
				if (isShared) {
					shrStr = "&shr=1";
				}
				String redirectParams = "?token=" + shaCode + shrStr;
				String cvURLShortnerServiceUrl = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
						ConfigKeys.CV_URL_SHORTNER_SERVICE_URL);
				String cvPatientPortalUrlShortnerAppName = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
						ConfigKeys.CV_PATIENT_PORTAL_URL_SHORTNER_APP_NAME);
				String cvCheckoutLinkExpiryURL = ConfigReader.readConfig(clientId, Constants.DEFAULT_LANGUAGE,
						ConfigKeys.CV_CHECKOUT_LINK_EXPIRY_URL);
				// String shortenedURL = null;
				Integer orderURLExpiryPeriod = ((cvOrderUrlExpirationPeriod != null
						&& cvOrderUrlExpirationPeriod.trim().length() != 0)
						? Integer.parseInt(cvOrderUrlExpirationPeriod)
								: 0);
				shortenedURL = urlShortnerService.URLShortnerServiceWithRedirectParamsAndExpUrl(
						cvURLShortnerServiceUrl, URL, clientName, cvPatientPortalUrlShortnerAppName, redirectParams,
						orderURLExpiryPeriod, cvCheckoutLinkExpiryURL);
				if (null != (orderUrl.get(Constants.ORDER_URL))
						&& orderUrl.get(Constants.ORDER_URL).toString().equalsIgnoreCase(Constants.INVALID)) {
					notificationDao.insertOrUpdateJdbcData(QueryBuilder.UPDATE_ORDER_URL,
							new Object[] { shortenedURL, cvOrderUrlExpirationPeriod, customerId, clientId, });
				} else {
					notificationDao.insertOrUpdateJdbcData(QueryBuilder.INSERT_CUSTOMER_ORDER_URL,
							new Object[] { customerId, clientId, shortenedURL, cvOrderUrlExpirationPeriod });
				}
			} else {
				shortenedURL = orderUrl.get(Constants.ORDER_URL).toString();
			}

			
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error in method {} : {}", methodName, e.getMessage());
			throw new MscriptsException(clientId, e.getMessage(), e, errorSeverity);
		}
		
		return shortenedURL;

	}
	
	
}
