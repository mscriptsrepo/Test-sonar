package com.mscripts.externalrequesthandler.domain;

public class PharmacyData {

	private String pharmacyChainID;
	private String pharmacyNPI;
	private String pharmacyNCPDP;

	public String getPharmacyChainID() {
		return pharmacyChainID;
	}

	public void setPharmacyChainID(String pharmacyChainID) {
		this.pharmacyChainID = pharmacyChainID;
	}

	public String getPharmacyNPI() {
		return pharmacyNPI;
	}

	public void setPharmacyNPI(String pharmacyNPI) {
		this.pharmacyNPI = pharmacyNPI;
	}

	public String getPharmacyNCPDP() {
		return pharmacyNCPDP;
	}

	public void setPharmacyNCPDP(String pharmacyNCPDP) {
		this.pharmacyNCPDP = pharmacyNCPDP;
	}
}
