package com.mscripts.externalrequesthandler.domain;

public class TransactionData {

	private String prescriptionNumber;
	private String refillNum;
	private String txNumber;
	private String filledDate;
	private String drugSchedule;
	private String drugName;
	private String productIDType;
	private String productID;
	private String drugGPI;
	private String drugMultiSource;
	private String daysSupply;
	private String quantityDispensed;
	private String drugUnit;
	private String writtenDate;
	private String lastFilledDate;
	private String refillsAuthorized;
	private String fulfillmentStatus;
	private String autoFillEnabled;
	private String dawCode;
	private String originalFillDate;
	private String refillsRemaining;
	private String expirationDate;
	private String rxOrigin;
	private String shipDate;
	private String shipperName;
	private String trackingNumber;
	private String deliveryDate;
	private String shipType;
	private String refillSource;
	private String outOfStockDate;
	private String partialFillCompletionDate;
	private String partialFillStatus;
	private String willCallReadyDate;
	private String willCallPickedUpDate;
	private String fillLocation;
	private String contactReason;
	private String promiseTime;
	private String syncScriptEnrolled;
	private String nextSyncFillDate;
	//auto fill allowable fields
	private String specialtyDrug;
	private String drugDisallowAutofill;
	private String freeFormText;
	
	public String getSyncScriptEnrolled() {
		return syncScriptEnrolled;
	}

	public void setSyncScriptEnrolled(String syncScriptEnrolled) {
		this.syncScriptEnrolled = syncScriptEnrolled;
	}

	public String getNextSyncFillDate() {
		return nextSyncFillDate;
	}

	public void setNextSyncFillDate(String nextSyncFillDate) {
		this.nextSyncFillDate = nextSyncFillDate;
	}

	public String getPrescriptionNumber() {
		return prescriptionNumber;
	}

	public void setPrescriptionNumber(String prescriptionNumber) {
		this.prescriptionNumber = prescriptionNumber;
	}

	public String getRefillNum() {
		return refillNum;
	}

	public void setRefillNum(String refillNum) {
		this.refillNum = refillNum;
	}

	public String getTxNumber() {
		return txNumber;
	}

	public void setTxNumber(String txNumber) {
		this.txNumber = txNumber;
	}

	public String getFilledDate() {
		return filledDate;
	}

	public void setFilledDate(String filledDate) {
		this.filledDate = filledDate;
	}

	public String getDrugSchedule() {
		return drugSchedule;
	}

	public void setDrugSchedule(String drugSchedule) {
		this.drugSchedule = drugSchedule;
	}

