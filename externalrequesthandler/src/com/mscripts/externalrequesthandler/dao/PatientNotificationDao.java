package com.mscripts.externalrequesthandler.dao;

import java.util.List;
import java.util.Map;

import com.mscripts.exceptions.MscriptsException;

public interface PatientNotificationDao {
	
	public Map<String, String> jdbcDataGet(String sqlString, Object[] args);
	
	public void insertOrUpdateJdbcData(String sqlString, Object[] objArr) throws MscriptsException;
	
	public List<String> jdbcDataGetList(String sqlString, Object[] args) throws MscriptsException;

	//TXN_STATUS_AND_CUSTOMER_PRESCRIPTION_REFILL_STATUS_BY_RXNUMBER
	public Map<String, Object> getTxnStatusAndCustomerPrxRefillStatusByRxNumber( Object[] objArr) throws MscriptsException;
	
}
