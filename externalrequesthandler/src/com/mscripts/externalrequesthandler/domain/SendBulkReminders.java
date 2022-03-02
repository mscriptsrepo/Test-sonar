package com.mscripts.externalrequesthandler.domain;

public class SendBulkReminders {
	private String id;
	private String mobile_number;
	private String message_text;
	private String customer_id;
	private String errorSmsReturnType;
	private String shortcode;
	private String client_id;
	private String rx_number;
	private String communication_id;
	private String message_sent;

	private String send_date;
	private String created_at;
	private String created_by;
	private String updated_at;

	private String updated_by;
	private String email_text;
	private String push_text;
	private String customer_timezone;
	private String customer_reminder_send_hour;
	private String mscripts_auto_fill_id;
	private String rx_refill_request_retry_id;

	public String getCustomer_timezone() {
		return customer_timezone;
	}

	public void setCustomer_timezone(String customer_timezone) {
		this.customer_timezone = customer_timezone;
	}

	public String getCustomer_reminder_send_hour() {
		return customer_reminder_send_hour;
	}

	public void setCustomer_reminder_send_hour(String customer_reminder_send_hour) {
		this.customer_reminder_send_hour = customer_reminder_send_hour;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMobile_number() {
		return mobile_number;
	}

	public void setMobile_number(String mobile_number) {
		this.mobile_number = mobile_number;
	}

	public String getMessage_text() {
		return message_text;
	}

	public void setMessage_text(String message_text) {
		this.message_text = message_text;
	}

	public String getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}

	public String getErrorSmsReturnType() {
		return errorSmsReturnType;
	}

	public void setErrorSmsReturnType(String errorSmsReturnType) {
		this.errorSmsReturnType = errorSmsReturnType;
	}

	public String getShortcode() {
		return shortcode;
	}

	public void setShortcode(String shortcode) {
		this.shortcode = shortcode;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getRx_number() {
		return rx_number;
	}

	public void setRx_number(String rx_number) {
		this.rx_number = rx_number;
	}

	public String getCommunication_id() {
		return communication_id;
	}

	public void setCommunication_id(String communication_id) {
		this.communication_id = communication_id;
	}

	public String getMessage_sent() {
		return message_sent;
	}

	public void setMessage_sent(String message_sent) {
		this.message_sent = message_sent;
	}

	public String getSend_date() {
		return send_date;
	}

	public void setSend_date(String send_date) {
		this.send_date = send_date;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getUpdated_by() {
		return updated_by;
	}

	public void setUpdated_by(String updated_by) {
		this.updated_by = updated_by;
	}

	public String getEmail_text() {
		return email_text;
	}

	public void setEmail_text(String email_text) {
		this.email_text = email_text;
	}

	public String getPush_text() {
		return push_text;
	}

	public void setPush_text(String push_text) {
		this.push_text = push_text;
	}

	public String getMscripts_auto_fill_id() {
		return mscripts_auto_fill_id;
	}

	public void setMscripts_auto_fill_id(String mscripts_auto_fill_id) {
		this.mscripts_auto_fill_id = mscripts_auto_fill_id;
	}

	public String getRx_refill_request_retry_id() {
		return rx_refill_request_retry_id;
	}

	public void setRx_refill_request_retry_id(String rx_refill_request_retry_id) {
		this.rx_refill_request_retry_id = rx_refill_request_retry_id;
	}

}
