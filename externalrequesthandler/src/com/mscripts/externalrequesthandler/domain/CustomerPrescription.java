/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.domain;

/**
 *
 * @author ssreeraj
 */
public class CustomerPrescription {

    private String rxNum;
    private String sgCode;
    private String sched;
    private String refAut;
    private String refRem;
    private String prefill;
    private String storeNCPDP;
    private String first;
    private String expire;
    private String sigText;
    private String prescribingDocName;
    private String prescDrugName;
    private String prescDrugNDC;
    private String prescDrugGPI;
    private String filled;
    private String quantity;
    private String days;
    private String anticipatedRefillDate;
    private String prescribingDocFname;
    private String prescribingDocMname;
    private String prescribingDocLname;
    private String prescribingDocCity;
    private String prescribingDocState;
    private String prescribingDocZip;
    private String prescribingDocDEA;
    private String prescribingDocAreaCode;
    private String prescribingDocPhone;
    private String prescribingDocFaxAreaCode;
    private String prescribingDocFaxPhone;
    private String oldrxnum;
    private String newrxnum;
    private String transfer;
    private String deleteRx;
    private int latestTxNumber;
    private String rxStatus;
    private String txStatus;
    private String latestSoldDate;
    private String willCallReady;
    private String deleteTx;
    private String whyDeact;
    private String txList;
    private CustomerTransactionFile[] customerTxFile;
    private String dispDrugName;
    private String dispDrugNDC;
    private String dispDrugGPI;
    private String copay;
    private String prescribingDocAddress1;
    private String prescribingDocAddress2;

    public String getPrescribingDocAddress1() {
		return prescribingDocAddress1;
	}

	public void setPrescribingDocAddress1(String prescribingDocAddress1) {
		this.prescribingDocAddress1 = prescribingDocAddress1;
	}

	public String getPrescribingDocAddress2() {
		return prescribingDocAddress2;
	}

	public void setPrescribingDocAddress2(String prescribingDocAddress2) {
		this.prescribingDocAddress2 = prescribingDocAddress2;
	}

	public String getWhyDeact() {
        return whyDeact;
    }

    public void setWhyDeact(String whyDeact) {
        this.whyDeact = whyDeact;
    }


    public String getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(String txStatus) {
        this.txStatus = txStatus;
    }

    public int getLatestTxNumber() {
        return latestTxNumber;
    }

    public void setLatestTxNumber(int latestTxNumber) {
        this.latestTxNumber = latestTxNumber;
    }

    public String getDeleteTx() {
        return deleteTx;
    }

    public void setDeleteTx(String deleteTx) {
        this.deleteTx = deleteTx;
    }

    public String getLatestSoldDate() {
        return latestSoldDate;
    }

    public void setLatestSoldDate(String latestSoldDate) {
        this.latestSoldDate = latestSoldDate;
    }

    public String getWillCallReady() {
        return willCallReady;
    }

    public void setWillCallReady(String willCallReady) {
        this.willCallReady = willCallReady;
    }

    public String getRxStatus() {
        return rxStatus;
    }

    public void setRxStatus(String rxStatus) {
        this.rxStatus = rxStatus;
    }

    public String getDeleteRx() {
        return deleteRx;
    }

    public void setDeleteRx(String deleteRx) {
        this.deleteRx = deleteRx;
    }

    public String getNewrxnum() {
        return newrxnum;
    }

    public void setNewrxnum(String newrxnum) {
        this.newrxnum = newrxnum;
    }

    public String getOldrxnum() {
        return oldrxnum;
    }

    public void setOldrxnum(String oldrxnum) {
        this.oldrxnum = oldrxnum;
    }

    public String getTransfer() {
        return transfer;
    }

    public void setTransfer(String transfer) {
        this.transfer = transfer;
    }

    public String getPrescribingDocAreaCode() {
        return prescribingDocAreaCode;
    }

