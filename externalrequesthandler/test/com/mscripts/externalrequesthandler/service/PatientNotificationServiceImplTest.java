package com.mscripts.externalrequesthandler.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mscripts.commonintegrator.service.MessageService;
import com.mscripts.commonintegrator.service.impl.MessageServiceImpl;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.MscriptsCommonDao;
import com.mscripts.dao.QueryInvoker;
import com.mscripts.dao.jdbc.QueryInvokerImpl;
import com.mscripts.domain.SendCommunicationMode;
import com.mscripts.enums.DrugFilterType;
import com.mscripts.enums.NotificationTypeKey;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.PatientNotificationDao;
import com.mscripts.externalrequesthandler.domain.PatientData;
import com.mscripts.externalrequesthandler.domain.PatientNotificationRequest;
import com.mscripts.externalrequesthandler.domain.PatientNotificationResponse;
import com.mscripts.externalrequesthandler.domain.PharmacyData;
import com.mscripts.externalrequesthandler.domain.TransactionData;
import com.mscripts.externalrequesthandler.utils.EOPNUtils;
import com.mscripts.service.MscriptsCommonService;
import com.mscripts.txndata.domain.DawCode;
import com.mscripts.txndata.service.DawCodeService;
import com.mscripts.txndata.service.PatientRxTxnMedianService;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.DrugFilterCriteriaUtil;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.MscriptsStringUtils;
import com.mscripts.utils.NotificationFilterUtil;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.QueryBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigReader.class, MiscUtils.class})
@PowerMockIgnore({"javax.management.*"})
public class PatientNotificationServiceImplTest {

	private static final String RESPONSE_TYPE = "responseType";

	private static final String FIRST_REFILL = "firstRefill";

	private static final String PARTIAL_FILL = "partialFill";

    private static final String SUCCESS = "SUCCESS";

	private static final String EXCEPTION = "EXCEPTION";

	private static final String PRESCRIPTION_ID = "prescription_id";

	private static final String PARTIAL_FILL_STATUS = "partialFillStatus";

	@Mock
	private PHICredentials pHICredentials;

	@Mock
	private MessageService messageService = new MessageServiceImpl();

	@Mock
	private PatientNotificationDao patientNotificationDao;

	@Mock
	private QueryInvoker queryInvoker = new QueryInvokerImpl();

	@Mock
	private EOPNUtils eopnUtils;

	@Mock
	private DawCodeService dawCodeService;

	@Mock
	private MscriptsCommonService mscriptsCommonService;

	@Mock
	private PatientRxTxnMedianService patientRxTxnMedianService;
	
	@Mock
    private NotificationFilterUtil notificationFilterUtil;
	
	@Mock
	private MscriptsCommonDao mscriptsCommonDao;
	
	@Mock
	private DrugFilterCriteriaUtil drugFilterCriteriaUtil;
	

	@InjectMocks
	private PatientNotificationServiceImpl patientNotificationServiceImpl = new PatientNotificationServiceImpl();
	
	
	
	public static final String PARTIAL_FILL_ON_HOLD_STRING = "Rx Partial Fill and on Hold";
	public static final String CENTRAL_FILL_DELAYED_STRING = "Central Fill Order Delayed";
	public static final String THIRD_PARTY_EXCEPTION_STRING = "Third party exception";
	public static final String CALL_PRESCRIBER = "Call Prescriber";
	public static final String E_SCRIPT_RECEIVED = "E-Script Received";
	public static final String PRESCRIPTION_NUMBER = "3456789";
	public static final String TXN_NUMBER = "3456789";
	public static final String COMMUNICATION_NAME = "communicationName";
	public static final String CUSTOMER_ID = "customer_id";
	public static final String VAR_PICKUP_COUNT_FOR_TODAY = "varPickupCountForToday";
	public static final String SEND_MESSAGE_STATUS = "sendMessageStatus";
	public static final String CLIENT_ID = "client_id";
	public static final String RX_IN_PROCESS = "Rx In Process";
	public static final String RX_OUT_OF_STOCK = "Rx Out of Stock";
	public static final String RX_PARTIAL_FILL = "Rx Partial Fill";
	public static final String RX_REJECTED = "Rx Rejected";

	
	
	
	/**
	 * Test method for EOPN contact reason 1 when rx-number/prescription number is missing,
	 * we should send EOPN generic message.
	 * @throws Exception 
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNConcatReason1RxNumberMissing() throws Exception {
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVISPARTIALFILLMESSAGEENABLED))
		.thenReturn("1");
		
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_EOPN_GENERIC_MSG)).thenReturn("EOPN Generic Message");
		Map<String, String> requestMap = getRequestMap("1", null, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		String communicationName = "EOPN Generic Message";
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		patientNotification.getTransactionData().get(0).setPrescriptionNumber(null);
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			assertEquals("SUCCESS", actualResponse.getStatus());
			assertEquals("200", actualResponse.getHttpStatusCode());
		} catch (Exception exp) {
			fail(exp.getMessage());
		}
		
		
	}

	/**
	 * Test method for EOPN contact reason 7 when reminder mode is email
	 */
	@Test
	public void testProcessPatientNotification__ForEOPNContactReason7ViaEmail() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when reminder mode is text
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7ViaText() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	
	

