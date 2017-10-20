package com.hotels.restassuredframework.core.utils;

import java.math.BigDecimal;

import com.hotels.restassuredframework.core.utils.log.LoggingUtils;


/**
 * Input converter for the validations.
 */
public class InputConverter {
    private static final String LOG_VALUE = "String to convert is : {} and its converted value is {}";
    private final LoggingUtils logUtils;

    public InputConverter(LoggingUtils logUtils) {
        this.logUtils = logUtils;
    }

    /**
     * Convert a String into Integer.
     * @param stringToInt string to convert
     * @return stringAsInt the input string as Integer
     */
    public Integer convertToInt(String stringToInt) {
        Integer stringAsInt = new Double(stringToInt).intValue();
        logUtils.trace(LOG_VALUE, stringToInt, stringAsInt);
        return stringAsInt;
    }

    /**
     * Convert a String into Double.
     * @param stringToDouble string to convert
     * @return stringAsDouble the input string as Double
     */
    public Double convertToDouble(String stringToDouble) {
        Double stringAsDouble = Double.parseDouble(stringToDouble);
        logUtils.trace(LOG_VALUE, stringToDouble, stringAsDouble);
        return stringAsDouble;
    }

    /**
     * Convert a String into BigDecimal.
     * @param stringToBigDecimal string to convert
     * @return stringAsBigDecimal the input string as BigDecimal
     */
    public BigDecimal convertToBigDecimal(String stringToBigDecimal) {
        BigDecimal stringAsBigDecimal = new BigDecimal(stringToBigDecimal.replaceAll("[^.\\d]", "")).setScale(1);
        logUtils.trace(LOG_VALUE, stringToBigDecimal, stringAsBigDecimal);
        return stringAsBigDecimal;
    }

    /**
     * Convert a String into Boolean.
     * @param stringToBoolean string to convert
     * @return stringAsBoolean the input string as Boolean
     */
    public Boolean convertToBoolean(String stringToBoolean) {
        Boolean stringAsBoolean = Boolean.valueOf(stringToBoolean);
        logUtils.trace(LOG_VALUE, stringToBoolean, stringAsBoolean);
        return stringAsBoolean;
    }
}
