/**
 * *****************************************************
 * Title : NotificationDaoJdbc.java Author : Abhinandan U s Description :
 * Implements the NotificationDao interface. Modification History: Not
 * Applicable Created : 18-Jan-10 Modified : Not Applicable Notes : None
 * *****************************************************
 */
package com.mscripts.externalrequesthandler.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mscripts.configurationhandler.config.ConfigReader;
import com.mscripts.dao.QueryInvoker;
import com.mscripts.dao.SPInvoker;
import com.mscripts.dispensing.invocation.domain.Transaction;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionTxtpfile;
import com.mscripts.domain.SendCommunicationMode;
import com.mscripts.exceptions.MscriptsException;
import com.mscripts.externalrequesthandler.dao.NotificationDao;
import com.mscripts.externalrequesthandler.domain.CustomerPrescription;
import com.mscripts.externalrequesthandler.domain.CustomerTransactionFile;
import com.mscripts.externalrequesthandler.domain.InsuranceCard;
import com.mscripts.externalrequesthandler.domain.InsuranceCardLink;
import com.mscripts.utils.ConfigKeys;
import com.mscripts.utils.Constants;
import com.mscripts.utils.ErrorCodes;
import com.mscripts.utils.MscriptsStringUtils;
import com.mscripts.utils.QueryBuilder;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author abhinandanus
 */
public class NotificationDaoJdbc implements NotificationDao {

	private SPInvoker spInvoker;
	private SPInvoker readInvoker;
	private QueryInvoker queryInvoker;
	private DataSourceTransactionManager transactionManager;
	private Properties transactionAttributes;

