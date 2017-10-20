package com.hotels.restassuredframework.core.specificexception;

/**
 * Specific exception for heat test framework.
 */
public class HeatException extends Error {

    public HeatException(String exceptionMessage) {
        super(exceptionMessage);
    }
    public HeatException(String exceptionMessage, Throwable cause) {
        super(exceptionMessage, cause);
    }
}
