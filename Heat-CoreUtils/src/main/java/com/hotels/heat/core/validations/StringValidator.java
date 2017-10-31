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
package com.hotels.heat.core.validations;

import com.hotels.heat.core.handlers.AssertionHandler;
import com.hotels.heat.core.utils.log.LoggingUtils;

/**
 * String validator.
 */
public class StringValidator {
    public static final String STRING_OPERATOR_NOT_CONTAINS = "not contains";
    public static final String STRING_OPERATOR_CONTAINS = "contains";
    public static final String STRING_OPERATOR_EQUALS_TO = "equals to";
    public static final String STRING_OPERATOR_NOT_EQUALS_TO = "not equals to";

    private final AssertionHandler assertionHandler;
    private final LoggingUtils logUtils;

    /**
     * Constructor of the string validator.
     * It supports validations between two strings (equals and not equals).
     * @param logUtils logging utility
     */
    public StringValidator(LoggingUtils logUtils) {
        this.logUtils = logUtils;
        this.assertionHandler = new AssertionHandler();
    }


    /**
     * String validations.
     * @param isBlocking it is a boolean that indicates if it is necessary to use an hard assertion (true) or a soft one (false)
     * @param operation is the check (contains, does not contains, etc.).
     * @param stringToCheck is an item to validate from response A.
     * @param stringExpected is an item to validate from response B.
     * @param checkDescription is the description of the check.
     * @return true if the check is OK, false otherwise
     */
    public boolean stringEqualChecks(boolean isBlocking, String operation, String stringToCheck, String stringExpected, String checkDescription) {
        logUtils.trace("Requested operation '{}'", operation);
        boolean isCheckOk = true;
        switch (operation) {
        case StringValidator.STRING_OPERATOR_NOT_EQUALS_TO:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertNotEquals", checkDescription, stringToCheck, stringExpected);
            break;
        case StringValidator.STRING_OPERATOR_EQUALS_TO:
            isCheckOk = assertionHandler.assertion(isBlocking, "assertEquals", checkDescription, stringToCheck, stringExpected);
            break;
        default:
            logUtils.trace("None of the operations matched, proceed with other validator classes.");
            break;
        }
        logUtils.trace("check execution: {}", isCheckOk ? "OK" : "NOT OK");
        return isCheckOk;
    }
}
