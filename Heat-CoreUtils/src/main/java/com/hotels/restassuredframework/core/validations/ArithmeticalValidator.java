/**
 * Copyright (C) 2015-2017 Expedia Inc.
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
package com.hotels.restassuredframework.core.validations;


import com.hotels.restassuredframework.core.handlers.AssertionHandler;
import com.hotels.restassuredframework.core.utils.InputConverter;
import com.hotels.restassuredframework.core.utils.log.LoggingUtils;

/**
 * Cases of the arithmetic checks.
 */
public class ArithmeticalValidator {

    public static final String MATH_OPERATOR_EQUAL_TO                   = "=";
    public static final String MATH_OPERATOR_NOT_EQUAL_TO               = "!=";
    public static final String MATH_OPERATOR_LESS_THAN                  = "<";
    public static final String MATH_OPERATOR_LESS_THAN_OR_EQUAL_TO      = "<=";
    public static final String MATH_OPERATOR_GREATER_THAN_OR_EQUAL_TO   = ">=";
    public static final String MATH_OPERATOR_GREATER_THAN               = ">";
    private static final String BOOLEAN_FORMAT_NUMBER                   = "boolean";
    private static final String INT_FORMAT_NUMBER                       = "int";
    private static final String DOUBLE_FORMAT_NUMBER                    = "double";

    private final InputConverter converter;
    private final AssertionHandler assertionHandler;
    private final LoggingUtils logUtils;

    /**
     * Constructor of Arithmetical Validator.
     * This class is useful in case of validation of any kind of numbers, since it supports several types (boolean, int, double), and it supports operations such as
     * =, !=, &gt;, &gt;=, &lt;, &lt;+
     * @param logUtils the object that contains test case information useful for logging
     */
    public ArithmeticalValidator(LoggingUtils logUtils) {
        this.logUtils = logUtils;
        this.converter = new InputConverter(logUtils);
        this.assertionHandler = new AssertionHandler();
    }
    /**
     * Mathematical checks.
     * @param isBlocking it is a boolean that indicates if it is necessary to use an hard assertion (true) or a soft one (false)
     * @param operation is the check (&lt;,=,&gt;,&gt;=,etc.).
     * @param stringToCheck is an item to validate from response A.
     * @param stringExpected is an item to validate from response B.
     * @param validationMessage is the description of the check.
     * @param formatOfTypeCheckInput A String represent the type of input for mathematical check (int, double, boolean)
     * @return true if the check is OK, false otherwise
     */
    public boolean mathematicalChecks(boolean isBlocking,
            String operation,
            String stringToCheck,
            String stringExpected,
            String validationMessage,
            String formatOfTypeCheckInput) {
        boolean isCheckOk = true;
        logUtils.trace("Requested operation '{}'", operation);
        switch (operation) {
        case MATH_OPERATOR_GREATER_THAN:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertTrue", validationMessage,
                    converter.convertToDouble(stringToCheck) > converter.convertToDouble(stringExpected));
            break;
        case MATH_OPERATOR_GREATER_THAN_OR_EQUAL_TO:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertTrue", validationMessage,
                    converter.convertToDouble(stringToCheck) >= converter.convertToDouble(stringExpected));
            break;
        case MATH_OPERATOR_LESS_THAN:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertTrue", validationMessage,
                    converter.convertToDouble(stringToCheck) < converter.convertToDouble(stringExpected));
            break;
        case MATH_OPERATOR_LESS_THAN_OR_EQUAL_TO:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertTrue", validationMessage,
                    converter.convertToDouble(stringToCheck) <= converter.convertToDouble(stringExpected));
            break;
        case MATH_OPERATOR_EQUAL_TO:
            isCheckOk = mathEqualCheck(isBlocking, stringToCheck, stringExpected, validationMessage, formatOfTypeCheckInput);
            break;
        case MATH_OPERATOR_NOT_EQUAL_TO:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertNotEquals", validationMessage,
                    converter.convertToInt(stringToCheck), converter.convertToInt(stringExpected));
            break;
        default:
            logUtils.trace("None of the operations matched, proceed with other validator classes.");
            break;
        }
        logUtils.trace("check execution: {}", isCheckOk ? "OK" : "NOT OK");
        return isCheckOk;
    }

    private boolean mathEqualCheck(boolean isBlocking, String stringToCheck, String stringExpected, String validationMessage, String formatOfTypeCheckInput) {
        boolean isCheckOk;
        String formatOfTypeCheck = formatOfTypeCheckInput != null ? formatOfTypeCheckInput : "int";
        logUtils.trace("Check type is '{}'", formatOfTypeCheck);
        switch (formatOfTypeCheck) {
        case DOUBLE_FORMAT_NUMBER:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertEquals", validationMessage,
                    converter.convertToBigDecimal(stringToCheck), converter.convertToBigDecimal(stringExpected));
            break;
        case INT_FORMAT_NUMBER:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertEquals", validationMessage,
                    converter.convertToInt(stringToCheck), converter.convertToInt(stringExpected));
            break;
        case BOOLEAN_FORMAT_NUMBER:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertEquals", validationMessage,
                    converter.convertToBoolean(stringToCheck), converter.convertToBoolean(stringExpected));
            break;
        default:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertTrue", validationMessage,
                    stringToCheck.equals(stringExpected));
            break;
        }
        return isCheckOk;
    }


}
