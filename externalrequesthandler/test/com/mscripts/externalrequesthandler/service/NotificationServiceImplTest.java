package com.mscripts.externalrequesthandler.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mscripts.configurationhandler.config.ConfigManager;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.jdbc.MscriptsCommonDaoImpl;
import com.mscripts.enums.DrugFilterType;
import com.mscripts.enums.NotificationTypeKey;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.jdbc.NotificationDaoJdbc;
import com.mscripts.externalrequesthandler.domain.CustomerPrescription;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionFile;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionTxtpfile;
import com.mscripts.service.IvrService;
import com.mscripts.utils.Constants;
import com.mscripts.utils.DrugFilterCriteriaUtil;
import com.mscripts.utils.NotificationFilterUtil;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.TextMaskingService;
import com.mscripts.utils.XMLUtils;

@PrepareForTest({ ConfigReader.class, ConfigManager.class, NotificationServiceImpl.class, XMLUtils.class })
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*"})
public class NotificationServiceImplTest {

	private static final String EXCEPTION_OCCURED_WHILE_PROCESSING_CHECK_TEXT_DELIVERY_ENABLED_FOR_CLIENT_METHOD = "Exception occured while processing checkTextDeliveryEnabledForClient method";
	private static final String CUSTOMER_PRES_LIST = "customerPresList";
	private static final String DELIVERY_DETAIL_ID = "delivery_detail_id";
	private static final String CONSTANT_Y = "Y";
	private static final String CV_IS_DELIVERY_ALLOWED = "cvIsDeliveryAllowed";
	private static final String CV_TX_FILE_WILL_CALL_READY_FLAG = "cvTxFileWillCallReadyFlag";
	private static final String TEST_STRING = "dummy";
	private static final String CLIENT_ID = "1";
	private static final String LANGUAGE = "en";
	
	@Mock
	IvrService mockIVRService;
	
	@Mock
	TextMaskingService mockTextMaskingService;

	NotificationServiceImpl notificationServiceImpl = spy(new NotificationServiceImpl());
	MscriptsCommonDaoImpl mscriptsCommonDaoImpl = new MscriptsCommonDaoImpl();
	PHICredentials phiCredentials = new PHICredentials();
	NotificationDaoJdbc notificationDao = new NotificationDaoJdbc();
	private DrugFilterCriteriaUtil mockDrugFilterCriteriaUtil;
	private NotificationFilterUtil mockNotificationFilterUtil;
	
	@Before
	public void initialiseMocks() {
		mscriptsCommonDaoImpl = mock(MscriptsCommonDaoImpl.class);
		notificationServiceImpl.setMscriptsCommonDao(mscriptsCommonDaoImpl);
		phiCredentials = mock(PHICredentials.class);
		notificationServiceImpl.setpHICredentials(phiCredentials);
		notificationDao = mock(NotificationDaoJdbc.class);
		notificationServiceImpl.setNotificationDao(notificationDao);
		mockDrugFilterCriteriaUtil = mock(DrugFilterCriteriaUtil.class);
		mockNotificationFilterUtil = mock(NotificationFilterUtil.class);
		notificationServiceImpl.setDrugFilterCriteriaUtil(mockDrugFilterCriteriaUtil);
		notificationServiceImpl.setNotificationFilterUtil(mockNotificationFilterUtil);
		notificationServiceImpl.setIvrService(mockIVRService);
		notificationServiceImpl.setTextMaskingService(mockTextMaskingService);
		PowerMockito.mockStatic(XMLUtils.class);
		PowerMockito.mockStatic(ConfigReader.class);
		PowerMockito.mockStatic(ConfigManager.class);
	}

	public void prepareConfigs(String value) throws Exception {
		when(ConfigReader.readConfig(anyString(), anyString(), anyString())).thenReturn(TEST_STRING);
		when(ConfigReader.readConfig(CLIENT_ID, LANGUAGE, "cvLogTransactionLevelInsuranceCard")).thenReturn(value);
	}

