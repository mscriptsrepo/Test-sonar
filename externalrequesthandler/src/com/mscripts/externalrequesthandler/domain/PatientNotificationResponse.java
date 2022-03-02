/**
 * Property of mscripts, LLC
 */
package com.mscripts.externalrequesthandler.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mshri
 */
@XmlRootElement(name = "PatientNotificationResponse")
public class PatientNotificationResponse {
	private String status;
	private String httpStatusCode;
	private String error;
	private String errorCode;
	private String errorMessage;
	private String errorId;

	public PatientNotificationResponse() {
	}

	@XmlElement
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlElement
	public String getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(String httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	@XmlElement
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@XmlElement
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@XmlElement
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@XmlElement
	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	@Override
	public String toString() {
		return "PatientNotificationResponse [status: " + status + "; httpStatusCode: " + httpStatusCode + "; error: "
				+ error + "; errorCode: " + errorCode + "; errorMessage: " + errorMessage + "; errorId: " + errorId
				+ "]";
	}
    
}
