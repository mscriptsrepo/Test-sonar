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
public class InsuranceCardLink {

    String adc;
    String altcard;
    String bchome;
    String cardnumber;
    String carrierid;
    String child;
    String clinic;
    String bdate;
    String edate;
    String pdate;
    String eligible;
    String eligovr;
    String employ;
    String group;
    String level;
    String location;
    String medicaidID;
    String medicaidInd;
    String nh;
    String other;
    String patBenNoAssign;
    String plan;
    String qualCMSFacility;
    String relat;
    String residence;
    String sc;
    String series;
    String student;
    String special;
    String DeleteTPLink;
    private static final Logger LOGGER = Logger.getLogger(InsuranceCardLink.class);

    public String getAdc() {
        return adc;
    }

    public void setAdc(String adc) {
        this.adc = adc;
    }

    public String getAltcard() {
        return altcard;
    }

    public void setAltcard(String altcard) {
        this.altcard = altcard;
    }

    public String getBchome() {
        return bchome;
    }

    public void setBchome(String bchome) {
        this.bchome = bchome;
    }

    public String getBdate() {
        return bdate;
    }

    public void setBdate(String bdate) {
        this.bdate = bdate;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCarrierid() {
        return carrierid;
    }

    public void setCarrierid(String carrierid) {
        this.carrierid = carrierid;
    }

    public String getChild() {
        return child;
    }

    public void setChild(String child) {
        this.child = child;
    }

    public String getClinic() {
        return clinic;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public String getDeleteTPLink() {
        return DeleteTPLink;
    }

    public void setDeleteTPLink(String DeleteTPLink) {
        this.DeleteTPLink = DeleteTPLink;
    }

    public String getEdate() {
        return edate;
    }

    public void setEdate(String edate) {
        this.edate = edate;
    }

    public String getEligible() {
        return eligible;
    }

    public void setEligible(String eligible) {
        this.eligible = eligible;
    }

    public String getEligovr() {
        return eligovr;
    }

    public void setEligovr(String eligovr) {
        this.eligovr = eligovr;
    }

    public String getEmploy() {
        return employ;
    }

    public void setEmploy(String employ) {
        this.employ = employ;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMedicaidID() {
        return medicaidID;
    }

    public void setMedicaidID(String medicaidID) {
        this.medicaidID = medicaidID;
    }

    public String getMedicaidInd() {
        return medicaidInd;
    }

    public void setMedicaidInd(String medicaidInd) {
        this.medicaidInd = medicaidInd;
    }

    public String getNh() {
        return nh;
    }

    public void setNh(String nh) {
        this.nh = nh;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getPatBenNoAssign() {
        return patBenNoAssign;
    }

    public void setPatBenNoAssign(String patBenNoAssign) {
        this.patBenNoAssign = patBenNoAssign;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getQualCMSFacility() {
        return qualCMSFacility;
    }

    public void setQualCMSFacility(String qualCMSFacility) {
        this.qualCMSFacility = qualCMSFacility;
    }

    public String getRelat() {
        return relat;
    }

    public void setRelat(String relat) {
        this.relat = relat;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getSc() {
        return sc;
    }

    public void setSc(String sc) {
        this.sc = sc;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public static InsuranceCardLink[] parseCustomerCardLink(NodeList insCardLinkList) throws mscriptsException {
        String errorSource = "com.mscripts.externalrequesthandler.service.NotificationServiceImpl-parseCustomerCard";
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Entered into parsing customer insurance card method");
        }

        Document cardDoc = null;
        InsuranceCardLink cardLinkFile[] = new InsuranceCardLink[insCardLinkList.getLength()];
        try {
            for (int i = 0; i < insCardLinkList.getLength(); i++) {

                cardDoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(insCardLinkList.item(i)));
                InsuranceCardLink insuranceCardLink = new InsuranceCardLink();

                insuranceCardLink.setCarrierid(XMLUtils.getNodeValue(cardDoc, "//tplink/id"));
                insuranceCardLink.setCardnumber(XMLUtils.getNodeValue(cardDoc, "//tplink/card"));
                insuranceCardLink.setGroup(XMLUtils.getNodeValue(cardDoc, "//tplink/group"));
                insuranceCardLink.setLevel(XMLUtils.getNodeValue(cardDoc, "//tplink/level"));
                insuranceCardLink.setRelat(XMLUtils.getNodeValue(cardDoc, "//tplink/relat"));
                insuranceCardLink.setLocation(XMLUtils.getNodeValue(cardDoc, "//tplink/location"));
                insuranceCardLink.setAdc(XMLUtils.getNodeValue(cardDoc, "//tplink/adc"));
                insuranceCardLink.setAltcard(XMLUtils.getNodeValue(cardDoc, "//tplink/altcard"));
                insuranceCardLink.setBchome(XMLUtils.getNodeValue(cardDoc, "//tplink/bchome"));
                insuranceCardLink.setBdate(XMLUtils.getNodeValue(cardDoc, "//tplink/bdate"));
                insuranceCardLink.setEdate(XMLUtils.getNodeValue(cardDoc, "//tplink/edate"));
                insuranceCardLink.setPdate(XMLUtils.getNodeValue(cardDoc, "//tplink/pdate"));
                insuranceCardLink.setChild(XMLUtils.getNodeValue(cardDoc, "//tplink/child"));
                insuranceCardLink.setClinic(XMLUtils.getNodeValue(cardDoc, "//tplink/clinic"));
                insuranceCardLink.setDeleteTPLink(XMLUtils.getNodeValue(cardDoc, "//tplink/DeleteTPLink"));
                insuranceCardLink.setEligible(XMLUtils.getNodeValue(cardDoc, "//tplink/eligible"));
                insuranceCardLink.setEligovr(XMLUtils.getNodeValue(cardDoc, "//tplink/eligovr"));
                insuranceCardLink.setEmploy(XMLUtils.getNodeValue(cardDoc, "//tplink/employ"));
                insuranceCardLink.setLocation(XMLUtils.getNodeValue(cardDoc, "//tplink/location"));
                insuranceCardLink.setMedicaidID(XMLUtils.getNodeValue(cardDoc, "//tplink/medicaidID"));
                insuranceCardLink.setMedicaidInd(XMLUtils.getNodeValue(cardDoc, "//tplink/medicaidInd"));
                insuranceCardLink.setNh(XMLUtils.getNodeValue(cardDoc, "//tplink/nh"));
                insuranceCardLink.setOther(XMLUtils.getNodeValue(cardDoc, "//tplink/other"));
                insuranceCardLink.setPatBenNoAssign(XMLUtils.getNodeValue(cardDoc, "//tplink/patBenNoAssign"));
                insuranceCardLink.setPlan(XMLUtils.getNodeValue(cardDoc, "//tplink/plan"));
                insuranceCardLink.setQualCMSFacility(XMLUtils.getNodeValue(cardDoc, "//tplink/qualCMSFacility"));
                insuranceCardLink.setResidence(XMLUtils.getNodeValue(cardDoc, "//tplink/residence"));
                insuranceCardLink.setSc(XMLUtils.getNodeValue(cardDoc, "//tplink/sc"));
                insuranceCardLink.setSeries(XMLUtils.getNodeValue(cardDoc, "//tplink/series"));
                insuranceCardLink.setStudent(XMLUtils.getNodeValue(cardDoc, "//tplink/student"));
                insuranceCardLink.setSpecial(XMLUtils.getNodeValue(cardDoc, "//tplink/special"));

                cardLinkFile[i] = insuranceCardLink;
            }

        } catch (mscriptsException mex) {
            LOGGER.error(" Exception occured while parsing customer prescription:", mex);
            throw mex;
        } catch (Exception ex) {
            LOGGER.error(" Exception occured while parsing customer prescription:", ex);
            throw new mscriptsException(null, errorSource, mscriptsExceptionSeverity.Medium, ex);
        }

        return cardLinkFile;
    }
}
