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
