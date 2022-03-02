package com.mscripts.externalrequesthandler.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PatientNotificationRequest")
public class PatientNotificationRequest {

	private String version;
	private String service;
	private String source;
	private String messageID;
	private String messageDate;
	private String clientID;
	private List<PatientData> patientData;
//	private List<CreditCard> creditCard;
	private List<PharmacyData> pharmacyData;
	private List<PrescriberData> prescriberData;
	private List<TransactionData> transactionData;
	private InsurancePlanData insurancePlanData;

	@XmlAttribute
	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	@XmlAttribute
	public String getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(String messageDate) {
		this.messageDate = messageDate;
	}

	@XmlAttribute
	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	@XmlAttribute
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	@XmlAttribute
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@XmlAttribute
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@XmlElement
	public List<PatientData> getPatientData() {
		return patientData;
	}

	public void setPatientData(List<PatientData> patientData) {
		this.patientData = patientData;
	}

//	@XmlElement
//	public List<CreditCard> getCreditCard() {
//		return creditCard;
//	}
//
//	public void setCreditCard(List<CreditCard> creditCard) {
//		this.creditCard = creditCard;
//	}

	@XmlElement
	public List<PharmacyData> getPharmacyData() {
		return pharmacyData;
	}

	public void setPharmacyData(List<PharmacyData> pharmacyData) {
		this.pharmacyData = pharmacyData;
	}

	@XmlElement
	public List<PrescriberData> getPrescriberData() {
		return prescriberData;
	}

	public void setPrescriberData(List<PrescriberData> prescriberData) {
		this.prescriberData = prescriberData;
	}

	@XmlElement
	public List<TransactionData> getTransactionData() {
		return transactionData;
	}

	public void setTransactionData(List<TransactionData> transactionData) {
		this.transactionData = transactionData;
	}
	
	@XmlElement
	public InsurancePlanData getInsurancePlanData() {
		return insurancePlanData;
	}

	public void setInsurancePlanData(InsurancePlanData insurancePlanData) {
		this.insurancePlanData = insurancePlanData;
	}

}