	/**
	 * Test method for EOPN contact reason 7 when reminder mode is iospush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7ViaIospush() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.IOS_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when reminder mode is android push
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7ViAndroidpush() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.ANDROID_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when duplicate notification comes
	 * in
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7DuplicateMessage() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "1";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when notification comes before
	 * lower cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7BeforeLowerCutOffTime() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_LESS_THATN_LOWER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when notification comes after upper
	 * cutoff time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7AfterUpperCutOffTime() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when patient consent flag is turned
	 * off
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7PatientConsentFalse() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = false;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 7 when there is no data in
	 * patient_rx_txn table for the perescription
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason7InsertPatientRxTxn() {

		Map<String, String> requestMap = getRequestMap("7", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(true);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when reminder mode is email
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10ViaEmail() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when reminder mode is text
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10ViaText() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when reminder mode is iospush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10ViaIospush() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.IOS_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when reminder mode is androidpush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10ViAndroidpush() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.ANDROID_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when duplicate notification comes
	 * in
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10DuplicateMessage() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "1";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when notification comes before
	 * lower cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10BeforeLowerCutOffTime() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_LESS_THATN_LOWER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when notification comes after
	 * upper cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10AfterUpperCutOffTime() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when patient consent flag is
	 * turned off
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10PatientConsentFalse() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = false;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 10 when no data present in
	 * patient_rx_txn for prescription
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason10InsertPatientRxTxn() {

		Map<String, String> requestMap = getRequestMap("10", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CENTRAL_FILL_DELAYED_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(true);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when reminder mode is email
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12ViaEmail() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when reminder mode is text
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12ViaText() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when reminder mode is iospush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12ViaIospush() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.IOS_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when reminder mode is androidpush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12ViAndroidpush() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.ANDROID_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when duplicate notification comes
	 * in
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12DuplicateMessage() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "1";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when notification comes before
	 * lower cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12BeforeLowerCutOffTime() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_LESS_THATN_LOWER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when notification comes after
	 * upper cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12AfterUpperCutOffTime() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when patient consent flag is
	 * turned off
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12PatientConsentFalse() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = false;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 12 when no data present in
	 * patient_rx_txn table for prescription
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason12InsertPatientRxTxn() {

		Map<String, String> requestMap = getRequestMap("12", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = THIRD_PARTY_EXCEPTION_STRING;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(true);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when reminder mode is email
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13ViaEmail() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when reminder mode is text
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13ViaText() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when reminder mode is iospush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13ViaIospush() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.IOS_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when reminder mode is android push
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13ViAndroidpush() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.ANDROID_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when duplicate notification comes
	 * in
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13DuplicateMessage() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "1";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when notification comes before
	 * lower cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13BeforeLowerCutOffTime() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_LESS_THATN_LOWER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when notification comes after
	 * upper cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13AfterUpperCutOffTime() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when patient consent flag is
	 * turned off
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13PatientConsentFalse() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = false;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 13 when no data present in
	 * patient_rx_txn table for prescription
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason13InsertPatientRxTxn() {

		Map<String, String> requestMap = getRequestMap("13", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = CALL_PRESCRIBER;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(true);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when reminder mode is email
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14ViaEmail() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when reminder mode is text
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14ViaText() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when reminder mode is iospush
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14ViaIospush() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.IOS_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when reminder mode is android push
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14ViAndroidpush() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.ANDROID_PUSH;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	


	
	/**
	 * Test method for EOPN contact reason 14 when duplicate notifications comes
	 * in
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14DuplicateMessage() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "1";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when notification comes before
	 * lower cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14BeforeLowerCutOffTime() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_LESS_THATN_LOWER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when notification comes after
	 * upper cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14AfterUpperCutOffTime() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when patient consent flag is
	 * turned off
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14PatientConsentFalse() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = false;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when no data present in
	 * patient_rx_txn table for prescription
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14InsertPatientRxTxn() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(true);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when notification comes for the
	 * prescription which is not present in mscripts database
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14NewPrescription() {

		Map<String, String> requestMap = getRequestMap("14", "34567890", TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when notification comes in non
	 * blocking hours
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14NonBlockingHours() {

		Map<String, String> requestMap = getRequestMap("14", "34567890", TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when patient and prescription both
	 * doesn't exist in mscripts database
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14PatientNotFoundForNewPrescription() {

		Map<String, String> requestMap = getRequestMap("14", "3456790", TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(true);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(Constants.PATIENT_NOT_FOUND_ERROR_MSG,
					"ECEH025");
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for EOPN contact reason 14 when patient doesn't exist in
	 * mscripts database
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason14PatientNotFoundForExistingPrescription() {

		Map<String, String> requestMap = getRequestMap("14", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(true);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(Constants.PATIENT_NOT_FOUND_ERROR_MSG,
					"ECEH025");
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}


	/**
	 * function to return transactionStateMap it returns null if isMapNull flag
	 * is true else returns transactionStateMap with mocked values
	 * 
	 * @param isMapNull
	 * @return
	 */
	public Map<String, String> getTransactionStateMap(boolean isMapNull) {
		if (isMapNull)
			return null;
		else {
			Map<String, String> transactionStateMap = new HashMap<>();
			transactionStateMap.put("quantity", "30.0");
			transactionStateMap.put("total_quantity", null);
			transactionStateMap.put("id", "121");
			transactionStateMap.put("rx_txn_status_id", "3");
			transactionStateMap.put("promise_time", null);
			return transactionStateMap;
		}

	}

	/**
	 * function to return patientDetailsMap it returns null if isMapNull flag is
	 * true else returns patientDetailsMap with mocked values
	 * 
	 * @param isMapNull
	 * @return
	 */
	public Map<String, String> getPatientDetailsMap(boolean isMapNull) {
		if (isMapNull)
			return null;
		else {
			Map<String, String> patientDetailsMap = new HashMap<>();
			patientDetailsMap.put(PRESCRIPTION_ID, "3042");
			patientDetailsMap.put(CUSTOMER_ID, "1277");
			patientDetailsMap.put("prescription_code", "DC");
			return patientDetailsMap;
		}
	}

	/**
	 * function to createInputMap to mock the values
	 * 
	 * @param communicationName
	 * @param reminderMode
	 * @param varPickupCountForToday
	 * @param clientId
	 * @param sendMessageStatus
	 * @return
	 */
	public Map<String, String> createInputMap(String communicationName, String reminderMode,
			String varPickupCountForToday, String clientId, String sendMessageStatus) {
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(COMMUNICATION_NAME, communicationName);
		inputMap.put("reminderMode", reminderMode);
		inputMap.put(VAR_PICKUP_COUNT_FOR_TODAY, varPickupCountForToday);
		inputMap.put("clientId", clientId);
		inputMap.put(SEND_MESSAGE_STATUS, sendMessageStatus);
		return inputMap;

	}

	/**
	 * function to return clientIdMap it returns null if isMapNull flag is true
	 * else returns clientIdMap with mocked values
	 * 
	 * @param isMapNull
	 * @param inputMap
	 * @return
	 */
	public Map<String, String> getClientIdMap(boolean isMapNull, Map<String, String> inputMap) {
		if (isMapNull)
			return null;
		else {
			Map<String, String> clientIdMap = new HashMap<>();
			clientIdMap.put(CLIENT_ID, inputMap.get("clientId"));
			return clientIdMap;
		}

	}

