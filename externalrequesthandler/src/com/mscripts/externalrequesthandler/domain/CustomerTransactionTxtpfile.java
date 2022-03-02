package com.mscripts.externalrequesthandler.domain;

 public class CustomerTransactionTxtpfile implements Comparable<CustomerTransactionTxtpfile>  {
 	
 	private String counter;
     private String copay;
 	
    private String id;
    private String plan;
    private String card;
    private String group;
    private String split;
    private String copover;
    private String origtype;
    private String reverse;
    private String balance;
    private String collect;
    private String incent;
    private String txtpPrice;
    private String txtpCost;
    private String txtpTax;
    private String txtpCompfee;
    private String upcharge;
    private String othamt;
    private String paid;
    private String planName;
    private String planBin;
    private String planPCN;
    
    public String getCopay() {
 		return copay;
 	}
 	
    public void setCopay(String copay) {
 		this.copay = copay;
 	}
 
 	public String getCounter() {
 		return counter;
 	}
 
 	public void setCounter(String counter) {
 		this.counter = counter;
 	}
 	
 	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getCopover() {
		return copover;
	}

	public void setCopover(String copover) {
		this.copover = copover;
	}

	public String getOrigtype() {
		return origtype;
	}

	public void setOrigtype(String origtype) {
		this.origtype = origtype;
	}

	public String getReverse() {
		return reverse;
	}

	public void setReverse(String reverse) {
		this.reverse = reverse;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getCollect() {
		return collect;
	}

	public void setCollect(String collect) {
		this.collect = collect;
	}

	public String getIncent() {
		return incent;
	}

	public void setIncent(String incent) {
		this.incent = incent;
	}

	public String getTxtpPrice() {
		return txtpPrice;
	}

	public void setTxtpPrice(String txtpPrice) {
		this.txtpPrice = txtpPrice;
	}

	public String getTxtpCost() {
		return txtpCost;
	}

	public void setTxtpCost(String txtpCost) {
		this.txtpCost = txtpCost;
	}

	public String getTxtpTax() {
		return txtpTax;
	}

	public void setTxtpTax(String txtpTax) {
		this.txtpTax = txtpTax;
	}

	public String getTxtpCompfee() {
		return txtpCompfee;
	}

	public void setTxtpCompfee(String txtpCompfee) {
		this.txtpCompfee = txtpCompfee;
	}

	public String getTxtpUpcharge() {
		return upcharge;
	}

	public void setUpcharge(String upcharge) {
		this.upcharge = upcharge;
	}

	public String getOthamt() {
		return othamt;
	}

	public void setOthamt(String othamt) {
		this.othamt = othamt;
	}

	public String getPaid() {
		return paid;
	}

	public void setPaid(String paid) {
		this.paid = paid;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getPlanBin() {
		return planBin;
	}

	public void setPlanBin(String planBin) {
		this.planBin = planBin;
	}

	public String getPlanPCN() {
		return planPCN;
	}

	public void setPlanPCN(String planPCN) {
		this.planPCN = planPCN;
	}

	@Override
 	public int compareTo(CustomerTransactionTxtpfile a){
 		return this.counter.compareTo(a.counter);
 	}

	@Override
	public String toString() {
		return "CustomerTransactionTxtpfile [counter=" + counter + ", copay=" + copay + ", id=" + id + ", plan=" + plan
				+ ", card=" + card + ", group=" + group + ", split=" + split + ", copover=" + copover + ", origtype="
				+ origtype + ", reverse=" + reverse + ", balance=" + balance + ", collect=" + collect + ", incent="
				+ incent + ", txtpPrice=" + txtpPrice + ", txtpCost=" + txtpCost + ", txtpTax=" + txtpTax
				+ ", txtpCompfee=" + txtpCompfee + ", upcharge=" + upcharge + ", othamt=" + othamt + ", paid=" + paid
				+ ", planName=" + planName + ", planBin=" + planBin + ", planPCN=" + planPCN + "]";
	} 
	
 }