/*
 * Property of mscripts, LLC 2016
 */
package com.mscripts.externalrequesthandler.domain;

import java.util.List;

/**
 * @author Manigandan Shri <mshri@mscripts.com>
 */
public class AdhPatientCommn {

    private int patientId;
    private String pharmacyIdentifier;
    private int pharmacyId;
    private List<AdhCommn> eligibleCommnList;

    /**
     * @return the patientId
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * @param patientId the patientId to set
     */
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    /**
     * @return the pharmacyIdentifier
     */
    public String getPharmacyIdentifier() {
        return pharmacyIdentifier;
    }

    /**
     * @param pharmacyIdentifier the pharmacyIdentifier to set
     */
    public void setPharmacyIdentifier(String pharmacyIdentifier) {
        this.pharmacyIdentifier = pharmacyIdentifier;
    }

    /**
     * @return the pharmacyId
     */
    public int getPharmacyId() {
        return pharmacyId;
    }

    /**
     * @param pharmacyId the pharmacyId to set
     */
    public void setPharmacyId(int pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

	public List<AdhCommn> getEligibleCommnList() {
		return eligibleCommnList;
	}

	public void setEligibleCommnList(List<AdhCommn> eligibleCommnList) {
		this.eligibleCommnList = eligibleCommnList;
	}    

}
