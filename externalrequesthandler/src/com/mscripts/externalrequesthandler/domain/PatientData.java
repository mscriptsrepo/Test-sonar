package com.mscripts.externalrequesthandler.domain;

import java.util.List;

public class PatientData {

	private String patientLastName;
	private String patientFirstName;
	private String patientMiddleName;
	private String patientSuffix;
	private String patientGender;
	private String patientLanguage;
	private String patientBirthDate;
	private String patientRelation;
	private String patientAddress1;
	private String patientAddress2;
	private String patientCity;
	private String patientState;
	private String patientZipCode;
	private String contactWhenReady;
	private String contactEmailAddress;
	private String contactPhoneNumber;
	private String contactSMSNumber;
	private String deliveryPreference;
	private String patientMobileService;
	private String loyaltyCardNumber;
	private String loyaltyCardOptOut;
	private String communicationConsent;
	private List<CreditCard> creditCard;
	private String syncScriptEnrolled;
	private String rxcomPatientID;
	private String patientDisallowAutofill;
	

	public String getRxcomPatientID() {
		return rxcomPatientID;
	}

	public void setRxcomPatientID(String rxcomPatientID) {
		this.rxcomPatientID = rxcomPatientID;
	}

	public String getSyncScriptEnrolled() {
		return syncScriptEnrolled;
	}

	public void setSyncScriptEnrolled(String syncScriptEnrolled) {
		this.syncScriptEnrolled = syncScriptEnrolled;
	}

	public String getLoyaltyCardOptOut() {
		return loyaltyCardOptOut;
	}

	public void setLoyaltyCardOptOut(String loyaltyCardOptOut) {
		this.loyaltyCardOptOut = loyaltyCardOptOut;
	}

	public String getPatientLastName() {
		return patientLastName;
	}

	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}

	public String getPatientFirstName() {
		return patientFirstName;
	}

	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}

	public String getPatientMiddleName() {
		return patientMiddleName;
	}

	public void setPatientMiddleName(String patientMiddleName) {
		this.patientMiddleName = patientMiddleName;
	}

	public String getPatientSuffix() {
		return patientSuffix;
	}

	public void setPatientSuffix(String patientSuffix) {
		this.patientSuffix = patientSuffix;
	}

	public String getPatientGender() {
		return patientGender;
	}

	public void setPatientGender(String patientGender) {
		this.patientGender = patientGender;
	}

	public String getPatientLanguage() {
		return patientLanguage;
	}

	public void setPatientLanguage(String patientLanguage) {
		this.patientLanguage = patientLanguage;
	}

	public String getPatientBirthDate() {
		return patientBirthDate;
	}

	public void setPatientBirthDate(String patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}

	public String getPatientRelation() {
		return patientRelation;
	}

	public void setPatientRelation(String patientRelation) {
		this.patientRelation = patientRelation;
	}

	public String getPatientAddress1() {
		return patientAddress1;
	}

	public void setPatientAddress1(String patientAddress1) {
		this.patientAddress1 = patientAddress1;
	}

	public String getPatientAddress2() {
		return patientAddress2;
	}

	public void setPatientAddress2(String patientAddress2) {
		this.patientAddress2 = patientAddress2;
	}

	public String getPatientCity() {
		return patientCity;
	}

	public void setPatientCity(String patientCity) {
		this.patientCity = patientCity;
	}

	public String getPatientState() {
		return patientState;
	}

	public void setPatientState(String patientState) {
		this.patientState = patientState;
	}

	public String getPatientZipCode() {
		return patientZipCode;
	}

	public void setPatientZipCode(String patientZipCode) {
		this.patientZipCode = patientZipCode;
	}

	public String getContactWhenReady() {
		return contactWhenReady;
	}

	public void setContactWhenReady(String contactWhenReady) {
		this.contactWhenReady = contactWhenReady;
	}

	public String getContactEmailAddress() {
		return contactEmailAddress;
	}

	public void setContactEmailAddress(String contactEmailAddress) {
		this.contactEmailAddress = contactEmailAddress;
	}

	public String getContactPhoneNumber() {
		return contactPhoneNumber;
	}

	public void setContactPhoneNumber(String contactPhoneNumber) {
		this.contactPhoneNumber = contactPhoneNumber;
	}

	public String getContactSMSNumber() {
		return contactSMSNumber;
	}

	public void setContactSMSNumber(String contactSMSNumber) {
		this.contactSMSNumber = contactSMSNumber;
	}

	public String getDeliveryPreference() {
		return deliveryPreference;
	}

	public void setDeliveryPreference(String deliveryPreference) {
		this.deliveryPreference = deliveryPreference;
	}

	public String getPatientMobileService() {
		return patientMobileService;
	}

	public void setPatientMobileService(String patientMobileService) {
		this.patientMobileService = patientMobileService;
	}

	public String getLoyaltyCardNumber() {
		return loyaltyCardNumber;
	}

	public void setLoyaltyCardNumber(String loyaltyCardNumber) {
		this.loyaltyCardNumber = loyaltyCardNumber;
	}

	public String getCommunicationConsent() {
		return communicationConsent;
	}

	public void setCommunicationConsent(String communicationConsent) {
		this.communicationConsent = communicationConsent;
	}

	public List<CreditCard> getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(List<CreditCard> creditCard) {
		this.creditCard = creditCard;
	}

	public String getPatientDisallowAutofill() {
		return patientDisallowAutofill;
	}

	public void setPatientDisallowAutofill(String patientDisallowAutofill) {
		this.patientDisallowAutofill = patientDisallowAutofill;
	}
	
}
