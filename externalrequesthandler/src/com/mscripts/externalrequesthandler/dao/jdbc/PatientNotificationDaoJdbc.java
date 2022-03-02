package com.mscripts.externalrequesthandler.dao.jdbc;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

import com.mscripts.dao.QueryInvoker;
import com.mscripts.dao.SPInvoker;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.PatientNotificationDao;
import com.mscripts.utils.QueryBuilder;
import com.mscripts.utils.mscriptsExceptionSeverity;


public class PatientNotificationDaoJdbc implements PatientNotificationDao {
	private QueryInvoker queryInvoker;
	private SPInvoker spInvoker;
	
	private static final Logger LOGGER_PHI = LogManager
			.getLogger("phi." + PatientNotificationDaoJdbc.class.getName());

	public Map<String, String> jdbcDataGet(String sqlString, Object[] args) {
		try {
			return queryInvoker.invokeQueryMap(sqlString, args);
		} catch (EmptyResultDataAccessException e) {
			LOGGER_PHI.error("Empty Result set from DB, Sql query = " + sqlString );
			return null;
		}
	}
	public void insertOrUpdateJdbcData(String sqlString, Object[] objArr) throws MscriptsException{
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        try {
        	queryInvoker.updateUsingSqlString(sqlString,objArr);
        } catch (Exception e) {
			throw new MscriptsException(null, e.getMessage(), e, errorSeverity);
        }
	}
	
	public List<String> jdbcDataGetList(String sqlString, Object[] objArr) throws MscriptsException{
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        try {
        	return queryInvoker.executeQueryListOfString(sqlString,objArr);
        } catch (Exception e) {
        	throw new MscriptsException(null, e.getMessage(), e, errorSeverity);
        }
	}
	
	public Map<String, Object> getTxnStatusAndCustomerPrxRefillStatusByRxNumber( Object[] objArr) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Map<String, Object> result = null;
		try {			
			result = queryInvoker.executeQueryForMap(
					QueryBuilder.GET_TXN_STATUS_AND_CUSTOMER_PRESCRIPTION_REFILL_STATUS_BY_RXNUMBER,
					objArr);
		}
		catch (Exception ex) {
			LOGGER_PHI.error("Error occured while getting prescription and prescription transaction info: ", ex);
			throw new MscriptsException(null, ex.getMessage(), ex, errorSeverity);
		}
		return result;
	}

	public QueryInvoker getQueryInvoker() {
		return queryInvoker;
	}
	public void setQueryInvoker(QueryInvoker queryInvoker) {
		this.queryInvoker = queryInvoker;
	}
	public SPInvoker getSpInvoker() {
		return spInvoker;
	}
	public void setSpInvoker(SPInvoker spInvoker) {
		this.spInvoker = spInvoker;
	}
		
}
