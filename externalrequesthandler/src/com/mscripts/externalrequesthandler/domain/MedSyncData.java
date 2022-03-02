package com.mscripts.externalrequesthandler.domain;

public class MedSyncData {

	public String customer_firstname;
	public String store_name;
	public String client_name;
	public String store_address_line1;
	public String next_med_sync_date;
	public String support_email_address;
	public String support_phone_number;
	public String getCustomer_firstname() {
		return customer_firstname;
	}
	public void setCustomer_firstname(String customer_firstname) {
		this.customer_firstname = customer_firstname;
	}
	public String getStore_name() {
		return store_name;
	}
	public void setStore_name(String store_name) {
		this.store_name = store_name;
	}
	public String getClient_name() {
		return client_name;
	}
	public void setClient_name(String client_name) {
		this.client_name = client_name;
	}
	public String getStore_address_line1() {
		return store_address_line1;
	}
	public void setStore_address_line1(String store_address_line1) {
		this.store_address_line1 = store_address_line1;
	}
	public String getNext_med_sync_date() {
		return next_med_sync_date;
	}
	public void setNext_med_sync_date(String next_med_sync_date) {
		this.next_med_sync_date = next_med_sync_date;
	}
	public String getSupport_email_address() {
		return support_email_address;
	}
	public void setSupport_email_address(String support_email_address) {
		this.support_email_address = support_email_address;
	}
	public String getSupport_phone_number() {
		return support_phone_number;
	}
	public void setSupport_phone_number(String support_phone_number) {
		this.support_phone_number = support_phone_number;
	}
	@Override
	public String toString() {
		return "MedSyncData [customer_firstname=" + customer_firstname + ", store_name=" + store_name + ", client_name="
				+ client_name + ", store_address_line1=" + store_address_line1 + ", next_med_sync_date="
				+ next_med_sync_date + ", support_email_address=" + support_email_address + ", support_phone_number="
				+ support_phone_number + "]";
	}
}
