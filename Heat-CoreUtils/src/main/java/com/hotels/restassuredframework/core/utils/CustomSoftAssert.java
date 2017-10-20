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
package com.hotels.restassuredframework.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;
import org.testng.collections.Maps;

/**
 * This object is a little refactoring of the testng SoftAssert implementation.
 * The refactoring has been necessary in order to retrieve also the errors (even
 * if not blocking) to use them in the logs.
 */
public class CustomSoftAssert extends Assertion {

    // LinkedHashMap to preserve the order
    private Map<AssertionError, IAssert> errorsRetrieved = Maps.newLinkedHashMap();

    @Override
    public void executeAssert(IAssert a) {
        errorsRetrieved = new HashMap<>();
        try {
            a.doAssert();
        } catch (AssertionError ex) {
            onAssertFailure(a, ex);
            errorsRetrieved.put(ex, a);
        }
    }

    public Map<AssertionError, IAssert> getErrorsRetrieved() {
        return errorsRetrieved;
    }

}
