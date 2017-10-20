package com.hotels.restassuredframework.core.utils;

import java.util.Iterator;
import java.util.Map;

import org.testng.ITestContext;

import com.jayway.restassured.response.Response;


/**
 * Interface to handle all the runners present in the framework.
 */
public interface RunnerInterface {

    void beforeTestSuite(String propFilePath,
                                        String inputWebappName,
                                        ITestContext context);

    void beforeTestCase(String inputJsonParamPath,
            String enabledEnvironments,
            ITestContext context);

    Iterator<Object[]> providerJson();

    Map resolvePlaceholdersInTcParams(Map<String, Object> testCaseParams);

    void specificChecks(Map testCaseParams, Map<String, Response> rspRetrieved, String environment);

}
