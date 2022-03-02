/*******************************************************
Title               : NotificationDao.java
Author              : Abhinandan U S
Description         : Interface for NotificationDao.
Modification History: Not Applicable
Created             : 18-Jan-10
Modified            : Not Applicable
Notes               : None
 *******************************************************/

package com.mscripts.externalrequesthandler.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mscripts.domain.SendCommunicationMode;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.domain.CustomerPrescription;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionFile;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionTxtpfile;
import com.mscripts.externalrequesthandler.domain.InsuranceCard;
import com.mscripts.externalrequesthandler.domain.InsuranceCardLink;
import com.mscripts.utils.mscriptsException;

/**
 *
 * @author abhinandanus
 */
public interface NotificationDao {

	public boolean updateCustomerEmail(String customerid, String emailid, String deleteEmail, String clientID,
			String accessKey) throws mscriptsException;

	public void logPrescriptionTransactions(String clientID, String customerID, String rxnumber, String storencpdp,
			CustomerTransactionFile[] custTxFile) throws mscriptsException;

	public Map updateCustomerPrescriptions(String customerID, CustomerPrescription customerPres, String clientID,
			String messageDate, String secondaryKey,Map<String, Object> inputParamsMap) throws MscriptsException;

	public void updateBulkRxPickupErrorNotes(String clientID, String recordID, String errorNotes, SendCommunicationMode sendCommunication) throws MscriptsException;

	public void deleteBulkRxPickup(String clientID, String recordID) throws mscriptsException;

	public void updatePatient(String clientID, String customerID, String deceased) throws mscriptsException;

	public void updateCustomerCard(String clientID, String customerID, InsuranceCard insCard, String secondaryKey)
			throws mscriptsException;

	public void updateCustomerCardLink(String clientID, String customerID, InsuranceCardLink insCardLink,
			String secondaryKey) throws mscriptsException;

	public Map getCustomerReminderPreferences(String ClientId, String CustomerId) throws mscriptsException;

	public Map getRefillReminderDetails(String clientID, String customerID) throws mscriptsException;

	public void addRefillReminderDetails(String clientID, String customerID, String prescriptionIdList,
			String additionalReminderDate, String reminderOn, String remindBefore, String noOfReminders,
			String sendHour, String refillAllPrescriptions, String language)
			throws mscriptsException, MscriptsException;

	public Map selectRxPickupReminderInstancesRecords(String clientID, String brpiID, String secondaryKey)
			throws mscriptsException;

	public void moveTempValueToPatientTxn(Map rxStatus, String cvRxAdjudicatedStatus, String clientID,
			String customerID, String rxNumber, String storeNCPDPId) throws mscriptsException;

	public void updateRxcomId(String sClientId, String sCustomerId, String sRxcomId) throws mscriptsException;

	public void insertOrUpdateJdbcData(String sqlString, Object[] objArr) throws mscriptsException;

	/**
	 * 
	 * @param clientId
	 * @param customerId
	 * @param cvIsError
	 * @throws mscriptsException
	 */
	public void processPickupReminderForMigratedUser(String clientId, String customerId, int isMUPickupEligible,
			String cvAccelaretRxPickupType, int cvIsError) throws mscriptsException;
	
	public String updateMscriptsProxyAccessToken(String clientId, String customerId, String childCustomerId,
			String shaCode, String type, String language) throws MscriptsException;
	public Map getOrderUrl(String clientID, String customerID) throws MscriptsException;

	String getCommunicationName(String communicationId, String clientId) throws MscriptsException;
	
	/**
	 * Method to update the record type of a customer into the <tt>customers</tt> table
	 * 
	 * @author dbhat
	 * 
	 * @param clientId - A {@link String} containing the ID from the <tt>clients</tt> table
	 * @param customerId - A {@link String} containing the ID from the <tt>customers</tt> table
	 * @param recordType - A {@link String} containing the <tt>type</tt> of record<tt>(1 for Person & 2 for Pet)</tt>
	 * 
	 * @throws MscriptsException
	 */
	public void updateRecordType(String clientId, String customerId, String recordType) throws MscriptsException;

	/**
	 * Method to log the insurance card transactions associated with the prescription into the rx_refill_txn_txtpfile table
	 * 
	 * @param clientId - String containing the ID from the clients table
	 * @param customerID - String containing the ID from the customers table
	 * @param rxNumber - String containing the rx_number associated with the prescription
	 * @param storeNCPDP - String containing the NCPDP ID of a store
	 * @param customerTransactionFile - Array of the type CustomerTransactionFile which contains insurance card transactions for a prescription
	 * @throws McriptsException
	 */
	public void logInsuranceCardTransactions(String clientID, String customerID,  String rxNumber, String storeNCPDP,
			CustomerTransactionFile[] customerTransactionFiles) throws MscriptsException;

	/**
	 * This method fetches you days and quantity for a given customer prescription id.
	 * @param clientID
	 * @param customerPrescriptionId
	 * @return Map<String, String>
	 * @throws MscriptsException
	 */
	public Map<String, String> getRxDetailsForDaysQtyClients(String clientID, String customerPrescriptionId)
			throws MscriptsException;
	
	/**
	 * Method to update insurance card transactions associated with the prescription in the rx_refill_txn_txtpfile table
	 * 
	 * @param clientId - String containing the ID from the clients table
	 * @param txtpId - String containing the ID from the customers table
	 * @param rxRefillTxnId - String containing the rx_number associated with the prescription
	 * @param txtpUpdate - List of type CustomerTransactionTxtpfile that contains insurance card details to be updated
	 * @throws McriptsException
	 */
	public void updateInsuranceCard(final String clientID, final List<String> txtpId, final Map<String, String> rxRefillTxnMap,
			 final List<CustomerTransactionTxtpfile> txtpUpdate) throws MscriptsException ;
	
	/**
	 * Method to insert insurance card transactions associated with the prescription into the rx_refill_txn_txtpfile table
	 * 
	 * @param clientId - String containing the ID from the clients table
	 * @param rxRefillTxnId - String containing the ID from the customers table
	 * @param txtpInsert - List of type CustomerTransactionTxtpfile that contains insurance card details to be inserted into rx_refill_txn_txtpfile table
	 * @throws McriptsException
	 */
	public void insertInsuranceCard(final String clientID, Map<String, String> rxRefillTxnMap,
			 final List<CustomerTransactionTxtpfile> txtpInsert) throws MscriptsException ; 
	
}
