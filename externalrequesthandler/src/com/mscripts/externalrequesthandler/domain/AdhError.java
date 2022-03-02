/*
 * Property of mscripts, LLC 2016
 */
package com.mscripts.externalrequesthandler.domain;

/**
 * @author Manigandan Shri <mshri@mscripts.com>
 */
public class AdhError {

    private String message;
    private String type;
    private String code;

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
}
