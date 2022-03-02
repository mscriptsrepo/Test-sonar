package com.mscripts.externalrequesthandler.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.codehaus.jackson.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.enums.CommunicationTemplateStrings;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.NotificationDao;
import com.mscripts.externalrequesthandler.dao.PatientNotificationDao;
import com.mscripts.externalrequesthandler.domain.PatientData;
import com.mscripts.externalrequesthandler.domain.TransactionData;
import com.mscripts.service.MscriptsCommonService;
import com.mscripts.service.URLShortner;
import com.mscripts.utils.ConfigKeys;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.mscripts.enums.CommunicationTemplateStrings;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.domain.CreditCard;
import com.mscripts.externalrequesthandler.domain.PatientData;
import com.mscripts.txndata.service.PatientPaymentMethodService;
import com.mscripts.utils.Constants;
import com.mscripts.utils.GenerateRandom;
import com.mscripts.utils.NotificationFilterUtil;
import com.mscripts.utils.mscriptsException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigReader.class,GenerateRandom.class })
@PowerMockIgnore({ "javax.management.*" })
public class EOPNUtilsTest {

	@Mock
	PatientNotificationDao mockPatientNotificationDao;

	@Mock
	NotificationFilterUtil mockNotificationFilterUtil;
	
	@Mock
	NotificationDao mockNotificationDao;
	
	@Mock
	MscriptsCommonService mockMscriptsCommonService;
	
	@Mock
	URLShortner mockUrlShortnerService;

	@InjectMocks
	EOPNUtils eopnUtils = new EOPNUtils();

