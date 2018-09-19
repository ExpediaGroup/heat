/**
 * Copyright (C) 2015-2018 Expedia Inc.
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
package com.hotels.heat.core.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.ITestContext;

import com.hotels.heat.core.handlers.AssertionHandler;
import com.hotels.heat.core.handlers.OperationHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.specificexception.HeatException;
import com.hotels.heat.core.utils.TestCaseUtils;
import com.hotels.heat.core.utils.log.LoggingUtils;

import io.restassured.http.Header;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;



/**
 * This class models a basic check on test case, such as response code, json schema validation,
 * headers, cookies, and any specified check inside JSON test case definition.
 */
public class BasicChecks {

    public static final String CONDITION_JSON_ELEMENT = "condition";
    public static final String EXPECTED_VALUE_JSON_ELEMENT = "expectedValue";
    public static final String ACTUAL_VALUE_JSON_ELEMENT = "actualValue";
    public static final String EXPECTS_JSON_ELEMENT = "expects";
    public static final String RESPONSE_CODE_JSON_ELEMENT = "responseCode";
    public static final String JSON_SCHEMA_TO_CHECK_JSON_ELEMENT = "jsonSchemaToCheck";
    public static final String HEADER_CHECK_JSON_ELEMENT = "headerCheck";
    public static final String COOKIE_CHECK_JSON_ELEMENT = "cookieCheck";
    public static final String FIELD_CHECK_JSON_ELEMENT = "fieldCheck";
    public static final String DESCRIPTION_JSON_ELEMENT = "description";

    private Object responses; // in case of flow, this is the step responses
    private AssertionHandler assertionHandler;

    private final LoggingUtils logUtils;
    private final ITestContext testContext;

    private Map<Integer, Map<String, String>> retrievedParameters = new HashMap();

    /**
     * This is the constructor of the class BasicChecks.
     * @param testContext context of the test case. It contains infos for
     * logging
     */
    public BasicChecks(ITestContext testContext) {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.testContext = testContext;
        this.assertionHandler = new AssertionHandler();
    }

    /**
     * Method that orchestrate the checks to be done: - check on responses code -
 json schema validation on the responses retrieved - checks on specific
 fields of the responses retrieved - checks on the responses headers -
 checks on the responses cookies.
     *
     * @param testCaseParams it is a map retrieved from the json input file with
     * all input test data
     */
    public void commonTestValidation(Map testCaseParams) {
        boolean isTestOk = true;
        try {
            isTestOk &= checkResponseCode(testCaseParams);
            isTestOk &= jsonSchemaValidation(testCaseParams);
            isTestOk &= fieldChecks(testCaseParams);
            isTestOk &= headerChecks(testCaseParams);
            isTestOk &= cookieChecks(testCaseParams);
        } catch (Exception oEx) {
            logUtils.error("Exception: class {}, cause {}, message {}",
                oEx.getClass(), oEx.getCause(), oEx.getLocalizedMessage());
            throw new HeatException(logUtils.getExceptionDetails() + "localized message: '"
                + oEx.getLocalizedMessage() + "'");
        }

        if (!isTestOk) {
            logUtils.error("Common validation FAILED");
            throw new HeatException(logUtils.getExceptionDetails() + "Common validation FAILED");
        }
    }

    /**
     * Check of the responses code of the retrieved responses.
     * @param testCaseParams it is a map retrieved from the json input file with
     * all input test data
     * @return true if the check is ok, false otherwise
     */
    public boolean checkResponseCode(Map testCaseParams) {
        boolean isCheckOk = true;
        //isBlocking: if it is true, in case of failure the test stops running, otherwise it will go on running with the other checks (the final result does not change)
        boolean isBlocking = TestSuiteHandler.getInstance().getTestCaseUtils().getSystemParamOnBlocking();
        if (responses == null) {
            logUtils.error("response NULL");
            throw new HeatException(logUtils.getExceptionDetails() + "response NULL");
        }
        if (testCaseParams.containsKey(EXPECTS_JSON_ELEMENT)) {
            Map<String, String> expectedParams = (Map<String, String>) testCaseParams.get(EXPECTS_JSON_ELEMENT);
            String expectedRespCode = expectedParams.get(RESPONSE_CODE_JSON_ELEMENT);
            if (expectedRespCode != null) {
                String currentStatusCode = String.valueOf(((Response) responses).getStatusCode());
                logUtils.debug("check response code: current '{}' / expected '{}'", currentStatusCode, expectedRespCode);
                isCheckOk &= assertionHandler.assertion(isBlocking, "assertEquals", logUtils.getTestCaseDetails() + "{checkResponseCode} ", currentStatusCode, expectedRespCode);
            }
        } else {
            throw new HeatException(logUtils.getExceptionDetails() + "not any 'expects' found");
        }
        return isCheckOk;
    }