	/*
	 * Method to test if the function logInsuranceCardTransactions is invoked if the
	 * flags cvLogTransactionLevelInsuranceCard flag is turned on
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testUpdateCustomerPrescription_logInsuranceCardTransactions() throws Exception {
		try {
			List<CustomerPrescription> c = new ArrayList<>();
			CustomerPrescription cp = new CustomerPrescription();
			cp.setCopay("copay");
			c.add(cp);
			List custPresList = new ArrayList();
			custPresList.add(c);
			Map prescriptionMap = new HashMap();
			prescriptionMap.put(CUSTOMER_PRES_LIST, c);
			Map<String, String> migrationResponse = new HashMap<>();
			migrationResponse.put("isPickupEligible", "1");
			prepareConfigs("1");
			
			Map<String, String> rxMiscDetailsMap = new HashMap<String, String>();
			rxMiscDetailsMap.put("days", "20");
			rxMiscDetailsMap.put("quantity", "20");
			rxMiscDetailsMap.put("refnum", "1");
			when(notificationDao.getRxDetailsForDaysQtyClients(anyString(), anyString())).thenReturn(rxMiscDetailsMap);
			
			Map<String, Object> pickupReminderDetails = new HashMap<>();
			pickupReminderDetails.put("days", "20");
			pickupReminderDetails.put("quantity", "20");
			pickupReminderDetails.put("refill_number", "1");
			when(notificationDao
					.selectRxPickupReminderInstancesRecords(anyString(), anyString(), anyString())).thenReturn(pickupReminderDetails);
			PowerMockito.doReturn(prescriptionMap).when(notificationServiceImpl).parseCustomerPrescriptions(anyString(),
					anyString(), anyString());
			when(mscriptsCommonDaoImpl.checkMigratedUserForPickupReminder(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyString())).thenReturn(migrationResponse);
			when(phiCredentials.getSecondaryKey()).thenReturn(TEST_STRING);
			when(notificationDao.updateCustomerPrescriptions(anyString(), (CustomerPrescription) anyObject(),
					anyString(), anyString(), anyString(), anyMap())).thenReturn(prescriptionMap);
			PowerMockito.doNothing().when(notificationServiceImpl, "insertAgingPickupReminders", anyString(), anyMap(),
					anyBoolean());
			notificationServiceImpl.updateCustomerPrescription(TEST_STRING, TEST_STRING, CLIENT_ID, TEST_STRING,
					TEST_STRING);
		} catch (Exception e) {
			verify(notificationDao, Mockito.times(1)).logInsuranceCardTransactions(anyString(), anyString(),
					anyString(), anyString(), (CustomerTransactionFile[]) any());
		}
	}
	
	/*
	 * Method to test if the function logInsuranceCardTransactions is not invoked if
	 * the flags cvLogTransactionLevelInsuranceCard flag is turned off
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testUpdateCustomerPrescription_doNotLogInsuranceCardTransactions() throws Exception {
		try {
			List<CustomerPrescription> c = new ArrayList<>();
			CustomerPrescription cp = new CustomerPrescription();
			cp.setCopay("copay");
			c.add(cp);
			List custPresList = new ArrayList();
			custPresList.add(c);
			Map prescriptionMap = new HashMap();
			prescriptionMap.put(CUSTOMER_PRES_LIST, c);
			Map<String, String> migrationResponse = new HashMap<>();
			migrationResponse.put("isPickupEligible", "1");
			prepareConfigs("0");
			PowerMockito.doReturn(prescriptionMap).when(notificationServiceImpl).parseCustomerPrescriptions(anyString(),
					anyString(), anyString());
			when(mscriptsCommonDaoImpl.checkMigratedUserForPickupReminder(anyString(), anyString(), anyString(),
					anyString(), anyString(), anyString())).thenReturn(migrationResponse);
			when(phiCredentials.getSecondaryKey()).thenReturn(TEST_STRING);
			when(notificationDao.updateCustomerPrescriptions(anyString(), (CustomerPrescription) anyObject(),
					anyString(), anyString(), anyString(), anyMap())).thenReturn(prescriptionMap);
			PowerMockito.doNothing().when(notificationServiceImpl, "insertAgingPickupReminders", anyString(), anyMap(),
					anyBoolean());
			notificationServiceImpl.updateCustomerPrescription(TEST_STRING, TEST_STRING, CLIENT_ID, TEST_STRING,
					TEST_STRING);
		} catch (Exception e) {
			verify(notificationDao, Mockito.times(0)).logInsuranceCardTransactions(anyString(), anyString(),
					anyString(), anyString(), (CustomerTransactionFile[]) any());
		}
	}

	/*
	 * Method to test if the TxtpFile is being parsed correctly
	 */
	@Test
	public void testParseCustomerTransactionTxtpfile_parsed() throws Exception {
		List<CustomerTransactionTxtpfile> result;
		final NodeList customerTransactionTxtpfile = new NodeList() {
			@Override
			public Node item(int index) {
				return null;
			}

			@Override
			public int getLength() {
				return 1;
			}
		};

		when(XMLUtils.getNodeValue((Document) any(), anyString())).thenReturn(TEST_STRING);
		result = notificationServiceImpl.parseCustomerTransactionTxtpfile(CLIENT_ID, customerTransactionTxtpfile);
		for (int i = 0; i < result.size(); i++) {
			assertEquals(getEpectedResult().get(i).getId(), result.get(i).getId());
			assertEquals(getEpectedResult().get(i).getCounter(), result.get(i).getCounter());
			assertEquals(getEpectedResult().get(i).getCopay(), result.get(i).getCopay());
			assertEquals(getEpectedResult().get(i).getCard(), result.get(i).getCard());
			assertEquals(getEpectedResult().get(i).getBalance(), result.get(i).getTxtpPrice());
			assertEquals(getEpectedResult().get(i).getTxtpCost(), result.get(i).getTxtpCost());
			assertEquals(getEpectedResult().get(i).getPaid(), result.get(i).getPaid());
			assertEquals(getEpectedResult().get(i).getIncent(), result.get(i).getIncent());
			assertEquals(getEpectedResult().get(i).getOrigtype(), result.get(i).getOrigtype());
			assertEquals(getEpectedResult().get(i).getPlan(), result.get(i).getPlan());
			assertEquals(getEpectedResult().get(i).getTxtpTax(), result.get(i).getTxtpTax());
			assertEquals(getEpectedResult().get(i).getTxtpCompfee(), result.get(i).getTxtpCompfee());
			assertEquals(getEpectedResult().get(i).getPlanBin(), result.get(i).getPlanBin());
			assertEquals(getEpectedResult().get(i).getPlanName(), result.get(i).getPlanName());
			assertEquals(getEpectedResult().get(i).getPlanPCN(), result.get(i).getPlanPCN());
			assertEquals(getEpectedResult().get(i).getGroup(), result.get(i).getGroup());
			assertEquals(getEpectedResult().get(i).getSplit(), result.get(i).getSplit());
			assertEquals(getEpectedResult().get(i).getCopover(), result.get(i).getCopover());
			assertEquals(getEpectedResult().get(i).getReverse(), result.get(i).getReverse());
			assertEquals(getEpectedResult().get(i).getCollect(), result.get(i).getCollect());
			assertEquals(getEpectedResult().get(i).getOthamt(), result.get(i).getOthamt());
			assertEquals(getEpectedResult().get(i).getTxtpUpcharge(), result.get(i).getTxtpUpcharge());

		}

	}

