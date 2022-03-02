/*
 * Property of mscripts, LLC 2016
 */
package com.mscripts.externalrequesthandler.domain;

/**
 *
 * @author Manigandan Shri <mshri@mscripts.com>
 */
public class AdhInterceptMsgRequest {
    private String pharmacyIdentifier;
    private long patientId;
    private long patientRxId;
    private String mediation;

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
     * @return the patientId
     */
    public long getPatientId() {
        return patientId;
    }

    /**
     * @param patientId the patientId to set
     */
    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    /**
     * @return the patientRxId
     */
    public long getPatientRxId() {
        return patientRxId;
    }

    /**
     * @param patientRxId the patientRxId to set
     */
    public void setPatientRxId(long patientRxId) {
        this.patientRxId = patientRxId;
    }

    /**
     * @return the mediation
     */
    public String getMediation() {
        return mediation;
    }

    /**
     * @param mediation the mediation to set
     */
    public void setMediation(String mediation) {
        this.mediation = mediation;
    }
    
}