    /**
     * Check on the json schema of the retrieved responses; if the element
 "jsonSchemaToCheck" is not present in the json input file, the check will
 be simply skipped.
     *
     * @param testCaseParams it is a map retrieved from the json input file with
     * all input test data
     * @return true if the check is ok, false otherwise
     */
    private boolean jsonSchemaValidation(Map testCaseParams) {
        boolean isCheckOk = true;
        TestCaseUtils tcUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        //isBlocking: if it is true, in case of failure the test stops running, otherwise it will go on running with the other checks (the final result does not change)
        boolean isBlocking = tcUtils.getSystemParamOnBlocking();
        Map<String, String> expectedParams = (Map<String, String>) testCaseParams.get(EXPECTS_JSON_ELEMENT);
        String jsonSchemaPathToCheck = tcUtils.getRspJsonSchemaPath(expectedParams.get(JSON_SCHEMA_TO_CHECK_JSON_ELEMENT));
        if (expectedParams.containsKey(JSON_SCHEMA_TO_CHECK_JSON_ELEMENT) && jsonSchemaPathToCheck != null) {
            logUtils.debug("starting json schema validation");
            try {
                getClass().getResourceAsStream("/" + jsonSchemaPathToCheck).available();
                isCheckOk &= validateSchema(isBlocking, (Response) responses, jsonSchemaPathToCheck);
            } catch (Exception oEx) {
                assertionHandler.assertion(isBlocking, "fail",
                        logUtils.getTestCaseDetails() + "BasicChecks - jsonSchemaValidation -- the file '/" + jsonSchemaPathToCheck + "' does not exist");
            }
        } else {
            logUtils.debug("json schema validation disabled");
        }
        return isCheckOk;
    }

    /**
     * Json Schema validation.
     *
     * @param isBlocking boolean value: if it is true, in case of failure the
     * test stops running, otherwise it will go on running with the other checks
     * (the final result does not change)
     * @param resp Response retrieved
     * @param jsonSchemaPath path of the json schema to use for the check
     * @return true if the check is ok, false otherwise
     */
    private boolean validateSchema(boolean isBlocking, Response resp, String jsonSchemaPath) {
        boolean isCheckOk = true;
        try {
            JsonSchemaValidator validator = JsonSchemaValidator.matchesJsonSchemaInClasspath(jsonSchemaPath);
            resp.then().assertThat().body(validator);
            logUtils.debug("json schema validation OK");
        } catch (Exception oEx) {
            isCheckOk = false;
            logUtils.error("json schema validation NOT OK");
            assertionHandler.assertion(isBlocking, "fail", logUtils.getTestCaseDetails()
                    + "BasicChecks - validateSchema >> validation schema failed. -- exception: "
                    + oEx.getLocalizedMessage());
        }
        return isCheckOk;
    }

    /**
     * Check on the specific header element of the response; if the element
     * "headerCheck" is not present in the json input file, the check will be
     * simply skipped.
     * @param testCaseParams it is a map retrieved from the json input file with
     * all input test data
     */
    private boolean headerChecks(Map testCaseParams) {
        //isBlocking: if it is true, in case of failure the test stops running, otherwise it will go on running with the other checks (the final result does not change)
        boolean isCheckOk = true;
        boolean isBlocking = TestSuiteHandler.getInstance().getTestCaseUtils().getSystemParamOnBlocking();
        Map<String, Object> expectedParams = (Map<String, Object>) testCaseParams.get(EXPECTS_JSON_ELEMENT);
        if (expectedParams.containsKey(HEADER_CHECK_JSON_ELEMENT)) {
            Map<String, Object> headerCheckMaps = (Map<String, Object>) expectedParams.get(HEADER_CHECK_JSON_ELEMENT);
            Set<Map.Entry<String, Object>> headerEntries = headerCheckMaps.entrySet();

            for (Map.Entry<String, Object> headerEntry: headerEntries) {
                String headerName = headerEntry.getKey();
                if (headerEntry.getValue() instanceof String) {
                    String headerExpectedValue = (String) headerEntry.getValue();
                    String currentHeader = ((Response) responses).getHeader(headerName);

                    logUtils.debug("header check: currentHeader = '{}'/ headerExpectedValue = '{}'",
                        currentHeader, headerExpectedValue);
                    isCheckOk &= assertionHandler.assertion(isBlocking, "assertEquals", logUtils.getTestCaseDetails() + "check on header '" + headerName + "'-- ",
                            currentHeader, headerExpectedValue);
                } else {
                    ArrayList<String> headerExpectedValues = (ArrayList<String>) headerEntry.getValue();
                    logUtils.debug("header name '{}'", headerName);

                    List<Header> headers = ((Response) responses).getHeaders().getList(headerName);
                    isCheckOk &= assertionHandler.assertion(isBlocking, "assertEquals", logUtils.getTestCaseDetails() + "check on header '" + headerName + "'-- ",
                            headerExpectedValues.size(), headers.size());

                    for (String expectedHeader : headerExpectedValues) {
                        isCheckOk &= assertionHandler.assertion(isBlocking, "assertTrue", logUtils.getTestCaseDetails() + "check on header '" + headerName
                                        + "' The expected value is '" + expectedHeader + "' - The returned values are " + headers + "-- ",
                                headers.stream().anyMatch(header -> header.getValue().equals(expectedHeader))
                            );
                    }
                }
            }
        }
        return isCheckOk;
    }