	private List<CustomerTransactionTxtpfile> getEpectedResult() {
		List<CustomerTransactionTxtpfile> customerTransactionTxtpfiles = new ArrayList<>();
		CustomerTransactionTxtpfile customerTxtpFile = new CustomerTransactionTxtpfile();
		customerTxtpFile.setCounter(TEST_STRING);
		customerTxtpFile.setCopay(TEST_STRING);
		customerTxtpFile.setId(TEST_STRING);
		customerTxtpFile.setCard(TEST_STRING);
		customerTxtpFile.setBalance(TEST_STRING);
		customerTxtpFile.setTxtpPrice(TEST_STRING);
		customerTxtpFile.setTxtpCost(TEST_STRING);
		customerTxtpFile.setPaid(TEST_STRING);
		customerTxtpFile.setIncent(TEST_STRING);
		customerTxtpFile.setOrigtype(TEST_STRING);
		customerTxtpFile.setPlan(TEST_STRING);
		customerTxtpFile.setTxtpTax(TEST_STRING);
		customerTxtpFile.setTxtpCompfee(TEST_STRING);
		customerTxtpFile.setPlanPCN(TEST_STRING);
		customerTxtpFile.setPlanName(TEST_STRING);
		customerTxtpFile.setPlanBin(TEST_STRING);
		customerTxtpFile.setGroup(null);
		customerTxtpFile.setSplit(null);
		customerTxtpFile.setCopover(null);
		customerTxtpFile.setReverse(null);
		customerTxtpFile.setCollect(null);
		customerTxtpFile.setUpcharge(null);
		customerTxtpFile.setOthamt(null);
		customerTransactionTxtpfiles.add(customerTxtpFile);
		return customerTransactionTxtpfiles;
	}
	