	/**
	 * Function to mock EOPNUtils methods
	 * 
	 * @param patientNotification
	 * @param isPatientCommunicationConsentSatisfied
	 * @param transactionStateMap
	 * @param patientDetailsMap
	 * @param inputMap
	 * @param clientIdMap
	 * @throws Exception
	 */
	public void mockEOPNUtilsMethods(PatientNotificationRequest patientNotification,
			boolean isPatientCommunicationConsentSatisfied, Map<String, String> transactionStateMap,
			Map<String, String> patientDetailsMap, Map<String, String> inputMap, Map<String, String> clientIdMap)
			throws Exception {
		List<PatientData> patientData = patientNotification.getPatientData();
		List<PharmacyData> pharmacyData = patientNotification.getPharmacyData();
		List<TransactionData> transactionData = patientNotification.getTransactionData();
		String secondaryKey = "sq9KLYvH";
		String customerDob = "1990-01-01";
		String nextSyncFillDate = null;
		String encryptionKey = "encryptionKey";
		boolean isNotificationInSequence = true;
		String contactReasonStatus = null;
		String sAutoFillEnabled = transactionData.get(0).getAutoFillEnabled();
		int iPdxDawCode = Integer.parseInt(transactionData.get(0).getDawCode());
		String cvOtherReminderMappingID = "4";
		String lastFilledDate = "2020-03-26 07:39:57";
		String rxNameOrNumber = "XXX0065";

		when(pHICredentials.getSecondaryKey()).thenReturn(secondaryKey);

		Map<String, String> encryptionKeyMap = new HashMap<>();
		encryptionKeyMap.put(encryptionKey, "mscriptssq9KLYvH");

		DawCode.Builder builder = new DawCode.Builder();
		builder.dawCodeId(0);
		builder.dawCodeDesc("No Product Selection Indicated");
		builder.pdxIdentifier(0);
		builder.mcKessonIdentifier("0");
		DawCode dawCode = builder.build();
		String promiseTime = "2020-04-23 10:00:00";
		Map<String, String> countMessagesMap = new HashMap<>();
		countMessagesMap.put(VAR_PICKUP_COUNT_FOR_TODAY, inputMap.get(VAR_PICKUP_COUNT_FOR_TODAY));
		Map<String, String> customerPreference = new HashMap<>();
		customerPreference.put("shortcode_username", "redhot_32407e");
		customerPreference.put("email_verified", "1");
		customerPreference.put("storeNcpdpId", "0025365");
		customerPreference.put("show_rx_name", "0");
		customerPreference.put("timezone", "US/Central");
		customerPreference.put("prefix", "pq");
		customerPreference.put("send_reminder_mode", inputMap.get("reminderMode"));
		customerPreference.put("shortcode", "25166");
		customerPreference.put("store_phone_number", "(425) 827-1693");
		customerPreference.put("shortcode_serviceid", "58169");
		customerPreference.put("mobile_number_verified", "1");
		customerPreference.put("email_address", "ddsouza@mscripts.com");
		customerPreference.put("address_line_1", "123 120th Ave NE");
		customerPreference.put("store_name", "Core PDX Pharmacy");
		customerPreference.put("is_text_message_active", "1");
		customerPreference.put("mobile_number", "4156047502");
		customerPreference.put("client_name", "CoreQa-Pdx");
		customerPreference.put("storeCity", "Washington");
		Map<String, String> communicationIdMap = new HashMap<>();
		communicationIdMap.put("id", "1544");
		Map<String, String> notificationTimeMap = new HashMap<>();
		notificationTimeMap.put(SEND_MESSAGE_STATUS, inputMap.get(SEND_MESSAGE_STATUS));
		Map<String, String> patientDetailsWithoutClientIdMap = new HashMap<>();
		patientDetailsWithoutClientIdMap.put(PRESCRIPTION_ID, "3042");
		patientDetailsWithoutClientIdMap.put(CUSTOMER_ID, "1277");
		patientDetailsWithoutClientIdMap.put("prescription_code", "DC");
		patientDetailsWithoutClientIdMap.put(CLIENT_ID, "1");

		when(eopnUtils.getFinalKey(Constants.cvDefaultClientId, secondaryKey)).thenReturn(encryptionKeyMap);
		when(eopnUtils.getClientId(transactionData, pharmacyData)).thenReturn(clientIdMap);
		
	
		/**
		 * 1. first if condition has been put in place to test the scenario when
		 * patient and prescription both doesn't exist in mscripts database 2.
		 * Second else if condition has been put in place to test the scenario
		 * when patient is present in mscripts database but prescription is not
		 * present in mscripts database
		 */
		if (MscriptsStringUtils.isMapEmptyOrNull(clientIdMap)
				&& MscriptsStringUtils.isMapEmptyOrNull(patientDetailsMap)) {
			when(eopnUtils.getCustomersDetailsWithoutClientId(customerDob, encryptionKeyMap.get(encryptionKey),
					patientData)).thenReturn(patientDetailsMap);
		} else if (MscriptsStringUtils.isMapEmptyOrNull(clientIdMap)
				&& (!MscriptsStringUtils.isMapEmptyOrNull(patientDetailsMap))) {
			when(eopnUtils.getCustomersDetailsWithoutClientId(customerDob, encryptionKeyMap.get(encryptionKey),
					patientData)).thenReturn(patientDetailsWithoutClientIdMap);
		}
		/**
		 * 1. if condition is setting values to clientIdMap if clientIdMap comes
		 * as null to handle the null pointer exception 2. Else condition is
		 * calling getAllConfigsWithClientId method with the client id which
		 * comes in method parameter
		 */
		if (MscriptsStringUtils.isMapEmptyOrNull(clientIdMap)) {
			clientIdMap = new HashMap<>();
			clientIdMap.put(CLIENT_ID, "1");
		} else {
			getAllConfigsWithClientId(clientIdMap.get(CLIENT_ID));
		}
		when(eopnUtils.getCustomerDetails(encryptionKeyMap.get(encryptionKey), patientData,
				transactionData, pharmacyData, Constants.cvDefaultClientId)).thenReturn(patientDetailsMap);
		PowerMockito.mockStatic(MiscUtils.class);
		when(MiscUtils.dateFormat(anyString(), anyString(), anyString())).thenReturn(customerDob);
		when(eopnUtils.checkIfPrescNotificationInSequenceWithCurrentPrescriptionStatus(contactReasonStatus,
				transactionData.get(0).getPrescriptionNumber(), pharmacyData.get(0).getPharmacyNCPDP()))
						.thenReturn(isNotificationInSequence);

		when(eopnUtils.getRxAutoFillEnabledValue(sAutoFillEnabled)).thenReturn(0);
		when(dawCodeService.getDawCodeByIdentifier(iPdxDawCode, null)).thenReturn(dawCode);
		when(eopnUtils.getLastFilledDate(customerPreference.get(Constants.TIME_ZONE),
				transactionData.get(0).getLastFilledDate())).thenReturn(lastFilledDate);

		when(eopnUtils.checkNotificationTime(clientIdMap.get(CLIENT_ID), customerPreference.get(Constants.TIME_ZONE)))
				.thenReturn(notificationTimeMap);
		when(eopnUtils.checkRxnumberOrName(transactionData, customerPreference.get(Constants.SHOW_RX_NAME),
				clientIdMap.get(CLIENT_ID))).thenReturn(rxNameOrNumber);

		/**
		 * this if condition is present to handle the null pointer exception if
		 * patientDetailsMap comes as null in method parameter
		 */
		if (!MscriptsStringUtils.isMapEmptyOrNull(patientDetailsMap)) {

			when(eopnUtils.getLocalDatabaseTime(patientDetailsMap.get(CUSTOMER_ID),
					transactionData.get(0).getNextSyncFillDate())).thenReturn(nextSyncFillDate);
			when(eopnUtils.getPatientRxTxn(patientDetailsMap)).thenReturn(transactionStateMap);
			when(eopnUtils.getLocalDatabaseTime(patientDetailsMap.get(CUSTOMER_ID),
					transactionData.get(0).getPromiseTime())).thenReturn(promiseTime);
			when(eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHours(
					anyString(), anyString(),
					anyString())).thenReturn(countMessagesMap);
			when(eopnUtils.getCustomerPreference(encryptionKeyMap.get(encryptionKey),
					patientDetailsMap.get(CUSTOMER_ID), clientIdMap.get(CLIENT_ID), cvOtherReminderMappingID,
					pharmacyData.get(0).getPharmacyNCPDP())).thenReturn(customerPreference);
			when(eopnUtils.getCustomerCommunicationId(clientIdMap.get(CLIENT_ID), inputMap.get(COMMUNICATION_NAME),
					patientDetailsMap.get(CUSTOMER_ID))).thenReturn(communicationIdMap);

			when(eopnUtils.getLocalDatabaseTime(patientDetailsMap.get(CUSTOMER_ID),
					transactionData.get(0).getPromiseTime())).thenReturn(promiseTime);
			when(mscriptsCommonService.isPatientCommunicationConsentSatisfied(clientIdMap.get(CLIENT_ID),
					patientDetailsMap.get(CUSTOMER_ID))).thenReturn(isPatientCommunicationConsentSatisfied);
			when(patientNotificationDao.jdbcDataGet(QueryBuilder.GET_CUSTOMER_TEXT_INFO,
					new Object[] { "1", patientDetailsMap.get("customer_id"), "1" ,"4",pharmacyData.get(0).getPharmacyNCPDP()}))
							.thenReturn(customerPreference);
			when(eopnUtils.getPrescNotificationTypeCountWithinSpecifiedHoursByCustomerId(
					anyString(), anyString(),
					anyString())).thenReturn(countMessagesMap);

		}

	}

