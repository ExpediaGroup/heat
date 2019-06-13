/**
 * Copyright (C) 2015-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.heat.core.utils;

import java.math.BigDecimal;

import com.hotels.heat.core.utils.log.LoggingUtils;


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
