/*******************************************************
Title               : NotificationService.java
Author              : Abhinandan U S
Description         : NotificationService Interface
Modification History: Not Applicable
Created             : 18-Jan-10
Modified            : Not Applicable
Notes               : None
 *******************************************************/

package com.mscripts.externalrequesthandler.service;

import org.w3c.dom.NodeList;

import com.mscripts.exceptions.MscriptsException;
import com.mscripts.utils.mscriptsException;
/**
 *
 * @author abhinandanus
 */
public interface NotificationService {

    public boolean updateCustomerEmail(String customerid, String emailid,String deleteEmail,String clientID) throws mscriptsException;
    
	public void updateCustomerPrescription(String customerID, String prescriptionXml, String clientID,
			String patientUpdateNode, String messageDate) throws mscriptsException;
    
    public void updatePatient(String clientID, String customerID, String deceased) throws mscriptsException;
    
    public void updateInsurance(String clientID, String customerID,NodeList insuranceCardNode,NodeList insuranceLinkNode)throws mscriptsException;

	public void updateRxcomId(String sClientId, String sCustomerId, String sRxcomId) throws mscriptsException;
	
	/**
	 * Method to update the record type of a customer
	 * 
	 * @author dbhat
	 * 
	 * @param clientId - A {@link String} containing the ID from the <tt>clients</tt> table
	 * @param customerId - A {@link String} containing the ID from the <tt>customers</tt> table
	 * @param recordType - Number representing the record type <tt>(1 for Person & 2 for Pet)</tt>
	 * 
	 * @throws MscriptsException
	 */
	public void updateRecordType(String clientId, String customerId, String recordType) throws MscriptsException;

}