    public void setPrescribingDocAreaCode(String prescribingDocAreaCode) {
        this.prescribingDocAreaCode = prescribingDocAreaCode;
    }

    public String getPrescribingDocCity() {
        return prescribingDocCity;
    }

    public void setPrescribingDocCity(String prescribingDocCity) {
        this.prescribingDocCity = prescribingDocCity;
    }

    public String getPrescribingDocDEA() {
        return prescribingDocDEA;
    }

    public void setPrescribingDocDEA(String prescribingDocDEA) {
        this.prescribingDocDEA = prescribingDocDEA;
    }

    public String getPrescribingDocFaxAreaCode() {
        return prescribingDocFaxAreaCode;
    }

    public void setPrescribingDocFaxAreaCode(String prescribingDocFaxAreaCode) {
        this.prescribingDocFaxAreaCode = prescribingDocFaxAreaCode;
    }

    public String getPrescribingDocFaxPhone() {
        return prescribingDocFaxPhone;
    }

    public void setPrescribingDocFaxPhone(String prescribingDocFaxPhone) {
        this.prescribingDocFaxPhone = prescribingDocFaxPhone;
    }

    public String getPrescribingDocFname() {
        return prescribingDocFname;
    }

    public void setPrescribingDocFname(String prescribingDocFname) {
        this.prescribingDocFname = prescribingDocFname;
    }

    public String getPrescribingDocMname() {
        return prescribingDocMname;
    }

    public void setPrescribingDocMname(String prescribingDocMname) {
        this.prescribingDocMname = prescribingDocMname;
    }

    public String getPrescribingDocLname() {
        return prescribingDocLname;
    }

    public void setPrescribingDocLname(String prescribingDocLname) {
        this.prescribingDocLname = prescribingDocLname;
    }

    public String getPrescribingDocPhone() {
        return prescribingDocPhone;
    }

    public void setPrescribingDocPhone(String prescribingDocPhone) {
        this.prescribingDocPhone = prescribingDocPhone;
    }

    public String getPrescribingDocState() {
        return prescribingDocState;
    }

    public void setPrescribingDocState(String prescribingDocState) {
        this.prescribingDocState = prescribingDocState;
    }

    public String getPrescribingDocZip() {
        return prescribingDocZip;
    }

    public void setPrescribingDocZip(String prescribingDocZip) {
        this.prescribingDocZip = prescribingDocZip;
    }

    /**
     * @return the rxNum
     */
    public String getRxNum() {
        return rxNum;
    }

    /**
     * @param rxNum the rxNum to set
     */
    public void setRxNum(String rxNum) {
        this.rxNum = rxNum;
    }

    public String getStoreNCPDP() {
        return storeNCPDP;
    }

    public void setStoreNCPDP(String storeNCPDP) {
        this.storeNCPDP = storeNCPDP;
    }

    /**
     * @return the sgCode
     */
    public String getSgCode() {
        return sgCode;
    }

    /**
     * @param sgCode the sgCode to set
     */
    public void setSgCode(String sgCode) {
        this.sgCode = sgCode;
    }

    /**
     * @return the sched
     */
    public String getSched() {
        return sched;
    }

    /**
     * @param sched the sched to set
     */
    public void setSched(String sched) {
        this.sched = sched;
    }

    /**
     * @return the refAut
     */
    public String getRefAut() {
        return refAut;
    }

    /**
     * @param refAut the refAut to set
     */
    public void setRefAut(String refAut) {
        this.refAut = refAut;
    }

    /**
     * @return the refRem
     */
    public String getRefRem() {
        return refRem;
    }

    /**
     * @param refRem the refRem to set
     */
    public void setRefRem(String refRem) {
        this.refRem = refRem;
    }
    
    /**
     * @return the prefill
     */
    public String getPrefill() {
        return prefill;
    }

    /**
     * @param prefill the prefill to set
     */
    public void setPrefill(String prefill) {
        this.prefill = prefill;
    }

