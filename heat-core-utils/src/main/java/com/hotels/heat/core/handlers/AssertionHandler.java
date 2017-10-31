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
package com.hotels.heat.core.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.testng.asserts.Assertion;

import com.hotels.heat.core.specificexception.HeatException;
import com.hotels.heat.core.utils.CustomSoftAssert;
import com.hotels.heat.core.utils.log.LoggingUtils;


/**
 * Assertion Handler class to manage custom assertion (hard and soft).
 */
public class AssertionHandler {

    private final LoggingUtils logUtils;

    public AssertionHandler() {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
    }


    /**
     * The assertion method is useful to manage assertion hard and soft.
     * @param isBlocking it is a boolean that indicates if it is necessary to use an hard assertion (true) or a soft one (false)
     * @param assertType it is a string that represents the name of the method to call for a normal assert (example: assertEquals, ...)
     * @param message it is a string representing the message related to the assertion
     * @param currentObjs it is the array of objects to analyse in the assertion
     * @return a boolean, true is the assertion is ok, false otherwise.
     */
    public boolean assertion(boolean isBlocking, String assertType, String message, Object... currentObjs) {
        boolean checkOk = true;
        try {
            if (isBlocking) {
                Assertion hardAssertion = new Assertion();
                executeAssertion(assertType, message, hardAssertion, currentObjs);
            } else {
                CustomSoftAssert softAssertion = new CustomSoftAssert();
                executeAssertion(assertType, message, softAssertion, currentObjs);
                if (!softAssertion.getErrorsRetrieved().isEmpty()) {
                    checkOk = false;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException oEx) {
            throw new HeatException(logUtils.getExceptionDetails() + "exception '" + oEx.getClass() + "' with message '" + oEx.getLocalizedMessage() + "'");
        } catch (InvocationTargetException oEx) {
            //this is the exception raised in case of failure of the reflection-called method
            throw new HeatException(oEx.getCause().toString().split(":")[1].trim());
        }
        return checkOk;
    }

    private void executeAssertion(String assertType, String message, Assertion hardAssertion, Object[] currentObjs)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method assertionMethod;
        if (currentObjs == null || currentObjs.length == 0) {
            Class[] cArg = new Class[1];
            cArg[0] = String.class;
            assertionMethod = (hardAssertion.getClass()).getMethod(assertType, cArg);
            assertionMethod.invoke(hardAssertion, message);
        } else if (currentObjs.length == 1) {
            Class[] cArg = new Class[2];
            cArg[0] = boolean.class;
            cArg[1] = String.class;
            assertionMethod = (hardAssertion.getClass()).getMethod(assertType, cArg);
            assertionMethod.invoke(hardAssertion, currentObjs[0], message);
        } else if (currentObjs.length == 2) {
            Class[] cArg = new Class[3];
            cArg[0] = Object.class;
            cArg[1] = Object.class;
            cArg[2] = String.class;
            assertionMethod = (hardAssertion.getClass()).getMethod(assertType, cArg);
            assertionMethod.invoke(hardAssertion, currentObjs[0], currentObjs[1], message);
        } else {
            logUtils.trace("number of input objects not supported!");
        }
    }

}