    /**
     * Check on the specific header element of the response; if the element
     * "cookieCheck" is not present in the json input file, the check will be
     * simply skipped.
     *
     * @param testCaseParams it is a map retrieved from the json input file with
     * all input test data
     */
    private boolean cookieChecks(Map testCaseParams) {
        boolean isCheckOk = true;
        //isBlocking if it is true, in case of failure the test stops running, otherwise it will go on running with the other checks(the final result does not change)
        boolean isBlocking = TestSuiteHandler.getInstance().getTestCaseUtils().getSystemParamOnBlocking();
        Map<String, Object> expectedParams = (Map<String, Object>) testCaseParams.get(EXPECTS_JSON_ELEMENT);
        if (expectedParams.containsKey(COOKIE_CHECK_JSON_ELEMENT)) {
            Map<String, String> cookieCheckMaps = (Map<String, String>) expectedParams.get(COOKIE_CHECK_JSON_ELEMENT);
            Set<Map.Entry<String, String>> cookieEntries = cookieCheckMaps.entrySet();

            for (Map.Entry<String, String> cookieEntry: cookieEntries) {
                String cookieName = cookieEntry.getKey();
                String cookieExpectedValue = cookieEntry.getValue();
                logUtils.debug("cookie name '{}'", cookieName);
                String currentCookie = ((Response) responses).getCookie(cookieName);
                isCheckOk &= assertionHandler.assertion(isBlocking, "assertEquals", logUtils.getTestCaseDetails() + "check on cookie '" + cookieName + "'-- ",
                        currentCookie, cookieExpectedValue);
            }
        }
        return isCheckOk;
    }

    /**
     * This method reads all the EXPECTS_JSON_ELEMENT block from the json input
     * file and considers all its internal "fieldCheck" blocks.
     * @param testCaseParams it is the map representing the json input file.
     */
    private boolean fieldChecks(Map testCaseParams) {
        boolean isCheckOk = true;
        //isBlocking: If it is true, if the check fails, the test case will fail without executing the following checks.
        // If it is false, all the test case checks will be executed (if one of the checks will fail,
        // the test case will be 'red' but the report will show all the results about all the checks).
        boolean isBlocking = TestSuiteHandler.getInstance().getTestCaseUtils().getSystemParamOnBlocking();
        Map<String, Object> expectedParams = (Map<String, Object>) testCaseParams.get(EXPECTS_JSON_ELEMENT);
        if (expectedParams.containsKey(FIELD_CHECK_JSON_ELEMENT)) {
            List<Map<String, Object>> fieldCheckMaps = (ArrayList<Map<String, Object>>) expectedParams.get(FIELD_CHECK_JSON_ELEMENT);

            for (Map<String, Object> fieldCheck : fieldCheckMaps) {
                String checkBlockdescription = "";
                if (fieldCheck.containsKey(DESCRIPTION_JSON_ELEMENT)) {
                    checkBlockdescription = (String) fieldCheck.get(DESCRIPTION_JSON_ELEMENT);
                }
                boolean isConditionVerified = true;
                logUtils.trace("SINGLE CHECK BLOCK {}", fieldCheck.toString());
                if (fieldCheck.containsKey(CONDITION_JSON_ELEMENT)) {
                    logUtils.debug("{} --> There are some conditions for this check!", checkBlockdescription);
                    isConditionVerified = conditionVerification((ArrayList<Object>) fieldCheck.get(CONDITION_JSON_ELEMENT), checkBlockdescription);
                }
                if (isConditionVerified) {
                    isCheckOk &= singleBlockCheck(isBlocking, fieldCheck);
                } else {
                    logUtils.debug("condition not verified for: '{}'", getCheckDescription(fieldCheck));
                }
            }
        }
        return isCheckOk;
    }