    /**
     * @return the first
     */
    public String getFirst() {
        return first;
    }

    /**
     * @param first the first to set
     */
    public void setFirst(String first) {
        this.first = first;
    }

    /**
     * @return the expire
     */
    public String getExpire() {
        return expire;
    }

    /**
     * @param expire the expire to set
     */
    public void setExpire(String expire) {
        this.expire = expire;
    }

    /**
     * @return the sigText
     */
    public String getSigText() {
        return sigText;
    }

    /**
     * @param sigText the sigText to set
     */
    public void setSigText(String sigText) {
        this.sigText = sigText;
    }

    public String getPrescribingDocName() {
        return prescribingDocName;
    }

    public void setPrescribingDocName(String prescribingDocName) {
        this.prescribingDocName = prescribingDocName;
    }

    /**
     * @return the prescDrugName
     */
    public String getPrescDrugName() {
        return prescDrugName;
    }

    /**
     * @param prescDrugName the prescDrugName to set
     */
    public void setPrescDrugName(String prescDrugName) {
        this.prescDrugName = prescDrugName;
    }

    public String getPrescDrugGPI() {
        return prescDrugGPI;
    }

    public void setPrescDrugGPI(String prescDrugGPI) {
        this.prescDrugGPI = prescDrugGPI;
    }

    public String getPrescDrugNDC() {
        return prescDrugNDC;
    }

    public void setPrescDrugNDC(String prescDrugNDC) {
        this.prescDrugNDC = prescDrugNDC;
    }

    /**
     * @return the filled
     */
    public String getFilled() {
        return filled;
    }

    /**
     * @param filled the filled to set
     */
    public void setFilled(String filled) {
        this.filled = filled;
    }

    /**
     * @return the quantity
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the days
     */
    public String getDays() {
        return days;
    }

    /**
     * @param days the days to set
     */
    public void setDays(String days) {
        this.days = days;
    }

    /**
     * @return the anticipatedRefillDate
     */
    public String getAnticipatedRefillDate() {
        return anticipatedRefillDate;
    }

    /**
     * @param anticipatedRefillDate the anticipatedRefillDate to set
     */
    public void setAnticipatedRefillDate(String anticipatedRefillDate) {
        this.anticipatedRefillDate = anticipatedRefillDate;
    }

    /**
     * @return prescription transaction list from the update notification
     */
    public String getTxList() {
        return txList;
    }

    /**
     * @param txList as ~~ seperated format for each prescription
     */
    public void setTxList(String txList) {
        this.txList = txList;
    }

    public CustomerTransactionFile[] getCustomerTxFile() {
        return customerTxFile;
    }

    public void setCustomerTxFile(CustomerTransactionFile[] customerTxFile) {
        this.customerTxFile = customerTxFile;
    }

    /**
     * @return the dispDrugName
     */
    public String getDispDrugName() {
        return dispDrugName;
    }

    /**
     * @param dispDrugName the dispDrugName to set
     */
    public void setDispDrugName(String dispDrugName) {
        this.dispDrugName = dispDrugName;
    }

    /**
     * @return the dispDrugNDC
     */
    public String getDispDrugNDC() {
        return dispDrugNDC;
    }

    /**
     * @param dispDrugNDC the dispDrugNDC to set
     */
    public void setDispDrugNDC(String dispDrugNDC) {
        this.dispDrugNDC = dispDrugNDC;
    }

    /**
     * @return the dispDrugGPI
     */
    public String getDispDrugGPI() {
        return dispDrugGPI;
    }

    /**
     * @param dispDrugGPI the dispDrugGPI to set
     */
    public void setDispDrugGPI(String dispDrugGPI) {
        this.dispDrugGPI = dispDrugGPI;
    }
    
    public String getCopay() {
		return copay;
	}

	public void setCopay(String copay) {
		this.copay = copay;
	}
}
