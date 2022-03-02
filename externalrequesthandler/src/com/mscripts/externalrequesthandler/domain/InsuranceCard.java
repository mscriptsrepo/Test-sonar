/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.domain;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mscripts.utils.XMLUtils;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author sbhat
 */
public class InsuranceCard {

    String carrierid;
    String cardnumber;
    String group;
    String firstnm;
    String midnm;
    String lastnm;
    String altcard;
    String pdate;
    String bdate;
    String edate;
    String benefit;
    String cardql;
    String plan;
    String cover;
    String eligible;
    String workcompflag;
    String DeleteCard;
    private static final Logger LOGGER = Logger.getLogger(InsuranceCardLink.class);

    public String getCarrierid() {
        return carrierid;
    }

    public void setCarrierid(String carrierid) {
        this.carrierid = carrierid;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFirstnm() {
        return firstnm;
    }

    public void setFirstnm(String firstnm) {
        this.firstnm = firstnm;
    }

    public String getMidnm() {
        return midnm;
    }

    public void setMidnm(String midnm) {
        this.midnm = midnm;
    }

    public String getLastnm() {
        return lastnm;
    }

    public void setLastnm(String lastnm) {
        this.lastnm = lastnm;
    }

    public String getAltcard() {
        return altcard;
    }

    public void setAltcard(String altcard) {
        this.altcard = altcard;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public String getBdate() {
        return bdate;
    }

    public void setBdate(String bdate) {
        this.bdate = bdate;
    }

    public String getEdate() {
        return edate;
    }

    public void setEdate(String edate) {
        this.edate = edate;
    }

    public String getBenefit() {
        return benefit;
    }

    public void setBenefit(String benefit) {
        this.benefit = benefit;
    }

    public String getCardql() {
        return cardql;
    }

    public void setCardql(String cardql) {
        this.cardql = cardql;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getEligible() {
        return eligible;
    }

    public void setEligible(String eligible) {
        this.eligible = eligible;
    }

    public String getWorkcompflag() {
        return workcompflag;
    }

    public void setWorkcompflag(String workcompflag) {
        this.workcompflag = workcompflag;
    }

    public String getDeleteCard() {
        return DeleteCard;
    }

    public void setDeleteCard(String DeleteCard) {
        this.DeleteCard = DeleteCard;
    }

    public static InsuranceCard[] parseCustomerCard(NodeList insCardNodeList) throws mscriptsException {

        String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-parseCustomerCard";
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Entered into parsing customer insurance card method");
        }

        Document cardDoc = null;
        InsuranceCard cardFile[] = new InsuranceCard[insCardNodeList.getLength()];
        try {
            for (int i = 0; i < insCardNodeList.getLength(); i++) {

                cardDoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(insCardNodeList.item(i)));
                InsuranceCard insuranceCard = new InsuranceCard();

                insuranceCard.setCarrierid(XMLUtils.getNodeValue(cardDoc, "//card/id"));
                insuranceCard.setCardnumber(XMLUtils.getNodeValue(cardDoc, "//card/cardnumber"));
                insuranceCard.setGroup(XMLUtils.getNodeValue(cardDoc, "//card/group"));
                insuranceCard.setFirstnm(XMLUtils.getNodeValue(cardDoc, "//card/firstnm"));
                insuranceCard.setMidnm(XMLUtils.getNodeValue(cardDoc, "//card/midnm"));
                insuranceCard.setLastnm(XMLUtils.getNodeValue(cardDoc, "//card/lastnm"));
                insuranceCard.setAltcard(XMLUtils.getNodeValue(cardDoc, "//card/altcard"));
                insuranceCard.setPdate(XMLUtils.getNodeValue(cardDoc, "//card/pdate"));
                insuranceCard.setBdate(XMLUtils.getNodeValue(cardDoc, "//card/bdate"));
                insuranceCard.setEdate(XMLUtils.getNodeValue(cardDoc, "//card/edate"));
                insuranceCard.setBenefit(XMLUtils.getNodeValue(cardDoc, "//card/benefit"));
                insuranceCard.setCardql(XMLUtils.getNodeValue(cardDoc, "//card/cardql"));
                insuranceCard.setPlan(XMLUtils.getNodeValue(cardDoc, "//card/plan"));
                insuranceCard.setCover(XMLUtils.getNodeValue(cardDoc, "//card/cover"));
                insuranceCard.setEligible(XMLUtils.getNodeValue(cardDoc, "//card/eligible"));
                insuranceCard.setWorkcompflag(XMLUtils.getNodeValue(cardDoc, "//card/workcompflag"));
                insuranceCard.setDeleteCard(XMLUtils.getNodeValue(cardDoc, "//card/DeleteCard"));

                cardFile[i] = insuranceCard;
            }

        } catch (mscriptsException mex) {
            LOGGER.error(" Exception occured while parsing customer prescription:", mex);
            throw mex;
        } catch (Exception ex) {
            LOGGER.error(" Exception occured while parsing customer prescription:", ex);
            throw new mscriptsException(null, errorSource, mscriptsExceptionSeverity.Medium, ex);
        }

        return cardFile;
    }
}
