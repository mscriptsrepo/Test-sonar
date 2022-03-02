package com.mscripts.externalrequesthandler.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.mscripts.configurationhandler.config.ConfigManager;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.domain.json.Response;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.domain.PatientNotificationRequest;
import com.mscripts.externalrequesthandler.domain.PatientNotificationResponse;
import com.mscripts.externalrequesthandler.service.PatientNotificationService;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.ErrorCodes;
import com.mscripts.utils.MiscUtils;
import com.mscripts.utils.XMLUtils;
import com.mscripts.utils.mscriptsExceptionSeverity;

public class PatientNotificationController extends MultiActionController {
	
	private static final Logger LOGGER_NON_PHI = LogManager
			.getLogger("non.phi." + PatientNotificationController.class.getName());
	private static final Logger LOGGER_PHI = LogManager
			.getLogger("phi." + PatientNotificationController.class.getName());

	PatientNotificationService patientNotificationService;
	
	public PatientNotificationService getPatientNotificationService() {
		return patientNotificationService;
	}

	public void setPatientNotificationService(PatientNotificationService patientNotificationService) {
		this.patientNotificationService = patientNotificationService;
	}

	public PatientNotificationController() {
		ClassPathXmlApplicationContext ct = new ClassPathXmlApplicationContext("applicationContext.xml");

		patientNotificationService = (PatientNotificationService) ct.getBean("patientNotificationService");
		ct.close();
	}

	public void processPatientNotification(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, MscriptsException, TransformerConfigurationException,
			TransformerException {
		LOGGER_NON_PHI.info("Inside processPatientNotification Controller");

		String langCode = Constants.LANG_CODE;

		PatientNotificationResponse patientNotificationResponse = new PatientNotificationResponse();

		// Create the XML response document.
		Document dcResponse = null;
		try {
			dcResponse = XMLUtils.createXMLDocument();

			final String reqString = MiscUtils.convertInputStreamToString(request.getInputStream());
			final byte[] reqStringBytes = reqString.getBytes();

			String cvIsWSo2Enabled = ConfigReader.readConfig(Constants.cvDefaultClientId, langCode, ConfigKeys.CVISWSO2ENABLED);

			if ("1".equals(cvIsWSo2Enabled)) {

				LOGGER_PHI.log(Level.getLevel("SUPPORT"), " :: Request from WSO2  ={} " + reqString);

				PatientNotificationRequest patientNotification = null;
				SAXParserFactory spf = SAXParserFactory.newInstance();
				spf.setFeature(Constants.XML_EXTERNAL_GENERAL_ENTITIES, false);
				spf.setFeature(Constants.XML_EXTERNAL_PARAMETER_ENTITIES, false);
				spf.setFeature(Constants.XML_LOAD_EXTERNAL_DTD, false);
				Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(),
                    new InputSource(new StringReader(reqString)));
				JAXBContext jaxbContext = JAXBContext.newInstance(PatientNotificationRequest.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				patientNotification = (PatientNotificationRequest) jaxbUnmarshaller.unmarshal(xmlSource);

				patientNotificationResponse = patientNotificationService
							.processPatientNotification(patientNotification, reqString);
			}

			LOGGER_NON_PHI.info("Framing response to PDX Handler WSO2");
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception: " + e.getMessage());
			patientNotificationResponse.setStatus(Constants.RESPONSE_FAILURE);
			patientNotificationResponse.setHttpStatusCode(Constants.HTTP_STATUS_CODE_FAILURE);
			patientNotificationResponse.setError(e.getMessage());
			patientNotificationResponse.setErrorCode(ErrorCodes.GENERIC_ERROR);
			patientNotificationResponse.setErrorMessage(null);
			patientNotificationResponse.setErrorId(null);
		} finally {
			response.setStatus(Integer.parseInt(patientNotificationResponse.getHttpStatusCode()));

			// Create the XML response document.
			Element elresponse = dcResponse.createElement("response");
			dcResponse.appendChild(elresponse);

			Element elStatus = dcResponse.createElement("status");
			elStatus.setTextContent(patientNotificationResponse.getStatus());
			elresponse.appendChild(elStatus);

			Element elHttpStatusCode = dcResponse.createElement("http_status_code");
			elHttpStatusCode.setTextContent(patientNotificationResponse.getHttpStatusCode());
			elresponse.appendChild(elHttpStatusCode);

			Element elError = dcResponse.createElement("error");
			elError.setTextContent(patientNotificationResponse.getError());
			elresponse.appendChild(elError);

			Element elErrorCode = dcResponse.createElement("error_code");
			elErrorCode.setTextContent(patientNotificationResponse.getErrorCode());
			elresponse.appendChild(elErrorCode);

			Element elErrorMessage = dcResponse.createElement("error_message");
			elErrorMessage.setTextContent(patientNotificationResponse.getErrorMessage());
			elresponse.appendChild(elErrorMessage);

			Element elErrorId = dcResponse.createElement("error_id");
			elErrorId.setTextContent(patientNotificationResponse.getErrorId());
			elresponse.appendChild(elErrorId);

			response.getOutputStream().print(XMLUtils.convertXMLtoString(dcResponse));
		}
	}
	
	@RequestMapping(value = { "/configRefresh" }, method = RequestMethod.GET)
	public void configRefresh(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String clientId = Constants.cvDefaultClientId;
		String responseString = null;
		String msg = "message";
		
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		Response resp = new Response();

		try {

			LOGGER_PHI.debug("Entered PatientNotificationController.configRefresh");
			
			ConfigManager configManagerObject = new ConfigManager();
			configManagerObject.configUpdate();

			msg = Constants.RESPONSE_SUCCESS;
			resp.setCode(Constants.HTTP_STATUS_CODE_SUCCESS);
			resp.setStatus(Constants.RESPONSE_SUCCESS);
			resp.setMessage(msg);
			resp.setDescription("");

			ObjectMapper mapper = new ObjectMapper();
			Writer strWriter = new StringWriter();
			mapper.writeValue(strWriter, resp);
			responseString = strWriter.toString();
			response.getOutputStream().println(responseString);

		} catch (Exception e) {
			LOGGER_NON_PHI.error("Error occurred while processing ConfigRefresh request = ", e);

			Response responseError = new Response();
			responseError.setStatus(Constants.RESPONSE_FAILURE);
			responseError.setCode(Constants.HTTP_STATUS_CODE_PARTIAL_SUCCESS);
			responseError.setMessage(new MscriptsException(clientId, e.getMessage(),
					Constants.CV_ERROR_CODE_EMPTY_ATTRIBUTE, e, errorSeverity).getErrorMessage());
			responseError.setErrorCode(Constants.CV_ERROR_CODE_EMPTY_ATTRIBUTE);

			Writer strWriter = new StringWriter();

			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(strWriter, responseError);

			responseString = strWriter.toString();
			response.getOutputStream().println(responseString);
			LOGGER_NON_PHI.error("Exception occured while refreshing  configs:", e);
		} finally {
			LOGGER_NON_PHI.info("**************Exited PatientNotificationController.configRefresh()****************");
		}
	}

}
