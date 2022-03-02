package com.mscripts.externalrequesthandler.service;

import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.domain.PatientNotificationRequest;
import com.mscripts.externalrequesthandler.domain.PatientNotificationResponse;
import com.mscripts.utils.mscriptsException;

public interface PatientNotificationService {

	public PatientNotificationResponse processPatientNotification(PatientNotificationRequest patientNotification,
			String requestString) throws mscriptsException, MscriptsException;
}
