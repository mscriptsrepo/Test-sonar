/**
 * *****************************************************
 * Title : ExternalRequestHandler.java Author : Pratyush Description : Web
 * Service class for handling external requests Modification History: Not
 * Applicable Created : 11-Feb-10 Modified : Not Applicable Notes : None
 * *****************************************************
 */
package com.mscripts.externalrequesthandler.service;

import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.externalrequesthandler.dao.GeneralDao;
import com.mscripts.utils.AppConfiguration;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.PHICredentials;
import com.mscripts.utils.XMLUtils;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionHandler;
import com.mscripts.utils.mscriptsExceptionSeverity;
/**
 *
 * @author ppushkar
 */
@WebService()
public class ExternalRequestHandler {

    private GeneralService generalService;
    private AppConfiguration appConfiguration;
    private NotificationService notificationService;
    private GeneralDao generalDao;
    private mscriptsExceptionHandler mscExceptionHandler;
    private CouponFeedService couponFeedService;
    private TenForTenFeedService tenForTenFeedService;
    private PHICredentials pHICredentials;
    private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + ExternalRequestHandler.class.getName());
    private static final Logger LOGGER_PHI = LogManager.getLogger("phi." +ExternalRequestHandler.class.getName());

    //Constructor that loads the application context and initializes beans.
    public ExternalRequestHandler() {
        ClassPathXmlApplicationContext ct = new ClassPathXmlApplicationContext("applicationContext.xml");
        generalService = (GeneralService) ct.getBean("generalService");
        appConfiguration = (AppConfiguration) ct.getBean("appConfiguration");
        notificationService = (NotificationService) ct.getBean("notificationService");
        generalDao = (GeneralDao) ct.getBean("generalDao");
        mscExceptionHandler = (mscriptsExceptionHandler) ct.getBean("mscriptsExceptionHandler");
        couponFeedService = (CouponFeedService) ct.getBean("couponFeedService");
        pHICredentials = (PHICredentials) ct.getBean("pHICredentials");
        tenForTenFeedService = (TenForTenFeedService) ct.getBean("tenForTenFeedService");
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "initialLinkTokenMessage")
    public String initialLinkTokenMessage(@WebParam(name = "mobile") String mobile, @WebParam(name = "token") String token,
            @WebParam(name = "verificationCode") String verificationCode, @WebParam(name = "action") String action,
            @WebParam(name = "fName") String fName, @WebParam(name = "lName") String lName,
            @WebParam(name = "storeID") String storeID, @WebParam(name = "timeZone") String timeZone,
            @WebParam(name = "oldToken") String oldToken, @WebParam(name = "oldMobile") String oldMobile, @WebParam(name = "survivingToken") String survivingToken) {
        //TODO write your implementation code here:++

        Document dcResponse = null;
        String clientID = null;
        String clientName = null;
        String nodeName = "pdxiltm";
        String handlerResponse = null;
        String cvHandlerApiILTM = null;
        String audit = null;
        String handlerResponseCode = null;
        String cvHandlerDefaultResponseCodeFail = null;
        String errorDetailID = null;
        String addedToQueue = null;
        String cvHandlerApiFCILTM = null;
        String cvErrorCodeInvRequest = null;
        String errorSource = "com.mscripts.externalrequesthandler.service.ExternalRequestHandler-initialLinkTokenMessage";
        String langCode = Constants.LANG_CODE;
        
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        
        audit = "mobile:" + mobile + ",token:" + token + ",generatedAuthCode:" + verificationCode + ",action:" + action + ",patientFirstName:" + fName + ",patientLastName:" + lName + ",requestingStoreNcpdp:" + storeID + ",timezone:" + timeZone + ",oldToken:" + oldToken + ",oldMobile:" + oldMobile + ",survivingToken:" + survivingToken;
        try {
            try {
                //Hard coding the clientID to 1 because the client id is still not found.
                cvHandlerApiILTM = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVHANDLERAPIILTM);
                cvHandlerApiFCILTM = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVHANDLERAPIFCILTM);
                clientID = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVHANDLERDEFAULTCLIENTID);
                handlerResponseCode = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVHANDLERDEFAULTRESPONSECODEOK);
                cvHandlerDefaultResponseCodeFail = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVHANDLERDEFAULTRESPONSECODEFAIL);
                cvErrorCodeInvRequest = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVERRORCODEINVREQUEST);
                
                try{
                    Map clientDetailsMap = generalService.getClientDetails(token, mobile, verificationCode, fName, storeID);
                    clientID = clientDetailsMap.get("id").toString();
                    clientName = clientDetailsMap.get("client_name").toString();
                }catch(Exception e){
                	LOGGER_NON_PHI.error("Exception occured while getting client details for the ILTM",e);
                    throw new mscriptsException(cvErrorCodeInvRequest, errorSource, errorSeverity, e);
                }
                
            
                generalService.initialLinkTokenMessage(MiscUtils.sanatizeString(mobile, false), token, verificationCode, action, fName, lName, storeID,
                        timeZone, clientID, MiscUtils.sanatizeString(clientName, true), oldToken, oldMobile, survivingToken, langCode);
                //Create the XML response document.
                dcResponse = XMLUtils.createXMLDocument();
                //Root node of the xml string.
                Element elresponse = dcResponse.createElement("response");
                dcResponse.appendChild(elresponse);
                Element elcustomerDetail = dcResponse.createElement("pdxiltm");
                elresponse.appendChild(elcustomerDetail);
                elcustomerDetail.setAttribute("token", token);
                elcustomerDetail.setAttribute("mobile", mobile);
                elcustomerDetail.setAttribute("statusmessage", "Request processed successfully");
                elcustomerDetail.setAttribute("errormessage", "");
                //Display the output xml stream in the UI.
                handlerResponse = (XMLUtils.convertXMLtoString(dcResponse));
            } catch (mscriptsException mEx) {
                LOGGER_NON_PHI.error("mscripts Exception occured while processing ILTM = {}", mEx);
                handlerResponseCode = cvHandlerDefaultResponseCodeFail;
                String result[] = mscExceptionHandler.handleException(mEx, clientID, nodeName, true, null);
                errorDetailID = result[0];
                handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
            } catch (Exception Ex) {
                //Ex.printStackTrace();
                LOGGER_NON_PHI.error(" Exception occured while processing ILTM = {}", Ex);
                handlerResponseCode = cvHandlerDefaultResponseCodeFail;
                mscriptsException mEx = new mscriptsException(Ex.getMessage(), "com.mscripts.externalrequesthandler.service.ExternalRequestHandler-initialLinkTokenMessage", mscriptsExceptionSeverity.Medium, Ex);
                String result[] = mscExceptionHandler.handleException(mEx, clientID, nodeName, true, null);
                errorDetailID = result[0];
                handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
            }
        } catch (Exception ex) {


            handlerResponseCode = cvHandlerDefaultResponseCodeFail;

            // Need to handle this!! (default it to error xml)
            handlerResponse = "<?xml version='1.0' encoding='UTF-8' standalone='no'?> <response><" + nodeName + "><error errorcode='EC__000' message='There was an error while processing the request.Please try again.'/> </" + nodeName + "> </response>  ";

            LOGGER_NON_PHI.error(" Exception occured while processing ILTM with handlerresponsecode ={}", handlerResponseCode + "having node name={}" ,nodeName + " ={} ", ex);
        }
        try {
            // Audit the update anticipated refill date request
            
                LOGGER_NON_PHI.info("Adding audit log info to Database while initialising link token message");
          
            generalDao.auditLog(clientID, cvHandlerApiILTM, cvHandlerApiFCILTM, audit, handlerResponseCode, errorDetailID, handlerResponse, addedToQueue, pHICredentials.getSecondaryKey());
        } catch (Exception e) {
            LOGGER_NON_PHI.error(" Exception occured while auditing log to database with clientID ={}" , clientID + "={} and handlerResponseCode={}",handlerResponseCode, e);
        }
        return handlerResponse;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "updateNotification")
    public String updateNotification(@WebParam(name = "patient_change_xml") String patient_change_xml, @WebParam(name = "token") String token) {
        String cvErrorCodeNoUpdate = null;
        String cvErrorCodeInvRequest = null;
        String cvErrorCodeInvSMSToken = null;
        String cvHandlerApiFCPU = null;
        String nodeName = "PatientUpdate";
        String errorSource = "com.mscripts.externalrequesthandler.service.ExternalRequestHandler-updateNotification";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        NodeList nodes;
        NodeList emailNodes;
        NodeList prescriptionUpdates;
        NodeList deleteEmailNodes;
        NodeList patientNode;
        NodeList insuranceLinkNode;
        NodeList insuranceCardNode;
        Node emailNode;
        Node deleteEmailNode;
        Node deceasedNode;
        String emailid = null;
        Document doc = null;
        Document dcResponse = null;
        String clientID = null;
        String patientUpdateNode = null;
        String deleteEmailValue = null;
        String handlerResponse = null;
        String cvHandlerApiPU = null;
        String audit = null;
        String handlerResponseCode = null;
        String cvHandlerDefaultResponseCodeFail = null;
        String errorDetailID = null;
        String addedToQueue = null;

        try {
            try {
                //Get the message from config file.
                clientID = ConfigReader.readConfig(Constants.cvDefaultClientId,Constants.DEFAULT_LANGUAGE,"cvHandlerDefaultClientID");
                cvErrorCodeNoUpdate = ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvErrorCodeNoUpdate");
                cvErrorCodeInvRequest =ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvErrorCodeInvRequest");
                cvErrorCodeInvSMSToken =ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvErrorCodeInvSMSToken");
                cvHandlerApiPU = ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvHandlerApiPU");
                cvHandlerApiFCPU = ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvHandlerApiFCPU");
                handlerResponseCode = ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvHandlerDefaultResponseCodeOk");
                cvHandlerDefaultResponseCodeFail = ConfigReader.readConfig(Constants.cvDefaultClientId, Constants.DEFAULT_LANGUAGE,"cvHandlerDefaultResponseCodeFail");

                audit = "token:" + token + ",patient_change_xml" + patient_change_xml;

                try {
                    Map clientDetailsMap = generalService.getClientDetails(token, null, null, null, null);
                    clientID = clientDetailsMap.get("id").toString();
                    doc = XMLUtils.createXMLDocument(patient_change_xml);
                } catch (Exception e) {
                    throw new mscriptsException(cvErrorCodeInvRequest, errorSource, errorSeverity, e);
                }

				NodeList xmlMessageNodeList = XMLUtils.getNodeList(doc, "//request/XmlMessage");
				Node xmlMessageNode = xmlMessageNodeList.item(0);
				NamedNodeMap xmlMessageNodeMap = xmlMessageNode.getAttributes();
				String messageDate = xmlMessageNodeMap.getNamedItem("messageDate").getNodeValue();
				String messageTime = xmlMessageNodeMap.getNamedItem("messageTime").getNodeValue();
				messageDate = messageDate.concat(" ").concat(messageTime);

				// Retrieve the update node.
				nodes = XMLUtils.getNodeList(doc, "//request/XmlMessage/patientUpdate");
				patientUpdateNode = "patientUpdate";
				if (nodes.getLength() == 0) {
					// If update not found then throw exception.
					throw new mscriptsException(cvErrorCodeNoUpdate, errorSource, errorSeverity, null);
				}

                //Read the email node.
                emailNodes = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/pdxemail");
                Map map = generalService.isValidSmsToken(token, clientID);

                //invalid token
				if (map == null) {
                        LOGGER_NON_PHI.info("Map recieved after validating SMS token is null");
                    throw new mscriptsException(cvErrorCodeInvSMSToken, errorSource, errorSeverity, null);
                } else {
					String customerID = (String) map.get("id");

                    if (emailNodes.getLength() > 0) {
                        emailNodes = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/pdxemail/email");
                        emailNode = emailNodes.item(0);
                        //Retrieve the email id value.
                        emailid = emailNode.getFirstChild().getNodeValue();

                        deleteEmailNodes = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/pdxemail/DeleteEmail");
                        deleteEmailNode = deleteEmailNodes.item(0);
                        deleteEmailValue = deleteEmailNode.getFirstChild().getNodeValue();

                        notificationService.updateCustomerEmail(map.get("id").toString(), emailid, deleteEmailValue, clientID);
                    }

                    /* Update Insurance */
                    try {
                        insuranceLinkNode = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/tplink");
                        insuranceCardNode = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/card");
                        if (insuranceCardNode.getLength() > 0 || insuranceLinkNode.getLength() > 0) {
                            
                            //call service to update card and link
                            notificationService.updateInsurance(clientID,customerID, insuranceCardNode, insuranceLinkNode);
                        }
              
                    } catch (Exception ex) {
                        LOGGER_NON_PHI.error("Error while updating insurance update", ex);
                        //Log error and continue with prescription nodes processing.
                    }

					// Update Customer Rxcom Id
					try {
						Document patientUpdateDoc = XMLUtils.createXMLDocument(XMLUtils.nodeToString(nodes.item(0)));
						String sRxcomId = XMLUtils.getNodeValue(patientUpdateDoc, "/patientUpdate/patientRxcomID");
						sRxcomId = sRxcomId.isEmpty() ? null : sRxcomId;
						notificationService.updateRxcomId(clientID, customerID, sRxcomId);
					} catch (Exception ex) {
						LOGGER_NON_PHI.error("Error while updating insurance update", ex);
						// Log error and continue with prescription nodes
						// processing.
					}

                    prescriptionUpdates = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/rxfile");
                    if (prescriptionUpdates.getLength() > 0) {

						notificationService.updateCustomerPrescription(map.get("id").toString(),
								XMLUtils.convertXMLtoString(doc), clientID, patientUpdateNode, messageDate);
                    }

                    patientNode = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/patient/deceased");
                    if (patientNode.getLength() > 0) {
                        deceasedNode = patientNode.item(0);
                        String deceased = deceasedNode.getFirstChild().getNodeValue();
                        notificationService.updatePatient(clientID, map.get("id").toString(), deceased);
					}
                    
                    patientNode = XMLUtils.getNodeList(doc, "//request/XmlMessage/" + patientUpdateNode + "/patient/recordType");
                    if (patientNode.getLength() > 0) {
                    	String sRecordType = patientNode.item(0).getFirstChild().getNodeValue();
                        notificationService.updateRecordType(clientID, customerID, sRecordType);
					}

                    //Create the XML response document.
                    dcResponse = XMLUtils.createXMLDocument();
                    //Root node of the xml string.
                    Element elresponse = dcResponse.createElement("response");
                    dcResponse.appendChild(elresponse);
                    Element patientUpdate = dcResponse.createElement("PatientUpdate");
                    elresponse.appendChild(patientUpdate);
                    patientUpdate.setAttribute("errormessage", "");
                    patientUpdate.setAttribute("status", "200");
                    patientUpdate.setAttribute("description", "Update Processed");
                    //Display the output xml stream in the UI.
                    handlerResponse = XMLUtils.convertXMLtoString(dcResponse);
                }
            } catch (mscriptsException mEx) {

                LOGGER_NON_PHI.error("mscripts Exception occured while updating notifications={}", mEx);
                handlerResponseCode = cvHandlerDefaultResponseCodeFail;
                String result[] = mscExceptionHandler.handleException(mEx, clientID, nodeName, true, null);
                errorDetailID = result[0];
                handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
            } catch (Exception Ex) {
                LOGGER_NON_PHI.error(" Exception occured while updating notifications={}", Ex);
                handlerResponseCode = cvHandlerDefaultResponseCodeFail;
                mscriptsException mEx = new mscriptsException(Ex.getMessage(), errorSource, errorSeverity, Ex);
                //Set the "errormessage" attribute with the exception occured and display the response stream.
                String result[] = mscExceptionHandler.handleException(mEx, clientID, nodeName, true, null);
                errorDetailID = result[0];
                handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
            }
        } catch (Exception ex) {
            handlerResponseCode = cvHandlerDefaultResponseCodeFail;
            // Need to handle this!! (default it to error xml)
            handlerResponse = "<?xml version='1.0' encoding='UTF-8' standalone='no'?> <response><" + nodeName + "><error errorcode='EC__000' message='There was an error while processing the request.Please try again.'/>   </" + nodeName + "> </response>  ";

            LOGGER_NON_PHI.error(" Exception occured while updating notifications with handlerresponsecode ={}" , handlerResponseCode + "having node name={}" , nodeName + " : ", ex);
        }
        try {
            // Audit the update anticipated refill date request
            
                LOGGER_NON_PHI.info("Adding audit log info to Database while updating notifiactions");
            
            generalDao.auditLog(clientID, cvHandlerApiPU, cvHandlerApiFCPU, audit, handlerResponseCode, errorDetailID, handlerResponse, addedToQueue, pHICredentials.getSecondaryKey());
        } catch (Exception e) {
            LOGGER_NON_PHI.error(" Exception occured while auditing log to database while updating notifications with clientID ={}" , clientID + ": and handlerResponseCode ={} " , handlerResponseCode, e);
        }
        return handlerResponse;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "feedCoupons")
    public String feedCoupons(@WebParam(name = "campaignId") String campaignID, @WebParam(name = "campaignXml") String campaignXml) {
        Document dcResponse = null;
        String handlerResponseCode = null;
        String cvHandlerDefaultResponseCodeFail = null;
        String handlerResponse = null;
        String cvHandlerApiFCFC = null;
        String clientID = null;
        String cvErrorCodeInvRequest = null;
        String cvErrorCodeNoFeed = null;
        String cvHandlerApiFC = null;
        String errorDetailID = null;
        String nodeName = "campaign";
        String audit = null;
        String addedToQueue = null;
        String errorSource = "com.mscripts.externalrequesthandler.service.ExternalRequestHandler-feedCoupons";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;

        audit = "campaignID:" + campaignID + ",campaignXml" + campaignXml;

        try {
            clientID = appConfiguration.get("cvHandlerDefaultClientID");
            cvErrorCodeInvRequest = this.appConfiguration.get("cvErrorCodeInvRequest");
            cvErrorCodeNoFeed = this.appConfiguration.get("cvErrorCodeNoFeed");
            cvHandlerDefaultResponseCodeFail = appConfiguration.get("cvHandlerDefaultResponseCodeFail");
            cvHandlerApiFC = appConfiguration.get("cvHandlerApiFC");
            cvHandlerApiFCFC = appConfiguration.get("cvHandlerApiFCFC");
            handlerResponseCode = appConfiguration.get("cvHandlerDefaultResponseCodeOk");
            Document campaignDoc = null;
            try {
                Map clientDetailsMap = generalService.getClientDetails(appConfiguration.get("cvPdxClientName"));
                clientID = clientDetailsMap.get("id").toString();
                campaignDoc = XMLUtils.createXMLDocument(campaignXml);
            } catch (Exception e) {
                LOGGER_NON_PHI.error(" Exception occured while feeding coupons :", e);
                throw new mscriptsException(cvErrorCodeInvRequest, errorSource, errorSeverity, e);
            }

            NodeList batchNodes = XMLUtils.getNodeList(campaignDoc, "//" + nodeName + "/batch");
            //Check if update exist.
            if (batchNodes.getLength() == 0) {
                //If update not found then throw exception.
                throw new mscriptsException(cvErrorCodeNoFeed, errorSource, errorSeverity, null);
            }

            couponFeedService.addCouponBatches(campaignID, batchNodes, clientID);

            //Create the XML response document.
            dcResponse = XMLUtils.createXMLDocument();
            //Root node of the xml string.
            Element elResponse = dcResponse.createElement("response");
            dcResponse.appendChild(elResponse);
            Element couponFeed = dcResponse.createElement("campaign");
            elResponse.appendChild(couponFeed);
            couponFeed.setAttribute("errormessage", "");
            couponFeed.setAttribute("status", handlerResponseCode);
            couponFeed.setAttribute("description", "Feed Processed");
            //Display the output xml stream in the UI.
            handlerResponse = XMLUtils.convertXMLtoString(dcResponse);

        } catch (mscriptsException mEx) {
            LOGGER_NON_PHI.error("mscripts Exception occured while feeding coupons :", mEx);
            handlerResponseCode = cvHandlerDefaultResponseCodeFail;
            String result[] = mscExceptionHandler.handleException(mEx, clientID, nodeName, true, null);
            errorDetailID = result[0];
            handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
        } catch (Exception Ex) {
            LOGGER_NON_PHI.error("Exception occured while feeding coupons :", Ex);
            handlerResponseCode = cvHandlerDefaultResponseCodeFail;
            mscriptsException mEx = new mscriptsException(Ex.getMessage(), errorSource, errorSeverity, Ex);
            //Set the "errormessage" attribute with the exception occured and display the response stream.
            String result[] = mscExceptionHandler.handleException(mEx, clientID, nodeName, true, null);
            errorDetailID = result[0];
            handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
        }
        try {
            // Audit the update anticipated refill date request
            
                LOGGER_NON_PHI.info("Adding audit log info to Database while feeding coupons");
          
            generalDao.auditLog(clientID, cvHandlerApiFC, cvHandlerApiFCFC, audit, handlerResponseCode, errorDetailID, handlerResponse, addedToQueue, pHICredentials.getSecondaryKey());
        } catch (Exception e) {
            LOGGER_NON_PHI.error(" Exception occured while auditing log to database while feeding coupons with clientID ={}" , clientID + ": and handlerResponseCode ={}" , handlerResponseCode, e);
        }
        return handlerResponse;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "feedTenForTen")
    public String feedTenForTen(@WebParam(name = "TenForTenList") String tenForTenFeedXml) {
        //TODO write your implementation code here:
        //Initialize variables.
        Document dcResponse = null;
        String handlerResponseDescription = null;
        String clientID = null;
        String cvHandlerApiFTFT = null;
        String cvHandlerDefaultResponseCodeFail = null;
//        String cvErrorString = null;
        String addedToQueue = null;
        String cvErrorCodeInvRequest = null;
//        String cvErrorCodeIncompleteWebParams = null;
        Document tenForTenFeedXmlDoc = null;
        String cvErrorCodeInvalidXML = null;
        String handlerRequestBody = tenForTenFeedXml;
        String handlerResponseCode = null;
        String handlerResponse = null;
//        String errorMessage = null;
        String errorDetailID = null;
        String feedRunID = null;
        Map feedRunMap = null;
        String errorRecords = "";
        String errorSource = "com.mscripts.externalrequesthandler.service.ExternalRequestHandler-feedTenForTen";
        mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
        String cvHandlerApiFCFTFT = null;


        try {

            NodeList nodes = null;
            String feedDate = null;
            String feedFileName = null;
            //Read config variables
            cvHandlerApiFTFT = appConfiguration.get("cvHandlerApiFTFT");
            cvHandlerApiFCFTFT = appConfiguration.get("cvHandlerApiFCFTFT");
            cvErrorCodeInvRequest = this.appConfiguration.get("cvErrorCodeInvRequest");
            clientID = appConfiguration.get("cvHandlerDefaultClientID");
            handlerResponseCode = appConfiguration.get("cvHandlerDefaultResponseCodeOk");
            cvHandlerDefaultResponseCodeFail = appConfiguration.get("cvHandlerDefaultResponseCodeFail");
            handlerResponseDescription = appConfiguration.get("cvHandlerDefaultResponseDescription");

//            cvErrorString = appConfiguration.get("cvErrorString");
            cvErrorCodeInvalidXML = appConfiguration.get("cvErrorCodeInvalidXML");

            //Get client ID
            try {
                Map clientDetailsMap = generalService.getClientDetails(appConfiguration.get("cvPdxClientName"));
                clientID = clientDetailsMap.get("id").toString();
            } catch (Exception e) {

                LOGGER_NON_PHI.error("Exception occured while feeding ten for ten  coupons:", e);
                throw new mscriptsException(cvErrorCodeInvRequest, errorSource, errorSeverity, e);
            }

            //Validate request XML
            try {
                handlerRequestBody = handlerRequestBody.replaceAll("> <", "><");
                tenForTenFeedXmlDoc = XMLUtils.createXMLDocument(handlerRequestBody);
                nodes = XMLUtils.getNodeList(tenForTenFeedXmlDoc, "//GetTenForTenRequest/cardfile");
                feedDate = XMLUtils.getNodeList(tenForTenFeedXmlDoc, "//feeddate").item(0).getFirstChild().getNodeValue();
                feedFileName = XMLUtils.getNodeList(tenForTenFeedXmlDoc, "//feedfilename").item(0).getFirstChild().getNodeValue();
            } catch (Exception ex) {
                LOGGER_NON_PHI.error("Exception occured while feeding ten for ten  coupons:", ex);
                throw new mscriptsException(cvErrorCodeInvalidXML, errorSource, errorSeverity, ex);
            }

            feedRunMap = tenForTenFeedService.feedTenForTen(nodes, feedFileName, feedDate, clientID);

            if (feedRunMap.get("recordsWithError") != null) {
                errorRecords = feedRunMap.values().toString().substring(1, feedRunMap.values().toString().length() - 1);
            }
        } catch (mscriptsException mEx) {
            String exErrorMessage = mEx.getErrorMessage();
            handlerResponseCode = cvHandlerDefaultResponseCodeFail;
            String result[] = mscExceptionHandler.handleException(mEx, clientID, null, false, null);
            errorDetailID = result[0];
            handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
            LOGGER_NON_PHI.error("mscripts Exception occured while feeding ten for ten  coupons:", mEx);
        } catch (Exception Ex) {
            String exErrorMessage = Ex.getMessage();
            handlerResponseCode = cvHandlerDefaultResponseCodeFail;
            mscriptsException mEx = new mscriptsException(exErrorMessage, errorSource, errorSeverity, Ex);
            String result[] = mscExceptionHandler.handleException(mEx, clientID, null, false, null);
            errorDetailID = result[0];
            handlerResponse = errorDetailID + "-handlerErrorDetail-" + result[1];
            LOGGER_NON_PHI.error(" Exception occured while feeding ten for ten  coupons:", Ex);
        }

        //Create XML response
        try {
            dcResponse = XMLUtils.createXMLDocument();
            //Root node of the xml string.
            Element elresponse = dcResponse.createElement("GetTenForTenResponse");
            dcResponse.appendChild(elresponse);
            elresponse.setAttribute("status", handlerResponseCode);
            elresponse.setAttribute("description", handlerResponseDescription);
            elresponse.setAttribute("errorrecords", errorRecords);
            handlerResponse = XMLUtils.convertXMLtoString(dcResponse);
        } catch (Exception ex) {
            LOGGER_NON_PHI.error(" Exception occured while feeding ten for ten  coupons:", ex);
        }
        try {
            // Audit the update bulk rx pickup request
            
                LOGGER_NON_PHI.info("Adding audit log info to Database while feeding ten to ten coupons");
            
            generalDao.auditLog(clientID, cvHandlerApiFTFT, cvHandlerApiFCFTFT, handlerRequestBody, handlerResponseCode, errorDetailID, handlerResponse, addedToQueue, pHICredentials.getSecondaryKey());
        } catch (Exception e) {
            LOGGER_NON_PHI.error(" Exception occured while auditing log to database while feeding ten to ten coupons with clientID ={}" , clientID + "{} and handlerResponseCode ={} " , handlerResponseCode, e);
        }

        // Return response
        return handlerResponse;
    }
    

}
