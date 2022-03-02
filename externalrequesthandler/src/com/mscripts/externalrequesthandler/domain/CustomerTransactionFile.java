/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.domain;

import java.util.List;

/**
 *
 * @author sbhat
 */
public class CustomerTransactionFile {

	private String txnum;
    private String filleddate;
    private String solddate;
    private String phcode;
    private String sgcode;
    private String drcode;
    private String ndc;
    private String mfg;
    private String status;
    private String tpbill;
    private String hold;
    private String postype;
    private String prcode;
    private String taxcode;
    private String initials;
    private String order;
    private String rphcoun;
    private String techinit;
    private String daw;
    private String intover;
    private String allover;
    private String pdover;
    private String dcover;
    private String dtover;
    private String durover;
    private String mesg;
    private String quantity;
    private String refnum;
    private String days;
    private String cost;
    private String accost;
    private String discount;
    private String tax;
    private String price;
    private String ucprice;
    private String compfee;
    private String upcharge;
    private String drexp;
    private String host;
    private String usual;
    private String progadd;
    private String schdrug;
    private String genmesg;
    private String nscchoice;
    private String counchoice;
    private String pacmed;
    private String viaprefill;
    private String othprice;
    private String AcsPriority;
    private String decqty;
    private String DeleteTx;
    private String dispDrugName;
    private String dispDrugNDC;
    private String dispDrugGPI;
    private String willCallReady;
    private String txstatus;
    // Adding copay amount for payments
    private String copay;
    private List<CustomerTransactionTxtpfile> customerTransactionTxtpfile;

	public String getCopay() {
		return copay;
	}

	public void setCopay(String copay) {
		this.copay = copay;
	}

	public String getTxnum() {
        return txnum;
    }

    public void setTxnum(String txnum) {
        this.txnum = txnum;
    }

    public String getFilleddate() {
        return filleddate;
    }

    public void setFilleddate(String filleddate) {
        this.filleddate = filleddate;
    }

    public String getPhcode() {
        return phcode;
    }

    public void setPhcode(String phcode) {
        this.phcode = phcode;
    }

    public String getSgcode() {
        return sgcode;
    }

    public void setSgcode(String sgcode) {
        this.sgcode = sgcode;
    }

    public String getDrcode() {
        return drcode;
    }

    public void setDrcode(String drcode) {
        this.drcode = drcode;
    }

    public String getNdc() {
        return ndc;
    }

    public void setNdc(String ndc) {
        this.ndc = ndc;
    }

    public String getMfg() {
        return mfg;
    }

