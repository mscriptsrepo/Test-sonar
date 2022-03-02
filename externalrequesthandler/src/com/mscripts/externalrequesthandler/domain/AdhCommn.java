/*
 * Property of mscripts, LLC 2016
 */
package com.mscripts.externalrequesthandler.domain;

/**
 * @author Manigandan Shri <mshri@mscripts.com>
 */
public class AdhCommn {

    private long programId;
    private long programCommnId;
    private boolean isEligible;
    private String rationality;
    private boolean isTextMsg;
    private String[] textMsg;

    /**
     * @return the programId
     */
    public long getProgramId() {
        return programId;
    }

    /**
     * @param programId the programId to set
     */
    public void setProgramId(long programId) {
        this.programId = programId;
    }

    /**
     * @return the programCommnId
     */
    public long getProgramCommnId() {
        return programCommnId;
    }

    /**
     * @param programCommnId the programCommnId to set
     */
    public void setProgramCommnId(long programCommnId) {
        this.programCommnId = programCommnId;
    }

    /**
     * @return the isEligible
     */
    public boolean isEligible() {
        return isEligible;
    }

    /**
     * @param isEligible the isEligible to set
     */
    public void setIsEligible(boolean isEligible) {
        this.isEligible = isEligible;
    }

    /**
     * @return the rationality
     */
    public String getRationality() {
        return rationality;
    }

    /**
     * @param rationality the rationality to set
     */
    public void setRationality(String rationality) {
        this.rationality = rationality;
    }

    /**
     * @return the isTextMsg
     */
    public boolean isTextMsg() {
        return isTextMsg;
    }

    /**
     * @param isTextMsg the isTextMsg to set
     */
    public void setIsTextMsg(boolean isTextMsg) {
        this.isTextMsg = isTextMsg;
    }

    /**
     * @return the textMsg
     */
    public String[] getTextMsg() {
        return textMsg;
    }

    /**
     * @param textMsg the textMsg to set
     */
    public void setTextMsg(String[] textMsg) {
        this.textMsg = textMsg;
    }

}
