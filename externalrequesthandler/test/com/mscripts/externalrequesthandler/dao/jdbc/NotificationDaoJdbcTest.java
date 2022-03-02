package com.mscripts.externalrequesthandler.dao.jdbc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mscripts.dao.QueryInvoker;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionFile;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionTxtpfile;

@PrepareForTest({ NotificationDaoJdbc.class })
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*" })
public class NotificationDaoJdbcTest {

	private NotificationDaoJdbc notificationDaoJdbc = new NotificationDaoJdbc();
	private QueryInvoker queryInvoker = mock(QueryInvoker.class);
	private static final String CLIENT_ID = "1";

	@Before
	public void initializeMocks() {
		notificationDaoJdbc.setQueryInvoker(queryInvoker);
	}

	/*
	 * Method to test if the required parameters are being passed while invoking the
	 * query
	 */
	@Test
	public void testlogInsuranceCardTransactions_testArguments() throws Exception {
		CustomerTransactionFile[] customerTransactionFiles = getCustomerTransactionFiles();
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put("id", "20");
		Object[] expectedArguments = { CLIENT_ID, "dummy", "dummy" };
		when(queryInvoker.invokeQueryMap(anyString(), (Object[]) anyObject())).thenReturn(returnMap);
		notificationDaoJdbc.logInsuranceCardTransactions(CLIENT_ID, "dummy", "dummy", "dummy",
				customerTransactionFiles);

		ArgumentCaptor<String> ParameterStringCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object[]> ParameterObjectCaptor = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(queryInvoker).invokeQueryMap(ParameterStringCaptor.capture(), ParameterObjectCaptor.capture());
		Object[] arguments = ParameterObjectCaptor.getValue();
		for (int i = 0; i < expectedArguments.length; i++)
			assertEquals(expectedArguments[i], arguments[i]);
	}

	private CustomerTransactionFile[] getCustomerTransactionFiles() {
		List<CustomerTransactionTxtpfile> customerTransactionTxtpfiles = new ArrayList<>();
		CustomerTransactionTxtpfile customerTransactionTxtpfile = new CustomerTransactionTxtpfile();
		customerTransactionTxtpfile.setCounter("0");
		customerTransactionTxtpfiles.add(customerTransactionTxtpfile);
		CustomerTransactionFile customerTransactionFile[] = new CustomerTransactionFile[1];
		customerTransactionFile[0] = new CustomerTransactionFile();
		customerTransactionFile[0].setCustomerTransactionTxtpfile(customerTransactionTxtpfiles);
		return customerTransactionFile;
	}

}