    public void setMfg(String mfg) {
        this.mfg = mfg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTpbill() {
        return tpbill;
    }

    public void setTpbill(String tpbill) {
        this.tpbill = tpbill;
    }

    public String getHold() {
        return hold;
    }

    public void setHold(String hold) {
        this.hold = hold;
    }

    public String getPostype() {
        return postype;
    }

    public void setPostype(String postype) {
        this.postype = postype;
    }

    public String getPrcode() {
        return prcode;
    }

    public void setPrcode(String prcode) {
        this.prcode = prcode;
    }

    public String getTaxcode() {
        return taxcode;
    }

    public void setTaxcode(String taxcode) {
        this.taxcode = taxcode;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getRphcoun() {
        return rphcoun;
    }

    public void setRphcoun(String rphcoun) {
        this.rphcoun = rphcoun;
    }

    public String getTechinit() {
        return techinit;
    }

    public void setTechinit(String techinit) {
        this.techinit = techinit;
    }

    public String getDaw() {
        return daw;
    }

    public void setDaw(String daw) {
        this.daw = daw;
    }

    public String getIntover() {
        return intover;
    }

    public void setIntover(String intover) {
        this.intover = intover;
    }

    public String getAllover() {
        return allover;
    }

    public void setAllover(String allover) {
        this.allover = allover;
    }

    public String getPdover() {
        return pdover;
    }

    public void setPdover(String pdover) {
        this.pdover = pdover;
    }

    public String getDcover() {
        return dcover;
    }

    public void setDcover(String dcover) {
        this.dcover = dcover;
    }

    public String getDtover() {
        return dtover;
    }

    public void setDtover(String dtover) {
        this.dtover = dtover;
    }

    public String getDurover() {
        return durover;
    }

    public void setDurover(String durover) {
        this.durover = durover;
    }

    public String getMesg() {
        return mesg;
    }

    public void setMesg(String mesg) {
        this.mesg = mesg;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRefnum() {
        return refnum;
    }

    public void setRefnum(String refnum) {
        this.refnum = refnum;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getAccost() {
        return accost;
    }

    public void setAccost(String accost) {
        this.accost = accost;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUcprice() {
        return ucprice;
    }

    public void setUcprice(String ucprice) {
        this.ucprice = ucprice;
    }

    public String getCompfee() {
        return compfee;
    }

    public void setCompfee(String compfee) {
        this.compfee = compfee;
    }

    public String getUpcharge() {
        return upcharge;
    }

    public void setUpcharge(String upcharge) {
        this.upcharge = upcharge;
    }

    public String getDrexp() {
        return drexp;
    }

    public void setDrexp(String drexp) {
        this.drexp = drexp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSolddate() {
        return solddate;
    }

    public void setSolddate(String solddate) {
        this.solddate = solddate;
    }

    public String getUsual() {
        return usual;
    }

    public void setUsual(String usual) {
        this.usual = usual;
    }

    public String getProgadd() {
        return progadd;
    }

    public void setProgadd(String progadd) {
        this.progadd = progadd;
    }

    public String getSchdrug() {
        return schdrug;
    }

    public void setSchdrug(String schdrug) {
        this.schdrug = schdrug;
    }

    public String getGenmesg() {
        return genmesg;
    }

    public void setGenmesg(String genmesg) {
        this.genmesg = genmesg;
    }

    public String getNscchoice() {
        return nscchoice;
    }

    public void setNscchoice(String nscchoice) {
        this.nscchoice = nscchoice;
    }

    public String getCounchoice() {
        return counchoice;
    }

    public void setCounchoice(String counchoice) {
        this.counchoice = counchoice;
    }

    public String getPacmed() {
        return pacmed;
    }

    public void setPacmed(String pacmed) {
        this.pacmed = pacmed;
    }

    public String getViaprefill() {
        return viaprefill;
    }

    public void setViaprefill(String viaprefill) {
        this.viaprefill = viaprefill;
    }

    public String getOthprice() {
        return othprice;
    }

    public void setOthprice(String othprice) {
        this.othprice = othprice;
    }

    public String getAcsPriority() {
        return AcsPriority;
    }

    public void setAcsPriority(String acsPriority) {
        AcsPriority = acsPriority;
    }

    public String getDecqty() {
        return decqty;
    }

    public void setDecqty(String decqty) {
        this.decqty = decqty;
    }

    public String getDeleteTx() {
        return DeleteTx;
    }

    public void setDeleteTx(String deleteTx) {
        DeleteTx = deleteTx;
    }

    public String getDispDrugName() {
        return dispDrugName;
    }

    public void setDispDrugName(String dispDrugName) {
        this.dispDrugName = dispDrugName;
    }

    public String getDispDrugNDC() {
        return dispDrugNDC;
    }

    public void setDispDrugNDC(String dispDrugNDC) {
        this.dispDrugNDC = dispDrugNDC;
    }

    public String getDispDrugGPI() {
        return dispDrugGPI;
    }

    public void setDispDrugGPI(String dispDrugGPI) {
        this.dispDrugGPI = dispDrugGPI;
    }

    public String getWillCallReady() {
        return willCallReady;
    }

    public void setWillCallReady(String willCallReady) {
        this.willCallReady = willCallReady;
    }

    public String getTxstatus() {
        return txstatus;
    }

    public void setTxstatus(String txstatus) {
        this.txstatus = txstatus;
    }
    
	public List<CustomerTransactionTxtpfile> getCustomerTransactionTxtpfile() {
		return customerTransactionTxtpfile;
	}

	public void setCustomerTransactionTxtpfile(List<CustomerTransactionTxtpfile> customerTransactionTxtpfile) {
		this.customerTransactionTxtpfile = customerTransactionTxtpfile;
	}

}