	/**
	 * Function to get all the configs with default client id
	 * 
	 * @throws Exception
	 */
	@Before
	public void getAllConfigsWithDefaultClientId() throws Exception {
		PowerMockito.mockStatic(ConfigReader.class);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONOUTOFSTOCK)).thenReturn("2");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONPARTIALFILL)).thenReturn("3");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVCONTACTREASONREADY))
				.thenReturn("4");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONRXREJECTED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONRXADJUDICATIONCOMPLETE)).thenReturn("8");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVCONTACTREASONSOLD))
				.thenReturn("5");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONONDEMAND)).thenReturn("9");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONPARTIALFILLONHOLD)).thenReturn("7");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONCENTRALFILLDELAYED)).thenReturn("10");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONTHIRDPARTYEXCEPTION)).thenReturn("12");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONCALLPRESCRIBER)).thenReturn("13");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONEPRESCRIPTIONRECEIVED)).thenReturn("14");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVDATETIMEFORMATDOB))
				.thenReturn("yyyy-MM-dd HH:mm:ss");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVDATETIMEFORMATINPUTPARSED)).thenReturn("yyyy-MM-dd'T'HH:mm:ss");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVTIMEFORMATUSER))
				.thenReturn("h:mm a");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVPARTIALFILLSTATUSPARTIAL)).thenReturn("P");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVPARTIALFILLSTATUSCOMPLETED)).thenReturn("C");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVPARTIALFILLSTATUSNEW)).thenReturn("N");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTREFILLSTATUSFILLED)).thenReturn("Ready");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVUPDATEPROMISETIMEENABLED)).thenReturn("0");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVCONTACTREASONCANCELLEDOUTOFWILLCALL)).thenReturn("6");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVSAVEPATIENTCOMMUNICATIONCONSENT)).thenReturn("0");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPEDRUGEXCLUDED)).thenReturn("Drug Excluded");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONSCHEDULEDRUGEXCLUDED)).thenReturn(
						"Drug Filter is of Type Schedule which is in exclusion list for sending notification");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONGPIDRUGEXCLUDED))
						.thenReturn("Drug Filter is of Type GPI which is in exclusion list for sending notification");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONNOTIFICATIONDISABLED)).thenReturn(
						"Drug is in exclusion list because Customer Notification Disabled for sending notification");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPEBACKDATEDMESSAGE)).thenReturn("Back Dated Message");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONBACKDATEDMESSAGE))
						.thenReturn("Stopped Sending Notification because of back dated message from PDX");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPEMOBILENOTVERIFIED)).thenReturn("Mobile Number Not Verified");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONMOBILENOTVERIFIED))
						.thenReturn("Stopped Sending Notification because customer Mobile Number is Not Verified");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPETEXTNOTACTIVE)).thenReturn("Text Message Not Active");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONTEXTNOTACTIVE))
						.thenReturn("Stopped Sending Notification because customer Text is Not Active");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVSENDREMINDERMODETEXT)).thenReturn(Constants.TEXT);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVSENDREMINDERMODEANDROIDPUSH)).thenReturn(Constants.ANDROID_PUSH);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVBLOCKTYPEPUSH))
				.thenReturn("Push");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVBLOCKREASONPUSH))
				.thenReturn("Stopped Sending Notification because customer opted for push");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVSENDREMINDERMODEIOSPUSH)).thenReturn(Constants.IOS_PUSH);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVSENDREMINDERMODEEMAIL)).thenReturn(Constants.EMAIL);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVBLOCKTYPEEMAIL))
				.thenReturn("Email");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVBLOCKREASONEMAIL))
				.thenReturn("Stopped Sending Notification because customer opted for Email");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPESTORENOTALLOWED)).thenReturn("Store Excluded");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONSTORENOTALLOWED))
						.thenReturn("Store is excluded from sending notification to the end user");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPESYNCSCRIPT)).thenReturn("Sync Script Enrolled");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONSYNCSCRIPT))
						.thenReturn("Stopped Sending Notification because script is enrolled for sync");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPECONSENTNOTGIVEN)).thenReturn("Communication Consent Not Given");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONCONSENTNOTGIVEN)).thenReturn(
						"Stopped Sending Notification because customer has not given communication consent");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVOTHERREMINDERMAPPINGID)).thenReturn("4");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVTIMEZONEUTC))
				.thenReturn("UTC");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISPARTIALFILLONHOLDMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISPARTIALFILLONHOLDCOMMUNICATIONENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISCENTRALFILLDELAYEDMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISCENTRALFILLDELAYEDCOMMUNICATIONENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISTHIRDPARTYEXCEPTIONMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISTHIRDPARTYEXCEPTIONCOMMUNICATIONENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISCALLPRESCRIBERMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISCALLPRESCRIBERCOMMUNICATIONENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISEPRESCRIPTIONRECEIVEDMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISEPRESCRIPTIONRECEIVEDCOMMUNICATIONENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED)).thenReturn(E_SCRIPT_RECEIVED);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVDATEFORMATDOB)).thenReturn("yyyy-MM-dd");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVPICKUPREMINDERMAPPINGID)).thenReturn("4");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISREJECTEDMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVISOUTOFSTOCKMESSAGEENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_EOPN_GENERIC_MSG)).thenReturn("");
	}

	/**
	 * function to get configs with give client id
	 * 
	 * @param clientId
	 * @throws Exception
	 */
	public void getAllConfigsWithClientId(String clientId) throws Exception {
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISADJUDICATIONMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISADJUDICATIONCOMMUNICATIONENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVCHECKMSCRIPTSAUTOFILLCRITERIA))
				.thenReturn("0");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMERXADJUDICATIONCOMPLETE))
				.thenReturn(RX_IN_PROCESS);
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, Constants.cvRoundTimeToNearestQuarter))
				.thenReturn("0");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISSTOREEXCEPTIONENABLED))
				.thenReturn("0");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISONDEMANDCOMMUNICATIONENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISEPRESCRIPTIONRECEIVEDMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE,
				ConfigKeys.CVISEPRESCRIPTIONRECEIVEDCOMMUNICATIONENABLED)).thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED))
				.thenReturn(E_SCRIPT_RECEIVED);
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMEONDEMANDMESSAGE))
				.thenReturn("On Demand Message");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVAPPRESOLVERPAGEURL)).thenReturn("");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISOUTOFSTOCKMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISOUTOFSTOCKCOMMUNICATIONENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISPARTIALFILLMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISREADYMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISREADYCOMMUNICATIONENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISREJECTEDMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISREJECTEDCOMMUNICATIONENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISSOLDMESSAGEENABLED)).thenReturn("1");

		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISCANLLEDOUTOFWILLCALLMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CV_COMM_NAME_RX_READY_INSTORE_WITH_COPAY))
				.thenReturn("RX Ready in Store with copay");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMERXREADYINSTORE))
				.thenReturn("RX Ready in Store");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMEPARTIALFILLONHOLD))
				.thenReturn(PARTIAL_FILL_ON_HOLD_STRING);
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMECENTRALFILLDELAYED))
				.thenReturn(CENTRAL_FILL_DELAYED_STRING);
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMETHIRDPARTYEXCEPTION))
				.thenReturn(THIRD_PARTY_EXCEPTION_STRING);
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMECALLPRESCRIBER))
				.thenReturn(CALL_PRESCRIBER);
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVPICKUPRESTOCKINGLIMIT))
				.thenReturn("10");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMEPARTIALFILL))
				.thenReturn("Rx Partial Fill");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMEOUTOFSTOCK))
				.thenReturn("Rx Out of Stock");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVTCOMMNAMERXREJECTED))
				.thenReturn("Rx Rejected");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVRXCANCELLOUTOFWILLCALLSTATUS))
				.thenReturn("Cancelled Out Of Will Call");
	
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVPARTIALFILLSTATUSNEW))
		.thenReturn("N");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVSENDRXREADYMSGONRECEIVINGEOPNREADYUPDATE))
		.thenReturn("1");
		
		
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVOUTOFSTOCKSTATUS))
				.thenReturn("Out Of Stock");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISREADYMESSAGEENABLED))
				.thenReturn("1");
		when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISPARTIALFILLCOMMUNICATIONENABLED))
				.thenReturn("1");
	}

	/**
	 * function to create the map to make the value configurable in request
	 * 
	 * @param contactReason
	 * @param prescriptionNumber
	 * @param txNumber
	 * @return
	 */
	public Map<String, String> getRequestMap(String contactReason, String prescriptionNumber, String txNumber) {
		Map<String, String> requestMap = new HashMap<>();
		requestMap.put("messageDate", "2020-04-13T12:43:16.830Z");
		requestMap.put("rxcomPatientID", "1213356");
		requestMap.put("patientLastName", "DEV");
		requestMap.put("patientFirstName", "PRIYA");
		requestMap.put("patientBirthDate", "1980-01-01T17:00:00Z");
		requestMap.put("patientZipCode", "98030");
		requestMap.put("pharmacyChainID", "3002");
		requestMap.put("pharmacyNCPDP", "0025365");
		requestMap.put("prescriptionNumber", prescriptionNumber);
		requestMap.put("txNumber", txNumber);
		requestMap.put("filledDate", "2020-04-13T12:39:57Z");
		requestMap.put("lastFilledDate", "2019-09-10T12:39:57Z");
		requestMap.put("refillsRemaining", "10");
		requestMap.put(PARTIAL_FILL_STATUS, "P");
		requestMap.put("contactReason", contactReason);
		return requestMap;
	}

	/**
	 * Function to get the expected response
	 * 
	 * @param errorMessage
	 * @param errorCode
	 * @return
	 */
	public PatientNotificationResponse getExpectedResponse(String errorMessage, String errorCode) {
		PatientNotificationResponse patientNotificationResponse = new PatientNotificationResponse();
		patientNotificationResponse.setHttpStatusCode("200");
		patientNotificationResponse.setError(null);
		patientNotificationResponse.setErrorCode(errorCode);
		patientNotificationResponse.setErrorId(null);
		patientNotificationResponse.setErrorMessage(errorMessage);
		patientNotificationResponse.setStatus(SUCCESS);
		return patientNotificationResponse;
	}
	
	/**
	 * Method returns the mock data for delivery details
	 * 
	 * @return List of text delivery enabled delivery details
	 */
	private List<Map<String, String>> getTextDeliveryEnabledDeliveryDetails() {
		List<Map<String, String>> deliveryDetailList = new ArrayList<>();
		Map<String, String> deliveryDetailMap = new HashMap<>();
		deliveryDetailMap.put(Constants.DELIVERY_DETAIL_ID, "1");
		deliveryDetailList.add(deliveryDetailMap);
		return deliveryDetailList;
	}

	/**
	 * Function to create request object of type PatientNotification from xml
	 * request string
	 * 
	 * @param reqString
	 * @return
	 */
	public PatientNotificationRequest getPatientNoficationRequest(String reqString) {
		PatientNotificationRequest patientNotificationRequest = null;
		final byte[] reqStringBytes = reqString.getBytes();
		InputStream inputStream = new ByteArrayInputStream(reqStringBytes);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(PatientNotificationRequest.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			patientNotificationRequest = (PatientNotificationRequest) jaxbUnmarshaller.unmarshal(inputStream);
			inputStream.close();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return patientNotificationRequest;
	}

	/**
	 * function to get XML request string
	 * 
	 * @param requestMap
	 * @return
	 */
	public String getRequestString(Map<String, String> requestMap) {
		return "<PatientNotificationRequest>" + "<patientData>" + "<rxcomPatientID>" + requestMap.get("rxcomPatientID")
				+ "</rxcomPatientID>" + "<patientLastName>" + requestMap.get("patientLastName") + "</patientLastName>"
				+ "<patientFirstName>" + requestMap.get("patientFirstName") + "</patientFirstName>"
				+ "<patientGender>F</patientGender>" + "<patientLanguage>en</patientLanguage>" + "<patientBirthDate>"
				+ requestMap.get("patientBirthDate") + "</patientBirthDate>" + "<patientRelation>1</patientRelation>"
				+ "<patientAddress1>Test</patientAddress1>" + "<patientCity>Catr</patientCity>"
				+ "<patientState>MI</patientState>" + "<patientZipCode>" + requestMap.get("patientZipCode")
				+ "</patientZipCode>" + "<contactWhenReady>N</contactWhenReady>"
				+ "<contactSMSNumber>4158619704</contactSMSNumber>" + "<loyaltyCardOptOut>N</loyaltyCardOptOut>"
				+ "<patientMobileService>Y</patientMobileService>" + "<creditCard>" + "<type>3</type>"
				+ "<lastFourDigits>6666</lastFourDigits>" + "<expiryDate>1221</expiryDate>" + "</creditCard>"
				+ "<communicationConsent>Y</communicationConsent>" + "<syncScriptEnrolled>E</syncScriptEnrolled>"
				+ "<patientDisallowAutofill>N</patientDisallowAutofill>" + "</patientData>" + "<pharmacyData>"
				+ "<pharmacyChainID>" + requestMap.get("pharmacyChainID") + "</pharmacyChainID>"
				+ "<pharmacyNPI>1249867379</pharmacyNPI>" + "<pharmacyNCPDP>" + requestMap.get("pharmacyNCPDP")
				+ "</pharmacyNCPDP>" + "</pharmacyData>" + "<prescriberData>"
				+ "<prescriberLastName>SMITH</prescriberLastName>" + "<prescriberFirstName>BARRY</prescriberFirstName>"
				+ "<prescriberMiddleName>L</prescriberMiddleName>"
				+ "<prescriberAddress1>1090 AMSTERDAM AVE STE 11B</prescriberAddress1>"
				+ "<prescriberCity>NEW YORK</prescriberCity>" + "<prescriberState>NY</prescriberState>"
				+ "<prescriberZipCode>10025</prescriberZipCode>"
				+ "<prescriberPhoneNumber>2125235898</prescriberPhoneNumber>" + "<prescriberID>"
				+ "<prescriberIDType>01</prescriberIDType>" + "<prescriberIDValue>1770568073</prescriberIDValue>"
				+ "</prescriberID>" + "<prescriberID>" + "<prescriberIDType>08</prescriberIDType>"
				+ "<prescriberIDValue>MI</prescriberIDValue>" + "</prescriberID>" + "</prescriberData>"
				+ "<transactionData>" + "<prescriptionNumber>" + requestMap.get("prescriptionNumber")
				+ "</prescriptionNumber>" + "<refillNum>1</refillNum>" + "<txNumber>" + requestMap.get("txNumber")
				+ "</txNumber>" + "<filledDate>" + requestMap.get("filledDate") + "</filledDate>"
				+ "<drugSchedule>6</drugSchedule>" + "<drugName>Patadine New</drugName>"
				+ "<productIDType>03</productIDType>" + "<productID>00178010102</productID>"
				+ "<drugGPI>30042010100870</drugGPI>" + "<drugMultiSource>N</drugMultiSource>"
				+ "<daysSupply>5</daysSupply>" + "<quantityDispensed>8.0</quantityDispensed>"
				+ "<drugUnit>TAB</drugUnit>" + "<writtenDate>2019-05-30T04:00:00Z</writtenDate>" + "<lastFilledDate>"
				+ requestMap.get("lastFilledDate") + "</lastFilledDate>" + "<refillsAuthorized>1</refillsAuthorized>"
				+ "<autoFillEnabled>N</autoFillEnabled>" + "<dawCode>0</dawCode>"
				+ "<originalFillDate>2019-11-20T12:26:12Z</originalFillDate>" + "<refillsRemaining>"
				+ requestMap.get("refillsRemaining") + "</refillsRemaining>"
				+ "<expirationDate>2020-05-29T04:00:00Z</expirationDate>" + "<rxOrigin>1</rxOrigin>"
				+ "<refillSource>8</refillSource>" + "<partialFillStatus>" + requestMap.get(PARTIAL_FILL_STATUS)
				+ "</partialFillStatus>" + "<willCallReadyDate>2020-01-26T12:43:16.850Z</willCallReadyDate>"
				+ "<fillLocation>L</fillLocation>" + "<contactReason>" + requestMap.get("contactReason")
				+ "</contactReason>" + "<promiseTime>2020-01-13T15:00:00Z</promiseTime>"
				+ "<nextSyncFillDate>2019-06-03T16:00:00Z</nextSyncFillDate>"
				+ "<drugDisallowAutofill>N</drugDisallowAutofill>" + "</transactionData>" + "<insurancePlanData>"
				+ "<bin>011651</bin>" + "<pcn>CASH</pcn>" + "<cardholderID>76552014164</cardholderID>"
				+ "<planType>CA</planType>" + "<planDisallowAutofill>N</planDisallowAutofill>" + "</insurancePlanData>"
				+ "</PatientNotificationRequest>";
	}
	
	/**
	 * Method to test checkDeliveryEnabledForClient method for success response when
	 * the delivery allowed flag is turned on
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCheckDeliveryEnabledForClientForSuccessResponse() throws Exception {
		// Mock config to specify delivery allowed or not
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVISDELIVERYALLOWED))
				.thenReturn("1");
		Map<String, String> params = new HashMap<>();
		params.put(PARTIAL_FILL, "0");
		params.put(FIRST_REFILL, "0");
		params.put(RESPONSE_TYPE,SUCCESS);
        checkForEOPNContactReason4(params);
	}

	/**
	 * Method to test checkDeliveryEnabledForClient method for success response when
	 * the delivery allowed flag is turned on and refill auth and refill remain are
	 * same (first refill)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCheckDeliveryEnabledForClientSuccessResponseForFirstPrescriptionFill() throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVISDELIVERYALLOWED))
				.thenReturn("1");
		Map<String, String> params = new HashMap<>();
		params.put(PARTIAL_FILL, "0");
		params.put(FIRST_REFILL, "1");
		params.put(RESPONSE_TYPE,SUCCESS);
		checkForEOPNContactReason4(params);

	}
	/**
	 * Method to test checkDeliveryEnabledForClient method for success response when
	 * the delivery allowed flag is turned off
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCheckDeliveryEnabledForClientWhenDeliveryFlagIsOff() throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVISDELIVERYALLOWED))
				.thenReturn("0");
		Map<String, String> params = new HashMap<>();
		params.put(PARTIAL_FILL, "0");
		params.put(FIRST_REFILL, "0");
		params.put(RESPONSE_TYPE,SUCCESS);
        checkForEOPNContactReason4(params);

	}
	
	/**
	 * To test checkDeliveryEnabledForClient method works without any exception when
	 * getDeliveryDetails method throws an exception
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCheckDeliveryEnabledForClientWhenGetDeliveryMethodThrowsException() throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVISDELIVERYALLOWED))
				.thenReturn("1");
		Map<String, String> params = new HashMap<>();
		params.put(PARTIAL_FILL, "0");
		params.put(FIRST_REFILL, "0");
		params.put(RESPONSE_TYPE,EXCEPTION);
		checkForEOPNContactReason4(params);

	}
	
	/**
	 * Method to test checkDeliveryEnabledForClient method for success response when
	 * the delivery allowed flag is turned off and fill type is the partial fill
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPatientNotificationForContactReason4ForPartialFill() throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE, ConfigKeys.CVISDELIVERYALLOWED))
				.thenReturn("0");
		Map<String, String> params = new HashMap<>();
		params.put(PARTIAL_FILL, "1");
		params.put(FIRST_REFILL, "0");
		params.put(RESPONSE_TYPE,SUCCESS);
		checkForEOPNContactReason4(params);

	}

	/**
	 * To test drug exclusion status when the drug is eligible for delivery
	 * 
	 * @param firstRefill
	 * @throws Exception
	 */
	public void checkForEOPNContactReason4(Map<String, String> params) throws Exception {
		// Get request string
		Map<String, String> requestMap = getRequestMap("4", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);

		// Create patient notification request object
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		patientNotification.setMessageDate("2020-05-29T04:00:00Z");

		// Call mockEOPNUtilsMethods method to mock required config values and mock
		// prescription,patient related data
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		String communicationName = E_SCRIPT_RECEIVED;
		String reminderMode = Constants.TEXT;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
				patientDetailsMap, inputMap, clientIdMap);
		
		Map<String, Object> drugFilterCriteriaMap = new HashMap<>();
		drugFilterCriteriaMap.put(Constants.NOTIFICATION_KEY,
				NotificationTypeKey.rxrefillreadyforpickup);
		when(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
				patientNotification.getTransactionData().get(0).getDrugGPI(), drugFilterCriteriaMap))
						.thenReturn(false);
		
		//Read parameters from map
		boolean firstRefill = params.get(FIRST_REFILL).equals(Constants.NUMERIC_TRUE_STRING) ? true : false;
		boolean partialFill = params.get(PARTIAL_FILL).equals(Constants.NUMERIC_TRUE_STRING) ? true : false;

		
		// Mock GET_CUSTOMER_PRESCRIPTION_DETAILS query result
		Map<String, String> customerPrescriptionDetailsMap = getPrescriptionDetailMap(firstRefill);
		when(patientNotificationDao.jdbcDataGet(QueryBuilder.GET_CUSTOMER_PRESCRIPTION_DETAILS,
				new Object[] { clientId, patientDetailsMap.get(PRESCRIPTION_ID) }))
						.thenReturn(customerPrescriptionDetailsMap);
		if (partialFill) {
			patientNotification.getTransactionData().get(0).setPartialFillStatus("P");
			when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISPARTIALFILLCOMMUNICATIONENABLED))
					.thenReturn("1");
			Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
			drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxpartialfill);
			when(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
					patientNotification.getTransactionData().get(0).getDrugGPI(), drugFilterCriteriasMap))
							.thenReturn(true);
		} else {
			patientNotification.getTransactionData().get(0).setPartialFillStatus("N");
			patientNotification.getTransactionData().get(0).setRefillNum("1");
			// Mock drugfilterCriteriaUtil response
			if (firstRefill) {
				PowerMockito
						.when(drugFilterCriteriaUtil.isDrugExcluded(anyString(), any(DrugFilterType.class), anyString(),
								anyMap()))
						.thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(false).thenReturn(false);
			} else {
				PowerMockito.when(drugFilterCriteriaUtil.isDrugExcluded(anyString(), any(DrugFilterType.class), anyString(),
						anyMap())).thenReturn(true).thenReturn(false).thenReturn(false).thenReturn(false);
			}
		}

		// Mock sendCommunication object data
		SendCommunicationMode sendCommunicationMode = new SendCommunicationMode();
		sendCommunicationMode.setSendSms("1");
		when(eopnUtils.setSendCommunicationMode(anyString(), anyMap(), anyString())).thenReturn(sendCommunicationMode);

	    // Mock getTextDeliveryEnabledVendors response
		if(!EXCEPTION.equalsIgnoreCase(params.get(RESPONSE_TYPE))) {
		PowerMockito.when(mscriptsCommonDao.getTextDeliveryEnabledVendors(anyString()))
				.thenReturn(getTextDeliveryEnabledDeliveryDetails());
		}else {
			PowerMockito.when(mscriptsCommonDao.getTextDeliveryEnabledVendors(anyString()))
			.thenThrow(MscriptsException.class);	
		}

	  PatientNotificationResponse actualResponse = patientNotificationServiceImpl
				.processPatientNotification(patientNotification, requestString);
		
		if(partialFill) {
			verify(eopnUtils, Mockito.times(1)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
					anyString(), anyList(), anyString(), anyList(), anyString(), anyString());	
		}

		PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);

		assertEquals(expectedResponse.getError(), actualResponse.getError());
		assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
		assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
		assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
		assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
		assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());

	}
	
	/**
	 * Test method for EOPN contact reason 10 when notification comes after
	 * upper cut off time
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason8AfterUpperCutOffTimeForBlockAndDeleteIfCustomerNotificationDisabled() {

		Map<String, String> requestMap = getRequestMap("8", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = RX_IN_PROCESS;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			when(eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(anyString(), anyString(), anyString(), anyList(), anyString(), anyList(), anyMap())).thenReturn(true);
			PowerMockito.when(drugFilterCriteriaUtil.isDrugExcluded(anyString(), any(DrugFilterType.class), anyString(),
					anyMap())).thenReturn(false).thenReturn(false).thenReturn(false).thenReturn(false);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Method to create prescription details map
	 * @param firstRefill
	 * @return map of prescription details
	 */
	private Map<String, String> getPrescriptionDetailMap(boolean firstRefill) {
		Map<String, String> customerPrescriptionDetailsMap = new HashMap<>();
		customerPrescriptionDetailsMap.put("latest_refill_status", "Ready");
		if(!firstRefill) {
		customerPrescriptionDetailsMap.put("ref_aut", "4");
		customerPrescriptionDetailsMap.put("ref_rem", "2");
		}else {
			customerPrescriptionDetailsMap.put("ref_aut", "4");
			customerPrescriptionDetailsMap.put("ref_rem", "4");	
		}
		return customerPrescriptionDetailsMap;
	}
	
	/**
	 * Test method for EOPN contact reason 8 when prescription parameters are not
	 * blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason8WhenPrescriptionParamsNotBlocked() {
		boolean scheduleBlocked = false;
		boolean gpiBlocked = false;
		boolean clientMapAvailable = true;
		processPatientNotificationForContactReason8(scheduleBlocked, gpiBlocked, clientMapAvailable);
	}

	/**
	 * Test method for EOPN contact reason 8 when the specified prescription
	 * schedule is blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason8WhenScheduleBlocked() {
		boolean scheduleBlocked = true;
		boolean gpiBlocked = false;
		boolean clientMapAvailable = true;
		processPatientNotificationForContactReason8(scheduleBlocked, gpiBlocked, clientMapAvailable);
	}

	/**
	 * Test method for EOPN contact reason 8 when the specified prescription
	 * GPI is blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason8WhenGPIBlocked() {
		boolean scheduleBlocked = false;
		boolean gpiBlocked = true;
		boolean clientMapAvailable = true;
		processPatientNotificationForContactReason8(scheduleBlocked, gpiBlocked, clientMapAvailable);
	}

	/**
	 * Test method for EOPN contact reason 8 when the specified client id does not
	 * exist
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason8WhenClientIdNotExists() {
		boolean scheduleBlocked = false;
		boolean gpiBlocked = false;
		boolean clientMapAvailable = false;
		processPatientNotificationForContactReason8(scheduleBlocked, gpiBlocked, clientMapAvailable);
	}

	/**
	 * Test method for EOPN contact reason 8
	 */
	private void processPatientNotificationForContactReason8(boolean scheduleBlocked, boolean gpiBlocked,
			boolean clientMapAvailable) {
		Map<String, String> requestMap = getRequestMap("8", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = RX_IN_PROCESS;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = null;
		if (clientMapAvailable) {
			clientIdMap = getClientIdMap(false, inputMap);
		}
		
		try {
            //Mock EOPN utils method
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISADJUDICATIONMESSAGEENABLED))
			.thenReturn("1");
			Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
			drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxrefillinprocess);
			//Mock drug filter criteria util response
			when(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.SCHEDULE,
					patientNotification.getTransactionData().get(0).getDrugSchedule(), drugFilterCriteriasMap))
							.thenReturn(scheduleBlocked);
			when(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
					patientNotification.getTransactionData().get(0).getDrugGPI(), drugFilterCriteriasMap))
							.thenReturn(gpiBlocked);
			patientNotification.getTransactionData().get(0).setPartialFillStatus("N");
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			if (scheduleBlocked || gpiBlocked) {
				verify(eopnUtils, Mockito.times(1)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
						anyString(), anyList(), anyString(), anyList(), anyString(), anyString());
			} else {
				verify(eopnUtils, Mockito.times(0)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
						anyString(), anyList(), anyString(), anyList(), anyString(), anyString());
			}
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			//Compare actual and expected response
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	@Test
	public void testProcessPatientNotification_ForEOPNContactReason2AfterUpperCutOffTimeForBlockAndDeleteIfCustomerNotificationDisabled() {

		Map<String, String> requestMap = getRequestMap("2", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = RX_OUT_OF_STOCK;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			when(eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(anyString(), anyString(), anyString(), anyList(), anyString(), anyList(), anyMap())).thenReturn(true);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testProcessPatientNotification_ForEOPNContactReason1AfterUpperCutOffTimeForBlockAndDeleteIfCustomerNotificationDisabled() {

		Map<String, String> requestMap = getRequestMap("1", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = RX_REJECTED;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			when(eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(anyString(), anyString(), anyString(), anyList(), anyString(), anyList(), anyMap())).thenReturn(true);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for EOPN contact reason 2 when prescription GPI is not blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason2WhenGPINotBlocked() {
		boolean gpiBlocked = false;
		processpatientNotificationForContactReason2(gpiBlocked);
	}

	/**
	 * Test method for EOPN contact reason 2 when prescription GPI is blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason2WhenGPIBlocked() {
		boolean gpiBlocked = true;
		processpatientNotificationForContactReason2(gpiBlocked);
	}

	/**
	 * Test method for EOPN contact reason 8
	 */
	private void processpatientNotificationForContactReason2(boolean gpiBlocked) {
		Map<String, String> requestMap = getRequestMap("2", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
			drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxoutofstock);
			when(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
					patientNotification.getTransactionData().get(0).getDrugGPI(), drugFilterCriteriasMap))
							.thenReturn(gpiBlocked);
			when(MiscUtils.checkDayorDate(anyString(),anyString(),anyString(),anyString(),anyString()))
			.thenReturn(Constants.cvDateFormatUser);
			when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVDATEFORMATUSER))
			.thenReturn("MM/dd/yyyy");
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			if (gpiBlocked) {
				verify(eopnUtils, Mockito.times(1)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
						anyString(), anyList(), anyString(), anyList(), anyString(), anyString());
			} else {
				verify(eopnUtils, Mockito.times(0)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
						anyString(), anyList(), anyString(), anyList(), anyString(), anyString());
			}
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

		@Test
		public void testProcessPatientNotification_ForEOPNContactReason4AfterUpperCutOffTimeForBlockAndDeleteIfCustomerNotificationDisabled() {

			Map<String, String> requestMap = getRequestMap("4", PRESCRIPTION_NUMBER, TXN_NUMBER);
			String requestString = getRequestString(requestMap);
			PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
			String communicationName = RX_PARTIAL_FILL;
			String reminderMode = Constants.EMAIL;
			String varPickupCountForToday = "0";
			String clientId = "1";
			String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
			boolean isPatientCommunicationConsentSatisfied = true;
			Map<String, String> transactionStateMap = getTransactionStateMap(false);
			Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
			Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
					sendMessageStatus);
			Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
			try {
				mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
						patientDetailsMap, inputMap, clientIdMap);
				when(eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(anyString(), anyString(), anyString(), anyList(), anyString(), anyList(), anyMap())).thenReturn(true);
				Map<String, String> rxStatusMap = new HashMap<String, String>();
				rxStatusMap.put("refillStatus","InProcess");
				rxStatusMap.put("txNumber","1234");
				when(patientNotificationDao.jdbcDataGet(QueryBuilder.GET_LATEST_REFILL_STATUS_AND_TRANSACTION_NUMBER,
						new Object[] { "1234", "1234" })).thenReturn(rxStatusMap);
				PatientNotificationResponse actualResponse = patientNotificationServiceImpl
						.processPatientNotification(patientNotification, requestString);
				PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
				assertEquals(expectedResponse.getError(), actualResponse.getError());
				assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
				assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
				assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
				assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
				assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}

	
	/**
	 * Test method for EOPN contact reason 1 when prescription GPI is not blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason1WhenGPINotBlocked() {
		boolean gpiBlocked = false;
		processpatientNotificationForContactReason1(gpiBlocked);
	}
	
	/**
	 * Test method for EOPN contact reason 1 when prescription GPI is blocked
	 */
	@Test
	public void testProcessPatientNotificationForEOPNContactReason1WhenGPIBlocked() {
		boolean gpiBlocked = true;
		processpatientNotificationForContactReason1(gpiBlocked);
	}
	
	/**
	 * Test method for EOPN contact reason 1
	 */
	private void processpatientNotificationForContactReason1(boolean gpiBlocked) {
		Map<String, String> requestMap = getRequestMap("1", PRESCRIPTION_NUMBER, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = PARTIAL_FILL_ON_HOLD_STRING;
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(false, inputMap);
		try {

			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
			drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxrejected);
			when(drugFilterCriteriaUtil.isDrugExcluded(clientId, DrugFilterType.GPI,
					patientNotification.getTransactionData().get(0).getDrugGPI(), drugFilterCriteriasMap))
							.thenReturn(gpiBlocked);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			if (gpiBlocked) {
				verify(eopnUtils, Mockito.times(1)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
						anyString(), anyList(), anyString(), anyList(), anyString(), anyString());
			} else {
				verify(eopnUtils, Mockito.times(0)).insertIntoExternalRequestHandlerBlockedDetails(anyString(),
						anyString(), anyList(), anyString(), anyList(), anyString(), anyString());
			}
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testProcessPatientNotification_ForEOPNContactReason8NewRxAfterUpperCutOffTimeForBlockAndDeleteIfCustomerNotificationDisabled() {

		Map<String, String> requestMap = getRequestMap("8", "3456790", TXN_NUMBER);
		requestMap.put("partialFillStatus", "N");
		String requestString = getRequestString(requestMap);
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		String communicationName = RX_IN_PROCESS;
		String reminderMode = Constants.EMAIL;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_GREATER_THAN_UPPER_CUT_OFF;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			when(ConfigReader.readConfig(clientId, Constants.LANG_CODE, ConfigKeys.CVISADJUDICATIONMESSAGEENABLED))
					.thenReturn("1");
			when(eopnUtils.blockAndDeleteIfCustomerNotificationDisabled(anyString(), anyString(), anyString(), anyList(), anyString(), anyList(), anyMap())).thenReturn(true);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			PatientNotificationResponse expectedResponse = getExpectedResponse(null, null);
			assertEquals(expectedResponse.getError(), actualResponse.getError());
			assertEquals(expectedResponse.getErrorCode(), actualResponse.getErrorCode());
			assertEquals(expectedResponse.getErrorId(), actualResponse.getErrorId());
			assertEquals(expectedResponse.getErrorMessage(), actualResponse.getErrorMessage());
			assertEquals(expectedResponse.getHttpStatusCode(), actualResponse.getHttpStatusCode());
			assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	
	/**
	 * ERH should send generic message when rx details is absent in the EOPN for contact reason 2
	 * @throws Exception
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNConcatReason2RxNumberMissing() throws Exception {
		
		
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_EOPN_GENERIC_MSG_OUT_OF_STOCK)).thenReturn("EOPN Generic Message");
		Map<String, String> requestMap = getRequestMap("2", null, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		String communicationName = "EOPN Generic Message";
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		patientNotification.getTransactionData().get(0).setPrescriptionNumber(null);
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			assertEquals("SUCCESS", actualResponse.getStatus());
			assertEquals("200", actualResponse.getHttpStatusCode());
		} catch (Exception exp) {
			fail(exp.getMessage());
		}
		
	}
	
	/**
	 * ERH should send generic message when rx details is absent in the EOPN for contact reason 12
	 * @throws Exception
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNConcatReason12RxNumberMissing() throws Exception {
		
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_EOPN_GENERIC_MSG_THIRD_PARTY_EXCEPTION)).thenReturn("EOPN Generic Message");
		Map<String, String> requestMap = getRequestMap("12", null, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		String communicationName = "EOPN Generic Message";
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		patientNotification.getTransactionData().get(0).setPrescriptionNumber(null);
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			assertEquals("SUCCESS", actualResponse.getStatus());
			assertEquals("200", actualResponse.getHttpStatusCode());
		} catch (Exception exp) {
			fail(exp.getMessage());
		}
		
		
	}
	
	/**
	 * ERH should send generic message when rx details is absent in the EOPN for contact reason 13
	 * @throws Exception
	 */
	@Test
	public void testProcessPatientNotification_ForEOPNConcatReason13RxNumberMissing() throws Exception {
		
		
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_EOPN_GENERIC_MSG_CALL_PRESCRIBER)).thenReturn("EOPN Generic Message");
		Map<String, String> requestMap = getRequestMap("13", null, TXN_NUMBER);
		String requestString = getRequestString(requestMap);
		String communicationName = "EOPN Generic Message";
		PatientNotificationRequest patientNotification = getPatientNoficationRequest(requestString);
		patientNotification.getTransactionData().get(0).setPrescriptionNumber(null);
		String reminderMode = Constants.TEXT;
		String varPickupCountForToday = "0";
		String clientId = "1";
		String sendMessageStatus = Constants.TIME_BTW_CUTOFFS;
		boolean isPatientCommunicationConsentSatisfied = true;
		Map<String, String> transactionStateMap = getTransactionStateMap(false);
		Map<String, String> patientDetailsMap = getPatientDetailsMap(false);
		Map<String, String> inputMap = createInputMap(communicationName, reminderMode, varPickupCountForToday, clientId,
				sendMessageStatus);
		Map<String, String> clientIdMap = getClientIdMap(true, inputMap);
		try {
			mockEOPNUtilsMethods(patientNotification, isPatientCommunicationConsentSatisfied, transactionStateMap,
					patientDetailsMap, inputMap, clientIdMap);
			PatientNotificationResponse actualResponse = patientNotificationServiceImpl
					.processPatientNotification(patientNotification, requestString);
			assertEquals("SUCCESS", actualResponse.getStatus());
			assertEquals("200", actualResponse.getHttpStatusCode());
		} catch (Exception exp) {
			fail(exp.getMessage());
		}
		
		
	}
	
}