	@Before
	public void mockConfigs() throws Exception {
		PowerMockito.mockStatic(ConfigReader.class);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKREASONNOTIFICATIONDISABLED)).thenReturn("dummyMessage");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVBLOCKTYPEDRUGEXCLUDED)).thenReturn("dummyType");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMEOUTOFSTOCK)).thenReturn("OutOfStockCommunication");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMERXREJECTED)).thenReturn("RxRejectedCommunication");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMERXADJUDICATIONCOMPLETE)).thenReturn("RxInProcessCommunication");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_NAME_RX_READY_INSTORE_WITH_COPAY)).thenReturn("RxReadyWithCopay");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_NAME_RX_READY_WITH_COPAY_DELIVERY_LINK)).thenReturn("RxReadyDeliveryWithCopay");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMERXREADYINSTORE)).thenReturn("RxReadyInStore");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMERXREADYINSTOREFINAL)).thenReturn("RxReadyInStoreFinal");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_COMM_NAME_RX_READY_DELIVERY_LINK_FINAL)).thenReturn("RxReadyDeliveryFinal");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMEPARTIALFILL)).thenReturn("RxPartialFill");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMEPARTIALFILLONHOLD)).thenReturn("RxPartialFillOnHold");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMECENTRALFILLDELAYED)).thenReturn("RxCentralFillDelayed");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMETHIRDPARTYEXCEPTION)).thenReturn("RxThirdPartyException");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMECALLPRESCRIBER)).thenReturn("RxCallPrescriber");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTCOMMNAMEEPRESCRIPTIONRECEIVED)).thenReturn("RxEPrescriptionReceived");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVTIMEZONEUTC)).thenReturn("UTC");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CVDATETIMEFORMATDOB)).thenReturn("yyyy-MM-dd HH:mm:ss");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.LANG_CODE,
				ConfigKeys.CV_HOW_LONG_IN_HOURS_BEFORE_SENDING_SAME_NOTIFICATION_FOR_PRESC)).thenReturn("24");
		
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, ConfigKeys.CV_ORDER_URL_EXPIRATION_PERIOD)).thenReturn("15");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, ConfigKeys.CV_CLIENT_NAME)).thenReturn("Test");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, ConfigKeys.CV_URL_SHORTNER_SERVICE_URL)).thenReturn("http://m-rx.bz/shorten");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, ConfigKeys.CV_PATIENT_PORTAL_URL_SHORTNER_APP_NAME)).thenReturn("PatientPortal");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, ConfigKeys.CV_CHECKOUT_LINK_EXPIRY_URL)).thenReturn("checkoutUrl");
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, "cvSHAAlgorithm")).thenReturn("SHA-256");
		
		
		
	}
	
	private PatientPaymentMethodService mockPatientPaymentMethodService;

	@Before
	public void initializeMocks() {
		mockPatientPaymentMethodService = mock(PatientPaymentMethodService.class);
		eopnUtils.setPatientPaymentMethodService(mockPatientPaymentMethodService);
	}

	@Test
	public void testPrepareCommonCommunicationContent_populateAllTheCommonPlaceholders() throws MscriptsException {

		List<PatientData> patientData = new ArrayList<PatientData>();
		Map<String, String> customerPreference = new HashMap<>();

		customerPreference.put((Constants.CLIENT_NAME), "TestName");
		customerPreference.put((Constants.ADDRESS_LINE_1), "TestAddress");
		customerPreference.put((Constants.STORE_PHONE_NUMBER), "4156047502");
		customerPreference.put((Constants.STORE_NAME), "TestPharmacy");
		customerPreference.put((Constants.STORE_CITY_PREFERENCE), "StoreCity");

		PatientData data = new PatientData();
		data.setPatientFirstName("TestFirstName");
		patientData.add(data);

		Map<String, String> communicationContent = eopnUtils.prepareCommonCommunicationContent(customerPreference,
				patientData);

		try {
			assertEquals(communicationContent.get(CommunicationTemplateStrings.CLIENT_NAME.getTemplateString()),
					customerPreference.get(Constants.CLIENT_NAME));
			assertEquals(communicationContent.get(CommunicationTemplateStrings.CUSTOMER_FIRST_NAME.getTemplateString()),
					patientData.get(0).getPatientFirstName().toUpperCase());
			assertEquals(communicationContent.get(CommunicationTemplateStrings.STORE_ADDRESS_LINE1.getTemplateString()),
					customerPreference.get(Constants.ADDRESS_LINE_1));
			assertEquals(communicationContent.get(CommunicationTemplateStrings.STORE_PHONE_NUMBER.getTemplateString()),
					customerPreference.get(Constants.STORE_PHONE_NUMBER));
			assertEquals(communicationContent.get(CommunicationTemplateStrings.STORE_NAME.getTemplateString()),
					customerPreference.get(Constants.STORE_NAME));
			assertEquals(communicationContent.get(CommunicationTemplateStrings.STORE_CITY.getTemplateString()),
					customerPreference.get(Constants.STORE_CITY_PREFERENCE));
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

	@Test(expected = MscriptsException.class)
	public void testPrepareCommonCommunicationContent_WithNoPatientORClientData() throws MscriptsException {
		List<PatientData> patientData = new ArrayList<PatientData>();
		Map<String, String> customerPreference = new HashMap<>();

		eopnUtils.prepareCommonCommunicationContent(customerPreference,
				patientData);
	}

	@Test
	public void test_blockAndDeleteIfCustomerNotificationDisabled_success() throws Exception {
		List<PatientData> patientDataList = new ArrayList<PatientData>();
		PatientData patientData = new PatientData();
		patientData.setPatientFirstName("dummyFirstName");
		patientData.setPatientLastName("dummyLastName");
		patientDataList.add(patientData);
		List<TransactionData> transactionDataList = new ArrayList<TransactionData>();
		TransactionData transactionData = new TransactionData();
		transactionData.setTxNumber("1234");
		transactionDataList.add(transactionData);
		Map patientDetailsMap = new HashedMap();
		doNothing().when(mockPatientNotificationDao).insertOrUpdateJdbcData(anyString(), (Object[]) anyObject());
		when(mockNotificationFilterUtil.isCustomerNotifcationEnabled(anyString(), anyString(), anyString()))
				.thenReturn(false);
		boolean actaulIsDrugExcluded = eopnUtils.blockAndDeleteIfCustomerNotificationDisabled("1","1234",
				"dummy", patientDataList, "dummyFinalKey", transactionDataList, patientDetailsMap);
		assertEquals(true, actaulIsDrugExcluded);
	}

	@Test
	public void testProcessPatientCreditCard_basicCheck() throws MscriptsException {
		List<CreditCard> patientCreditCardList = new ArrayList<>();
		CreditCard CreditCard = new CreditCard();
		CreditCard.setType("1");
		CreditCard.setLastFourDigits("4444");
		CreditCard.setExpiryDate("2020");
		patientCreditCardList.add(CreditCard);
		eopnUtils.processPatientCreditCard(1, 1L, patientCreditCardList);

		ArgumentCaptor<Long> ParameterLongCaptorClientId =  ArgumentCaptor.forClass(Long.class); 
		ArgumentCaptor<Long> ParameterLongCaptorCustomerId =  ArgumentCaptor.forClass(Long.class); 
		ArgumentCaptor<String> ParameterStringCaptorPaymentTypeName =  ArgumentCaptor.forClass(String.class); 
		ArgumentCaptor<String> ParameterStringCaptorLastFourDigits =  ArgumentCaptor.forClass(String.class); 
		ArgumentCaptor<String> ParameterStringCaptorExpiryDate =  ArgumentCaptor.forClass(String.class); 
		ArgumentCaptor<String> ParameterStringCaptorPriorityOrder =  ArgumentCaptor.forClass(String.class); 
		ArgumentCaptor<String> ParameterStringCaptorCcToken =  ArgumentCaptor.forClass(String.class); 
		ArgumentCaptor<String> ParameterStringCaptorpatientPaymentAddressId =  ArgumentCaptor.forClass(String.class); 
		ArgumentCaptor<Boolean> ParameterBooleanCaptorpatientIsAutoRefillCard =  ArgumentCaptor.forClass(Boolean.class); 


		verify(mockPatientPaymentMethodService).createPatientPaymentMethod(ParameterLongCaptorClientId.capture(), 
				ParameterLongCaptorCustomerId.capture(), ParameterStringCaptorPaymentTypeName.capture(),
				ParameterStringCaptorLastFourDigits.capture(), 
				ParameterStringCaptorExpiryDate.capture(), ParameterStringCaptorPriorityOrder.capture(),
				ParameterStringCaptorCcToken.capture(), ParameterStringCaptorpatientPaymentAddressId.capture(),
				ParameterBooleanCaptorpatientIsAutoRefillCard.capture());

		Boolean assertion = (1 == ParameterLongCaptorClientId.getValue() &&
				1L == ParameterLongCaptorCustomerId.getValue());
		assertTrue(assertion);

		assertEquals("1",ParameterStringCaptorPaymentTypeName.getValue());
		assertEquals("4444",ParameterStringCaptorLastFourDigits.getValue());
		assertEquals("20/20",ParameterStringCaptorExpiryDate.getValue());
		assertEquals("1",ParameterStringCaptorPriorityOrder.getValue());
		assertEquals(null,ParameterStringCaptorCcToken.getValue());
		assertEquals(null,ParameterStringCaptorpatientPaymentAddressId.getValue());
		assertEquals(false,ParameterBooleanCaptorpatientIsAutoRefillCard.getValue());

	}
	
	
	@Test
	public void testGetOrderUrl_withExistingValidURL() throws Exception {
		Map orderUrlMap = new HashMap<String,String>();
		orderUrlMap.put(Constants.ORDER_URL, "http://orderurl.com");
		
		when(mockNotificationDao.getOrderUrl(anyString(), anyString())).thenReturn(orderUrlMap);
		
		
		String orderUrl = eopnUtils.getOrderUrl("1","1", "1234567890");
		
		assertEquals("http://orderurl.com", orderUrl);
		
	}
	
	@Test
	public void testGetOrderUrl_withNonExistingURLAndSharedMobile() throws Exception {
		Map orderUrlMap = new HashMap<String,String>();
		orderUrlMap.put("exists", "TRUE");
		orderUrlMap.put("is_shared", "TRUE");
		when(mockNotificationDao.getOrderUrl(anyString(), anyString())).thenReturn(orderUrlMap);
		when(mockNotificationDao.updateMscriptsProxyAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("http://patientPortalUrl.com");
		when(mockMscriptsCommonService.isSharedMobile(anyString(), anyString(), anyString(), anyString(), anyString()))
			.thenReturn(orderUrlMap);
		
		ArgumentCaptor<String> redirectParamsCaptor =  ArgumentCaptor.forClass(String.class);		
		
		when(mockUrlShortnerService.URLShortnerServiceWithRedirectParamsAndExpUrl(anyString(), anyString(), anyString(), anyString(), redirectParamsCaptor.capture(), anyInt(), anyString()))
		.thenReturn("http://newShortenedURL.com");
		doNothing().when(mockNotificationDao).insertOrUpdateJdbcData(anyString(), (Object[]) anyObject());
		
		String orderUrl = eopnUtils.getOrderUrl("1","1", "1234567890");
		
		assertEquals(true, redirectParamsCaptor.getValue().endsWith("&shr=1"));
		
		assertEquals("http://newShortenedURL.com", orderUrl);
	
	}
	
	@Test
	public void testGetOrderUrl_withNonExistingURLAndNonSharedMobile() throws Exception {
		Map orderUrlMap = new HashMap<String,String>();
		orderUrlMap.put(Constants.ORDER_URL, "");
		orderUrlMap.put("exists", "TRUE");
		orderUrlMap.put("is_shared", "FALSE");
		when(mockNotificationDao.getOrderUrl(anyString(), anyString())).thenReturn(orderUrlMap);
		when(mockNotificationDao.updateMscriptsProxyAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("http://patientPortalUrl.com");
		when(mockMscriptsCommonService.isSharedMobile(anyString(), anyString(), anyString(), anyString(), anyString()))
			.thenReturn(orderUrlMap);
		
		ArgumentCaptor<String> redirectParamsCaptor =  ArgumentCaptor.forClass(String.class);		
		
		when(mockUrlShortnerService.URLShortnerServiceWithRedirectParamsAndExpUrl(anyString(), anyString(), anyString(), anyString(), redirectParamsCaptor.capture(), anyInt(), anyString()))
		.thenReturn("http://newShortenedURL.com");
		doNothing().when(mockNotificationDao).insertOrUpdateJdbcData(anyString(), (Object[]) anyObject());
		
		String orderUrl = eopnUtils.getOrderUrl("1","1", "1234567890");
		
		assertEquals(true, redirectParamsCaptor.getValue().endsWith("&shr=0"));
		
		assertEquals("http://newShortenedURL.com", orderUrl);
	
	}
	
	
	@Test
	public void testGetOrderUrl_withInvalidURL() throws Exception {
		Map orderUrlMap = new HashMap<String,String>();
		orderUrlMap.put(Constants.ORDER_URL, "INVALID");
		orderUrlMap.put("exists", "FALSE");
		orderUrlMap.put("is_shared", "TRUE");
		when(mockNotificationDao.getOrderUrl(anyString(), anyString())).thenReturn(orderUrlMap);
		when(mockNotificationDao.updateMscriptsProxyAccessToken(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn("http://patientPortalUrl.com");
		when(mockMscriptsCommonService.isSharedMobile(anyString(), anyString(), anyString(), anyString(), anyString()))
			.thenReturn(orderUrlMap);
		when(ConfigReader.readConfig(Constants.cvDefaultClientId,
				Constants.LANG_CODE, ConfigKeys.CV_ORDER_URL_EXPIRATION_PERIOD)).thenReturn(null);
		
		ArgumentCaptor<String> redirectParamsCaptor =  ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> objectCaptor =  ArgumentCaptor.forClass(Object.class);	
		
		when(mockUrlShortnerService.URLShortnerServiceWithRedirectParamsAndExpUrl(anyString(), anyString(), anyString(), anyString(), redirectParamsCaptor.capture(), anyInt(), anyString()))
		.thenReturn("http://newShortenedURL.com");
		doNothing().when(mockNotificationDao).insertOrUpdateJdbcData(anyString(), (Object[]) objectCaptor.capture());
		
		String orderUrl = eopnUtils.getOrderUrl("1","1", "1234567890");
		
		assertEquals(true, redirectParamsCaptor.getValue().endsWith("&shr=0"));
		
	    List<Object> actualArgs = Arrays.asList((Object[]) objectCaptor.getValue());
		
		assertEquals("http://newShortenedURL.com", actualArgs.get(0));
		
		assertEquals("http://newShortenedURL.com", orderUrl);
	
	}
	

}
