/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mscripts.externalrequesthandler.dao.jdbc;

import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.mscripts.dao.SPInvoker;
import com.mscripts.externalrequesthandler.dao.TenForTenFeedDao;
import com.mscripts.utils.mscriptsException;
import com.mscripts.utils.mscriptsExceptionSeverity;

/**
 *
 * @author rhiresheddi
 */
public class TenForTenFeedDaoJdbc implements TenForTenFeedDao {

    private SPInvoker readInvoker;
    private DataSourceTransactionManager transactionManager;
    private Properties transactionAttributes;
    private static final Logger LOGGER_NON_PHI = LogManager.getLogger("non.phi." + TenForTenFeedDaoJdbc.class.getName());
   	private static final Logger LOGGER_PHI = LogManager.getLogger("phi." + TenForTenFeedDaoJdbc.class.getName());
    public Map feedTenForTen(String clientID, String feedFileName, String feedDate, String strUpdateAryValue) throws mscriptsException {
        Map map = null;
        try {
            
                LOGGER_NON_PHI.info("Entered into feedTenForTen  method with client ID={}" + clientID);
            
            map = readInvoker.invokeSp_Map("sp_ERH_feedTenForTenDetails", new Object[]{clientID, feedFileName, feedDate, strUpdateAryValue});
        } catch (Exception ex) {
             LOGGER_NON_PHI.error("Exception occured while updating customer email :", ex);
            throw new mscriptsException(ex.getMessage(), "com.mscripts.externalrequesthandler.dao.jdbc.GeneralDaoJdbc-feedTenForTen", mscriptsExceptionSeverity.High, ex);
        }
        return map/*.get("feedID").toString()*/;
    }

    public SPInvoker getReadInvoker() {
        return readInvoker;
    }

    public void setReadInvoker(SPInvoker readInvoker) {
        this.readInvoker = readInvoker;
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
}