	private static Logger LOGGER = LogManager.getLogger(GeneralDaoJdbc.class);
	private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + GeneralDaoJdbc.class.getName());
	private static final Logger LOGGER_PHI = LogManager.getLogger("phi." + GeneralDaoJdbc.class.getName());

	@Override
	public boolean updateCustomerEmail(String customerid, String emailid, String deleteEmail, String clientID,
			String accessKey) throws mscriptsException {
		try {
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into update customer method for customerId " + customerid);
			}
			spInvoker.invokeSp("sp_ERH_updateCustomerEmail",
					new Object[] { clientID, customerid, emailid, deleteEmail, accessKey });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while updating customer email :", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.dao.NotificationDao-updateCustomerEmail",
					mscriptsExceptionSeverity.Medium, ex);
		}
		return true;
	}

	@Override
	public void updatePatient(String clientID, String customerID, String deceased) throws mscriptsException {
		try {

			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into update patient method for customerId " + customerID);
			}

			spInvoker.invokeSp("sp_OPH_updateDeceasedPatient", new Object[] { clientID, customerID, deceased });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while updating customer email :", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.dao.NotificationDao-updatePatient",
					mscriptsExceptionSeverity.Medium, ex);
		}
	}

	@Override
	public void logPrescriptionTransactions(String clientID, String customerID, String rxnumber, String storencpdp,
			CustomerTransactionFile[] custTxFile) throws mscriptsException {
		try {
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into log prescriptions  method for customerId " + customerID);
			}

			// Get the CSV for all fields
			String delimiter = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvCouponDelimiter");
			int txfileCount = custTxFile.length;

			if (txfileCount > 0) {

				// Approximate sizes allocated according to practical values.
				// Length also includes delimiter value
				StringBuilder txnum_list = new StringBuilder(12 * txfileCount);
				StringBuilder filledDate_list = new StringBuilder(25 * txfileCount);
				StringBuilder soldDate_list = new StringBuilder(25 * txfileCount);
				StringBuilder willCallReady_list = new StringBuilder(7 * txfileCount);
				StringBuilder phcode_list = new StringBuilder(9 * txfileCount);
				StringBuilder sgcode_list = new StringBuilder(9 * txfileCount);
				StringBuilder drcode_list = new StringBuilder(6 * txfileCount);
				StringBuilder ndc_list = new StringBuilder(16 * txfileCount);
				StringBuilder mfg_list = new StringBuilder(10 * txfileCount);
				StringBuilder status_list = new StringBuilder(7 * txfileCount);
				StringBuilder tpbill_list = new StringBuilder(7 * txfileCount);
				StringBuilder hold_list = new StringBuilder(7 * txfileCount);
				StringBuilder postype_list = new StringBuilder(7 * txfileCount);
				StringBuilder prcode_list = new StringBuilder(7 * txfileCount);
				StringBuilder taxcode_list = new StringBuilder(7 * txfileCount);
				StringBuilder initials_list = new StringBuilder(7 * txfileCount);
				StringBuilder order_list = new StringBuilder(7 * txfileCount);
				StringBuilder rphcoun_list = new StringBuilder(7 * txfileCount);
				StringBuilder techinit_list = new StringBuilder(7 * txfileCount);
				StringBuilder daw_list = new StringBuilder(7 * txfileCount);
				StringBuilder intover_list = new StringBuilder(7 * txfileCount);
				StringBuilder allover_list = new StringBuilder(7 * txfileCount);
				StringBuilder pdover_list = new StringBuilder(7 * txfileCount);
				StringBuilder dcover_list = new StringBuilder(7 * txfileCount);
				StringBuilder dtover_list = new StringBuilder(7 * txfileCount);
				StringBuilder durover_list = new StringBuilder(7 * txfileCount);
				StringBuilder mesg_list = new StringBuilder(7 * txfileCount);
				StringBuilder quantity_list = new StringBuilder(7 * txfileCount);
				StringBuilder refnum_list = new StringBuilder(7 * txfileCount);
				StringBuilder days_list = new StringBuilder(7 * txfileCount);
				StringBuilder cost_list = new StringBuilder(7 * txfileCount);
				StringBuilder accost_list = new StringBuilder(7 * txfileCount);
				StringBuilder discount_list = new StringBuilder(7 * txfileCount);
				StringBuilder tax_list = new StringBuilder(7 * txfileCount);
				StringBuilder price_list = new StringBuilder(7 * txfileCount);
				StringBuilder ucprice_list = new StringBuilder(7 * txfileCount);
				StringBuilder compfee_list = new StringBuilder(7 * txfileCount);
				StringBuilder upcharge_list = new StringBuilder(7 * txfileCount);
				StringBuilder drexp_list = new StringBuilder(7 * txfileCount);
				StringBuilder host_list = new StringBuilder(7 * txfileCount);
				StringBuilder usual_list = new StringBuilder(7 * txfileCount);
				StringBuilder progadd_list = new StringBuilder(7 * txfileCount);
				StringBuilder schdrug_list = new StringBuilder(7 * txfileCount);
				StringBuilder genmesg_list = new StringBuilder(7 * txfileCount);
				StringBuilder nscchoice_list = new StringBuilder(7 * txfileCount);
				StringBuilder counchoice_list = new StringBuilder(7 * txfileCount);
				StringBuilder pacmed_list = new StringBuilder(7 * txfileCount);
				StringBuilder viaprefill_list = new StringBuilder(7 * txfileCount);
				StringBuilder othprice_list = new StringBuilder(7 * txfileCount);
				StringBuilder AcsPriority_list = new StringBuilder(7 * txfileCount);
				StringBuilder decqty_list = new StringBuilder(7 * txfileCount);
				StringBuilder DeleteTx_list = new StringBuilder(7 * txfileCount);
				StringBuilder dispDrugName_list = new StringBuilder(7 * txfileCount);
				StringBuilder dispDrugNDC_list = new StringBuilder(16 * txfileCount);
				StringBuilder dispDrugGPI_list = new StringBuilder(20 * txfileCount);
				StringBuilder txStatus_list = new StringBuilder(10 * txfileCount);

				// Create CSV String for every field, in case there are
				// txfiles>1
				for (CustomerTransactionFile cTx : custTxFile) {

					txnum_list.append(cTx.getTxnum()).append(delimiter);
					filledDate_list.append(cTx.getFilleddate()).append(delimiter);
					soldDate_list.append(cTx.getSolddate()).append(delimiter);
					willCallReady_list.append(cTx.getWillCallReady()).append(delimiter);
					phcode_list.append(cTx.getPhcode()).append(delimiter);
					sgcode_list.append(cTx.getSgcode()).append(delimiter);
					drcode_list.append(cTx.getDrcode()).append(delimiter);
					ndc_list.append(cTx.getNdc()).append(delimiter);
					mfg_list.append(cTx.getMfg()).append(delimiter);
					status_list.append(cTx.getStatus()).append(delimiter);
					tpbill_list.append(cTx.getTpbill()).append(delimiter);
					hold_list.append(cTx.getHold()).append(delimiter);
					postype_list.append(cTx.getPostype()).append(delimiter);
					prcode_list.append(cTx.getPrcode()).append(delimiter);
					taxcode_list.append(cTx.getTaxcode()).append(delimiter);
					initials_list.append(cTx.getInitials()).append(delimiter);
					order_list.append(cTx.getOrder()).append(delimiter);
					rphcoun_list.append(cTx.getRphcoun()).append(delimiter);
					techinit_list.append(cTx.getTechinit()).append(delimiter);
					daw_list.append(cTx.getDaw()).append(delimiter);
					intover_list.append(cTx.getIntover()).append(delimiter);
					allover_list.append(cTx.getAllover()).append(delimiter);
					pdover_list.append(cTx.getPdover()).append(delimiter);
					dcover_list.append(cTx.getDcover()).append(delimiter);
					dtover_list.append(cTx.getDtover()).append(delimiter);
					durover_list.append(cTx.getDurover()).append(delimiter);
					mesg_list.append(cTx.getMesg()).append(delimiter);
					quantity_list.append(cTx.getQuantity()).append(delimiter);
					refnum_list.append(cTx.getRefnum()).append(delimiter);
					days_list.append(cTx.getDays()).append(delimiter);
					cost_list.append(cTx.getCost()).append(delimiter);
					accost_list.append(cTx.getAccost()).append(delimiter);
					discount_list.append(cTx.getDiscount()).append(delimiter);
					tax_list.append(cTx.getTax()).append(delimiter);
					price_list.append(cTx.getPrice()).append(delimiter);
					ucprice_list.append(cTx.getUcprice()).append(delimiter);
					compfee_list.append(cTx.getCompfee()).append(delimiter);
					upcharge_list.append(cTx.getUpcharge()).append(delimiter);
					drexp_list.append(cTx.getDrexp()).append(delimiter);
					host_list.append(cTx.getHost()).append(delimiter);
					usual_list.append(cTx.getUsual()).append(delimiter);
					progadd_list.append(cTx.getProgadd()).append(delimiter);
					schdrug_list.append(cTx.getSchdrug()).append(delimiter);
					genmesg_list.append(cTx.getGenmesg()).append(delimiter);
					nscchoice_list.append(cTx.getNscchoice()).append(delimiter);
					counchoice_list.append(cTx.getCounchoice()).append(delimiter);
					pacmed_list.append(cTx.getPacmed()).append(delimiter);
					viaprefill_list.append(cTx.getViaprefill()).append(delimiter);
					othprice_list.append(cTx.getOthprice()).append(delimiter);
					AcsPriority_list.append(cTx.getAcsPriority()).append(delimiter);
					decqty_list.append(cTx.getDecqty()).append(delimiter);
					DeleteTx_list.append(cTx.getDeleteTx()).append(delimiter);
					dispDrugName_list.append(cTx.getDispDrugName()).append(delimiter);
					dispDrugNDC_list.append(cTx.getDispDrugNDC()).append(delimiter);
					dispDrugGPI_list.append(cTx.getDispDrugGPI()).append(delimiter);
					txStatus_list.append(cTx.getTxstatus()).append(delimiter);

				}

				spInvoker.invokeSp("sp_ERH_logPrescriptionTransactions", new Object[] { clientID, customerID, rxnumber,
						storencpdp, txfileCount, filledDate_list.toString(), soldDate_list.toString(),
						txnum_list.toString(), willCallReady_list.toString(), phcode_list.toString(),
						sgcode_list.toString(), drcode_list.toString(), ndc_list.toString(), mfg_list.toString(),
						status_list.toString(), tpbill_list.toString(), hold_list.toString(), postype_list.toString(),
						prcode_list.toString(), taxcode_list.toString(), initials_list.toString(),
						order_list.toString(), rphcoun_list.toString(), techinit_list.toString(), daw_list.toString(),
						intover_list.toString(), allover_list.toString(), pdover_list.toString(),
						dcover_list.toString(), dtover_list.toString(), durover_list.toString(), mesg_list.toString(),
						quantity_list.toString(), refnum_list.toString(), days_list.toString(), cost_list.toString(),
						accost_list.toString(), discount_list.toString(), tax_list.toString(), price_list.toString(),
						ucprice_list.toString(), compfee_list.toString(), upcharge_list.toString(),
						drexp_list.toString(), host_list.toString(), usual_list.toString(), progadd_list.toString(),
						schdrug_list.toString(), genmesg_list.toString(), nscchoice_list.toString(),
						counchoice_list.toString(), pacmed_list.toString(), viaprefill_list.toString(),
						othprice_list.toString(), AcsPriority_list.toString(), decqty_list.toString(),
						DeleteTx_list.toString(), dispDrugName_list.toString(), dispDrugNDC_list.toString(),
						dispDrugGPI_list.toString(), txStatus_list.toString(), delimiter });

			}
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while updating customer email :", ex);
			throw new mscriptsException(ex.getMessage(),
					"com.mscripts.externalrequesthandler.dao.NotificationDao-logPrescriptionTransactions",
					mscriptsExceptionSeverity.Medium, ex);
		}
	}

	@Override
	public void updateCustomerCard(String clientID, String customerID, InsuranceCard insCard, String secondaryKey)
			throws mscriptsException {
		try {

			spInvoker.invokeSp("sp_ERH_updateCustomerInsurance",
					new Object[] { clientID, customerID, insCard.getCarrierid(), insCard.getCardnumber(),
							insCard.getGroup(), insCard.getFirstnm(), insCard.getMidnm(), insCard.getLastnm(),
							insCard.getAltcard(), insCard.getPdate(), insCard.getBdate(), insCard.getEdate(),
							insCard.getBenefit(), insCard.getCardql(), insCard.getPlan(), insCard.getCover(),
							insCard.getEligible(), insCard.getWorkcompflag(), insCard.getDeleteCard(), secondaryKey });

		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while updating customer insurance:", e);
			throw new mscriptsException(e.getMessage(),
					"com.mscripts.externalrequesthandler.dao.NotificationDao-updateCustomerCard",
					mscriptsExceptionSeverity.High, e);
		}
	}

	@Override
	public void updateCustomerCardLink(String clientID, String customerID, InsuranceCardLink insCardLinkArr,
			String secondaryKey) throws mscriptsException {
		try {

			spInvoker.invokeSp("sp_ERH_updateCustomerInsuranceLink",
					new Object[] { clientID, customerID, insCardLinkArr.getCarrierid(), insCardLinkArr.getCardnumber(),
							insCardLinkArr.getGroup(), insCardLinkArr.getLevel(), insCardLinkArr.getRelat(),
							insCardLinkArr.getLocation(), insCardLinkArr.getAdc(), insCardLinkArr.getAltcard(),
							insCardLinkArr.getBchome(), insCardLinkArr.getBdate(), insCardLinkArr.getEdate(),
							insCardLinkArr.getPdate(), insCardLinkArr.getChild(), insCardLinkArr.getClinic(),
							insCardLinkArr.getDeleteTPLink(), insCardLinkArr.getEligible(), insCardLinkArr.getEligovr(),
							insCardLinkArr.getEmploy(), insCardLinkArr.getMedicaidID(), insCardLinkArr.getMedicaidInd(),
							insCardLinkArr.getNh(), insCardLinkArr.getOther(), insCardLinkArr.getPatBenNoAssign(),
							insCardLinkArr.getPlan(), insCardLinkArr.getQualCMSFacility(),
							insCardLinkArr.getResidence(), insCardLinkArr.getSc(), insCardLinkArr.getSeries(),
							insCardLinkArr.getStudent(), insCardLinkArr.getSpecial(), secondaryKey });

		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while updating customer insurance link record:", e);
			throw new mscriptsException(e.getMessage(),
					"com.mscripts.externalrequesthandler.dao.NotificationDao-updateCustomerCardLink",
					mscriptsExceptionSeverity.High, e);
		}
	}

	@Override
	public Map updateCustomerPrescriptions(String customerID, CustomerPrescription customerPres, String clientID,
			String messageDate, String secondaryKey,Map<String, Object> inputParams) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {

			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into upade customer  prescriptions  method for customerId " + customerID);
			}
			Map sendPickupRs = null;
			
			String cvtExtendedReminderTime = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtExtendedReminderTime");
			String cvtRefillStatusRequested = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRefillStatusRequested");
			String cvtRefillStatusFilled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRefillStatusFilled");
			String cvtRefillStatusCompleted = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRefillStatusCompleted");
			String cvtReminderUpperCutoffTime = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtReminderUpperCutoffTime");
			String cvtReminderLowerCutoffTime = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtReminderLowerCutoffTime");
			String cvtReminderRetryLogicLowerCutoff = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRxPickupReminderRetryLogicLowerCutoff");
			String cvtCommNameRxReadyInStore = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameRxReadyInStore");
			String cvAdmin = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvAdmin");
			String cvIsLegacyStorePresent = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvIsLegacyStorePresent");
			String cvHideExpiredZeroRefillPrescriptions = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvHideExpiredZeroRefillPrescriptions");
			String cvtInternationalCode = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtInternationalCode");
			String cvisEpsDisabledStoreEligibleForPickup = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvisEpsDisabledStoreEligibleForPickup");
			String cvisEpsEnabledStoreEligibleForPickup = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvisEpsEnabledStoreEligibleForPickup");
			String cvIsIgnoreWillCallFlag = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvIsIgnoreWillCallFlag");
			String cvRxPickupFilledExpiryPeriod = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRxPickupFilledExpiryPeriod");
			String cvtCommNameRxReadyInStoreWithCopay = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameRxReadyInStoreWithCopay");
			String cvCopayEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, "cvCopayEnabled");
			String cvtCommNameRxReadyInStoreFinal = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameRxReadyInStoreFinal");
			String cvtCommNameRxReadyDeliveryLinkFinal = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameRxReadyDeliveryLinkFinal"); 
			String cvRxAdjudicatedStatus = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvRxAdjudicatedStatus");
			String cvIsAdjudicationMessageEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvIsAdjudicationMessageEnabled");
			String cvtCommNameRxAdjudicationComplete = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameRxAdjudicationComplete");
			String cvIsPartialFillCommunicationEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvIsPartialFillCommunicationEnabled");
			String cvtCommNameRxRejected = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameRxRejected");
			String cvtCommNameOutOfStock = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtCommNameOutOfStock");
			String cvNumberOfHoursToConsiderForBackDatedReadyMsgs = ConfigReader.readConfig(clientID,
					Constants.DEFAULT_LANGUAGE, "cvNumberOfHoursToConsiderForBackDatedReadyMsgs");
			String cvtCommNamePartialFill = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					ConfigKeys.CVTCOMMNAMEPARTIALFILL);
			String cvPickupReminderMappingId = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, Constants.CVPICKUPREMINDERMAPPINGID);
			// MCE-519 Carry existing drug preferences to replacement
			// prescription
			String cvIsCarryOverDosagePrefEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvIsCarryOverDosagePrefEnabled");
			String cvtRxInProcessResetLimit = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRxInProcessResetLimit");
			String cvSendRxReadyMsgOnReceivingEopnReadyUpdate = ConfigReader.readConfig(clientID,
					Constants.DEFAULT_LANGUAGE, "cvSendRxReadyMsgOnReceivingEopnReadyUpdate");
			String cvtCommNameRxReadyWithCopayDeliveryLink =ConfigReader.readConfig(clientID,
					Constants.DEFAULT_LANGUAGE, "cvtCommNameRxReadyWithCopayDeliveryLink");
			String cvtPickupRestockingLimit = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtPickupReminderRestockingLimit");
			String cvTotalDaysOfSupply = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, ConfigKeys.CV_TOTAL_DAYS_OF_SUPPLY);
			String cvPartialDaysOfSupply = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, ConfigKeys.CV_PARTIAL_DAYS_OF_SUPPLY);
			
			Map<String, String> configMap = new HashMap<>();
			configMap.put("cvTotalDaysOfSupply", String.valueOf(cvTotalDaysOfSupply));
			configMap.put("cvPartialDaysOfSupply", String.valueOf(cvPartialDaysOfSupply));
			ObjectMapper mapper = new ObjectMapper();
			String configJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(configMap);
			
			Map rxStatus = new HashMap<>();
			try{
				rxStatus = spInvoker.invokeSp_Map("sp_ERH_updateCustomerPrescription",
					new Object[] { clientID, customerID, customerPres.getRxNum(), customerPres.getSgCode(),
							customerPres.getSched(), customerPres.getRefAut(), customerPres.getRefRem(),
							customerPres.getFirst(), customerPres.getExpire(), customerPres.getSigText(),
							customerPres.getPrescribingDocName(), customerPres.getPrescDrugName(),
							customerPres.getPrescDrugNDC(), customerPres.getPrescDrugGPI(), customerPres.getFilled(),
							customerPres.getQuantity(), customerPres.getDays(), customerPres.getAnticipatedRefillDate(),
							customerPres.getStoreNCPDP(), customerPres.getPrescribingDocLname(),
							customerPres.getPrescribingDocMname(), customerPres.getPrescribingDocFname(),
							customerPres.getPrescribingDocCity(), customerPres.getPrescribingDocState(),
							customerPres.getPrescribingDocZip(), customerPres.getPrescribingDocDEA(),
							customerPres.getPrescribingDocAreaCode(), customerPres.getPrescribingDocPhone(),
							customerPres.getPrescribingDocFaxAreaCode(), customerPres.getPrescribingDocFaxPhone(),
							customerPres.getTransfer(), customerPres.getNewrxnum(), customerPres.getOldrxnum(),
							customerPres.getDeleteRx(), customerPres.getLatestTxNumber(), customerPres.getRxStatus(),
							customerPres.getTxStatus(), customerPres.getLatestSoldDate(),
							customerPres.getWillCallReady(), customerPres.getDeleteTx(), customerPres.getWhyDeact(),
							customerPres.getPrefill(), customerPres.getDispDrugName(), customerPres.getDispDrugNDC(),
							customerPres.getDispDrugGPI(), cvtExtendedReminderTime, cvtRefillStatusRequested,
							cvtRefillStatusFilled, cvtRefillStatusCompleted,
							cvtReminderUpperCutoffTime, cvtReminderLowerCutoffTime, cvtReminderRetryLogicLowerCutoff,
							cvtPickupRestockingLimit, cvtCommNameRxReadyInStore, cvAdmin,
							cvIsLegacyStorePresent, cvHideExpiredZeroRefillPrescriptions, cvtInternationalCode,
							cvisEpsEnabledStoreEligibleForPickup, cvisEpsDisabledStoreEligibleForPickup,
							cvIsIgnoreWillCallFlag, cvRxPickupFilledExpiryPeriod, cvtCommNameRxReadyInStoreWithCopay,
							customerPres.getCopay(), cvCopayEnabled, customerPres.getPrescribingDocAddress1(),
							customerPres.getPrescribingDocAddress2(), cvtCommNameRxReadyInStoreFinal,
							cvRxAdjudicatedStatus, cvIsAdjudicationMessageEnabled, cvtCommNameRxAdjudicationComplete,
							cvtCommNameRxRejected, cvtCommNameOutOfStock,
							cvIsCarryOverDosagePrefEnabled, cvNumberOfHoursToConsiderForBackDatedReadyMsgs, messageDate,
							cvtCommNamePartialFill, secondaryKey, cvSendRxReadyMsgOnReceivingEopnReadyUpdate, (Boolean)inputParams.get("isTextDeliveryAllowed"),
								cvtCommNameRxReadyWithCopayDeliveryLink, cvtCommNameRxReadyDeliveryLinkFinal,
								cvPickupReminderMappingId, cvIsPartialFillCommunicationEnabled, configJson });
							}catch (DuplicateKeyException e) {
				/*If simultaneous sync prescription flow is triggered, inserting duplicates will throw a DuplicateKeyException
				 *[BASE-272]
				 */
				LOGGER_NON_PHI.error("Duplicate Flow while processing updateCustomerPrescriptions DAO : {} ", e);
			}
			LOGGER_NON_PHI.info("Call moveTempValueToPatientTxn method to move median data to txn table");
			if (MscriptsStringUtils.compareStringEquals(cvIsAdjudicationMessageEnabled, "1")) {
				moveTempValueToPatientTxn(rxStatus, cvRxAdjudicatedStatus, clientID, customerID,
						customerPres.getRxNum(), customerPres.getStoreNCPDP());
			}
			try {
				if(rxStatus != null && !rxStatus.isEmpty()) {
					if (Constants.NUMERIC_TRUE_STRING.equals(rxStatus.get("newPrescription")) && Constants.NUMERIC_TRUE_STRING.equals(rxStatus.get("refillAllPrescriptions"))) {
	
						String startReminderPeriod = null;
						String numberOfReminders = null;
						String sendHour = null;
						String refillAllPrescriptions = null;
	
						Map preferencesMap = getCustomerReminderPreferences(clientID, customerID);
						if (preferencesMap != null && !preferencesMap.isEmpty()) {
							startReminderPeriod = preferencesMap.get("start_reminder_period") != null? preferencesMap.get("start_reminder_period").toString():null;
							numberOfReminders = preferencesMap.get("refill_reminder_count") != null? preferencesMap.get("refill_reminder_count").toString():null;
							refillAllPrescriptions = preferencesMap.get("refill_all_prescriptions") !=null? preferencesMap.get("refill_all_prescriptions").toString():null;
							sendHour = preferencesMap.get("send_reminder_hour") != null? preferencesMap.get("send_reminder_hour").toString():null;
	
						}
	
						Map<String, String> reminderDetails = getRefillReminderDetails(clientID, customerID);
						if (reminderDetails != null && !reminderDetails.isEmpty()) {
							sendHour = (reminderDetails.get("sendHour") != null ? reminderDetails.get("sendHour"):null) + ":" + 
										(reminderDetails.get("sendMinute") != null ? reminderDetails.get("sendMinute"):null);
							startReminderPeriod = reminderDetails.get("rStartReminderPeriod") != null? reminderDetails.get("rStartReminderPeriod"):null;
							numberOfReminders = reminderDetails.get("rNoOfReminders") != null? reminderDetails.get("rNoOfReminders"):null;
	
							addRefillReminderDetails(clientID, customerID, rxStatus.get("custPrescID").toString(), null,
									"1", startReminderPeriod, numberOfReminders, sendHour, refillAllPrescriptions,
									Constants.DEFAULT_LANGUAGE);
						} else {
							// no settings present use client + customer preferences
							// to populate records
							addRefillReminderDetails(clientID, customerID, rxStatus.get("custPrescID").toString(), null,
									"1", startReminderPeriod, numberOfReminders, sendHour, refillAllPrescriptions,
									Constants.DEFAULT_LANGUAGE);
	
						}
	
					}
				}
			} catch (mscriptsException mEx) {
				LOGGER_NON_PHI.error(
						"Error occured in while processing creating refill reminders for a newly added prescription",
						mEx);
			} catch (Exception ex) {
				LOGGER_NON_PHI.error(
						"Error occured in while processing creating refill reminders for a newly added prescription",
						ex);
			}

			return rxStatus;
		} catch (Exception e) {
			LOGGER_NON_PHI.error("Exception occured while updating customer prescriptions=", e);
			throw new MscriptsException(clientID, "Exception occurred while executing updateCustomerPrescriptions", e, errorSeverity);

		}

	}

	@Override
	public Map selectRxPickupReminderInstancesRecords(String clientID, String brpiID, String secondaryKey)
			throws mscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-selectRxPickupReminderInstancesRecords";
		mscriptsExceptionSeverity severity = mscriptsExceptionSeverity.High;
		if (LOGGER_NON_PHI.isInfoEnabled()) {
			LOGGER_NON_PHI.info("Entered into selectRxPickupReminderInstancesRecords  method for clientID " + clientID);
		}

		try {

			String cvtReminderRetryLogicLowerCutoff = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtRxPickupReminderRetryLogicLowerCutoff");
			String cvtRxPickupReminderRetryLogicUpperCutoff = ConfigReader.readConfig(clientID,
					Constants.DEFAULT_LANGUAGE, "cvtRxPickupReminderRetryLogicUpperCutoff");
			String cvtIsInstantaneousPickupReminderOn = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtIsInstantaneousPickupReminderOn");
			String cvtWSSLinkforEmails = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtWSSLinkforEmails");
			// String
			// cvtCommNameRxReadyInStore=ConfigReader.readConfig(clientID,Constants.DEFAULT_LANGUAGE,"cvtCommNameRxReadyInStore");
			String cvEnableHideRxNumber = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvEnableHideRxNumber");
			String cvApnsKeyStorePath = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvApnsKeyStorePath");
			String cvPickupReminderMappingId = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE, Constants.CVPICKUPREMINDERMAPPINGID);
			String cvIsPatientCommunicationConsentRequiredForText = ConfigReader.readConfig(clientID,
					Constants.DEFAULT_LANGUAGE, "cvIsPatientCommunicationConsentRequiredForText");
			return readInvoker.invokeSp_Map("sp_ERH_jobSendRxPickupReminders",
					new Object[] { clientID, brpiID, secondaryKey, cvtReminderRetryLogicLowerCutoff,
							cvtRxPickupReminderRetryLogicUpperCutoff, cvtIsInstantaneousPickupReminderOn,
							cvtWSSLinkforEmails, cvEnableHideRxNumber, cvApnsKeyStorePath,
							cvIsPatientCommunicationConsentRequiredForText, cvPickupReminderMappingId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while picking up reminders =", ex);
			throw new mscriptsException(ex.getMessage(), source, severity, ex);
		}

	}

	@Override
	public void updateBulkRxPickupErrorNotes(String clientID, String recordID, String errorNotes, SendCommunicationMode sendCommunication) throws MscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-updateBulkRxPickupErrorNotes";
		mscriptsExceptionSeverity severity = mscriptsExceptionSeverity.High;

		try {
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into updateBulkRxPickupErrorNotes method for clientID " + clientID);
			}
			String cvtReminderUpperCutoffTime = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtReminderUpperCutoffTime");
			String cvtReminderLowerCutoffTime = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtReminderLowerCutoffTime");
			String cvtExtendedReminderTime = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					"cvtExtendedReminderTime");
			spInvoker.invokeSp("sp_ERH_updateRxPickupInstanceErrors",
					new Object[] { clientID, recordID, errorNotes, sendCommunication.getSendSms(),
							sendCommunication.getSendApns(), sendCommunication.getSendEmail(),
							sendCommunication.getSendGcms(), sendCommunication.getSendIvr(), cvtReminderUpperCutoffTime,
							cvtReminderLowerCutoffTime, cvtExtendedReminderTime });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while updating bulk pick up error notes  =", ex);
			throw new MscriptsException(clientID,ex.getMessage(), source, ex, severity);
		}

	}

	@Override
	public void deleteBulkRxPickup(String clientID, String recordID) throws mscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-deleteBulkRxPickup";
		mscriptsExceptionSeverity severity = mscriptsExceptionSeverity.High;

		try {
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into delete bulk rx pick up method for clientID " + clientID);
			}
			spInvoker.invokeSp("sp_ERH_deleteRxPickupInstances", new Object[] { clientID, recordID });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while deleting bulk pick up   =", ex);
			throw new mscriptsException(ex.getMessage(), source, severity, ex);
		}

	}

	@Override
	public Map getCustomerReminderPreferences(String clientId, String customerId) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.NotificationDao-getCustomerReminderPreferences";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.Medium;
		try {
			LOGGER_NON_PHI.debug("Inside getCustomerReminderPreferences Dao");
			LOGGER_PHI.debug("Invoking sp_UT_getCustomerReminderPreferences with params clientID = {}, customerID = {}",
					clientId, customerId);
			Map<String, String> map = spInvoker.invokeSp_Map("sp_UT_getCustomerReminderPreferences",
					new Object[] { clientId, customerId });
			return map;
		} catch (mscriptsException mEx) {
			LOGGER_NON_PHI.error("Error occured in getCustomerReminderPreferences", mEx);
			throw mEx;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured in getCustomerReminderPreferences", ex);
			throw new mscriptsException(ex.getMessage(), errorSource, errorSeverity, ex);
		}
	}

	@Override
	public Map getRefillReminderDetails(String clientID, String customerID) throws mscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-getRefillReminderDetails";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {
			LOGGER_NON_PHI.trace("Invoking Query to get Refill Reminder Details with clientId : " + clientID
					+ " customerID : " + customerID);
			return spInvoker.invokeSp_Map("sp_OPH_getRefillReminderDetails", new Object[] { clientID, customerID });
		} catch (BadSqlGrammarException bsge) {
			LOGGER_NON_PHI.error("Invalid SQL Error. Please check logs", bsge);
			throw new mscriptsException(bsge.getMessage(), source, errorSeverity, bsge);
		} catch (IncorrectResultSizeDataAccessException ex) {
			// LOGGER_NON_PHI.error("Zero return from DB for Input params", ex);
			return null;
		} catch (TransientDataAccessResourceException ex) {
			// LOGGER_NON_PHI.error("Zero return from DB for Input params", ex);
			return null;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured while processing getRefillReminderDetails ", ex);
			throw new mscriptsException(ex.getMessage(), source, errorSeverity, ex);
		}
	}

	@Override
	public void addRefillReminderDetails(String clientID, String customerID, String prescriptionIdList,
			String additionalReminderDate, String reminderOn, String remindBefore, String noOfReminders,
			String sendHour, String refillAllPrescriptions, String language)
			throws mscriptsException, MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {
			LOGGER_NON_PHI.info("Inside addRefillReminderDetails DAO");
			LOGGER_NON_PHI.debug(
					"Invoking SP to add Refill Reminder Details :: prescriptionIdList {} ::additionalReminderDate {}  ::reminderOn {} ::remindBefore {} ::noOfReminders {} ::sendHour {} :: refillAllPrescriptions {}",
					prescriptionIdList, additionalReminderDate, reminderOn, remindBefore, noOfReminders, sendHour,
					refillAllPrescriptions);

			String cvRefillReminderLimit = ConfigReader.readConfig(clientID, language, "cvRefillReminderLimit");
			String cvPrescriptionExpiryPeriod = ConfigReader.readConfig(clientID, language,
					"cvPrescriptionExpiryPeriod");
			String cvtARDLowerLimit = ConfigReader.readConfig(clientID, language, "cvtARDLowerLimit");
			String cvRefillTypeInstore = ConfigReader.readConfig(clientID, language, "cvRefillTypeInstore");
			String cvRefillReminderUnit = ConfigReader.readConfig(clientID, language, "cvRefillReminderUnit");
			String cvComNameBothRefillInstore = ConfigReader.readConfig(clientID, language,
					"cvComNameBothRefillInstore");
			String cvIsMscriptsAutofillEnabled = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.cvIsMscriptsAutofillEnabled);
			String cvPercentageCutOff = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,"cvPercentageCutOff");
			String cvRefillReminderMappingID = ConfigReader.readConfig(clientID, Constants.DEFAULT_LANGUAGE,
					Constants.CVREFILLREMINDERMAPPINGID);
			
			spInvoker.invokeSp("sp_OPH_addRefillReminderDetails",
					new Object[] { clientID, customerID, prescriptionIdList, additionalReminderDate, reminderOn,
							remindBefore, noOfReminders, sendHour, refillAllPrescriptions, cvRefillReminderLimit,
							cvPrescriptionExpiryPeriod, cvtARDLowerLimit, cvRefillTypeInstore, cvRefillReminderUnit,
							cvComNameBothRefillInstore, cvIsMscriptsAutofillEnabled,cvPercentageCutOff, cvRefillReminderMappingID });

		} catch (BadSqlGrammarException bsge) {
			LOGGER_NON_PHI.error("Invalid SQL Error. Please check logs", bsge);
			throw new MscriptsException(clientID, "Invalid SQL Error. Please check logs", ErrorCodes.GENERIC_ERROR,
					bsge, errorSeverity);
		} catch (IncorrectResultSizeDataAccessException ex) {
			LOGGER_NON_PHI.error("Zero return from DB for Input params", ex);
		} catch (TransientDataAccessResourceException ex) {
			LOGGER_NON_PHI.error("Zero return from DB for Input params", ex);
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured while processing addRefillReminderDetails ::", ex);
			throw new MscriptsException(clientID, "Error occured while processing addingRefillReminderDetails",
					ErrorCodes.GENERIC_ERROR, ex, errorSeverity);
		}
	}

	@Override
	public void moveTempValueToPatientTxn(Map rxStatus, String cvRxAdjudicatedStatus, String clientID,
			String customerID, String rxNumber, String storeNCPDPId) throws mscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-moveTempValueToPatientTxn";
		mscriptsExceptionSeverity severity = mscriptsExceptionSeverity.High;

		try {
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI.info("Entered into moveTempValueToPatientTxn method for clientID " + clientID);
			}
			LOGGER_NON_PHI.info("Execute  CHECK_PATIENT_RX_TXN_VALUE_EXISTS query");
			Map<String, Object> patientTxnValueCount = queryInvoker
					.executeQueryForMap(QueryBuilder.CHECK_PATIENT_RX_TXN_VALUE_EXISTS, rxStatus.get("custPrescID"));
			if (patientTxnValueCount.get("totalCount").toString().equals("0")) {
				LOGGER_NON_PHI.info("Execute  INSERT_TEMP_VALUE_TO_PATIENT_RX_TXN query");
				queryInvoker.executeQuery(QueryBuilder.INSERT_TEMP_VALUE_TO_PATIENT_RX_TXN,
						new Object[] { customerID, customerID, cvRxAdjudicatedStatus, rxStatus.get("custPrescID") });
				LOGGER_NON_PHI.info("Execute  DELETE_PATIENT_RX_TXN_MEDIAN query");
				queryInvoker.executeQuery(QueryBuilder.DELETE_PATIENT_RX_TXN_MEDIAN,
						new Object[] { rxNumber, storeNCPDPId });
			} else {
				LOGGER_NON_PHI.info("Execute  UPDATE_TEMP_VALUE_TO_PATIENT_RX_TXN query");
				queryInvoker.executeQuery(QueryBuilder.UPDATE_TEMP_VALUE_TO_PATIENT_RX_TXN,
						new Object[] { rxStatus.get("custPrescID"), rxNumber, storeNCPDPId });
				LOGGER_NON_PHI.info("Execute  DELETE_PATIENT_RX_TXN_MEDIAN query");
				queryInvoker.executeQuery(QueryBuilder.DELETE_PATIENT_RX_TXN_MEDIAN,
						new Object[] { rxNumber, storeNCPDPId });
			}
			// Query to update the sync date for all Rxs under particular store
			// and for the customer
			queryInvoker.executeQuery(QueryBuilder.UPDATE_SYNC_DATE_ALL_RX_UNDER_STORE_FOR_CUSTOMER,
					new Object[] { rxStatus.get("custPrescID") });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while calling moveTempValueToPatientTxn method   =", ex);
			throw new mscriptsException(ex.getMessage(), source, severity, ex);
		}

	}

	@Override
	public void updateRxcomId(String sClientId, String sCustomerId, String sRxcomId) throws mscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-updateRxcomId";
		mscriptsExceptionSeverity severity = mscriptsExceptionSeverity.High;

		try {
			queryInvoker.executeQuery(QueryBuilder.UPDATE_RX_COM_ID, new Object[] { sRxcomId, "updateRxComId", sCustomerId });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while executing UPDATE_RX_COM_ID :: ", ex);
			throw new mscriptsException(ex.getMessage(), source, severity, ex);
		}
	}

	public SPInvoker getSpInvoker() {
		return spInvoker;
	}

	public void setSpInvoker(SPInvoker spInvoker) {
		this.spInvoker = spInvoker;
	}

	public Properties getTransactionAttributes() {
		return transactionAttributes;
	}

	public void setTransactionAttributes(Properties transactionAttributes) {
		this.transactionAttributes = transactionAttributes;
	}

	public DataSourceTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(DataSourceTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public SPInvoker getReadInvoker() {
		return readInvoker;
	}

	public void setReadInvoker(SPInvoker readInvoker) {
		this.readInvoker = readInvoker;
	}

	public QueryInvoker getQueryInvoker() {
		return queryInvoker;
	}

	public void setQueryInvoker(QueryInvoker queryInvoker) {
		this.queryInvoker = queryInvoker;
	}

	@Override
	public void processPickupReminderForMigratedUser(String clientId, String customerId, int isMUPickupEligible,
			String cvAccelaretRxPickupType, int cvIsError) throws mscriptsException {
		String source = "com.mscripts.externalrequesthandler.dao.NotificationDao-processPickupReminderForMigratedUser";
		mscriptsExceptionSeverity severity = mscriptsExceptionSeverity.High;

		try {
			if (LOGGER_NON_PHI.isInfoEnabled()) {
				LOGGER_NON_PHI
						.info("Entered into processPickupReminderForMigratedUser method for customerId " + customerId);
			}
			spInvoker.invokeSp("sp_MU_processPickupReminderForMigratedUser",
					new Object[] { clientId, customerId, isMUPickupEligible, cvAccelaretRxPickupType, cvIsError });
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while deleting bulk pick up   =", ex);
		}

	}

	@Override
	public void insertOrUpdateJdbcData(String sqlString, Object[] objArr) throws mscriptsException {
		String errorSource = "com.mscripts.externalrequesthandler.dao.jdbc.NotificationDaoJdbc-insertOrUpdateJdbcData";
		try {
			queryInvoker.updateUsingSqlString(sqlString, objArr);
		} catch (Exception e) {
			throw new mscriptsException(null, errorSource, mscriptsExceptionSeverity.High, e);
		}
	}
	@Override
	public String updateMscriptsProxyAccessToken(String clientId, String customerId, String childCustomerId,
			String shaCode, String type, String language) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {
			LOGGER_PHI.debug(
					"Inside update mscripts proxy access token dao with params customerID = {} childCustomerId = {} shaCode = {} shaType = {} language = {}",
					customerId, childCustomerId, shaCode, type, language);
			spInvoker.invokeSp("sp_OPH_insertShaCode",
					new Object[] { clientId, customerId, childCustomerId, shaCode, type });
			if (Constants.NUMERIC_FALSE_STRING.equals(childCustomerId) && Constants.ORDER_PAGE.equalsIgnoreCase(type)) {
				return ConfigReader.readConfig(clientId, language, "cvPatientPortalUrl");
			}
			return ConfigReader.readConfig(clientId, language, "cvPatientPortalUrl");
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Error occured while procesing updateMscriptsProxyAccessToken", ex);
			throw new MscriptsException(clientId, ex.getMessage(), "ECOH001", ex, errorSeverity);
		}
	}
	
	@Override
	public Map getOrderUrl(String clientID, String customerID) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
				
			LOGGER_PHI.debug(
					"Inside getOrderUrl  for clientID {},  customerID = {}",clientID , customerID);
			try {
				
				Map<String, String> orderUrlMap=queryInvoker.invokeQueryMap(QueryBuilder.GET_VALID_ORDER_URL,
						new Object[] { clientID, customerID} );
				return orderUrlMap;
			} catch (Exception e) {
				LOGGER_NON_PHI.error("Error occured while procesing getOrderUrl", e);
				throw new MscriptsException(clientID, e.getMessage(), e, errorSeverity);
				
			}
			
	}
	
	@Override 
	public String getCommunicationName(String communicationId, String clientId) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		String communicationName = null;
		try {
			LOGGER_NON_PHI.debug("Inside getCommunicationName from notification dao method for clientId = {}, id= {}", clientId,
					communicationId);
			Map<String, String> communicationMap = getQueryInvoker().invokeQueryMap(QueryBuilder.GET_COMMUNICATION_NAME,
					new Object[] { communicationId, clientId });
				if (!MscriptsStringUtils.isMapEmptyOrNull(communicationMap)){
				communicationName = communicationMap.get("communication_name");
			}
			return communicationName;
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occurred with getCommunicationName = {}", ex);
			throw new MscriptsException(clientId, "Exception occurred with getCommunicationName", ex, errorSeverity);
		}
	}

	@Override
	public void updateRecordType(String clientId, String customerId, String recordType) throws MscriptsException {
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		try {
			queryInvoker.executeQuery(QueryBuilder.UPDATE_RECORD_TYPE, recordType, customerId, clientId);
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while executing updateRecordType :: ", ex);
			throw new MscriptsException(clientId, "Exception occurred while executing updateRecordType", ex, errorSeverity);
		}
	}
	
	@Override
	public Map<String, String> getRxDetailsForDaysQtyClients(String clientID, String customerPrescriptionId)
			throws MscriptsException {
		String errorString = "Exception occured while processing getRxDetailsForPaymentServiceForNonEOPNClients Dao";
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		Map<String, String> returnMap = null;
		try {
			returnMap = queryInvoker.invokeQueryMap(QueryBuilder.GET_RX_DETAILS_DAYS_QTY,
					new Object[] { customerPrescriptionId });
		} catch (BadSqlGrammarException bsge) {
			LOGGER_NON_PHI.error(errorString, bsge);
			throw new MscriptsException(clientID, errorString,bsge, errorSeverity);
		} catch (IncorrectResultSizeDataAccessException ex) {
			LOGGER_NON_PHI.error(errorString, ex);
			throw new MscriptsException(clientID, errorString,ex, errorSeverity);
		} catch (Exception ex) {
			LOGGER_NON_PHI.error(errorString, ex);
			throw new MscriptsException(clientID, errorString,ex, errorSeverity);
		}
		return returnMap;
	}
	
	
	@Override
	public void logInsuranceCardTransactions(String clientID, String customerID, String rxNumber,
			String storeNCPDP, CustomerTransactionFile[] customerTransactionFiles) throws MscriptsException {

		Map<String, String> rxRefillTxnMap = null;
		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;
		List<String> txtpId = new ArrayList<>();

		try {

			if (customerTransactionFiles != null && customerTransactionFiles.length > 1) {
				Collections.sort(Arrays.asList(customerTransactionFiles), new Comparator<CustomerTransactionFile>() {
					@Override
					public int compare(CustomerTransactionFile a, CustomerTransactionFile b) {
						try {
							Integer txnNumberA = Integer.valueOf(a.getTxnum());
							Integer txnNumberB = Integer.valueOf(b.getTxnum());
							return txnNumberB.compareTo(txnNumberA);
						} catch (Exception e) {
							LOGGER_NON_PHI.error("Error encountered while comparing INTEGER equivalent of {} and {} ",
									a.getTxnum(), b.getTxnum());
							return 0;
						}
					}
				});
			}

			LOGGER_NON_PHI.debug("Logging Insurance card details via login");

			rxRefillTxnMap = queryInvoker.invokeQueryMap(QueryBuilder.GET_RX_REFILL_TXN_ID_PDX,
					new Object[] { customerTransactionFiles[0].getTxnum(), rxNumber, storeNCPDP, clientID });

			List<CustomerTransactionTxtpfile> txtpInsert = new ArrayList<>();
			List<CustomerTransactionTxtpfile> txtpUpdate = new ArrayList<>();
			List<CustomerTransactionTxtpfile> txtpFile = customerTransactionFiles[0].getCustomerTransactionTxtpfile();

			if (txtpFile != null) {

				LOGGER_NON_PHI.debug("Logging Insurance card details via login");

				rxRefillTxnMap = queryInvoker.invokeQueryMap(QueryBuilder.GET_RX_REFILL_TXN_ID_PDX,
						new Object[] { customerTransactionFiles[0].getTxnum(), rxNumber, storeNCPDP, clientID });

				if (!rxRefillTxnMap.get("id").equalsIgnoreCase(Constants.NUMERIC_FALSE_STRING)) {

					String query = QueryBuilder.GET_TXTPFILE_ID_AND_COUNTER;
					List<Map<String, String>> docMap = queryInvoker.invokeQueryMapList(query,
							new Object[] { rxRefillTxnMap.get("id"), clientID });

					if (docMap.size() > 0) {
						for (int i = 0; i < txtpFile.size(); i++) {
							for (int j = 0; j < docMap.size(); j++) {
								if (txtpFile.get(i).getCounter().equals(docMap.get(j).get("counter"))) {
									txtpUpdate.add(txtpFile.get(i));
									txtpId.add(docMap.get(j).get("id"));
								}
							}
						}
					}

					txtpInsert.addAll(txtpFile);
					txtpInsert.removeAll(txtpUpdate);

					if (!txtpUpdate.isEmpty()) {
						updateInsuranceCard(clientID, txtpId, rxRefillTxnMap, txtpUpdate);
					}

					if (!txtpInsert.isEmpty()) {
						insertInsuranceCard(clientID, rxRefillTxnMap, txtpInsert);
					}
				}
			}
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured in logInsuranceCardTransactions Dao :", ex);
			throw new MscriptsException(clientID, ex.getMessage(), ex, errorSeverity);
		}
	};

	@Override
	public void updateInsuranceCard(final String clientID, final List<String> txtpId,
			final Map<String, String> rxRefillTxnMap, final List<CustomerTransactionTxtpfile> txtpUpdate)
			throws MscriptsException {

		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;

		try {
			String insertQuery = QueryBuilder.UPDATE_RX_REFILL_TXN_TXTPFILE;
			LOGGER_NON_PHI.debug("Updating Insurance card details via login");
			BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					CustomerTransactionTxtpfile txtpFile = txtpUpdate.get(i);
					ps.setString(1, clientID);
					ps.setString(2, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCounter()) ? null
							: txtpFile.getCounter());
					ps.setString(3,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getId()) ? null : txtpFile.getId());
					ps.setString(4,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlan()) ? null : txtpFile.getPlan());
					ps.setString(5,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCard()) ? null : txtpFile.getCard());
					ps.setString(6,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getGroup()) ? null : txtpFile.getGroup());
					ps.setString(7,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getSplit()) ? null : txtpFile.getSplit());
					ps.setString(8, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCopover()) ? null
							: txtpFile.getCopover());
					ps.setString(9, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getOrigtype()) ? null
							: txtpFile.getOrigtype());
					ps.setString(10, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getReverse()) ? null
							: txtpFile.getReverse());
					ps.setString(11,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCopay()) ? null : txtpFile.getCopay());
					ps.setString(12, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getBalance()) ? null
							: txtpFile.getBalance());
					ps.setString(13, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCollect()) ? null
							: txtpFile.getCollect());
					ps.setString(14, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getIncent()) ? null
							: txtpFile.getIncent());
					ps.setString(15, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpPrice()) ? null
							: txtpFile.getTxtpPrice());
					ps.setString(16, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpCost()) ? null
							: txtpFile.getTxtpCost());
					ps.setString(17, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpTax()) ? null
							: txtpFile.getTxtpTax());
					ps.setString(18, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpCompfee()) ? null
							: txtpFile.getTxtpCompfee());
					ps.setString(19, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpUpcharge()) ? null
							: txtpFile.getTxtpUpcharge());
					ps.setString(20, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getOthamt()) ? null
							: txtpFile.getOthamt());
					ps.setString(21,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPaid()) ? null : txtpFile.getPaid());
					ps.setString(22, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlanName()) ? null
							: txtpFile.getPlanName());
					ps.setString(23, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlanBin()) ? null
							: txtpFile.getPlanBin());
					ps.setString(24, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlanPCN()) ? null
							: txtpFile.getPlanPCN());
					ps.setString(25, Constants.UPDATE_RX_REFILL_TXN_TXTPFILE);
					ps.setString(26, txtpId.get(i));
				}

				@Override
				public int getBatchSize() {
					return null == txtpUpdate ? 0 : txtpUpdate.size();

				}
			};

			// bulk insert data into rx_refill_txn_txtpfile
			queryInvoker.invokeBatchUpdate(insertQuery, pss);

		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while processing updateInsuranceCard DAO: {}", ex);
			throw new MscriptsException(clientID, ex.getMessage(), ex, errorSeverity);
		}

	};

	@Override
	public void insertInsuranceCard(final String clientID, final Map<String, String> rxRefillTxnMap,
			final List<CustomerTransactionTxtpfile> txtpInsert) throws MscriptsException {

		mscriptsExceptionSeverity errorSeverity = mscriptsExceptionSeverity.High;

		try {
			String insertQuery = QueryBuilder.INSERT_RX_REFILL_TXN_TXTPFILE;
			LOGGER_NON_PHI.debug("Inserting Insurance card details");
			BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					CustomerTransactionTxtpfile txtpFile = txtpInsert.get(i);
					ps.setString(1, clientID);
					ps.setString(2, rxRefillTxnMap.get("id"));
					ps.setString(3, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCounter()) ? null
							: txtpFile.getCounter());
					ps.setString(4,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getId()) ? null : txtpFile.getId());
					ps.setString(5,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlan()) ? null : txtpFile.getPlan());
					ps.setString(6,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCard()) ? null : txtpFile.getCard());
					ps.setString(7,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getGroup()) ? null : txtpFile.getGroup());
					ps.setString(8,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getSplit()) ? null : txtpFile.getSplit());
					ps.setString(9, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCopover()) ? null
							: txtpFile.getCopover());
					ps.setString(10, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getOrigtype()) ? null
							: txtpFile.getOrigtype());
					ps.setString(11, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getReverse()) ? null
							: txtpFile.getReverse());
					ps.setString(12,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCopay()) ? null : txtpFile.getCopay());
					ps.setString(13, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getBalance()) ? null
							: txtpFile.getBalance());
					ps.setString(14, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getCollect()) ? null
							: txtpFile.getCollect());
					ps.setString(15, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getIncent()) ? null
							: txtpFile.getIncent());
					ps.setString(16, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpPrice()) ? null
							: txtpFile.getTxtpPrice());
					ps.setString(17, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpCost()) ? null
							: txtpFile.getTxtpCost());
					ps.setString(18, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpTax()) ? null
							: txtpFile.getTxtpTax());
					ps.setString(19, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpCompfee()) ? null
							: txtpFile.getTxtpCompfee());
					ps.setString(20, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getTxtpUpcharge()) ? null
							: txtpFile.getTxtpUpcharge());
					ps.setString(21, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getOthamt()) ? null
							: txtpFile.getOthamt());
					ps.setString(22,
							MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPaid()) ? null : txtpFile.getPaid());
					ps.setString(23, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlanName()) ? null
							: txtpFile.getPlanName());
					ps.setString(24, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlanBin()) ? null
							: txtpFile.getPlanBin());
					ps.setString(25, MscriptsStringUtils.isStringEmptyOrNull(txtpFile.getPlanPCN()) ? null
							: txtpFile.getPlanPCN());
					ps.setString(26, Constants.INSERT_RX_REFILL_TXN_TXTPFILE);
					ps.setString(27, Constants.INSERT_RX_REFILL_TXN_TXTPFILE);
				}

				@Override
				public int getBatchSize() {
					return null == txtpInsert ? 0 : txtpInsert.size();

				}
			};

			// bulk insert data into rx_refill_txn_txtpfile
			queryInvoker.invokeBatchUpdate(insertQuery, pss);
		} catch (Exception ex) {
			LOGGER_NON_PHI.error("Exception occured while processing insertInsuranceCard DAO: {}", ex);
			throw new MscriptsException(clientID, ex.getMessage(), ex, errorSeverity);
		}
	}
}