	public String getDrugName() {
		return drugName;
	}

	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}

	public String getProductIDType() {
		return productIDType;
	}

	public void setProductIDType(String productIDType) {
		this.productIDType = productIDType;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String getDrugGPI() {
		return drugGPI;
	}

	public void setDrugGPI(String drugGPI) {
		this.drugGPI = drugGPI;
	}

	public String getDrugMultiSource() {
		return drugMultiSource;
	}

	public void setDrugMultiSource(String drugMultiSource) {
		this.drugMultiSource = drugMultiSource;
	}

	public String getDaysSupply() {
		return daysSupply;
	}

	public void setDaysSupply(String daysSupply) {
		this.daysSupply = daysSupply;
	}

	public String getQuantityDispensed() {
		return quantityDispensed;
	}

	public void setQuantityDispensed(String quantityDispensed) {
		this.quantityDispensed = quantityDispensed;
	}

	public String getDrugUnit() {
		return drugUnit;
	}

	public void setDrugUnit(String drugUnit) {
		this.drugUnit = drugUnit;
	}

	public String getWrittenDate() {
		return writtenDate;
	}

	public void setWrittenDate(String writtenDate) {
		this.writtenDate = writtenDate;
	}

	public String getLastFilledDate() {
		return lastFilledDate;
	}

	public void setLastFilledDate(String lastFilledDate) {
		this.lastFilledDate = lastFilledDate;
	}

	public String getRefillsAuthorized() {
		return refillsAuthorized;
	}

	public void setRefillsAuthorized(String refillsAuthorized) {
		this.refillsAuthorized = refillsAuthorized;
	}

	public String getFulfillmentStatus() {
		return fulfillmentStatus;
	}

	public void setFulfillmentStatus(String fulfillmentStatus) {
		this.fulfillmentStatus = fulfillmentStatus;
	}

	public String getAutoFillEnabled() {
		return autoFillEnabled;
	}

	public void setAutoFillEnabled(String autoFillEnabled) {
		this.autoFillEnabled = autoFillEnabled;
	}

	public String getDawCode() {
		return dawCode;
	}

	public void setDawCode(String dawCode) {
		this.dawCode = dawCode;
	}

	public String getOriginalFillDate() {
		return originalFillDate;
	}

	public void setOriginalFillDate(String originalFillDate) {
		this.originalFillDate = originalFillDate;
	}

	public String getRefillsRemaining() {
		return refillsRemaining;
	}

	public void setRefillsRemaining(String refillsRemaining) {
		this.refillsRemaining = refillsRemaining;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getRxOrigin() {
		return rxOrigin;
	}

	public void setRxOrigin(String rxOrigin) {
		this.rxOrigin = rxOrigin;
	}

	public String getShipDate() {
		return shipDate;
	}

	public void setShipDate(String shipDate) {
		this.shipDate = shipDate;
	}

	public String getShipperName() {
		return shipperName;
	}

	public void setShipperName(String shipperName) {
		this.shipperName = shipperName;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public String getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public String getShipType() {
		return shipType;
	}

	public void setShipType(String shipType) {
		this.shipType = shipType;
	}

	public String getRefillSource() {
		return refillSource;
	}

	public void setRefillSource(String refillSource) {
		this.refillSource = refillSource;
	}

	public String getOutOfStockDate() {
		return outOfStockDate;
	}

	public void setOutOfStockDate(String outOfStockDate) {
		this.outOfStockDate = outOfStockDate;
	}

	public String getPartialFillCompletionDate() {
		return partialFillCompletionDate;
	}

	public void setPartialFillCompletionDate(String partialFillCompletionDate) {
		this.partialFillCompletionDate = partialFillCompletionDate;
	}

	public String getPartialFillStatus() {
		return partialFillStatus;
	}

	public void setPartialFillStatus(String partialFillStatus) {
		this.partialFillStatus = partialFillStatus;
	}

	public String getWillCallReadyDate() {
		return willCallReadyDate;
	}

	public void setWillCallReadyDate(String willCallReadyDate) {
		this.willCallReadyDate = willCallReadyDate;
	}

	public String getWillCallPickedUpDate() {
		return willCallPickedUpDate;
	}

	public void setWillCallPickedUpDate(String willCallPickedUpDate) {
		this.willCallPickedUpDate = willCallPickedUpDate;
	}

	public String getFillLocation() {
		return fillLocation;
	}

	public void setFillLocation(String fillLocation) {
		this.fillLocation = fillLocation;
	}

	public String getContactReason() {
		return contactReason;
	}

	public void setContactReason(String contactReason) {
		this.contactReason = contactReason;
	}

	public String getPromiseTime() {
		return promiseTime;
	}

	public void setPromiseTime(String promiseTime) {
		this.promiseTime = promiseTime;
	}

	public String getSpecialtyDrug() {
		return specialtyDrug;
	}

	public void setSpecialtyDrug(String specialtyDrug) {
		this.specialtyDrug = specialtyDrug;
	}

	public String getDrugDisallowAutofill() {
		return drugDisallowAutofill;
	}

	public void setDrugDisallowAutofill(String drugDisallowAutofill) {
		this.drugDisallowAutofill = drugDisallowAutofill;
	}

	public String getFreeFormText() {
		return freeFormText;
	}

	public void setFreeFormText(String freeFormText) {
		this.freeFormText = freeFormText;
	}
}