    /**
     * Retireving of the MANDATORY "description" field present in each check
     * block.
     *
     * @param fieldCheck it is the map representing the single check block from
     * the json input file.
     * @return the string with the check description
     */
    private String getCheckDescription(Map fieldCheck) {
        String description = "";
        try {
            description = (String) fieldCheck.get(DESCRIPTION_JSON_ELEMENT);
        } catch (Exception oEx) {
            logUtils.error("Exception: class {}, cause {}, message {}",
                    oEx.getClass(), oEx.getCause(), oEx.getLocalizedMessage());
            throw new HeatException(logUtils.getExceptionDetails() + "It is not possible to retrieve the description of the check");
        }
        return description;
    }

    /**
     * Handling of the list of possible conditions to check before executing the
     * real checks; If all the conditions are ok, then the check will be run, no
     * otherwise.
     *
     * @param conditionArray array of the objects containing the conditions to
     * check
     * @return true if all the conditions are ok, false otherwise
     */
    private boolean conditionVerification(ArrayList<Object> conditionArray, String checkBlockDescription) {
        boolean isConditionVerified = true;
        logUtils.debug("For the check '{}', there are {} conditions to verify",
                checkBlockDescription, conditionArray.size());
        logUtils.trace("{}", conditionArray.toString());
        Iterator itr = conditionArray.iterator();
        while (itr.hasNext()) {
            logUtils.debug("### Condition: ");
            isConditionVerified = isConditionVerified && singleBlockCheck(false, (Map<String, Object>) itr.next());
        }
        logUtils.debug("Conditions verified '{}'", isConditionVerified);
        return isConditionVerified;
    }

    /**
     * Method that orchestrates checks contained in each json input file check
     * block.
     *
     * @param isBlocking boolean. If it is true, if the check fails, the test
     * case will fail without executing the following checks. If it is false,
     * all the test case checks will be executed (if one of the checks will
     * fail, the test case will be 'red' but the report will show all the
     * results about all the checks).
     * @param fieldCheck it is the map representing the single check block from
     * the json input file.
     * @return true if all the checks are ok, false otherwise
     */
    private boolean singleBlockCheck(boolean isBlocking, Map<String, Object> fieldCheck) {
        boolean checkResult = true;
        try {
            logUtils.trace("SINGLE BLOCK CHECK {}", fieldCheck.toString());
            if (fieldCheck.containsKey(ACTUAL_VALUE_JSON_ELEMENT) && fieldCheck.containsKey(EXPECTED_VALUE_JSON_ELEMENT)) {
                checkResult = executeCheck(isBlocking, fieldCheck, responses);
            } else {
                logUtils.error("modality not still supported: {}", fieldCheck.toString());
                throw new HeatException(logUtils.getExceptionDetails()
                        + "Not supported modality for the check '" + getCheckDescription(fieldCheck) + "'");
            }
        } catch (Exception oEx) {
            logUtils.error("Exception: class {}, cause {}, message {}",
                    oEx.getClass(), oEx.getCause(), oEx.getLocalizedMessage());
            throw new HeatException(logUtils.getExceptionDetails()
                    + "It is not possible to execute the check '" + getCheckDescription(fieldCheck) + "'");
        }
        return checkResult;
    }

    /**
     * Method that executes checks contained in each json input file check
     * block.
     *
     * @param isBlocking boolean. If it is true, if the check fails, the test
     * case will fail without executing the following checks. If it is false,
     * all the test case checks will be executed (if one of the checks will
     * fail, the test case will be 'red' but the report will show all the
     * results about all the checks).
     * @param fieldToCheck it is the map representing the single check block
     * from the json input file.
     * @param responses it is the response map retrieved after the request to
     * the service under test
     * @return true if the check is ok, false otherwise
     */
    public boolean executeCheck(boolean isBlocking, Map fieldToCheck, Object responses) {
        if (responses.getClass().equals(Response.class)) {
            logUtils.trace("BasicChecks - executeCheck --> 'responses' is a single response");
            this.responses = (Response) responses;
        } else {
            logUtils.trace("BasicChecks - executeCheck -- 'responses' is a map. Called by BasicMultipleChecks");
            this.responses = responses;
        }

        OperationHandler operationHandler = new OperationHandler(fieldToCheck, this.responses);
        operationHandler.setOperationBlocking(isBlocking);
        operationHandler.setFlowOutputParameters(retrievedParameters);
        return operationHandler.execute();
    }

    public ITestContext getContext() {
        return this.testContext;
    }

    public void setResponse(Response apiResponse) {
        responses = apiResponse;
    }

    public void setFlowOutputParameters(Map<Integer, Map<String, String>> retrievedParameters) {
        this.retrievedParameters = retrievedParameters;
    }

}