	/**
	 * Method to test checkDeliveryEnabledForClient method for success response when
	 * the delivery allowed flag is turned on
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCheckDeliveryEnabledForClientForSuccessResponse() throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(CLIENT_ID, LANGUAGE, CV_IS_DELIVERY_ALLOWED)).thenReturn("1");
		checkTextDeliveryEnabledMethodCall(false);

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
		when(ConfigReader.readConfig(CLIENT_ID, LANGUAGE, CV_IS_DELIVERY_ALLOWED)).thenReturn("1");
		checkTextDeliveryEnabledMethodCall(true);

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
		when(ConfigReader.readConfig(CLIENT_ID, LANGUAGE, CV_IS_DELIVERY_ALLOWED)).thenReturn("0");
		checkTextDeliveryEnabledMethodCall(false);

	}

	/**
	 * To test checkDeliveryEnabledForClient method throws an MscriptsException when
	 * getDeliveryDetails method throws an exception
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCheckDeliveryEnabledForClientForDeliveryDetailsFaliureResponse() throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(CLIENT_ID, LANGUAGE, CV_IS_DELIVERY_ALLOWED)).thenReturn("1");

		// Create mock data for prescriptions
		Map prescriptionMap = new HashMap();
		prescriptionMap.put(CUSTOMER_PRES_LIST, getPrescriptionList(false));

		// Mock parseCustomerPrescriptions call
		PowerMockito.doReturn(prescriptionMap).when(notificationServiceImpl).parseCustomerPrescriptions(anyString(),
				anyString(), anyString());

		// Mock getTextDeliveryEnabledVendors failsure response
		PowerMockito.when(mscriptsCommonDaoImpl.getTextDeliveryEnabledVendors(anyString()))
				.thenThrow(MscriptsException.class);

		String expectedString=EXCEPTION_OCCURED_WHILE_PROCESSING_CHECK_TEXT_DELIVERY_ENABLED_FOR_CLIENT_METHOD;
		String actualString = null;
		// Check for updateCustomerPrescription call
		try {
			notificationServiceImpl.updateCustomerPrescription(TEST_STRING, TEST_STRING, CLIENT_ID, TEST_STRING,
					TEST_STRING);
		} catch (Exception ex) {
			actualString = EXCEPTION_OCCURED_WHILE_PROCESSING_CHECK_TEXT_DELIVERY_ENABLED_FOR_CLIENT_METHOD;
		}

		// compare expected and actual response
		assertEquals(expectedString, actualString);
		
		// To verify drugExcuded method will not be called when
		// getTextDeliveryEnabledVendors throws an exception
		verify(mockDrugFilterCriteriaUtil, Mockito.times(0)).isDrugExcluded(anyString(), any(DrugFilterType.class),
				anyString(), anyMap());

	}

	/**
	 * 
	 * @param firstRefill - To specify prescription filled first time or not
	 * @throws Exception
	 */
	private void checkTextDeliveryEnabledMethodCall(boolean firstRefill) throws Exception {
		// Mock required config values
		when(ConfigReader.readConfig(CLIENT_ID, LANGUAGE, CV_TX_FILE_WILL_CALL_READY_FLAG)).thenReturn(CONSTANT_Y);

		// Create mock data for prescriptions
		Map prescriptionMap = new HashMap();
		prescriptionMap.put(CUSTOMER_PRES_LIST, getPrescriptionList(firstRefill));

		// Mock parseCustomerPrescriptions call
		PowerMockito.doReturn(prescriptionMap).when(notificationServiceImpl).parseCustomerPrescriptions(anyString(),
				anyString(), anyString());

		// Mock getTextDeliveryEnabledVendors response
		PowerMockito.when(mscriptsCommonDaoImpl.getTextDeliveryEnabledVendors(anyString()))
				.thenReturn(getTextDeliveryEnabledDeliveryDetails());

		// Mock drugfilterCriteriaUtil response
		if (firstRefill) {
			PowerMockito
					.when(mockDrugFilterCriteriaUtil.isDrugExcluded(anyString(), any(DrugFilterType.class), anyString(),
							anyMap()))
					.thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(false).thenReturn(false);
		} else {
			PowerMockito.when(mockDrugFilterCriteriaUtil.isDrugExcluded(anyString(), any(DrugFilterType.class),
					anyString(), anyMap())).thenReturn(true).thenReturn(false).thenReturn(false).thenReturn(false);
		}

		// Check for updateCustomerPrescription call
		notificationServiceImpl.updateCustomerPrescription(TEST_STRING, TEST_STRING, CLIENT_ID, TEST_STRING,
				TEST_STRING);

	}

