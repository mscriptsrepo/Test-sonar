package com.mscripts.externalrequesthandler.domain;

public class InsurancePlanData {

	private String bin;
	private String pcn;
	private String cardholderID;
	private String groupID;
	private String planType;
	private String insuranceDisallowAutofill;

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getPcn() {
		return pcn;
	}

	public void setPcn(String pcn) {
		this.pcn = pcn;
	}

	public String getCardholderID() {
		return cardholderID;
	}

	public void setCardholderID(String cardholderID) {
		this.cardholderID = cardholderID;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getPlanType() {
		return planType;
	}

	public void setPlanType(String planType) {
		this.planType = planType;
	}

	public String getInsuranceDisallowAutofill() {
		return insuranceDisallowAutofill;
	}

	public void setInsuranceDisallowAutofill(String insuranceDisallowAutofill) {
		this.insuranceDisallowAutofill = insuranceDisallowAutofill;
	}
	
}
