package com.mscripts.externalrequesthandler.domain;

import java.util.List;

public class PrescriberData {

	private String prescriberLastName;
	private String prescriberFirstName;
	private String prescriberMiddleName;
	private String prescriberAddress1;
	private String prescriberAddress2;
	private String prescriberCity;
	private String prescriberState;
	private String prescriberZipCode;
	private String prescriberPhoneNumber;
	private String prescriberFaxNumber;
	private String prescriberTaxonomyCode;
	private List<PrescriberID> prescriberID;
	private String prescriberDisallowAutofill;

	public List<PrescriberID> getPrescriberID() {
		return prescriberID;
	}

	public void setPrescriberID(List<PrescriberID> prescriberID) {
		this.prescriberID = prescriberID;
	}

	public String getPrescriberLastName() {
		return prescriberLastName;
	}

	public void setPrescriberLastName(String prescriberLastName) {
		this.prescriberLastName = prescriberLastName;
	}

	public String getPrescriberFirstName() {
		return prescriberFirstName;
	}

	public void setPrescriberFirstName(String prescriberFirstName) {
		this.prescriberFirstName = prescriberFirstName;
	}

	public String getPrescriberMiddleName() {
		return prescriberMiddleName;
	}

	public void setPrescriberMiddleName(String prescriberMiddleName) {
		this.prescriberMiddleName = prescriberMiddleName;
	}

	public String getPrescriberAddress1() {
		return prescriberAddress1;
	}

	public void setPrescriberAddress1(String prescriberAddress1) {
		this.prescriberAddress1 = prescriberAddress1;
	}

	public String getPrescriberAddress2() {
		return prescriberAddress2;
	}

	public void setPrescriberAddress2(String prescriberAddress2) {
		this.prescriberAddress2 = prescriberAddress2;
	}

	public String getPrescriberCity() {
		return prescriberCity;
	}

	public void setPrescriberCity(String prescriberCity) {
		this.prescriberCity = prescriberCity;
	}

	public String getPrescriberState() {
		return prescriberState;
	}

	public void setPrescriberState(String prescriberState) {
		this.prescriberState = prescriberState;
	}

	public String getPrescriberZipCode() {
		return prescriberZipCode;
	}

	public void setPrescriberZipCode(String prescriberZipCode) {
		this.prescriberZipCode = prescriberZipCode;
	}

	public String getPrescriberPhoneNumber() {
		return prescriberPhoneNumber;
	}

	public void setPrescriberPhoneNumber(String prescriberPhoneNumber) {
		this.prescriberPhoneNumber = prescriberPhoneNumber;
	}

	public String getPrescriberFaxNumber() {
		return prescriberFaxNumber;
	}

	public void setPrescriberFaxNumber(String prescriberFaxNumber) {
		this.prescriberFaxNumber = prescriberFaxNumber;
	}

	public String getPrescriberTaxonomyCode() {
		return prescriberTaxonomyCode;
	}

	public void setPrescriberTaxonomyCode(String prescriberTaxonomyCode) {
		this.prescriberTaxonomyCode = prescriberTaxonomyCode;
	}

	public String getPrescriberDisallowAutofill() {
		return prescriberDisallowAutofill;
	}

	public void setPrescriberDisallowAutofill(String prescriberDisallowAutofill) {
		this.prescriberDisallowAutofill = prescriberDisallowAutofill;
	}

}