	/**
	 * Method to test updateCustomerPrescription method for in process notification
	 * flow when drug is not excluded for sending notification
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateCustomerPrescriptionForInProcessNotificationWhenDrugNotExcluded() throws Exception {
		boolean scheduleExcluded = false;
		boolean gpiExcluded = false;
		checkForInprocessFlow(scheduleExcluded, gpiExcluded);
	}

	/**
	 * Method to test updateCustomerPrescription method for in process notification
	 * flow when drug schedule is excluded from sending notification
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateCustomerPrescriptionForInProcessNotificationWhenDrugScheduleExcluded() throws Exception {
		boolean scheduleExcluded = true;
		boolean gpiExcluded = false;
		checkForInprocessFlow(scheduleExcluded, gpiExcluded);
	}

	/**
	 * Method to test updateCustomerPrescription method for in process notification
	 * flow when drug GPI is excluded from sending notification
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateCustomerPrescriptionForInProcessNotificationWhenDrugGPIExcluded() throws Exception {
		boolean scheduleExcluded = false;
		boolean gpiExcluded = true;
		checkForInprocessFlow(scheduleExcluded, gpiExcluded);
	}

	/**
	 * Method to test in process flow in update notification
	 * 
	 * @param scheduleExcluded
	 * @param gpiExcluded
	 * @throws Exception
	 */
	private void checkForInprocessFlow(boolean scheduleExcluded, boolean gpiExcluded) throws Exception {
		// Create mock data for prescriptions
		Map<String,Object> prescriptionMap = new HashMap<>();
		setPrescritptionTestData(prescriptionMap);
        
        // Mock parseCustomerPrescriptions call
		PowerMockito.doReturn(prescriptionMap).when(notificationServiceImpl).parseCustomerPrescriptions(anyString(),
				anyString(), anyString());
		prepareConfigs("0");

		when(notificationDao.updateCustomerPrescriptions(anyString(), (CustomerPrescription) anyObject(), anyString(),
				anyString(), anyString(), anyMap())).thenReturn(prescriptionMap);
		Map<String, Object> drugFilterCriteriasMap = new HashMap<>();
		drugFilterCriteriasMap.put(Constants.NOTIFICATION_KEY, NotificationTypeKey.rxrefillinprocess);
		when(mockDrugFilterCriteriaUtil.isDrugExcluded("1", DrugFilterType.SCHEDULE, "5", drugFilterCriteriasMap))
				.thenReturn(scheduleExcluded);
		when(mockDrugFilterCriteriaUtil.isDrugExcluded("1", DrugFilterType.GPI, "110000000000", drugFilterCriteriasMap))
				.thenReturn(gpiExcluded);

		notificationServiceImpl.updateCustomerPrescription(TEST_STRING, TEST_STRING, CLIENT_ID, TEST_STRING,
				TEST_STRING);

		verify(mscriptsCommonDaoImpl, Mockito.times(1)).fillRefillReminderInstances(anyString(), anyString(),
				anyString(),anyString());
		if (!scheduleExcluded && !gpiExcluded) {
			verify(notificationDao, Mockito.times(1)).insertOrUpdateJdbcData(anyString(), (Object[]) anyObject());

		} else {
			verify(notificationDao, Mockito.times(0)).insertOrUpdateJdbcData(anyString(), (Object[]) anyObject());

		}

	}

	/**
	 * Method to set the prescription data
	 * 
	 * @param prescriptionMap
	 */
	private void setPrescritptionTestData(Map<String, Object> prescriptionMap) {
		prescriptionMap.put(CUSTOMER_PRES_LIST, getPrescriptionList(false));
		prescriptionMap.put("vIsDrugEligibleForInProcessText", "1");
		prescriptionMap.put("sendRxPickup", "0");
		prescriptionMap.put("varIsRxSold", "0");
		prescriptionMap.put("isRxStatusNotificationsEnabled", "0");
		prescriptionMap.put("filled", TEST_STRING);
		prescriptionMap.put("refillStatus", TEST_STRING);
		prescriptionMap.put("custPrescID", TEST_STRING);
		prescriptionMap.put("wso2notificationTime", TEST_STRING);
		prescriptionMap.put("prescriptionCode", TEST_STRING);
		prescriptionMap.put("rxAdjudicationComID", TEST_STRING);
	}

	/**
	 * Method returns the mock data for customer prescription list
	 * 
	 * @return list of customer prescription objects
	 */
	private List<CustomerPrescription> getPrescriptionList(boolean firstRefill) {
		List<CustomerPrescription> prescriptionList = new ArrayList<>();
		CustomerPrescription prescription = new CustomerPrescription();
		if (firstRefill)
			prescription.setRefRem("2");
		else
			prescription.setRefRem("1");
		prescription.setRefAut("2");
		prescription.setStoreNCPDP("12345");
		prescription.setSched("5");
		prescription.setPrescDrugNDC("123");
		prescription.setPrescDrugGPI("110000000000");
		prescription.setDispDrugGPI(prescription.getPrescDrugGPI());
		prescription.setWillCallReady(CONSTANT_Y);
		prescriptionList.add(prescription);
		return prescriptionList;

	}

	/**
	 * Method returns the mock data for delivery details
	 * 
	 * @return List of text delivery enabled delivery details
	 */
	private List<Map<String, String>> getTextDeliveryEnabledDeliveryDetails() {
		List<Map<String, String>> deliveryDetailList = new ArrayList();
		Map<String, String> deliveryDetailMap = new HashMap<>();
		deliveryDetailMap.put(DELIVERY_DETAIL_ID, "1");
		deliveryDetailList.add(deliveryDetailMap);
		return deliveryDetailList;
	}
	
	private HashMap<String, String> getCustomerMap(){
		HashMap<String, String> customerMap = new HashMap<>();
		customerMap.put("customerId", TEST_STRING);
		customerMap.put("rxNumber", TEST_STRING);
		customerMap.put("storeName", TEST_STRING);
		customerMap.put("storeAddressLine1", TEST_STRING);
		customerMap.put("storePhoneNumber", TEST_STRING);
		customerMap.put("customerFirstName", TEST_STRING);
		customerMap.put("mscriptsEntityId", TEST_STRING);
		customerMap.put("mobileNumber", TEST_STRING);
		customerMap.put("rxName", TEST_STRING);
		customerMap.put("showRxNameVal", TEST_STRING);
		customerMap.put("communicationId", TEST_STRING);
		customerMap.put("storeCity", TEST_STRING);
		return customerMap;
	}
	
	
	@Test
	public void testSendIvrCommunication_Success() throws Exception {
		prepareConfigs("1");
		when(mockIVRService.fetchCustomerPreference(anyString(), anyString())).thenReturn("rxName");
		when(mockTextMaskingService.maskingTextBasedOnMaskingType(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("dummyRxNameOrNumber");
		String actual = notificationServiceImpl.sendIvrCommunication("1", getCustomerMap());
		assertEquals(Constants.IVRSUCCESS, actual);
	}
}